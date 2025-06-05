package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.dto.CompletedAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.dto.CompletedAutomationOutputForExcel;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.model.CheckoutCompletedAutomation;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.repository.CheckoutCompletedAutomationRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckoutCompletedAutomationService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutCompletedAutomationService.class);

    private final CheckoutCompletedAutomationRepository repository;

    public CheckoutCompletedAutomationService(CheckoutCompletedAutomationRepository repository) {
        this.repository = repository;
    }

    private boolean ecIsValid(String ec) {
        if (ec == null) return false;
        if (ec.isBlank()) return false;
        if (ec.length() != 10) return false;

        Optional<CheckoutCompletedAutomation> failOptional = repository.findByEc(ec);

        if (failOptional.isPresent()) return false;

        try {
            return Long.parseLong(ec) > 0;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }

    public void save(CheckoutCompletedAutomation automation) {
        if (ecIsValid(automation.getEc())) {
            try {
                LOG.info("Attempting to save result to DB");
                repository.save(automation);
            } catch (final Exception exception) {
                LOG.error("Could not save result to DB", exception);
            }
        } else {
            LOG.warn("Automation is not being saved because EC is not valid ..");
        }
    }

    public List<CompletedAutomationOutputForExcel> outputForExcel() {
        LOG.info("Output for Excel called");
        final List<CheckoutCompletedAutomation> originalList = repository.findAll();
        return originalList.stream()
                .map(original -> new CompletedAutomationOutputForExcel(
                        original.getRecordId(),
                        original.getEc(),
                        original.getAlias(),
                        original.getName(),
                        original.isBlocked(),
                        original.isTestModeEnabled(),
                        original.isInternationalPaymentEnabled(),
                        original.getNotificationUrl(),
                        original.getReturnUrl(),
                        original.getStatusChangeUrl(),
                        original.isThreeDSEnabled(),
                        original.getAmexMid(),
                        original.isFacialAuthEnabled(),
                        original.getRecordTimestamp()))
                .toList();
    }

    public List<CompletedAutomationOutput> outputForJson() {
        LOG.info("Output for Json called");
        final List<CheckoutCompletedAutomation> originalList = repository.findAll();
        return originalList.stream()
                .map(original -> new CompletedAutomationOutput(original.getEc(), original.isBlocked()))
                .toList();
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
