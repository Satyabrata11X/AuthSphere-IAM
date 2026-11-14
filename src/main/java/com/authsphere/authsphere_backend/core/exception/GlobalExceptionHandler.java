
package com.authsphere.authsphere_backend.core.exception;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================================================
    // AUTHENTICATION EXCEPTIONS
    // =========================================================

    @ExceptionHandler({
            InvalidCredentialsException.class,
            InvalidMfaChallengeException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            RuntimeException exception) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );
    }

    // =========================================================
    // RESOURCE NOT FOUND
    // =========================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException exception) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    // =========================================================
    // BUSINESS CONFLICTS
    // =========================================================

    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            BusinessConflictException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(
            RuntimeException exception) {

        String message = exception instanceof DataIntegrityViolationException
                ? "A database constraint was violated."
                : exception.getMessage();

        return buildResponse(HttpStatus.CONFLICT, message);
    }

    // =========================================================
    // BAD REQUEST
    // =========================================================

    @ExceptionHandler({
            IllegalArgumentException.class,
            WeakPasswordException.class,
            PasswordSameAsOldException.class,
            InvalidVerificationTokenException.class,
            VerificationTokenExpiredException.class,
            InvalidPasswordResetTokenException.class,
            PasswordResetTokenExpiredException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            RuntimeException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    // =========================================================
    // REQUEST VALIDATION
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .orElse("Request validation failed.");

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    // =========================================================
    // INVALID REQUEST PARAMETERS
    // =========================================================

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(
            Exception exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameters or request body."
        );
    }

    // =========================================================
    // UNEXPECTED EXCEPTIONS
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception exception) {

        log.error("Unexpected application error", exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal server error occurred."
        );
    }

    // =========================================================
    // COMMON RESPONSE BUILDER
    // =========================================================

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            HttpStatus httpStatus,
            String message) {

        ApiResponse<Void> response = buildErrorResponse(message);

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    private ApiResponse<Void> buildErrorResponse(String message) {

        return ApiResponse.<Void>builder()
                .success(false)
                .status(ApiStatus.ERROR)
                .message(message)
                .data(null)
                .build();
    }
}
