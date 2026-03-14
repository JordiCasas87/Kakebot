package com.jordi.kakebot.expense.controller;

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
    public ResponseEntity<?> createExpense(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ExpenseRequestDto request
    ) {
        ExpenseResponseDto response = expenseService.createExpense(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/today")
    public ResponseEntity<List<ExpenseResponseDto>> getTodayExpenses() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }


    @GetMapping("/month")
    public ResponseEntity<List<ExpenseResponseDto>> getMonthExpenses() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }


    @GetMapping("/total/today")
    public ResponseEntity<TotalResponseDto> getTodayTotal() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }


    @GetMapping("/total/month")
    public ResponseEntity<TotalResponseDto> getMonthTotal() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }


    @GetMapping("/recent")
    public ResponseEntity<List<ExpenseResponseDto>> getRecentExpenses() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
