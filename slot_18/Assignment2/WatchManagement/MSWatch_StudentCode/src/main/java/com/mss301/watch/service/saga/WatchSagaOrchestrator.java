package com.mss301.watch.service.saga;

import com.mss301.watch.client.BrandServiceClient;
import com.mss301.watch.exception.SagaExecutionException;
import com.mss301.watch.service.WatchData;
import com.mss301.watch.service.WatchLocalService;
import feign.FeignException;
import feign.RetryableException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Runs each Saga as a chain of local transactions. It deliberately has no transaction annotation:
 * a single transaction here would span the MSBrand calls and roll back the Saga history on failure.
 */
@Slf4j
@Service
public class WatchSagaOrchestrator {

    private static final String CREATE_WATCH = "CREATE_WATCH";
    private static final String UPDATE_WATCH = "UPDATE_WATCH";
    private static final String RESTORE_WATCH = "RESTORE_WATCH";
    private static final String DELETE_WATCH = "DELETE_WATCH";
    private static final String ASSIGN_BRAND = "ASSIGN_BRAND";
    private static final String ASSIGN_BRAND_BACK = "ASSIGN_BRAND_BACK";
    private static final String UNASSIGN_BRAND = "UNASSIGN_BRAND";
    private static final String COMPENSATION_HALTED = "COMPENSATION_HALTED";

    private final WatchLocalService localService;
    private final BrandServiceClient brandClient;
    private final SagaTracker tracker;
    private final SagaFaultInjector faultInjector;
    private final JsonMapper jsonMapper;
    private final int maxAttempts;
    private final long backoffMs;

    public WatchSagaOrchestrator(
            WatchLocalService localService,
            BrandServiceClient brandClient,
            SagaTracker tracker,
            SagaFaultInjector faultInjector,
            JsonMapper jsonMapper,
            @Value("${saga.compensation.max-attempts}") int maxAttempts,
            @Value("${saga.compensation.backoff-ms}") long backoffMs) {
        this.localService = localService;
        this.brandClient = brandClient;
        this.tracker = tracker;
        this.faultInjector = faultInjector;
        this.jsonMapper = jsonMapper;
        this.maxAttempts = maxAttempts;
        this.backoffMs = backoffMs;
    }

    // [TODO-6.11] (a) CREATE: insert the watch, then assign it to its brand
    public SagaResult createWatch(WatchData after, Integer failAfterStep) {
        String sagaId = tracker.start(SagaType.CREATE_WATCH, null, payload(null, after));
        AtomicReference<Integer> watchId = new AtomicReference<>();
        List<SagaStep> steps = List.of(
                SagaStep.of(CREATE_WATCH,
                        () -> {
                            watchId.set(localService.insert(after));
                            tracker.markWatchId(sagaId, watchId.get());
                        },
                        DELETE_WATCH, () -> localService.deleteIfExists(watchId.get())),
                SagaStep.of(ASSIGN_BRAND, () -> brandClient.assignWatch(after.brandId(), watchId.get()),
                        UNASSIGN_BRAND, () -> brandClient.unassignWatch(watchId.get())));
        execute(sagaId, steps, failAfterStep);
        return new SagaResult(sagaId, watchId.get());
    }

    // [TODO-6.11] (b) UPDATE: write the new data, then move the watch to the new brand
    public SagaResult updateWatch(Integer watchId, WatchData before, WatchData after, Integer failAfterStep) {
        String sagaId = tracker.start(SagaType.UPDATE_WATCH, watchId, payload(before, after));
        List<SagaStep> steps = List.of(
                SagaStep.of(UPDATE_WATCH, () -> localService.update(watchId, after),
                        RESTORE_WATCH, () -> localService.update(watchId, before)),
                SagaStep.of(ASSIGN_BRAND, () -> brandClient.assignWatch(after.brandId(), watchId),
                        ASSIGN_BRAND_BACK, () -> brandClient.assignWatch(before.brandId(), watchId)));
        execute(sagaId, steps, failAfterStep);
        return new SagaResult(sagaId, watchId);
    }

    // [TODO-6.11] (c) DELETE: the hard delete cannot be undone, so it is the pivot and runs last
    public SagaResult deleteWatch(Integer watchId, WatchData before, Integer failAfterStep) {
        String sagaId = tracker.start(SagaType.DELETE_WATCH, watchId, payload(before, null));
        List<SagaStep> steps = List.of(
                SagaStep.of(UNASSIGN_BRAND, () -> brandClient.unassignWatch(watchId),
                        ASSIGN_BRAND_BACK, () -> brandClient.assignWatch(before.brandId(), watchId)),
                SagaStep.pivot(DELETE_WATCH, () -> localService.delete(watchId)));
        execute(sagaId, steps, failAfterStep);
        return new SagaResult(sagaId, watchId);
    }

