package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.HeaderValidator;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.MerchantsToEmailInput;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutCompletedAutomationService;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutFailedAutomationService;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutMailSender;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.sqs.CheckoutMessagingSystem;
import dev.franke.felipee.braspag_automator_v2.contracts.controller.EcSearchMainController;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout/retrieve-merchant")
public class CheckoutAutomationController implements EcSearchMainController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Logger LOG = LoggerFactory.getLogger(CheckoutAutomationController.class);
    private static final String INITIAL_AUTOMATION_QUEUE_NAME = "checkout-cielo-retrieve-data";

    private final SqsTemplate sqsTemplate;
    private final HeaderValidator headerValidator;
    private final CheckoutMailSender checkoutMailSender;
    private final CheckoutCompletedAutomationService checkoutCompletedAutomationService;
    private final CheckoutFailedAutomationService checkoutFailedAutomationService;

    public CheckoutAutomationController(
            SqsTemplate sqsTemplate,
            HeaderValidator headerValidator,
            CheckoutMessagingSystem messagingSystem,
            CheckoutMailSender checkoutMailSender,
            CheckoutFailedAutomationService checkoutFailedAutomationService,
            CheckoutCompletedAutomationService checkoutCompletedAutomationService) {
        this.sqsTemplate = sqsTemplate;
        this.headerValidator = headerValidator;
        this.checkoutMailSender = checkoutMailSender;
        this.checkoutFailedAutomationService = checkoutFailedAutomationService;
        this.checkoutCompletedAutomationService = checkoutCompletedAutomationService;
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> executeAutomation(
            @RequestBody String[] merchantEcNumbers,
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        sendMessageForECs(merchantEcNumbers);
        return ResponseEntity.noContent().build();
    }

    private void sendMessageForECs(String[] ecs) {
        CompletableFuture.runAsync(
                () -> {
                    for (String ec : ecs) {
                        sendMessage(ec);
                    }
                },
                executor);
    }

    private void sendMessage(String ec) {
        LOG.info("[{}] Attempting to send message", ec);
        try {
            sqsTemplate.send(
                    to -> to.queue(INITIAL_AUTOMATION_QUEUE_NAME).payload(ec).delaySeconds(20));
            LOG.info("[{}] Message sent", ec);
        } catch (Exception exception) {
            LOG.error(
                    "[{}] Error while attempting to send message to queue '{}'",
                    ec,
                    INITIAL_AUTOMATION_QUEUE_NAME,
                    exception);
        }
    }

    @Override
    @GetMapping
    public ResponseEntity<List<?>> getResultsInJson(@RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        return ResponseEntity.ok(checkoutCompletedAutomationService.jsonOutput());
    }

    @Override
    @GetMapping("/text")
    public ResponseEntity<String> getResultsInText(@RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        StringBuilder builder = new StringBuilder();
        builder.append("Automações com sucessso:").append("\n");
        checkoutCompletedAutomationService.jsonOutput().forEach(automation -> builder.append("\n")
                .append(automation.ec()));
        builder.append("\n\nAutomações com erro:").append("\n");
        checkoutFailedAutomationService.jsonOutput().forEach(automation -> builder.append("\n")
                .append(automation.ec()));
        return ResponseEntity.ok(builder.toString());
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteResults(@RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        checkoutCompletedAutomationService.clear();
        checkoutFailedAutomationService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email")
    public ResponseEntity<?> getMerchantsToEmail(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody final MerchantsToEmailInput input) {
        headerValidator.validate(authorizationHeader);
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
