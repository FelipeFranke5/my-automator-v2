package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.dto.ResultOutput;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.model.CheckoutFailedAutomation;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.repository.CheckoutFailedAutomationRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckoutFailedAutomationService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutFailedAutomationService.class);

    private final CheckoutFailedAutomationRepository repository;
    private final CheckoutCompletedAutomationService completedAutomationService;

    public CheckoutFailedAutomationService(
            CheckoutFailedAutomationRepository repository,
            CheckoutCompletedAutomationService completedAutomationService) {
        this.repository = repository;
        this.completedAutomationService = completedAutomationService;
    }

    private boolean resultIsValid(String result) {
        if (result == null) return false;
        return !result.isBlank();
    }

    private boolean ecIsValid(String ec) {
        if (ec == null) return false;
        if (ec.isBlank()) return false;
        if (ec.length() != 10) return false;

        Optional<CheckoutFailedAutomation> failOptional = repository.findByEcNumber(ec);

        if (failOptional.isPresent()) return false;

        try {
            return Long.parseLong(ec) > 0 && !completedAutomationService.existsByEc(ec);
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }

    public void save(final String ecNumber, final String message) {
        if (ecIsValid(ecNumber) && resultIsValid(message)) {
            try {
                LOG.info("[{}] Attempting to save", ecNumber);
                repository.save(new CheckoutFailedAutomation(ecNumber, message));
                LOG.info("[{}] Saved", ecNumber);
            } catch (final Exception exception) {
                LOG.warn("[{}] There was an error while trying to save", ecNumber);
                LOG.error("[{}] Error while trying to save", ecNumber, exception);
            }
        } else {
            LOG.warn("Could not save, because EC / Result Message is not valid ..");
        }
    }

    public List<ResultOutput> jsonOutput() {
        return getAll().stream()
                .map(res -> new ResultOutput(res.getEcNumber(), res.getMessage()))
                .toList();
    }

    private List<CheckoutFailedAutomation> getAll() {
        return repository.findAll();
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
