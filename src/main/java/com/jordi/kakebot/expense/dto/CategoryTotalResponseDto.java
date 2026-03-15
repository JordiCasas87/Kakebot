package com.jordi.kakebot.expense.dto;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import java.math.BigDecimal;

public record CategoryTotalResponseDto(
        ExpenseCategory category,
        BigDecimal total
) {
}
