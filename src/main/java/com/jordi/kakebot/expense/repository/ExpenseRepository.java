package com.jordi.kakebot.expense.repository;

import com.jordi.kakebot.expense.model.Expense;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdOrderByRegisteredAtDesc(Long userId);

    List<Expense> findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Expense> findByIdAndUserId(Long id, Long userId);
}
