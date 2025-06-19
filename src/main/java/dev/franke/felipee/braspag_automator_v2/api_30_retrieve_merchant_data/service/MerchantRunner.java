package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ReadScriptJsonException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ScriptLastLineIsBlankException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ScriptLastLineIsNullException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.ScriptLastLineIsStdErrorException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.repository.MerchantRepository;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.utils.ProcessExecutionAPI30;
import dev.franke.felipee.braspag_automator_v2.contracts.service.AutomationRunner;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MerchantRunner implements AutomationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(MerchantRunner.class);

    private final MerchantRepository repository;
    private final ProcessExecutionAPI30 processExecution;
    private final ObjectMapper objectMapper;
    private final AutomationFileHandler fileHandler;

    public MerchantRunner(
            MerchantRepository repository,
            ProcessExecutionAPI30 processExecution,
            ObjectMapper objectMapper,
            AutomationFileHandler fileHandler) {
        this.repository = repository;
        this.processExecution = processExecution;
        this.objectMapper = objectMapper;
        this.fileHandler = fileHandler;
    }

    @Override
    public Optional<Merchant> singleEcRoutine(String ecNumber) {
        LOG.info("[{}] Starting Automation Routine", ecNumber);
        if (repository.existsByEc(ecNumber)) {
            LOG.warn("[{}] A task with this number was executed already", ecNumber);
            return Optional.empty();
        }
        return fetchMerchantInfo(ecNumber);
    }

    private Optional<Merchant> fetchMerchantInfo(String ec) {
        Merchant merchant;
        try {
            merchant = getMerchantDataFromFile(ec);
            LOG.info("[{}] Data was retrieved", ec);
            fileHandler.deleteJsonFileAfterAutomation(ec);
            return Optional.of(merchant);
        } catch (ScriptLastLineIsBlankException
                | ReadScriptJsonException
                | ScriptLastLineIsStdErrorException
                | ScriptLastLineIsNullException execException) {
            LOG.error("[{}] There was a error during execution", ec, execException);
            return Optional.empty();
        }
    }

    private void ensureLastLineValid(String ec) throws IOException {
        String lastLine = processExecution.run(ec);

        if (lastLine == null) {
            LOG.warn("[{}] Last line is null!", ec);
            throw new ScriptLastLineIsNullException("Ultima linha do script e nula");
        }
        if (lastLine.isBlank()) {
            LOG.warn("[{}] Last line is blank!", ec);
            throw new ScriptLastLineIsBlankException("Ultima linha do script nao possui conteudo");
        }
        if (!lastLine.equalsIgnoreCase("Result is in JSON file")) {
            LOG.warn("[{}] Last line was: {}", ec, lastLine);
            throw new ScriptLastLineIsStdErrorException("erro std na execucao: " + lastLine);
        }
    }

    private Merchant getMerchantDataFromFile(String ec)
            throws ScriptLastLineIsNullException, ScriptLastLineIsBlankException, ReadScriptJsonException {
        try {
            ensureLastLineValid(ec);
            LOG.info("[{}] Last line is valid, proceeding to read JSON file", ec);
            return objectMapper.readValue(new File(ec + ".json"), Merchant.class);
        } catch (IOException ioException) {
            String message =
                    "Error while trying to read from file '" + ec + ".json' - Message: " + ioException.getMessage();
            throw new ReadScriptJsonException(message);
        }
    }
}
