package com.mss301.watch.controller;

import com.mss301.watch.dto.ApiResponse;
import com.mss301.watch.dto.SagaResponse;
import com.mss301.watch.entity.SagaState;
import com.mss301.watch.exception.ApiException;
import com.mss301.watch.service.saga.SagaTracker;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// [TODO-6.14] Saga monitoring, Admin only
@RestController
@RequestMapping("/api/sagas")
@RequiredArgsConstructor
public class SagaController {

    private static final int MAX_LIMIT = 100;

    private final SagaTracker tracker;

    @GetMapping
    public ApiResponse<List<SagaResponse>> findRecent(@RequestParam(defaultValue = "20") int limit) {
        int size = Math.clamp(limit, 1, MAX_LIMIT);
        return ApiResponse.ok("Sagas", tracker.findRecent(size).stream()
                .map(state -> SagaResponse.from(state, null))
                .toList());
    }

    @GetMapping("/{sagaId}")
    public ApiResponse<SagaResponse> findOne(@PathVariable String sagaId) {
        SagaState state = tracker.findState(sagaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Saga not found with id: " + sagaId));
        return ApiResponse.ok("Saga", SagaResponse.from(state, tracker.findSteps(sagaId)));
    }
}
