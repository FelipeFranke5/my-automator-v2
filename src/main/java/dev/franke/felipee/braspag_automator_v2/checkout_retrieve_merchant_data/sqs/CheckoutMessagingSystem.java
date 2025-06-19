package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.sqs;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.CheckoutMerchantValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.model.CheckoutCompletedAutomation;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutCompletedAutomationService;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutFailedAutomationService;
import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service.CheckoutRunner;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CheckoutMessagingSystem {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutMessagingSystem.class);
    private static final String INITIAL_AUTOMATION_QUEUE_NAME = "checkout-cielo-retrieve-data";
    private static final String AFTER_AUTOMATION_QUEUE_NAME = "checkout-cielo-after";
    private static final String FAIL_QUEUE_NAME = "checkout-cielo-fail";

    private final CheckoutFailedAutomationService failedAutomationService;
    private final CheckoutCompletedAutomationService completedAutomationService;
    private final CheckoutMerchantValidator validator;
    private final CheckoutRunner checkoutRunner;
    private final SqsTemplate sqsTemplate;

    public CheckoutMessagingSystem(
            CheckoutFailedAutomationService failedAutomationService,
            CheckoutMerchantValidator validator,
            CheckoutRunner checkoutRunner,
            SqsTemplate sqsTemplate,
            CheckoutCompletedAutomationService completedAutomationService) {
        this.failedAutomationService = failedAutomationService;
        this.validator = validator;
        this.checkoutRunner = checkoutRunner;
        this.sqsTemplate = sqsTemplate;
        this.completedAutomationService = completedAutomationService;
    }

    @SqsListener(INITIAL_AUTOMATION_QUEUE_NAME)
    public void receiveFromQueueForAutomation(String ecNumber) {
        LOG.info("Received message for automation");
        if (validator.ecIsValid(ecNumber)) {
            var result = checkoutRunner.singleEcRoutine(ecNumber);
            result.ifPresentOrElse(
                    this::sendMessageForSuccessfulAutomation, () -> sendMessageForFailedAutomation(ecNumber));
            return;
        }
        sendMessageForFailedAutomation("1111111111");
    }

    @SqsListener(AFTER_AUTOMATION_QUEUE_NAME)
    public void listenAfterAutomation(CheckoutCompletedAutomation automation) {
        LOG.info("Received message after automation is completed");
        completedAutomationService.save(automation);
    }

    @SqsListener(FAIL_QUEUE_NAME)
    public void listenAfterFail(String ecNumber) {
        LOG.info("Received message after automation failure");
        failedAutomationService.save(ecNumber, "Falha na Execucao");
    }

    private void sendMessageForSuccessfulAutomation(CheckoutCompletedAutomation automation) {
        LOG.info("[{}] Attempting to send successful message", automation.getEc());
        try {
            sqsTemplate.send(to ->
                    to.queue(AFTER_AUTOMATION_QUEUE_NAME).payload(automation).delaySeconds(20));
            LOG.info("[{}] Sent!", automation.getEc());
        } catch (Exception exception) {
            LOG.error("[{}] Could not send message", automation.getEc(), exception);
        }
    }

    private void sendMessageForFailedAutomation(String ec) {
        LOG.info("[{}] Attempting to send failed message", ec);
        try {
            sqsTemplate.send(to -> to.queue(FAIL_QUEUE_NAME).payload(ec).delaySeconds(20));
            LOG.info("[{}] Sent Message!", ec);
        } catch (Exception exception) {
            LOG.error("[{}] Could not send message", ec, exception);
        }
    }
}
