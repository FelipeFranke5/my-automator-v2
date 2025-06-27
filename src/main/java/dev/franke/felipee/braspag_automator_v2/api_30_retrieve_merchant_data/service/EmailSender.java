package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.EmptyQueryException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.InvalidEmailException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.repository.MerchantRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    private static final Logger LOG = LoggerFactory.getLogger(EmailSender.class.getName());
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MerchantValidator validator;

    @Autowired
    private AutomationFileHandler fileHandler;

    public void sendEmailWithExcelResults(String emailAddress) {
        assertEmailIsValid(emailAddress);
        assertMerchantsAreNotEmpty();
        handleExcelEmailTask(emailAddress);
    }

    private void assertEmailIsValid(String email) {
        if (!validator.validEmail(email)) {
            throw new InvalidEmailException("Email is not valid");
        }
    }

    private void assertMerchantsAreNotEmpty() {
        List<Merchant> merchants = merchantRepository.findAll();
        if (merchants.isEmpty()) {
            throw new EmptyQueryException("Merchant query is empty");
        }
    }

    public void sendEmailInformingFailure(String emailAddressTo) {
        LOG.info("Initialized service to send Email for failure result");
        try {
            // Mime Message
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeHelper = new MimeMessageHelper(mimeMessage);

            // Email Configs
            String subject = "[Falha] Automação Braspag - Consulta API 3.0 - ID: " + UUID.randomUUID();
            String body = "A automação não foi concluída com sucesso. Tente novamente";
            mimeHelper.setTo(emailAddressTo);
            mimeHelper.setSubject(subject);
            mimeHelper.setText(body);

            // Send it
            mailSender.send(mimeMessage);
            LOG.info("Email informing failure Sent!");
        } catch (MessagingException messagingException) {
            LOG.error("Could not Send Message!", messagingException);
        }
    }

    private void sendEmailWithResults(byte[] excelBytes, String emailAddressTo) {
        LOG.info("Initialized service to send Email for sucessful result");
        try {
            // Mime Message
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeHelper = new MimeMessageHelper(mimeMessage, true);
            LOG.info("MimeMessage and MimeMessageHelper defined");

            // Email Configs
            String subject = "Automação Braspag - Consulta API 3.0 - ID: " + UUID.randomUUID();
            String body = "Segue em anexo o resultado da sua automação";
            mimeHelper.setTo(emailAddressTo);
            mimeHelper.setSubject(subject);
            mimeHelper.setText(body);
            LOG.info("Email Configuration Defined");

            // Attach Excel
            String attachmentName = UUID.randomUUID() + ".xlsx";
            mimeHelper.addAttachment(attachmentName, new ByteArrayResource(excelBytes));
            LOG.info("Attachment Defined");

            // Send it
            mailSender.send(mimeMessage);
            LOG.info("Email Sent!");
        } catch (MessagingException messagingException) {
            LOG.error("Could not Send Message!", messagingException);
        }
    }

    private void handleExcelEmailTask(String emailAddress) {
        CompletableFuture.runAsync(
                () -> {
                    Optional<byte[]> optionalExcelBytes = writeToExcel();

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

    private Optional<byte[]> writeToExcel() {
        try {
            byte[] excelBytes = fileHandler.writeToExcelFile();
            return Optional.of(excelBytes);
        } catch (IOException ioException) {
            LOG.error("Could not execute routine to write excel", ioException);
            return Optional.empty();
        }
    }

    private void sendEmail(byte[] excelBytes, String emailAddress) {
        sendEmailWithResults(excelBytes, emailAddress);
    }

    private void sendEmail(String emailAddress) {
        sendEmailInformingFailure(emailAddress);
    }
}
