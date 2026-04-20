package com.sa.spring_api.classification.service;

import com.sa.spring_api.classification.dto.ClassificationDTO;
import com.sa.spring_api.classification.dto.ClassificationResponseMessage;
import com.sa.spring_api.classification.model.Classification;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ClassificationService {
    List<Classification> getUserClassifications(UUID userId);

    Classification getClassificationByIdAndUserId(UUID id, UUID userId);

    String getUploadUrl(String fileName);

    UUID classifyFile(String s3Key, UUID userId);

    void handleClassificationResponse(ClassificationResponseMessage message);

    Classification getClassifiedResult(UUID id);

    void deleteClassificationById(UUID id);
}
