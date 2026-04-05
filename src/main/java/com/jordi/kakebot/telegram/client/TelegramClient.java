package com.jordi.kakebot.telegram.client;

import com.jordi.kakebot.telegram.config.TelegramProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TelegramClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramClient.class);

    private final RestClient telegramRestClient;
    private final TelegramProperties telegramProperties;

    public TelegramClient(RestClient telegramRestClient, TelegramProperties telegramProperties) {
        this.telegramRestClient = telegramRestClient;
        this.telegramProperties = telegramProperties;
    }

    public void sendMessage(Long chatId, String text) {
        if (telegramProperties.token() == null || telegramProperties.token().isBlank()) {
            LOGGER.warn("Telegram bot token is not configured. Skipping message to chat {}", chatId);
            return;
        }

        try {
            telegramRestClient.post()
                    .uri("/sendMessage")
                    .body(Map.of(
                            "chat_id", chatId,
                            "text", text
                    ))
                    .retrieve()
                    .toBodilessEntity();

            LOGGER.info("Telegram message sent to chat {}", chatId);
        } catch (RestClientException exception) {
            LOGGER.error(
                    "Telegram message could not be sent to chat {}. The main operation was completed, but the notification failed.",
                    chatId,
                    exception
            );
        }
    }

    public void sendPhoto(Long chatId, String classpathImagePath, String caption) {
        if (telegramProperties.token() == null || telegramProperties.token().isBlank()) {
            LOGGER.warn("Telegram bot token is not configured. Skipping photo to chat {}", chatId);
            return;
        }

        try {
            ClassPathResource imageResource = new ClassPathResource(classpathImagePath);
            byte[] imageBytes = readBytes(imageResource);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("chat_id", chatId.toString());
            body.add("caption", caption);
            body.add("photo", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return imageResource.getFilename();
                }
            });

            telegramRestClient.post()
                    .uri("/sendPhoto")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            LOGGER.info("Telegram photo message sent to chat {}", chatId);
        } catch (IOException | RestClientException exception) {
            LOGGER.error(
                    "Telegram photo message could not be sent to chat {}. The main operation was completed, but the notification failed.",
                    chatId,
                    exception
            );
        }
    }

    private byte[] readBytes(ClassPathResource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }
}
