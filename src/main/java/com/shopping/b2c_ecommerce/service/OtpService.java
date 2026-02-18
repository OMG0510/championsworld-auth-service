package com.shopping.b2c_ecommerce.service;

import com.shopping.b2c_ecommerce.exception.OtpSendFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    @Value("${msg91.auth-key}")
    private String authKey;

    @Value("${msg91.otp-template-id}")
    private String templateId;

    private final RestTemplate restTemplate = new RestTemplate();

    // =========================
    // LOCAL OTP VERIFICATION STATE
    // =========================
    private final Set<String> verifiedOtps = ConcurrentHashMap.newKeySet();

    // =========================
    // SEND OTP
    // =========================
    public void sendOtp(String mobile) {

        log.info("Send OTP request initiated. mobile={}", mobile);

        try {
            String url = "https://api.msg91.com/api/v5/otp";

            HttpHeaders headers = new HttpHeaders();
            headers.set("authkey", authKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "mobile", "91" + mobile,
                    "template_id", templateId,
                    "otp_expiry", 5
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, entity, String.class);

            if (response.getBody() == null || !response.getBody().contains("success")) {
                log.error("MSG91 OTP send failed. mobile={}", mobile);
                throw new OtpSendFailedException();
            }

            log.info("OTP sent successfully via MSG91. mobile={}", mobile);

        } catch (Exception ex) {
            log.error("MSG91 OTP service error. mobile={}", mobile, ex);
            throw new OtpSendFailedException();
        }
    }


    // =========================
    // VERIFY OTP (MSG91)
    // =========================
    public boolean verifyOtp(String mobile, String otp) {

        log.info("Verify OTP request initiated. mobile={}", mobile);

        // REAL MSG91 VERIFICATION (unchanged)
        String url = "https://api.msg91.com/api/v5/otp/verify";

        HttpHeaders headers = new HttpHeaders();
        headers.set("authkey", authKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "mobile", "91" + mobile,
                "otp", otp
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        boolean success = response.getBody() != null && response.getBody().contains("success");

        if (success) {
            log.info("OTP verified successfully via MSG91. mobile={}", mobile);
        } else {
            log.warn("OTP verification failed via MSG91. mobile={}", mobile);
        }

        return success;
    }

    // =========================
    // OTP STATE MANAGEMENT (REQUIRED BY AuthService)
    // =========================
    public void markOtpVerified(String mobile) {
        verifiedOtps.add(mobile);
        log.debug("OTP marked as verified locally. mobile={}", mobile);
    }

    public boolean isOtpVerified(String mobile) {
        boolean verified = verifiedOtps.contains(mobile);
        log.debug("Check local OTP verified state. mobile={}, verified={}", mobile, verified);
        return verified;
    }

    public void clearOtpState(String mobile) {
        verifiedOtps.remove(mobile);
        log.debug("Cleared local OTP verification state. mobile={}", mobile);
    }
}