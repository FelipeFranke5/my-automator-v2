package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller;

import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HeaderValidator {

  private static final Logger LOG = LoggerFactory.getLogger(HeaderValidator.class);

  @Value("${braspag.prod.login}")
  private String login;

  @Value("${braspag.prod.password}")
  private String password;

  private boolean isProperlyFormatted(String headerValue) {
    LOG.info("Starting to check if the Header is valid");

    if (headerValue == null) {
      LOG.warn("Header value is null!");
      return false;
    }

    if (headerValue.isBlank()) {
      LOG.warn("Header value is blank!");
      return false;
    }

    if (!headerValue.startsWith("Base64 ")) {
      LOG.warn("Invalid Header! Does not start with Base64 prefix");
      return false;
    }

    try {
      String credentials = headerValue.split("Base64 ")[1].trim();

      if (credentials.isBlank()) {
        LOG.warn("Credentials part is blank!");
        return false;
      }
    } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
      LOG.warn("Invalid String, could not parse the authorization value");
      LOG.warn("Out of bounds error");
      return false;
    }

    LOG.info("Validation completed. It is properly formatted");
    return true;
  }

  private String getConcatenatedCredentials() {
    return login + ":" + password;
  }

  private String getEcondedString(String headerValue) {
    return headerValue.split("Base64 ")[1];
  }

  private String getEncodedHeader() {
    return Base64.getEncoder().encodeToString(getConcatenatedCredentials().getBytes());
  }

  public boolean headerIsValid(String headerValue) {
    return isProperlyFormatted(headerValue)
        && getEcondedString(headerValue).equals(getEncodedHeader());
  }
}
