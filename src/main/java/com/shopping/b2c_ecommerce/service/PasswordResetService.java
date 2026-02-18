package com.shopping.b2c_ecommerce.service;

import com.shopping.b2c_ecommerce.entity.PasswordResetToken;
import com.shopping.b2c_ecommerce.entity.User;
import com.shopping.b2c_ecommerce.entity.UserRole;
import com.shopping.b2c_ecommerce.exception.*;
import com.shopping.b2c_ecommerce.repository.PasswordResetTokenRepository;
import com.shopping.b2c_ecommerce.repository.UserRepository;
import com.shopping.b2c_ecommerce.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRoleRepository userRoleRepository;

    // =========================
    // ADMIN FORGOT PASSWORD
    // =========================
    public void adminForgotPassword(String email)
    {
        log.info("Admin forgot password request received. email={}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);

        //  If email does not exist - throw exception
        if (userOpt.isEmpty())
        {
            log.warn("Admin forgot password failed. Email not found: {}", email);
            throw new PasswordResetUserNotFoundException();
        }

        User user = userOpt.get();

        //  VALIDATE USER IS ADMIN OR SUPER_ADMIN
        Optional<UserRole> userRoleOpt = userRoleRepository.findByUser(user);

        if (userRoleOpt.isEmpty())
        {
            log.warn("Admin forgot password failed. User has no role. userId={}", user.getId());
            throw new RoleNotAssignedException();
        }

        String roleName = userRoleOpt.get().getRole().getName();

        if (!roleName.equals("ADMIN") && !roleName.equals("SUPER_ADMIN"))
        {
            log.warn("Admin forgot password failed. User is not an admin. userId={}, role={}",
                    user.getId(), roleName);
            throw new UserNotAdminException();
        }

        log.info("Admin role validated. userId={}, role={}", user.getId(), roleName);

        // Generate OTP
        String otp = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setOtpHash(passwordEncoder.encode(otp));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // 15 min for admins
        token.setUsed(false);

        tokenRepository.save(token);
        log.info("Admin password reset token created. userId={}", user.getId());

        // Send email
        emailService.sendOtpEmail(user.getEmail(), otp);
        log.info("Admin password reset OTP email sent. userId={}", user.getId());
    }

    // =========================
    // ADMIN RESET PASSWORD
    // =========================
    public void adminResetPassword(String email, String otp, String newPassword, String confirmPassword)
    {
        log.info("Admin reset password request received. email={}", email);

        if (!newPassword.equals(confirmPassword))
        {
            log.warn("Admin reset password failed. Passwords do not match. email={}", email);
            throw new PasswordMismatchException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(PasswordResetUserNotFoundException::new);

        //  VALIDATE USER IS ADMIN OR SUPER_ADMIN
        Optional<UserRole> userRoleOpt = userRoleRepository.findByUser(user);

        if (userRoleOpt.isEmpty())
        {
            log.warn("Admin reset password failed. User has no role. userId={}", user.getId());
            throw new PasswordResetUserNotFoundException();
        }

        String roleName = userRoleOpt.get().getRole().getName();

        if (!roleName.equals("ADMIN") && !roleName.equals("SUPER_ADMIN"))
        {
            log.warn("Admin reset password failed. User is not an admin. userId={}, role={}",
                    user.getId(), roleName);
            throw new PasswordResetUserNotFoundException(); // Do not reveal role mismatch
        }

        log.info("Admin role validated for password reset. userId={}, role={}", user.getId(), roleName);

        // Validate OTP
        PasswordResetToken token = tokenRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(PasswordResetTokenNotFoundException::new);

        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now()))
        {
            log.warn("Admin reset password failed. OTP expired or already used. userId={}", user.getId());
            throw new PasswordResetTokenInvalidException("OTP expired or already used");
        }

        if (!passwordEncoder.matches(otp, token.getOtpHash()))
        {
            log.warn("Admin reset password failed. Invalid OTP. userId={}", user.getId());
            throw new PasswordResetTokenInvalidException("Invalid OTP");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Admin password reset successful. userId={}", user.getId());

        // ✅ Send confirmation email
        try {
            emailService.sendPasswordResetConfirmationEmail(user.getEmail(), roleName);
            log.info("Password reset confirmation email sent. userId={}", user.getId());
        } catch (Exception ex) {
            log.warn("Failed to send password reset confirmation email. userId={}", user.getId(), ex);
            // Don't fail the reset if email fails - password already changed
        }
    }
}