package com.jordi.kakebot.telegram.dto;

public record TelegramWebhookRequestDto(
        Long updateId,
        TelegramMessageDto message
) {
}
