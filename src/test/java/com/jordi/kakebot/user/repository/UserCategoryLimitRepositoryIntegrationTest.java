package com.jordi.kakebot.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jordi.kakebot.TestcontainersConfiguration;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.model.UserCategoryLimit;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class UserCategoryLimitRepositoryIntegrationTest {

    @Autowired
    private UserCategoryLimitRepository categoryLimitRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsLimitByUserAndCategory() {
        User user = saveUser("jordi");
        categoryLimitRepository.saveAndFlush(limit(user, ExpenseCategory.FOOD, "200.00"));

        assertThat(categoryLimitRepository.findByUserIdAndCategory(user.getId(), ExpenseCategory.FOOD))
                .isPresent()
                .get()
                .extracting(UserCategoryLimit::getMonthlyLimit)
                .isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    void returnsEmptyWhenUserHasNoLimitForCategory() {
        User user = saveUser("jordi");
        categoryLimitRepository.saveAndFlush(limit(user, ExpenseCategory.FOOD, "200.00"));

        assertThat(categoryLimitRepository.findByUserIdAndCategory(user.getId(), ExpenseCategory.LEISURE))
                .isEmpty();
    }

    @Test
    void listsOnlyRequestedUsersLimitsOrderedByCategory() {
        User jordi = saveUser("jordi");
        User maria = saveUser("maria");
        categoryLimitRepository.save(limit(jordi, ExpenseCategory.TRANSPORT, "100.00"));
        categoryLimitRepository.save(limit(jordi, ExpenseCategory.FOOD, "200.00"));
        categoryLimitRepository.save(limit(jordi, ExpenseCategory.HOME, "900.00"));
        categoryLimitRepository.save(limit(maria, ExpenseCategory.LEISURE, "50.00"));
        categoryLimitRepository.flush();

        List<UserCategoryLimit> result = categoryLimitRepository.findAllByUserIdOrderByCategoryAsc(jordi.getId());

        assertThat(result).extracting(UserCategoryLimit::getCategory)
                .containsExactly(ExpenseCategory.FOOD, ExpenseCategory.HOME, ExpenseCategory.TRANSPORT);
    }

    @Test
    void allowsSameCategoryForDifferentUsers() {
        User jordi = saveUser("jordi");
        User maria = saveUser("maria");

        categoryLimitRepository.saveAndFlush(limit(jordi, ExpenseCategory.FOOD, "200.00"));
        categoryLimitRepository.saveAndFlush(limit(maria, ExpenseCategory.FOOD, "300.00"));

        assertThat(categoryLimitRepository.findAll()).hasSize(2);
    }

    @Test
    void rejectsDuplicatedCategoryForSameUser() {
        User user = saveUser("jordi");
        categoryLimitRepository.saveAndFlush(limit(user, ExpenseCategory.FOOD, "200.00"));

        assertThatThrownBy(() -> categoryLimitRepository.saveAndFlush(
                limit(user, ExpenseCategory.FOOD, "300.00")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private User saveUser(String username) {
        return userRepository.saveAndFlush(
                new User(UserProvider.LOCAL, username, "password-hash", null, LocalDateTime.now())
        );
    }

    private UserCategoryLimit limit(User user, ExpenseCategory category, String amount) {
        return new UserCategoryLimit(user, category, new BigDecimal(amount));
    }
}
