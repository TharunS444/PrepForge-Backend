package com.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url:}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

        // 1. ALWAYS print the reset link to the console for development/troubleshooting convenience
        log.info("=========================================================================");
        log.info("PASSWORD RESET LINK GENERATED:");
        log.info("Send To: {}", toEmail);
        log.info("Click here to reset: {}", resetUrl);
        log.info("=========================================================================");

        // 2. Attempt actual email delivery via Gmail SMTP
        if (mailSender != null) {
            try {
                log.info("Attempting to send password reset email to {} via Gmail SMTP...", toEmail);
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("PrepForge Password Reset");
                message.setText(
                    "Hello,\n\n" +
                    "We received a request to reset your password.\n\n" +
                    "Click the link below:\n" +
                    resetUrl + "\n\n" +
                    "This link expires in 15 minutes.\n\n" +
                    "If you did not request this, ignore this email."
                );
                mailSender.send(message);
                log.info("Password reset email successfully sent to {}", toEmail);
            } catch (Exception e) {
                log.error("=========================================================================");
                log.error("SMTP EMAIL DELIVERY FAILED!");
                log.error("Target Recipient: {}", toEmail);
                log.error("Exact Error: {}", e.getMessage(), e);
                log.error("--------------------- TROUBLESHOOTING GUIDE -----------------------------");
                log.error("1. Did you configure real Gmail credentials in application.properties?");
                log.error("   Current configuration has: spring.mail.username / spring.mail.password");
                log.error("2. Since May 2022, Google does NOT support 'Less Secure Apps'. You must use an App Password.");
                log.error("   To create one: Go to Google Account -> Security -> 2-Step Verification -> App Passwords.");
                log.error("3. Ensure 2-Step Verification is enabled on your Google Account.");
                log.error("4. Check your internet connection or network firewall if connection timed out.");
                log.error("=========================================================================");
            }
        } else {
            log.warn("JavaMailSender bean is not configured. Email delivery was skipped.");
        }
    }
}
