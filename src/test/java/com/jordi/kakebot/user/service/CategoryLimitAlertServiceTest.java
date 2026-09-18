package com.jordi.kakebot.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.model.Expense;
import com.jordi.kakebot.expense.repository.ExpenseRepository;
import com.jordi.kakebot.telegram.service.TelegramAlertService;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.model.UserCategoryLimit;
import com.jordi.kakebot.user.repository.UserCategoryLimitRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryLimitAlertServiceTest {

    private static final Long USER_ID = 7L;
    private static final LocalDateTime REGISTERED_AT = LocalDateTime.of(2026, 3, 18, 12, 30);

    @Mock
    private UserCategoryLimitRepository categoryLimitRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private TelegramAlertService telegramAlertService;

    @Mock
    private User user;

    private CategoryLimitAlertService service;

    @BeforeEach
    void setUp() {
        service = new CategoryLimitAlertService(categoryLimitRepository, expenseRepository, telegramAlertService);
        when(user.getId()).thenReturn(USER_ID);
    }

    @Test
    void processLimitAlertDoesNothingWhenCategoryHasNoLimit() {
        Expense expense = expense(ExpenseCategory.FOOD, "25.00");
        when(categoryLimitRepository.findByUserIdAndCategory(USER_ID, ExpenseCategory.FOOD))
                .thenReturn(Optional.empty());

        service.processLimitAlertAfterExpenseCreated(expense);

        verifyNoInteractions(expenseRepository, telegramAlertService);
        verify(categoryLimitRepository, never()).save(any());
    }

    @Test
    void processLimitAlertDoesNothingWhenSpendingEqualsLimit() {
        Expense expense = expense(ExpenseCategory.FOOD, "25.00");
        UserCategoryLimit limit = limit(ExpenseCategory.FOOD, "50.00");
        when(categoryLimitRepository.findByUserIdAndCategory(USER_ID, ExpenseCategory.FOOD))
                .thenReturn(Optional.of(limit));
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(
                USER_ID,
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999_999_999)
        )).thenReturn(List.of(expense(ExpenseCategory.FOOD, "20.00"), expense(ExpenseCategory.FOOD, "30.00")));

        service.processLimitAlertAfterExpenseCreated(expense);

        verifyNoInteractions(telegramAlertService);
        verify(categoryLimitRepository, never()).save(any());
    }

    @Test
    void processLimitAlertSendsOnceAndStoresAlertPeriodWhenLimitIsExceeded() {
        Expense expense = expense(ExpenseCategory.FOOD, "25.00");
        UserCategoryLimit limit = limit(ExpenseCategory.FOOD, "50.00");
        when(categoryLimitRepository.findByUserIdAndCategory(USER_ID, ExpenseCategory.FOOD))
                .thenReturn(Optional.of(limit));
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(
                USER_ID,
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999_999_999)
        )).thenReturn(List.of(
                expense(ExpenseCategory.FOOD, "30.00"),
                expense(ExpenseCategory.TRANSPORT, "500.00"),
                expense(ExpenseCategory.FOOD, "25.50")
        ));

        service.processLimitAlertAfterExpenseCreated(expense);

        verify(telegramAlertService).sendCategoryLimitExceededAlert(
                user,
                ExpenseCategory.FOOD,
                new BigDecimal("50.00"),
                new BigDecimal("55.50")
        );
        assertThat(limit.getLastAlertSentYear()).isEqualTo(2026);
        assertThat(limit.getLastAlertSentMonth()).isEqualTo(3);
        verify(categoryLimitRepository).save(limit);
    }

    @Test
    void processLimitAlertDoesNotRepeatAlertAlreadySentInSameMonth() {
        Expense expense = expense(ExpenseCategory.LEISURE, "60.00");
        UserCategoryLimit limit = limit(ExpenseCategory.LEISURE, "50.00");
        limit.setLastAlertSentYear(2026);
        limit.setLastAlertSentMonth(3);
        when(categoryLimitRepository.findByUserIdAndCategory(USER_ID, ExpenseCategory.LEISURE))
                .thenReturn(Optional.of(limit));
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(any(), any(), any()))
                .thenReturn(List.of(expense));

        service.processLimitAlertAfterExpenseCreated(expense);

        verifyNoInteractions(telegramAlertService);
        verify(categoryLimitRepository, never()).save(any());
    }

    @Test
    void processLimitAlertCanSendAgainInANewMonth() {
        Expense expense = expense(ExpenseCategory.HOME, "101.00");
        UserCategoryLimit limit = limit(ExpenseCategory.HOME, "100.00");
        limit.setLastAlertSentYear(2026);
        limit.setLastAlertSentMonth(2);
        when(categoryLimitRepository.findByUserIdAndCategory(USER_ID, ExpenseCategory.HOME))
                .thenReturn(Optional.of(limit));
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(any(), any(), any()))
                .thenReturn(List.of(expense));

        service.processLimitAlertAfterExpenseCreated(expense);

        verify(telegramAlertService).sendCategoryLimitExceededAlert(
                user,
                ExpenseCategory.HOME,
                new BigDecimal("100.00"),
                new BigDecimal("101.00")
        );
        assertThat(limit.getLastAlertSentMonth()).isEqualTo(3);
        verify(categoryLimitRepository).save(limit);
    }

    private Expense expense(ExpenseCategory category, String amount) {
        return new Expense(user, category, "Descripcion", new BigDecimal(amount), REGISTERED_AT);
    }

    private UserCategoryLimit limit(ExpenseCategory category, String monthlyLimit) {
        return new UserCategoryLimit(user, category, new BigDecimal(monthlyLimit));
    }
}
