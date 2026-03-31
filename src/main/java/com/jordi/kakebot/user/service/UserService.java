package com.jordi.kakebot.user.service;

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
import java.time.LocalDateTime;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UserService(UserRepository userRepository, UserMapper userMapper, Clock clock) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.clock = clock;
    }

    public UserResponseDto register(UserRegisterRequestDto request) {
        String normalizedUsername = request.username().trim();

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }

        User user = new User(
                UserProvider.LOCAL,
                normalizedUsername,
                passwordEncoder.encode(request.password()),
                null,
                LocalDateTime.now(clock)
        );

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }

    public UserResponseDto login(UserLoginRequestDto request) {
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return userMapper.toResponseDto(user);
    }

    public UserResponseDto getMe(Long userId) {
        User user = resolveUserOrThrow(userId);
        return userMapper.toResponseDto(user);
    }

    public UserResponseDto updateMe(Long userId, UserUpdateRequestDto request) {
        User user = resolveUserOrThrow(userId);
        updateUsernameIfPresent(user, request.username());
        updatePasswordIfPresent(user, request.currentPassword(), request.newPassword());

        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDto(updatedUser);
    }

    private User resolveUserOrThrow(Long userId) {
        if (userId == null) {
            throw new InvalidUserRequestException("El id de usuario es obligatorio");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void updateUsernameIfPresent(User user, String username) {
        if (username == null) {
            return;
        }

        String normalizedUsername = username.trim();

        if (normalizedUsername.isEmpty()) {
            throw new InvalidUserRequestException("El nombre de usuario no puede estar vacio");
        }

        boolean usernameBelongsToAnotherUser = userRepository.existsByUsername(normalizedUsername)
                && !normalizedUsername.equals(user.getUsername());

        if (usernameBelongsToAnotherUser) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }

        user.setUsername(normalizedUsername);
    }

    private void updatePasswordIfPresent(User user, String currentPassword, String newPassword) {
        if (newPassword == null) {
            return;
        }

        if (newPassword.isBlank()) {
            throw new InvalidUserRequestException("La nueva contrasena no puede estar vacia");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }
}
