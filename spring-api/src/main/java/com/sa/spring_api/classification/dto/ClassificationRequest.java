package com.sa.spring_api.classification.dto;

import jakarta.validation.constraints.NotBlank;

public record ClassificationRequest(@NotBlank String s3Key) {
}
