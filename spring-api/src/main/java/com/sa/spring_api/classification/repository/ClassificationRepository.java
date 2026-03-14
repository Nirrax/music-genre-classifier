package com.sa.spring_api.classification.repository;

import com.sa.spring_api.classification.enums.Status;
import com.sa.spring_api.classification.model.Classification;
import com.sa.spring_api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassificationRepository extends JpaRepository<Classification, UUID> {
    List<Classification> findByUserIdAndStatus(UUID userId, Status status);

    Optional<Classification> findByIdAndUserId(UUID id, UUID userId);

    Optional<Classification> findClassificationByS3KeyAndUser(String s3Key, User user);
}
