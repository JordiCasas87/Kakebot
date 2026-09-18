package com.jordi.kakebot.telegram.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.telegram.client.TelegramClient;
import com.jordi.kakebot.user.model.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramAlertServiceTest {

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private User user;

    private TelegramAlertService service;

    @BeforeEach
    void setUp() {
        service = new TelegramAlertService(telegramClient);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void sendCategoryLimitExceededAlertDoesNothingWithoutTelegramLink(String externalId) {
        when(user.getExternalId()).thenReturn(externalId);

        service.sendCategoryLimitExceededAlert(
                user,
                ExpenseCategory.FOOD,
                new BigDecimal("100.00"),
                new BigDecimal("125.50")
        );

        verifyNoInteractions(telegramClient);
    }

    @Test
    void sendCategoryLimitExceededAlertUsesExternalIdAsChatIdAndSendsPhoto() {
        when(user.getExternalId()).thenReturn("123456");

        service.sendCategoryLimitExceededAlert(
                user,
                ExpenseCategory.FOOD,
                new BigDecimal("100.00"),
                new BigDecimal("125.50")
        );

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramClient).sendPhoto(
                org.mockito.ArgumentMatchers.eq(123456L),
                org.mockito.ArgumentMatchers.eq("static/images/sadBotTelegram.png"),
                messageCaptor.capture()
        );
        assertThat(messageCaptor.getValue())
                .contains("Alerta de gasto")
                .contains("Te has pasado en Comida")
                .contains("100,00")
                .contains("125,50");
    }

    @ParameterizedTest
    @ValueSource(strings = {"HOME", "FOOD", "TRANSPORT", "LEISURE", "OTHER"})
    void sendCategoryLimitExceededAlertFormatsEveryCategory(String categoryName) {
        when(user.getExternalId()).thenReturn("123456");
        ExpenseCategory category = ExpenseCategory.valueOf(categoryName);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        service.sendCategoryLimitExceededAlert(user, category, BigDecimal.TEN, new BigDecimal("11.00"));

        verify(telegramClient).sendPhoto(
                org.mockito.ArgumentMatchers.eq(123456L),
                org.mockito.ArgumentMatchers.eq("static/images/sadBotTelegram.png"),
                messageCaptor.capture()
        );
        assertThat(messageCaptor.getValue()).contains(expectedCategoryName(category));
    }

    private String expectedCategoryName(ExpenseCategory category) {
        return switch (category) {
            case HOME -> "Casa";
            case FOOD -> "Comida";
            case TRANSPORT -> "Transporte";
            case LEISURE -> "Ocio";
            case OTHER -> "Otros";
        };
    }
}
