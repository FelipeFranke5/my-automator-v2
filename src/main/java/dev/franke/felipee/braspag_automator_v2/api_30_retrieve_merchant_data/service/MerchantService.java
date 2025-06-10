package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.successful.SuccessfulAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.repository.MerchantRepository;
import dev.franke.felipee.braspag_automator_v2.contracts.service.EcSearchMainService;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MerchantService implements EcSearchMainService {

    private static final Logger LOG = LoggerFactory.getLogger(MerchantService.class);

    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public List<Merchant> findAll() {
        return merchantRepository.findAll();
    }

    @Override
    public List<SuccessfulAutomationOutput> jsonOutput() {
        return merchantRepository.findAll().stream()
                .map(merchant -> new SuccessfulAutomationOutput(
                        merchant.getRecordId(), merchant.getEc(), merchant.getRecordTimestamp()))
                .toList();
    }

    @Override
    public void clear() {
        LOG.info("Clearing all merchants from database");
        merchantRepository.deleteAll();
    }

    @Override
    public void save(Object merchant) {
        var data = (Merchant) merchant;
        LOG.info("Attempting to save record");
        try {
            merchantRepository.save(data);
            LOG.info("Record saved");
        } catch (Exception exception) {
            LOG.error("Error during save", exception);
        }
    }

    public boolean existsByEc(String ec) {
        return merchantRepository.existsByEc(ec);
    }
}
