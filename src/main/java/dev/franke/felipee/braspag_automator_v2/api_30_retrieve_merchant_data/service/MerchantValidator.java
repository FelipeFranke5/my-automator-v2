package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MerchantValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MerchantValidator.class);
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    private boolean singleEcIsValid(String merchantOrMerchantList) {
        if (merchantOrMerchantList.contains(",")) {
            LOG.warn("[{}] EC contains the character ',' - Returning false", merchantOrMerchantList);
            return false;
        }

        if (merchantOrMerchantList.length() != 10) {
            LOG.warn("[{}] EC length does not equal 10 - Returning false", merchantOrMerchantList);
            return false;
        }

        try {
            LOG.info("[{}] Attempting to check if EC is numeric and greater than zero", merchantOrMerchantList);
            return Long.parseLong(merchantOrMerchantList) > 0;
        } catch (NumberFormatException numberFormatException) {
            LOG.warn("[{}] Either the EC is not numeric or the length is invalid", merchantOrMerchantList);
            return false;
        } finally {
            LOG.info("[{}] Validation has completed", merchantOrMerchantList);
        }
    }

    public boolean validEmail(String address) {
        return address != null && !address.isBlank() && PATTERN.matcher(address).matches();
    }

    public boolean merchantArrayIsValid(String[] ecsArray) {
        for (String ecNumber : ecsArray) {
            if (!singleEcIsValid(ecNumber)) return false;
        }
        return true;
    }
}
