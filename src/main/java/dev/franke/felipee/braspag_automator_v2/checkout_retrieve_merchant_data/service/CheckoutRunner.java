package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.model.CheckoutCompletedAutomation;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.repository.CheckoutCompletedAutomationRepository;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.utils.ProcessExecutionCheckoutData;
import dev.franke.felipee.braspag_automator_v2.contracts.service.AutomationRunner;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckoutRunner implements AutomationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutRunner.class);

    private final CheckoutFileHandler fileHandler;
    private final ProcessExecutionCheckoutData processExecutionCheckoutData;
    private final CheckoutCompletedAutomationRepository repository;

    public CheckoutRunner(
            CheckoutFileHandler fileHandler,
            ProcessExecutionCheckoutData processExecutionCheckoutData,
            CheckoutCompletedAutomationRepository repository) {
        this.fileHandler = fileHandler;
        this.processExecutionCheckoutData = processExecutionCheckoutData;
        this.repository = repository;
    }

    @Override
    public Optional<CheckoutCompletedAutomation> singleEcRoutine(String ecNumber) {
        LOG.info("[{}] Starting Automation Routine", ecNumber);
        if (repository.existsByEc(ecNumber)) {
            LOG.warn("[{}] A task with this number was executed already", ecNumber);
            return Optional.empty();
        }
        LOG.info("[{}] Automation was not executed before. Continuing", ecNumber);
        LOG.info("[{}] Calling singleEcRun to get result in a byte", ecNumber);
        byte result = singleEcRun(ecNumber);
        if (result == 0) {
            LOG.info("[{}] Executed successfully", ecNumber);
            var data = fileHandler.getMerchantDataFromFile(ecNumber);
            fileHandler.deleteJsonFileAfterAutomation(ecNumber);
            return data;
        }
        LOG.warn("[{}] Some problem occured", ecNumber);
        return Optional.empty();
    }

    private byte singleEcRun(final String ecNumber) {
        byte finalResult;
        try {
            String lastLine = processExecutionCheckoutData.run(ecNumber);
            if (lastLine.equals("Merchant Wrote")) {
                finalResult = 0;
            } else {
                LOG.error("[{}] Error processing", ecNumber);
                finalResult = -1;
            }
        } catch (IOException ioException) {
            LOG.error("[{}] Error running process", ecNumber, ioException);
            finalResult = -1;
        }
        return finalResult;
    }
}
