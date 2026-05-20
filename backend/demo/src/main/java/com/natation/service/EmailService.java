package com.natation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public void sendPasscode(String toEmail, String passcode) {
        System.out.println("--------------------------------------------------");
        System.out.println("Sending Natation Portal passcode email...");
        System.out.println("FROM: " + mailUsername);
        System.out.println("TO: " + toEmail);
        System.out.println("SUBJECT: Your Natation Portal Passcode");
        System.out.println("--------------------------------------------------");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setFrom(mailUsername);
                message.setReplyTo(mailUsername);
                message.setSubject("Your Natation Portal Passcode");
                message.setText("Welcome back! Your security passcode to dive into the portal is: " + passcode + "\n\nIf you did not request this, please ignore this email.");
                mailSender.send(message);
                System.out.println("Real email sent successfully to " + toEmail + ".");
            } catch (Exception e) {
                System.out.println("Real mail sending failed: " + e.getMessage());
                System.out.println("\n====================================================================");
                System.out.println("[SMTP CONFIGURATION CHECK]");
                System.out.println("Verify backend/demo/smtp.properties contains valid Gmail SMTP credentials.");
                System.out.println("\n💡  HOW TO GET A GMAIL APP PASSWORD:");
                System.out.println("1. Go to Google Account -> Security");
                System.out.println("2. Enable 2-Step Verification");
                System.out.println("3. Go to 'App passwords' (https://myaccount.google.com/apppasswords)");
                System.out.println("4. Generate a password for your App and paste it without spaces.");
                System.out.println("====================================================================\n");
            }
        } else {
            System.out.println("JavaMailSender is not initialized. Please verify configuration properties.");
        }
    }
}
