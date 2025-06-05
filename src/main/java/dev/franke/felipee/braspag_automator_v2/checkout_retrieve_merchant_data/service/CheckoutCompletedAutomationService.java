package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.dto.CompletedAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.dto.CompletedAutomationOutputForExcel;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.model.CheckoutCompletedAutomation;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.repository.CheckoutCompletedAutomationRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckoutCompletedAutomationService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutCompletedAutomationService.class);

    private final CheckoutCompletedAutomationRepository automationRepository;

    public CheckoutCompletedAutomationService(final CheckoutCompletedAutomationRepository automationRepository) {
        this.automationRepository = automationRepository;
    }

    public void save(final CheckoutCompletedAutomation automation) {
        try {
            LOG.info("Attempting to save result to DB");
            automationRepository.save(automation);
        } catch (final Exception exception) {
            LOG.error("Could not save result to DB", exception);
        }
    }

    public List<CompletedAutomationOutputForExcel> outputForExcel() {
        LOG.info("Output for Excel called");
        final List<CheckoutCompletedAutomation> originalList = automationRepository.findAll();
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
        final List<CheckoutCompletedAutomation> originalList = automationRepository.findAll();
        return originalList.stream()
                .map(original -> {
                    return new CompletedAutomationOutput(original.getEc(), original.isBlocked());
                })
                .toList();
    }
}
