package com.sa.spring_api.classification.dto;

import com.sa.spring_api.classification.model.Classification;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ClassificationDTO(UUID id, String filename,
                                String genre, List<String> genreSequence,
                                Map<String, Integer> genreCount,
                                LocalDate issuedAt,
                                LocalDate completedAt) {
    public static ClassificationDTO from(Classification classification) {
        return new ClassificationDTO(classification.getId(),
                classification.getFilename(),
                classification.getGenre(),
                classification.getGenreSequence(),
                classification.getGenreCounts(),
                classification.getIssuedAt(),
                classification.getCompletedAt());
    }
}
