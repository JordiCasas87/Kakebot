package com.jordi.kakebot.telegram.service;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.telegram.client.TelegramClient;
import com.jordi.kakebot.user.model.User;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class TelegramAlertService {

    private static final String SAD_BOT_IMAGE_PATH = "images/sadBot.png";

    private final TelegramClient telegramClient;

    public TelegramAlertService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendCategoryLimitExceededAlert(
            User user,
            ExpenseCategory category,
            BigDecimal monthlyLimit,
            BigDecimal currentMonthlySpent
    ) {
        if (user.getExternalId() == null || user.getExternalId().isBlank()) {
            return;
        }

        Long chatId = Long.valueOf(user.getExternalId());

        String message = """
                Cuidado. Alerta de gasto.

                Te has pasado en %s.
                Tope: %s
                Llevas: %s
                """.formatted(
                formatCategoryForAlert(category),
                formatAmount(monthlyLimit),
                formatAmount(currentMonthlySpent)
        );

        telegramClient.sendPhoto(chatId, SAD_BOT_IMAGE_PATH, message);
    }

    private String formatCategoryForAlert(ExpenseCategory category) {
        return switch (category) {
            case HOME -> "Casa";
            case FOOD -> "Comida";
            case TRANSPORT -> "Transporte";
            case LEISURE -> "Ocio";
            case OTHER -> "Otros";
        };
    }

    private String formatAmount(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(new Locale("es", "ES")).format(amount);
    }
}
