package com.shopping.b2c_ecommerce.service;

import com.shopping.b2c_ecommerce.dto.AdminSummaryResponse;
import com.shopping.b2c_ecommerce.entity.User;
import com.shopping.b2c_ecommerce.entity.UserRole;
import com.shopping.b2c_ecommerce.exception.RoleNotAssignedException;
import com.shopping.b2c_ecommerce.exception.UserNotAdminException;
import com.shopping.b2c_ecommerce.exception.UserNotFoundException;
import com.shopping.b2c_ecommerce.repository.UserRepository;
import com.shopping.b2c_ecommerce.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository)
    {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public User createUser(User user)
    {
        log.debug("Creating user");
        User savedUser = userRepository.save(user);
        log.debug("User created successfully. userId={}", savedUser.getId());
        return savedUser;
    }

    public Optional<User> findByEmail(String email)
    {
        log.debug("Finding user by email. email={}", email);
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email)
    {
        log.debug("Checking if email exists. email={}", email);
        return userRepository.existsByEmail(email);
    }

    public boolean existsByMobileNumber(String mobile)
    {
        log.debug("Checking if mobile exists. mobile={}", mobile);
        return userRepository.existsByMobileNumber(mobile);
    }

    public Optional<User> findByMobileNumber(String mobile)
    {
        log.debug("Finding user by mobile. mobile={}", mobile);
        return userRepository.findByMobileNumber(mobile);
    }

    public void changeAdminStatus(Long adminId, boolean active)
    {
        log.info("Change admin status requested. adminId={}, active={}", adminId, active);

        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException(adminId));

        UserRole userRole = userRoleRepository.findByUser(user)
                .orElseThrow(RoleNotAssignedException::new);

        // If user is not an ADMIN
        if (!userRole.getRole().getName().equals("ADMIN"))
        {
            log.warn("Change admin status failed. User is not ADMIN. userId={}", adminId);
            throw new UserNotAdminException();
        }

        // If user is already active / inactive
        if (Boolean.TRUE.equals(user.getActive()) == active)
        {
            log.info("No admin status change required. userId={}, active={}", adminId, active);
            return; // No change
        }

        user.setActive(active);
        userRepository.save(user);

        log.info("Admin status updated successfully. userId={}, active={}", adminId, active);
    }

    public List<AdminSummaryResponse> getAllAdmins()
    {
        log.info("Fetching all admins");
        List<AdminSummaryResponse> admins = userRepository.findAllAdmins();
        log.info("Admins fetched successfully. count={}", admins.size());
        return admins;
    }
}