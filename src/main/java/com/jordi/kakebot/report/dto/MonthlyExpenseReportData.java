package com.jordi.kakebot.report.dto;

import com.jordi.kakebot.expense.dto.CategoryTotalResponseDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlyExpenseReportData(
        String username,
        YearMonth period,
        List<CategoryTotalResponseDto> categoryTotals,
        BigDecimal total,
        List<ExpenseResponseDto> expenses
) {
}
