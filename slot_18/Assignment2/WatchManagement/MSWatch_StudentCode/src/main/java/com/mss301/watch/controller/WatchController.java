package com.mss301.watch.controller;

import com.mss301.watch.dto.ApiResponse;
import com.mss301.watch.dto.WatchRequest;
import com.mss301.watch.dto.WatchResponse;
import com.mss301.watch.dto.WatchSagaResponse;
import com.mss301.watch.service.WatchService;
import com.mss301.watch.service.saga.SagaResult;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// [TODO-6.14] Listing is public; writes are Admin only and report their Saga in X-Saga-Id
@RestController
@RequestMapping("/api/watches")
@RequiredArgsConstructor
public class WatchController {

    private static final String FAIL_AFTER_HEADER = "X-Saga-Fail-After-Step";

    private final WatchService watchService;

    @GetMapping
    public ApiResponse<List<WatchResponse>> findAll() {
        return ApiResponse.ok("Watches", watchService.findAll());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WatchResponse>> create(
            @Valid @RequestBody WatchRequest request,
            @RequestHeader(value = FAIL_AFTER_HEADER, required = false) Integer failAfterStep) {
        SagaResult result = watchService.create(request, failAfterStep);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(WatchSagaResponse.SAGA_HEADER, result.sagaId())
                .body(ApiResponse.ok("Watch created", watchService.findById(result.watchId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WatchResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody WatchRequest request,
            @RequestHeader(value = FAIL_AFTER_HEADER, required = false) Integer failAfterStep) {
        SagaResult result = watchService.update(id, request, failAfterStep);
        return ResponseEntity.ok()
                .header(WatchSagaResponse.SAGA_HEADER, result.sagaId())
                .body(ApiResponse.ok("Watch updated", watchService.findById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> delete(
            @PathVariable Integer id,
            @RequestHeader(value = FAIL_AFTER_HEADER, required = false) Integer failAfterStep) {
        SagaResult result = watchService.delete(id, failAfterStep);
        return ResponseEntity.ok()
                .header(WatchSagaResponse.SAGA_HEADER, result.sagaId())
                .body(ApiResponse.ok("Watch deleted", Map.of("watchId", id)));
    }
}
