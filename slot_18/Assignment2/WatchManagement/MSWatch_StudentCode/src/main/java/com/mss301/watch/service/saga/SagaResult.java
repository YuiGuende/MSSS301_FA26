package com.mss301.watch.service.saga;

// [TODO-6.11] Outcome of a Saga that finished with COMPLETED
public record SagaResult(String sagaId, Integer watchId) {
}
