package com.jordi.kakebot.report.service;

import com.jordi.kakebot.expense.dto.CategoryTotalResponseDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.service.ExpenseService;
import com.jordi.kakebot.report.dto.MonthlyExpenseReportData;
import com.jordi.kakebot.user.exception.UserNotFoundException;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExpenseReportService {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;
    private final ExpensePdfGenerator expensePdfGenerator;

    public ExpenseReportService(
            ExpenseService expenseService,
            UserRepository userRepository,
            ExpensePdfGenerator expensePdfGenerator
    ) {
        this.expenseService = expenseService;
        this.userRepository = userRepository;
        this.expensePdfGenerator = expensePdfGenerator;
    }

    public byte[] generateMonthlyExpensePdf(Long userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        YearMonth period = YearMonth.of(year, month);
        List<CategoryTotalResponseDto> categoryTotals = expenseService.getCategoryTotalsByMonth(userId, year, month);
        List<ExpenseResponseDto> expenses = expenseService.getExpensesByMonth(userId, year, month);
        BigDecimal total = categoryTotals.stream()
                .map(CategoryTotalResponseDto::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MonthlyExpenseReportData reportData = new MonthlyExpenseReportData(
                user.getUsername(),
                period,
                categoryTotals,
                total,
                expenses
        );

        return expensePdfGenerator.generateMonthlyReport(reportData);
    }
}
