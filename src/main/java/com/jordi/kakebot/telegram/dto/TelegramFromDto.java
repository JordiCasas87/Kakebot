package com.jordi.kakebot.telegram.dto;

public record TelegramFromDto(
        Long id,
        String firstName,
        String username
) {
}
