package com.sa.spring_api.classification.exception;

public class ClassificationNotFoundException extends RuntimeException {
    public ClassificationNotFoundException(String message) {
        super(message);
    }
}
