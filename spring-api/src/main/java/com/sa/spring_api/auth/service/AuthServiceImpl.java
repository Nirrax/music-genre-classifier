package com.sa.spring_api.auth.service;

import com.sa.spring_api.auth.dto.AuthDTO;
import com.sa.spring_api.auth.entity.RefreshToken;
import com.sa.spring_api.auth.exception.TokenNotFoundException;
import com.sa.spring_api.auth.repository.RefreshTokenRepository;
import com.sa.spring_api.config.security.JwtUserDetails;
import com.sa.spring_api.config.security.JwtUtil;
import com.sa.spring_api.user.exception.UserNotFoundException;
import com.sa.spring_api.user.model.User;
import com.sa.spring_api.user.repository.UserRepository;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Long refreshTokenDurationMs;


    public AuthServiceImpl(RefreshTokenRepository refreshTokenRepository,
                           UserRepository userRepository,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           @Value("${jwt.refresh.expiration}") Long refreshTokenDurationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenDurationMs = refreshTokenDurationMs;
    }

    @Override
    @Transactional
    public AuthDTO registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)){
            throw new IllegalArgumentException("Username is already in use");
        }

        User user = new User(username, passwordEncoder.encode(password));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());
        String refreshToken = createRefreshToken(user.getId()).getRefreshToken();
        return new AuthDTO(token, refreshToken);
    }

    @Override
    @Transactional
    public AuthDTO loginUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();

        refreshTokenRepository.deleteRefreshTokenByUserId(userDetails.getId());
        refreshTokenRepository.flush();

        String token = jwtUtil.generateToken(userDetails.getId());
        String refreshToken = createRefreshToken(userDetails.getId()).getRefreshToken();
        return new AuthDTO(token, refreshToken);
    }

    @Override
    @Transactional
    public void logoutUser(String token) {
        if (token == null || token.isBlank()) {
            //throw new IllegalArgumentException("Refresh token is required");
            return;
        }

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        refreshToken.ifPresent(refreshTokenRepository::delete);
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        RefreshToken refreshToken = new RefreshToken(UUID.randomUUID().toString(),
                Instant.now().plusMillis(refreshTokenDurationMs), user
        );
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public AuthDTO refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Invalid refresh token"));

        if (isTokenExpired(refreshToken)) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenNotFoundException("Refresh token expired");
        }

        UUID userId = refreshToken.getUser().getId();

        refreshTokenRepository.delete(refreshToken);
        refreshTokenRepository.flush();

        String newJwt = jwtUtil.generateToken(userId);
        RefreshToken newRefreshToken = createRefreshToken(userId);

        return new AuthDTO(newJwt, newRefreshToken.getRefreshToken());
    }

    private boolean isTokenExpired(RefreshToken token) {
        return token.getExpiresAt().isBefore(Instant.now());
    }

}
