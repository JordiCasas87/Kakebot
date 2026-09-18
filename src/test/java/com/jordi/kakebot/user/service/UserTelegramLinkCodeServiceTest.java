package com.jordi.kakebot.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jordi.kakebot.user.dto.UserTelegramLinkCodeResponseDto;
import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.exception.InvalidUserRequestException;
import com.jordi.kakebot.user.exception.UserNotFoundException;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.model.UserTelegramLinkCode;
import com.jordi.kakebot.user.repository.UserRepository;
import com.jordi.kakebot.user.repository.UserTelegramLinkCodeRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
class UserTelegramLinkCodeServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long TELEGRAM_USER_ID = 123456L;
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-18T10:15:30Z"),
            ZoneId.of("Europe/Madrid")
    );
    private static final LocalDateTime NOW = LocalDateTime.now(FIXED_CLOCK);

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTelegramLinkCodeRepository linkCodeRepository;

    @Mock
    private User user;

    private UserTelegramLinkCodeService service;

    @BeforeEach
    void setUp() {
        service = new UserTelegramLinkCodeService(userRepository, linkCodeRepository, FIXED_CLOCK);
    }

    @Test
    void generateLinkCodeCreatesEightCharacterUppercaseCodeExpiringInTenMinutes() {
        givenExistingUser();
        when(linkCodeRepository.existsByCode(any(String.class))).thenReturn(false);
        when(linkCodeRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(linkCodeRepository.save(any(UserTelegramLinkCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserTelegramLinkCodeResponseDto result = service.generateLinkCode(USER_ID);

        assertThat(result.code()).matches("[A-F0-9]{8}");
        assertThat(result.expiresAt()).isEqualTo(NOW.plusMinutes(10));
        ArgumentCaptor<UserTelegramLinkCode> captor = ArgumentCaptor.forClass(UserTelegramLinkCode.class);
        verify(linkCodeRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void generateLinkCodeReplacesExistingCodeAndRefreshesDates() {
        givenExistingUser();
        UserTelegramLinkCode existing = new UserTelegramLinkCode(
                user,
                "OLD12345",
                NOW.minusMinutes(1),
                NOW.minusHours(1)
        );
        when(linkCodeRepository.existsByCode(any(String.class))).thenReturn(false);
        when(linkCodeRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
        when(linkCodeRepository.save(existing)).thenReturn(existing);

        UserTelegramLinkCodeResponseDto result = service.generateLinkCode(USER_ID);

        assertThat(result.code()).matches("[A-F0-9]{8}").isNotEqualTo("OLD12345");
        assertThat(existing.getCreatedAt()).isEqualTo(NOW);
        assertThat(existing.getExpiresAt()).isEqualTo(NOW.plusMinutes(10));
        verify(linkCodeRepository).save(existing);
    }

    @Test
    void generateLinkCodeRejectsNullUserId() {
        assertThatThrownBy(() -> service.generateLinkCode(null))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("El id de usuario es obligatorio");

        verifyNoInteractions(userRepository, linkCodeRepository);
    }

    @Test
    void generateLinkCodeThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateLinkCode(USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("No existe usuario con id: 7");

        verifyNoInteractions(linkCodeRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void linkTelegramUserRejectsMissingCode(String code) {
        assertThatThrownBy(() -> service.linkTelegramUser(code, TELEGRAM_USER_ID))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("El codigo de vinculacion es obligatorio");

        verifyNoInteractions(userRepository, linkCodeRepository);
    }

    @Test
    void linkTelegramUserRejectsNullTelegramUserId() {
        assertThatThrownBy(() -> service.linkTelegramUser("ABC12345", null))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("El id de Telegram es obligatorio");

        verifyNoInteractions(userRepository, linkCodeRepository);
    }

    @Test
    void linkTelegramUserRejectsAlreadyLinkedTelegramAccount() {
        when(userRepository.existsByExternalId(String.valueOf(TELEGRAM_USER_ID))).thenReturn(true);

        assertThatThrownBy(() -> service.linkTelegramUser("ABC12345", TELEGRAM_USER_ID))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("Este usuario de Telegram ya esta vinculado");

        verifyNoInteractions(linkCodeRepository);
    }

    @Test
    void linkTelegramUserNormalizesCodeAndRejectsUnknownCode() {
        when(userRepository.existsByExternalId(String.valueOf(TELEGRAM_USER_ID))).thenReturn(false);
        when(linkCodeRepository.findByCode("ABC12345")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.linkTelegramUser("  abc12345  ", TELEGRAM_USER_ID))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("El codigo de vinculacion no existe");

        verify(linkCodeRepository).findByCode("ABC12345");
        verify(userRepository, never()).save(any());
    }

    @Test
    void linkTelegramUserRejectsExpiredCode() {
        UserTelegramLinkCode linkCode = linkCode(NOW.minusNanos(1));
        when(userRepository.existsByExternalId(String.valueOf(TELEGRAM_USER_ID))).thenReturn(false);
        when(linkCodeRepository.findByCode("ABC12345")).thenReturn(Optional.of(linkCode));

        assertThatThrownBy(() -> service.linkTelegramUser("ABC12345", TELEGRAM_USER_ID))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("El codigo de vinculacion ha caducado");

        verify(userRepository, never()).save(any());
        verify(linkCodeRepository, never()).delete(any());
    }

    @Test
    void linkTelegramUserLinksAccountAndConsumesValidCode() {
        User realUser = new User(UserProvider.LOCAL, "jordi", "hash", null, NOW.minusDays(1));
        UserTelegramLinkCode linkCode = new UserTelegramLinkCode(
                realUser,
                "ABC12345",
                NOW.plusMinutes(1),
                NOW.minusMinutes(1)
        );
        when(userRepository.existsByExternalId(String.valueOf(TELEGRAM_USER_ID))).thenReturn(false);
        when(linkCodeRepository.findByCode("ABC12345")).thenReturn(Optional.of(linkCode));

        service.linkTelegramUser(" abc12345 ", TELEGRAM_USER_ID);

        assertThat(realUser.getExternalId()).isEqualTo(String.valueOf(TELEGRAM_USER_ID));
        verify(userRepository).save(realUser);
        verify(linkCodeRepository).delete(linkCode);
    }

    private void givenExistingUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(USER_ID);
    }

    private UserTelegramLinkCode linkCode(LocalDateTime expiresAt) {
        return new UserTelegramLinkCode(user, "ABC12345", expiresAt, NOW.minusMinutes(1));
    }
}
