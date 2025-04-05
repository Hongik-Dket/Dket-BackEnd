package com.example.demo.base.code.exception;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.status.ErrorStatus;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice
public class CommonException {

    // 비밀 번호 규칙 : 영문자, 숫자, 특수문자 또는 밑줄을 최소 하나씩 포함해야 하며, 길이는 8~20자 사이여야 한다.
    public static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[\\W_])[a-zA-Z0-9\\W_]{8,20}$";

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        log.error("Custom Error: {} - Code: {} - Status: {}",
                ex.getMessage(),
                ex.getClass().getSimpleName(),
                ex.getErrorStatus().getHttpStatus());

        return CustomException.createErrorResponse(ex.getErrorStatus(), null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage()
                ));
        log.error("Validation Error: {}", errors);
        return CustomException.createErrorResponse(ErrorStatus.COMMON_BAD_REQUEST, errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage()
                ));
        log.error("Validation Error: {}", errors);
        return CustomException.createErrorResponse(ErrorStatus.COMMON_BAD_REQUEST, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) cause;
            if (invalidFormatException.getTargetType().isEnum()) {
                return CustomException.createErrorResponse(ErrorStatus.USER_INVALID_PROVIDER, null);
            }
        }

        if (cause instanceof JsonMappingException) {
            String fieldName = getInvalidFieldName((JsonMappingException) cause);
            if ("password".equals(fieldName)) {
                return CustomException.createErrorResponse(ErrorStatus.USER_INVALID_PASSWORD_FORMAT, null);
            }
        }

        return CustomException.createErrorResponse(ErrorStatus.COMMON_BAD_REQUEST, null);
    }

    private String getInvalidFieldName(JsonMappingException exception) {
        List<JsonMappingException.Reference> path = exception.getPath();
        if (!path.isEmpty()) {
            return path.get(path.size() - 1).getFieldName();
        }
        return null;
    }
    private void validatePassword(String password) {
        if (!Pattern.matches(PASSWORD_PATTERN, password)) {
            throw new CustomException(ErrorStatus.USER_INVALID_PASSWORD_FORMAT);
        }
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected Error: ", ex);
        return CustomException.createErrorResponse(ErrorStatus.COMMON_INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}