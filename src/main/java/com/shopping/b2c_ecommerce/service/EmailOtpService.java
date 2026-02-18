package com.shopping.b2c_ecommerce.service;

import com.shopping.b2c_ecommerce.exception.EmailOtpSendException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailOtpService {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpService.class);

    @Autowired
    private JavaMailSender mailSender;

    // email -> OTP details
    private final Map<String, OtpDetails> otpStore = new ConcurrentHashMap<>();

    // email -> verified-for-registration
    private final Set<String> verifiedEmails = ConcurrentHashMap.newKeySet();

    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_SECONDS = 5 * 60; // 5 minutes

    private SecureRandom secureRandom;

    @PostConstruct
    public void init() {
        secureRandom = new SecureRandom();
        log.info("EmailOtpService initialized");
    }

    // =========================
    // SEND OTP
    // =========================
    public void sendOtp(String email) {

        log.info("Send email OTP requested. email={}", email);

        String otp = generateOtp();

        otpStore.put(
                email,
                new OtpDetails(otp, Instant.now().plusSeconds(OTP_EXPIRY_SECONDS))
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP for Verification");
        message.setText(
                "Your OTP is: " + otp + "\n\n"
                        + "This OTP is valid for 5 minutes.\n"
                        + "Do not share this OTP with anyone."
        );

        try {
            mailSender.send(message);
            log.info("Email OTP sent successfully. email={}", email);
        } catch (Exception ex) {
            log.error("Failed to send email OTP. email={}", email, ex);
            throw new EmailOtpSendException(email);
        }
    }

    // =========================
    // VERIFY OTP (ONE-TIME)
    // =========================
    public boolean verifyOtp(String email, String otp) {

        log.debug("Verify email OTP attempt. email={}", email);

        OtpDetails details = otpStore.get(email);
        if (details == null) {
            log.warn("OTP verification failed. No OTP found. email={}", email);
            return false;
        }

        if (Instant.now().isAfter(details.expiryTime())) {
            otpStore.remove(email);
            log.warn("OTP verification failed. OTP expired. email={}", email);
            return false;
        }

        boolean valid = details.otp().equals(otp);

        if (valid) {
            otpStore.remove(email); // consume OTP
            log.info("OTP verified successfully. email={}", email);
        } else {
            log.warn("OTP verification failed. Invalid OTP. email={}", email);
        }

        return valid;
    }

    // =========================
    // REGISTRATION STATE (REQUIRED BY AuthService)
    // =========================
    public void markOtpVerified(String email) {
        verifiedEmails.add(email);
        log.debug("Email marked as OTP verified. email={}", email);
    }

    public boolean isOtpVerified(String email) {
        boolean verified = verifiedEmails.contains(email);
        log.debug("Check OTP verified state. email={}, verified={}", email, verified);
        return verified;
    }

    public void clearOtpState(String email) {
        verifiedEmails.remove(email);
        log.debug("Cleared OTP verification state. email={}", email);
    }

    // =========================
    // OTP GENERATION
    // =========================
    private String generateOtp() {

        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    // =========================
    // OTP RECORD
    // =========================
    private record OtpDetails(String otp, Instant expiryTime) {
    }
}