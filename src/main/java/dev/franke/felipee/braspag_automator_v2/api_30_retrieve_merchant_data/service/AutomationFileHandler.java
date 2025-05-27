package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AutomationFileHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AutomationFileHandler.class);

  public void deleteExcelFileAfterNotification() {
    try {
      LOG.info("Attempting to delete the Excel file");
      LOG.info(
          "Does the file exist? {}",
          new File("automation.xlsx").exists() ? "Yes, it does" : "No, it does not");
      Files.deleteIfExists(Path.of("automation.xlsx"));
    } catch (IOException ioException) {
      LOG.error("There was an error while attempting to delete the Excel file", ioException);
    }
  }

  public void deleteJsonFileAfterAutomation(String ec) {
    if (ec == null || ec.isBlank() || ec.length() != 10) {
      LOG.warn("Not deleting file, because EC is not valid");
      return;
    }

    try {
      LOG.info("Attempting to delete the JSON file");
      Path filePath = Path.of(ec + ".json");
      LOG.info(
          "Does the file exist? {}",
          new File(filePath.toString()).exists() ? "Yes, it does" : "No, it does not");
      boolean result = Files.deleteIfExists(Path.of(ec + ".json"));
      LOG.info("Result of deletion: {}", result);
    } catch (IOException ioException) {
      LOG.error("There was an error while attempting to delete the JSON file", ioException);
    }
  }

  public byte[] writeToExcelFile(List<Merchant> merchants) throws IOException {
    LOG.info("Initializing function to write to excel");
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Serviços Habilitados");
      sheet.setColumnWidth(0, 6000);
      sheet.setColumnWidth(1, 4000);

      Row header = sheet.createRow(0);
      CellStyle headerStyle = workbook.createCellStyle();
      headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
      headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      headerStyle.setAlignment(HorizontalAlignment.CENTER);
      headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

      XSSFFont font = workbook.createFont();
      font.setFontName("Calibri");
      font.setFontHeightInPoints((short) 12);
      font.setBold(true);
      font.setColor(IndexedColors.WHITE.getIndex());
      headerStyle.setFont(font);

      CellStyle bodyStyle = workbook.createCellStyle();
      XSSFFont bodyFont = workbook.createFont();
      bodyFont.setFontName("Calibri");
      bodyFont.setFontHeightInPoints((short) 9);
      bodyStyle.setFont(bodyFont);
      bodyStyle.setAlignment(HorizontalAlignment.CENTER);
      bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);

      Cell headerCell = header.createCell(0);
      headerCell.setCellValue("EC");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(1);
      headerCell.setCellValue("Merchant Id");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(2);
      headerCell.setCellValue("Tipo de Documento");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(3);
      headerCell.setCellValue("Documento");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(4);
      headerCell.setCellValue("Nome Fantasia");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(5);
      headerCell.setCellValue("Data de Criação");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(6);
      headerCell.setCellValue("Loja Bloqueada");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(7);
      headerCell.setCellValue("Pix Habilitado");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(8);
      headerCell.setCellValue("Possui Antifraude");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(9);
      headerCell.setCellValue("Possui Tokenização");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(10);
      headerCell.setCellValue("Velocity Habilitado");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(11);
      headerCell.setCellValue("Recorrência Habilitada");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(12);
      headerCell.setCellValue("Zero Auth Habilitado");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(13);
      headerCell.setCellValue("Consulta BIN Habilitado");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(14);
      headerCell.setCellValue("Autenticação Seletiva Habilitada");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(15);
      headerCell.setCellValue("Cancelamento Garantido Habilitado");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(16);
      headerCell.setCellValue("Forçar Autenticação Braspag Habilitado");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(17);
      headerCell.setCellValue("MTLS Habilitado");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(18);
      headerCell.setCellValue("Webhook Cadastrado");
      headerCell.setCellStyle(headerStyle);

      headerCell = header.createCell(19);
      headerCell.setCellValue("Quantidade de IP's");
      headerCell.setCellStyle(headerStyle);

      AtomicReference<Short> currentRowNumber = new AtomicReference<>((short) 1);

      merchants.stream()
          .forEach(
              merchant -> {
                Row newRow = sheet.createRow(currentRowNumber.get());

                Cell ecCell = newRow.createCell(0);
                ecCell.setCellValue(merchant.getEc());
                ecCell.setCellStyle(bodyStyle);

                Cell midCell = newRow.createCell(1);
                midCell.setCellValue(merchant.getId().toString());
                midCell.setCellStyle(bodyStyle);

                Cell documentTypeCell = newRow.createCell(2);
                documentTypeCell.setCellValue(merchant.getDocumentType());
                documentTypeCell.setCellStyle(bodyStyle);

                Cell documentCell = newRow.createCell(3);
                documentCell.setCellValue(merchant.getDocumentNumber());
                documentCell.setCellStyle(bodyStyle);

                Cell nameCell = newRow.createCell(4);
                nameCell.setCellValue(merchant.getName());
                nameCell.setCellStyle(bodyStyle);

                Cell createdAtCell = newRow.createCell(5);
                createdAtCell.setCellValue(merchant.getCreatedAt());
                createdAtCell.setCellStyle(bodyStyle);

                Cell blockedCell = newRow.createCell(6);
                blockedCell.setCellValue(merchant.isBlocked() ? "SIM" : "NÃO");
                blockedCell.setCellStyle(bodyStyle);

                Cell pixEnabledCell = newRow.createCell(7);
                pixEnabledCell.setCellValue(merchant.isPixEnabled() ? "SIM" : "NÃO");
                pixEnabledCell.setCellStyle(bodyStyle);

                Cell antifraudEnabledCell = newRow.createCell(8);
                antifraudEnabledCell.setCellValue(merchant.isAntifraudEnabled() ? "SIM" : "NÃO");
                antifraudEnabledCell.setCellStyle(bodyStyle);

                Cell tokenizationEnabledCell = newRow.createCell(9);
                tokenizationEnabledCell.setCellValue(
                    merchant.isTokenizationEnabled() ? "SIM" : "NÃO");
                tokenizationEnabledCell.setCellStyle(bodyStyle);

                Cell velocityEnabledCell = newRow.createCell(10);
                velocityEnabledCell.setCellValue(merchant.isVelocityEnabled() ? "SIM" : "NÃO");
                velocityEnabledCell.setCellStyle(bodyStyle);

                Cell recurrentEnabledCell = newRow.createCell(11);
                recurrentEnabledCell.setCellValue(
                    merchant.isSmartRecurrencyEnabled() ? "SIM" : "NÃO");
                recurrentEnabledCell.setCellStyle(bodyStyle);

                Cell zeroDollarEnabledCell = newRow.createCell(12);
                zeroDollarEnabledCell.setCellValue(
                    merchant.isZeroDollarAuthEnabled() ? "SIM" : "NÃO");
                zeroDollarEnabledCell.setCellStyle(bodyStyle);

                Cell binQueryEnabledCell = newRow.createCell(13);
                binQueryEnabledCell.setCellValue(merchant.isBinQueryEnabled() ? "SIM" : "NÃO");
                binQueryEnabledCell.setCellStyle(bodyStyle);

                Cell selectiveAuthEnabledCell = newRow.createCell(14);
                selectiveAuthEnabledCell.setCellValue(
                    merchant.isSelectiveAuthEnabled() ? "SIM" : "NÃO");
                selectiveAuthEnabledCell.setCellStyle(bodyStyle);

                Cell automaticCancellationEnabledCell = newRow.createCell(15);
                automaticCancellationEnabledCell.setCellValue(
                    merchant.isTryAutomaticCancellationEnabled() ? "SIM" : "NÃO");
                automaticCancellationEnabledCell.setCellStyle(bodyStyle);

                Cell forceAuthEnabledCell = newRow.createCell(16);
                forceAuthEnabledCell.setCellValue(
                    merchant.isForceBraspagAuthEnabled() ? "SIM" : "NÃO");
                forceAuthEnabledCell.setCellStyle(bodyStyle);

                Cell mtlsEnabledCell = newRow.createCell(17);
                mtlsEnabledCell.setCellValue(merchant.isMtlsEnabled() ? "SIM" : "NÃO");
                mtlsEnabledCell.setCellStyle(bodyStyle);

                Cell webhookEnabledCell = newRow.createCell(18);
                webhookEnabledCell.setCellValue(merchant.isWebhookEnabled() ? "SIM" : "NÃO");
                webhookEnabledCell.setCellStyle(bodyStyle);

                Cell whiteListCell = newRow.createCell(19);
                whiteListCell.setCellValue(merchant.getWhiteListIpCount());
                whiteListCell.setCellStyle(bodyStyle);

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
