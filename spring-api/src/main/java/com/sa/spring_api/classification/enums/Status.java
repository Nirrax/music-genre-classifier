package com.sa.spring_api.classification.enums;

public enum Status {
    PENDING, PROCESSING, DONE, FAILED;

    public static Status fromString(String value) {
        return Status.valueOf(value.toUpperCase());
    }
}