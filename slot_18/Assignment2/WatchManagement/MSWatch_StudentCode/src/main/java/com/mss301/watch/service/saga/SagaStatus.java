package com.mss301.watch.service.saga;

// [TODO-6.3] The six states of the Saga state machine
public enum SagaStatus {
    PENDING,
    EXECUTING,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}
