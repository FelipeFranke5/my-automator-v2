package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.AutomationResult;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.MerchantsToEmailInput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.successful.SuccessfulAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.EmailSender;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantRunner;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/retrieve-merchant")
public class MerchantController {

    private final MerchantService merchantService;
    private final HeaderValidator headerValidator;
    private final EmailSender emailSender;
    private final MerchantRunner merchantRunner;

    public MerchantController(
            MerchantService merchantService,
            HeaderValidator headerValidator,
            EmailSender emailSender,
            MerchantRunner merchantRunner) {
        this.merchantService = merchantService;
        this.headerValidator = headerValidator;
        this.emailSender = emailSender;
        this.merchantRunner = merchantRunner;
    }

    @PostMapping
    public ResponseEntity<?> index(
            @RequestBody String merchants, @RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }

        AutomationBodyResponse bodyResponse;

        if (merchantRunner.inputIsValid(merchants)) {
            runAutomation(merchants);
            bodyResponse = new AutomationBodyResponse(LocalDateTime.now(), true);
        } else {
            bodyResponse = new AutomationBodyResponse(LocalDateTime.now(), false);
        }

        return ResponseEntity.status(201).body(bodyResponse);
    }

    @GetMapping("/email")
    public ResponseEntity<?> getMerchantsToEmail(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody MerchantsToEmailInput input) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }

        if (input == null) {
            return ResponseEntity.status(400).body("Email is required");
        }

        byte automationResult = emailSender.sendEmailWithExcelResults(input.email());
        AutomationResult result;

        return switch (automationResult) {
            case 2 -> {
                result = new AutomationResult("Nenhum resultado encontrado");
                yield ResponseEntity.status(404).body(result);
            }
            case 1 -> {
                result = new AutomationResult("Email invalido");
                yield ResponseEntity.status(400).body(result);
            }
            default -> {
                result = new AutomationResult("OK");
                yield ResponseEntity.status(200).body(result);
            }
        };
    }

    @GetMapping
    public ResponseEntity<List<SuccessfulAutomationOutput>> getMerchants(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(merchantService.jsonOutput());
    }

    @DeleteMapping
    public ResponseEntity<?> deleteRecords(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }

        merchantService.clearAllMerchants();
        return ResponseEntity.ok().build();
    }

    private void runAutomation(String merchants) {
        merchantService.runAutomation(merchants);
    }
}
