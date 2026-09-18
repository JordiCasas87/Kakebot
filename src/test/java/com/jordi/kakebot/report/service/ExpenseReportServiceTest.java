package com.jordi.kakebot.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jordi.kakebot.expense.dto.CategoryTotalResponseDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.service.ExpenseService;
import com.jordi.kakebot.report.dto.MonthlyExpenseReportData;
import com.jordi.kakebot.user.exception.UserNotFoundException;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseReportServiceTest {

    private static final Long USER_ID = 7L;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpensePdfGenerator expensePdfGenerator;

    @Mock
    private User user;

    private ExpenseReportService service;

    @BeforeEach
    void setUp() {
        service = new ExpenseReportService(expenseService, userRepository, expensePdfGenerator);
    }

    @Test
    void generateMonthlyExpensePdfBuildsCompleteReportDataAndReturnsGeneratedBytes() {
        List<CategoryTotalResponseDto> categoryTotals = List.of(
                new CategoryTotalResponseDto(ExpenseCategory.HOME, new BigDecimal("800.50")),
                new CategoryTotalResponseDto(ExpenseCategory.FOOD, new BigDecimal("125.25")),
                new CategoryTotalResponseDto(ExpenseCategory.OTHER, BigDecimal.ZERO)
        );
        List<ExpenseResponseDto> expenses = List.of(
                new ExpenseResponseDto(
                        1L,
                        ExpenseCategory.FOOD,
                        "Compra",
                        new BigDecimal("25.25"),
                        LocalDateTime.of(2026, 2, 10, 18, 30)
                )
        );
        byte[] expectedPdf = new byte[]{1, 2, 3};
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getUsername()).thenReturn("jordi");
        when(expenseService.getCategoryTotalsByMonth(USER_ID, 2026, 2)).thenReturn(categoryTotals);
        when(expenseService.getExpensesByMonth(USER_ID, 2026, 2)).thenReturn(expenses);
        when(expensePdfGenerator.generateMonthlyReport(org.mockito.ArgumentMatchers.any()))
                .thenReturn(expectedPdf);

        byte[] result = service.generateMonthlyExpensePdf(USER_ID, 2026, 2);

        assertThat(result).isSameAs(expectedPdf);
        ArgumentCaptor<MonthlyExpenseReportData> captor = ArgumentCaptor.forClass(MonthlyExpenseReportData.class);
        verify(expensePdfGenerator).generateMonthlyReport(captor.capture());
        MonthlyExpenseReportData reportData = captor.getValue();
        assertThat(reportData.username()).isEqualTo("jordi");
        assertThat(reportData.period()).isEqualTo(YearMonth.of(2026, 2));
        assertThat(reportData.categoryTotals()).isSameAs(categoryTotals);
        assertThat(reportData.expenses()).isSameAs(expenses);
        assertThat(reportData.total()).isEqualByComparingTo("925.75");
    }

    @Test
    void generateMonthlyExpensePdfSupportsMonthWithoutExpenses() {
        byte[] expectedPdf = new byte[]{4, 5};
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getUsername()).thenReturn("jordi");
        when(expenseService.getCategoryTotalsByMonth(USER_ID, 2026, 3)).thenReturn(List.of());
        when(expenseService.getExpensesByMonth(USER_ID, 2026, 3)).thenReturn(List.of());
        when(expensePdfGenerator.generateMonthlyReport(org.mockito.ArgumentMatchers.any()))
                .thenReturn(expectedPdf);

        assertThat(service.generateMonthlyExpensePdf(USER_ID, 2026, 3)).isSameAs(expectedPdf);

        ArgumentCaptor<MonthlyExpenseReportData> captor = ArgumentCaptor.forClass(MonthlyExpenseReportData.class);
        verify(expensePdfGenerator).generateMonthlyReport(captor.capture());
        assertThat(captor.getValue().total()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(captor.getValue().expenses()).isEmpty();
    }

    @Test
    void generateMonthlyExpensePdfThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateMonthlyExpensePdf(USER_ID, 2026, 3))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("No existe usuario con id: 7");

        verifyNoInteractions(expenseService, expensePdfGenerator);
    }
}
