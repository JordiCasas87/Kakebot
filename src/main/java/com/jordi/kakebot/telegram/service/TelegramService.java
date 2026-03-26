package com.jordi.kakebot.telegram.service;

import com.jordi.kakebot.telegram.dto.TelegramMessageDto;
import com.jordi.kakebot.telegram.dto.TelegramWebhookRequestDto;
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
    }
}
