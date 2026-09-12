package com.resource_booking_system.Configure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "TestSecretKeyForResourceBookingSystem2026TestSecretKey";
    private static final long ACCESS_VALIDITY = 3_600_000L;    // 1 hour
    private static final long REFRESH_VALIDITY = 604_800_000L; // 7 days

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {

        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenValidity", ACCESS_VALIDITY);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenValidity", REFRESH_VALIDITY);

        userDetails = User.withUsername("john").password("password").authorities(Collections.singletonList(new SimpleGrantedAuthority("USER"))).build();
    }


    @Test
    void generateAccessToken_FromUsername_ShouldReturnValidToken() {

        String token = jwtUtil.generateAccessToken("john");

        assertNotNull(token);

        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals("john", claims.getSubject());
        assertEquals("ACCESS", claims.get("type", String.class));
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateAccessToken_FromUserDetails_ShouldReturnValidToken() {

        String token = jwtUtil.generateAccessToken(userDetails);

        assertNotNull(token);

        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals("john", claims.getSubject());
        assertEquals("ACCESS", claims.get("type", String.class));
    }


    @Test
    void generateRefreshToken_FromUsername_ShouldReturnValidToken() {

        String token = jwtUtil.generateRefreshToken("john");

        assertNotNull(token);

        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals("john", claims.getSubject());
        assertEquals("REFRESH", claims.get("type", String.class));
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateRefreshToken_FromUserDetails_ShouldReturnValidToken() {

        String token = jwtUtil.generateRefreshToken(userDetails);

        assertNotNull(token);

        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals("john", claims.getSubject());
        assertEquals("REFRESH", claims.get("type", String.class));
    }


    @Test
    void extractUsername_FromToken_ShouldReturnSubject() {

        String token = jwtUtil.generateAccessToken("john");

        String username = jwtUtil.extractUsername(token);

        assertEquals("john", username);
    }

    @Test
    void extractUsername_FromClaims_ShouldReturnSubject() {

        String token = jwtUtil.generateAccessToken("john");
        Claims claims = jwtUtil.extractAllClaims(token);

        String username = jwtUtil.extractUsername(claims);

        assertEquals("john", username);
    }


    @Test
    void validateToken_WhenValid_ShouldReturnTrue() {

        String token = jwtUtil.generateAccessToken(userDetails);
        Claims claims = jwtUtil.extractAllClaims(token);

        boolean result = jwtUtil.validateToken(claims, userDetails);

        assertTrue(result);
    }

    @Test
    void validateToken_WhenUsernameMismatch_ShouldReturnFalse() {

        String token = jwtUtil.generateAccessToken("otheruser");
        Claims claims = jwtUtil.extractAllClaims(token);

        boolean result = jwtUtil.validateToken(claims, userDetails);

        assertFalse(result);
    }

    @Test
    void validateToken_WhenRefreshTokenProvided_ShouldReturnFalse() {

        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        Claims claims = jwtUtil.extractAllClaims(refreshToken);

        boolean result = jwtUtil.validateToken(claims, userDetails);

        assertFalse(result, "Refresh token must not be accepted as access token");
    }

    @Test
    void validateToken_WhenExpired_ShouldReturnFalse() {

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", "john");
        claimsMap.put("type", "ACCESS");
        // exp is in SECONDS for JJWT DefaultClaims
        claimsMap.put("exp", (System.currentTimeMillis() / 1000) - 10);

        Claims expiredClaims = new DefaultClaims(claimsMap);

        boolean result = jwtUtil.validateToken(expiredClaims, userDetails);

        assertFalse(result);
    }

    @Test
    void validateToken_WhenNullExpiration_ShouldReturnFalse() {

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", "john");
        claimsMap.put("type", "ACCESS");

        Claims claims = new DefaultClaims(claimsMap);

        boolean result = jwtUtil.validateToken(claims, userDetails);

        assertFalse(result, "Token with null expiration should be rejected");
    }

    @Test
    void validateToken_WhenNullUsername_ShouldReturnFalse() {

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("type", "ACCESS");

        claimsMap.put("exp", (System.currentTimeMillis() / 1000) + 60);

        Claims claims = new DefaultClaims(claimsMap);

        boolean result = jwtUtil.validateToken(claims, userDetails);

        assertFalse(result);
    }

    @Test
    void validateToken_WhenWrongType_ShouldReturnFalse() {

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", "john");
        claimsMap.put("type", "SOMETHING_ELSE");
        claimsMap.put("exp", (System.currentTimeMillis() / 1000) + 60);

        Claims claims = new DefaultClaims(claimsMap);

        boolean result = jwtUtil.validateToken(claims, userDetails);

        assertFalse(result);
    }


    @Test
    void validateRefreshToken_WhenValid_ShouldReturnTrue() {

        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        Claims claims = jwtUtil.extractAllClaims(refreshToken);

        boolean result = jwtUtil.validateRefreshToken(claims);

        assertTrue(result);
    }

    @Test
    void validateRefreshToken_WhenAccessTokenProvided_ShouldReturnFalse() {

        String accessToken = jwtUtil.generateAccessToken(userDetails);
        Claims claims = jwtUtil.extractAllClaims(accessToken);

        boolean result = jwtUtil.validateRefreshToken(claims);

        assertFalse(result, "Access token must not be accepted as refresh token");
    }

    @Test
    void validateRefreshToken_WhenExpired_ShouldReturnFalse() {

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", "john");
        claimsMap.put("type", "REFRESH");
        claimsMap.put("exp", (System.currentTimeMillis() / 1000) - 10);

        Claims expiredClaims = new DefaultClaims(claimsMap);

        boolean result = jwtUtil.validateRefreshToken(expiredClaims);

        assertFalse(result);
    }

    @Test
    void validateRefreshToken_WhenNullExpiration_ShouldReturnFalse() {

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", "john");
        claimsMap.put("type", "REFRESH");

        Claims claims = new DefaultClaims(claimsMap);

        boolean result = jwtUtil.validateRefreshToken(claims);

        assertFalse(result);
    }

    @Test
    void validateRefreshToken_WhenWrongType_ShouldReturnFalse() {

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", "john");
        claimsMap.put("type", "SOMETHING_ELSE");
        claimsMap.put("exp", (System.currentTimeMillis() / 1000) + 60);

        Claims claims = new DefaultClaims(claimsMap);

        boolean result = jwtUtil.validateRefreshToken(claims);

        assertFalse(result);
    }


    @Test
    void extractAllClaims_WhenTokenMalformed_ShouldThrowException() {

        assertThrows(JwtException.class, () -> jwtUtil.extractAllClaims("not-a-jwt-token"));
    }

    @Test
    void extractAllClaims_WhenSignatureInvalid_ShouldThrowException() {

        String otherSecret = "AnotherSecretKeyForTestingDifferentSignature2026!!";
        SecretKey otherKey = Keys.hmacShaKeyFor(otherSecret.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("type", "ACCESS");

        String foreignToken = Jwts.builder().setClaims(claimsMap).setSubject("attacker").setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 60_000)).signWith(otherKey, SignatureAlgorithm.HS256).compact();

        assertThrows(SignatureException.class, () -> jwtUtil.extractAllClaims(foreignToken));
    }


    @Test
    void generateToken_WhenSecretTooShort_ShouldThrowIllegalState() {

        JwtUtil brokenUtil = new JwtUtil();
        ReflectionTestUtils.setField(brokenUtil, "secret", "short");
        ReflectionTestUtils.setField(brokenUtil, "accessTokenValidity", ACCESS_VALIDITY);
        ReflectionTestUtils.setField(brokenUtil, "refreshTokenValidity", REFRESH_VALIDITY);

        assertThrows(IllegalStateException.class, () -> brokenUtil.generateAccessToken("john"));
    }

    @Test
    void generateToken_WhenSecretNull_ShouldThrowIllegalState() {

        JwtUtil brokenUtil = new JwtUtil();
        ReflectionTestUtils.setField(brokenUtil, "secret", null);
        ReflectionTestUtils.setField(brokenUtil, "accessTokenValidity", ACCESS_VALIDITY);
        ReflectionTestUtils.setField(brokenUtil, "refreshTokenValidity", REFRESH_VALIDITY);

        assertThrows(IllegalStateException.class, () -> brokenUtil.generateAccessToken("john"));
    }
}