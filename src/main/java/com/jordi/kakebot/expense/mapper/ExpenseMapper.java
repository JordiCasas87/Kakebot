package com.jordi.kakebot.expense.mapper;

import com.jordi.kakebot.expense.dto.ExpenseRequestDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.model.Expense;
import com.jordi.kakebot.user.model.User;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseRequestDto request, User user, LocalDateTime registeredAt) {
        return new Expense(
                user,
                request.category(),
                request.description().trim(),
                request.amount(),
                registeredAt
        );
    }

    public ExpenseResponseDto toResponseDto(Expense expense) {
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getRegisteredAt()
        );
    }
}
