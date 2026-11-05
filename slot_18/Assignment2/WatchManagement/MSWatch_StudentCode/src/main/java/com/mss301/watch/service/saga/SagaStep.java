package com.mss301.watch.service.saga;

// [TODO-6.8] A local transaction and its compensation; a step without compensation is the pivot
public record SagaStep(String name, Runnable action, String compensationName, Runnable compensation) {

    public static SagaStep of(String name, Runnable action, String compensationName, Runnable compensation) {
        return new SagaStep(name, action, compensationName, compensation);
    }

    public static SagaStep pivot(String name, Runnable action) {
        return new SagaStep(name, action, null, null);
    }

    public boolean isPivot() {
        return compensation == null;
    }
}
