package com.ptit.story_speaker.common.exceptions;

import com.ptit.story_speaker.domain.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý chính cho tất cả các lỗi nghiệp vụ (AppException).
     * Bao gồm cả ResourceNotFoundException (vì nó kế thừa AppException).
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();

        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, errorCode.getStatus());
    }

    /**
     * Xử lý lỗi validation khi @RequestBody không hợp lệ (ví dụ: @NotEmpty, @Email).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getStatus().getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, errorCode.getStatus());
    }

    /**
     * Xử lý các lỗi chung (Catch-all) cho 500 Internal Server Error.
     * Đây là các lỗi mà bạn không lường trước được.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getStatus().getReasonPhrase(),
                errorCode.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, errorCode.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "BUSS_404",
                "Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
