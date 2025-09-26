package com.xertica.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.security.Key;

public class JwtUtils {

    private final static Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final static long EXPIRATION = 86400000; // 24h

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

// package com.xertica.security;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;
// import java.security.Key;
// import java.util.Date;

// public class JwtUtils {

//     private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
//     // Configuração de expiração (7 dias)
//     private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7 dias em milissegundos
    
//     // Ou use variável de ambiente (mais flexível)
//     // private static final long EXPIRATION_TIME = Long.parseLong(
//     //     System.getenv().getOrDefault("JWT_EXPIRATION", "604800000")); // 7 dias default

//     public static String generateToken(String email, Long userId) {
//         return Jwts.builder()
//                 .setSubject(email)
//                 .claim("userId", userId)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                 .signWith(SECRET_KEY)
//                 .compact();
//     }

//     public static Claims validateToken(String token) {
//         return Jwts.parserBuilder()
//                 .setSigningKey(SECRET_KEY)
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }

//     public static String getEmailFromToken(String token) {
//         return validateToken(token).getSubject();
//     }

//     public static Long getUserIdFromToken(String token) {
//         return validateToken(token).get("userId", Long.class);
//     }
    
//     public static boolean isTokenExpired(String token) {
//         return validateToken(token).getExpiration().before(new Date());
//     }
// }