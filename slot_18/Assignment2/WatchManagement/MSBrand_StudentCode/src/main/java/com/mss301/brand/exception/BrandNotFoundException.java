package com.mss301.brand.exception;

// [TODO-5.4] Mapped to HTTP 404; the orchestrator reads this message
public class BrandNotFoundException extends RuntimeException {

    public BrandNotFoundException(Integer brandId) {
        super("Brand not found with id: " + brandId);
    }
}
