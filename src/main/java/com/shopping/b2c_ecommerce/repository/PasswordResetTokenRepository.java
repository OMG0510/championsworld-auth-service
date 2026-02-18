package com.shopping.b2c_ecommerce.repository;

import com.shopping.b2c_ecommerce.entity.PasswordResetToken;
import com.shopping.b2c_ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(User user);

}

