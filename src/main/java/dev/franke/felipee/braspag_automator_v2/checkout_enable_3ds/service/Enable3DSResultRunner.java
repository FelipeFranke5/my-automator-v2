package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.AutomationsRunningOutput;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.utils.ProcessExecutionEnable3DS;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Enable3DSResultRunner {

  private static final Logger LOG = LoggerFactory.getLogger(Enable3DSResultRunner.class);
  private static final ExecutorService executor = Executors.newCachedThreadPool();

  private static final String INTERRUPTED_THREAD_MESSAGE =
      "Thread interrompida e falha na execucao";
  private static final String SUCESS_MESSAGE = "3DS Habilitado";
  private static final String GENERIC_ERROR_MESSAGE = "Falha na Execucao";
  private static final String INVALID_CREDENTIALS_MESSAGE = "Credenciais Invalidas";
  private static final String EC_NOT_FOUND_MESSAGE = "EC nao localizado na base checkout";
  private static final String BRASPAG_INTERNAL_ERROR_MESSAGE = "Braspag Intermitente";
  private static final String INVALID_PARAMETERS_MESSAGE = "Parametros Invalidos";
  private static final String MERCHANT_IS_BLOCKED_MESSAGE = "Loja Bloqueada";
  private static final String ALREADY_ENABLED_MESSAGE = "3DS ja esta habilitado";
  private static final String TIMEOUT_MESSAGE = "Timeout na espera para executar";

  private final Enable3DSResultService enable3dsResultService;
  private final CheckoutMerchantValidator checkoutMerchantValidator;
  private final ProcessExecutionEnable3DS processExecutionEnable3DS;

  public Enable3DSResultRunner(
      final Enable3DSResultService enable3dsResultService,
      final CheckoutMerchantValidator checkoutMerchantValidator,
      final ProcessExecutionEnable3DS processExecutionEnable3DS) {
    this.enable3dsResultService = enable3dsResultService;
    this.checkoutMerchantValidator = checkoutMerchantValidator;
    this.processExecutionEnable3DS = processExecutionEnable3DS;
  }

  public AutomationsRunningOutput numberOfAutomationsRunning() {
    final byte automations = processExecutionEnable3DS.getNumberOfPythonProcesses();
    String message;
    if (automations < 0) {
      message = "Erro ao obter o numero de automacoes em execucao";
      return new AutomationsRunningOutput(automations, message);
    }
    if (automations == 0) {
      message = "Nenhuma automacao sendo executada";
      return new AutomationsRunningOutput(automations, message);
    }
    message = "Automacoes em execucao";
    return new AutomationsRunningOutput(automations, message);
  }

  public void run(final String[] ecs) {
    LOG.info("Initilizing..");

    if (!(checkoutMerchantValidator.allEcsAreValid(ecs))) {
      LOG.warn("EC list is not valid..");
      return;
    }

    LOG.info("List is valid");

    CompletableFuture.runAsync(
        () -> {
          for (final String ec : ecs) {
            briefDelayForAutomation(ec);
            CompletableFuture.runAsync(
                () -> {
                  singleEcAutomation(ec);
                },
                executor);
          }
        },
        executor);

    LOG.info("Ran");
  }

  private void singleEcAutomation(final String ec) {
    byte currentAutomations = numberOfAutomationsRunning().numberOfAutomations();
    final byte timeout = 60; // Timeout in minutes - For checkout we need more ..
    final LocalDateTime startTime = LocalDateTime.now();

    while (currentAutomations > 0) {
      if (LocalDateTime.now().isAfter(startTime.plusMinutes(timeout))) {
        LOG.warn("[{}] Timeout reached while waiting for automations to finish", ec);
        enable3dsResultService.save(ec, TIMEOUT_MESSAGE);
        return;
      }

      LOG.info("[{}] Waiting for automations to finish. Current count: {}", ec, currentAutomations);
      currentAutomations = waitForAutomationCompletion(ec, currentAutomations);
    }

    LOG.info("[{}] Starting Automation process for EC", ec);
    final String message = getResultMessageFromExecutionLastLine(getLastLine(ec));
    enable3dsResultService.save(ec, message);
  }

  private byte waitForAutomationCompletion(final String ec, byte currentAutomations) {
    try {
      Thread.sleep(10000); // Wait 10 seconds before checking again
      currentAutomations = numberOfAutomationsRunning().numberOfAutomations();
    } catch (final InterruptedException interruptedException) {
      LOG.error("[{}] Thread was interrupted while waiting", ec, interruptedException);
      enable3dsResultService.save(ec, INTERRUPTED_THREAD_MESSAGE);
      Thread.currentThread().interrupt();
    }
    return currentAutomations;
  }

  private void briefDelayForAutomation(final String ec) {
    LOG.info("[{}] Waiting a few seconds before starting automation", ec);

    try {
      Thread.sleep(5000); // Wait 5 seconds before starting automation
      LOG.info("[{}] Finished waiting!", ec);
    } catch (final InterruptedException interruptedException) {
      LOG.error("Thread was interrupted while waiting", interruptedException);
      enable3dsResultService.save(ec, INTERRUPTED_THREAD_MESSAGE);
      Thread.currentThread().interrupt(); // Restore interrupted status
    }
  }

  private String getResultMessageFromExecutionLastLine(final byte result) {
    return switch (result) {
      case 0 -> SUCESS_MESSAGE;
      case 1 -> INVALID_CREDENTIALS_MESSAGE;
      case 2 -> EC_NOT_FOUND_MESSAGE;
      case 3 -> BRASPAG_INTERNAL_ERROR_MESSAGE;
      case 4 -> INVALID_PARAMETERS_MESSAGE;
      case 6 -> MERCHANT_IS_BLOCKED_MESSAGE;
      case 8 -> ALREADY_ENABLED_MESSAGE;
      case 5, 7, 9, 10 -> GENERIC_ERROR_MESSAGE;
      default -> GENERIC_ERROR_MESSAGE;
    };
  }

  private byte getLastLine(final String ecNumber) {
    final String[] singleEcArray = {ecNumber};

    // Not valid == just return -1
    if (!(checkoutMerchantValidator.allEcsAreValid(singleEcArray))) return -1;

    LOG.info("[{}] Attempting to get last line", ecNumber);
    String lastLine = "";

    try {
      lastLine = processExecutionEnable3DS.run(ecNumber);
    } catch (final IOException ioException) {
      LOG.warn("[{}] Error while trying to execute process", ecNumber);
      LOG.error("Exception has occured while attempting to execute process", ioException);
      return -1;
    }

    return switch (lastLine) {
      case "3DS enabled!" -> 0;
      case "Invalid credentials" -> 1;
      case "EC not found" -> 2;
      case "Braspag internal error" -> 3;
      case "Missing required arguments",
              "Invalid username length",
              "Invalid password length",
              "Invalid ec length",
              "Invalid ec" ->
          4;
      case "Could not find a element" -> 5;
      case "Merchant is Blocked" -> 6;
      case "Merchant Elements Not Found" -> 7;
      case "3DS Already Enabled" -> 8;
      case "Error while trying to save" -> 9;
      case "Unexpected error" -> 10;
      default -> -1;
    };
  }
}
