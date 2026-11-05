package com.mss301.watch.service.saga;

import com.mss301.watch.entity.SagaLog;
import com.mss301.watch.entity.SagaState;
import com.mss301.watch.repository.SagaLogRepository;
import com.mss301.watch.repository.SagaStateRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// [TODO-6.4] Every write commits on its own (REQUIRES_NEW), so the Saga history survives any step failure
@Service
@RequiredArgsConstructor
public class SagaTracker {

    private static final int MAX_TEXT_LENGTH = 1000;

    private final SagaStateRepository stateRepository;
    private final SagaLogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String start(SagaType type, Integer watchId, String payload) {
        LocalDateTime now = LocalDateTime.now();
        String sagaId = UUID.randomUUID().toString();
        stateRepository.save(SagaState.builder()
                .sagaId(sagaId)
                .sagaType(type)
                .watchId(watchId)
                .status(SagaStatus.PENDING)
                .payload(payload)
                .createdAt(now)
                .updatedAt(now)
                .build());
        return sagaId;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markStatus(String sagaId, SagaStatus status) {
        change(sagaId, state -> state.setStatus(status));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCurrentStep(String sagaId, String stepName) {
        change(sagaId, state -> state.setCurrentStep(stepName));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markWatchId(String sagaId, Integer watchId) {
        change(sagaId, state -> state.setWatchId(watchId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String sagaId, String reason) {
        change(sagaId, state -> {
            state.setStatus(SagaStatus.FAILED);
            state.setFailureReason(shorten(reason));
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStep(String sagaId, int stepNo, String stepName, SagaPhase phase, SagaStepResult result, String message) {
        logRepository.save(SagaLog.builder()
                .sagaId(sagaId)
                .stepNo(stepNo)
                .stepName(stepName)
                .phase(phase)
                .result(result)
                .message(shorten(message))
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public List<SagaState> findRecent(int limit) {
        return stateRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"))).getContent();
    }

    @Transactional(readOnly = true)
    public Optional<SagaState> findState(String sagaId) {
        return stateRepository.findBySagaId(sagaId);
    }

    @Transactional(readOnly = true)
    public List<SagaLog> findSteps(String sagaId) {
        return logRepository.findBySagaIdOrderByIdAsc(sagaId);
    }

    private void change(String sagaId, Consumer<SagaState> mutation) {
        SagaState state = stateRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new IllegalStateException("Unknown saga: " + sagaId));
        mutation.accept(state);
        state.setUpdatedAt(LocalDateTime.now());
    }

    private String shorten(String text) {
        return text != null && text.length() > MAX_TEXT_LENGTH ? text.substring(0, MAX_TEXT_LENGTH) : text;
    }
}
