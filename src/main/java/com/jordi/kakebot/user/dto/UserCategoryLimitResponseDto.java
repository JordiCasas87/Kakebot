package com.jordi.kakebot.user.dto;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import java.math.BigDecimal;

public record UserCategoryLimitResponseDto(
        ExpenseCategory category,
        BigDecimal monthlyLimit
) {
}
