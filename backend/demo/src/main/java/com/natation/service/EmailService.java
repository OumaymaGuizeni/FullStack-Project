package com.natation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendPasscode(String toEmail, String passcode) {
        System.out.println("--------------------------------------------------");
        System.out.println("SIMULATED EMAIL TO: " + toEmail);
        System.out.println("SUBJECT: Your Natation Portal Passcode");
        System.out.println("BODY: Your security passcode is: " + passcode);
        System.out.println("--------------------------------------------------");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("Your Natation Portal Passcode");
                message.setText("Welcome back! Your security passcode to dive into the portal is: " + passcode);
                mailSender.send(message);
            } catch (Exception e) {
                System.err.println("Real mail sending failed: " + e.getMessage());
            }
        }
    }
}
