package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.FailedScriptRecord;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.repository.FailedScriptRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FailedScriptService {

  private static final Logger LOG = LoggerFactory.getLogger(FailedScriptService.class);

  private final FailedScriptRepository failedScriptRepository;

  public FailedScriptService(FailedScriptRepository failedScriptRepository) {
    this.failedScriptRepository = failedScriptRepository;
  }

  public void save(String ecNumber, String message) {
    if (ecNumber == null || ecNumber.isBlank()) {
      LOG.warn("EC Number is null or blank, not saving record.");
      return;
    }

    if (message == null || message.isBlank()) {
      LOG.warn("Message is null or blank, not saving record.");
      return;
    }

    var record = new FailedScriptRecord(ecNumber, message);
    LOG.info("Saving failed script record for EC Number: {}, Message: {}", ecNumber, message);

    try {
      failedScriptRepository.save(record);
    } catch (Exception e) {
      LOG.error("Failed to save record: {}", e.getMessage());
    }
  }

  public List<FailedScriptRecord> findAll() {
    LOG.info("Retrieving all failed script records.");
    return failedScriptRepository.findAll();
  }
}
