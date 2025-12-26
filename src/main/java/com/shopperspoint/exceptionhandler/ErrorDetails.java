package com.shopperspoint.exceptionhandler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ErrorDetails {
    private String messageSize;
    private List<String> errors;
    private String details;
}
