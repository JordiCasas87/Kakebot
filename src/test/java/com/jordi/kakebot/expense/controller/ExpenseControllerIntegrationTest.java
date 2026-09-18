package com.jordi.kakebot.expense.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jordi.kakebot.common.config.TimeConfig;
import com.jordi.kakebot.expense.dto.ExpenseRequestDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.exception.ExpenseNotFoundException;
import com.jordi.kakebot.expense.service.ExpenseService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExpenseController.class)
@Import(TimeConfig.class)
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    void createsExpenseFromHttpRequest() throws Exception {
        ExpenseResponseDto response = new ExpenseResponseDto(
                10L,
                ExpenseCategory.FOOD,
                "Menu del dia",
                new BigDecimal("12.50"),
                LocalDateTime.of(2026, 9, 18, 14, 30)
        );
        when(expenseService.createExpense(
                1L,
                new ExpenseRequestDto(ExpenseCategory.FOOD, "Menu del dia", new BigDecimal("12.50"))
        )).thenReturn(response);

        mockMvc.perform(post("/api/expenses")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "comida",
                                  "description": "Menu del dia",
                                  "amount": "12,50"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.amount").value(12.50));
    }

    @Test
    void rejectsInvalidExpenseBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "comida",
                                  "description": "",
                                  "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Datos de entrada invalidos"))
                .andExpect(jsonPath("$.path").value("/api/expenses"))
                .andExpect(jsonPath("$.details.length()").value(2));

        verifyNoInteractions(expenseService);
    }

    @Test
    void translatesServiceExceptionToNotFoundResponse() throws Exception {
        org.mockito.Mockito.doThrow(new ExpenseNotFoundException(99L))
                .when(expenseService).deleteExpense(1L, 99L);

        mockMvc.perform(delete("/api/expenses/99").header("X-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/expenses/99"));

        verify(expenseService).deleteExpense(1L, 99L);
    }
}
