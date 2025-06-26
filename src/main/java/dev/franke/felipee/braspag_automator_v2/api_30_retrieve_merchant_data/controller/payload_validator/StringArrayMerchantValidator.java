package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.payload_validator;

import dev.franke.felipe.api30_automation_api.automation.merchant_data.domain.EstablishmentCodeImpl;
import org.springframework.stereotype.Component;

@Component
public class StringArrayMerchantValidator {

    public void validate(String[] merchantArray) {
        for (String establishment : merchantArray) {
            validateSingleEstablishmentCode(establishment);
        }
    }

    private void validateSingleEstablishmentCode(String establishmentCode) {
        new EstablishmentCodeImpl(establishmentCode);
    }
}
