package com.mss301.brand.service;

import com.mss301.brand.dto.BrandAssignmentResponse;
import com.mss301.brand.entity.BrandWatch;
import com.mss301.brand.exception.BrandNotFoundException;
import com.mss301.brand.repository.BrandWatchRepository;
import com.mss301.brand.repository.WatchBrandRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandService {

    private final WatchBrandRepository brandRepository;
    private final BrandWatchRepository brandWatchRepository;

    // [TODO-5.2] Upsert: one call or five calls leave the same result, so the orchestrator can retry
    @Transactional
    public BrandAssignmentResponse assignWatch(Integer brandId, Integer watchId) {
        if (!brandRepository.existsById(brandId)) {
            throw new BrandNotFoundException(brandId);
        }
        BrandWatch link = brandWatchRepository.findByWatchId(watchId)
                .orElseGet(() -> BrandWatch.builder().watchId(watchId).build());
        Integer previousBrandId = link.getBrandId();
        link.setBrandId(brandId);
        brandWatchRepository.save(link);
        log.info("[SAGA-PARTICIPANT] ASSIGN watch={} -> brand={} (previous brand={})", watchId, brandId, previousBrandId);
        return new BrandAssignmentResponse(watchId, brandId, previousBrandId, "ASSIGNED");
    }

    // [TODO-5.3] Nothing to remove is still a success, so a retried compensation stays safe
    @Transactional
    public BrandAssignmentResponse unassignWatch(Integer watchId) {
        Optional<BrandWatch> link = brandWatchRepository.findByWatchId(watchId);
        if (link.isEmpty()) {
            log.info("[SAGA-PARTICIPANT] UNASSIGN watch={} NOTHING_TO_DO", watchId);
            return new BrandAssignmentResponse(watchId, null, null, "NOTHING_TO_DO");
        }
        Integer previousBrandId = link.get().getBrandId();
        brandWatchRepository.delete(link.get());
        log.info("[SAGA-PARTICIPANT] UNASSIGN watch={} from brand={}", watchId, previousBrandId);
        return new BrandAssignmentResponse(watchId, null, previousBrandId, "UNASSIGNED");
    }
}
