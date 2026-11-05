package com.mss301.watch.service;

import com.mss301.watch.entity.Watch;
import com.mss301.watch.repository.WatchCategoryRepository;
import com.mss301.watch.repository.WatchRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// [TODO-6.5] Each method is one local transaction on DBWatch, committed when it returns
@Service
@RequiredArgsConstructor
public class WatchLocalService {

    private final WatchRepository watchRepository;
    private final WatchCategoryRepository categoryRepository;

    @Transactional
    public Integer insert(WatchData data) {
        Watch watch = new Watch();
        apply(watch, data);
        return watchRepository.save(watch).getWatchId();
    }

    /** Also serves RESTORE_WATCH: writing the same snapshot again gives the same result. */
    @Transactional
    public void update(Integer watchId, WatchData data) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new IllegalStateException("Watch not found with id: " + watchId));
        apply(watch, data);
    }

    @Transactional
    public void delete(Integer watchId) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new IllegalStateException("Watch not found with id: " + watchId));
        watchRepository.delete(watch);
    }

    /** Compensation of CREATE_WATCH: already gone is a success, so a retry stays safe. */
    @Transactional
    public void deleteIfExists(Integer watchId) {
        watchRepository.findById(watchId).ifPresent(watchRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<Watch> findAllNewestFirst() {
        return watchRepository.findAllNewestFirst();
    }

    @Transactional(readOnly = true)
    public Optional<Watch> findWithCategory(Integer watchId) {
        return watchRepository.findWithCategory(watchId);
    }

    private void apply(Watch watch, WatchData data) {
        watch.setName(data.name());
        watch.setCategory(categoryRepository.getReferenceById(data.categoryId()));
        watch.setBrandId(data.brandId());
        watch.setCaseMaterial(data.caseMaterial());
        watch.setStrapMaterial(data.strapMaterial());
        watch.setWaterResistance(data.waterResistance());
        watch.setPrice(data.price());
        watch.setReleaseDate(data.releaseDate());
        watch.setStock(data.stock());
    }
}
