package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.successful.SuccessfulAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.repository.MerchantRepository;
import java.util.*;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

    private static final Logger LOG = LoggerFactory.getLogger(MerchantService.class);

    @Autowired
    private MerchantRepository merchantRepository;

    public void clear() {
        LOG.info("Clearing all merchants from database");
        merchantRepository.deleteAll();
    }

    public void save(Object data) {
        var merchant = (Merchant) data;
        LOG.info("[{}] Attempting to save record", merchant.getEstablishmentCode());
        try {
            merchantRepository.save(merchant);
            LOG.info("[{}] Record saved", merchant.getEstablishmentCode());
        } catch (Exception exception) {
            LOG.error("[{}] Error during save", merchant.getEstablishmentCode(), exception);
        }
    }

    public List<SuccessfulAutomationOutput> jsonOutput() {
        return getEstablishmentStream(ProductFilterType.NO_FILTER)
                .map(merchant -> new SuccessfulAutomationOutput(
                        merchant.getId(),
                        merchant.getEstablishmentCode(),
                        merchant.getName(),
                        merchant.isBlocked(),
                        merchant.getRecordTimestamp()))
                .toList();
    }

    public String getTextResult(ProductFilterType filterType) {
        return switch (filterType) {
            case NO_FILTER -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.NO_FILTER)));
            case MERCHANT_BLOCKED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.MERCHANT_BLOCKED)));
            case PIX_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.PIX_ENABLED)));
            case ANTIFRAUD_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.ANTIFRAUD_ENABLED)));
            case TOKENIZATION_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.TOKENIZATION_ENABLED)));
            case VELOCITY_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.VELOCITY_ENABLED)));
            case SMART_RECURRENCY_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.SMART_RECURRENCY_ENABLED)));
            case ZERO_AUTH_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.ZERO_AUTH_ENABLED)));
            case BIN_QUERY_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.BIN_QUERY_ENABLED)));
            case SELECTIVE_AUTH_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.SELECTIVE_AUTH_ENABLED)));
            case AUTOMATIC_CANCELLATION_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.AUTOMATIC_CANCELLATION_ENABLED)));
            case FORCE_BRASPAG_AUTH_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.FORCE_BRASPAG_AUTH_ENABLED)));
            case MTLS_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.MTLS_ENABLED)));
            case WEBHOOK_ENABLED -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.WEBHOOK_ENABLED)));
            case AT_LEAST_ONE_IP -> establishmentCodesInString(
                    establishmentCodes(getEstablishmentStream(ProductFilterType.AT_LEAST_ONE_IP)));
        };
    }

    private Stream<Merchant> merchantStream() {
        return merchantRepository.findAll().stream();
    }

    private String establishmentCodesInString(List<String> establishmentCodes) {
        return establishmentCodes.stream().reduce("EC's: \n", (a, b) -> a + "\n" + b + "\n");
    }

    private List<String> establishmentCodes(Stream<Merchant> merchantStream) {
        return merchantStream.map(Merchant::getEstablishmentCode).toList();
    }

    private Stream<Merchant> getEstablishmentStream(ProductFilterType filterType) {
        return switch (filterType) {
            case NO_FILTER -> merchantStream();
            case MERCHANT_BLOCKED -> merchantStream().filter(Merchant::isBlocked);
            case PIX_ENABLED -> merchantStream().filter(Merchant::isPixEnabled);
            case ANTIFRAUD_ENABLED -> merchantStream().filter(Merchant::isAntifraudEnabled);
            case TOKENIZATION_ENABLED -> merchantStream().filter(Merchant::isTokenizationEnabled);
            case VELOCITY_ENABLED -> merchantStream().filter(Merchant::isVelocityEnabled);
            case SMART_RECURRENCY_ENABLED -> merchantStream().filter(Merchant::isSmartRecurrencyEnabled);
            case ZERO_AUTH_ENABLED -> merchantStream().filter(Merchant::isZeroAuthEnabled);
            case BIN_QUERY_ENABLED -> merchantStream().filter(Merchant::isBinQueryEnabled);
            case SELECTIVE_AUTH_ENABLED -> merchantStream().filter(Merchant::isSelectiveAuthEnabled);
            case AUTOMATIC_CANCELLATION_ENABLED -> merchantStream().filter(Merchant::isAutomaticCancelationEnabled);
            case FORCE_BRASPAG_AUTH_ENABLED -> merchantStream().filter(Merchant::isForceBraspagAuthEnabled);
            case MTLS_ENABLED -> merchantStream().filter(Merchant::isMtlsEnabled);
            case WEBHOOK_ENABLED -> merchantStream().filter(Merchant::isWebhookEnabled);
            case AT_LEAST_ONE_IP -> merchantStream().filter(merchant -> merchant.getWhiteListIpCount() > 1);
        };
    }

    public boolean existsByEc(String ec) {
        return merchantRepository.existsByEstablishmentCode(ec);
    }
}
