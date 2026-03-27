package com.jordi.kakebot.telegram.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TelegramClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramClient.class);

    public void sendMessage(Long chatId, String text) {
        LOGGER.info("Sending Telegram message to chat {}: {}", chatId, text);
    }
}
