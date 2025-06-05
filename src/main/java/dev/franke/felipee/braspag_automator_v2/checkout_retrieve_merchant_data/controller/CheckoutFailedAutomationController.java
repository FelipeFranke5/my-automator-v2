package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.HeaderValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutFailedAutomationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout/retrieve-merchant/failed")
public class CheckoutFailedAutomationController {

    private final CheckoutFailedAutomationService service;
    private final HeaderValidator headerValidator;

    public CheckoutFailedAutomationController(
            CheckoutFailedAutomationService service, HeaderValidator headerValidator) {
        this.service = service;
        this.headerValidator = headerValidator;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader(name = "Authorization") final String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@RequestHeader(name = "Authorization") final String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        service.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
