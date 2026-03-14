package com.sa.spring_api.exception;

import com.sa.spring_api.auth.exception.TokenNotFoundException;
import com.sa.spring_api.classification.exception.ClassificationFailedException;
import com.sa.spring_api.classification.exception.ClassificationNotFoundException;
import com.sa.spring_api.classification.exception.ClassificationNotReadyException;
import com.sa.spring_api.exception.dto.ErrorDTO;
import com.sa.spring_api.user.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            UserNotFoundException.class,
            ClassificationNotFoundException.class,
            NoResourceFoundException.class,
    })
    public ResponseEntity<ErrorDTO> handleNotFound(RuntimeException e) {
        ErrorDTO error = new ErrorDTO(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ClassificationNotReadyException.class)
    public ResponseEntity<ErrorDTO> handleClassificationNotReady(RuntimeException e) {
        ErrorDTO error = new ErrorDTO(
                HttpStatus.ACCEPTED.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.ACCEPTED);
    }

    @ExceptionHandler(ClassificationFailedException.class)
    public ResponseEntity<ErrorDTO> handleClassificationFailed(RuntimeException e) {
        ErrorDTO error = new ErrorDTO(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler({
            AccessDeniedException.class,
            TokenNotFoundException.class,})
    public ResponseEntity<ErrorDTO> handleAccessDenied(AccessDeniedException e) {
        ErrorDTO error = new ErrorDTO(
                HttpStatus.FORBIDDEN.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // 2. Validation errors (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorDTO error = new ErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                message
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. Illegal arguments (400)
    @ExceptionHandler({
            IllegalArgumentException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<ErrorDTO> handleIllegalArgument(RuntimeException e) {
        ErrorDTO error = new ErrorDTO(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGenericException(Exception e) {
        log.error("Unhandled exception", e);
        ErrorDTO error = new ErrorDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred"
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
