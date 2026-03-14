package com.sa.spring_api.classification.model;

import com.sa.spring_api.classification.enums.Status;
import com.sa.spring_api.classification.repository.GenreSequenceConverter;
import com.sa.spring_api.user.model.User;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "CLASSIFICATIONS")
public class Classification {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @NotNull
    private String filename;

    private String genre;

    @Lob
    @Convert(converter = GenreSequenceConverter.class)
    private List<String> genreSequence;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb") // Use "json" if not using jsonb
    private Map<String, Integer> genreCounts = new HashMap<>();

    @NotNull
    private String s3Key;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    @NotNull
    private LocalDate issuedAt;

    private LocalDate completedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    public Classification() {}


    public Classification(String s3Key, User user) {
        this.filename = s3Key.substring(s3Key.substring(0, s3Key.length() - 4).lastIndexOf('/') + 1, s3Key.length() - 4);
        this.s3Key = s3Key;
        this.user = user;
        this.issuedAt = LocalDate.now();
        this.status = Status.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public List<String> getGenreSequence() {
        return genreSequence;
    }

    public void setGenreSequence(List<String> genreSequence) {
        this.genreSequence = genreSequence;
    }

    public Map<String, Integer> getGenreCounts() {
        return genreCounts;
    }

    public void setGenreCounts(Map<String, Integer> genreCounts) {
        this.genreCounts = genreCounts;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDate issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDate getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDate completedAt) {
        this.completedAt = completedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}


