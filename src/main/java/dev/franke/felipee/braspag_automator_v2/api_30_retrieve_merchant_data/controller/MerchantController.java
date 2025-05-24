package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.AutomationResult;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.MerchantsToEmailInput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantService;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
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

  public MerchantController(MerchantService merchantService, HeaderValidator headerValidator) {
    this.merchantService = merchantService;
    this.headerValidator = headerValidator;
  }

  @PostMapping
  public ResponseEntity<?> index(
      @RequestBody String merchants,
      @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {

    if (!headerValidator.headerIsValid(authorizationHeader)) {
      return ResponseEntity.status(401).build();
    }

    AutomationBodyResponse bodyResponse;

    if (merchantService.inputIsValid(merchants)) {
      runAutomation(merchants);
      bodyResponse = new AutomationBodyResponse(LocalDateTime.now(), true);
    } else {
      bodyResponse = new AutomationBodyResponse(LocalDateTime.now(), false);
    }

    return ResponseEntity.status(201).body(bodyResponse);
  }

  @GetMapping("/email")
  public ResponseEntity<?> getMerchantsToEmail(
      @RequestHeader(name = "Authorization", required = true) String authorizationHeader,
      @RequestBody MerchantsToEmailInput input) {

    if (!headerValidator.headerIsValid(authorizationHeader)) {
      return ResponseEntity.status(401).build();
    }

    if (input == null) {
      return ResponseEntity.status(400).body("Email is required");
    }

    byte automationResult = merchantService.sendEmailWithExcelResults(input.email());
    AutomationResult result;

    switch (automationResult) {
      case 2:
        result = new AutomationResult("No results to send");
        return ResponseEntity.status(404).body(result);

      case 1:
        result = new AutomationResult("Invalid Email");
        return ResponseEntity.status(400).body(result);

      default:
        result = new AutomationResult("OK");
        return ResponseEntity.status(200).body(result);
    }
  }

  @GetMapping
  public ResponseEntity<?> getMerchants(
      @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {

    if (!headerValidator.headerIsValid(authorizationHeader)) {
      return ResponseEntity.status(401).build();
    }

    return ResponseEntity.ok(merchantService.listOfMerchants());
  }

  @DeleteMapping
  public ResponseEntity<?> deleteRecords(
      @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {

    if (!headerValidator.headerIsValid(authorizationHeader)) {
      return ResponseEntity.status(401).build();
    }

    merchantService.clearAllMerchants();
    return ResponseEntity.ok().build();
  }

  private void runAutomation(String merchants) {
    CompletableFuture.runAsync(
        () -> {
          merchantService.runAutomation(merchants);
        });
  }
}
