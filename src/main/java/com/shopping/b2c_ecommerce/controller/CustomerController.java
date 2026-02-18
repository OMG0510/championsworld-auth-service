package com.shopping.b2c_ecommerce.controller;

import com.shopping.b2c_ecommerce.dto.*;
import com.shopping.b2c_ecommerce.security.JwtUtil;
import com.shopping.b2c_ecommerce.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public CustomerController(AuthService authService, JwtUtil jwtUtil)
    {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    //*************************************
    // Phone Number based OTP Login Process
    //*************************************

    // LOGIN → SEND OTP (Phone Number)
    @PostMapping("/login/otp/phone/send")
    public ResponseEntity<?> sendOtp(@RequestBody OtpSendRequest req)
    {
        log.info("Send phone login OTP request. mobile={}", req.getMobile());

        if (req.getMobile() == null || req.getMobile().isEmpty())
        {
            log.warn("Send phone OTP failed. Mobile number is empty");
            return ResponseEntity.badRequest().body("Mobile number is required");
        }

        authService.sendLoginOtp(req.getMobile());
        log.info("Phone login OTP sent successfully. mobile={}", req.getMobile());

        return ResponseEntity.ok("OTP sent");
    }

    // LOGIN → VERIFY OTP (Phone Number)
    @PostMapping("/login/otp/phone/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest req)
    {
        log.info("Verify phone login OTP request. mobile={}", req.getMobile());

        if (req.getMobile() == null || req.getMobile().isEmpty())
        {
            log.warn("Verify phone OTP failed. Mobile number is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mobile number is required");
        }

        if (req.getOtp() == null || req.getOtp().isEmpty())
        {
            log.warn("Verify phone OTP failed. OTP is empty. mobile={}", req.getMobile());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP is required");
        }

        UserIdentity identity = authService.verifyOtpAndLogin(req.getMobile(), req.getOtp());
        log.info("Phone OTP login successful. userId={}", identity.getUserId());

        return ResponseEntity.ok(new LoginResponse(identity.getUserId(), identity.getEmail(), jwtUtil.generateToken(identity)));
    }

    //*******************************
    // Email based OTP Login Process
    //*******************************

    // LOGIN → SEND OTP (Email)
    @PostMapping("/login/otp/email/send")
    public ResponseEntity<?> sendEmailLoginOtp(@RequestBody EmailOtpSendRequest req)
    {
        log.info("Send email login OTP request. email={}", req.getEmail());

        if (req.getEmail() == null || req.getEmail().isEmpty())
        {
            log.warn("Send email OTP failed. Email is empty");
            return ResponseEntity.badRequest().body("Email address is required");
        }

        authService.sendEmailLoginOtp(req.getEmail());
        log.info("Email login OTP sent successfully. email={}", req.getEmail());

        return ResponseEntity.ok("Email OTP sent");
    }

    // LOGIN → VERIFY OTP (Email)
    @PostMapping("/login/otp/email/verify")
    public ResponseEntity<?> verifyEmailLoginOtp(@RequestBody EmailOtpVerifyRequest req)
    {
        log.info("Verify email login OTP request. email={}", req.getEmail());

        if (req.getEmail() == null || req.getEmail().isEmpty())
        {
            log.warn("Verify email OTP failed. Email is empty");
            return ResponseEntity.badRequest().body("Email address is required");
        }

        if (req.getOtp() == null || req.getOtp().isEmpty())
        {
            log.warn("Verify email OTP failed. OTP is empty. email={}", req.getEmail());
            return ResponseEntity.badRequest().body("OTP is required");
        }

        UserIdentity identity = authService.verifyEmailOtpAndLogin(req.getEmail(), req.getOtp());
        log.info("Email OTP login successful. userId={}", identity.getUserId());

        return ResponseEntity.ok(new LoginResponse(identity.getUserId(), identity.getEmail(), jwtUtil.generateToken(identity)));
    }

    //**********************************
    //  Email based Registration Process
    //**********************************

    // REGISTER -> SEND OTP -> EMAIL
    @PostMapping("/register/email/start")
    public ResponseEntity<?> startEmailRegistration(@RequestBody EmailOtpSendRequest req)
    {
        log.info("Start email registration request. email={}", req.getEmail());

        if (req.getEmail() == null || req.getEmail().isEmpty())
        {
            log.warn("Start email registration failed. Email is empty");
            return ResponseEntity.badRequest().body("Email address is required");
        }

        authService.startEmailRegistration(req.getEmail());
        log.info("Email registration OTP sent. email={}", req.getEmail());

        return ResponseEntity.ok("Email OTP sent");
    }

    // REGISTER -> VERIFY OTP -> EMAIL
    @PostMapping("/register/email/verify")
    public ResponseEntity<?> verifyEmailRegistrationOtp(@RequestBody EmailOtpVerifyRequest req)
    {
        log.info("Verify email registration OTP request. email={}", req.getEmail());

        if (req.getEmail() == null || req.getEmail().isEmpty())
        {
            log.warn("Verify email registration failed. Email is empty");
            return ResponseEntity.badRequest().body("Email address is required");
        }

        if (req.getOtp() == null || req.getOtp().isEmpty())
        {
            log.warn("Verify email registration failed. OTP is empty. email={}", req.getEmail());
            return ResponseEntity.badRequest().body("OTP is required");
        }

        authService.verifyEmailRegistrationOtp(req.getEmail(), req.getOtp());
        log.info("Email registration OTP verified. email={}", req.getEmail());

        return ResponseEntity.ok("Email verified");
    }

    // REGISTER -> COMPLETE REGISTRATION (EMAIL)
    @PostMapping("/register/email/complete")
    public ResponseEntity<?> completeEmailRegistration(@RequestBody EmailOtpSendRequest req)
    {
        log.info("Complete email registration request. email={}", req.getEmail());

        if (req.getEmail() == null || req.getEmail().isEmpty())
        {
            log.warn("Complete email registration failed. Email is empty");
            return ResponseEntity.badRequest().body("Email address is required");
        }

        UserIdentity identity = authService.completeEmailRegistration(req.getEmail());
        String token = jwtUtil.generateToken(identity);

        log.info("Email registration completed successfully. userId={}", identity.getUserId());

        return ResponseEntity.ok(new LoginResponse(identity.getUserId(), identity.getEmail(), token));
    }

    // REGISTER → SEND OTP -> MOBILE
    @PostMapping("/register/phone/start")
    public ResponseEntity<?> startRegistration(@RequestBody RegisterStepRequest req)
    {
        log.info("Start mobile registration request. mobile={}", req.getMobile());

        if (req.getMobile() == null || req.getMobile().isEmpty())
        {
            log.warn("Start mobile registration failed. Mobile is empty");
            return ResponseEntity.badRequest().body("Mobile number is required");
        }

        authService.startRegistration(req.getMobile());
        log.info("Mobile registration OTP sent. mobile={}", req.getMobile());

        return ResponseEntity.ok("OTP sent for verification");
    }

    // REGISTER -> VERIFY OTP -> MOBILE
    @PostMapping("/register/phone/verify")
    public ResponseEntity<?> verifyRegistrationOtp(@RequestBody OtpVerifyRequest req)
    {
        log.info("Verify mobile registration OTP request. mobile={}", req.getMobile());

        if (req.getMobile() == null || req.getMobile().isEmpty())
        {
            log.warn("Verify mobile registration failed. Mobile is empty");
            return ResponseEntity.badRequest().body("Mobile number is required");
        }

        if (req.getOtp() == null || req.getOtp().isEmpty())
        {
            log.warn("Verify mobile registration failed. OTP is empty. mobile={}", req.getMobile());
            return ResponseEntity.badRequest().body("OTP is required");
        }

        authService.verifyRegistrationOtp(req.getMobile(), req.getOtp());
        log.info("Mobile registration OTP verified. mobile={}", req.getMobile());

        return ResponseEntity.ok("Verification successful");
    }

    // REGISTER -> COMPLETE REGISTRATION
    @PostMapping("/register/phone/complete")
    public ResponseEntity<?> completeRegistration(@RequestBody RegisterStepRequest req)
    {
        log.info("Complete mobile registration request. mobile={}", req.getMobile());

        if (req.getMobile() == null || req.getMobile().isEmpty())
        {
            log.warn("Complete mobile registration failed. Mobile is empty");
            return ResponseEntity.badRequest().body("Mobile number is required");
        }

        UserIdentity identity = authService.completeRegistration(req.getMobile());
        String token = jwtUtil.generateToken(identity);

        log.info("Mobile registration completed successfully. userId={}", identity.getUserId());

        return ResponseEntity.ok(new LoginResponse(identity.getUserId(), identity.getEmail(), token));
    }

    // GOOGLE LOGIN (Customer)
    //https://accounts.google.com/o/oauth2/v2/auth?client_id=96478589645-mvurnhv2nl39njr4eoi8ao53i49mh84o.apps.googleusercontent.com&redirect_uri=http://localhost:3000/oauth/callback&response_type=code&scope=email%20profile
    @PostMapping("/oauth/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleCodeRequest request)
    {
        log.info("Google login request received");

        if (request.getCode() == null || request.getCode().isEmpty())
        {
            log.warn("Google login failed. Code is empty");
            return ResponseEntity.badRequest().body("Code is required");
        }

        UserIdentity identity = authService.authenticateGoogle(request.getCode());
        String token = jwtUtil.generateToken(identity);

        log.info("Google login successful. userId={}", identity.getUserId());

        return ResponseEntity.ok(new LoginResponse(identity.getUserId(), identity.getEmail(), token));
    }

    // ME
    @GetMapping("/me")
    public ResponseEntity<?> me()
    {
        log.debug("Me endpoint called");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserIdentity identity))
        {
            log.warn("Me endpoint unauthorized access");
            return ResponseEntity.status(401).body("Unauthorized User!");
        }

        log.debug("Me endpoint success. userId={}", identity.getUserId());

        return ResponseEntity.ok(
                new MeResponse(
                        identity.getUserId(),
                        identity.getEmail(),
                        identity.getRole()
                )
        );
    }
}