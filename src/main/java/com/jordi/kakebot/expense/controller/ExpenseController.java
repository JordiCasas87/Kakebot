package com.jordi.kakebot.expense.controller;

import com.jordi.kakebot.expense.dto.CategoryTotalResponseDto;
import com.jordi.kakebot.expense.dto.ExpenseRequestDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.dto.TotalResponseDto;
import com.jordi.kakebot.expense.service.ExpenseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ExpenseRequestDto request
    ) {
        ExpenseResponseDto response = expenseService.createExpense(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/today")
    public ResponseEntity<List<ExpenseResponseDto>> getTodayExpenses(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<ExpenseResponseDto> response = expenseService.getTodayExpenses(userId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/month")
    public ResponseEntity<List<ExpenseResponseDto>> getMonthExpenses(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<ExpenseResponseDto> response = expenseService.getMonthExpenses(userId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/total/today")
    public ResponseEntity<TotalResponseDto> getTodayTotal(
            @RequestHeader("X-User-Id") Long userId
    ) {
        TotalResponseDto response = expenseService.getTodayTotal(userId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/total/month")
    public ResponseEntity<TotalResponseDto> getMonthTotal(
            @RequestHeader("X-User-Id") Long userId
    ) {
        TotalResponseDto response = expenseService.getMonthTotal(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/total/month/by-category")
    public ResponseEntity<List<CategoryTotalResponseDto>> getMonthTotalByCategory(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<CategoryTotalResponseDto> response = expenseService.getMonthTotalByCategory(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/total/by-category/period")
    public ResponseEntity<List<CategoryTotalResponseDto>> getCategoryTotalsByPeriod(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<CategoryTotalResponseDto> response = expenseService.getCategoryTotalsByMonth(userId, year, month);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id
    ) {
        expenseService.deleteExpense(userId, id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/recent")
    public ResponseEntity<List<ExpenseResponseDto>> getRecentExpenses(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Integer limit
    ) {
        List<ExpenseResponseDto> response = expenseService.getRecentExpenses(userId, limit);
        return ResponseEntity.ok(response);
    }
}
