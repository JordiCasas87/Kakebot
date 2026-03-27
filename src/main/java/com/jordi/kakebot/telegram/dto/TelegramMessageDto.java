package com.jordi.kakebot.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramMessageDto(
        @JsonProperty("message_id")
        Long messageId,
        TelegramFromDto from,
        TelegramChatDto chat,
        String text,
        Long date
) {
}
