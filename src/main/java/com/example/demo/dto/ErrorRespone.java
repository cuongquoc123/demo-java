package com.example.demo.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ErrorRespone {
    private String message;
    private int status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String error;
    private String path;

    public ErrorRespone() {
    }

    public ErrorRespone(String message, int status, LocalDateTime timestamp, String error, String path) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.error = error;
        this.path = path;
    }

    public String getMessage() { return message; }
    public int getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getError() { return error; }
    public String getPath() { return path; }

    public void setMessage(String message) { this.message = message; }
    public void setStatus(int status) { this.status = status; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setError(String error) { this.error = error; }
    public void setPath(String path) { this.path = path; }
}
