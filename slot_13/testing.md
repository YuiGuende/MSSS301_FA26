# TODO-11 – Kiểm thử Saga bằng Postman

> Collection: [`Saga_Postman_Collection.json`](Saga_Postman_Collection.json) – **MSS301 - Saga Demo (Spring Boot 4.1.1)**
> Ảnh chụp màn hình đặt trong thư mục [`images/`](images/).

## 1. Chuẩn bị

```bash
cd slot_13
docker compose down -v          # reset dữ liệu về ban đầu (P1 = 10, P2 = 5, C1 = 5000, C2 = 100)
docker compose up -d --build
docker compose ps               # 3 DB (healthy) + 3 service Up
```

Postman → **Import** → chọn `Saga_Postman_Collection.json`.

| Biến | Giá trị |
|---|---|
| `orderUrl` | `http://localhost:8081` |
| `inventoryUrl` | `http://localhost:8082` |
| `paymentUrl` | `http://localhost:8083` |

**Ảnh 1 – `docker compose ps`**

![docker compose ps](images/01-docker-compose-ps.png)

---

## 2. Collection Runner: KB1 → KB5, KB7

Chuột phải collection → **Run collection** → **bỏ chọn** thư mục *"Thủ công (không chạy bằng Runner)"* → giữ thứ tự `0 → KB1 → KB2 → KB3 → KB4 → KB5 → KB7` → **Run**.

Kết quả mong đợi: tất cả test **Passed** (43 request, 83 assertion).

| KB | Request `POST /api/orders` | `status` / `sagaStatus` | `failureReason` | Thứ tự saga-logs |
|---|---|---|---|---|
| KB1 | `C1, P1, 2, 2000` | `CONFIRMED / COMPLETED` | `null` | CREATE_ORDER → RESERVE_INVENTORY → PROCESS_PAYMENT → CONFIRM_ORDER |
| KB2 | `C2, P1, 1, 1000` | `CANCELLED / COMPENSATED` | `402 - Số dư không đủ…` | CREATE_ORDER → RESERVE_INVENTORY → SAGA → COMPENSATION → CANCEL_ORDER |
| KB3 | `C1, P2, 100, 2000` | `CANCELLED / COMPENSATED` | `409 - Không đủ hàng…` | CREATE_ORDER → SAGA → CANCEL_ORDER |
| KB4 | `C1, P9, 1, 10` | `CANCELLED / COMPENSATED` | `404 - Không tìm thấy sản phẩm P9` | CREATE_ORDER → SAGA → CANCEL_ORDER |
| KB5 | `C9, P2, 1, 20` | `CANCELLED / COMPENSATED` | `404 - Không tìm thấy ví của C9` | giống KB2 |
| KB7 | release ×2, reserve lại, pay lại, refund order chưa trả | – | – | kho / ví không đổi |

**Ảnh 2 – Kết quả Collection Runner (tổng quan Passed)**

![Runner summary](images/02-runner-summary.png)

**Ảnh 3 – Kết quả Collection Runner (chi tiết từng test)**

![Runner detail](images/03-runner-detail.png)

**Ảnh 4 – `[KB1] 4. Saga logs`**

![KB1 saga logs](images/04-kb1-saga-logs.png)

**Ảnh 5 – `[KB2] 2. Đặt hàng` (CANCELLED / COMPENSATED, 402)**

![KB2 order](images/05-kb2-order.png)

**Ảnh 6 – `[KB2] 4. Saga logs`**

![KB2 saga logs](images/06-kb2-saga-logs.png)

**Ảnh 7 – `[KB3] 4. Saga logs` (không có COMPENSATION)**

![KB3 saga logs](images/07-kb3-saga-logs.png)

---

## 3. KB6 – payment-service bị tắt (thủ công)

1. Gửi `[KB6] 1. Snapshot kho & ví`.
2. `docker compose stop payment-service`
3. Gửi `[KB6] 2. Đặt hàng` → `CANCELLED / COMPENSATED`, `failureReason` bắt đầu bằng `Service không phản hồi: I/O error…`.
4. `docker compose start payment-service`
5. Gửi `[KB6] 3. Kiểm tra kho & ví` (P2 không đổi) và `[KB6] 4. Saga logs`.

**Ảnh 8 – `[KB6] 2. Đặt hàng`**

![KB6 order](images/08-kb6-order.png)

**Ảnh 9 – `[KB6] 4. Saga logs`**

![KB6 saga logs](images/09-kb6-saga-logs.png)

---

## 4. KB8 – Compensation cũng lỗi (tuỳ chọn, thủ công)

1. Khởi động lại order-service với độ trễ 15 giây trước bước thanh toán:
   - PowerShell: `$env:PAYMENT_DELAY_MS=15000; docker compose up -d order-service`
   - Git Bash: `PAYMENT_DELAY_MS=15000 docker compose up -d order-service`
2. Gửi `[KB8] 1. Snapshot kho & ví`, rồi gửi `[KB8] 2. Đặt hàng` (request treo ~15 giây).
3. **Trong 15 giây đó**: `docker compose stop payment-service inventory-service`
4. Response: `status = PENDING`, `sagaStatus = COMPENSATING`.
5. `docker compose start inventory-service payment-service`
6. Gửi `[KB8] 3. Kiểm tra kho bị treo` (P2 −1) và `[KB8] 4. Saga logs` (3 dòng `COMPENSATION FAILED`, không có `CANCEL_ORDER`).
7. Khôi phục: gửi `[KB8] 5. Khôi phục thủ công` và `[KB8] 6. Kiểm tra kho đã được trả`.
8. Tắt độ trễ: `$env:PAYMENT_DELAY_MS=0; docker compose up -d order-service`

**Ảnh 10 – `[KB8] 2. Đặt hàng` (PENDING / COMPENSATING)**

![KB8 order](images/10-kb8-order.png)

**Ảnh 11 – `[KB8] 4. Saga logs` (3 lần COMPENSATION FAILED)**

![KB8 saga logs](images/11-kb8-saga-logs.png)

---

## 5. Kết luận

| Kịch bản | Kết quả |
|---|---|
| KB1 – Happy path | ☐ Passed |
| KB2 – Thanh toán thất bại (402) | ☐ Passed |
| KB3 – Hết hàng (409) | ☐ Passed |
| KB4 – Sản phẩm không tồn tại (404) | ☐ Passed |
| KB5 – Ví không tồn tại (404) | ☐ Passed |
| KB6 – payment-service bị tắt | ☐ Passed |
| KB7 – Idempotent | ☐ Passed |
| KB8 – Compensation cũng lỗi | ☐ Passed |
