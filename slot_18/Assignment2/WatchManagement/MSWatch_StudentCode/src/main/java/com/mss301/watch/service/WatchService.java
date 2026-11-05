package com.mss301.watch.service;

import com.mss301.watch.dto.WatchRequest;
import com.mss301.watch.dto.WatchResponse;
import com.mss301.watch.entity.Watch;
import com.mss301.watch.exception.ApiException;
import com.mss301.watch.repository.WatchCategoryRepository;
import com.mss301.watch.service.saga.SagaResult;
import com.mss301.watch.service.saga.WatchSagaOrchestrator;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

// [TODO-6.13] Cheap validation runs before the Saga, so a bad request creates no SagaStates row
@Service
@RequiredArgsConstructor
public class WatchService {

    private static final int MIN_NAME_LENGTH = 11;

    private final WatchLocalService localService;
    private final WatchCategoryRepository categoryRepository;
    private final WatchSagaOrchestrator orchestrator;

    public List<WatchResponse> findAll() {
        return localService.findAllNewestFirst().stream().map(WatchResponse::from).toList();
    }

    public WatchResponse findById(Integer watchId) {
        return WatchResponse.from(requireWatch(watchId));
    }

    public SagaResult create(WatchRequest request, Integer failAfterStep) {
        return orchestrator.createWatch(validated(request), failAfterStep);
    }

    public SagaResult update(Integer watchId, WatchRequest request, Integer failAfterStep) {
        WatchData before = WatchData.from(requireWatch(watchId));
        return orchestrator.updateWatch(watchId, before, validated(request), failAfterStep);
    }

    public SagaResult delete(Integer watchId, Integer failAfterStep) {
        WatchData before = WatchData.from(requireWatch(watchId));
        return orchestrator.deleteWatch(watchId, before, failAfterStep);
    }

    private Watch requireWatch(Integer watchId) {
        return localService.findWithCategory(watchId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Watch not found with id: " + watchId));
    }

    private WatchData validated(WatchRequest request) {
        String name = request.name().trim();
        if (name.length() < MIN_NAME_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Name must be longer than 10 characters");
        }
        if (!LocalDate.now().equals(request.releaseDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ReleaseDate must be the current date");
        }
        if (!categoryRepository.existsById(request.categoryId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category not found with id: " + request.categoryId());
        }
        return new WatchData(
                name,
                request.categoryId(),
                request.brandId(),
                request.caseMaterial().trim(),
                request.strapMaterial().trim(),
                request.waterResistance().trim(),
                request.price(),
                request.releaseDate(),
                request.stock());
    }
}
