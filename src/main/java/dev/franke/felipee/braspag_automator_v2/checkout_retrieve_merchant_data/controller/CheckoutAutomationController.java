package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.HeaderValidator;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.MerchantsToEmailInput;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.model.CheckoutFailedAutomation;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutCompletedAutomationService;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutFailedAutomationService;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutMailSender;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutRunner;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout/retrieve-merchant")
public class CheckoutAutomationController {

    private final HeaderValidator headerValidator;
    private final CheckoutMailSender checkoutMailSender;
    private final CheckoutCompletedAutomationService checkoutCompletedAutomationService;
    private final CheckoutRunner checkoutRunner;
    private final CheckoutFailedAutomationService failedAutomationService;

    public CheckoutAutomationController(
            final HeaderValidator headerValidator,
            final CheckoutMailSender checkoutMailSender,
            final CheckoutCompletedAutomationService checkoutCompletedAutomationService,
            final CheckoutRunner checkoutRunner,
            final CheckoutFailedAutomationService failedAutomationService) {
        this.headerValidator = headerValidator;
        this.checkoutMailSender = checkoutMailSender;
        this.checkoutCompletedAutomationService = checkoutCompletedAutomationService;
        this.checkoutRunner = checkoutRunner;
        this.failedAutomationService = failedAutomationService;
    }

    @GetMapping("/pending")
    public ResponseEntity<CheckoutNumberOfProcesses> getPendingAutomations(
            @RequestHeader(name = "Authorization", required = true) final String authorizationHeader) {

        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok().body(checkoutRunner.getNumberOfProcesses());
    }

    @GetMapping("/failed")
    public ResponseEntity<List<CheckoutFailedAutomation>> getFailedAutomations(
            @RequestHeader(name = "Authorization", required = true) final String authorizationHeader) {

        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }

        final List<CheckoutFailedAutomation> failedAutomations = failedAutomationService.getAll();
        return ResponseEntity.ok(failedAutomations);
    }

    @PostMapping
    public ResponseEntity<Void> runAutomations(
            @RequestHeader(name = "Authorization", required = true) final String authorizationHeader,
            @RequestBody final String[] merchantEcNumbers) {

        if (!headerValidator.headerIsValid(authorizationHeader)) {
            ResponseEntity.status(401).build();
        }

        checkoutRunner.run(merchantEcNumbers);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> getCompletedAutomationOutput(
            @RequestHeader(name = "Authorization", required = true) final String authorizationHeader) {

        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(checkoutCompletedAutomationService.outputForJson());
    }

    @GetMapping("/email")
    public ResponseEntity<?> getMerchantsToEmail(
            @RequestHeader(name = "Authorization", required = true) final String authorizationHeader,
            @RequestBody final MerchantsToEmailInput input) {

        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }

        EmailResponseBody result;

        if (input.email() == null || input.email().isEmpty()) {
            result = new EmailResponseBody(LocalDateTime.now(), "Email e obrigatorio");
            return ResponseEntity.status(400).body(result);
        }

        final byte automationResult = checkoutMailSender.sendEmailWithExcelResults(input.email());
        int statusCode;

        if (automationResult == 0) {
            result = new EmailResponseBody(LocalDateTime.now(), "Em processamento");
            statusCode = 200;
        } else {
            result = new EmailResponseBody(LocalDateTime.now(), "Email invalido");
            statusCode = 400;
        }

        return ResponseEntity.status(statusCode).body(result);
    }
}
