package com.jordi.kakebot.expense.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jordi.kakebot.TestcontainersConfiguration;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.model.Expense;
import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ExpenseRepositoryIntegrationTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesExpenseWithItsUser() {
        User user = saveUser("jordi");
        Expense expense = expense(user, ExpenseCategory.FOOD, "Compra", "23.50", LocalDateTime.of(2026, 3, 10, 12, 0));

        Expense savedExpense = expenseRepository.saveAndFlush(expense);

        assertThat(savedExpense.getId()).isNotNull();
        assertThat(savedExpense.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedExpense.getAmount()).isEqualByComparingTo("23.50");
    }

    @Test
    void findsOnlyExpensesBelongingToRequestedUser() {
        User jordi = saveUser("jordi");
        User maria = saveUser("maria");
        expenseRepository.save(expense(jordi, ExpenseCategory.FOOD, "Compra", "20.00", LocalDateTime.of(2026, 3, 10, 10, 0)));
        expenseRepository.save(expense(maria, ExpenseCategory.HOME, "Alquiler", "800.00", LocalDateTime.of(2026, 3, 10, 11, 0)));
        expenseRepository.flush();

        List<Expense> result = expenseRepository.findByUserIdOrderByRegisteredAtDesc(jordi.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Compra");
    }

    @Test
    void ordersUserExpensesByRegistrationDateDescending() {
        User user = saveUser("jordi");
        expenseRepository.save(expense(user, ExpenseCategory.FOOD, "Primero", "10.00", LocalDateTime.of(2026, 3, 1, 9, 0)));
        expenseRepository.save(expense(user, ExpenseCategory.FOOD, "Ultimo", "20.00", LocalDateTime.of(2026, 3, 20, 9, 0)));
        expenseRepository.save(expense(user, ExpenseCategory.FOOD, "Medio", "15.00", LocalDateTime.of(2026, 3, 10, 9, 0)));
        expenseRepository.flush();

        List<Expense> result = expenseRepository.findByUserIdOrderByRegisteredAtDesc(user.getId());

        assertThat(result).extracting(Expense::getDescription)
                .containsExactly("Ultimo", "Medio", "Primero");
    }

    @Test
    void findsExpensesInsideDateRangeIncludingBoundaries() {
        User user = saveUser("jordi");
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 31, 23, 59, 59);
        expenseRepository.save(expense(user, ExpenseCategory.FOOD, "Anterior", "5.00", start.minusSeconds(1)));
        expenseRepository.save(expense(user, ExpenseCategory.FOOD, "Inicio", "10.00", start));
        expenseRepository.save(expense(user, ExpenseCategory.FOOD, "Final", "15.00", end));
        expenseRepository.save(expense(user, ExpenseCategory.FOOD, "Posterior", "20.00", end.plusSeconds(1)));
        expenseRepository.flush();

        List<Expense> result = expenseRepository
                .findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(user.getId(), start, end);

        assertThat(result).extracting(Expense::getDescription)
                .containsExactly("Final", "Inicio");
    }

    @Test
    void dateRangeQueryDoesNotMixDifferentUsers() {
        User jordi = saveUser("jordi");
        User maria = saveUser("maria");
        LocalDateTime date = LocalDateTime.of(2026, 3, 10, 12, 0);
        expenseRepository.save(expense(jordi, ExpenseCategory.FOOD, "Jordi", "10.00", date));
        expenseRepository.save(expense(maria, ExpenseCategory.FOOD, "Maria", "20.00", date));
        expenseRepository.flush();

        List<Expense> result = expenseRepository.findByUserIdAndRegisteredAtBetweenOrderByRegisteredAtDesc(
                jordi.getId(), date.minusDays(1), date.plusDays(1));

        assertThat(result).extracting(Expense::getDescription).containsExactly("Jordi");
    }

    @Test
    void findsExpenseOnlyWhenIdAndUserMatch() {
        User owner = saveUser("owner");
        User anotherUser = saveUser("another");
        Expense savedExpense = expenseRepository.saveAndFlush(
                expense(owner, ExpenseCategory.OTHER, "Libro", "12.00", LocalDateTime.now())
        );

        assertThat(expenseRepository.findByIdAndUserId(savedExpense.getId(), owner.getId())).isPresent();
        assertThat(expenseRepository.findByIdAndUserId(savedExpense.getId(), anotherUser.getId())).isEmpty();
    }

    private User saveUser(String username) {
        return userRepository.saveAndFlush(
                new User(UserProvider.LOCAL, username, "password-hash", null, LocalDateTime.now())
        );
    }

    private Expense expense(
            User user,
            ExpenseCategory category,
            String description,
            String amount,
            LocalDateTime registeredAt
    ) {
        return new Expense(user, category, description, new BigDecimal(amount), registeredAt);
    }
}
