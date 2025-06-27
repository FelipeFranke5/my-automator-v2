package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.sqs;

import dev.franke.felipe.api30_automation_api.automation.merchant_data.domain.CieloMerchant;
import dev.franke.felipe.api30_automation_api.automation.merchant_data.domain.EstablishmentCodeImpl;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.FailedScriptService;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantRunner;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class API30Messaging {

    private static final String INITIAL_QUEUE_NAME = "api30-init";

    private final MerchantRunner merchantRunner;
    private final MerchantService merchantService;
    private final FailedScriptService failedScriptService;

    public API30Messaging(
            MerchantRunner merchantRunner, MerchantService merchantService, FailedScriptService failedScriptService) {
        this.merchantRunner = merchantRunner;
        this.merchantService = merchantService;
        this.failedScriptService = failedScriptService;
    }

    @SqsListener(INITIAL_QUEUE_NAME)
    public void api30Listener(String ecNumber) {
        try {
            String establishmentCode = validateAndReturnBackEcNumber(ecNumber);
            Merchant merchant = getMerchant(getCieloMerchant(establishmentCode));
            saveMerchantWhenSuccess(merchant);
        } catch (Exception exception) {
            saveFailedAutomation(new FailedAutomationSqsPayload(ecNumber, exception));
        }
    }

    public void saveMerchantWhenSuccess(Merchant merchant) {
        merchantService.save(merchant);
    }

    public void saveFailedAutomation(FailedAutomationSqsPayload payload) {
        failedScriptService.save(
                payload.establishmentCode(), payload.exception().getMessage());
    }

    private Merchant getMerchant(CieloMerchant cieloMerchant) {
        return new Merchant(
                UUID.randomUUID(),
                cieloMerchant.establishmentCode(),
                cieloMerchant.merchantId(),
                cieloMerchant.documentType(),
                cieloMerchant.documentNumber(),
                cieloMerchant.name(),
                cieloMerchant.blocked(),
                cieloMerchant.pixEnabled(),
                cieloMerchant.antifraudEnabled(),
                cieloMerchant.tokenizationEnabled(),
                cieloMerchant.velocityEnabled(),
                cieloMerchant.smartRecurrencyEnabled(),
                cieloMerchant.zeroAuthEnabled(),
                cieloMerchant.binQueryEnabled(),
                cieloMerchant.selectiveAuthEnabled(),
                cieloMerchant.automaticCancelationEnabled(),
                cieloMerchant.forceBraspagAuthEnabled(),
                cieloMerchant.mtlsEnabled(),
                cieloMerchant.webhookEnabled(),
                cieloMerchant.whiteListIpCount(),
                LocalDateTime.now());
    }

    private CieloMerchant getCieloMerchant(String ecNumber) {
        return merchantRunner.singleEcRoutine(ecNumber);
    }

    private String validateAndReturnBackEcNumber(String ecNumber) {
        EstablishmentCodeImpl establishmentCode = new EstablishmentCodeImpl(ecNumber);
        return establishmentCode.establishmentNumber();
    }
}
