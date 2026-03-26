package com.jordi.kakebot.telegram.controller;

import com.jordi.kakebot.telegram.dto.TelegramWebhookRequestDto;
import com.jordi.kakebot.telegram.service.TelegramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telegram")
public class TelegramController {

    private final TelegramService telegramService;

    public TelegramController(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhookUpdate(@RequestBody TelegramWebhookRequestDto request) {
        telegramService.processWebhookUpdate(request);
        return ResponseEntity.ok().build();
    }
}
