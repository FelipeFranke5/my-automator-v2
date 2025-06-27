package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.failed.FailedAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.AlreadySavedException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.InvalidEstablishmentCodeException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.InvalidMessageException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.FailedScriptRecord;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.repository.FailedScriptRepository;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FailedScriptService {

    private static final Logger LOG = LoggerFactory.getLogger(FailedScriptService.class);

    @Autowired
    private FailedScriptRepository failedScriptRepository;

    @Autowired
    private MerchantService merchantService;

    public void save(String ecNumber, String message) {
        assertEcNumberIsNotNullOrBlank(ecNumber);
        assertMessageIsNotNullOrBlank(message);
        message = resizeMessageIfTooLong(message);
        assertEcIsNotAlreadySaved(ecNumber);
        try {
            var record = new FailedScriptRecord(ecNumber, message);
            LOG.info("[{}] Saving failed script record for EC", ecNumber);
            failedScriptRepository.save(record);
            LOG.info("[{}] Saved failed script record.", ecNumber);
        } catch (Exception e) {
            LOG.error("[{}] Failed to save record: {}", ecNumber, e.getMessage());
        }
    }

    public List<FailedAutomationOutput> jsonOutput() {
        return getStream()
                .map(result -> new FailedAutomationOutput(
                        result.getId(), result.getEcNumber(), result.getMessage(), result.getRecordTimestamp()))
                .toList();
    }

    public String getOutputInText() {
        return getUnifiedString();
    }

    public void deleteAll() {
        failedScriptRepository.deleteAll();
    }

    private Stream<FailedScriptRecord> getStream() {
        return findAll().stream();
    }

    private Stream<String> getStringStream() {
        return getStream().map(result -> result.getEcNumber() + " - " + result.getMessage());
    }

    private String getUnifiedString() {
        return getStringStream().reduce("EC's:\n", (a, b) -> a + "\n" + b + "\n");
    }

    private List<FailedScriptRecord> findAll() {
        return failedScriptRepository.findAll();
    }

    private String resizeMessageIfTooLong(String message) {
        if (message.length() > 254) {
            return message.substring(0, 253);
        }
        return message;
    }

    private void assertEcNumberIsNotNullOrBlank(String ecNumber) {
        if (ecNumber == null || ecNumber.isBlank()) {
            LOG.warn("EC Number is null or blank, not saving record");
            throw new InvalidEstablishmentCodeException("EC number is null or blank");
        }
    }

    private void assertMessageIsNotNullOrBlank(String message) {
        if (message == null || message.isBlank()) {
            LOG.warn("Message is null or blank, not saving record");
            throw new InvalidMessageException("Message is null or blank");
        }
    }

    private void assertEcIsNotAlreadySaved(String ecNumber) {
        if (merchantService.existsByEc(ecNumber)) {
            LOG.warn("[{}] Already registered. Not Saving record", ecNumber);
            throw new AlreadySavedException("Merchant already saved");
        }
    }
}
