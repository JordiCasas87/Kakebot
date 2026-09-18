package com.jordi.kakebot.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.jordi.kakebot.TestcontainersConfiguration;
import com.jordi.kakebot.telegram.dto.TelegramChatDto;
import com.jordi.kakebot.telegram.dto.TelegramFromDto;
import com.jordi.kakebot.telegram.dto.TelegramMessageDto;
import com.jordi.kakebot.telegram.dto.TelegramWebhookRequestDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TelegramEndToEndTest {

    private static final String TELEGRAM_TOKEN = "test-token";
    private static final WireMockServer TELEGRAM_API = new WireMockServer(
            options().dynamicPort().http2PlainDisabled(true)
    );

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void startTelegramApi() {
        TELEGRAM_API.start();
    }

    @AfterAll
    static void stopTelegramApi() {
        TELEGRAM_API.stop();
    }

    @DynamicPropertySource
    static void configureTelegramApi(DynamicPropertyRegistry registry) {
        if (!TELEGRAM_API.isRunning()) {
            TELEGRAM_API.start();
        }
        registry.add("telegram.bot.token", () -> TELEGRAM_TOKEN);
        registry.add("telegram.bot.base-url", () -> TELEGRAM_API.baseUrl() + "/bot");
    }

    @Test
    void receivesWebhookAndSendsResponseThroughTelegramApi() {
        TELEGRAM_API.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(
                        urlEqualTo("/bot" + TELEGRAM_TOKEN + "/sendMessage")
                )
                .willReturn(ok()));
        TelegramWebhookRequestDto webhook = new TelegramWebhookRequestDto(
                100L,
                new TelegramMessageDto(
                        200L,
                        new TelegramFromDto(300L, "Jordi", "jordi"),
                        new TelegramChatDto(400L, "private"),
                        "/start",
                        1_758_192_000L
                )
        );

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/telegram/webhook",
                webhook,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TELEGRAM_API.verify(postRequestedFor(urlEqualTo("/bot" + TELEGRAM_TOKEN + "/sendMessage"))
                .withRequestBody(equalToJson("""
                        {
                          "chat_id": 400,
                          "text": "Bienvenido a KakeBot. Registrate en la web y usa /link CODIGO para vincular tu cuenta."
                        }
                        """)));
    }
}
