package com.jordi.kakebot.expense.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.text.Normalizer;
import java.util.Locale;

public enum ExpenseCategory {
    HOME,
    FOOD,
    TRANSPORT,
    LEISURE,
    OTHER;

    @JsonCreator
    public static ExpenseCategory fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Categoria obligatoria. Valores permitidos: casa, comida, transporte, ocio, otros.");
        }

        String normalized = normalize(value);
        return switch (normalized) {
            case "home", "casa" -> HOME; // Spanish "casa" maps to HOME.
            case "food", "comida" -> FOOD; // Spanish "comida" maps to FOOD.
            case "transport", "transporte" -> TRANSPORT; // Spanish "transporte" maps to TRANSPORT.
            case "leisure", "ocio" -> LEISURE; // Spanish "ocio" maps to LEISURE.
            case "other", "otros" -> OTHER; // Spanish "otros" maps to OTHER.
            default -> throw new IllegalArgumentException(
                    "Categoria invalida. Valores permitidos: casa, comida, transporte, ocio, otros."
            );
        };
    }

    private static String normalize(String text) {
        String trimmed = text.trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }
}
