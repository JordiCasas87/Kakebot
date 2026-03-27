package com.jordi.kakebot.user.repository;

import com.jordi.kakebot.user.model.UserTelegramLinkCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTelegramLinkCodeRepository extends JpaRepository<UserTelegramLinkCode, Long> {

    Optional<UserTelegramLinkCode> findByCode(String code);

    Optional<UserTelegramLinkCode> findByUserId(Long userId);

    boolean existsByCode(String code);
}
