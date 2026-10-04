package br.com.controlei.application.exceptions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<ApiFieldError> fields;

    public ApiError() {
        this.timestamp = LocalDateTime.now();
        this.fields = new ArrayList<>();
    }

    public ApiError(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public void addFieldError(String field, String message) {
        this.fields.add(new ApiFieldError(field, message));
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ApiFieldError> getFields() {
        return fields;
    }

    public void setFields(List<ApiFieldError> fields) {
        this.fields = fields;
    }
}
