package com.mss301.brand.dto;

import java.util.List;

// [TODO-5.5] Lets a tester compare the DBBrand side of a Saga with DBWatch
public record BrandWatchesResponse(Integer brandId, String brandName, int watchCount, List<Integer> watchIds) {
}
