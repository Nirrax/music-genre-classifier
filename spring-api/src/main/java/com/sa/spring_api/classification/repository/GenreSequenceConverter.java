package com.sa.spring_api.classification.repository;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class GenreSequenceConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> genreSequence) {
        return genreSequence != null ? String.join(",", genreSequence) : "";
    }

    @Override
    public List<String> convertToEntityAttribute(String genreSequence) {
        return genreSequence != null && !genreSequence.isEmpty() ? List.of(genreSequence.split(",")) : List.of();
    }
}
