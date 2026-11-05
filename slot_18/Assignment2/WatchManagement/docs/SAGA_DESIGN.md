# Thiết kế Saga – Watch Management

MSWatch là **Orchestrator**, MSBrand là **Participant**. Mỗi thao tác thêm/sửa/xoá đồng hồ ghi vào 2 database: `DBWatch.Watches` và `DBBrand.BrandWatches`.

## 1. Vì sao không dùng `@Transactional` hay 2PC

- `@Transactional` chỉ quản lý được một database (DBWatch). Lời gọi HTTP sang MSBrand nằm ngoài transaction, nên DBBrand không được rollback theo.
- Nếu bọc cả orchestrator trong `@Transactional`, lỗi sẽ rollback luôn nhật ký Saga của DBWatch và giữ transaction mở trong suốt lời gọi mạng.
- 2PC phải khoá tài nguyên ở cả hai service trong lúc chờ, coordinator trở thành single point of failure, và driver/hạ tầng của bài không có giao thức này.
- Saga chia việc thành chuỗi local transaction. Bước nào đã commit mà cần huỷ thì chạy một compensating transaction.

## 2. Ba Saga

Quy ước: compensation chạy theo thứ tự **ngược** (LIFO) và chỉ cho những bước **đã commit**.

### CREATE_WATCH

| Bước | Local transaction | Database | Compensation | Idempotent |
|---|---|---|---|---|
| 1 | `CREATE_WATCH`: INSERT watch | DBWatch | `DELETE_WATCH` | ✔ `deleteIfExists`: watch đã mất thì không làm gì |
| 2 | `ASSIGN_BRAND`: gán watch vào brand | DBBrand | `UNASSIGN_BRAND` | ✔ Không có link thì trả 200 `NOTHING_TO_DO` |

### UPDATE_WATCH

| Bước | Local transaction | Database | Compensation | Idempotent |
|---|---|---|---|---|
| 1 | `UPDATE_WATCH`: ghi dữ liệu mới | DBWatch | `RESTORE_WATCH` | ✔ Ghi lại snapshot `before`, lặp lại vẫn cùng kết quả |
| 2 | `ASSIGN_BRAND`: chuyển sang brand mới | DBBrand | `ASSIGN_BRAND_BACK` | ✔ Upsert về brand cũ lấy từ snapshot `before` |

### DELETE_WATCH

| Bước | Local transaction | Database | Compensation | Idempotent |
|---|---|---|---|---|
| 1 | `UNASSIGN_BRAND`: gỡ watch khỏi brand | DBBrand | `ASSIGN_BRAND_BACK` | ✔ Upsert về brand cũ lấy từ snapshot `before` |
| 2 | `DELETE_WATCH`: xoá watch | DBWatch | Không có: **PIVOT** | Không áp dụng |

## 3. Pivot transaction

`DELETE_WATCH` là pivot: `WatchID` là cột `IDENTITY`, xoá cứng rồi thì không khôi phục được đúng ID cũ. Sau khi pivot commit, Saga chỉ có thể đi tiếp.

Pivot đứng **cuối** vì bước gọi qua mạng (`UNASSIGN_BRAND`) rủi ro cao hơn và vẫn bù trừ được. Nếu xoá watch trước rồi gỡ brand sau, khi MSBrand lỗi sẽ không có cách nào đưa watch về đúng ID, và `BrandWatches` còn link "mồ côi".

Lỗi giả lập (`X-Saga-Fail-After-Step: 2`) sau pivot bị bỏ qua và ghi `FAULT FAULT_INJECTION SKIPPED`.

## 4. Máy trạng thái

```
PENDING ──► EXECUTING ──────────────────────────────► COMPLETED
                │ (một bước lỗi)
                ▼
             FAILED ──► COMPENSATING ──► COMPENSATED
                              │
                              └─ compensation lỗi sau 3 lần: dừng ở COMPENSATING, xử lý thủ công
```

Khi một compensation hết 3 lần thử (backoff 1 giây), orchestrator ghi `COMPENSATION_HALTED` và **không** chạy tiếp các compensation còn lại. Nhờ vậy watch không bị xoá trong khi link ở MSBrand vẫn còn.

## 5. Luồng thất bại: CREATE với brand 999

```
MSWatch (Orchestrator)                 DBWatch                 MSBrand / DBBrand
   │ [1] CREATE_WATCH ───────────────► INSERT Watches (id=11) ✔ COMMIT
   │ [2] ASSIGN_BRAND ──── PUT /internal/brands/999/watches/11 ──► 404 Brand not found
   │      → FAILED
   │      → COMPENSATING
   │ [C1] DELETE_WATCH ──────────────► DELETE Watches id=11 ✔
   │      → COMPENSATED
   ▼  HTTP 400 {"sagaStatus":"COMPENSATED","failedStep":"ASSIGN_BRAND"}
```

`ASSIGN_BRAND` chưa commit nên không có `UNASSIGN_BRAND`. ID 11 bị nhảy cóc, chứng tỏ watch đã thực sự được commit rồi mới bị xoá.

## 6. Giới hạn đã biết

- **Thiếu Isolation:** trong lúc Saga chạy, `GET /api/watches` có thể thấy watch chưa được gán brand.
- **Read timeout:** nếu `ASSIGN_BRAND` hết thời gian chờ nhưng MSBrand đã commit, bước này không nằm trong stack nên link không được gỡ. Theo đề, chỉ bước đã commit mới vào stack; cách an toàn hơn là đưa compensation vào stack trước khi gọi vì compensation idempotent.
- **Dừng ở `COMPENSATING`:** bài chỉ dừng và chờ xử lý thủ công. Hệ thống thật cần job khôi phục đọc `SagaStates.Payload`.
