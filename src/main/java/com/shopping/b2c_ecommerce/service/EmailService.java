package com.shopping.b2c_ecommerce.service;

import com.shopping.b2c_ecommerce.exception.EmailSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp)
    {
        log.info("Sending password reset OTP email. toEmail={}", toEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP");
            message.setText(
                    "Your OTP for password reset is: " + otp
                            + "\n\nThis OTP is valid for 5 minutes."
            );

            mailSender.send(message);

            log.info("Password reset OTP email sent successfully. toEmail={}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send password reset email. toEmail={}", toEmail, ex);
            throw new EmailSendException(toEmail);
        }
    }
    /**
     * Send confirmation email after successful password reset
     */
    public void sendPasswordResetConfirmationEmail(String toEmail, String role)
    {
        log.info("Sending password reset confirmation email. toEmail={}, role={}", toEmail, role);

        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
            String timestamp = now.format(formatter);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Password Reset Successful - Champions World");
            message.setText(
                    "Hello,\n\n"
                            + "Your " + role + " account password has been reset successfully.\n\n"
                            + "Reset Details:\n"
                            + "- Email: " + toEmail + "\n"
                            + "- Role: " + role + "\n"
                            + "- Date & Time: " + timestamp + "\n\n"
                            + "If you did not perform this action, please contact support immediately.\n\n"
                            + "For security reasons, we recommend:\n"
                            + "1. Use a strong, unique password\n"
                            + "2. Do not share your password with anyone\n"
                            + "3. Enable two-factor authentication if available\n\n"
                            + "Thank you,\n"
                            + "Champions World Security Team"
            );

            mailSender.send(message);

            log.info("Password reset confirmation email sent successfully. toEmail={}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send password reset confirmation email. toEmail={}", toEmail, ex);
            // Don't throw exception - confirmation email failure shouldn't break password reset
            // Password reset already succeeded, just log the error
        }
    }
}