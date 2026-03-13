package com.sa.spring_api.user.dto;

import com.sa.spring_api.user.model.User;

import java.time.LocalDate;
import java.util.UUID;

public record UserDTO(UUID id, String username,
                      LocalDate createdAt) {
    public static UserDTO from(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getCreatedAt());
    }

}
