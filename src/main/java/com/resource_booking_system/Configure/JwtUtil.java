package com.resource_booking_system.Configure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private SecretKey getSigningKey() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateAccessToken(String username) {
        return generateToken(username, TYPE_ACCESS, accessTokenValidity);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(userDetails.getUsername());
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, TYPE_REFRESH, refreshTokenValidity);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateRefreshToken(userDetails.getUsername());
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    public boolean validateToken(Claims claims, UserDetails userDetails) {
        try {
            String username = claims.getSubject();
            String tokenType = claims.get(CLAIM_TYPE, String.class);
            Date expiration = claims.getExpiration();

            if (expiration == null || username == null) {
                return false;
            }

            return TYPE_ACCESS.equals(tokenType) && username.equals(userDetails.getUsername()) && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(Claims claims) {
        try {
            String tokenType = claims.get(CLAIM_TYPE, String.class);
            Date expiration = claims.getExpiration();

            if (expiration == null) {
                return false;
            }

            return TYPE_REFRESH.equals(tokenType) && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }


    private String generateToken(String subject, String type, long validity) {

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE, type);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(now).setExpiration(expiry).signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }
}