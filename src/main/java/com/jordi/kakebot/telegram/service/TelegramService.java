package com.jordi.kakebot.telegram.service;

import com.jordi.kakebot.telegram.client.TelegramClient;
import com.jordi.kakebot.telegram.dto.TelegramMessageDto;
import com.jordi.kakebot.telegram.dto.TelegramWebhookRequestDto;
import com.jordi.kakebot.telegram.enums.TelegramMessageType;
import com.jordi.kakebot.user.exception.InvalidUserRequestException;
import com.jordi.kakebot.user.service.UserTelegramLinkCodeService;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {

    private static final String LINK_COMMAND_PREFIX = "/link";

    private final TelegramClient telegramClient;
    private final UserTelegramLinkCodeService userTelegramLinkCodeService;

    public TelegramService(
            TelegramClient telegramClient,
            UserTelegramLinkCodeService userTelegramLinkCodeService
    ) {
        this.telegramClient = telegramClient;
        this.userTelegramLinkCodeService = userTelegramLinkCodeService;
    }

    public void processWebhookUpdate(TelegramWebhookRequestDto request) {
        TelegramMessageDto message = request.message();

        if (message == null || message.text() == null || message.text().isBlank()) {
            return;
        }

        Long telegramUserId = message.from() != null ? message.from().id() : null;
        Long chatId = message.chat() != null ? message.chat().id() : null;
        String text = message.text().trim();

        // Next step: route commands like /link CODIGO using telegramUserId, chatId and text.
        if (telegramUserId == null || chatId == null || text.isBlank()) {
            return;
        }

        TelegramMessageType messageType = resolveMessageType(text);
        routeMessageByType(messageType, telegramUserId, chatId, text);
    }

    private TelegramMessageType resolveMessageType(String text) {
        if (text.equalsIgnoreCase("/start")) {
            return TelegramMessageType.START_COMMAND;
        }

        if (text.equalsIgnoreCase("/help")) {
            return TelegramMessageType.HELP_COMMAND;
        }

        if (text.toLowerCase().startsWith("/link ")) {
            return TelegramMessageType.LINK_COMMAND;
        }

        if (!text.startsWith("/")) {
            return TelegramMessageType.EXPENSE_TEXT;
        }

        return TelegramMessageType.UNKNOWN;
    }

    private void routeMessageByType(
            TelegramMessageType messageType,
            Long telegramUserId,
            Long chatId,
            String text
    ) {
        switch (messageType) {
            case START_COMMAND -> handleStartCommand(telegramUserId, chatId);
            case HELP_COMMAND -> handleHelpCommand(telegramUserId, chatId);
            case LINK_COMMAND -> handleLinkCommand(telegramUserId, chatId, text);
            case EXPENSE_TEXT -> handleExpenseText(telegramUserId, chatId, text);
            case UNKNOWN -> handleUnknownCommand(telegramUserId, chatId, text);
        }
    }

    private void handleStartCommand(Long telegramUserId, Long chatId) {
        telegramClient.sendMessage(
                chatId,
                "Bienvenido a KakeBot. Registrate en la web y usa /link CODIGO para vincular tu cuenta."
        );
    }

    private void handleHelpCommand(Long telegramUserId, Long chatId) {
        telegramClient.sendMessage(
                chatId,
                "Comandos disponibles: /start, /help y /link CODIGO. Pronto podras registrar gastos desde Telegram."
        );
    }

    private void handleLinkCommand(Long telegramUserId, Long chatId, String text) {
        try {
            String code = extractLinkCode(text);
            userTelegramLinkCodeService.linkTelegramUser(code, telegramUserId);
            telegramClient.sendMessage(
                    chatId,
                    "Tu cuenta ha quedado vinculada correctamente. Ya puedes usar KakeBot desde Telegram."
            );
        } catch (InvalidUserRequestException exception) {
            telegramClient.sendMessage(chatId, exception.getMessage());
        }
    }

    private void handleExpenseText(Long telegramUserId, Long chatId, String text) {
        // Next step: verify linked Telegram user and delegate expense creation to ExpenseService.
    }

    private void handleUnknownCommand(Long telegramUserId, Long chatId, String text) {
        telegramClient.sendMessage(
                chatId,
                "No he entendido ese mensaje. Usa /help para ver los comandos disponibles."
        );
    }

    private String extractLinkCode(String text) {
        return text.substring(LINK_COMMAND_PREFIX.length()).trim();
    }
}
