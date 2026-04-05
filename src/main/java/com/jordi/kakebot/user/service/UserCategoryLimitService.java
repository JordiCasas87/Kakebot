package com.jordi.kakebot.user.service;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.user.dto.UserCategoryLimitRequestDto;
import com.jordi.kakebot.user.dto.UserCategoryLimitResponseDto;
import com.jordi.kakebot.user.exception.InvalidUserRequestException;
import com.jordi.kakebot.user.exception.UserNotFoundException;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.model.UserCategoryLimit;
import com.jordi.kakebot.user.repository.UserCategoryLimitRepository;
import com.jordi.kakebot.user.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserCategoryLimitService {

    private final UserCategoryLimitRepository userCategoryLimitRepository;
    private final UserRepository userRepository;

    public UserCategoryLimitService(
            UserCategoryLimitRepository userCategoryLimitRepository,
            UserRepository userRepository
    ) {
        this.userCategoryLimitRepository = userCategoryLimitRepository;
        this.userRepository = userRepository;
    }

    public List<UserCategoryLimitResponseDto> getMyCategoryLimits(Long userId) {
        validateUserId(userId);
        ensureUserExists(userId);

        return userCategoryLimitRepository.findAllByUserIdOrderByCategoryAsc(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public UserCategoryLimitResponseDto upsertMyCategoryLimit(
            Long userId,
            ExpenseCategory category,
            UserCategoryLimitRequestDto request
    ) {
        validateUserId(userId);

        if (category == null) {
            throw new InvalidUserRequestException("La categoria es obligatoria");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserCategoryLimit categoryLimit = userCategoryLimitRepository.findByUserIdAndCategory(userId, category)
                .orElseGet(() -> new UserCategoryLimit(user, category, request.monthlyLimit()));

        categoryLimit.setMonthlyLimit(request.monthlyLimit());

        UserCategoryLimit savedCategoryLimit = userCategoryLimitRepository.save(categoryLimit);
        return toResponseDto(savedCategoryLimit);
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new InvalidUserRequestException("El id de usuario es obligatorio");
        }
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private UserCategoryLimitResponseDto toResponseDto(UserCategoryLimit categoryLimit) {
        return new UserCategoryLimitResponseDto(
                categoryLimit.getCategory(),
                categoryLimit.getMonthlyLimit()
        );
    }
}
