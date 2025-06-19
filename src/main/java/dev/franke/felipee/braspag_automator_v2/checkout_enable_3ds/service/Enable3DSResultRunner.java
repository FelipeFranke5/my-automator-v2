package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.repository.Enable3DSResultRepository;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.utils.ProcessExecutionEnable3DS;
import dev.franke.felipee.braspag_automator_v2.contracts.service.AutomationRunner;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Enable3DSResultRunner implements AutomationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(Enable3DSResultRunner.class);

    private static final String SUCESS_MESSAGE = "3DS Habilitado";
    private static final String GENERIC_ERROR_MESSAGE = "Falha na Execucao";
    private static final String ALREADY_ENABLED_MESSAGE = "3DS ja esta habilitado";

    private final Enable3DSResultRepository repository;
    private final CheckoutMerchantValidator checkoutMerchantValidator;
    private final ProcessExecutionEnable3DS processExecutionEnable3DS;

    public Enable3DSResultRunner(
            Enable3DSResultRepository repository,
            CheckoutMerchantValidator checkoutMerchantValidator,
            ProcessExecutionEnable3DS processExecutionEnable3DS) {
        this.repository = repository;
        this.checkoutMerchantValidator = checkoutMerchantValidator;
        this.processExecutionEnable3DS = processExecutionEnable3DS;
    }

    @Override
    public Optional<String> singleEcRoutine(String ecNumber) {
        LOG.info("[{}] Starting Automation Routine", ecNumber);
        if (repository.existsByEc(ecNumber)) {
            LOG.warn("[{}] A task with this number was executed already", ecNumber);
            return Optional.empty();
        }
        LOG.info("[{}] Automation was not executed before. Continuing", ecNumber);
        byte automationResult = singleEcAutomation(ecNumber);
        if (automationResult == 0) {
            LOG.info("[{}] Executed successfully", ecNumber);
            return Optional.of("Success");
        }
        LOG.warn("[{}] Some problem occured", ecNumber);
        return Optional.of("Fail");
    }

    private byte singleEcAutomation(String ec) {
        String message = getResultMessageFromExecutionLastLine(getLastLine(ec));
        if (message.equals(SUCESS_MESSAGE) || message.equals(ALREADY_ENABLED_MESSAGE)) {
            return 0;
        }
        return -1;
    }

    private String getResultMessageFromExecutionLastLine(final byte result) {
        return switch (result) {
            case 0 -> SUCESS_MESSAGE;
            case 1 -> ALREADY_ENABLED_MESSAGE;
            default -> GENERIC_ERROR_MESSAGE;
        };
    }

    private byte getLastLine(final String ecNumber) {
        final String[] singleEcArray = {ecNumber};

        // Not valid == just return -1
        if (!(checkoutMerchantValidator.allEcsAreValid(singleEcArray))) return -1;

        LOG.info("[{}] Attempting to get last line", ecNumber);
        String lastLine;

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
