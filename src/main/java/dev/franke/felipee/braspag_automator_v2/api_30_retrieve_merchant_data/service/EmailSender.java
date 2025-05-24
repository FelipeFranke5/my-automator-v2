package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
public class EmailSender {

  private static final Logger LOG = LoggerFactory.getLogger(EmailSender.class);
  private static final ExecutorService executor = Executors.newCachedThreadPool();

  private final JavaMailSender mailSender;

  public EmailSender(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendEmailInformingFailureAync(String emailAddressTo) {
    CompletableFuture.runAsync(
        () -> {
          try {
            // Mime Message
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeHelper = new MimeMessageHelper(mimeMessage);

            // Email Configs
            String subject =
                "[Falha] Automação Braspag - Consulta API 3.0 - ID: " + UUID.randomUUID();
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
        },
        executor);
  }

  public void sendEmailWithResultsAsync(byte[] excelBytes, String emailAddressTo) {
    CompletableFuture.runAsync(
        () -> {
          try {
            // Mime Message
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeHelper = new MimeMessageHelper(mimeMessage, true);

            // Email Configs
            String subject = "Automação Braspag - Consulta API 3.0 - ID: " + UUID.randomUUID();
            String body = "Segue em anexo o resultado da sua automação";
            mimeHelper.setTo(emailAddressTo);
            mimeHelper.setSubject(subject);
            mimeHelper.setText(body);

            // Attach Excel
            String attachmentName = "automacao-" + UUID.randomUUID() + ".xlsx";
            mimeHelper.addAttachment(attachmentName, new ByteArrayResource(excelBytes));

            // Send it
            mailSender.send(mimeMessage);
            LOG.info("Email Sent!");
          } catch (MessagingException messagingException) {
            LOG.error("Could not Send Message!", messagingException);
          }
        },
        executor);
  }
}
