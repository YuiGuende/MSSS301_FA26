package com.mss301.watch.service.saga;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// [TODO-6.12] Simulates a crash right after step N commits; after the pivot the Saga must go on
@Slf4j
@Component
public class SagaFaultInjector {

    public static final String STEP_NAME = "FAULT_INJECTION";

    private final SagaTracker tracker;
    private final boolean enabled;

    public SagaFaultInjector(SagaTracker tracker, @Value("${saga.fault-injection.enabled}") boolean enabled) {
        this.tracker = tracker;
        this.enabled = enabled;
    }

    public void afterStep(String sagaId, int stepNo, Integer failAfterStep, boolean pivotCommitted) {
        if (!enabled || failAfterStep == null || failAfterStep != stepNo) {
            return;
        }
        if (pivotCommitted) {
            String message = "Fault after step " + stepNo + " skipped: pivot already committed";
            tracker.logStep(sagaId, stepNo, STEP_NAME, SagaPhase.FAULT, SagaStepResult.SKIPPED, message);
            log.warn("[SAGA {}] FAULT SKIPPED: {}", sagaId, message);
            return;
        }
        String message = "Fault injected after step " + stepNo;
        tracker.logStep(sagaId, stepNo, STEP_NAME, SagaPhase.FAULT, SagaStepResult.TRIGGERED, message);
        log.warn("[SAGA {}] FAULT TRIGGERED: {}", sagaId, message);
        throw new SagaFaultException(message);
    }
}
