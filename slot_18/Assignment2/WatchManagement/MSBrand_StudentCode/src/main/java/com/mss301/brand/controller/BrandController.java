package com.mss301.brand.controller;

import com.mss301.brand.dto.ApiResponse;
import com.mss301.brand.dto.BrandResponse;
import com.mss301.brand.dto.BrandWatchesResponse;
import com.mss301.brand.service.BrandQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// [TODO-5.5] Public brand API; the gateway requires a logged-in user
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandQueryService brandQueryService;

    @GetMapping
    public ApiResponse<List<BrandResponse>> findAll() {
        return ApiResponse.ok("Brands", brandQueryService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<BrandResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok("Brand", brandQueryService.findById(id));
    }

    @GetMapping("/{id}/watches")
    public ApiResponse<BrandWatchesResponse> findWatches(@PathVariable Integer id) {
        return ApiResponse.ok("Brand watches", brandQueryService.findWatches(id));
    }
}
