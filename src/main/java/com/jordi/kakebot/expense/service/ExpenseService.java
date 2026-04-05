package com.jordi.kakebot.expense.service;

import com.jordi.kakebot.expense.dto.CategoryTotalResponseDto;
import com.jordi.kakebot.expense.dto.ExpenseRequestDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.dto.TotalResponseDto;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Clock;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;
    private final CategoryLimitAlertService categoryLimitAlertService;
    private final Clock clock;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            ExpenseMapper expenseMapper,
            CategoryLimitAlertService categoryLimitAlertService,
            Clock clock
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.expenseMapper = expenseMapper;
        this.categoryLimitAlertService = categoryLimitAlertService;
        this.clock = clock;
    }

    public ExpenseResponseDto createExpense(Long userId, ExpenseRequestDto request) {
        User user = resolveUserOrThrow(userId);

        Expense expense = expenseMapper.toEntity(request, user, LocalDateTime.now(clock));

        Expense savedExpense = expenseRepository.save(expense);
        categoryLimitAlertService.processLimitAlertAfterExpenseCreated(savedExpense);
        return expenseMapper.toResponseDto(savedExpense);
    }

    public List<ExpenseResponseDto> getTodayExpenses(Long userId) {
        User user = resolveUserOrThrow(userId);

        LocalDate today = LocalDate.now(clock);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return expenseRepository
                .findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(user.getId(), startOfDay, endOfDay)
                .stream()
                .map(expenseMapper::toResponseDto)
                .toList();
    }

    public List<ExpenseResponseDto> getMonthExpenses(Long userId) {
        User user = resolveUserOrThrow(userId);

        LocalDate today = LocalDate.now(clock);
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = lastDayOfMonth.atTime(LocalTime.MAX);

        return expenseRepository
                .findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(user.getId(), startOfMonth, endOfMonth)
                .stream()
                .map(expenseMapper::toResponseDto)
                .toList();
    }

    public TotalResponseDto getTodayTotal(Long userId) {
        User user = resolveUserOrThrow(userId);

        LocalDate today = LocalDate.now(clock);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        BigDecimal total = expenseRepository
                .findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(user.getId(), startOfDay, endOfDay)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TotalResponseDto(total);
    }

    public TotalResponseDto getMonthTotal(Long userId) {
        User user = resolveUserOrThrow(userId);

        LocalDate today = LocalDate.now(clock);
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = lastDayOfMonth.atTime(LocalTime.MAX);

        BigDecimal total = expenseRepository
                .findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(user.getId(), startOfMonth, endOfMonth)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TotalResponseDto(total);
    }

    public List<CategoryTotalResponseDto> getMonthTotalByCategory(Long userId) {
        User user = resolveUserOrThrow(userId);

        LocalDate today = LocalDate.now(clock);
        return getCategoryTotalsForPeriod(user.getId(), today.getYear(), today.getMonthValue());
    }

    public List<CategoryTotalResponseDto> getCategoryTotalsByMonth(Long userId, int year, int month) {
        User user = resolveUserOrThrow(userId);

        if (month < 1 || month > 12) {
            throw new InvalidExpenseRequestException("El mes debe estar entre 1 y 12");
        }
        if (year < 2000 || year > 2100) {
            throw new InvalidExpenseRequestException("El anio debe estar entre 2000 y 2100");
        }

        return getCategoryTotalsForPeriod(user.getId(), year, month);
    }

    public void deleteExpense(Long userId, Long expenseId) {
        User user = resolveUserOrThrow(userId);

        if (expenseId == null) {
            throw new InvalidExpenseRequestException("El id del gasto es obligatorio");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new ExpenseNotFoundException(expenseId);
        }

        expenseRepository.delete(expense);
    }

    public List<ExpenseResponseDto> getRecentExpenses(Long userId, Integer limit) {
        User user = resolveUserOrThrow(userId);
        int safeLimit = (limit == null || limit <= 0) ? 10 : Math.min(limit, 100);

        return expenseRepository.findByUserIdOrderByRegisteredAtDesc(user.getId())
                .stream()
                .limit(safeLimit)
                .map(expenseMapper::toResponseDto)
                .toList();
    }


    private List<CategoryTotalResponseDto> getCategoryTotalsForPeriod(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        Map<ExpenseCategory, BigDecimal> totalsByCategory = new EnumMap<>(ExpenseCategory.class);
        for (ExpenseCategory category : ExpenseCategory.values()) {
            totalsByCategory.put(category, BigDecimal.ZERO);
        }

        expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(userId, start, end)
                .forEach(expense -> totalsByCategory.merge(expense.getCategory(), expense.getAmount(), BigDecimal::add));

        return totalsByCategory.entrySet().stream()
                .map(entry -> new CategoryTotalResponseDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    private User resolveUserOrThrow(Long userId) {
        if (userId == null) {
            throw new InvalidExpenseRequestException("El id de usuario es obligatorio");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpenseUserNotFoundException(userId));
    }

}
