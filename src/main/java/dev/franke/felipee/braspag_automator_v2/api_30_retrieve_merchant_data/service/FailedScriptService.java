package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.failed.FailedAutomationOutput;
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
    private final MerchantService merchantService;

    public FailedScriptService(FailedScriptRepository failedScriptRepository, MerchantService merchantService) {
        this.failedScriptRepository = failedScriptRepository;
        this.merchantService = merchantService;
    }

    public void save(String ecNumber, String message) {
        if (merchantService.existsByEc(ecNumber)) {
            LOG.warn("Already registered in the completed automations. Not Saving record");
            return;
        }

        if (existsByEcNumber(ecNumber)) {
            LOG.warn("Already registered. Not Saving record");
            return;
        }

        if (ecNumber == null || ecNumber.isBlank()) {
            LOG.warn("EC Number is null or blank, not saving record");
            return;
        }

        if (message == null || message.isBlank()) {
            LOG.warn("Message is null or blank, not saving record");
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

    public List<FailedAutomationOutput> jsonOutput() {
        return findAll().stream()
                .map(result -> new FailedAutomationOutput(
                        result.getId(), result.getEcNumber(), result.getMessage(), result.getRecordTimestamp()))
                .toList();
    }

    public void deleteAll() {
        failedScriptRepository.deleteAll();
    }

    public boolean existsByEcNumber(String ecNumber) {
        return failedScriptRepository.existsByEcNumber(ecNumber);
    }

    private List<FailedScriptRecord> findAll() {
        return failedScriptRepository.findAll();
    }
}
