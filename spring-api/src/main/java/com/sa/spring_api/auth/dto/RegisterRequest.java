package com.sa.spring_api.auth.dto;

import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @Pattern(
                regexp = "^[A-Za-z][A-Za-z0-9]{3,23}$",
                message = "Username must be 3–24 chars, start with a letter, and contain only letters, numbers"
        )
        String username,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$",
                message = "Password must be 8–24 chars and include upper, lower, number, and special character"
        )
        String password
) {
}
