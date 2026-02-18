package com.shopping.b2c_ecommerce.controller;

import com.shopping.b2c_ecommerce.dto.*;
import com.shopping.b2c_ecommerce.entity.User;
import com.shopping.b2c_ecommerce.security.JwtUtil;
import com.shopping.b2c_ecommerce.service.AuthService;
import com.shopping.b2c_ecommerce.service.PasswordResetService;
import com.shopping.b2c_ecommerce.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordResetService passwordResetService;


    public AdminController(AuthService authService, JwtUtil jwtUtil, UserService userService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    // REGISTER ADMIN (SUPER_ADMIN only)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request)
    {
        log.info("Admin registration request received. email={}", request.getEmail());

        if (request.getPassword() == null || request.getPassword().length() < 8)
        {
            log.warn("Admin registration failed. Invalid password length. email={}", request.getEmail());
            return ResponseEntity.badRequest().body("Password must be at least 8 characters");
            //throw new RuntimeException("Password must be at least 8 characters");
        }

        authService.registerAdmin(request.getEmail(), request.getPassword());
        log.info("Admin registered successfully. email={}", request.getEmail());

        return ResponseEntity.ok("Admin registered successfully");
    }

    // ADMIN LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request)
    {
        log.info("Admin login attempt. email={}", request.getEmail());

        if (request.getEmail() == null || request.getPassword() == null)
        {
            log.warn("Login failed. Empty credentials");
            return ResponseEntity.badRequest().body("Empty credentials");
        }

        var identity = authService.authenticate(request.getEmail(), request.getPassword());
        if (identity == null)
        {
            log.warn("Login failed. Invalid credentials. email={}", request.getEmail());
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.getActive())
        {
            log.warn("Login blocked. Account inactive. userId={}", user.getId());
            return ResponseEntity.badRequest().body("Account is inactive");
        }

        String token = jwtUtil.generateToken(identity);
        log.info("Admin login successful. userId={}", user.getId());

        return ResponseEntity.ok(new LoginResponse(identity.getUserId(), identity.getEmail(), token));
    }

    // ADMIN FORGOT PASSWORD
    @PostMapping("/forgot-password")
    public ResponseEntity<?> adminForgotPassword(@RequestBody ForgotPasswordRequest request)
    {
        log.info("Admin forgot password request received. email={}", request.getEmail());

        if (request.getEmail() == null || request.getEmail().isEmpty())
        {
            log.warn("Admin forgot password failed. Email is empty");
            return ResponseEntity.badRequest().body("Email is required");
        }

        passwordResetService.adminForgotPassword(request.getEmail());
        log.info("Admin forgot password flow triggered. email={}", request.getEmail());

        return ResponseEntity.ok("Password reset OTP sent to your email");
    }

    // ADMIN RESET PASSWORD
    @PostMapping("/reset-password")
    public ResponseEntity<?> adminResetPassword(@RequestBody ResetPasswordRequest request)
    {
        log.info("Admin reset password request received. email={}", request.getEmail());

        if (request.getEmail() == null || request.getEmail().isEmpty()
                || request.getNewPassword() == null || request.getNewPassword().isEmpty()
                || request.getConfirmPassword() == null || request.getConfirmPassword().isEmpty())
        {
            log.warn("Admin reset password failed. One or more fields empty. email={}", request.getEmail());
            return ResponseEntity.badRequest().body("All fields are required");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword()))
        {
            log.warn("Admin reset password failed. Password mismatch. email={}", request.getEmail());
            return ResponseEntity.badRequest().body("Passwords do not match");
        }

        if (request.getNewPassword().length() < 8)
        {
            log.warn("Admin reset password failed. Password too short. email={}", request.getEmail());
            return ResponseEntity.badRequest().body("Password must be at least 8 characters");
        }

        passwordResetService.adminResetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        log.info("Admin password reset successful. email={}", request.getEmail());
        return ResponseEntity.ok("Admin password reset successful");
    }

    // CHANGE ADMIN ACTIVE STATUS (SUPER_ADMIN only)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/admin/{adminId}/status")
    public ResponseEntity<?> updateAdminStatus(
            @PathVariable Long adminId,
            @RequestBody AdminStatusRequest request)
    {
        log.info("Admin status update request. adminId={}, active={}", adminId, request.isActive());

        authService.updateAdminActiveStatus(adminId, request.isActive());

        log.info("Admin status updated successfully. adminId={}", adminId);
        return ResponseEntity.ok("Admin status updated successfully");
    }

    // Show all admins
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/show-admins")
    public ResponseEntity<List<AdminSummaryResponse>> showAdmins()
    {
        log.info("Fetch all admins request received");

        List<AdminSummaryResponse> admins = userService.getAllAdmins();
        log.info("Admins fetched successfully. count={}", admins.size());

        return ResponseEntity.ok(admins);
    }

    // Hard Delete an ADMIN
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/admin/delete/{adminId}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long adminId)
    {
        log.info("SUPER_ADMIN requested hard delete for ADMIN with id={}", adminId);

        authService.hardDeleteAdmin(adminId);

        log.info("ADMIN with id={} deleted permanently by SUPER_ADMIN", adminId);

        return ResponseEntity.ok(Map.of("message", "Admin deleted permanently"));
    }

}
