package com.sa.spring_api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank(message = "Refresh token is required") String refreshToken) {

}
