package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
