package com.sa.spring_api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sa.spring_api.exception.dto.ErrorDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
       response.setContentType("application/json");
       response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorDTO error = new ErrorDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized"
        );

        response.getWriter().write(mapper.writeValueAsString(error));
    }
}
