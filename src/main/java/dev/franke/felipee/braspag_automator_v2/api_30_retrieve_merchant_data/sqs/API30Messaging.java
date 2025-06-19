package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.sqs;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.FailedScriptService;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantRunner;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantService;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantValidator;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class API30Messaging {

    private static final Logger LOG = LoggerFactory.getLogger(API30Messaging.class);
    private static final String INITIAL_QUEUE_NAME = "api30-init";
    private static final String FAIL_QUEUE_NAME = "api30-fail";
    private static final String SUCCESS_QUEUE_NAME = "api30-success";

    private final MerchantValidator merchantValidator;
    private final MerchantRunner merchantRunner;
    private final MerchantService merchantService;
    private final FailedScriptService failedScriptService;
    private final SqsTemplate sqsTemplate;

    public API30Messaging(
            MerchantValidator merchantValidator,
            MerchantRunner merchantRunner,
            MerchantService merchantService,
            FailedScriptService failedScriptService,
            SqsTemplate sqsTemplate) {
        this.merchantValidator = merchantValidator;
        this.merchantRunner = merchantRunner;
        this.merchantService = merchantService;
        this.failedScriptService = failedScriptService;
        this.sqsTemplate = sqsTemplate;
    }

    @SqsListener(INITIAL_QUEUE_NAME)
    public void api30Listener(String ecNumber) {
        LOG.info("[{}] Received message for automation", ecNumber);
        if (merchantValidator.merchantArrayIsValid(new String[] {ecNumber})) {
            merchantRunner
                    .singleEcRoutine(ecNumber)
                    .ifPresentOrElse(this::sendResultMessage, () -> sendFailMessage(ecNumber));
            return;
        }
        sendFailMessage(ecNumber);
    }

    @SqsListener(SUCCESS_QUEUE_NAME)
    public void enable3dsSucessListener(Merchant merchant) {
        LOG.info("[{}] Received message for sucessful automation", merchant.getEc());
        merchantService.save(merchant);
        LOG.info("[{}] Saved the result", merchant.getEc());
    }

    @SqsListener(FAIL_QUEUE_NAME)
    public void enable3dsFailListener(String ec) {
        LOG.info("[{}] Received message for fail automation", ec);
        failedScriptService.save(ec, "Erro de execucao");
        LOG.info("[{}] Saved fail result", ec);
    }

    private void sendResultMessage(Merchant merchant) {
        LOG.info("[{}] Sending message to sucess queue '{}'", merchant.getEc(), SUCCESS_QUEUE_NAME);
        sendSqsMessageSucess(merchant);
    }

    private void sendFailMessage(String ec) {
        sendSqsMessageFail(ec);
    }

    private void sendSqsMessageSucess(Merchant merchant) {
        LOG.info("[{}] Attempting to send message for successful result", merchant.getEc());
        try {
            sqsTemplate.send(
                    to -> to.queue(SUCCESS_QUEUE_NAME).payload(merchant).delaySeconds(20));
            LOG.info("[{}] Message Sent!", merchant.getEc());
        } catch (Exception exception) {
            LOG.error("[{}] Error while attempting to send message", merchant.getEc(), exception);
        }
    }

    private void sendSqsMessageFail(String ec) {
        LOG.info("[{}] Attempting to send message for fail result", ec);
        try {
            sqsTemplate.send(to -> to.queue(FAIL_QUEUE_NAME).payload(ec).delaySeconds(20));
            LOG.info("[{}] Message Sent for Failed Automation!", ec);
        } catch (Exception exception) {
            LOG.error("[{}] Error occured while attempting to send message", ec, exception);
        }
    }
}
