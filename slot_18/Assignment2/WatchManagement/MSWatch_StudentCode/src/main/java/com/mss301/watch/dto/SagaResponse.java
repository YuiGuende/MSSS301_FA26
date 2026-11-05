package com.mss301.watch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mss301.watch.entity.SagaLog;
import com.mss301.watch.entity.SagaState;
import com.mss301.watch.service.saga.SagaPhase;
import com.mss301.watch.service.saga.SagaStatus;
import com.mss301.watch.service.saga.SagaStepResult;
import com.mss301.watch.service.saga.SagaType;
import java.time.LocalDateTime;
import java.util.List;

// [TODO-6.14] Saga status with its ordered steps; the list endpoint leaves the steps out
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SagaResponse(
        String sagaId,
        SagaType sagaType,
        Integer watchId,
        SagaStatus status,
        String currentStep,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<Step> steps) {

    public static SagaResponse from(SagaState state, List<SagaLog> logs) {
        List<Step> steps = logs == null ? null : logs.stream().map(Step::from).toList();
        return new SagaResponse(
                state.getSagaId(),
                state.getSagaType(),
                state.getWatchId(),
                state.getStatus(),
                state.getCurrentStep(),
                state.getFailureReason(),
                state.getCreatedAt(),
                state.getUpdatedAt(),
                steps);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Step(
            Integer stepNo,
            String stepName,
            SagaPhase phase,
            SagaStepResult result,
            String message,
            LocalDateTime createdAt) {

        static Step from(SagaLog log) {
            return new Step(log.getStepNo(), log.getStepName(), log.getPhase(), log.getResult(), log.getMessage(), log.getCreatedAt());
        }
    }
}
