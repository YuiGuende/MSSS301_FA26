package com.mss301.brand.service;

import com.mss301.brand.dto.BrandResponse;
import com.mss301.brand.dto.BrandWatchesResponse;
import com.mss301.brand.entity.BrandWatch;
import com.mss301.brand.entity.WatchBrand;
import com.mss301.brand.exception.BrandNotFoundException;
import com.mss301.brand.repository.BrandWatchRepository;
import com.mss301.brand.repository.WatchBrandRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// [TODO-5.5] Read side of the public brand API
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandQueryService {

    private final WatchBrandRepository brandRepository;
    private final BrandWatchRepository brandWatchRepository;

    public List<BrandResponse> findAll() {
        Map<Integer, Long> counts = brandWatchRepository.findAll().stream()
                .collect(Collectors.groupingBy(BrandWatch::getBrandId, Collectors.counting()));
        return brandRepository.findAll().stream()
                .map(brand -> toResponse(brand, counts.getOrDefault(brand.getBrandId(), 0L)))
                .toList();
    }

    public BrandResponse findById(Integer brandId) {
        WatchBrand brand = requireBrand(brandId);
        return toResponse(brand, brandWatchRepository.findByBrandIdOrderByWatchIdAsc(brandId).size());
    }

    public BrandWatchesResponse findWatches(Integer brandId) {
        WatchBrand brand = requireBrand(brandId);
        List<Integer> watchIds = brandWatchRepository.findByBrandIdOrderByWatchIdAsc(brandId).stream()
                .map(BrandWatch::getWatchId)
                .toList();
        return new BrandWatchesResponse(brandId, brand.getBrandName(), watchIds.size(), watchIds);
    }

    private WatchBrand requireBrand(Integer brandId) {
        return brandRepository.findById(brandId).orElseThrow(() -> new BrandNotFoundException(brandId));
    }

    private BrandResponse toResponse(WatchBrand brand, long watchCount) {
        return new BrandResponse(brand.getBrandId(), brand.getBrandName(), brand.getCountryOfOrigin(), watchCount);
    }
}
