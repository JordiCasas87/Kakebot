package com.jordi.kakebot.common.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponseDto(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
