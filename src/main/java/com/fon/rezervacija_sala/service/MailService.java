package com.fon.rezervacija_sala.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mail;

    @Value("${app.mail.from}")
    private String from;

    public MailService(JavaMailSender mail) {
        this.mail = mail;
    }

    public void send(String to, String subject, String text) {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(to);
        m.setSubject(subject);
        m.setText(text);
        mail.send(m);
    }

    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage mm = mail.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(mm, "UTF-8");
            h.setFrom(from);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            mail.send(mm);
        } catch (MessagingException e) {
            throw new RuntimeException("Slanje HTML mejla neuspešno.", e);
        }
    }

}