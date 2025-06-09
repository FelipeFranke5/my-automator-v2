package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.dto.CompletedAutomationOutputForExcel;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.model.CheckoutCompletedAutomation;
import dev.franke.felipee.braspag_automator_v2.contracts.service.EcSearchFileHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckoutFileHandler implements EcSearchFileHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutFileHandler.class);

    private final CheckoutCompletedAutomationService automationService;
    private final ObjectMapper objectMapper;

    public CheckoutFileHandler(final CheckoutCompletedAutomationService automationService, ObjectMapper objectMapper) {
        this.automationService = automationService;
        this.objectMapper = objectMapper;
    }

    public Optional<CheckoutCompletedAutomation> getMerchantDataFromFile(String ec) {
        if (ec == null || ec.isBlank() || ec.length() != 10) {
            LOG.warn("EC is not valid, returning null");
            return Optional.empty();
        }

        try {
            LOG.info("[{}] Attempting to read the JSON file", ec);
            return Optional.of(objectMapper.readValue(new File(ec + ".json"), CheckoutCompletedAutomation.class));
        } catch (final IOException ioException) {
            LOG.error("[{}] There was an error while attempting to read the JSON file", ec, ioException);
            return Optional.empty();
        }
    }

    @Override
    public void deleteJsonFileAfterAutomation(String ec) {
        if (ec == null || ec.isBlank() || ec.length() != 10) {
            LOG.warn("Not deleting file, because EC is not valid");
            return;
        }

        try {
            LOG.info("[{}] Attempting to delete the JSON file", ec);
            final boolean result = Files.deleteIfExists(Path.of(ec + ".json"));
            LOG.info("[{}] Result of deletion: {}", ec, result);
        } catch (final IOException ioException) {
            LOG.error("There was an error while attempting to delete the JSON file", ioException);
        }
    }

    @Override
    public byte[] writeToExcelFile() throws IOException {
        LOG.info("Initializing function to write to excel");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet("Dados Checkout");
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 4000);

            final Row header = sheet.createRow(0);
            final CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            final XSSFFont font = workbook.createFont();
            font.setFontName("Calibri");
            font.setFontHeightInPoints((short) 12);
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);

            final CellStyle bodyStyle = workbook.createCellStyle();
            final XSSFFont bodyFont = workbook.createFont();
            bodyFont.setFontName("Calibri");
            bodyFont.setFontHeightInPoints((short) 9);
            bodyStyle.setFont(bodyFont);
            bodyStyle.setAlignment(HorizontalAlignment.CENTER);
            bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Cell headerCell = header.createCell(0);
            headerCell.setCellValue("ID do Registro");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(1);
            headerCell.setCellValue("EC");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(2);
            headerCell.setCellValue("Gostaria de ser chamado de");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(3);
            headerCell.setCellValue("Nome Fantasia");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(4);
            headerCell.setCellValue("Loja Bloqueada");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(5);
            headerCell.setCellValue("Modo de Teste Ativo");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(6);
            headerCell.setCellValue("Aceita Pagamento Internacional");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(7);
            headerCell.setCellValue("URL de Notificação");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(8);
            headerCell.setCellValue("URL de Retorno");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(9);
            headerCell.setCellValue("URL de Mudança de Status");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(10);
            headerCell.setCellValue("3DS Habilitado");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(11);
            headerCell.setCellValue("EC Amex");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(12);
            headerCell.setCellValue("Autenticação Facial Habilitada");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(13);
            headerCell.setCellValue("Data e Hora do Registro");
            headerCell.setCellStyle(headerStyle);

            final AtomicReference<Short> currentRowNumber = new AtomicReference<>((short) 1);
            final List<CompletedAutomationOutputForExcel> merchants = automationService.outputForExcel();

            merchants.stream().forEach(merchant -> {
                final Row newRow = sheet.createRow(currentRowNumber.get());

                final Cell ecCell = newRow.createCell(0);
                ecCell.setCellValue(merchant.recordId().toString());
                ecCell.setCellStyle(bodyStyle);

                final Cell midCell = newRow.createCell(1);
                midCell.setCellValue(merchant.ec());
                midCell.setCellStyle(bodyStyle);

                final Cell documentTypeCell = newRow.createCell(2);
                documentTypeCell.setCellValue(merchant.alias());
                documentTypeCell.setCellStyle(bodyStyle);

                final Cell documentCell = newRow.createCell(3);
                documentCell.setCellValue(merchant.name());
                documentCell.setCellStyle(bodyStyle);

                final Cell nameCell = newRow.createCell(4);
                nameCell.setCellValue(merchant.blocked() ? "Sim" : "Não");
                nameCell.setCellStyle(bodyStyle);

                final Cell createdAtCell = newRow.createCell(5);
                createdAtCell.setCellValue(merchant.testModeEnabled() ? "Sim" : "Não");
                createdAtCell.setCellStyle(bodyStyle);

                final Cell pixEnabledCell = newRow.createCell(6);
                pixEnabledCell.setCellValue(merchant.internationalPaymentEnabled() ? "Sim" : "Não");
                pixEnabledCell.setCellStyle(bodyStyle);

                final Cell antifraudEnabledCell = newRow.createCell(7);
                antifraudEnabledCell.setCellValue(merchant.notificationUrl());
                antifraudEnabledCell.setCellStyle(bodyStyle);

                final Cell tokenizationEnabledCell = newRow.createCell(8);
                tokenizationEnabledCell.setCellValue(merchant.returnUrl());
                tokenizationEnabledCell.setCellStyle(bodyStyle);

                final Cell velocityEnabledCell = newRow.createCell(9);
                velocityEnabledCell.setCellValue(merchant.statusChangeUrl());
                velocityEnabledCell.setCellStyle(bodyStyle);

                final Cell recurrentEnabledCell = newRow.createCell(10);
                recurrentEnabledCell.setCellValue(merchant.threeDSEnabled() ? "Sim" : "Não");
                recurrentEnabledCell.setCellStyle(bodyStyle);

                final Cell zeroDollarEnabledCell = newRow.createCell(11);
                zeroDollarEnabledCell.setCellValue(merchant.amexMid());
                zeroDollarEnabledCell.setCellStyle(bodyStyle);

                final Cell binQueryEnabledCell = newRow.createCell(12);
                binQueryEnabledCell.setCellValue(merchant.facialAuthEnabled() ? "Sim" : "Não");
                binQueryEnabledCell.setCellStyle(bodyStyle);

                final Cell selectiveAuthEnabledCell = newRow.createCell(13);
                selectiveAuthEnabledCell.setCellValue(merchant.recordTimestamp());
                selectiveAuthEnabledCell.setCellStyle(bodyStyle);

                currentRowNumber.getAndSet((short) (currentRowNumber.get() + 1));
            });

            LOG.info("Attempting to write to file");

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                LOG.info("Wrote successfully");
                return outputStream.toByteArray();
            }
        }
    }
}
