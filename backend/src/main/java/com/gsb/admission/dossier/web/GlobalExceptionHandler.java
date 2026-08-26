package com.gsb.admission.dossier.web;

import com.gsb.admission.dossier.service.ApiException;
import com.gsb.admission.dossier.web.dto.ApiErrorView;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorView> handleApiException(ApiException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ApiErrorView(e.getErrorCode(), e.getMessage(), e.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorView> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, Object> details = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiErrorView("VALIDATION_ERROR", "request validation failed", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorView> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, Object> details = new LinkedHashMap<>();
        e.getConstraintViolations().forEach(violation ->
                details.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiErrorView("VALIDATION_ERROR", "request validation failed", details));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorView> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorView("VALIDATION_ERROR", "malformed request: " + e.getMessage(),
                        Map.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorView> handleUnexpected(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorView("INTERNAL_ERROR", e.getMessage(), Map.of()));
    }
}
