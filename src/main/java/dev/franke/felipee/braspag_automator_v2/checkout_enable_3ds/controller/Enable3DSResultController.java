package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.HeaderValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.CheckoutMerchantValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.Enable3DSResultRunner;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.Enable3DSResultService;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout/enable-3ds")
public class Enable3DSResultController {

    private static final String INVALID_EC_MESSAGE = "Um dos ECs informados nao e valido";
    private static final String EXECUTION_MADE_MESSAGE = "A sua execucao foi iniciada e esta em andamento";

    private final Enable3DSResultRunner enable3dsResultRunner;
    private final HeaderValidator headerValidator;
    private final CheckoutMerchantValidator checkoutMerchantValidator;
    private final Enable3DSResultService enable3dsResultService;

    public Enable3DSResultController(
            Enable3DSResultRunner enable3dsResultRunner,
            HeaderValidator headerValidator,
            CheckoutMerchantValidator checkoutMerchantValidator,
            Enable3DSResultService enable3dsResultService) {
        this.enable3dsResultRunner = enable3dsResultRunner;
        this.headerValidator = headerValidator;
        this.checkoutMerchantValidator = checkoutMerchantValidator;
        this.enable3dsResultService = enable3dsResultService;
    }

    @PostMapping
    public ResponseEntity<?> executeAutomation(
            @RequestBody AutomationRequestBody requestBody,
            @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {

        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }

        if (!checkoutMerchantValidator.allEcsAreValid(requestBody.ecs())) {
            CheckoutEnable3dsResponseBody invalidEcResponseBody =
                    new CheckoutEnable3dsResponseBody(LocalDateTime.now(), INVALID_EC_MESSAGE);
            return ResponseEntity.badRequest().body(invalidEcResponseBody);
        }

        enable3dsResultRunner.run(requestBody.ecs());
        CheckoutEnable3dsResponseBody validEcResponseBody =
                new CheckoutEnable3dsResponseBody(LocalDateTime.now(), EXECUTION_MADE_MESSAGE);
        return ResponseEntity.status(201).body(validEcResponseBody);
    }

    @GetMapping
    public ResponseEntity<?> getResults(
            @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {

        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok().body(enable3dsResultService.resultsInString());
    }
}
