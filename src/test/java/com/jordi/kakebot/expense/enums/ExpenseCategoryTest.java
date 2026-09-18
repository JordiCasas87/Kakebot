package com.jordi.kakebot.expense.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ExpenseCategoryTest {

    @ParameterizedTest
    @CsvSource({
            "home, HOME",
            "casa, HOME",
            "food, FOOD",
            "comida, FOOD",
            "transport, TRANSPORT",
            "transporte, TRANSPORT",
            "leisure, LEISURE",
            "ocio, LEISURE",
            "other, OTHER",
            "otros, OTHER"
    })
    void fromValueAcceptsEnglishAndSpanishNames(String input, ExpenseCategory expected) {
        assertThat(ExpenseCategory.fromValue(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "'  CoMiDa  ', FOOD",
            "' TRANSPORTE ', TRANSPORT",
            "'tránsporté', TRANSPORT"
    })
    void fromValueNormalizesSpacesCaseAndAccents(String input, ExpenseCategory expected) {
        assertThat(ExpenseCategory.fromValue(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void fromValueRejectsMissingCategory(String input) {
        assertThatThrownBy(() -> ExpenseCategory.fromValue(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Categoria obligatoria");
    }

    @ParameterizedTest
    @ValueSource(strings = {"travel", "salud", "unknown"})
    void fromValueRejectsUnknownCategory(String input) {
        assertThatThrownBy(() -> ExpenseCategory.fromValue(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Categoria invalida");
    }
}
