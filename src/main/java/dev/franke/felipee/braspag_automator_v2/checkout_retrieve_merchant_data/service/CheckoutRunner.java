package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.CheckoutMerchantValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.controller.CheckoutNumberOfProcesses;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.utils.ProcessExecutionCheckoutData;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckoutRunner {

  // Generic function to convert array to set
  public static <T> Set<T> convertArrayToSet(final T array[]) {
    return Arrays.stream(array).collect(Collectors.toSet());
  }

  private static final Logger LOG = LoggerFactory.getLogger(CheckoutRunner.class);
  private static final ExecutorService executor = Executors.newFixedThreadPool(2);

  private static final String TIMEOUT_MESSAGE = "Falha devido a Timeout";
  private static final String INVALID_CREDENTIALS_MESSAGE = "Falha devido a Credenciais Invalidas";
  private static final String EC_NOT_FOUND_MESSAGE = "EC nao encontrado";
  private static final String BP_INTERNAL_ERROR_MESSAGE = "Erro interno da Braspag";
  private static final String MISSING_REQUIRED_ARGS_MESSAGE = "Faltando argumentos obrigatorios";
  private static final String INVALID_USERNAME_LENGTH_MESSAGE =
      "Tamanho do nome de usuario invalido";
  private static final String INVALID_PASSWORD_LENGTH_MESSAGE = "Tamanho da senha invalido";
  private static final String INVALID_EC_LENGTH_MESSAGE = "Tamanho do EC invalido";
  private static final String COULD_NOT_FIND_ELEMENT_MESSAGE =
      "Nao foi possivel encontrar o elemento";

  private final CheckoutMerchantValidator validator;
  private final CheckoutFileHandler fileHandler;
  private final ProcessExecutionCheckoutData processExecutionCheckoutData;
  private final CheckoutFailedAutomationService failedAutomationService;
  private final CheckoutCompletedAutomationService automationService;

  public CheckoutRunner(
      final CheckoutMerchantValidator validator,
      final CheckoutFileHandler fileHandler,
      final ProcessExecutionCheckoutData processExecutionCheckoutData,
      final CheckoutFailedAutomationService failedAutomationService,
      final CheckoutCompletedAutomationService automationService) {
    this.validator = validator;
    this.fileHandler = fileHandler;
    this.processExecutionCheckoutData = processExecutionCheckoutData;
    this.failedAutomationService = failedAutomationService;
    this.automationService = automationService;
  }

  public CheckoutNumberOfProcesses getNumberOfProcesses() {
    return new CheckoutNumberOfProcesses(
        LocalDateTime.now(), processExecutionCheckoutData.getNumberOfPythonProcesses());
  }

  public void run(final String[] merchantEcNumbers) {
    LOG.info("Starting CheckoutRunner service...");
    CompletableFuture.runAsync(
        () -> {
          validateAndRun(merchantEcNumbers);
        },
        executor);
    LOG.info("CheckoutRunner service execution completed.");
  }

  //

  private void validateAndRun(final String[] merchantEcNumbers) {
    if (!validator.allEcsAreValid(merchantEcNumbers)) {
      final String invalidEcs =
          Arrays.stream(merchantEcNumbers)
              .filter(ec -> !validator.allEcsAreValid(new String[] {ec}))
              .collect(Collectors.joining(", "));
      final String errorMessage = "ECs invalidos: " + invalidEcs;
      failedAutomationService.save("ECs invalidos", errorMessage);
      return;
    }

    final Set<String> ecNumbersSet = convertArrayToSet(merchantEcNumbers);

    ecNumbersSet.stream()
        .forEach(
            ecNumber -> {
              CompletableFuture.runAsync(
                  () -> {
                    final byte result = singleEcRun(ecNumber);
                    if (result == 0) {
                      final var data = fileHandler.getMerchantDataFromFile(ecNumber);
                      data.ifPresent(automationService::save);
                    }
                    fileHandler.deleteJsonFileAfterAutomation(ecNumber);
                  },
                  executor);
            });
  }

  private byte singleEcRun(final String ecNumber) {
    byte finalResult = -3;

    try {
      final String lastLine = processExecutionCheckoutData.run(ecNumber);
      if (lastLine.equals("Merchant Wrote")) {
        finalResult = 0;
      } else {
        LOG.error("Error processing EC number: {}", ecNumber);
        final String errorMessage = getErrorMessage(lastLine);
        failedAutomationService.save(ecNumber, errorMessage);
        finalResult = -1;
      }
    } catch (final IOException ioException) {
      LOG.error("Error while running process for EC number: {}", ecNumber, ioException);
      final String errorMessage = "Erro: " + ioException.getMessage();
      failedAutomationService.save(ecNumber, errorMessage);
      finalResult = -1;
    }

    return finalResult;
  }

  private String getErrorMessage(final String lastLine) {
    return switch (lastLine) {
      case "Error finding element due to timeout" -> TIMEOUT_MESSAGE;
      case "Invalid credentials" -> INVALID_CREDENTIALS_MESSAGE;
      case "EC not found" -> EC_NOT_FOUND_MESSAGE;
      case "Braspag internal error" -> BP_INTERNAL_ERROR_MESSAGE;
      case "Missing required arguments" -> MISSING_REQUIRED_ARGS_MESSAGE;
      case "Invalid username length" -> INVALID_USERNAME_LENGTH_MESSAGE;
      case "Invalid password length" -> INVALID_PASSWORD_LENGTH_MESSAGE;
      case "Invalid ec length" -> INVALID_EC_LENGTH_MESSAGE;
      case "Could not find a element" -> COULD_NOT_FIND_ELEMENT_MESSAGE;
      default -> "Unknown error: " + lastLine;
    };
  }
}
