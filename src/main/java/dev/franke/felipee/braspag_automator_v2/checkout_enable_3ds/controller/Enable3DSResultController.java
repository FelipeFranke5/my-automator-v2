package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.HeaderValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.CheckoutMerchantValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.Enable3DSResultRunner;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.Enable3DSResultService;
import dev.franke.felipee.braspag_automator_v2.contracts.controller.EcSearchMainController;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout/enable-3ds")
public class Enable3DSResultController implements EcSearchMainController {

    private final HeaderValidator headerValidator;
    private final CheckoutMerchantValidator checkoutMerchantValidator;
    private final Enable3DSResultService enable3dsResultService;
    private final Enable3DSResultRunner runner;

    public Enable3DSResultController(
            HeaderValidator headerValidator,
            CheckoutMerchantValidator checkoutMerchantValidator,
            Enable3DSResultService enable3dsResultService,
            Enable3DSResultRunner runner) {
        this.headerValidator = headerValidator;
        this.checkoutMerchantValidator = checkoutMerchantValidator;
        this.enable3dsResultService = enable3dsResultService;
        this.runner = runner;
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> executeAutomation(
            @RequestBody String[] ecs, @RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }

        if (!checkoutMerchantValidator.allEcsAreValid(ecs)) {
            return ResponseEntity.badRequest().build();
        }

        runner.run(ecs);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping
    public ResponseEntity<List<?>> getResultsInJson(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(enable3dsResultService.jsonOutput());
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteResults(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        enable3dsResultService.clear();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/text")
    public ResponseEntity<?> getResults(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(enable3dsResultService.resultsInString());
    }
}
