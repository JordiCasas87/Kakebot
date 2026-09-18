package com.jordi.kakebot.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.user.dto.UserCategoryLimitRequestDto;
import com.jordi.kakebot.user.dto.UserCategoryLimitResponseDto;
import com.jordi.kakebot.user.exception.InvalidUserRequestException;
import com.jordi.kakebot.user.exception.UserNotFoundException;
import com.jordi.kakebot.user.model.User;
import com.jordi.kakebot.user.model.UserCategoryLimit;
import com.jordi.kakebot.user.repository.UserCategoryLimitRepository;
import com.jordi.kakebot.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCategoryLimitServiceTest {

    private static final Long USER_ID = 7L;

    @Mock
    private UserCategoryLimitRepository categoryLimitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    private UserCategoryLimitService service;

    @BeforeEach
    void setUp() {
        service = new UserCategoryLimitService(categoryLimitRepository, userRepository);
    }

    @Test
    void getMyCategoryLimitsReturnsMappedLimitsInRepositoryOrder() {
        UserCategoryLimit food = new UserCategoryLimit(user, ExpenseCategory.FOOD, new BigDecimal("200.00"));
        UserCategoryLimit leisure = new UserCategoryLimit(user, ExpenseCategory.LEISURE, new BigDecimal("75.50"));
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(categoryLimitRepository.findAllByUserIdOrderByCategoryAsc(USER_ID))
                .thenReturn(List.of(food, leisure));

        List<UserCategoryLimitResponseDto> result = service.getMyCategoryLimits(USER_ID);

        assertThat(result).containsExactly(
                new UserCategoryLimitResponseDto(ExpenseCategory.FOOD, new BigDecimal("200.00")),
                new UserCategoryLimitResponseDto(ExpenseCategory.LEISURE, new BigDecimal("75.50"))
        );
    }

    @Test
    void getMyCategoryLimitsRejectsNullUserId() {
        assertThatThrownBy(() -> service.getMyCategoryLimits(null))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("El id de usuario es obligatorio");

        verifyNoInteractions(userRepository, categoryLimitRepository);
    }

    @Test
    void getMyCategoryLimitsThrowsWhenUserDoesNotExist() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.getMyCategoryLimits(USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("No existe usuario con id: 7");

        verifyNoInteractions(categoryLimitRepository);
    }

    @Test
    void upsertMyCategoryLimitCreatesNewLimit() {
        UserCategoryLimitRequestDto request = new UserCategoryLimitRequestDto(new BigDecimal("150.00"));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(categoryLimitRepository.findByUserIdAndCategory(USER_ID, ExpenseCategory.FOOD))
                .thenReturn(Optional.empty());
        when(categoryLimitRepository.save(any(UserCategoryLimit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserCategoryLimitResponseDto result = service.upsertMyCategoryLimit(USER_ID, ExpenseCategory.FOOD, request);

        assertThat(result).isEqualTo(new UserCategoryLimitResponseDto(ExpenseCategory.FOOD, new BigDecimal("150.00")));
        ArgumentCaptor<UserCategoryLimit> captor = ArgumentCaptor.forClass(UserCategoryLimit.class);
        verify(categoryLimitRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getCategory()).isEqualTo(ExpenseCategory.FOOD);
    }

    @Test
    void upsertMyCategoryLimitUpdatesExistingLimit() {
        UserCategoryLimit existing = new UserCategoryLimit(user, ExpenseCategory.HOME, new BigDecimal("300.00"));
        UserCategoryLimitRequestDto request = new UserCategoryLimitRequestDto(new BigDecimal("450.00"));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(categoryLimitRepository.findByUserIdAndCategory(USER_ID, ExpenseCategory.HOME))
                .thenReturn(Optional.of(existing));
        when(categoryLimitRepository.save(existing)).thenReturn(existing);

        UserCategoryLimitResponseDto result = service.upsertMyCategoryLimit(USER_ID, ExpenseCategory.HOME, request);

        assertThat(existing.getMonthlyLimit()).isEqualByComparingTo("450.00");
        assertThat(result.monthlyLimit()).isEqualByComparingTo("450.00");
        verify(categoryLimitRepository).save(existing);
    }

    @Test
    void upsertMyCategoryLimitRejectsNullCategory() {
        UserCategoryLimitRequestDto request = new UserCategoryLimitRequestDto(BigDecimal.TEN);

        assertThatThrownBy(() -> service.upsertMyCategoryLimit(USER_ID, null, request))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessage("La categoria es obligatoria");

        verifyNoInteractions(userRepository, categoryLimitRepository);
    }

    @Test
    void upsertMyCategoryLimitThrowsWhenUserDoesNotExist() {
        UserCategoryLimitRequestDto request = new UserCategoryLimitRequestDto(BigDecimal.TEN);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertMyCategoryLimit(USER_ID, ExpenseCategory.OTHER, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("No existe usuario con id: 7");

        verify(categoryLimitRepository, never()).save(any());
    }
}
