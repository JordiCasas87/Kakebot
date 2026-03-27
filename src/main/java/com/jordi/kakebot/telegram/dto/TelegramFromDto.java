package com.jordi.kakebot.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramFromDto(
        Long id,
        @JsonProperty("first_name")
        String firstName,
        String username
) {
}
