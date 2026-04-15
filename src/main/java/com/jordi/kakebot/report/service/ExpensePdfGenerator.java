package com.jordi.kakebot.report.service;

import com.jordi.kakebot.expense.dto.CategoryTotalResponseDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.report.dto.MonthlyExpenseReportData;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ExpensePdfGenerator {

    private static final String BACKGROUND_IMAGE_PATH = "static/images/fondoExplicacionClaro.png";
    private static final String LOGO_TEXT_IMAGE_PATH = "static/images/kakebotTexto.png";
    private static final String WINK_BOT_IMAGE_PATH = "static/images/kakebotGuiño.png";
    private static final Color DARK_BROWN = new Color(47, 32, 8);
    private static final Color SOFT_YELLOW = new Color(250, 238, 202);
    private static final Color LIGHT_CREAM = new Color(255, 252, 244);
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateMonthlyReport(MonthlyExpenseReportData reportData) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setPageEvent(new BackgroundPageEvent(BACKGROUND_IMAGE_PATH));
            document.open();

            addHeader(document, reportData);
            addCategorySummary(document, reportData);
            addExpenseDetail(document, reportData);

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException exception) {
            throw new IllegalStateException("No se ha podido generar el PDF de gastos.", exception);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("No se ha podido preparar el PDF de gastos.", exception);
        }
    }

    private void addHeader(Document document, MonthlyExpenseReportData reportData) throws DocumentException {
        Image logoText = loadImage(LOGO_TEXT_IMAGE_PATH);
        logoText.scaleToFit(140, 42);
        logoText.setAlignment(Element.ALIGN_CENTER);
        logoText.setSpacingAfter(8);
        document.add(logoText);

        Paragraph title = new Paragraph("Resumen mensual de gastos", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, DARK_BROWN));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        document.add(title);

        Image winkBot = loadImage(WINK_BOT_IMAGE_PATH);
        winkBot.scaleToFit(92, 92);
        winkBot.setAlignment(Element.ALIGN_CENTER);
        winkBot.setSpacingAfter(10);
        document.add(winkBot);

        Paragraph meta = new Paragraph(
                "%s · Usuario: %s".formatted(formatPeriod(reportData), reportData.username()),
                FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(92, 67, 22))
        );
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.setSpacingAfter(18);
        document.add(meta);

        PdfPTable totalTable = new PdfPTable(1);
        totalTable.setWidthPercentage(100);
        PdfPCell totalCell = new PdfPCell(new Phrase(
                "Total del mes: %s".formatted(formatAmount(reportData.total())),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, DARK_BROWN)
        ));
        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalCell.setPadding(14);
        totalCell.setBackgroundColor(SOFT_YELLOW);
        totalCell.setBorderColor(new Color(210, 169, 72));
        totalTable.addCell(totalCell);
        totalTable.setSpacingAfter(18);
        document.add(totalTable);
    }

    private void addCategorySummary(Document document, MonthlyExpenseReportData reportData) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Totales por categoria", sectionFont());
        sectionTitle.setSpacingAfter(8);
        document.add(sectionTitle);

        PdfPTable table = new PdfPTable(new float[]{3f, 2f});
        table.setWidthPercentage(100);
        addHeaderCell(table, "Categoria");
        addHeaderCell(table, "Total");

        for (CategoryTotalResponseDto categoryTotal : reportData.categoryTotals()) {
            addBodyCell(table, formatCategory(categoryTotal.category()));
            addAmountCell(table, formatAmount(categoryTotal.total()));
        }

        table.setSpacingAfter(18);
        document.add(table);
    }

    private void addExpenseDetail(Document document, MonthlyExpenseReportData reportData) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Detalle de gastos", sectionFont());
        sectionTitle.setSpacingAfter(8);
        document.add(sectionTitle);

        if (reportData.expenses().isEmpty()) {
            Paragraph emptyMessage = new Paragraph(
                    "No hay gastos registrados en este mes.",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(92, 67, 22))
            );
            document.add(emptyMessage);
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{2.2f, 1.8f, 4f, 1.6f});
        table.setWidthPercentage(100);
        addHeaderCell(table, "Fecha");
        addHeaderCell(table, "Categoria");
        addHeaderCell(table, "Descripcion");
        addHeaderCell(table, "Importe");

        for (ExpenseResponseDto expense : reportData.expenses()) {
            addBodyCell(table, DATE_FORMATTER.format(expense.registeredAt()));
            addBodyCell(table, formatCategory(expense.category()));
            addBodyCell(table, expense.description());
            addAmountCell(table, formatAmount(expense.amount()));
        }

        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
        cell.setBackgroundColor(DARK_BROWN);
        cell.setPadding(7);
        cell.setBorderColor(DARK_BROWN);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 9, DARK_BROWN)));
        cell.setPadding(7);
        cell.setBackgroundColor(LIGHT_CREAM);
        cell.setBorderColor(new Color(232, 216, 176));
        table.addCell(cell);
    }

    private void addAmountCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, DARK_BROWN)));
        cell.setPadding(7);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBackgroundColor(LIGHT_CREAM);
        cell.setBorderColor(new Color(232, 216, 176));
        table.addCell(cell);
    }

    private Font sectionFont() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, DARK_BROWN);
    }

    private String formatPeriod(MonthlyExpenseReportData reportData) {
        String period = MONTH_FORMATTER.format(reportData.period());
        return period.substring(0, 1).toUpperCase(Locale.ROOT) + period.substring(1);
    }

    private String formatCategory(ExpenseCategory category) {
        return switch (category) {
            case HOME -> "Casa";
            case FOOD -> "Comida";
            case TRANSPORT -> "Transporte";
            case LEISURE -> "Ocio";
            case OTHER -> "Otros";
        };
    }

    private String formatAmount(BigDecimal amount) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(new Locale("es", "ES"));
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00 €", symbols);
        return decimalFormat.format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    private Image loadImage(String classpathImagePath) {
        try {
            ClassPathResource imageResource = new ClassPathResource(classpathImagePath);
            return Image.getInstance(imageResource.getInputStream().readAllBytes());
        } catch (IOException exception) {
            throw new IllegalStateException("No se ha podido cargar la imagen del PDF: " + classpathImagePath, exception);
        }
    }

    private static class BackgroundPageEvent extends PdfPageEventHelper {

        private final String classpathImagePath;

        private BackgroundPageEvent(String classpathImagePath) {
            this.classpathImagePath = classpathImagePath;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Image background = Image.getInstance(new ClassPathResource(classpathImagePath)
                        .getInputStream()
                        .readAllBytes());
                Rectangle pageSize = document.getPageSize();
                background.scaleAbsolute(pageSize.getWidth(), pageSize.getHeight());
                background.setAbsolutePosition(0, 0);

                PdfContentByte canvas = writer.getDirectContentUnder();
                canvas.addImage(background);
            } catch (IOException | DocumentException exception) {
                throw new IllegalStateException("No se ha podido aplicar el fondo del PDF.", exception);
            }
        }
    }
}
