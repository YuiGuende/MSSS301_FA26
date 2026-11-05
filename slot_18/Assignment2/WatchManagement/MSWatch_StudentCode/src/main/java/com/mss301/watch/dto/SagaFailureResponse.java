package com.mss301.watch.dto;

import com.mss301.watch.service.saga.SagaStatus;

// [TODO-6.15] The data block of a failed Saga response
public record SagaFailureResponse(String sagaId, SagaStatus sagaStatus, String failedStep, String reason) {
}
