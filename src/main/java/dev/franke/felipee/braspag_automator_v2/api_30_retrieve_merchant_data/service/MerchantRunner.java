package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ReadScriptJsonException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ScriptLastLineIsBlankException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ScriptLastLineIsNullException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ScriptLastLineIsStdErrorException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.utils.ProcessExecutionAPI30;
import dev.franke.felipee.braspag_automator_v2.contracts.service.AutomationRunner;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MerchantRunner implements AutomationRunner {

    // Generic function to convert array to set
    public static <T> Set<T> convertArrayToSet(final T[] array) {
        return Arrays.stream(array).collect(Collectors.toSet());
    }

    private static final Logger LOG = LoggerFactory.getLogger(MerchantRunner.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final ProcessExecutionAPI30 processExecution;
    private final ObjectMapper objectMapper;
    private final AutomationFileHandler fileHandler;
    private final MerchantValidator validator;
    private final MerchantService service;
    private final FailedScriptService failedScriptService;

    public MerchantRunner(
            ProcessExecutionAPI30 processExecution,
            ObjectMapper objectMapper,
            AutomationFileHandler fileHandler,
            MerchantValidator validator,
            MerchantService service,
            FailedScriptService failedScriptService) {
        this.processExecution = processExecution;
        this.objectMapper = objectMapper;
        this.fileHandler = fileHandler;
        this.validator = validator;
        this.service = service;
        this.failedScriptService = failedScriptService;
    }

    @Override
    public void run(String[] merchantEcNumbers) {
        CompletableFuture.runAsync(
                () -> {
                    if (!validator.merchantArrayIsValid(merchantEcNumbers)) {
                        LOG.warn("Invalid input");
                        // TODO: save to err
                        return;
                    }
                    Set<String> ecs = getEcSet(merchantEcNumbers);
                    ecs.forEach(this::runAutomationForSingleMerchant);
                },
                executor);
    }

    private void runAutomationForSingleMerchant(String ec) {
        if (service.existsByEc(ec)) {
            LOG.warn("[{}] Automation will not be executed! Already set up", ec);
            return;
        }

        LOG.info("[{}] Starting Automation process", ec);
        Optional<Merchant> result = getAutomationResult(ec);

        result.ifPresent(res -> {
            LOG.info("[{}] Automation Result is present. Saving to database", ec);
            service.save(res);
        });
    }

    private Optional<Merchant> getAutomationResult(String ec) {
        if (ec == null || ec.isBlank() || ec.length() != 10) {
            LOG.warn("Not executing automation, because EC is not valid");
            failedScriptService.save(ec, "O numero do EC nao e valido");
            return Optional.empty();
        }

        LOG.info("[{}] Attempting to retrieve data for this merchant", ec);
        return fetchMerchantInfo(ec);
    }

    private Optional<Merchant> fetchMerchantInfo(String ec) {
        Merchant merchant;

        try {
            merchant = getMerchantDataFromFile(ec);
            LOG.info("[{}] Data was retrieved", ec);
            return Optional.of(merchant);
        } catch (ScriptLastLineIsBlankException
                | ReadScriptJsonException
                | ScriptLastLineIsStdErrorException
                | ScriptLastLineIsNullException execException) {
            LOG.error("[{}] There was a error during execution", ec, execException);
            String errorMessage = "Erro durante execucao do script: " + execException.getMessage();
            failedScriptService.save(ec, errorMessage);
            return Optional.empty();
        } finally {
            fileHandler.deleteJsonFileAfterAutomation(ec);
        }
    }

    private Set<String> getEcSet(String[] ecs) {
        return convertArrayToSet(ecs);
    }

    private void ensureLastLineValid(String ec) throws IOException {
        String lastLine = processExecution.run(ec);

        if (lastLine == null) {
            throw new ScriptLastLineIsNullException("Ultima linha do script e nula");
        }
        if (lastLine.isBlank()) {
            throw new ScriptLastLineIsBlankException("Ultima linha do script nao possui conteudo");
        }
        if (!lastLine.equalsIgnoreCase("Result is in JSON file")) {
            throw new ScriptLastLineIsStdErrorException("erro std na execucao: " + lastLine);
        }
        LOG.info("Last line is valid, proceeding to read JSON file");
    }

    private Merchant getMerchantDataFromFile(String ec)
            throws ScriptLastLineIsNullException, ScriptLastLineIsBlankException, ReadScriptJsonException {
        try {
            ensureLastLineValid(ec);
            return objectMapper.readValue(new File(ec + ".json"), Merchant.class);
        } catch (IOException ioException) {
            String message =
                    "Error while trying to read from file '" + ec + ".json' - Message: " + ioException.getMessage();
            throw new ReadScriptJsonException(message);
        }
    }
}
