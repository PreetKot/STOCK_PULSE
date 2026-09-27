package com.stockpulse.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ApiErrorCodes.VALIDATION_ERROR,
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining(", "));

        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ApiErrorCodes.VALIDATION_ERROR,
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String errorCode = ex.getErrorCode();

        // Map specific error codes to appropriate HTTP status codes
        HttpStatus status = switch (errorCode) {
            case ApiErrorCodes.NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ApiErrorCodes.DUPLICATE_SKU, ApiErrorCodes.PENDING_SUGGESTION_EXISTS,
                    ApiErrorCodes.SUGGESTION_ALREADY_FINALIZED, ApiErrorCodes.STOCK_CONFLICT -> HttpStatus.CONFLICT;
            case ApiErrorCodes.INVALID_DECISION -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                errorCode,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                ApiErrorCodes.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleConflictException(ConflictException ex, HttpServletRequest request) {
        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleIllegalArgument(RuntimeException ex, HttpServletRequest request) {
        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ApiErrorCodes.INVALID_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleMissingResource(org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest request) {
        return new ApiErrorResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), ApiErrorCodes.NOT_FOUND,
                "Resource not found", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGeneralException(Exception ex, HttpServletRequest request) {
        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                request.getRequestURI()
        );
    }
}
