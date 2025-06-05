package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.HeaderValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.Enable3DSFailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout/enable-3ds/failed")
public class Enable3DSFailController {

    private final Enable3DSFailService service;
    private final HeaderValidator headerValidator;

    public Enable3DSFailController(Enable3DSFailService service, HeaderValidator headerValidator) {
        this.service = service;
        this.headerValidator = headerValidator;
    }

    @GetMapping
    public ResponseEntity<?> getFailedResults(
            @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(service.listAll());
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(
            @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        service.removeAll();
        return ResponseEntity.noContent().build();
    }
}
