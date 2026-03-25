package com.jordi.kakebot.user.dto;

import com.jordi.kakebot.user.enums.UserProvider;
import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String username,
        UserProvider provider,
        String externalId,
        LocalDateTime createdAt
) {
}
