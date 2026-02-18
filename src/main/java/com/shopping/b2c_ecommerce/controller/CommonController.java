package com.shopping.b2c_ecommerce.controller;

import com.shopping.b2c_ecommerce.dto.TokenValidationResponse;
import com.shopping.b2c_ecommerce.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class CommonController {

    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    private final JwtUtil jwtUtil;

    // Constructor Injection
    public CommonController(JwtUtil jwtUtil)
    {
        this.jwtUtil = jwtUtil;
    }

    // TOKEN VALIDATION (For other microservices) - FIXED
    @GetMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader)
    {
        log.debug("Token validation request received");

        // Check if Authorization header exists
        if (authHeader == null || authHeader.isEmpty()) {
            log.warn("Token validation failed: Missing Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, null, null));
        }

        // Check if header has Bearer prefix
        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Token validation failed: Invalid Authorization header format");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, null, null));
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.validateAndExtractClaims(token);

            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);

            log.debug("Token validated successfully. userId={}, role={}", userId, role);

            return ResponseEntity.ok(
                    new TokenValidationResponse(true, userId, role)
            );

        } catch (ExpiredJwtException ex) {
            log.warn("Token validation failed: Token expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, null, null));

        } catch (MalformedJwtException ex) {
            log.warn("Token validation failed: Malformed token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, null, null));

        } catch (SignatureException ex) {
            log.warn("Token validation failed: Invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, null, null));

        } catch (Exception ex) {
            log.error("Token validation failed: Unexpected error", ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, null, null));
        }
    }
}