package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.utils.ProcessExecutionEnable3DS;
import dev.franke.felipee.braspag_automator_v2.contracts.service.AutomationRunner;
import java.io.IOException;
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
public class Enable3DSResultRunner implements AutomationRunner {

    // Generic function to convert array to set
    public static <T> Set<T> convertArrayToSet(final T[] array) {
        return Arrays.stream(array).collect(Collectors.toSet());
    }

    private static final Logger LOG = LoggerFactory.getLogger(Enable3DSResultRunner.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    private static final String SUCESS_MESSAGE = "3DS Habilitado";
    private static final String GENERIC_ERROR_MESSAGE = "Falha na Execucao";
    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenciais Invalidas";
    private static final String EC_NOT_FOUND_MESSAGE = "EC nao localizado na base checkout";
    private static final String BRASPAG_INTERNAL_ERROR_MESSAGE = "Braspag Intermitente";
    private static final String INVALID_PARAMETERS_MESSAGE = "Parametros Invalidos";
    private static final String MERCHANT_IS_BLOCKED_MESSAGE = "Loja Bloqueada";
    private static final String ALREADY_ENABLED_MESSAGE = "3DS ja esta habilitado";

    private final Enable3DSResultService enable3dsResultService;
    private final Enable3DSFailService enable3DSFailService;
    private final CheckoutMerchantValidator checkoutMerchantValidator;
    private final ProcessExecutionEnable3DS processExecutionEnable3DS;

    public Enable3DSResultRunner(
            Enable3DSResultService enable3dsResultService,
            Enable3DSFailService enable3DSFailService,
            CheckoutMerchantValidator checkoutMerchantValidator,
            ProcessExecutionEnable3DS processExecutionEnable3DS) {
        this.enable3dsResultService = enable3dsResultService;
        this.enable3DSFailService = enable3DSFailService;
        this.checkoutMerchantValidator = checkoutMerchantValidator;
        this.processExecutionEnable3DS = processExecutionEnable3DS;
    }

    @Override
    public void run(final String[] ecs) {
        LOG.info("Initilizing..");

        if (!(checkoutMerchantValidator.allEcsAreValid(ecs))) {
            LOG.warn("EC list is not valid..");
            return;
        }

        LOG.info("List is valid");
        CompletableFuture.runAsync(() -> runAllAutomations(ecs), executor);
        LOG.info("Ran");
    }

    private void runAllAutomations(String[] ecArray) {
        var ecs = getEcNumberSet(ecArray);
        ecs.forEach(this::singleEcAutomation);
    }

    private Set<String> getEcNumberSet(String[] ecNumbers) {
        return convertArrayToSet(ecNumbers);
    }

    private void singleEcAutomation(final String ec) {
        if (enable3dsResultService.existsByEc(ec)) {
            LOG.warn("[{}] Automation is not going to be executed", ec);
            LOG.warn("[{}] Already executed this task", ec);
            return;
        }

        LOG.info("[{}] Automation is going to be executed", ec);
        final String message = getResultMessageFromExecutionLastLine(getLastLine(ec));

        if (message.equals(SUCESS_MESSAGE) || message.equals(ALREADY_ENABLED_MESSAGE)) {
            enable3dsResultService.save(ec, message);
        } else {
            enable3DSFailService.save(ec, message);
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
                    "Invalid ec" -> 4;
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
