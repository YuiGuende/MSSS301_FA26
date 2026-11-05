package com.mss301.watch.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

// [TODO-6.13] All fields required, Name longer than 10 characters, Price at least 100
public record WatchRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 11, max = 255, message = "Name must be longer than 10 characters and at most 255")
        String name,

        @NotNull(message = "CategoryID is required")
        Integer categoryId,

        @NotNull(message = "BrandID is required")
        Integer brandId,

        @NotBlank(message = "CaseMaterial is required")
        @Size(max = 100, message = "CaseMaterial must be at most 100 characters")
        String caseMaterial,

        @NotBlank(message = "StrapMaterial is required")
        @Size(max = 100, message = "StrapMaterial must be at most 100 characters")
        String strapMaterial,

        @NotBlank(message = "WaterResistance is required")
        @Size(max = 50, message = "WaterResistance must be at most 50 characters")
        String waterResistance,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "100", message = "Price must be at least 100")
        @Digits(integer = 8, fraction = 2, message = "Price must fit DECIMAL(10,2)")
        BigDecimal price,

        @NotNull(message = "ReleaseDate is required")
        LocalDate releaseDate,

        @NotNull(message = "Stock is required")
        @PositiveOrZero(message = "Stock must not be negative")
        Integer stock) {
}
