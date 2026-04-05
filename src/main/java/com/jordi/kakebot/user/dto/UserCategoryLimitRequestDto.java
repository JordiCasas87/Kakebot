package com.jordi.kakebot.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.jordi.kakebot.expense.json.LenientBigDecimalDeserializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record UserCategoryLimitRequestDto(
        @NotNull(message = "El limite mensual es obligatorio")
        @Positive(message = "El limite mensual debe ser mayor que cero")
        @JsonDeserialize(using = LenientBigDecimalDeserializer.class)
        BigDecimal monthlyLimit
) {
}
