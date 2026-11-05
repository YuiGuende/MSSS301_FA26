package com.mss301.brand.dto;

// [TODO-5.4] Outcome of an internal assign or unassign call
public record BrandAssignmentResponse(Integer watchId, Integer brandId, Integer previousBrandId, String status) {
}
