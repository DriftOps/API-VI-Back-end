package com.xertica.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Envia um e-mail HTML
     * 
     * @param to      E-mail do destinatário
     * @param subject Assunto do e-mail
     * @param body    Corpo em HTML do e-mail
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("E-mail enviado para: " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao enviar e-mail", e);
        }
    }
}
