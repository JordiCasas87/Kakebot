package com.jordi.kakebot.user.repository;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.user.model.UserCategoryLimit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCategoryLimitRepository extends JpaRepository<UserCategoryLimit, Long> {

    List<UserCategoryLimit> findAllByUserIdOrderByCategoryAsc(Long userId);

    Optional<UserCategoryLimit> findByUserIdAndCategory(Long userId, ExpenseCategory category);
}
