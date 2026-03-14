package com.sa.spring_api.classification.dto;

import java.util.UUID;

public record ClassificationRequestMessage(UUID classificationId, String s3key) {

}
