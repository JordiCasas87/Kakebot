package com.jordi.kakebot.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jordi.kakebot.TestcontainersConfiguration;
import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.model.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsUserById() {
        User user = new User(UserProvider.LOCAL, "jordi", "password-hash", null, LocalDateTime.now());

        User savedUser = userRepository.saveAndFlush(user);
        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(foundUser.getUsername()).isEqualTo("jordi");
        assertThat(foundUser.getProvider()).isEqualTo(UserProvider.LOCAL);
        assertThat(foundUser.getPasswordHash()).isEqualTo("password-hash");
        assertThat(foundUser.getCreatedAt()).isNotNull();
    }

    @Test
    void findsUserByUsername() {
        userRepository.saveAndFlush(user("jordi", null));

        assertThat(userRepository.findByUsername("jordi"))
                .isPresent()
                .get()
                .extracting(User::getUsername)
                .isEqualTo("jordi");
    }

    @Test
    void checksWhetherUsernameExists() {
        userRepository.saveAndFlush(user("jordi", null));

        assertThat(userRepository.existsByUsername("jordi")).isTrue();
        assertThat(userRepository.existsByUsername("missing")).isFalse();
    }

    @Test
    void findsUserByExternalId() {
        userRepository.saveAndFlush(user("jordi", "telegram-123"));

        assertThat(userRepository.findByExternalId("telegram-123"))
                .isPresent()
                .get()
                .extracting(User::getUsername)
                .isEqualTo("jordi");
    }

    @Test
    void checksWhetherExternalIdExists() {
        userRepository.saveAndFlush(user("jordi", "telegram-123"));

        assertThat(userRepository.existsByExternalId("telegram-123")).isTrue();
        assertThat(userRepository.existsByExternalId("telegram-999")).isFalse();
    }

    @Test
    void findsUserByProviderAndExternalId() {
        User telegramUser = new User(
                UserProvider.TELEGRAM,
                "telegram-user",
                "password-hash",
                "telegram-123",
                LocalDateTime.now()
        );
        userRepository.saveAndFlush(telegramUser);

        assertThat(userRepository.findByProviderAndExternalId(UserProvider.TELEGRAM, "telegram-123"))
                .isPresent();
        assertThat(userRepository.findByProviderAndExternalId(UserProvider.LOCAL, "telegram-123"))
                .isEmpty();
    }

    @Test
    void rejectsDuplicatedUsername() {
        userRepository.saveAndFlush(user("jordi", null));

        assertThatThrownBy(() -> userRepository.saveAndFlush(user("jordi", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User user(String username, String externalId) {
        return new User(UserProvider.LOCAL, username, "password-hash", externalId, LocalDateTime.now());
    }
}
