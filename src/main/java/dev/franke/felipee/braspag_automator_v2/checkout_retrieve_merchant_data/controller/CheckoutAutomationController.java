package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.HeaderValidator;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.MerchantsToEmailInput;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutCompletedAutomationService;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutMailSender;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutRunner;
import dev.franke.felipee.braspag_automator_v2.contracts.controller.EcSearchMainController;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout/retrieve-merchant")
public class CheckoutAutomationController implements EcSearchMainController {

    private final HeaderValidator headerValidator;
    private final CheckoutMailSender checkoutMailSender;
    private final CheckoutCompletedAutomationService checkoutCompletedAutomationService;
    private final CheckoutRunner runner;

    public CheckoutAutomationController(
            HeaderValidator headerValidator,
            CheckoutMailSender checkoutMailSender,
            CheckoutCompletedAutomationService checkoutCompletedAutomationService,
            CheckoutRunner runner) {
        this.headerValidator = headerValidator;
        this.checkoutMailSender = checkoutMailSender;
        this.checkoutCompletedAutomationService = checkoutCompletedAutomationService;
        this.runner = runner;
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> executeAutomation(
            @RequestBody String[] merchantEcNumbers,
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            ResponseEntity.status(401).build();
        }
        runner.run(merchantEcNumbers);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping
    public ResponseEntity<List<?>> getResultsInJson(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(checkoutCompletedAutomationService.jsonOutput());
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteResults(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        checkoutCompletedAutomationService.clear();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email")
    public ResponseEntity<?> getMerchantsToEmail(
            @RequestHeader(name = "Authorization") String authorizationHeader,
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
