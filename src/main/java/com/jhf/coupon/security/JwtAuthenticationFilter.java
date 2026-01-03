package com.jhf.coupon.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter - Extracts and validates JWT tokens from requests
 * Runs once per request to authenticate users based on their JWT token
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String email = tokenProvider.getEmailFromToken(jwt);
                String clientType = tokenProvider.getClientTypeFromToken(jwt);
                Integer userId = tokenProvider.getUserIdFromToken(jwt);

                // Map client type to Spring Security role
                String role = mapClientTypeToRole(clientType);

                // Create authentication token with role
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(authority));

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store userId in request attributes for controller access
                request.setAttribute("userId", userId);
                request.setAttribute("clientType", clientType);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Set authentication for user: {} with role: {}", email, role);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     *
     * @param request HTTP request
     * @return JWT token or null if not present
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Map ClientType to Spring Security role
     *
     * @param clientType Client type string (admin, company, customer)
     * @return Spring Security role (ROLE_ADMIN, ROLE_COMPANY, ROLE_CUSTOMER)
     */
    private String mapClientTypeToRole(String clientType) {
        return switch (clientType.toLowerCase()) {
            case "admin" -> "ROLE_ADMIN";
            case "company" -> "ROLE_COMPANY";
            case "customer" -> "ROLE_CUSTOMER";
            default -> "ROLE_USER";
        };
    }
}
