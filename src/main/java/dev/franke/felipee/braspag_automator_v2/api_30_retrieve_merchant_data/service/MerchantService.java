package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.OutputMerchant;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ReadScriptJsonException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ScriptLastLineIsBlankException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ScriptLastLineIsNullException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ScriptLastLineIsStdErrorException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.repository.MerchantRepository;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.utils.ProcessExecutionAPI30;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

  private static final Logger LOG = LoggerFactory.getLogger(MerchantService.class);
  private static final ExecutorService executor = Executors.newCachedThreadPool();

  @Value("${braspag.prod.login}")
  private String login;

  @Value("${braspag.prod.password}")
  private String password;

  private final ProcessExecutionAPI30 processExecution;
  private final ObjectMapper objectMapper;
  private final AutomationFileHandler fileHandler;
  private final MerchantRepository merchantRepository;
  private final MerchantValidator validator;

  public MerchantService(
      ProcessExecutionAPI30 processExecution,
      ObjectMapper objectMapper,
      AutomationFileHandler fileHandler,
      MerchantRepository merchantRepository,
      MerchantValidator validator) {
    this.processExecution = processExecution;
    this.objectMapper = objectMapper;
    this.fileHandler = fileHandler;
    this.merchantRepository = merchantRepository;
    this.validator = validator;
  }

  public List<OutputMerchant> listOfMerchants() {
    List<Merchant> merchants = merchantRepository.findAll();
    List<OutputMerchant> output = new ArrayList<>();
    merchants.stream()
        .forEach(
            merchant -> {
              OutputMerchant outputMerchant =
                  new OutputMerchant(
                      merchant.getRecordId(),
                      merchant.getEc(),
                      merchant.getId(),
                      merchant.getDocumentType(),
                      merchant.getDocumentNumber(),
                      merchant.getName(),
                      merchant.getCreatedAt(),
                      merchant.isBlocked(),
                      merchant.isPixEnabled(),
                      merchant.isAntifraudEnabled(),
                      merchant.isTokenizationEnabled(),
                      merchant.isVelocityEnabled(),
                      merchant.isSmartRecurrencyEnabled(),
                      merchant.isZeroDollarAuthEnabled(),
                      merchant.isBinQueryEnabled(),
                      merchant.isSelectiveAuthEnabled(),
                      merchant.isTryAutomaticCancellationEnabled(),
                      merchant.isForceBraspagAuthEnabled(),
                      merchant.isMtlsEnabled(),
                      merchant.isWebhookEnabled(),
                      merchant.getWhiteListIpCount(),
                      merchant.getRecordTimestamp());
              output.add(outputMerchant);
            });
    return output;
  }

  public void clearAllMerchants() {
    merchantRepository.deleteAll();
  }

  private void saveMerchantToDatabase(Merchant merchant) {
    LOG.info("Attempting to save record");
    try {
      merchantRepository.save(merchant);
      LOG.info("Record saved");
    } catch (Exception exception) {
      LOG.error("Error during save", exception);
    }
  }

  // This should be called first
  public boolean inputIsValid(String merchants) {
    return validator.validInput(merchants);
  }

  // This should be called second
  public void runAutomation(String merchants) {
    LOG.info("Starting Automation!");

    if (!(inputIsValid(merchants))) {
      LOG.warn("Input is not valid. Returning right now");
      return;
    }

    if (merchants.length() > 10) {
      runAutomationForMultipleMerchants(merchants);
    } else {
      runAutomationForSingleMerchant(merchants);
    }
  }

  private void runAutomationForSingleMerchant(String ec) {
    LOG.info("Starting Automation process for EC");
    Optional<Merchant> result = getAutomationResult(ec);
    result.ifPresent(
        res -> {
          LOG.info("Automation Result is present. Saving to database");
          saveMerchantToDatabase(res);
        });
  }

  private Optional<Merchant> getAutomationResult(String ec) {
    LOG.info("Attempting to retrieve data for this merchant");
    Merchant merchant = null;

    if (ec == null || ec.isBlank() || ec.length() != 10) {
      LOG.warn("Not executing automation, because EC is not valid");
      return Optional.empty();
    }

    try {
      merchant = getMerchantDataFromFile(ec);
      LOG.info("Data was retrieved");
      return Optional.of(merchant);
    } catch (ScriptLastLineIsBlankException
        | ReadScriptJsonException
        | ScriptLastLineIsNullException execException) {
      LOG.error("There was a error during execution", execException);
      return Optional.empty();
    } finally {
      CompletableFuture.runAsync(() -> fileHandler.deleteJsonFileAfterAutomation(ec), executor);
    }
  }

  private void runAutomationForMultipleMerchants(String merchants) {
    LOG.info("Multiple EC's identified");
    String[] merchantsArray = merchants.split(",");

    for (String ec : merchantsArray) {
      CompletableFuture.runAsync(() -> runAutomationForSingleMerchant(ec), executor);
    }
  }

  private Merchant getMerchantDataFromFile(String ec)
      throws ScriptLastLineIsNullException,
          ScriptLastLineIsBlankException,
          ReadScriptJsonException {
    try {
      String lastLine = processExecution.run(ec);
      if (lastLine == null)
        throw new ScriptLastLineIsNullException("Script Failed - Last Line Null");
      if (lastLine.isBlank())
        throw new ScriptLastLineIsBlankException("Script Failed - Last Line Blank");
      if (!lastLine.equalsIgnoreCase("Result is in JSON file"))
        throw new ScriptLastLineIsStdErrorException("Script Failed with std error - " + lastLine);
      return objectMapper.readValue(new File(ec + ".json"), Merchant.class);
    } catch (IOException ioException) {
      String message =
          "Error while trying to read from file '"
              + ec
              + ".json' - Message: "
              + ioException.getMessage();
      throw new ReadScriptJsonException(message);
    }
  }
}
