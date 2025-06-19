package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.failed.FailedAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.FailedScriptRecord;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.repository.FailedScriptRepository;
import dev.franke.felipee.braspag_automator_v2.contracts.service.EcSearchFailedMainService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FailedScriptService implements EcSearchFailedMainService {

    private static final Logger LOG = LoggerFactory.getLogger(FailedScriptService.class);

    private final FailedScriptRepository failedScriptRepository;
    private final MerchantService merchantService;

    public FailedScriptService(FailedScriptRepository failedScriptRepository, MerchantService merchantService) {
        this.failedScriptRepository = failedScriptRepository;
        this.merchantService = merchantService;
    }

    @Override
    public void save(String ecNumber, String message) {
        if (ecNumber == null || ecNumber.isBlank()) {
            LOG.warn("EC Number is null or blank, not saving record");
            return;
        }

        if (message == null || message.isBlank()) {
            LOG.warn("Message is null or blank, not saving record");
            return;
        }

        if (merchantService.existsByEc(ecNumber)) {
            LOG.warn("[{}] Already registered in the completed automations. Not Saving record", ecNumber);
            return;
        }

        if (existsByEcNumber(ecNumber)) {
            LOG.warn("[{}] Already registered. Not Saving record", ecNumber);
            return;
        }

        var record = new FailedScriptRecord(ecNumber, message);
        LOG.info("[{}] Saving failed script record for EC", ecNumber);

        try {
            failedScriptRepository.save(record);
            LOG.info("[{}] Saved failed script record.", ecNumber);
        } catch (Exception e) {
            LOG.error("[{}] Failed to save record: {}", ecNumber, e.getMessage());
        }
    }

    @Override
    public List<FailedAutomationOutput> jsonOutput() {
        return findAll().stream()
                .map(result -> new FailedAutomationOutput(
                        result.getId(), result.getEcNumber(), result.getMessage(), result.getRecordTimestamp()))
                .toList();
    }

    @Override
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
