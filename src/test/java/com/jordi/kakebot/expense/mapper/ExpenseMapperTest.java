package com.jordi.kakebot.expense.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.jordi.kakebot.expense.dto.ExpenseRequestDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.model.Expense;
import com.jordi.kakebot.user.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ExpenseMapperTest {

    private final ExpenseMapper mapper = new ExpenseMapper();

    @Test
    void toEntityMapsRequestUserAndRegistrationDate() {
        User user = org.mockito.Mockito.mock(User.class);
        LocalDateTime registeredAt = LocalDateTime.of(2026, 3, 18, 12, 30);
        ExpenseRequestDto request = new ExpenseRequestDto(
                ExpenseCategory.FOOD,
                "  Compra semanal  ",
                new BigDecimal("23.50")
        );

        Expense result = mapper.toEntity(request, user, registeredAt);

        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getCategory()).isEqualTo(ExpenseCategory.FOOD);
        assertThat(result.getDescription()).isEqualTo("Compra semanal");
        assertThat(result.getAmount()).isEqualByComparingTo("23.50");
        assertThat(result.getRegisteredAt()).isEqualTo(registeredAt);
    }

    @Test
    void toResponseDtoMapsEveryPublicExpenseField() {
        LocalDateTime registeredAt = LocalDateTime.of(2026, 3, 18, 12, 30);
        Expense expense = new Expense(
                org.mockito.Mockito.mock(User.class),
                ExpenseCategory.TRANSPORT,
                "Metro",
                new BigDecimal("2.75"),
                registeredAt
        );

        ExpenseResponseDto result = mapper.toResponseDto(expense);

        assertThat(result.id()).isNull();
        assertThat(result.category()).isEqualTo(ExpenseCategory.TRANSPORT);
        assertThat(result.description()).isEqualTo("Metro");
        assertThat(result.amount()).isEqualByComparingTo("2.75");
        assertThat(result.registeredAt()).isEqualTo(registeredAt);
    }
}
