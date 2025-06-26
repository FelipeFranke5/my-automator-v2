package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.InvalidHeaderException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HeaderValidator {

    @Value("${braspag.prod.login}")
    private String login;

    @Value("${braspag.prod.password}")
    private String password;

    public void validate(String userHeader) {
        checkUserHeaderIsValid(userHeader);
        checkCredentialsAreEqual(getEcondedString(userHeader), getEncodedHeader());
    }

    private String[] getHeaderParts(String header) {
        return header.split("Base64 ");
    }

    private boolean headerPartAfterBase64IsNullOrBlank(String[] headerParts) {
        var secondPart = headerParts[1];
        return secondPart == null || secondPart.isBlank();
    }

    private boolean headerPartsLengthIsValid(String header) {
        var headerParts = getHeaderParts(header);
        return headerParts.length == 2;
    }

    private boolean headerIsNullOrBlank(String header) {
        return header == null || header.isBlank();
    }

    private boolean headerStartsWithBase64(String header) {
        return header.startsWith("Base64 ");
    }

    private boolean encodedUserHeaderEqualsServerEncodedHeader(String encodedUserHeader, String encodedServerHeader) {
        return encodedUserHeader.equals(encodedServerHeader);
    }

    private void checkUserHeaderIsValid(String headerValue) {
        checkHeaderNullOrBlank(headerValue);
        checkHeaderStartsWithBase64(headerValue);
        checkHeaderLengthIsValid(headerValue);
        checkHeaderPartAfterBase64IsValid(headerValue);
    }

    private void checkCredentialsAreEqual(String encodedUserHeader, String encodedServerHeader) {
        if (!encodedUserHeaderEqualsServerEncodedHeader(encodedUserHeader, encodedServerHeader)) {
            throw new InvalidHeaderException("Credentials are not valid");
        }
    }

    private void checkHeaderPartAfterBase64IsValid(String header) {
        var headerParts = getHeaderParts(header);
        if (headerPartAfterBase64IsNullOrBlank(headerParts)) {
            throw new InvalidHeaderException("Header Part after Base64 is Null or Blank");
        }
    }

    private void checkHeaderLengthIsValid(String header) {
        try {
            if (!headerPartsLengthIsValid(header)) {
                throw new InvalidHeaderException("Invalid Header Length");
            }
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new InvalidHeaderException("Invalid Header Length");
        }
    }

    private void checkHeaderNullOrBlank(String header) {
        if (headerIsNullOrBlank(header)) {
            throw new InvalidHeaderException("Header is null or blank");
        }
    }

    private void checkHeaderStartsWithBase64(String header) {
        if (!headerStartsWithBase64(header)) {
            throw new InvalidHeaderException("Header does not start with Base64");
        }
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
}
