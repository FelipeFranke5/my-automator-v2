package dev.franke.felipee.braspag_automator_v2.exception_handlers;

import dev.franke.felipe.api30_automation_api.automation.merchant_data.exception.InvalidEstablishmentCodeException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.EmptyQueryException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.InvalidEmailException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.InvalidFilterByException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.InvalidHeaderException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EmptyQueryException.class)
    public ProblemDetail handleEmptyQueryException(EmptyQueryException emptyQueryException, WebRequest webRequest) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, emptyQueryException.getMessage());
        problemDetail.setInstance(URI.create("query"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    @ExceptionHandler(InvalidHeaderException.class)
    public ProblemDetail handleInvalidHeaderException(
            InvalidHeaderException invalidHeaderException, WebRequest webRequest) {
        var problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, invalidHeaderException.getMessage());
        problemDetail.setInstance(URI.create("authentication"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("authorizationHeaderValue", webRequest.getHeader("Authorization"));
        return problemDetail;
    }

    @ExceptionHandler(InvalidFilterByException.class)
    public ProblemDetail handleInvalidFilterByException(
            InvalidFilterByException invalidFilterByException, WebRequest webRequest) {
        var problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, invalidFilterByException.getMessage());
        problemDetail.setInstance(URI.create("filterBy"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("filterByValue", webRequest.getParameter("filterBy"));
        var validValuesMap = new HashMap<String, String>();
        validValuesMap.put("merchantBlocked", "Search for Merchants that are blocked");
        validValuesMap.put("pixEnabled", "Search for Merchants with PIX enabled");
        validValuesMap.put("antifraudEnabled", "Search for Merchants with Antifraud enabled");
        validValuesMap.put("tokenizationEnabled", "Search for Merchants with Tokenization enabled");
        validValuesMap.put("velocityEnabled", "Search for Merchants with Velocity enabled");
        validValuesMap.put("smartRecurrencyEnabled", "Search for Merchants with Smart Recurrency enabled");
        validValuesMap.put("zeroAuthEnabled", "Search for Merchants with Zero Auth enabled");
        validValuesMap.put("binQueryEnabled", "Search for Merchants with Bin Query enabled");
        validValuesMap.put("selectiveAuthEnabled", "Search for Merchants with Selective Auth enabled");
        validValuesMap.put("automaticCancellationEnabbled", "Search for Merchants with Automatic Cancellation enabled");
        validValuesMap.put("forceBraspagAuthEnabled", "Search for Merchants with Force Braspag Auth enabled");
        validValuesMap.put("mtlsEnabled", "Search for Merchants with MTLS enabled");
        validValuesMap.put("webhookEnabled", "Search for Merchants with Webhook enabled");
        validValuesMap.put("atLeastOneIpEnabled", "Search for Merchants with at least one trust IP");
        problemDetail.setProperty("validFilterByValues", validValuesMap);
        return problemDetail;
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ProblemDetail handleInvalidEmailException(
            InvalidEmailException invalidEmailException, WebRequest webRequest) {
        var problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, invalidEmailException.getMessage());
        problemDetail.setInstance(URI.create("sendEmail"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    @ExceptionHandler(InvalidEstablishmentCodeException.class)
    public ProblemDetail handleInvalidEstablishmentCodeException(
            InvalidEstablishmentCodeException invalidEstablishmentCodeException, WebRequest webRequest) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, invalidEstablishmentCodeException.getMessage());
        problemDetail.setInstance(URI.create("automation"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }
}
