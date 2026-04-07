package com.example.bankcards.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AppRuntimeException extends RuntimeException {
    public AppRuntimeException(String message) {
        super(message);
    }
}
