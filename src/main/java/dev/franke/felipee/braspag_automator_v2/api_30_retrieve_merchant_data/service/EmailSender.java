package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

  private static final Logger LOG = LoggerFactory.getLogger(EmailSender.class);

  private final JavaMailSender mailSender;

  public EmailSender(JavaMailSender mailSender) {
    this.mailSender = mailSender;
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

  public void sendEmailWithResults(byte[] excelBytes, String emailAddressTo) {
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
}
