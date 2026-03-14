package com.sa.spring_api.classification.dto;

import jakarta.validation.constraints.NotBlank;

public record ClassificationUploadRequest(@NotBlank String filename) {
}
