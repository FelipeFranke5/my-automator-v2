package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.AutomationResult;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.MerchantsToEmailInput;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.EmailSender;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.FailedScriptService;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantService;
import dev.franke.felipee.braspag_automator_v2.contracts.controller.EcSearchMainController;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MerchantController implements EcSearchMainController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Logger LOG = LoggerFactory.getLogger(MerchantController.class);
    private static final String INITIAL_AUTOMATION_QUEUE_NAME = "api30-init";

    private final SqsTemplate sqsTemplate;
    private final FailedScriptService failedScriptService;
    private final MerchantService merchantService;
    private final HeaderValidator headerValidator;
    private final EmailSender emailSender;

    public MerchantController(
            SqsTemplate sqsTemplate,
            FailedScriptService failedScriptService,
            MerchantService merchantService,
            HeaderValidator headerValidator,
            EmailSender emailSender) {
        this.sqsTemplate = sqsTemplate;
        this.failedScriptService = failedScriptService;
        this.merchantService = merchantService;
        this.headerValidator = headerValidator;
        this.emailSender = emailSender;
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> executeAutomation(
            @RequestBody String[] merchants, @RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        sendMessageForECs(merchants);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping
    public ResponseEntity<List<?>> getResultsInJson(@RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        return ResponseEntity.ok(merchantService.jsonOutput());
    }

    @Override
    @GetMapping("/text")
    public ResponseEntity<String> getResultsInText(@RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        StringBuilder builder = new StringBuilder();
        builder.append("Automações com sucessso:").append("\n");
        merchantService.jsonOutput().forEach(automation -> builder.append("\n").append(automation.ecNumber()));
        builder.append("\n\nAutomações com erro:").append("\n");
        failedScriptService.jsonOutput().forEach(automation -> builder.append("\n")
                .append(automation.ecNumber()));
        return ResponseEntity.ok(builder.toString());
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteResults(@RequestHeader(name = "Authorization") String authorizationHeader) {
        headerValidator.validate(authorizationHeader);
        merchantService.clear();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/email")
    public ResponseEntity<?> getMerchantsToEmail(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody MerchantsToEmailInput input) {
        headerValidator.validate(authorizationHeader);

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
        LOG.info("[{}] Attempting to send message in order to initialize automation", ec);
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
}
