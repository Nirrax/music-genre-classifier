package com.sa.spring_api.user.model;

import com.sa.spring_api.classification.model.Classification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @NotNull
    @Column(unique = true)
    private String username;

    @NotNull
    private String passwordHash;

    @OneToMany(mappedBy = "user")
    private Set<Classification> classifications;

    @NotNull
    private LocalDate createdAt;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.passwordHash = password;
        this.createdAt = LocalDate.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<Classification> getClassifications() {
        return classifications;
    }

    public void setClassifications(Set<Classification> classifications) {
        this.classifications = classifications;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

}
