package com.mss301.watch.dto;

import com.mss301.watch.entity.Watch;
import java.math.BigDecimal;
import java.time.LocalDate;

// [TODO-6.14] All Watches columns plus CategoryName
public record WatchResponse(
        Integer watchId,
        String name,
        Integer categoryId,
        String categoryName,
        Integer brandId,
        String caseMaterial,
        String strapMaterial,
        String waterResistance,
        BigDecimal price,
        LocalDate releaseDate,
        Integer stock) {

    public static WatchResponse from(Watch watch) {
        return new WatchResponse(
                watch.getWatchId(),
                watch.getName(),
                watch.getCategory().getCategoryId(),
                watch.getCategory().getCategoryName(),
                watch.getBrandId(),
                watch.getCaseMaterial(),
                watch.getStrapMaterial(),
                watch.getWaterResistance(),
                watch.getPrice(),
                watch.getReleaseDate(),
                watch.getStock());
    }
}
