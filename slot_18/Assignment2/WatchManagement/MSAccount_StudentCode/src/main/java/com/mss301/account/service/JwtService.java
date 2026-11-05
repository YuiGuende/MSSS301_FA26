package com.mss301.account.service;

import com.mss301.account.entity.StoreAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// [TODO-3.2] Issues access tokens (role claim, 15 minutes) and refresh tokens (7 days)
@Service
public class JwtService {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(StoreAccount account) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(account.getAccountId()))
                .claim(CLAIM_ROLE, account.getRole())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessExpirationMs))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(StoreAccount account) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(account.getAccountId()))
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(new Date(now))
                .expiration(new Date(now + refreshExpirationMs))
                .signWith(key)
                .compact();
    }

    /** Verifies signature and expiry; throws {@link JwtException} when the token is not valid. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
