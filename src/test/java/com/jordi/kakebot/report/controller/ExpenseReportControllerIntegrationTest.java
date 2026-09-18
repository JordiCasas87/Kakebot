package com.jordi.kakebot.report.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jordi.kakebot.common.config.TimeConfig;
import com.jordi.kakebot.report.service.ExpenseReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExpenseReportController.class)
@Import(TimeConfig.class)
class ExpenseReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseReportService expenseReportService;

    @Test
    void downloadsMonthlyReportAsPdfAttachment() throws Exception {
        byte[] pdf = "%PDF-1.4 test".getBytes();
        when(expenseReportService.generateMonthlyExpensePdf(1L, 2026, 9)).thenReturn(pdf);

        mockMvc.perform(get("/api/reports/expenses/monthly/pdf")
                        .header("X-User-Id", 1L)
                        .param("year", "2026")
                        .param("month", "9"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"kakebot-gastos-2026-09.pdf\""
                ))
                .andExpect(content().bytes(pdf));
    }
}
