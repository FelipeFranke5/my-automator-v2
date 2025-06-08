package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.successful.SuccessfulAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.repository.MerchantRepository;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

    private static final Logger LOG = LoggerFactory.getLogger(MerchantService.class);

    private final MerchantRepository merchantRepository;
    private final MerchantRunner runner;

    public MerchantService(MerchantRepository merchantRepository, MerchantRunner runner) {
        this.merchantRepository = merchantRepository;
        this.runner = runner;
    }

    public List<SuccessfulAutomationOutput> jsonOutput() {
        return merchantRepository.findAll().stream()
                .map(merchant -> new SuccessfulAutomationOutput(
                        merchant.getRecordId(), merchant.getEc(), merchant.getRecordTimestamp()))
                .toList();
    }

    public void clearAllMerchants() {
        LOG.info("Clearing all merchants from database");
        merchantRepository.deleteAll();
    }

    public void saveMerchantToDatabase(Merchant merchant) {
        LOG.info("Attempting to save record");
        try {
            merchantRepository.save(merchant);
            LOG.info("Record saved");
        } catch (Exception exception) {
            LOG.error("Error during save", exception);
        }
    }

    public boolean existsByEc(String ec) {
        return merchantRepository.existsByEc(ec);
    }

    public void runAutomation(String merchants) {
        runner.runAutomation(merchants);
    }
}
