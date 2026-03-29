package com.jordi.kakebot.expense.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.json.LenientBigDecimalDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ExpenseRequestDto(
        @NotNull(message = "La categoria es obligatoria")
        ExpenseCategory category,
        @NotBlank(message = "La descripción es obligatoria")
        String description,
        @NotNull(message = "El importe es obligatorio")
        @Positive(message = "El importe debe ser mayor que cero")
        @JsonDeserialize(using = LenientBigDecimalDeserializer.class)
        BigDecimal amount
) {
}
