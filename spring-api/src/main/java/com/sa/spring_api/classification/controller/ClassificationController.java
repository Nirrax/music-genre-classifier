package com.sa.spring_api.classification.controller;

import com.sa.spring_api.classification.dto.ClassificationDTO;
import com.sa.spring_api.classification.dto.ClassificationRequest;
import com.sa.spring_api.classification.dto.ClassificationUploadRequest;
import com.sa.spring_api.classification.enums.Status;
import com.sa.spring_api.classification.model.Classification;
import com.sa.spring_api.classification.service.ClassificationService;
import com.sa.spring_api.config.security.JwtUserDetails;
import io.jsonwebtoken.Jwt;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/classifications")
public class ClassificationController {
    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @GetMapping("")
    public ResponseEntity<List<ClassificationDTO>> getClassifications(@AuthenticationPrincipal JwtUserDetails user) {
        List<ClassificationDTO> classifications = classificationService.getUserClassifications(user.getId()).stream().map(ClassificationDTO::from).toList();

        return new ResponseEntity<>(classifications, HttpStatus.OK);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassificationDTO> getClassificationById(@AuthenticationPrincipal JwtUserDetails user, @PathVariable UUID id) {
        return new ResponseEntity<>(ClassificationDTO.from(classificationService.getClassificationByIdAndUserId(id, user.getId())), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<String> createClassification(@Valid @RequestBody ClassificationUploadRequest request) {
        return new ResponseEntity<>(classificationService.getUploadUrl(request.filename()), HttpStatus.CREATED);
    }

    @PostMapping("/classify")
    public ResponseEntity<String> classify(@AuthenticationPrincipal JwtUserDetails user, @Valid @RequestBody ClassificationRequest request) {
        return new ResponseEntity<>(classificationService.classifyFile(request.s3Key(), user.getId()).toString(), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClassificationById(@PathVariable UUID id) {
        classificationService.deleteClassificationById(id);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

}
