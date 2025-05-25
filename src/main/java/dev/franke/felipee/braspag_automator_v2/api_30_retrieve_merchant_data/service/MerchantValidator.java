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

  private boolean inputNullOrBlank(String merchantOrMerchantList) {
    return merchantOrMerchantList == null || merchantOrMerchantList.isBlank();
  }

  private boolean singleEcIsValid(String merchantOrMerchantList) {
    if (merchantOrMerchantList.contains(",")) {
      LOG.warn("EC contains the character ',' - Returning false");
      return false;
    }

    if (merchantOrMerchantList.length() != 10) {
      LOG.warn("EC length does not equal 10 - Returning false");
      return false;
    }

    try {
      LOG.info("Attempting to check if EC is numeric and greater than zero");
      boolean validationResult = Long.parseLong(merchantOrMerchantList) > 0;
      LOG.info("Valid (Numeric and greater than zero): {}", validationResult ? "Valid" : "Invalid");
      return validationResult;
    } catch (NumberFormatException numberFormatException) {
      LOG.warn("Either the EC is not numeric or the length is invalid");
      return false;
    } finally {
      LOG.info("Validation has completed");
    }
  }

  private boolean multipleEcsAreValid(String merchantOrMerchantList) {
    if (!(merchantOrMerchantList.contains(","))) return false;
    String[] merchantsArray = merchantOrMerchantList.split(",");

    for (String merchant : merchantsArray) {
      LOG.info("Merchant - {}", merchant);
      LOG.info("Merchant Length is Invalid: {}", merchant.length() != 10 ? "Yes" : "No");

      if (!(singleEcIsValid(merchant))) {
        return false;
      }
    }

    return true;
  }

  private boolean multipleEcs(String merchantOrMerchantList) {
    return merchantOrMerchantList.length() > 10;
  }

  public boolean validInput(String merchantOrMerchantList) {
    LOG.info("Starting to check user input");

    if (inputNullOrBlank(merchantOrMerchantList)) {
      LOG.warn("Input is null / blank");
      return false;
    }

    if (multipleEcs(merchantOrMerchantList)) {
      LOG.info("Validating input for multiple EC's");
      return multipleEcsAreValid(merchantOrMerchantList);
    } else {
      LOG.info("Validating input for single EC");
      return singleEcIsValid(merchantOrMerchantList);
    }
  }

  public boolean validEmail(String address) {
    if (address == null || address.isEmpty()) {
      return false;
    }

    return PATTERN.matcher(address).matches();
  }
}
