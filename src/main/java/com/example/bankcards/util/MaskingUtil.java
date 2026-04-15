package com.example.bankcards.util;

import java.util.Arrays;
import java.util.List;

public final class MaskingUtil {
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber.matches("\\d{16}")) {
            List<String> blocks = Arrays.stream(cardNumber.split("(?<=\\G.{4})")).toList();
            return "**** **** **** " + blocks.get(3);
        } else {
            return "****";
        }
    }
}
