package com.sa.spring_api.classification.service;

import com.sa.spring_api.classification.dto.ClassificationDTO;
import com.sa.spring_api.classification.dto.ClassificationRequestMessage;
import com.sa.spring_api.classification.dto.ClassificationResponseMessage;
import com.sa.spring_api.classification.enums.Status;
import com.sa.spring_api.classification.exception.ClassificationFailedException;
import com.sa.spring_api.classification.exception.ClassificationNotFoundException;
import com.sa.spring_api.classification.exception.ClassificationNotReadyException;
import com.sa.spring_api.classification.model.Classification;
import com.sa.spring_api.classification.repository.ClassificationRepository;
import com.sa.spring_api.config.mq.MQservice;
import com.sa.spring_api.user.model.User;
import com.sa.spring_api.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClassificationServiceImpl implements ClassificationService {
    private static final Logger log = LoggerFactory.getLogger(ClassificationServiceImpl.class);
    private final ClassificationRepository classificationRepository;
    private final UserService userService;
    private final S3Presigner s3Presigner;
    private final MQservice mqservice;
    private final String bucketName;

    public ClassificationServiceImpl(ClassificationRepository classificationRepository,
                                     UserService userService,
                                     S3Presigner s3Presigner,
                                     MQservice mqservice,
                                     @Value("${aws.s3.bucketName}") String bucketName) {
        this.classificationRepository = classificationRepository;
        this.userService = userService;
        this.s3Presigner = s3Presigner;
        this.mqservice = mqservice;
        this.bucketName = bucketName;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Classification> getUserClassifications(UUID userId) {
        return classificationRepository.findByUserIdAndStatus(userId, Status.DONE);
    }

    @Transactional(readOnly = true)
    @Override
    public Classification getClassificationByIdAndUserId(UUID id, UUID userId) {
        return classificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ClassificationNotFoundException("Classification not found"));
    }

    @Override
    public String getUploadUrl(String fileName) {
        if (!fileName.toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException("Only MP3 files are allowed");
        }

        String key = "files/" + UUID.randomUUID().toString() + "/" + fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("audio/mpeg")
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    @Transactional
    @Override
    public UUID classifyFile(String s3Key, UUID userId) {
        User user = userService.getUserById(userId);

        Optional<Classification> existing = classificationRepository.findClassificationByS3KeyAndUser(s3Key, user);

        if (existing.isPresent()) {
            Classification classification = existing.get();

            if (classification.getStatus() == Status.DONE || classification.getStatus() == Status.PROCESSING) {
                return classification.getId();
            }

            if (classification.getStatus() == Status.FAILED) {
                throw new ClassificationFailedException("Unable to process classification");
            }

            // Only retry if PENDING (message may not have been sent)
            if (classification.getStatus() == Status.PENDING) {
                try {
                    mqservice.sendMessage(new ClassificationRequestMessage(classification.getId(), s3Key));

                    classification.setStatus(Status.PROCESSING);
                    classificationRepository.save(classification);
                } catch (AmqpException e) {
                    throw new ClassificationFailedException("Unable to process classification");
                }

                return classification.getId();
            }
        }

        // Create new classification
        Classification classification = new Classification(s3Key, user);
        classification.setStatus(Status.PENDING);
        classificationRepository.save(classification);

        try {
            mqservice.sendMessage(new ClassificationRequestMessage(classification.getId(), s3Key));

            classification.setStatus(Status.PROCESSING);
            classificationRepository.save(classification);
        } catch (AmqpException e) {
            throw new ClassificationFailedException("Unable to process classification");
        }

        return classification.getId();
    }

    @EventListener
    @Transactional
    @Override
    public void handleClassificationResponse(ClassificationResponseMessage message) {
        Classification classification = classificationRepository.findById(message.classificationId())
                .orElseThrow(() -> new ClassificationNotFoundException("Classification not found"));

        classification.setGenre(message.genre());
        classification.setGenreSequence(message.genreSequence());
        classification.setGenreCounts(message.genreCounts());
        classification.setCompletedAt(message.completedAt());
        classification.setStatus(Status.fromString(message.status()));

        classificationRepository.save(classification);
    }

    @Transactional(readOnly = true)
    @Override
    public Classification getClassifiedResult(UUID id) {
        Classification classification = classificationRepository.findById(id)
                .orElseThrow(() -> new ClassificationNotFoundException("Classification not found for id: " + id));

        if (classification.getStatus() == Status.FAILED) {
            throw new ClassificationFailedException("Classification failed for id: " + id);
        }

        if (classification.getStatus() != Status.DONE) {
            throw new ClassificationNotReadyException("Classification not yet completed for id: " + id);
        }

        return classification;
    }

    @Transactional
    @Override
    public void deleteClassificationById(UUID id) {
        classificationRepository.deleteById(id);
    }

}
