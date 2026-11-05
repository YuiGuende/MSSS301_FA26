package com.mss301.watch.service.saga;

// [TODO-6.12] A failure simulated on request, used only to prove compensation
public class SagaFaultException extends RuntimeException {

    public SagaFaultException(String message) {
        super(message);
    }
}
