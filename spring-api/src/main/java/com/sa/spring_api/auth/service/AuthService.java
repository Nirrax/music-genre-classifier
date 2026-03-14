package com.sa.spring_api.auth.service;

import com.sa.spring_api.auth.dto.AuthDTO;
import com.sa.spring_api.auth.entity.RefreshToken;
import com.sa.spring_api.user.model.User;
import org.antlr.v4.runtime.misc.Pair;

import java.util.UUID;

public interface AuthService {
    AuthDTO registerUser(String username, String password);

    AuthDTO loginUser(String username, String password);

    void logoutUser(String token);

    RefreshToken createRefreshToken(UUID userId);

    AuthDTO refreshToken(String token);
}
