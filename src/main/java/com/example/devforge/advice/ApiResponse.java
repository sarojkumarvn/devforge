package com.example.devforge.advice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ApiResponse<T> {

    private String message;
    private T data;
    private boolean success;

    // OLD constructor (so your old code doesn't break)
    public ApiResponse(String message) {
        this.message = message;
        this.success = true;
    }

    // NEW constructor
    public ApiResponse(String message, T data, boolean success) {
        this.message = message;
        this.data = data;
        this.success = success;
    }
}