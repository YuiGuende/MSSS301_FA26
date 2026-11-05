package com.mss301.brand.controller;

import com.mss301.brand.dto.ApiResponse;
import com.mss301.brand.dto.BrandAssignmentResponse;
import com.mss301.brand.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// [TODO-5.4] Saga participant API called only by MSWatch; the gateway has no route to it
@RestController
@RequestMapping("/internal/brands")
@RequiredArgsConstructor
public class InternalBrandController {

    private final BrandService brandService;

    @PutMapping("/{brandId}/watches/{watchId}")
    public ApiResponse<BrandAssignmentResponse> assign(@PathVariable Integer brandId, @PathVariable Integer watchId) {
        BrandAssignmentResponse result = brandService.assignWatch(brandId, watchId);
        return ApiResponse.ok(result.status(), result);
    }

    @DeleteMapping("/watches/{watchId}")
    public ApiResponse<BrandAssignmentResponse> unassign(@PathVariable Integer watchId) {
        BrandAssignmentResponse result = brandService.unassignWatch(watchId);
        return ApiResponse.ok(result.status(), result);
    }
}
