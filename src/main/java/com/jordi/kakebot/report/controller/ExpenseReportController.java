package com.jordi.kakebot.report.controller;

import com.jordi.kakebot.report.service.ExpenseReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/expenses")
public class ExpenseReportController {

    private final ExpenseReportService expenseReportService;

    public ExpenseReportController(ExpenseReportService expenseReportService) {
        this.expenseReportService = expenseReportService;
    }

    @GetMapping("/monthly/pdf")
    public ResponseEntity<byte[]> downloadMonthlyExpensePdf(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        byte[] pdf = expenseReportService.generateMonthlyExpensePdf(userId, year, month);
        String filename = "kakebot-gastos-%d-%02d.pdf".formatted(year, month);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename)
                        .build()
                        .toString())
                .body(pdf);
    }
}
