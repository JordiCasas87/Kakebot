package com.jordi.kakebot.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jordi.kakebot.TestcontainersConfiguration;
import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.model.UserTelegramLinkCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class UserTelegramLinkCodeRepositoryIntegrationTest {

    @Autowired
    private UserTelegramLinkCodeRepository linkCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsLinkCodeByCode() {
        User user = saveUser("jordi");
        UserTelegramLinkCode linkCode = linkCode(user, "ABC12345");
        linkCodeRepository.saveAndFlush(linkCode);

        assertThat(linkCodeRepository.findByCode("ABC12345"))
                .isPresent()
                .get()
                .extracting(UserTelegramLinkCode::getUser)
                .extracting(User::getId)
                .isEqualTo(user.getId());
    }

    @Test
    void findsLinkCodeByUserId() {
        User user = saveUser("jordi");
        linkCodeRepository.saveAndFlush(linkCode(user, "ABC12345"));

        assertThat(linkCodeRepository.findByUserId(user.getId()))
                .isPresent()
                .get()
                .extracting(UserTelegramLinkCode::getCode)
                .isEqualTo("ABC12345");
    }

    @Test
    void checksWhetherCodeExists() {
        User user = saveUser("jordi");
        linkCodeRepository.saveAndFlush(linkCode(user, "ABC12345"));

        assertThat(linkCodeRepository.existsByCode("ABC12345")).isTrue();
        assertThat(linkCodeRepository.existsByCode("MISSING1")).isFalse();
    }

    @Test
    void rejectsDuplicatedCode() {
        linkCodeRepository.saveAndFlush(linkCode(saveUser("jordi"), "ABC12345"));

        assertThatThrownBy(() -> linkCodeRepository.saveAndFlush(linkCode(saveUser("maria"), "ABC12345")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsMoreThanOneLinkCodeForSameUser() {
        User user = saveUser("jordi");
        linkCodeRepository.saveAndFlush(linkCode(user, "ABC12345"));

        assertThatThrownBy(() -> linkCodeRepository.saveAndFlush(linkCode(user, "XYZ98765")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User saveUser(String username) {
        return userRepository.saveAndFlush(
                new User(UserProvider.LOCAL, username, "password-hash", null, LocalDateTime.now())
        );
    }

    private UserTelegramLinkCode linkCode(User user, String code) {
        LocalDateTime now = LocalDateTime.now();
        return new UserTelegramLinkCode(user, code, now.plusMinutes(10), now);
    }
}
