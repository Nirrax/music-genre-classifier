package com.sa.spring_api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
      @NotBlank(message = "Username is required")
      @Size(min = 3, message = "Username must be atleast 3 characters")
      String username,

      @NotBlank(message = "Password is required")
      @Size(min = 8, message = "Password must be at least 8 characters")
      String password
) {
}
