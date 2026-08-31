package com.fon.rezervacija_sala.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(UserDetails user, Map<String, Object> extra) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        Map<String, Object> claims = new HashMap<>();
        if (extra != null) {
            claims.putAll(extra);
        }
        claims.put("jti", UUID.randomUUID().toString());

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    private Claims sviClaimovi(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return sviClaimovi(token).getSubject();
    }

    public boolean isValid(String token, UserDetails user) {
        try {
            final String un = extractUsername(token);
            return un.equals(user.getUsername()) && !isExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return sviClaimovi(token).getExpiration().before(new Date());
    }

    public List<String> extractRoles(String token) {
        Claims claims = sviClaimovi(token);
        Object roleObj = claims.get("role");
        List<String> roles = new ArrayList<>();

        if (roleObj instanceof String roleStr) {
            roles.add(roleStr.toUpperCase());
        } else if (roleObj instanceof List<?> list) {
            for (Object r : list) {
                if (r instanceof String s) {
                    roles.add(s.toUpperCase());
                }
            }
        }

        return roles;
    }

    public Date extractExpiration(String token) {
        return sviClaimovi(token).getExpiration();
    }

    public Date extractIssuedAt(String token) {
        return sviClaimovi(token).getIssuedAt();
    }

}