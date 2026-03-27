package com.jordi.kakebot.user.dto;

import java.time.LocalDateTime;

public record UserTelegramLinkCodeResponseDto(
        String code,
        LocalDateTime expiresAt
) {
}
