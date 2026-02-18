package com.shopping.b2c_ecommerce.repository;

import com.shopping.b2c_ecommerce.entity.User;
import com.shopping.b2c_ecommerce.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByUser(User user);
    void deleteByUserId(Long userId);
    boolean existsByUserIdAndRoleName(Long userId, String roleName);
}
