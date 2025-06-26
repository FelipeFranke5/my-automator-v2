package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.filter.APIMerchantFilter;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.message_sender.APIMessageSender;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.payload_validator.StringArrayMerchantValidator;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.MerchantsToEmailInput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.failed.FailedAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.successful.SuccessfulAutomationOutput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.EmailSender;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.FailedScriptService;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantService;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/retrieve-merchant")
public class MerchantController {

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private FailedScriptService failedScriptService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private HeaderValidator headerValidator;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private APIMerchantFilter apiMerchantFilter;

    @Autowired
    private APIMessageSender apiMessageSender;

    @Autowired
    private StringArrayMerchantValidator stringArrayMerchantValidator;

    @PostMapping
    public ResponseEntity<Void> executeAutomation(
            @RequestBody String[] merchants, @RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        stringArrayMerchantValidator.validate(merchants);
        apiMessageSender.sendMessageForECs(merchants);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<SuccessfulAutomationOutput>> getResultsInJson(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        return ResponseEntity.ok(merchantService.jsonOutput());
    }

    @GetMapping("failed")
    public ResponseEntity<List<FailedAutomationOutput>> getFailedResultsInJson(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        return ResponseEntity.ok(failedScriptService.jsonOutput());
    }

    @GetMapping("/text")
    public ResponseEntity<String> getResultsInText(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam(name = "filterBy", defaultValue = "none") String filterBy) {
        headerValidator.validate(authorizationHeader);
        apiMerchantFilter.assertFilterByIsValid(filterBy);
        return ResponseEntity.ok(apiMerchantFilter.applyFilter(filterBy));
    }

    @GetMapping("/failed/text")
    public ResponseEntity<String> getFailedResultsInText(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        return ResponseEntity.ok(failedScriptService.getOutputInText());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResults(@RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        merchantService.clear();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/failed")
    public ResponseEntity<Void> deleteFailedResults(@RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        failedScriptService.deleteAll();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/email")
    public ResponseEntity<Void> getMerchantsToEmail(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody MerchantsToEmailInput input) {
        headerValidator.validate(authorizationHeader);
        emailSender.sendEmailWithExcelResults(input.email());
        return ResponseEntity.noContent().build();
    }
}
