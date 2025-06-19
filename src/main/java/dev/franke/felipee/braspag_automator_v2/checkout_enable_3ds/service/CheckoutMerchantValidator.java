package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckoutMerchantValidator {
    private static final Logger LOG = LoggerFactory.getLogger(CheckoutMerchantValidator.class);

    private boolean singleEcIsValid(final String merchantEcNumber) {
        LOG.info("------------------------------------------");

        if (merchantEcNumber == null) {
            LOG.warn("[null] EC is null");
            return false;
        }

        if (merchantEcNumber.isBlank()) {
            LOG.warn("[] EC is blank");
            return false;
        }

        if (merchantEcNumber.contains(",")) {
            LOG.warn("[{}] EC contains the character ',' - Returning false", merchantEcNumber);
            return false;
        }

        if (merchantEcNumber.length() != 10) {
            LOG.warn("[{}] EC length does not equal 10 - Returning false", merchantEcNumber);
            return false;
        }

        try {
            LOG.info("[{}] Attempting to check if EC is numeric and greater than zero", merchantEcNumber);
            final boolean validationResult = Long.parseLong(merchantEcNumber) > 0;
            LOG.info(
                    "[{}] Valid (Numeric and greater than zero): {}",
                    merchantEcNumber,
                    validationResult ? "Valid" : "Invalid");
            return validationResult;
        } catch (final NumberFormatException numberFormatException) {
            LOG.warn("[{}] Either the EC is not numeric or the length is invalid", merchantEcNumber);
            return false;
        } finally {
            LOG.info("[{}] Validation has completed", merchantEcNumber);
        }
    }

    public boolean ecIsValid(String ecNumber) {
        return singleEcIsValid(ecNumber);
    }

    public boolean allEcsAreValid(final String[] ecs) {
        for (final String ecNumber : ecs) {
            if (!(singleEcIsValid(ecNumber))) return false;
        }
        return true;
    }
}
