package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.model.CheckoutFailedAutomation;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.repository.CheckoutFailedAutomationRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckoutFailedAutomationService {

  private static final Logger LOG = LoggerFactory.getLogger(CheckoutFailedAutomationService.class);

  private final CheckoutFailedAutomationRepository failedAutomationRepository;

  public CheckoutFailedAutomationService(
      final CheckoutFailedAutomationRepository failedAutomationRepository) {
    this.failedAutomationRepository = failedAutomationRepository;
  }

  public void save(final String ecNumber, final String message) {
    try {
      LOG.info("Attempting to save");
      failedAutomationRepository.save(new CheckoutFailedAutomation(ecNumber, message));
      LOG.info("Saved");
    } catch (final Exception exception) {
      LOG.warn("There was an error while trying to save");
      LOG.error("Error while trying to save", exception);
    }
  }

  public List<CheckoutFailedAutomation> getAll() {
    return failedAutomationRepository.findAll();
  }
}
