package com.jordi.kakebot.telegram.service;

import com.jordi.kakebot.expense.dto.ExpenseRequestDto;
import com.jordi.kakebot.expense.dto.CategoryTotalResponseDto;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.exception.InvalidExpenseRequestException;
import com.jordi.kakebot.expense.service.ExpenseService;
import com.jordi.kakebot.telegram.client.TelegramClient;
import com.jordi.kakebot.telegram.dto.TelegramMessageDto;
import com.jordi.kakebot.telegram.dto.TelegramWebhookRequestDto;
import com.jordi.kakebot.telegram.enums.TelegramMessageType;
import com.jordi.kakebot.user.exception.InvalidUserRequestException;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.repository.UserRepository;
import com.jordi.kakebot.user.service.UserTelegramLinkCodeService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {

    private static final String LINK_COMMAND_PREFIX = "/link";
    private static final String MONTH_SUMMARY_IMAGE_PATH = "static/images/botPillopng.png";
    private static final int EXPENSE_LINES_COUNT = 3;

    private final TelegramClient telegramClient;
    private final UserTelegramLinkCodeService userTelegramLinkCodeService;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    public TelegramService(
            TelegramClient telegramClient,
            UserTelegramLinkCodeService userTelegramLinkCodeService,
            UserRepository userRepository,
            ExpenseService expenseService
    ) {
        this.telegramClient = telegramClient;
        this.userTelegramLinkCodeService = userTelegramLinkCodeService;
        this.userRepository = userRepository;
        this.expenseService = expenseService;
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

        if (text.equalsIgnoreCase("/mes")) {
            return TelegramMessageType.MONTH_SUMMARY_COMMAND;
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
            case MONTH_SUMMARY_COMMAND -> handleMonthSummaryCommand(telegramUserId, chatId);
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
                """
                Comandos disponibles:

                /start
                Te da la bienvenida a KakeBot 🤖

                /link CODIGO
                Vincula tu cuenta de Telegram con tu usuario 🔗

                /mes
                Consulta el total del mes por categorias y el total general 📊

                /help
                Muestra esta ayuda 📖

                Como registrar un gasto:
                Envia el mensaje en 3 lineas:

                categoria
                descripcion
                importe

                Categorias disponibles:
                casa 🏠
                comida 🍽️
                transporte 🚌
                ocio 🎉
                otros 🧾

                Ejemplo:
                comida
                compra en supermercado
                23,50
                """
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

    private void handleMonthSummaryCommand(Long telegramUserId, Long chatId) {
        try {
            User linkedUser = resolveLinkedUserOrThrow(telegramUserId);
            List<CategoryTotalResponseDto> categoryTotals = expenseService.getMonthTotalByCategory(linkedUser.getId());
            BigDecimal monthTotal = expenseService.getMonthTotal(linkedUser.getId()).total();

            telegramClient.sendPhoto(chatId, MONTH_SUMMARY_IMAGE_PATH, buildMonthSummaryMessage(categoryTotals, monthTotal));
        } catch (InvalidUserRequestException exception) {
            telegramClient.sendMessage(chatId, exception.getMessage());
        }
    }

    private void handleExpenseText(Long telegramUserId, Long chatId, String text) {
        try {
            User linkedUser = resolveLinkedUserOrThrow(telegramUserId);
            ExpenseRequestDto request = parseExpenseRequest(text);
            expenseService.createExpense(linkedUser.getId(), request);
            telegramClient.sendMessage(chatId, "Gasto guardado correctamente.");
        } catch (InvalidUserRequestException | InvalidExpenseRequestException exception) {
            telegramClient.sendMessage(chatId, exception.getMessage());
        }
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

    private User resolveLinkedUserOrThrow(Long telegramUserId) {
        return userRepository.findByExternalId(String.valueOf(telegramUserId))
                .orElseThrow(() -> new InvalidUserRequestException(
                        "Tu cuenta de Telegram no esta vinculada. Usa /link CODIGO desde el bot."
                ));
    }

    private ExpenseRequestDto parseExpenseRequest(String text) {
        String[] lines = text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toArray(String[]::new);

        if (lines.length != EXPENSE_LINES_COUNT) {
            throw new InvalidExpenseRequestException(
                    "Formato de gasto invalido. Envia el gasto en 3 lineas: CATEGORIA, su descripción e importe."
            );
        }

        ExpenseCategory category = parseExpenseCategory(lines[0]);
        String description = lines[1];
        BigDecimal amount = parseExpenseAmount(lines[2]);

        return new ExpenseRequestDto(category, description, amount);
    }

    private ExpenseCategory parseExpenseCategory(String rawCategory) {
        try {
            return ExpenseCategory.fromValue(rawCategory);
        } catch (IllegalArgumentException exception) {
            throw new InvalidExpenseRequestException(
                    "Categoria de gasto invalida. Usa una de estas: HOME, FOOD, TRANSPORT, LEISURE, OTHER."
            );
        }
    }

    private BigDecimal parseExpenseAmount(String rawAmount) {
        String normalizedAmount = rawAmount.trim().replace(',', '.');

        try {
            BigDecimal amount = new BigDecimal(normalizedAmount);
            if (amount.signum() <= 0) {
                throw new InvalidExpenseRequestException("El importe debe ser mayor que cero.");
            }
            return amount;
        } catch (NumberFormatException exception) {
            throw new InvalidExpenseRequestException("El importe no es valido.");
        }
    }

    private String buildMonthSummaryMessage(List<CategoryTotalResponseDto> categoryTotals, BigDecimal monthTotal) {
        StringBuilder messageBuilder = new StringBuilder("Mes y categorias\n\n");

        categoryTotals.forEach(categoryTotal -> messageBuilder
                .append(formatCategoryForSummary(categoryTotal.category()))
                .append(": ")
                .append(formatAmount(categoryTotal.total()))
                .append("\n")
        );

        messageBuilder
                .append("\nTotal del mes: ")
                .append(formatAmount(monthTotal));

        return messageBuilder.toString();
    }

    private String formatCategoryForSummary(ExpenseCategory category) {
        return switch (category) {
            case HOME -> "Casa";
            case FOOD -> "Comida";
            case TRANSPORT -> "Transporte";
            case LEISURE -> "Ocio";
            case OTHER -> "Otros";
        };
    }

    private String formatAmount(BigDecimal amount) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(new Locale("es", "ES"));
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00 €", symbols);
        return decimalFormat.format(amount.setScale(2, RoundingMode.HALF_UP));
    }
}
