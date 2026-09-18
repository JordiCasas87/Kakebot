package com.jordi.kakebot.user.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jordi.kakebot.common.config.TimeConfig;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.user.dto.UserCategoryLimitRequestDto;
import com.jordi.kakebot.user.dto.UserCategoryLimitResponseDto;
import com.jordi.kakebot.user.service.UserCategoryLimitService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserCategoryLimitController.class)
@Import(TimeConfig.class)
class UserCategoryLimitControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCategoryLimitService userCategoryLimitService;

    @Test
    void updatesCategoryLimitUsingSpanishCategory() throws Exception {
        when(userCategoryLimitService.upsertMyCategoryLimit(
                1L,
                ExpenseCategory.FOOD,
                new UserCategoryLimitRequestDto(new BigDecimal("300.00"))
        )).thenReturn(new UserCategoryLimitResponseDto(ExpenseCategory.FOOD, new BigDecimal("300.00")));

        mockMvc.perform(put("/api/users/me/category-limits/comida")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monthlyLimit": "300,00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.monthlyLimit").value(300.00));
    }

    @Test
    void rejectsNonPositiveCategoryLimit() throws Exception {
        mockMvc.perform(put("/api/users/me/category-limits/comida")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monthlyLimit": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value(
                        "monthlyLimit: El limite mensual debe ser mayor que cero"
                ));

        verifyNoInteractions(userCategoryLimitService);
    }
}
