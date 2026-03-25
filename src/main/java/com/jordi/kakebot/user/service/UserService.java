package com.jordi.kakebot.user.service;

import com.jordi.kakebot.user.dto.UserLoginRequestDto;
import com.jordi.kakebot.user.dto.UserRegisterRequestDto;
import com.jordi.kakebot.user.dto.UserResponseDto;
import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.exception.InvalidCredentialsException;
import com.jordi.kakebot.user.exception.InvalidUserRequestException;
import com.jordi.kakebot.user.exception.UserNotFoundException;
import com.jordi.kakebot.user.exception.UsernameAlreadyExistsException;
import com.jordi.kakebot.user.mapper.UserMapper;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
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
                LocalDateTime.now()
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
        if (userId == null) {
            throw new InvalidUserRequestException("El id de usuario es obligatorio");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return userMapper.toResponseDto(user);
    }
}
