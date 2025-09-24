package myapp.backend.domain.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import myapp.backend.domain.auth.vo.UserVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserVO user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getUser_id());
        claims.put("sns_type", user.getSns_type());
        claims.put("sns_id", user.getSns_id());
        claims.put("username", user.getUsername());

        return createToken(claims, String.valueOf(user.getUser_id()));
    }

    public String generateToken(UserVO user, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getUser_id());
        claims.put("sns_type", user.getSns_type());
        claims.put("sns_id", user.getSns_id());
        claims.put("username", user.getUsername());
        claims.put("is_admin", isAdmin);
        
        // ADMIN 권한 추가
        if (isAdmin) {
            claims.put("authorities", Arrays.asList("ROLE_ADMIN"));
        }

        return createToken(claims, String.valueOf(user.getUser_id()));
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer("myapp-backend")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        return claims.get("username", String.class);
    }

    public Integer extractUserId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("user_id", Integer.class);
    }

    public String extractSnsType(String token) {
        Claims claims = extractClaims(token);
        return claims.get("sns_type", String.class);
    }

    public String extractSnsId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("sns_id", String.class);
    }

    public Boolean extractIsAdmin(String token) {
        Claims claims = extractClaims(token);
        return claims.get("is_admin", Boolean.class);
    }
} 