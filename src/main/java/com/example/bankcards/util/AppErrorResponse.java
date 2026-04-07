package com.example.bankcards.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppErrorResponse {
    private String message;
    private long timestamp;
}
