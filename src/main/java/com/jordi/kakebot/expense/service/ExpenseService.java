package com.jordi.kakebot.expense.service;

import com.jordi.kakebot.expense.dto.ExpenseRequestDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.exception.ExpenseUserNotFoundException;
import com.jordi.kakebot.expense.exception.InvalidExpenseRequestException;
import com.jordi.kakebot.expense.mapper.ExpenseMapper;
import com.jordi.kakebot.expense.model.Expense;
import com.jordi.kakebot.expense.repository.ExpenseRepository;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository, ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.expenseMapper = expenseMapper;
    }

    public ExpenseResponseDto createExpense(Long userId, ExpenseRequestDto request) {
        if (userId == null) {
            throw new InvalidExpenseRequestException("El id de usuario es obligatorio");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ExpenseUserNotFoundException(userId));

        Expense expense = expenseMapper.toEntity(request, user, LocalDateTime.now());

        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponseDto(savedExpense);
    }
}
