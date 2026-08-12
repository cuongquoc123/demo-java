package com.example.demo.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.dto.ErrorRespone;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionFillter {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorRespone> handleResourceNotFoundException(
        ResourceNotFoundException ex, 
        HttpServletRequest request) {
        ErrorRespone errorResponse = new ErrorRespone(
            ex.getMessage(),               // 1. message (String)
            HttpStatus.NOT_FOUND.value(),  // 2. status (int)
            LocalDateTime.now(),           // 3. timestamp (LocalDateTime)
            "Not Found",             // 4. error (String)
            request.getRequestURI()        // 5. path (String)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
