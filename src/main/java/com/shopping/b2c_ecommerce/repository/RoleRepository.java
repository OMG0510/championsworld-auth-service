package com.shopping.b2c_ecommerce.repository;

import com.shopping.b2c_ecommerce.entity.Role;
import com.shopping.b2c_ecommerce.entity.User;
import com.shopping.b2c_ecommerce.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

}
