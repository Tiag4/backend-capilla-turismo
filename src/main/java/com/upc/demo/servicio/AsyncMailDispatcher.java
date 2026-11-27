package com.upc.demo.servicio;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncMailDispatcher {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Async
    public void sendHtmlAsync(String fromAddress, String fromName, String recipient, String subject, String htmlBody, String contextLabel) {
        sendHtmlSync(fromAddress, fromName, recipient, subject, htmlBody, contextLabel);
    }

    public boolean sendHtmlSync(String fromAddress, String fromName, String recipient, String subject, String htmlBody, String contextLabel) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("JavaMailSender no disponible. Correo [{}] no enviado a {}", contextLabel, recipient);
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(fromAddress, fromName, StandardCharsets.UTF_8.name()));
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Correo [{}] enviado exitosamente a {}", contextLabel, recipient);
            return true;
        } catch (MailException | jakarta.mail.MessagingException | java.io.UnsupportedEncodingException exception) {
            log.error("No se pudo enviar correo [{}] a {}: {}", contextLabel, recipient, exception.getMessage());
            return false;
        }
    }
}
