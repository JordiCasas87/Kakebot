package com.jordi.kakebot.telegram.dto;

import java.time.Instant;

public record TelegramMessageDto(
        Long messageId,
        TelegramFromDto from,
        TelegramChatDto chat,
        String text,
        Instant date
) {
}
