package com.sa.spring_api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "Old password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String currentPassword,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$",
                message = "Password must be 8–24 chars and include upper, lower, number, and special character"
        )
        String newPassword
) {
}
