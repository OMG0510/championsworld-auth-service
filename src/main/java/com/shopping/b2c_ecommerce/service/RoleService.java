package com.shopping.b2c_ecommerce.service;

import com.shopping.b2c_ecommerce.entity.Role;
import com.shopping.b2c_ecommerce.exception.RoleNotFoundException;
import com.shopping.b2c_ecommerce.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getRoleByName(String roleName) {

        log.debug("Fetching role by name. roleName={}", roleName);

        return roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.warn("Role not found. roleName={}", roleName);
                    return new RoleNotFoundException(roleName);
                });
    }
}