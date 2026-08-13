package com.example.demo.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.dto.ErrorRespone;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

@RestControllerAdvice
public class GlobalExceptionFillter {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorRespone> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        ErrorRespone errorResponse = new ErrorRespone(
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now(),
            "Not Found",
            request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorRespone> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request) {
        ErrorRespone errorResponse = new ErrorRespone(
            "Tên đăng nhập hoặc mật khẩu không chính xác.",
            HttpStatus.UNAUTHORIZED.value(),
            LocalDateTime.now(),
            "Unauthorized",
            request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespone> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        ErrorRespone errorResponse = new ErrorRespone(
            "Bạn không có quyền thực hiện thao tác này.",
            HttpStatus.FORBIDDEN.value(),
            LocalDateTime.now(),
            "Forbidden",
            request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespone> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {
        ErrorRespone errorResponse = new ErrorRespone(
            ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now(),
            "Internal Server Error",
            request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
