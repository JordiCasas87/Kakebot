package com.jordi.kakebot.user.service;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.model.Expense;
import com.jordi.kakebot.expense.repository.ExpenseRepository;
import com.jordi.kakebot.telegram.service.TelegramService;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.model.UserCategoryLimit;
import com.jordi.kakebot.user.repository.UserCategoryLimitRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CategoryLimitAlertService {

    private final UserCategoryLimitRepository userCategoryLimitRepository;
    private final ExpenseRepository expenseRepository;
    private final TelegramService telegramService;

    public CategoryLimitAlertService(
            UserCategoryLimitRepository userCategoryLimitRepository,
            ExpenseRepository expenseRepository,
            TelegramService telegramService
    ) {
        this.userCategoryLimitRepository = userCategoryLimitRepository;
        this.expenseRepository = expenseRepository;
        this.telegramService = telegramService;
    }

    public void processLimitAlertAfterExpenseCreated(Expense expense) {
        User user = expense.getUser();
        ExpenseCategory category = expense.getCategory();

        Optional<UserCategoryLimit> optionalCategoryLimit = userCategoryLimitRepository.findByUserIdAndCategory(
                user.getId(),
                category
        );

        if (optionalCategoryLimit.isEmpty()) {
            return;
        }

        UserCategoryLimit categoryLimit = optionalCategoryLimit.get();
        BigDecimal currentMonthlySpent = calculateCurrentMonthlySpent(user.getId(), category, expense.getRegisteredAt());

        if (currentMonthlySpent.compareTo(categoryLimit.getMonthlyLimit()) <= 0) {
            return;
        }

        int currentYear = expense.getRegisteredAt().getYear();
        int currentMonth = expense.getRegisteredAt().getMonthValue();

        if (wasAlertAlreadySentThisMonth(categoryLimit, currentYear, currentMonth)) {
            return;
        }

        telegramService.sendCategoryLimitExceededAlert(
                user,
                category,
                categoryLimit.getMonthlyLimit(),
                currentMonthlySpent
        );

        categoryLimit.setLastAlertSentYear(currentYear);
        categoryLimit.setLastAlertSentMonth(currentMonth);
        userCategoryLimitRepository.save(categoryLimit);
    }

    private BigDecimal calculateCurrentMonthlySpent(Long userId, ExpenseCategory category, LocalDateTime registeredAt) {
        YearMonth yearMonth = YearMonth.from(registeredAt);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        return expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(userId, startOfMonth, endOfMonth)
                .stream()
                .filter(expense -> expense.getCategory() == category)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean wasAlertAlreadySentThisMonth(UserCategoryLimit categoryLimit, int currentYear, int currentMonth) {
        return Integer.valueOf(currentYear).equals(categoryLimit.getLastAlertSentYear())
                && Integer.valueOf(currentMonth).equals(categoryLimit.getLastAlertSentMonth());
    }
}
