package com.sa.spring_api.classification.exception;

public class ClassificationNotReadyException extends RuntimeException {
    public ClassificationNotReadyException(String message) {
        super(message);
    }
}
