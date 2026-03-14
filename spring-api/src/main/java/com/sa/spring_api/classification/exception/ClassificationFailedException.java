package com.sa.spring_api.classification.exception;

public class ClassificationFailedException extends RuntimeException {
    public ClassificationFailedException(String message) {
        super(message);
    }
}
