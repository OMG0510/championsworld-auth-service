package com.shopping.b2c_ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // Email may be null for OTP users
        @Column(unique = true)
        private String email;

        @Column
        private String password;

        @Column(nullable = false)
        private Boolean active = true;

        @CreationTimestamp
        private LocalDateTime createdAt;

        @UpdateTimestamp
        private LocalDateTime updatedAt;

        @Column(nullable = false)
        private String provider; // LOCAL / GOOGLE / OTP

        private String providerId; // Google user id (sub)

        @Column(unique = true)
        private String mobileNumber;

        @Column
        private Boolean mobileVerified = false;
}
