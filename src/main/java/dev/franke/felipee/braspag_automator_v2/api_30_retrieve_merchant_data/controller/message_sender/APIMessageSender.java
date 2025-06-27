package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.message_sender;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.EmptyQueryException;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class APIMessageSender {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);
    private static final String INITIAL_AUTOMATION_QUEUE_NAME = "api30-init";
    private static final Logger LOG = LoggerFactory.getLogger(APIMessageSender.class.getName());

    @Autowired
    private SqsTemplate sqsTemplate;

    public void sendMessage(String ec) {
        LOG.info("[{}] Attempting to send message in order to initialize automation", ec);

        try {
            sqsTemplate.send(
                    to -> to.queue(INITIAL_AUTOMATION_QUEUE_NAME).payload(ec).delaySeconds(2));
            LOG.info("[{}] Message sent", ec);
        } catch (Exception exception) {
            LOG.error(
                    "[{}] Error while attempting to send message to queue '{}'",
                    ec,
                    INITIAL_AUTOMATION_QUEUE_NAME,
                    exception);
        }
    }

    public void sendMessageForECs(String[] ecs) {
        if (ecs.length == 0) {
            throw new EmptyQueryException("Merchant List is empty");
        }

        CompletableFuture.runAsync(
                () -> {
                    for (String ec : ecs) {
                        sendMessage(ec);
                    }
                },
                EXECUTOR_SERVICE);
    }
}
