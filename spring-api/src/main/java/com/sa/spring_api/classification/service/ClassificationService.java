package com.sa.spring_api.classification.service;

import com.sa.spring_api.classification.dto.ClassificationDTO;
import com.sa.spring_api.classification.dto.ClassificationResponseMessage;
import com.sa.spring_api.classification.model.Classification;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ClassificationService {

    ClassificationDTO getClassificationByIdAndUserId(UUID id, UUID userId);

    void deleteClassificationById(UUID id);

    List<ClassificationDTO> getUserClassifications(UUID userId);

    String getUploadUrl(String fileName);

    UUID classifyFile(String s3Key, UUID userId);

    void handleClassificationResponse(ClassificationResponseMessage message);

    ClassificationDTO getClassifiedResult(UUID id);
}
