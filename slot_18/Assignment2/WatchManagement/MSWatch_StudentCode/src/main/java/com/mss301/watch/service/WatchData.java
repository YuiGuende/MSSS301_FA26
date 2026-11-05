package com.mss301.watch.service;

import com.mss301.watch.entity.Watch;
import java.math.BigDecimal;
import java.time.LocalDate;

// [TODO-6.6] Immutable snapshot of a watch: "before" restores it, "after" is what gets written
public record WatchData(
        String name,
        Integer categoryId,
        Integer brandId,
        String caseMaterial,
        String strapMaterial,
        String waterResistance,
        BigDecimal price,
        LocalDate releaseDate,
        Integer stock) {

    public static WatchData from(Watch watch) {
        return new WatchData(
                watch.getName(),
                watch.getCategory().getCategoryId(),
                watch.getBrandId(),
                watch.getCaseMaterial(),
                watch.getStrapMaterial(),
                watch.getWaterResistance(),
                watch.getPrice(),
                watch.getReleaseDate(),
                watch.getStock());
    }
}
