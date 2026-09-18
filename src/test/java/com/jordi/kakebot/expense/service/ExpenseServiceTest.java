package com.jordi.kakebot.expense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jordi.kakebot.expense.dto.CategoryTotalResponseDto;
import com.jordi.kakebot.expense.dto.ExpenseRequestDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.exception.ExpenseNotFoundException;
import com.jordi.kakebot.expense.exception.ExpenseUserNotFoundException;
import com.jordi.kakebot.expense.exception.InvalidExpenseRequestException;
import com.jordi.kakebot.expense.mapper.ExpenseMapper;
import com.jordi.kakebot.expense.model.Expense;
import com.jordi.kakebot.expense.repository.ExpenseRepository;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.repository.UserRepository;
import com.jordi.kakebot.user.service.CategoryLimitAlertService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    private static final Long USER_ID = 7L;
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-18T10:15:30Z"),
            ZoneId.of("Europe/Madrid")
    );

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private CategoryLimitAlertService categoryLimitAlertService;

    @Mock
    private User user;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(
                expenseRepository,
                userRepository,
                expenseMapper,
                categoryLimitAlertService,
                FIXED_CLOCK
        );
    }

    @Test
    void createExpenseSavesProcessesAlertAndReturnsResponse() {
        givenExistingUser();
        ExpenseRequestDto request = new ExpenseRequestDto(ExpenseCategory.FOOD, "Compra", new BigDecimal("12.50"));
        Expense mappedExpense = mock(Expense.class);
        Expense savedExpense = mock(Expense.class);
        ExpenseResponseDto expected = response(1L, ExpenseCategory.FOOD, "Compra", "12.50");
        ArgumentCaptor<LocalDateTime> registeredAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        when(expenseMapper.toEntity(org.mockito.ArgumentMatchers.eq(request), org.mockito.ArgumentMatchers.eq(user), registeredAtCaptor.capture()))
                .thenReturn(mappedExpense);
        when(expenseRepository.save(mappedExpense)).thenReturn(savedExpense);
        when(expenseMapper.toResponseDto(savedExpense)).thenReturn(expected);

        ExpenseResponseDto result = expenseService.createExpense(USER_ID, request);

        assertThat(result).isEqualTo(expected);
        assertThat(registeredAtCaptor.getValue()).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
        verify(categoryLimitAlertService).processLimitAlertAfterExpenseCreated(savedExpense);
    }

    @Test
    void createExpenseThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ExpenseRequestDto request = new ExpenseRequestDto(ExpenseCategory.OTHER, "Prueba", BigDecimal.ONE);

        assertThatThrownBy(() -> expenseService.createExpense(USER_ID, request))
                .isInstanceOf(ExpenseUserNotFoundException.class)
                .hasMessage("No existe usuario con id: 7");

        verifyNoInteractions(expenseMapper, expenseRepository, categoryLimitAlertService);
    }

    @Test
    void createExpenseThrowsWhenUserIdIsNull() {
        ExpenseRequestDto request = new ExpenseRequestDto(ExpenseCategory.OTHER, "Prueba", BigDecimal.ONE);

        assertThatThrownBy(() -> expenseService.createExpense(null, request))
                .isInstanceOf(InvalidExpenseRequestException.class)
                .hasMessage("El id de usuario es obligatorio");

        verifyNoInteractions(userRepository, expenseMapper, expenseRepository, categoryLimitAlertService);
    }

    @Test
    void getTodayExpensesQueriesExactDayAndKeepsRepositoryOrder() {
        givenExistingUserWithId();
        Expense newest = mock(Expense.class);
        Expense oldest = mock(Expense.class);
        ExpenseResponseDto newestResponse = response(2L, ExpenseCategory.LEISURE, "Cine", "9.00");
        ExpenseResponseDto oldestResponse = response(1L, ExpenseCategory.FOOD, "Cafe", "2.00");
        LocalDateTime start = LocalDateTime.of(2026, 3, 18, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 18, 23, 59, 59, 999_999_999);

        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(USER_ID, start, end))
                .thenReturn(List.of(newest, oldest));
        when(expenseMapper.toResponseDto(newest)).thenReturn(newestResponse);
        when(expenseMapper.toResponseDto(oldest)).thenReturn(oldestResponse);

        List<ExpenseResponseDto> result = expenseService.getTodayExpenses(USER_ID);

        assertThat(result).containsExactly(newestResponse, oldestResponse);
    }

    @Test
    void getMonthExpensesQueriesCurrentMonthBoundaries() {
        givenExistingUserWithId();
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999_999_999);
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(USER_ID, start, end))
                .thenReturn(List.of());

        assertThat(expenseService.getMonthExpenses(USER_ID)).isEmpty();

        verify(expenseRepository).findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(USER_ID, start, end);
    }

    @Test
    void getExpensesByMonthQueriesRequestedMonthBoundaries() {
        givenExistingUserWithId();
        LocalDateTime start = LocalDateTime.of(2024, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 2, 29, 23, 59, 59, 999_999_999);
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(USER_ID, start, end))
                .thenReturn(List.of());

        assertThat(expenseService.getExpensesByMonth(USER_ID, 2024, 2)).isEmpty();

        verify(expenseRepository).findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(USER_ID, start, end);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 13})
    void getExpensesByMonthRejectsInvalidMonth(int month) {
        givenExistingUser();

        assertThatThrownBy(() -> expenseService.getExpensesByMonth(USER_ID, 2026, month))
                .isInstanceOf(InvalidExpenseRequestException.class)
                .hasMessage("El mes debe estar entre 1 y 12");

        verifyNoInteractions(expenseRepository);
    }

    @ParameterizedTest
    @ValueSource(ints = {1999, 2101})
    void getExpensesByMonthRejectsInvalidYear(int year) {
        givenExistingUser();

        assertThatThrownBy(() -> expenseService.getExpensesByMonth(USER_ID, year, 3))
                .isInstanceOf(InvalidExpenseRequestException.class)
                .hasMessage("El anio debe estar entre 2000 y 2100");

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void getTodayTotalAddsAllExpenses() {
        givenExistingUserWithId();
        Expense first = expense(ExpenseCategory.FOOD, "10.25");
        Expense second = expense(ExpenseCategory.TRANSPORT, "4.75");
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(
                org.mockito.ArgumentMatchers.eq(USER_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(first, second));

        assertThat(expenseService.getTodayTotal(USER_ID).total()).isEqualByComparingTo("15.00");
    }

    @Test
    void getTodayTotalReturnsZeroWhenThereAreNoExpenses() {
        givenExistingUserWithId();
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(
                org.mockito.ArgumentMatchers.eq(USER_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThat(expenseService.getTodayTotal(USER_ID).total()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getMonthTotalAddsExpensesAndQueriesMonthBoundaries() {
        givenExistingUserWithId();
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999_999_999);
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(USER_ID, start, end))
                .thenReturn(List.of(expense(ExpenseCategory.HOME, "20.00"), expense(ExpenseCategory.FOOD, "5.50")));

        assertThat(expenseService.getMonthTotal(USER_ID).total()).isEqualByComparingTo("25.50");
    }

    @Test
    void getMonthTotalByCategorySumsEachCategoryAndIncludesEmptyCategories() {
        givenExistingUserWithId();
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(
                org.mockito.ArgumentMatchers.eq(USER_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        expense(ExpenseCategory.FOOD, "10.00"),
                        expense(ExpenseCategory.FOOD, "2.50"),
                        expense(ExpenseCategory.TRANSPORT, "3.00")
                ));

        Map<ExpenseCategory, BigDecimal> totals = expenseService.getMonthTotalByCategory(USER_ID).stream()
                .collect(Collectors.toMap(CategoryTotalResponseDto::category, CategoryTotalResponseDto::total));

        assertThat(totals).hasSize(ExpenseCategory.values().length);
        assertThat(totals.get(ExpenseCategory.FOOD)).isEqualByComparingTo("12.50");
        assertThat(totals.get(ExpenseCategory.TRANSPORT)).isEqualByComparingTo("3.00");
        assertThat(totals.get(ExpenseCategory.HOME)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totals.get(ExpenseCategory.LEISURE)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totals.get(ExpenseCategory.OTHER)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCategoryTotalsByMonthUsesRequestedPeriod() {
        givenExistingUserWithId();
        LocalDateTime start = LocalDateTime.of(2025, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 30, 23, 59, 59, 999_999_999);
        when(expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(USER_ID, start, end))
                .thenReturn(List.of());

        List<CategoryTotalResponseDto> result = expenseService.getCategoryTotalsByMonth(USER_ID, 2025, 4);

        assertThat(result).hasSize(ExpenseCategory.values().length);
        verify(expenseRepository).findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(USER_ID, start, end);
    }

    @Test
    void deleteExpenseDeletesExpenseOwnedByUser() {
        givenExistingUserWithId();
        Expense storedExpense = mock(Expense.class);
        User owner = mock(User.class);
        when(expenseRepository.findById(3L)).thenReturn(Optional.of(storedExpense));
        when(storedExpense.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(USER_ID);

        expenseService.deleteExpense(USER_ID, 3L);

        verify(expenseRepository).delete(storedExpense);
    }

    @Test
    void deleteExpenseRejectsNullExpenseId() {
        givenExistingUser();

        assertThatThrownBy(() -> expenseService.deleteExpense(USER_ID, null))
                .isInstanceOf(InvalidExpenseRequestException.class)
                .hasMessage("El id del gasto es obligatorio");

        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void deleteExpenseThrowsWhenExpenseDoesNotExist() {
        givenExistingUser();
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(USER_ID, 99L))
                .isInstanceOf(ExpenseNotFoundException.class)
                .hasMessage("No existe gasto con id: 99");

        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void deleteExpenseDoesNotRevealOrDeleteAnotherUsersExpense() {
        givenExistingUserWithId();
        Expense storedExpense = mock(Expense.class);
        User anotherOwner = mock(User.class);
        when(expenseRepository.findById(3L)).thenReturn(Optional.of(storedExpense));
        when(storedExpense.getUser()).thenReturn(anotherOwner);
        when(anotherOwner.getId()).thenReturn(999L);

        assertThatThrownBy(() -> expenseService.deleteExpense(USER_ID, 3L))
                .isInstanceOf(ExpenseNotFoundException.class)
                .hasMessage("No existe gasto con id: 3");

        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void getRecentExpensesRespectsRequestedLimitAndOrder() {
        givenExistingUserWithId();
        Expense newest = mock(Expense.class);
        Expense second = mock(Expense.class);
        Expense ignored = mock(Expense.class);
        ExpenseResponseDto newestResponse = response(3L, ExpenseCategory.OTHER, "Tres", "3.00");
        ExpenseResponseDto secondResponse = response(2L, ExpenseCategory.OTHER, "Dos", "2.00");
        when(expenseRepository.findByUserIdOrderByRegisteredAtDesc(USER_ID))
                .thenReturn(List.of(newest, second, ignored));
        when(expenseMapper.toResponseDto(newest)).thenReturn(newestResponse);
        when(expenseMapper.toResponseDto(second)).thenReturn(secondResponse);

        List<ExpenseResponseDto> result = expenseService.getRecentExpenses(USER_ID, 2);

        assertThat(result).containsExactly(newestResponse, secondResponse);
        verify(expenseMapper, never()).toResponseDto(ignored);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void getRecentExpensesUsesDefaultLimitForMissingOrNonPositiveValue(Integer limit) {
        givenExistingUserWithId();
        List<Expense> expenses = mockedExpenses(12);
        ExpenseResponseDto mappedResponse = response(1L, ExpenseCategory.OTHER, "Reciente", "1.00");
        when(expenseRepository.findByUserIdOrderByRegisteredAtDesc(USER_ID)).thenReturn(expenses);
        when(expenseMapper.toResponseDto(any(Expense.class))).thenReturn(mappedResponse);

        assertThat(expenseService.getRecentExpenses(USER_ID, limit)).hasSize(10);
    }

    @Test
    void getRecentExpensesCapsLimitAtOneHundred() {
        givenExistingUserWithId();
        List<Expense> expenses = mockedExpenses(105);
        ExpenseResponseDto mappedResponse = response(1L, ExpenseCategory.OTHER, "Reciente", "1.00");
        when(expenseRepository.findByUserIdOrderByRegisteredAtDesc(USER_ID)).thenReturn(expenses);
        when(expenseMapper.toResponseDto(any(Expense.class))).thenReturn(mappedResponse);

        assertThat(expenseService.getRecentExpenses(USER_ID, 500)).hasSize(100);
    }

    private void givenExistingUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    private void givenExistingUserWithId() {
        givenExistingUser();
        when(user.getId()).thenReturn(USER_ID);
    }

    private Expense expense(ExpenseCategory category, String amount) {
        return new Expense(user, category, "Descripcion", new BigDecimal(amount), LocalDateTime.now(FIXED_CLOCK));
    }

    private ExpenseResponseDto response(Long id, ExpenseCategory category, String description, String amount) {
        return new ExpenseResponseDto(id, category, description, new BigDecimal(amount), LocalDateTime.now(FIXED_CLOCK));
    }

    private List<Expense> mockedExpenses(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> mock(Expense.class))
                .toList();
    }
}
