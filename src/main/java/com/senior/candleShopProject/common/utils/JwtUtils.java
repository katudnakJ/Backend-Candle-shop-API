package com.senior.candleShopProject.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    @Value("${JWT_SECRET_KEY}")
    private String jwtSecretKey;

    @Value("${JWT_ACCESS_EXPIRATION_TIME}")
    private Long jwtAccessExpirationTime;

//    Create secret key from the base64 encoded string
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

//    Generate token by using userId
    public String generateToken(UUID userId, String userRole, boolean isOwner) {
        String userIdString = userId.toString();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtAccessExpirationTime);

        return Jwts.builder()
                .subject(userIdString)
                .claim("role",userRole)
                .claim("isOwner", isOwner)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

//    Extract userId From Token
    public String extractUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

//    Extract user Role From Token
    public String extractUserRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }

//    Extract isOwner From Token
    public boolean extractIsOwnerFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("isOwner", Boolean.class);
    }

//    Validate Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

//     Check Token Expiration
    public boolean isTokenExpired(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    public String getUserIdFromToken(String token) {
        return extractUserIdFromToken(token);
    }

    public String getUserRoleFromToken(String token) {
        return extractUserRoleFromToken(token);
    }

    public boolean getIsOwnerFromToken(String token) {return extractIsOwnerFromToken(token);}
}
