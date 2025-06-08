package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.failed.FailedAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.FailedScriptService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/retrieve-merchant/failed")
public class MerchantFailedScriptsController {

    private final FailedScriptService service;
    private final HeaderValidator headerValidator;

    public MerchantFailedScriptsController(FailedScriptService service, HeaderValidator headerValidator) {
        this.service = service;
        this.headerValidator = headerValidator;
    }

    @GetMapping
    public ResponseEntity<List<FailedAutomationOutput>> all(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(service.jsonOutput());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        service.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