    // [TODO-6.9] Runs the steps in order; only a step that has committed goes on the stack
    private void execute(String sagaId, List<SagaStep> steps, Integer failAfterStep) {
        Deque<CommittedStep> committed = new ArrayDeque<>();
        boolean pivotCommitted = false;
        tracker.markStatus(sagaId, SagaStatus.EXECUTING);
        for (int i = 0; i < steps.size(); i++) {
            int stepNo = i + 1;
            SagaStep step = steps.get(i);
            tracker.markCurrentStep(sagaId, step.name());
            try {
                step.action().run();
            } catch (RuntimeException ex) {
                Failure failure = describe(ex);
                tracker.logStep(sagaId, stepNo, step.name(), SagaPhase.EXECUTE, SagaStepResult.FAILED, failure.reason());
                throw abort(sagaId, step.name(), failure, committed);
            }
            tracker.logStep(sagaId, stepNo, step.name(), SagaPhase.EXECUTE, SagaStepResult.SUCCESS, null);
            log.info("[SAGA {}] step {} {} -> SUCCESS", sagaId, stepNo, step.name());
            if (step.isPivot()) {
                pivotCommitted = true;
            } else {
                committed.push(new CommittedStep(stepNo, step));
            }
            try {
                faultInjector.afterStep(sagaId, stepNo, failAfterStep, pivotCommitted);
            } catch (SagaFaultException ex) {
                throw abort(sagaId, SagaFaultInjector.STEP_NAME, describe(ex), committed);
            }
        }
        tracker.markStatus(sagaId, SagaStatus.COMPLETED);
        log.info("[SAGA {}] finished with status {}", sagaId, SagaStatus.COMPLETED);
    }

    private SagaExecutionException abort(String sagaId, String failedStep, Failure failure, Deque<CommittedStep> committed) {
        log.error("[SAGA {}] FAILED at {}: {}", sagaId, failedStep, failure.reason());
        tracker.markFailed(sagaId, failure.reason());
        tracker.markStatus(sagaId, SagaStatus.COMPENSATING);
        if (!compensate(sagaId, committed)) {
            log.error("[SAGA {}] finished with status {}", sagaId, SagaStatus.COMPENSATING);
            return new SagaExecutionException(sagaId, SagaStatus.COMPENSATING, failedStep, failure.reason(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        tracker.markStatus(sagaId, SagaStatus.COMPENSATED);
        log.info("[SAGA {}] finished with status {}", sagaId, SagaStatus.COMPENSATED);
        return new SagaExecutionException(sagaId, SagaStatus.COMPENSATED, failedStep, failure.reason(), failure.httpStatus());
    }

    // [TODO-6.10] Undoes committed steps in LIFO order and stops at the first one that cannot be undone
    private boolean compensate(String sagaId, Deque<CommittedStep> committed) {
        while (!committed.isEmpty()) {
            CommittedStep entry = committed.pop();
            if (!compensateWithRetry(sagaId, entry)) {
                tracker.logStep(sagaId, entry.stepNo(), COMPENSATION_HALTED, SagaPhase.COMPENSATE, SagaStepResult.FAILED,
                        "Stopped at " + entry.step().compensationName() + "; manual recovery is required");
                return false;
            }
        }
        return true;
    }

    // [TODO-6.10] Retries a temporary failure; compensations are idempotent, so repeating them is safe
    private boolean compensateWithRetry(String sagaId, CommittedStep entry) {
        SagaStep step = entry.step();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                step.compensation().run();
                tracker.logStep(sagaId, entry.stepNo(), step.compensationName(), SagaPhase.COMPENSATE, SagaStepResult.SUCCESS, null);
                log.info("[SAGA {}] compensate step {} {} -> SUCCESS", sagaId, entry.stepNo(), step.compensationName());
                return true;
            } catch (RuntimeException ex) {
                String message = "Attempt " + attempt + "/" + maxAttempts + ": " + describe(ex).reason();
                tracker.logStep(sagaId, entry.stepNo(), step.compensationName(), SagaPhase.COMPENSATE, SagaStepResult.FAILED, message);
                log.warn("[SAGA {}] compensate step {} {} -> FAILED ({})", sagaId, entry.stepNo(), step.compensationName(), message);
            }
            if (attempt < maxAttempts && !waitBeforeRetry()) {
                return false;
            }
        }
        return false;
    }

    private boolean waitBeforeRetry() {
        try {
            Thread.sleep(backoffMs);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // [TODO-6.15] Maps a step failure to its reason and HTTP status
    private Failure describe(RuntimeException ex) {
        if (ex instanceof SagaFaultException) {
            return new Failure(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (ex instanceof RetryableException) {
            return new Failure("MSBrand is unreachable: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (ex instanceof FeignException feign) {
            String reason = "MSBrand returned " + feign.status() + ": " + remoteMessage(feign);
            HttpStatus status = feign.status() == HttpStatus.NOT_FOUND.value()
                    ? HttpStatus.BAD_REQUEST
                    : HttpStatus.SERVICE_UNAVAILABLE;
            return new Failure(reason, status);
        }
        return new Failure(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String remoteMessage(FeignException ex) {
        try {
            String message = jsonMapper.readTree(ex.contentUTF8()).path("message").asString("");
            return message.isBlank() ? ex.getMessage() : message;
        } catch (JacksonException parseError) {
            return ex.getMessage();
        }
    }

    private String payload(WatchData before, WatchData after) {
        Map<String, WatchData> snapshots = new LinkedHashMap<>();
        snapshots.put("before", before);
        snapshots.put("after", after);
        return jsonMapper.writeValueAsString(snapshots);
    }

    private record CommittedStep(int stepNo, SagaStep step) {
    }

    private record Failure(String reason, HttpStatus httpStatus) {
    }
}
