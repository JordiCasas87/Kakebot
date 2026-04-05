package com.jordi.kakebot.user.controller;

import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.user.dto.UserCategoryLimitRequestDto;
import com.jordi.kakebot.user.dto.UserCategoryLimitResponseDto;
import com.jordi.kakebot.user.service.UserCategoryLimitService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/category-limits")
public class UserCategoryLimitController {

    private final UserCategoryLimitService userCategoryLimitService;

    public UserCategoryLimitController(UserCategoryLimitService userCategoryLimitService) {
        this.userCategoryLimitService = userCategoryLimitService;
    }

    @GetMapping
    public ResponseEntity<List<UserCategoryLimitResponseDto>> getMyCategoryLimits(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<UserCategoryLimitResponseDto> response = userCategoryLimitService.getMyCategoryLimits(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{category}")
    public ResponseEntity<UserCategoryLimitResponseDto> upsertMyCategoryLimit(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String category,
            @Valid @RequestBody UserCategoryLimitRequestDto request
    ) {
        UserCategoryLimitResponseDto response = userCategoryLimitService.upsertMyCategoryLimit(
                userId,
                ExpenseCategory.fromValue(category),
                request
        );
        return ResponseEntity.ok(response);
    }
}
