package com.jordi.kakebot.telegram.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jordi.kakebot.common.config.TimeConfig;
import com.jordi.kakebot.telegram.service.TelegramService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TelegramController.class)
@Import(TimeConfig.class)
class TelegramControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TelegramService telegramService;

    @Test
    void deserializesTelegramWebhookAndDelegatesIt() throws Exception {
        mockMvc.perform(post("/api/telegram/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "update_id": 100,
                                  "message": {
                                    "message_id": 200,
                                    "from": {"id": 300, "first_name": "Jordi", "username": "jordi"},
                                    "chat": {"id": 400, "type": "private"},
                                    "text": "/start",
                                    "date": 1758192000
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        verify(telegramService).processWebhookUpdate(argThat(request ->
                request.updateId().equals(100L)
                        && request.message().chat().id().equals(400L)
                        && request.message().text().equals("/start")
        ));
    }
}
