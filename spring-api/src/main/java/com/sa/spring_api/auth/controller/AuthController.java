package com.sa.spring_api.auth.controller;

import com.sa.spring_api.auth.dto.LogoutRequest;
import com.sa.spring_api.auth.dto.RefreshRequest;
import com.sa.spring_api.auth.service.AuthService;
import com.sa.spring_api.auth.dto.AuthDTO;
import com.sa.spring_api.auth.dto.LoginRequest;
import com.sa.spring_api.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthDTO> registerUser(@Valid @RequestBody RegisterRequest credentials){
        AuthDTO tokens = authService.registerUser(credentials.username(), credentials.password());
        return new ResponseEntity<>(tokens, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthDTO> loginUser(@Valid @RequestBody LoginRequest credentials){
        AuthDTO tokens = authService.loginUser(credentials.username(), credentials.password());
        return new ResponseEntity<>(tokens, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@Valid @RequestBody LogoutRequest token){
        authService.logoutUser(token.refreshToken());
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDTO> refreshToken(@Valid @RequestBody RefreshRequest token){
        AuthDTO tokens = authService.refreshToken(token.refreshToken());
        return new ResponseEntity<>(tokens, HttpStatus.OK);
    }

}
