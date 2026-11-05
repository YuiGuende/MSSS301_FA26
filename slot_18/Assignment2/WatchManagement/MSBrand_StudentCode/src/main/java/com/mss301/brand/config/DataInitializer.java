package com.mss301.brand.config;

import com.mss301.brand.entity.BrandWatch;
import com.mss301.brand.entity.WatchBrand;
import com.mss301.brand.repository.BrandWatchRepository;
import com.mss301.brand.repository.WatchBrandRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// [TODO-5.6] Seeds the brands and the links matching WatchID -> BrandID of the seeded watches
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final int[] BRAND_OF_WATCH = {1, 2, 3, 4, 5, 2, 3, 1, 4, 4};

    private final WatchBrandRepository brandRepository;
    private final BrandWatchRepository brandWatchRepository;

    @Override
    public void run(String... args) {
        if (brandRepository.count() == 0) {
            List<WatchBrand> brands = List.of(
                    brand("Rolex", "Switzerland"),
                    brand("Omega", "Switzerland"),
                    brand("Seiko", "Japan"),
                    brand("Casio", "Japan"),
                    brand("TAG Heuer", "Switzerland"));
            brandRepository.saveAll(brands);
            log.info("Seeded {} brands", brands.size());
        }
        if (brandWatchRepository.count() == 0) {
            for (int i = 0; i < BRAND_OF_WATCH.length; i++) {
                brandWatchRepository.save(BrandWatch.builder().watchId(i + 1).brandId(BRAND_OF_WATCH[i]).build());
            }
            log.info("Seeded {} brand-watch links", BRAND_OF_WATCH.length);
        }
    }

    private WatchBrand brand(String name, String country) {
        return WatchBrand.builder().brandName(name).countryOfOrigin(country).build();
    }
}
