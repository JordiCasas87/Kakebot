package com.jordi.kakebot.expense.dto;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseResponseDto(
        Long id,
        ExpenseCategory category,
        String description,
        BigDecimal amount,
        LocalDateTime registeredAt
) {
}
