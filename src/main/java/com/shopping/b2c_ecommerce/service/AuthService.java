package com.shopping.b2c_ecommerce.service;

import com.shopping.b2c_ecommerce.dto.*;
import com.shopping.b2c_ecommerce.entity.Role;
import com.shopping.b2c_ecommerce.entity.User;
import com.shopping.b2c_ecommerce.entity.UserRole;
import com.shopping.b2c_ecommerce.enums.AuthProvider;
import com.shopping.b2c_ecommerce.exception.*;
import com.shopping.b2c_ecommerce.repository.UserRepository;
import com.shopping.b2c_ecommerce.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final RoleService roleService;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final GoogleOAuthService googleOAuthService;
    private final OtpService otpService;
    private final EmailOtpService emailOtpService;
    private final UserRepository userRepository;

    public AuthService(
            UserService userService,
            RoleService roleService,
            UserRoleRepository userRoleRepository,
            BCryptPasswordEncoder passwordEncoder,
            GoogleOAuthService googleOAuthService,
            OtpService otpService,
            EmailOtpService emailOtpService,
            UserRepository userRepository
    ) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.googleOAuthService = googleOAuthService;
        this.otpService = otpService;
        this.emailOtpService = emailOtpService;
        this.userRepository = userRepository;
    }

    // =========================
    // ADMIN REGISTRATION
    // =========================
    public void registerAdmin(String email, String rawPassword) {

        log.info("Admin registration attempt. email={}", email);

        if (userService.existsByEmail(email)) {
            log.warn("Admin registration failed. Email already exists: {}", email);
            throw new UserAlreadyExistsException("email");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setProvider(AuthProvider.LOCAL.name());

        User savedUser = userService.createUser(user);
        log.info("Admin user created. userId={}", savedUser.getId());

        assignRole(savedUser, "ADMIN");
    }

    // =========================
    // PASSWORD LOGIN
    // =========================
    public UserIdentity authenticate(String email, String rawPassword) {

        log.info("Password login attempt. email={}", email);

        User user = userService.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        validateActiveUser(user);

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("Password login failed. Password mismatch. email={}", email);
            throw new InvalidCredentialsException();
        }

        log.info("Password login successful. userId={}", user.getId());
        return buildIdentity(user);
    }

    // =========================
    // GOOGLE LOGIN
    // =========================
    public UserIdentity authenticateGoogle(String code) {

        log.info("Google login started");

        GoogleTokenResponse token = googleOAuthService.getToken(code);
        GoogleUserInfo googleUser = googleOAuthService.getUserInfo(token.getAccessToken());

        User user = userService.findByEmail(googleUser.getEmail())
                .orElseGet(() -> {

                    log.info("Google user not found. Creating new user. email={}", googleUser.getEmail());

                    User u = new User();
                    u.setEmail(googleUser.getEmail());
                    u.setActive(true);
                    u.setProvider(AuthProvider.GOOGLE.name());
                    u.setProviderId(googleUser.getId());

                    User saved = userService.createUser(u);
                    log.info("Google user created. userId={}", saved.getId());

                    assignRole(saved, "CUSTOMER");
                    return saved;
                });

        validateActiveUser(user);
        log.info("Google login successful. userId={}", user.getId());

        return buildIdentity(user);
    }

    // =========================
    // OTP LOGIN (MOBILE)
    // =========================
    public void sendLoginOtp(String mobile) {

        log.info("Send login OTP request. mobile={}", mobile);

        if (!userService.existsByMobileNumber(mobile)) {
            log.warn("Login OTP failed. Mobile not registered: {}", mobile);
            throw new UserNotFoundException(mobile);
        }

        otpService.sendOtp(mobile);
        log.info("Login OTP sent successfully. mobile={}", mobile);
    }

    public UserIdentity verifyOtpAndLogin(String mobile, String otp) {

        log.info("Verify OTP login attempt. mobile={}", mobile);

        if (!otpService.verifyOtp(mobile, otp)) {
            log.warn("OTP verification failed. mobile={}", mobile);
            throw new OtpVerificationException();
        }

        User user = userService.findByMobileNumber(mobile)
                .orElseThrow(() -> {
                    log.error("User not found after OTP verification. mobile={}", mobile);
                    return new RuntimeException("User not found");
                });

        validateActiveUser(user);
        log.info("OTP login successful. userId={}", user.getId());

        return buildIdentity(user);
    }

    // =========================
    // EMAIL OTP LOGIN
    // =========================
    public void sendEmailLoginOtp(String email) {

        log.info("Send email login OTP request. email={}", email);

        if (!userService.existsByEmail(email)) {
            log.warn("Email OTP login failed. Email not registered: {}", email);
            throw new UserNotFoundException(email);
        }

        emailOtpService.sendOtp(email);
        log.info("Email login OTP sent. email={}", email);
    }

    public UserIdentity verifyEmailOtpAndLogin(String email, String otp) {

        log.info("Verify email OTP login attempt. email={}", email);

        if (!emailOtpService.verifyOtp(email, otp)) {
            log.warn("Email OTP verification failed. email={}", email);
            throw new OtpVerificationException();
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found after email OTP verification. email={}", email);
                    return new RuntimeException("User not found");
                });

        validateActiveUser(user);
        log.info("Email OTP login successful. userId={}", user.getId());

        return buildIdentity(user);
    }

    // =========================
    // REGISTRATION – MOBILE
    // =========================
    public void startRegistration(String mobile) {

        log.info("Start mobile registration. mobile={}", mobile);

        if (userService.existsByMobileNumber(mobile)) {
            log.warn("Mobile registration failed. Already registered: {}", mobile);
            throw new UserAlreadyExistsException("mobile");
        }

        otpService.sendOtp(mobile);
    }

    public void verifyRegistrationOtp(String mobile, String otp) {

        log.info("Verify registration OTP. mobile={}", mobile);

        if (!otpService.verifyOtp(mobile, otp)) {
            log.warn("Registration OTP verification failed. mobile={}", mobile);
            throw new OtpVerificationException();
        }

        otpService.markOtpVerified(mobile);
        log.info("Registration OTP verified. mobile={}", mobile);
    }

    public UserIdentity completeRegistration(String mobile) {

        log.info("Complete mobile registration. mobile={}", mobile);

        if (!otpService.isOtpVerified(mobile)) {
            log.warn("Registration blocked. OTP not verified. mobile={}", mobile);
            throw new OtpNotVerifiedException();
        }

        if (userService.existsByMobileNumber(mobile)) {
            log.warn("Registration failed. User already registered. mobile={}", mobile);
            throw new UserAlreadyExistsException("mobile");
        }

        User user = new User();
        user.setMobileNumber(mobile);
        user.setActive(true);
        user.setProvider(AuthProvider.OTP.name());

        User savedUser = userService.createUser(user);
        log.info("Mobile user registered. userId={}", savedUser.getId());

        assignRole(savedUser, "CUSTOMER");
        otpService.clearOtpState(mobile);

        return buildIdentity(savedUser);
    }

    // =========================
    // REGISTRATION – EMAIL
    // =========================
    public void startEmailRegistration(String email) {

        log.info("Start email registration. email={}", email);

        if (userService.existsByEmail(email)) {
            log.warn("Email registration failed. Already registered: {}", email);
            throw new UserAlreadyExistsException("email");
        }

        emailOtpService.sendOtp(email);
    }

    public void verifyEmailRegistrationOtp(String email, String otp) {

        log.info("Verify email registration OTP. email={}", email);

        if (!emailOtpService.verifyOtp(email, otp)) {
            log.warn("Email registration OTP failed. email={}", email);
            throw new OtpVerificationException();
        }

        emailOtpService.markOtpVerified(email);
        log.info("Email registration OTP verified. email={}", email);
    }

    public UserIdentity completeEmailRegistration(String email) {

        log.info("Complete email registration. email={}", email);

        if (!emailOtpService.isOtpVerified(email)) {
            log.warn("Email registration blocked. OTP not verified. email={}", email);
            throw new OtpNotVerifiedException();
        }

        User user = new User();
        user.setEmail(email);
        user.setActive(true);
        user.setProvider(AuthProvider.OTP.name());

        User savedUser = userService.createUser(user);
        log.info("Email user registered. userId={}", savedUser.getId());

        assignRole(savedUser, "CUSTOMER");
        emailOtpService.clearOtpState(email);

        return buildIdentity(savedUser);
    }

    // =========================
    // ADMIN STATUS
    // =========================
    public void updateAdminActiveStatus(Long adminId, boolean active) {

        log.info("Updating admin status. adminId={}, active={}", adminId, active);
        userService.changeAdminStatus(adminId, active);
    }

    // =========================
    // HELPERS
    // =========================
    private void assignRole(User user, String roleName) {

        log.info("Assigning role. userId={}, role={}", user.getId(), roleName);

        Role role = roleService.getRoleByName(roleName);
        UserRole ur = new UserRole();
        ur.setUser(user);
        ur.setRole(role);
        userRoleRepository.save(ur);
    }

    private UserIdentity buildIdentity(User user) {

        log.debug("Building identity. userId={}", user.getId());

        String role = userRoleRepository.findByUser(user)
                .orElseThrow(RoleNotFoundException::new)
                .getRole()
                .getName();

        return new UserIdentity(user.getId(), user.getEmail(), role);
    }

    private void validateActiveUser(User user) {

        if (!user.getActive()) {
            log.warn("Inactive account access attempt. userId={}", user.getId());
            throw new AccountInactiveException();
        }
    }

    // Hard Deleting Admin
    @Transactional
    public void hardDeleteAdmin(Long adminId) {

        log.info("Starting hard delete process for adminId={}", adminId);

        if (!userRepository.existsById(adminId)) {
            log.warn("Hard delete failed: User not found for adminId={}", adminId);
            throw new UserNotFoundException(adminId);
        }

        // ❌ Block SUPER_ADMIN deletion
        if (userRoleRepository.existsByUserIdAndRoleName(adminId, "SUPER_ADMIN")) {
            log.warn("Hard delete blocked: Attempt to delete SUPER_ADMIN with adminId={}", adminId);
            throw new UnauthorizedAdminActionException("SUPER_ADMIN cannot be deleted");
        }

        // ✅ Allow only ADMIN deletion
        if (!userRoleRepository.existsByUserIdAndRoleName(adminId, "ADMIN")) {
            log.warn("Hard delete blocked: User is not ADMIN, adminId={}", adminId);
            throw new UnauthorizedAdminActionException("Only ADMIN users can be deleted");
        }

        // 🧹 Delete role mappings first (FK safety)
        log.debug("Deleting role mappings for adminId={}", adminId);
        userRoleRepository.deleteByUserId(adminId);

        // 💥 Hard delete user
        log.debug("Deleting user record for adminId={}", adminId);
        userRepository.deleteById(adminId);

        log.info("Hard delete completed successfully for adminId={}", adminId);
    }

}
