package com.shopperspoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public final class GenericResponse {

    private String message;
    private String status;
    private LocalDateTime timestamp;

    public GenericResponse(String message, String status, LocalDateTime timestamp) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
    }
}
