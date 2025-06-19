package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.HeaderValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.CheckoutMerchantValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.Enable3DSFailService;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.Enable3DSResultService;
import dev.franke.felipee.braspag_automator_v2.contracts.controller.EcSearchMainController;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout/enable-3ds")
public class Enable3DSResultController implements EcSearchMainController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Logger LOG = LoggerFactory.getLogger(Enable3DSResultController.class);
    private static final String INITIAL_AUTOMATION_QUEUE_NAME = "checkout-cielo-enable-3ds-init";

    private final SqsTemplate sqsTemplate;
    private final HeaderValidator headerValidator;
    private final CheckoutMerchantValidator checkoutMerchantValidator;
    private final Enable3DSResultService enable3dsResultService;
    private final Enable3DSFailService enable3DSFailService;

    public Enable3DSResultController(
            SqsTemplate sqsTemplate,
            HeaderValidator headerValidator,
            CheckoutMerchantValidator checkoutMerchantValidator,
            Enable3DSResultService enable3dsResultService,
            Enable3DSFailService enable3DSFailService) {
        this.sqsTemplate = sqsTemplate;
        this.headerValidator = headerValidator;
        this.checkoutMerchantValidator = checkoutMerchantValidator;
        this.enable3dsResultService = enable3dsResultService;
        this.enable3DSFailService = enable3DSFailService;
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

        sendMessageForECs(ecs);
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
    @GetMapping("/text")
    public ResponseEntity<String> getResultsInText(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!headerValidator.headerIsValid(authorizationHeader)) {
            return ResponseEntity.status(401).build();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Automações com sucessso:").append("\n");
        enable3dsResultService.jsonOutput().forEach(automation -> builder.append("\n")
                .append(automation.ec()));
        builder.append("\n\nAutomações com erro:").append("\n");
        enable3DSFailService.jsonOutput().forEach(automation -> builder.append("\n")
                .append(automation.ec()));
        return ResponseEntity.ok(builder.toString());
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
            sqsTemplate.send(to -> to.queue(INITIAL_AUTOMATION_QUEUE_NAME).payload(ec));
            LOG.info("[{}] Message sent", ec);
        } catch (Exception exception) {
            LOG.error(
                    "[{}] Error while attempting to send message to queue '{}'",
                    ec,
                    INITIAL_AUTOMATION_QUEUE_NAME,
                    exception);
        }
    }
}
