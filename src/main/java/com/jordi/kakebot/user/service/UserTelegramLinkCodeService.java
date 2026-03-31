package com.jordi.kakebot.user.service;

import com.jordi.kakebot.user.dto.UserTelegramLinkCodeResponseDto;
import com.jordi.kakebot.user.exception.InvalidUserRequestException;
import com.jordi.kakebot.user.exception.UserNotFoundException;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.model.UserTelegramLinkCode;
import com.jordi.kakebot.user.repository.UserRepository;
import com.jordi.kakebot.user.repository.UserTelegramLinkCodeRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserTelegramLinkCodeService {

    private static final int LINK_CODE_LENGTH = 8;
    private static final long LINK_CODE_EXPIRATION_MINUTES = 10;

    private final UserRepository userRepository;
    private final UserTelegramLinkCodeRepository userTelegramLinkCodeRepository;
    private final Clock clock;

    public UserTelegramLinkCodeService(
            UserRepository userRepository,
            UserTelegramLinkCodeRepository userTelegramLinkCodeRepository,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.userTelegramLinkCodeRepository = userTelegramLinkCodeRepository;
        this.clock = clock;
    }

    public UserTelegramLinkCodeResponseDto generateLinkCode(Long userId) {
        User user = resolveUserOrThrow(userId);
        String code = generateUniqueCode();
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt = now.plusMinutes(LINK_CODE_EXPIRATION_MINUTES);

        UserTelegramLinkCode linkCode = userTelegramLinkCodeRepository.findByUserId(user.getId())
                .map(existingCode -> updateExistingCode(existingCode, code, now, expiresAt))
                .orElseGet(() -> new UserTelegramLinkCode(user, code, expiresAt, now));

        UserTelegramLinkCode savedLinkCode = userTelegramLinkCodeRepository.save(linkCode);
        return new UserTelegramLinkCodeResponseDto(savedLinkCode.getCode(), savedLinkCode.getExpiresAt());
    }

    @Transactional
    public void linkTelegramUser(String code, Long telegramUserId) {
        if (code == null || code.isBlank()) {
            throw new InvalidUserRequestException("El codigo de vinculacion es obligatorio");
        }

        if (telegramUserId == null) {
            throw new InvalidUserRequestException("El id de Telegram es obligatorio");
        }

        if (userRepository.existsByExternalId(String.valueOf(telegramUserId))) {
            throw new InvalidUserRequestException("Este usuario de Telegram ya esta vinculado");
        }

        UserTelegramLinkCode linkCode = userTelegramLinkCodeRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new InvalidUserRequestException("El codigo de vinculacion no existe"));

        if (linkCode.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
            throw new InvalidUserRequestException("El codigo de vinculacion ha caducado");
        }

        User user = linkCode.getUser();
        user.setExternalId(String.valueOf(telegramUserId));
        userRepository.save(user);
        userTelegramLinkCodeRepository.delete(linkCode);
    }

    private User resolveUserOrThrow(Long userId) {
        if (userId == null) {
            throw new InvalidUserRequestException("El id de usuario es obligatorio");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserTelegramLinkCode updateExistingCode(
            UserTelegramLinkCode existingCode,
            String code,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        existingCode.setCode(code);
        existingCode.setCreatedAt(createdAt);
        existingCode.setExpiresAt(expiresAt);
        return existingCode;
    }

    private String generateUniqueCode() {
        String code;

        do {
            code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, LINK_CODE_LENGTH)
                    .toUpperCase();
        } while (userTelegramLinkCodeRepository.existsByCode(code));

        return code;
    }
}
