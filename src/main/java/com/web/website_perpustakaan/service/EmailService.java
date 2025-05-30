package com.web.website_perpustakaan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("Perpustakaan Online <no-reply@perpustakaan.com>");
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Gagal mengirim email: " + e.getMessage(), e);
        }
    }
}
