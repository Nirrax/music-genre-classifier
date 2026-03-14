package com.sa.spring_api.classification.dto;

import com.sa.spring_api.classification.enums.Status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ClassificationResponseMessage(UUID classificationId,
                                            String genre,
                                            List<String> genreSequence,
                                            Map<String, Integer> genreCounts,
                                            LocalDate completedAt,
                                            String status) {
}
