package com.jordi.kakebot.telegram.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jordi.kakebot.expense.dto.CategoryTotalResponseDto;
import com.jordi.kakebot.expense.dto.ExpenseRequestDto;
import com.jordi.kakebot.expense.dto.TotalResponseDto;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.service.ExpenseService;
import com.jordi.kakebot.telegram.client.TelegramClient;
import com.jordi.kakebot.telegram.dto.TelegramChatDto;
import com.jordi.kakebot.telegram.dto.TelegramFromDto;
import com.jordi.kakebot.telegram.dto.TelegramMessageDto;
import com.jordi.kakebot.telegram.dto.TelegramWebhookRequestDto;
import com.jordi.kakebot.user.exception.InvalidUserRequestException;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.repository.UserRepository;
import com.jordi.kakebot.user.service.UserTelegramLinkCodeService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramServiceTest {

    private static final Long TELEGRAM_USER_ID = 123456L;
    private static final Long CHAT_ID = 654321L;
    private static final Long USER_ID = 7L;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private UserTelegramLinkCodeService linkCodeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private User linkedUser;

    private TelegramService service;

    @BeforeEach
    void setUp() {
        service = new TelegramService(telegramClient, linkCodeService, userRepository, expenseService);
    }

    @Test
    void processWebhookUpdateIgnoresRequestWithoutMessage() {
        service.processWebhookUpdate(new TelegramWebhookRequestDto(1L, null));

        verifyNoInteractions(telegramClient, linkCodeService, userRepository, expenseService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void processWebhookUpdateIgnoresMissingOrBlankText(String text) {
        service.processWebhookUpdate(webhook(text, true, true));

        verifyNoInteractions(telegramClient, linkCodeService, userRepository, expenseService);
    }

    @Test
    void processWebhookUpdateIgnoresMessageWithoutSender() {
        service.processWebhookUpdate(webhook("/start", false, true));

        verifyNoInteractions(telegramClient, linkCodeService, userRepository, expenseService);
    }

    @Test
    void processWebhookUpdateIgnoresMessageWithoutChat() {
        service.processWebhookUpdate(webhook("/start", true, false));

        verifyNoInteractions(telegramClient, linkCodeService, userRepository, expenseService);
    }

    @Test
    void startCommandSendsWelcomeMessage() {
        service.processWebhookUpdate(webhook("  /START  ", true, true));

        verify(telegramClient).sendMessage(
                CHAT_ID,
                "Bienvenido a KakeBot. Registrate en la web y usa /link CODIGO para vincular tu cuenta."
        );
        verifyNoInteractions(linkCodeService, userRepository, expenseService);
    }

    @Test
    void helpCommandSendsAvailableCommandsAndExpenseFormat() {
        service.processWebhookUpdate(webhook("/help", true, true));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramClient).sendMessage(org.mockito.ArgumentMatchers.eq(CHAT_ID), messageCaptor.capture());
        assertThat(messageCaptor.getValue())
                .contains("/start", "/link CODIGO", "/mes", "/help")
                .contains("Como registrar un gasto")
                .contains("comida", "23,50");
    }

    @Test
    void unknownCommandSendsGuidanceMessage() {
        service.processWebhookUpdate(webhook("/unknown", true, true));

        verify(telegramClient).sendMessage(
                CHAT_ID,
                "No he entendido ese mensaje. Usa /help para ver los comandos disponibles."
        );
    }

    @Test
    void linkCommandUsesNormalizedCodeAndSendsConfirmation() {
        service.processWebhookUpdate(webhook("/link   abC12345  ", true, true));

        verify(linkCodeService).linkTelegramUser("abC12345", TELEGRAM_USER_ID);
        verify(telegramClient).sendMessage(
                CHAT_ID,
                "Tu cuenta ha quedado vinculada correctamente. Ya puedes usar KakeBot desde Telegram."
        );
    }

    @Test
    void linkCommandSendsBusinessErrorToChat() {
        doThrowInvalidUser("El codigo de vinculacion no existe")
                .when(linkCodeService).linkTelegramUser("BADCODE", TELEGRAM_USER_ID);

        service.processWebhookUpdate(webhook("/link BADCODE", true, true));

        verify(telegramClient).sendMessage(CHAT_ID, "El codigo de vinculacion no existe");
    }

    @Test
    void monthCommandSendsPhotoWithCategoryAndGeneralTotals() {
        givenLinkedUserWithId();
        List<CategoryTotalResponseDto> totals = List.of(
                new CategoryTotalResponseDto(ExpenseCategory.HOME, new BigDecimal("1200.50")),
                new CategoryTotalResponseDto(ExpenseCategory.FOOD, new BigDecimal("50.00"))
        );
        when(expenseService.getMonthTotalByCategory(USER_ID)).thenReturn(totals);
        when(expenseService.getMonthTotal(USER_ID)).thenReturn(new TotalResponseDto(new BigDecimal("1250.50")));

        service.processWebhookUpdate(webhook("/mes", true, true));

        ArgumentCaptor<String> captionCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramClient).sendPhoto(
                org.mockito.ArgumentMatchers.eq(CHAT_ID),
                org.mockito.ArgumentMatchers.eq("static/images/botPillopng.png"),
                captionCaptor.capture()
        );
        assertThat(captionCaptor.getValue())
                .contains("Mes y categorias")
                .contains("Casa: 1.200,50 €")
                .contains("Comida: 50,00 €")
                .contains("Total del mes: 1.250,50 €");
    }

    @Test
    void monthCommandTellsUnlinkedUserHowToLinkAccount() {
        when(userRepository.findByExternalId(String.valueOf(TELEGRAM_USER_ID))).thenReturn(Optional.empty());

        service.processWebhookUpdate(webhook("/mes", true, true));

        verify(telegramClient).sendMessage(
                CHAT_ID,
                "Tu cuenta de Telegram no esta vinculada. Usa /link CODIGO desde el bot."
        );
        verifyNoInteractions(expenseService);
    }

    @Test
    void expenseTextCreatesExpenseFromSpanishCategoryAndCommaAmount() {
        givenLinkedUserWithId();

        service.processWebhookUpdate(webhook("  comida\n Compra semanal \n23,50 ", true, true));

        ArgumentCaptor<ExpenseRequestDto> requestCaptor = ArgumentCaptor.forClass(ExpenseRequestDto.class);
        verify(expenseService).createExpense(org.mockito.ArgumentMatchers.eq(USER_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue()).isEqualTo(
                new ExpenseRequestDto(ExpenseCategory.FOOD, "Compra semanal", new BigDecimal("23.50"))
        );
        verify(telegramClient).sendMessage(CHAT_ID, "Gasto guardado correctamente.");
    }

    @Test
    void expenseTextAcceptsEnglishCategoryAndDotAmount() {
        givenLinkedUserWithId();

        service.processWebhookUpdate(webhook("transport\nBus\n2.75", true, true));

        ArgumentCaptor<ExpenseRequestDto> requestCaptor = ArgumentCaptor.forClass(ExpenseRequestDto.class);
        verify(expenseService).createExpense(org.mockito.ArgumentMatchers.eq(USER_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().category()).isEqualTo(ExpenseCategory.TRANSPORT);
        assertThat(requestCaptor.getValue().amount()).isEqualByComparingTo("2.75");
    }

    @Test
    void expenseTextRejectsIncorrectNumberOfLines() {
        givenLinkedUser();

        service.processWebhookUpdate(webhook("comida\n23,50", true, true));

        verify(telegramClient).sendMessage(
                CHAT_ID,
                "Formato de gasto invalido. Envia el gasto en 3 lineas: CATEGORIA, su descripción e importe."
        );
        verify(expenseService, never()).createExpense(any(), any());
    }

    @Test
    void expenseTextRejectsUnknownCategory() {
        givenLinkedUser();

        service.processWebhookUpdate(webhook("viajes\nAvion\n100", true, true));

        verify(telegramClient).sendMessage(
                CHAT_ID,
                "Categoria de gasto invalida. Usa una de estas: HOME, FOOD, TRANSPORT, LEISURE, OTHER."
        );
        verify(expenseService, never()).createExpense(any(), any());
    }

    @Test
    void expenseTextRejectsNonNumericAmount() {
        givenLinkedUser();

        service.processWebhookUpdate(webhook("food\nCompra\nabc", true, true));

        verify(telegramClient).sendMessage(CHAT_ID, "El importe no es valido.");
        verify(expenseService, never()).createExpense(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0,01"})
    void expenseTextRejectsNonPositiveAmount(String amount) {
        givenLinkedUser();

        service.processWebhookUpdate(webhook("food\nCompra\n" + amount, true, true));

        verify(telegramClient).sendMessage(CHAT_ID, "El importe debe ser mayor que cero.");
        verify(expenseService, never()).createExpense(any(), any());
    }

    @Test
    void expenseTextTellsUnlinkedUserHowToLinkBeforeParsingExpense() {
        when(userRepository.findByExternalId(String.valueOf(TELEGRAM_USER_ID))).thenReturn(Optional.empty());

        service.processWebhookUpdate(webhook("food\nCompra\n20", true, true));

        verify(telegramClient).sendMessage(
                CHAT_ID,
                "Tu cuenta de Telegram no esta vinculada. Usa /link CODIGO desde el bot."
        );
        verifyNoInteractions(expenseService);
    }

    private void givenLinkedUser() {
        when(userRepository.findByExternalId(String.valueOf(TELEGRAM_USER_ID)))
                .thenReturn(Optional.of(linkedUser));
    }

    private void givenLinkedUserWithId() {
        givenLinkedUser();
        when(linkedUser.getId()).thenReturn(USER_ID);
    }

    private TelegramWebhookRequestDto webhook(String text, boolean includeSender, boolean includeChat) {
        TelegramFromDto from = includeSender ? new TelegramFromDto(TELEGRAM_USER_ID, "Jordi", "jordi") : null;
        TelegramChatDto chat = includeChat ? new TelegramChatDto(CHAT_ID, "private") : null;
        TelegramMessageDto message = new TelegramMessageDto(10L, from, chat, text, 1L);
        return new TelegramWebhookRequestDto(1L, message);
    }

    private org.mockito.stubbing.Stubber doThrowInvalidUser(String message) {
        return org.mockito.Mockito.doThrow(new InvalidUserRequestException(message));
    }
}
