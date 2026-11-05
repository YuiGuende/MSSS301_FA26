package com.mss301.watch.config;

import com.mss301.watch.entity.Watch;
import com.mss301.watch.entity.WatchCategory;
import com.mss301.watch.repository.WatchCategoryRepository;
import com.mss301.watch.repository.WatchRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// [TODO-6.16] Seeds categories and watches verbatim from the assignment
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final WatchCategoryRepository categoryRepository;
    private final WatchRepository watchRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0 || watchRepository.count() > 0) {
            return;
        }
        WatchCategory dive = category("Dive Watches",
                "Designed for underwater diving and feature high water resistance.", "Automatic", "$500 - $10,000");
        WatchCategory chronograph = category("Chronograph Watches",
                "Watches with a stopwatch function in addition to the main display.", "Automatic or Quartz", "$300 - $8,000");
        WatchCategory dress = category("Dress Watches",
                "Elegant and simple, intended to be worn with formal wear.", "Automatic or Manual", "$400 - $20,000");
        WatchCategory digital = category("Digital Watches",
                "Watches with an LCD or LED screen that show time in a digital format.", "Quartz", "$50 - $500");
        WatchCategory pilot = category("Pilot Watches",
                "Originally designed for aviators, featuring large, easy-to-read dials.", "Automatic", "$700 - $15,000");
        List<WatchCategory> categories = categoryRepository.saveAll(List.of(dive, chronograph, dress, digital, pilot));
        log.info("Seeded {} categories", categories.size());

        List<Watch> watches = watchRepository.saveAll(List.of(
                watch("Submariner Date", dive, 1, "Oystersteel", "Oystersteel", "300 meters", "9550.00", "2020-09-01", 50),
                watch("Speedmaster Moonwatch Professional", chronograph, 2, "Stainless Steel", "Stainless Steel", "50 meters", "7200.00", "2021-01-05", 75),
                watch("Presage Cocktail Time", dress, 3, "Stainless Steel", "Leather", "50 meters", "425.00", "2019-06-01", 200),
                watch("G-Shock DW-5600E-1V", digital, 4, "Resin", "Resin", "200 meters", "69.95", "1996-06-12", 500),
                watch("Carrera Calibre Heuer 02", chronograph, 5, "Stainless Steel", "Alligator Leather", "100 meters", "5900.00", "2022-03-30", 60),
                watch("Seamaster Diver 300M", dive, 2, "Stainless Steel", "Rubber", "300 meters", "5400.00", "2018-02-15", 120),
                watch("Prospex SPB143", dive, 3, "Stainless Steel", "Stainless Steel", "200 meters", "1200.00", "2020-07-01", 150),
                watch("Daytona", chronograph, 1, "Oystersteel", "Oystersteel", "100 meters", "14550.00", "2016-03-16", 30),
                watch("F-91W", digital, 4, "Resin", "Resin", "30 meters", "19.99", "1989-01-01", 1000),
                watch("IWC Pilot's Watch Mark XVIII", pilot, 4, "Stainless Steel", "Leather", "60 meters", "4500.00", "2016-01-18", 80)));
        log.info("Seeded {} watches", watches.size());
    }

    private WatchCategory category(String name, String description, String movementType, String priceRange) {
        return WatchCategory.builder()
                .categoryName(name)
                .description(description)
                .movementType(movementType)
                .priceRange(priceRange)
                .build();
    }

    private Watch watch(String name, WatchCategory category, int brandId, String caseMaterial, String strapMaterial,
            String waterResistance, String price, String releaseDate, int stock) {
        return Watch.builder()
                .name(name)
                .category(category)
                .brandId(brandId)
                .caseMaterial(caseMaterial)
                .strapMaterial(strapMaterial)
                .waterResistance(waterResistance)
                .price(new BigDecimal(price))
                .releaseDate(LocalDate.parse(releaseDate))
                .stock(stock)
                .build();
    }
}
