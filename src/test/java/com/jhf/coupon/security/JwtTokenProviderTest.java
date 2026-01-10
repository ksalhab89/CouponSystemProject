package com.jhf.coupon.security;

import com.jhf.coupon.backend.login.ClientType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for JwtTokenProvider
 * Target: 100% coverage for security-critical component
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String testSecret = "test-secret-key-for-jwt-must-be-at-least-32-characters-long-for-hs256";
    private final long accessTokenExpiration = 3600000L; // 1 hour
    private final long refreshTokenExpiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(tokenProvider, "accessTokenExpiration", accessTokenExpiration);
        ReflectionTestUtils.setField(tokenProvider, "refreshTokenExpiration", refreshTokenExpiration);
    }

    @Test
    void testGenerateAccessToken_WithValidClaims_ReturnsToken() {
        String email = "test@example.com";
        ClientType clientType = ClientType.CUSTOMER;
        int userId = 123;

        String token = tokenProvider.generateAccessToken(email, clientType, userId);

        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verify token contains correct claims
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(email, claims.getSubject());
        assertEquals("customer", claims.get("clientType", String.class));
        assertEquals(userId, claims.get("userId", Integer.class));
        assertEquals("access", claims.get("type", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testGenerateAccessToken_ForAdmin_ContainsAdminRole() {
        String token = tokenProvider.generateAccessToken("admin@test.com", ClientType.ADMIN, 1);

        String clientType = tokenProvider.getClientTypeFromToken(token);
        assertEquals("admin", clientType);
    }

    @Test
    void testGenerateAccessToken_ForCompany_ContainsCompanyRole() {
        String token = tokenProvider.generateAccessToken("company@test.com", ClientType.COMPANY, 10);

        String clientType = tokenProvider.getClientTypeFromToken(token);
        assertEquals("company", clientType);
    }

    @Test
    void testGenerateRefreshToken_WithValidEmail_ReturnsToken() {
        String email = "refresh@example.com";

        String token = tokenProvider.generateRefreshToken(email);

        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verify token contains correct claims
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(email, claims.getSubject());
        assertEquals("refresh", claims.get("type", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testValidateToken_WithValidToken_ReturnsTrue() {
        String token = tokenProvider.generateAccessToken("test@example.com", ClientType.CUSTOMER, 1);

        boolean isValid = tokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WithExpiredToken_ReturnsFalse() {
        // Create token provider with very short expiration
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(shortExpirationProvider, "accessTokenExpiration", -1000L); // Already expired

        String expiredToken = shortExpirationProvider.generateAccessToken("test@example.com", ClientType.CUSTOMER, 1);

        boolean isValid = tokenProvider.validateToken(expiredToken);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithTamperedToken_ReturnsFalse() {
        String validToken = tokenProvider.generateAccessToken("test@example.com", ClientType.CUSTOMER, 1);

        // Tamper with token by changing a character
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        boolean isValid = tokenProvider.validateToken(tamperedToken);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithInvalidSignature_ReturnsFalse() {
        // Create token with different secret
        JwtTokenProvider differentSecretProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(differentSecretProvider, "jwtSecret", "different-secret-key-that-is-also-at-least-32-characters-long");
        ReflectionTestUtils.setField(differentSecretProvider, "accessTokenExpiration", accessTokenExpiration);

        String tokenWithDifferentSecret = differentSecretProvider.generateAccessToken("test@example.com", ClientType.CUSTOMER, 1);

        boolean isValid = tokenProvider.validateToken(tokenWithDifferentSecret);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithMalformedToken_ReturnsFalse() {
        String malformedToken = "this.is.not.a.valid.jwt.token";

        boolean isValid = tokenProvider.validateToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithEmptyToken_ReturnsFalse() {
        boolean isValid = tokenProvider.validateToken("");

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithNullToken_ReturnsFalse() {
        boolean isValid = tokenProvider.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithUnsupportedToken_ReturnsFalse() {
        // A JWT with NO signature is often considered "unsupported" by many parsers
        // if they expect a signed one.
        String unsignedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.";
        boolean isValid = tokenProvider.validateToken(unsignedToken);
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithIllegalArgument_ReturnsFalse() {
        // Empty string with just dots might trigger IllegalArgumentException in some versions of jjwt
        // depending on how strict the decoder is before validation.
        boolean isValid = tokenProvider.validateToken(" . . ");
        assertFalse(isValid);
    }

    @Test
    void testGetEmailFromToken_WithValidToken_ReturnsEmail() {
        String expectedEmail = "user@example.com";
        String token = tokenProvider.generateAccessToken(expectedEmail, ClientType.CUSTOMER, 1);

        String actualEmail = tokenProvider.getEmailFromToken(token);

        assertEquals(expectedEmail, actualEmail);
    }

    @Test
    void testGetClientTypeFromToken_WithValidToken_ReturnsClientType() {
        String token = tokenProvider.generateAccessToken("test@example.com", ClientType.COMPANY, 1);

        String clientType = tokenProvider.getClientTypeFromToken(token);

        assertEquals("company", clientType);
    }

    @Test
    void testGetUserIdFromToken_WithValidToken_ReturnsUserId() {
        int expectedUserId = 456;
        String token = tokenProvider.generateAccessToken("test@example.com", ClientType.ADMIN, expectedUserId);

        Integer actualUserId = tokenProvider.getUserIdFromToken(token);

        assertEquals(expectedUserId, actualUserId);
    }

    @Test
    void testTokenExpiration_AccessToken_ExpiresAfterConfiguredTime() {
        String token = tokenProvider.generateAccessToken("test@example.com", ClientType.CUSTOMER, 1);

        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long issuedAt = claims.getIssuedAt().getTime();
        long expiration = claims.getExpiration().getTime();
        long actualExpiration = expiration - issuedAt;

        // Allow small margin for execution time
        assertTrue(actualExpiration >= accessTokenExpiration - 1000);
        assertTrue(actualExpiration <= accessTokenExpiration + 1000);
    }

    @Test
    void testTokenExpiration_RefreshToken_ExpiresAfterConfiguredTime() {
        String token = tokenProvider.generateRefreshToken("test@example.com");

        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long issuedAt = claims.getIssuedAt().getTime();
        long expiration = claims.getExpiration().getTime();
        long actualExpiration = expiration - issuedAt;

        // Allow small margin for execution time
        assertTrue(actualExpiration >= refreshTokenExpiration - 1000);
        assertTrue(actualExpiration <= refreshTokenExpiration + 1000);
    }

    @Test
    void testGenerateAccessToken_WithSpecialCharactersInEmail_WorksCorrectly() {
        String emailWithSpecialChars = "test+user@sub-domain.example.com";
        String token = tokenProvider.generateAccessToken(emailWithSpecialChars, ClientType.CUSTOMER, 1);

        String extractedEmail = tokenProvider.getEmailFromToken(token);

        assertEquals(emailWithSpecialChars, extractedEmail);
    }

    @Test
    void testGenerateAccessToken_WithMaxUserId_WorksCorrectly() {
        int maxUserId = Integer.MAX_VALUE;
        String token = tokenProvider.generateAccessToken("test@example.com", ClientType.CUSTOMER, maxUserId);

        Integer extractedUserId = tokenProvider.getUserIdFromToken(token);

        assertEquals(maxUserId, extractedUserId);
    }

    @Test
    void testGenerateAccessToken_MultipleTokens_AreUnique() throws InterruptedException {
        String token1 = tokenProvider.generateAccessToken("test@example.com", ClientType.CUSTOMER, 1);

        // Wait 1 second to ensure different issuedAt time (JWT uses second precision)
        Thread.sleep(1000);

        String token2 = tokenProvider.generateAccessToken("test@example.com", ClientType.CUSTOMER, 1);

        assertNotEquals(token1, token2, "Tokens should be unique even with same parameters due to different issued time");
    }
}
