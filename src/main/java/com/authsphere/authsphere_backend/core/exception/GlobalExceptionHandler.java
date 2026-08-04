package com.authsphere.authsphere_backend.core.exception;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ApiResponse<Void> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException exception) {

        return ApiResponse.<Void>builder()
                .success(false)
                .status(ApiStatus.ERROR)
                .message(exception.getMessage())
                .build();
    }
}