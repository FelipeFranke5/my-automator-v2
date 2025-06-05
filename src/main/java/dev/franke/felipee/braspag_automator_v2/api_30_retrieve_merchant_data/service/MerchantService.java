package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.AutomationsRunningOutput;
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
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Value("${braspag.prod.login}")
    private String login;

    @Value("${braspag.prod.password}")
    private String password;

    private final ProcessExecutionAPI30 processExecution;
    private final ObjectMapper objectMapper;
    private final AutomationFileHandler fileHandler;
    private final MerchantRepository merchantRepository;
    private final MerchantValidator validator;
    private final FailedScriptService failedScriptService;
    private final EmailSender emailSender;

    public MerchantService(
            ProcessExecutionAPI30 processExecution,
            ObjectMapper objectMapper,
            AutomationFileHandler fileHandler,
            MerchantRepository merchantRepository,
            MerchantValidator validator,
            FailedScriptService failedScriptService,
            EmailSender emailSender) {
        this.processExecution = processExecution;
        this.objectMapper = objectMapper;
        this.fileHandler = fileHandler;
        this.merchantRepository = merchantRepository;
        this.validator = validator;
        this.failedScriptService = failedScriptService;
        this.emailSender = emailSender;
    }

    private Optional<byte[]> writeToExcel(List<Merchant> merchants) {
        try {
            byte[] excelBytes = fileHandler.writeToExcelFile(merchants);
            return Optional.of(excelBytes);
        } catch (IOException ioException) {
            LOG.error("Could not execute routine to write excel", ioException);
            return Optional.empty();
        }
    }

    // In case of success
    private void sendEmail(byte[] excelBytes, String emailAddress) {
        emailSender.sendEmailWithResults(excelBytes, emailAddress);
    }

    // In case of failure
    private void sendEmail(String emailAddress) {
        emailSender.sendEmailInformingFailure(emailAddress);
    }

    public AutomationsRunningOutput numberOfAutomationsRunning() {
        byte automations = processExecution.getNumberOfPythonProcesses();
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

    public byte sendEmailWithExcelResults(String emailAddress) {
        LOG.info("Initializing service - Write to Excel and Send Email");

        // Return 0 -> Task Completed
        // Return 1 -> Email is not valid
        // Return 2 -> Nothing to write

        if (!validator.validEmail(emailAddress)) {
            LOG.warn("Email Address is not valid!");
            return 1;
        }

        List<Merchant> merchants = merchantRepository.findAll();

        if (merchants.isEmpty()) {
            LOG.info("Nothing to write!");
            return 2;
        }

        LOG.info("Attempting to write results to Excel file");
        LOG.info("And then send Email with Excel file");
        LOG.info("Results count: {}", merchants.size());

        handleExcelEmailTask(emailAddress, merchants);

        LOG.info("Async task already started. I am free now to return");
        return 0;
    }

    private void handleExcelEmailTask(String emailAddress, List<Merchant> merchants) {
        CompletableFuture.runAsync(
                () -> {
                    Optional<byte[]> optionalExcelBytes = writeToExcel(merchants);

                    optionalExcelBytes.ifPresentOrElse(
                            excelBytes -> {
                                LOG.info("Result is an optional of byte[]");
                                LOG.info("Sending email with result");
                                sendEmail(excelBytes, emailAddress);
                            },
                            () -> {
                                LOG.warn("Result is an empty optional, which means an error has occured");
                                LOG.warn("Sending email to inform error");
                                sendEmail(emailAddress);
                            });
                },
                executor);
    }

    public List<OutputMerchant> listOfMerchants() {
        LOG.info("Retrieving all merchants from database");
        List<Merchant> merchants = merchantRepository.findAll();
        List<OutputMerchant> output = new ArrayList<>();
        merchants.stream().forEach(merchant -> {
            OutputMerchant outputMerchant =
                    new OutputMerchant(merchant.getRecordId(), merchant.getEc(), merchant.getRecordTimestamp());
            output.add(outputMerchant);
        });
        return output;
    }

    public void clearAllMerchants() {
        LOG.info("Clearing all merchants from database");
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
        LOG.info("Validating input: {}", merchants);
        return validator.validInput(merchants);
    }

    // This should be called second
    public void runAutomation(String merchants) {
        CompletableFuture.runAsync(
                () -> {
                    LOG.info("Starting Automation!");

                    if (!(inputIsValid(merchants))) {
                        LOG.warn("Input is not valid. Returning right now");
                        failedScriptService.save("INVALIDO", "ECs invalidos");
                        return;
                    }

                    if (merchants.length() > 10) {
                        runAutomationForMultipleMerchants(merchants);
                    } else {
                        runAutomationForSingleMerchant(merchants);
                    }
                },
                executor);
    }

    private void runAutomationForSingleMerchant(String ec) {
        LOG.info("Starting Automation process for EC");
        Optional<Merchant> result = getAutomationResult(ec);

        result.ifPresent(res -> {
            LOG.info("Automation Result is present. Saving to database");
            saveMerchantToDatabase(res);
        });
    }

    private Optional<Merchant> getAutomationResult(String ec) {
        LOG.info("Attempting to retrieve data for this merchant");
        if (ec == null || ec.isBlank() || ec.length() != 10) {
            LOG.warn("Not executing automation, because EC is not valid");
            failedScriptService.save(ec, "O numero do EC nao e valido");
            return Optional.empty();
        }
        return fetchMerchantInfo(ec);
    }

    private Optional<Merchant> fetchMerchantInfo(String ec) {
        Merchant merchant;
        try {
            merchant = getMerchantDataFromFile(ec);
            LOG.info("Data was retrieved");
            return Optional.of(merchant);
        } catch (ScriptLastLineIsBlankException
                | ReadScriptJsonException
                | ScriptLastLineIsStdErrorException
                | ScriptLastLineIsNullException execException) {
            LOG.error("There was a error during execution", execException);
            String errorMessage = "Erro durante execucao do script: " + execException.getMessage();
            failedScriptService.save(ec, errorMessage);
            return Optional.empty();
        } finally {
            fileHandler.deleteJsonFileAfterAutomation(ec);
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
}
