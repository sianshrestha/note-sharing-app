package com.sian.noteshare.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

/**
 * Utility class for creating, parsing, and validating JSON Web Tokens (JWT).
 */
@Component
public class JwtUtil {

    @Value("${jwt.secretKey}")
    private String jwtSecret;

    @Value("${jwt.expirationTime}")
    private long jwtExpirationTime;

    /**
     * Decodes the base64 secret key and generates an HMAC SHA cryptographic key.
     *
     * @return The signing key used for JWT operations.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a new JWT token containing the user's email as the subject.
     *
     * @param email The user's email address.
     * @return The generated JWT token string.
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username (subject) from a validated JWT token.
     *
     * @param token The JWT token string.
     * @return The subject contained within the token.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Validates a JWT token's signature, structure, and expiration date.
     *
     * @param token The JWT token string to validate.
     * @return true if the token is fully valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            System.out.println("JWT expired");
        } catch (UnsupportedJwtException ex) {
            System.out.println("Unsupported JWT");
        } catch (MalformedJwtException ex) {
            System.out.println("Malformed JWT");
        } catch (SignatureException ex) {
            System.out.println("Invalid JWT signature");
        } catch (IllegalArgumentException ex) {
            System.out.println("JWT claims string is empty");
        }
        return false;
    }
}