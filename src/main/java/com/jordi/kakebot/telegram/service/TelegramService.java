package com.jordi.kakebot.telegram.service;

import com.jordi.kakebot.telegram.dto.TelegramMessageDto;
import com.jordi.kakebot.telegram.dto.TelegramWebhookRequestDto;
import com.jordi.kakebot.telegram.enums.TelegramMessageType;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {

    public void processWebhookUpdate(TelegramWebhookRequestDto request) {
        TelegramMessageDto message = request.message();

        if (message == null || message.text() == null || message.text().isBlank()) {
            return;
        }

        Long telegramUserId = message.from() != null ? message.from().id() : null;
        Long chatId = message.chat() != null ? message.chat().id() : null;
        String text = message.text().trim();

        // Next step: route commands like /link CODIGO using telegramUserId, chatId and text.
        if (telegramUserId == null || chatId == null || text.isBlank()) {
            return;
        }

        TelegramMessageType messageType = resolveMessageType(text);
        routeMessageByType(messageType, telegramUserId, chatId, text);
    }

    private TelegramMessageType resolveMessageType(String text) {
        if (text.equalsIgnoreCase("/start")) {
            return TelegramMessageType.START_COMMAND;
        }

        if (text.equalsIgnoreCase("/help")) {
            return TelegramMessageType.HELP_COMMAND;
        }

        if (text.toLowerCase().startsWith("/link ")) {
            return TelegramMessageType.LINK_COMMAND;
        }

        if (!text.startsWith("/")) {
            return TelegramMessageType.EXPENSE_TEXT;
        }

        return TelegramMessageType.UNKNOWN;
    }

    private void routeMessageByType(
            TelegramMessageType messageType,
            Long telegramUserId,
            Long chatId,
            String text
    ) {
        switch (messageType) {
            case START_COMMAND -> handleStartCommand(telegramUserId, chatId);
            case HELP_COMMAND -> handleHelpCommand(telegramUserId, chatId);
            case LINK_COMMAND -> handleLinkCommand(telegramUserId, chatId, text);
            case EXPENSE_TEXT -> handleExpenseText(telegramUserId, chatId, text);
            case UNKNOWN -> handleUnknownCommand(telegramUserId, chatId, text);
        }
    }

    private void handleStartCommand(Long telegramUserId, Long chatId) {
        // Next step: send welcome instructions through Telegram client.
    }

    private void handleHelpCommand(Long telegramUserId, Long chatId) {
        // Next step: send available commands through Telegram client.
    }

    private void handleLinkCommand(Long telegramUserId, Long chatId, String text) {
        // Next step: extract link code and delegate account linking to UserService.
    }

    private void handleExpenseText(Long telegramUserId, Long chatId, String text) {
        // Next step: verify linked Telegram user and delegate expense creation to ExpenseService.
    }

    private void handleUnknownCommand(Long telegramUserId, Long chatId, String text) {
        // Next step: send fallback help message through Telegram client.
    }
}
