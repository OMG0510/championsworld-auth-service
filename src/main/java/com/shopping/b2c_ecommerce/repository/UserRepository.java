package com.shopping.b2c_ecommerce.repository;

import com.shopping.b2c_ecommerce.dto.AdminSummaryResponse;
import com.shopping.b2c_ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobile);

    Optional<User> findByMobileNumber(String mobileNumber);

    @Query("""
    SELECT new com.shopping.b2c_ecommerce.dto.AdminSummaryResponse(
        ur.user.id,
        ur.user.email,
        ur.user.active
    )
    FROM UserRole ur
    WHERE ur.role.name = 'ADMIN'
""")
    List<AdminSummaryResponse> findAllAdmins();


}