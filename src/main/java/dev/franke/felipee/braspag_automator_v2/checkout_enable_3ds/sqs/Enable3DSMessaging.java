package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.sqs;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.model.Enable3DSResult;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.CheckoutMerchantValidator;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.Enable3DSFailService;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.Enable3DSResultRunner;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.Enable3DSResultService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Enable3DSMessaging {

    private static final Logger LOG = LoggerFactory.getLogger(Enable3DSMessaging.class);
    private static final String INITIAL_QUEUE_NAME = "checkout-cielo-enable-3ds-init";
    private static final String FAIL_QUEUE_NAME = "checkout-cielo-enable-3ds-fail";
    private static final String SUCCESS_QUEUE_NAME = "checkout-cielo-enable-3ds-success";

    private final CheckoutMerchantValidator validator;
    private final Enable3DSResultRunner enable3DSResultRunner;
    private final Enable3DSResultService enable3DSResultService;
    private final Enable3DSFailService enable3DSFailService;
    private final SqsTemplate sqsTemplate;

    public Enable3DSMessaging(
            CheckoutMerchantValidator validator,
            Enable3DSResultRunner enable3DSResultRunner,
            Enable3DSResultService enable3DSResultService,
            Enable3DSFailService enable3DSFailService,
            SqsTemplate sqsTemplate) {
        this.validator = validator;
        this.enable3DSResultRunner = enable3DSResultRunner;
        this.enable3DSResultService = enable3DSResultService;
        this.enable3DSFailService = enable3DSFailService;
        this.sqsTemplate = sqsTemplate;
    }

    @SqsListener(INITIAL_QUEUE_NAME)
    public void enable3dsInitListener(String ecNumber) {
        LOG.info("Received message for automation");
        if (validator.ecIsValid(ecNumber)) {
            enable3DSResultRunner
                    .singleEcRoutine(ecNumber)
                    .ifPresentOrElse(res -> sendResultMessage(res, ecNumber), () -> sendFailMessage(ecNumber));
            return;
        }
        sendFailMessage("111111111");
    }

    @SqsListener(SUCCESS_QUEUE_NAME)
    public void enable3dsSucessListener(String ec) {
        LOG.info("[{}] Received message for sucessful automation", ec);
        Enable3DSResult enable3DSResultRecord = new Enable3DSResult(ec, "3DS Habilitado");
        enable3DSResultService.save(enable3DSResultRecord);
        LOG.info("[{}] Saved the result", ec);
    }

    @SqsListener(FAIL_QUEUE_NAME)
    public void enable3dsFailListener(String ec) {
        LOG.info("[{}] Received message for fail automation", ec);
        enable3DSFailService.save(ec, "Erro de Execucao");
        LOG.info("[{}] Saved fail result", ec);
    }

    private void sendResultMessage(String result, String ec) {
        LOG.info("Validating result");
        if (result.equals("Sucess")) {
            LOG.info("[{}] Sending message to sucess queue '{}'", ec, SUCCESS_QUEUE_NAME);
            sendSqsMessageSucess(ec);
            return;
        }
        LOG.warn("[{}] Error during automation. Sending message to queue '{}'", ec, FAIL_QUEUE_NAME);
        sendFailMessage(ec);
    }

    private void sendFailMessage(String ec) {
        sendSqsMessageFail(ec);
    }

    private void sendSqsMessageSucess(String ec) {
        LOG.info("[{}] Attempting to send message for successful result", ec);
        try {
            sqsTemplate.send(to -> to.queue(SUCCESS_QUEUE_NAME).payload(ec).delaySeconds(10));
            LOG.info("[{}] Message Sent!", ec);
        } catch (Exception exception) {
            LOG.error("[{}] Error while attempting to send message", ec, exception);
        }
    }

    private void sendSqsMessageFail(String ec) {
        LOG.info("[{}] Attempting to send message for fail result", ec);
        try {
            sqsTemplate.send(to -> to.queue(FAIL_QUEUE_NAME).payload(ec).delaySeconds(10));
            LOG.info("[{}] Message Sent for Failed Automation!", ec);
        } catch (Exception exception) {
            LOG.error("[{}] Error occured while attempting to send message", ec, exception);
        }
    }
}
