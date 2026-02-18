package com.shopping.b2c_ecommerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.b2c_ecommerce.dto.UserIdentity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String secret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            try {
                SecretKey key = Keys.hmacShaKeyFor(
                        secret.getBytes(StandardCharsets.UTF_8)
                );

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String email = claims.getSubject();
                String role = claims.get("role", String.class);

                // Normalize role
                String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                Long userId = claims.get("userId", Long.class);

                UserIdentity identity = new UserIdentity(userId, email, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                identity,
                                null,
                                List.of(new SimpleGrantedAuthority(normalizedRole))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException ex) {
                log.warn("Token expired for request: {}", request.getRequestURI());
                SecurityContextHolder.clearContext();

                // ✅ Extract user info from expired token
                Claims claims = ex.getClaims();
                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);

                // Return 401 with error message AND user info
                sendExpiredTokenResponse(response, userId, role);
                return; // Stop filter chain

            } catch (MalformedJwtException ex) {
                log.warn("Malformed token for request: {}", request.getRequestURI());
                SecurityContextHolder.clearContext();

                // Return 401 with specific error message
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
                return; // Stop filter chain

            } catch (SignatureException ex) {
                log.warn("Invalid token signature for request: {}", request.getRequestURI());
                SecurityContextHolder.clearContext();

                // Return 401 with specific error message
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token signature");
                return; // Stop filter chain

            } catch (Exception ex) {
                log.error("Token validation error for request: {}", request.getRequestURI(), ex);
                SecurityContextHolder.clearContext();

                // Return 401 with generic error message
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
                return; // Stop filter chain
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Send JSON error response with user info from expired token
     */
    private void sendExpiredTokenResponse(
            HttpServletResponse response,
            Long userId,
            String role
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("valid", false);
        errorResponse.put("message", "Token has expired");
        errorResponse.put("userId", userId);
        errorResponse.put("role", role);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }

    /**
     * Send JSON error response
     */
    private void sendErrorResponse(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}