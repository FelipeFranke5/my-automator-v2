package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantValidator;
import dev.franke.felipee.braspag_automator_v2.contracts.service.EcSearchMailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class CheckoutMailSender implements EcSearchMailSender {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutMailSender.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final JavaMailSender mailSender;
    private final CheckoutFileHandler fileHandler;
    private final MerchantValidator validator;

    public CheckoutMailSender(
            final JavaMailSender mailSender, CheckoutFileHandler fileHandler, MerchantValidator validator) {
        this.mailSender = mailSender;
        this.fileHandler = fileHandler;
        this.validator = validator;
    }

    @Override
    public byte sendEmailWithExcelResults(String emailAddress) {
        LOG.info("Initializing service - Write to Excel and Send Email");

        if (!validator.validEmail(emailAddress)) {
            LOG.warn("Email Address is not valid!");
            return 1;
        }

        asyncEmailWithExcelData(emailAddress);
        return 0;
    }

    private void asyncEmailWithExcelData(String emailAddress) {
        CompletableFuture.runAsync(
                () -> {
                    final Optional<byte[]> optionalExcelBytes = writeToExcel();
                    optionalExcelBytes.ifPresentOrElse(
                            excelBytes -> {
                                LOG.info("Result is an optional of byte[]");
                                LOG.info("Sending email with result");
                                sendEmail(excelBytes, emailAddress);
                            },
                            () -> {
                                LOG.warn("Result is an empty optional, which means an error has occured");
                                LOG.warn("Sending email to inform error");
                                sendEmail(emailAddress);
                            });
                },
                executor);
    }

    // In case of success
    private void sendEmail(byte[] excelBytes, String emailAddress) {
        sendEmailWithResults(excelBytes, emailAddress);
    }

    // In case of failure
    private void sendEmail(String emailAddress) {
        sendEmailInformingFailure(emailAddress);
    }

    private Optional<byte[]> writeToExcel() {
        try {
            final byte[] excelBytes = fileHandler.writeToExcelFile();
            return Optional.of(excelBytes);
        } catch (final IOException ioException) {
            LOG.error("Could not execute routine to write excel", ioException);
            return Optional.empty();
        }
    }

    private void sendEmailInformingFailure(String emailAddressTo) {
        LOG.info("Initialized service to send Email for failure result");

        try {
            // Mime Message
            final MimeMessage mimeMessage = mailSender.createMimeMessage();
            final MimeMessageHelper mimeHelper = new MimeMessageHelper(mimeMessage);

            // Email Configs
            final String subject = "[Falha] Automação Braspag - Consulta Checkout Cielo - ID: " + UUID.randomUUID();
            final String body = "A automação não foi concluída com sucesso. Tente novamente";
            mimeHelper.setTo(emailAddressTo);
            mimeHelper.setSubject(subject);
            mimeHelper.setText(body);

            // Send it
            mailSender.send(mimeMessage);
            LOG.info("Email informing failure Sent!");
        } catch (final MessagingException messagingException) {
            LOG.error("Could not Send Message!", messagingException);
        }
    }

    private void sendEmailWithResults(byte[] excelBytes, String emailAddressTo) {
        LOG.info("Initialized service to send Email for sucessful result");

        try {
            // Mime Message
            final MimeMessage mimeMessage = mailSender.createMimeMessage();
            final MimeMessageHelper mimeHelper = new MimeMessageHelper(mimeMessage, true);
            LOG.info("MimeMessage and MimeMessageHelper defined");

            // Email Configs
            final String subject = "Automação Braspag - Consulta Checkout Cielo - ID: " + UUID.randomUUID();
            final String body = "Segue em anexo o resultado da sua automação";
            mimeHelper.setTo(emailAddressTo);
            mimeHelper.setSubject(subject);
            mimeHelper.setText(body);
            LOG.info("Email Configuration Defined");

            // Attach Excel
            final String attachmentName = UUID.randomUUID() + ".xlsx";
            mimeHelper.addAttachment(attachmentName, new ByteArrayResource(excelBytes));
            LOG.info("Attachment Defined");

            // Send it
            mailSender.send(mimeMessage);
            LOG.info("Email Sent!");
        } catch (final MessagingException messagingException) {
            LOG.error("Could not Send Message!", messagingException);
        }
    }
}
