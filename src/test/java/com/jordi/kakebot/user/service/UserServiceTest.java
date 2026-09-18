package com.jordi.kakebot.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jordi.kakebot.user.dto.UserLoginRequestDto;
import com.jordi.kakebot.user.dto.UserRegisterRequestDto;
import com.jordi.kakebot.user.dto.UserResponseDto;
import com.jordi.kakebot.user.dto.UserUpdateRequestDto;
import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.exception.InvalidCredentialsException;
import com.jordi.kakebot.user.exception.InvalidUserRequestException;
import com.jordi.kakebot.user.exception.UserNotFoundException;
import com.jordi.kakebot.user.exception.UsernameAlreadyExistsException;
import com.jordi.kakebot.user.mapper.UserMapper;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.repository.UserRepository;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long USER_ID = 7L;
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-18T10:15:30Z"),
            ZoneId.of("Europe/Madrid")
    );

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper, FIXED_CLOCK);
    }

    @Test
    void registerNormalizesUsernameHashesPasswordAndReturnsResponse() {
        UserRegisterRequestDto request = new UserRegisterRequestDto("  jordi  ", "secret1");
        UserResponseDto expected = response("jordi", null);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.existsByUsername("jordi")).thenReturn(false);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponseDto(any(User.class))).thenReturn(expected);

        UserResponseDto result = userService.register(request);

        User savedUser = userCaptor.getValue();
        assertThat(result).isEqualTo(expected);
        assertThat(savedUser.getUsername()).isEqualTo("jordi");
        assertThat(savedUser.getProvider()).isEqualTo(UserProvider.LOCAL);
        assertThat(savedUser.getExternalId()).isNull();
        assertThat(savedUser.getCreatedAt()).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("secret1");
        assertThat(new BCryptPasswordEncoder().matches("secret1", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    void registerRejectsExistingNormalizedUsername() {
        UserRegisterRequestDto request = new UserRegisterRequestDto("  jordi  ", "secret1");
        when(userRepository.existsByUsername("jordi")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("El nombre de usuario ya existe: jordi");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }

    @Test
    void loginNormalizesUsernameAndReturnsUserForValidPassword() {
        UserLoginRequestDto request = new UserLoginRequestDto("  jordi  ", "secret1");
        User user = user("jordi", "secret1", null);
        UserResponseDto expected = response("jordi", null);
        when(userRepository.findByUsername("jordi")).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(expected);

        assertThat(userService.login(request)).isEqualTo(expected);
    }

    @Test
    void loginRejectsUnknownUsernameWithoutUsingMapper() {
        UserLoginRequestDto request = new UserLoginRequestDto("missing", "secret1");
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales invalidas");

        verifyNoInteractions(userMapper);
    }

    @Test
    void loginRejectsInvalidPasswordWithoutUsingMapper() {
        UserLoginRequestDto request = new UserLoginRequestDto("jordi", "wrong-password");
        User user = user("jordi", "secret1", null);
        when(userRepository.findByUsername("jordi")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales invalidas");

        verifyNoInteractions(userMapper);
    }

    @Test
    void getMeReturnsMappedUser() {
        User user = user("jordi", "secret1", null);
        UserResponseDto expected = response("jordi", null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(expected);

        assertThat(userService.getMe(USER_ID)).isEqualTo(expected);
    }

    @Test
    void getMeRejectsNullUserId() {
        assertThatThrownBy(() -> userService.getMe(null))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("El id de usuario es obligatorio");

        verifyNoInteractions(userRepository, userMapper);
    }

    @Test
    void getMeThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("No existe usuario con id: 7");

        verifyNoInteractions(userMapper);
    }

    @Test
    void updateMeNormalizesAndChangesUsername() {
        User user = user("old-name", "secret1", null);
        UserUpdateRequestDto request = new UserUpdateRequestDto("  new-name  ", null, null);
        UserResponseDto expected = response("new-name", null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("new-name")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(expected);

        UserResponseDto result = userService.updateMe(USER_ID, request);

        assertThat(result).isEqualTo(expected);
        assertThat(user.getUsername()).isEqualTo("new-name");
    }

    @Test
    void updateMeAllowsKeepingCurrentUsername() {
        User user = user("jordi", "secret1", null);
        UserUpdateRequestDto request = new UserUpdateRequestDto("jordi", null, null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("jordi")).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        userService.updateMe(USER_ID, request);

        assertThat(user.getUsername()).isEqualTo("jordi");
        verify(userRepository).save(user);
    }

    @Test
    void updateMeRejectsUsernameBelongingToAnotherUser() {
        User user = user("jordi", "secret1", null);
        UserUpdateRequestDto request = new UserUpdateRequestDto("taken", null, null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateMe(USER_ID, request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("El nombre de usuario ya existe: taken");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void updateMeRejectsBlankUsername(String username) {
        User user = user("jordi", "secret1", null);
        UserUpdateRequestDto request = new UserUpdateRequestDto(username, null, null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateMe(USER_ID, request))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("El nombre de usuario no puede estar vacio");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateMeChangesPasswordWhenCurrentPasswordIsValid() {
        User user = user("jordi", "secret1", null);
        String previousHash = user.getPasswordHash();
        UserUpdateRequestDto request = new UserUpdateRequestDto(null, "secret1", "secret2");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateMe(USER_ID, request);

        assertThat(user.getPasswordHash()).isNotEqualTo(previousHash);
        assertThat(new BCryptPasswordEncoder().matches("secret2", user.getPasswordHash())).isTrue();
        assertThat(new BCryptPasswordEncoder().matches("secret1", user.getPasswordHash())).isFalse();
        verify(userRepository).save(user);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void updateMeRejectsBlankNewPassword(String newPassword) {
        User user = user("jordi", "secret1", null);
        UserUpdateRequestDto request = new UserUpdateRequestDto(null, "secret1", newPassword);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateMe(USER_ID, request))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("La nueva contrasena no puede estar vacia");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateMeRejectsIncorrectCurrentPassword() {
        User user = user("jordi", "secret1", null);
        UserUpdateRequestDto request = new UserUpdateRequestDto(null, "incorrect", "secret2");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateMe(USER_ID, request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales invalidas");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }

    @Test
    void unlinkTelegramClearsExternalIdAndSavesUser() {
        User user = user("jordi", "secret1", "telegram-123");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        userService.unlinkTelegram(USER_ID);

        assertThat(user.getExternalId()).isNull();
        verify(userRepository).save(user);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void unlinkTelegramRejectsUserWithoutLinkedAccount(String externalId) {
        User user = user("jordi", "secret1", externalId);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.unlinkTelegram(USER_ID))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("No hay cuenta de Telegram vinculada");

        verify(userRepository, never()).save(any());
    }

    private User user(String username, String rawPassword, String externalId) {
        return new User(
                UserProvider.LOCAL,
                username,
                new BCryptPasswordEncoder().encode(rawPassword),
                externalId,
                LocalDateTime.now(FIXED_CLOCK)
        );
    }

    private UserResponseDto response(String username, String externalId) {
        return new UserResponseDto(
                USER_ID,
                username,
                UserProvider.LOCAL,
                externalId,
                LocalDateTime.now(FIXED_CLOCK)
        );
    }
}
