package com.xertica.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.security.Key;

public class JwtUtils {

    private final static Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final static long EXPIRATION = 1000 * 60 * 60 * 24; // 24h

    public static String generateToken(String email, Long userId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    public static Jws<Claims> validateToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}