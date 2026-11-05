package com.mss301.account.service;

import com.mss301.account.dto.LoginRequest;
import com.mss301.account.dto.TokenResponse;
import com.mss301.account.entity.StoreAccount;
import com.mss301.account.exception.AuthException;
import com.mss301.account.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // [TODO-3.3] Check order: find username -> isActive (403) -> BCrypt password (401)
    public TokenResponse login(LoginRequest request) {
        StoreAccount account = accountRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        requireActive(account);
        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return issueTokens(account);
    }

    // [TODO-3.3] Only a valid refresh token can be exchanged for new tokens
    public TokenResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parse(refreshToken);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        if (!JwtService.TYPE_REFRESH.equals(claims.get(JwtService.CLAIM_TYPE, String.class))) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token is not a refresh token");
        }
        StoreAccount account = accountRepository.findById(Integer.valueOf(claims.getSubject()))
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
        requireActive(account);
        return issueTokens(account);
    }

    private void requireActive(StoreAccount account) {
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Account is inactive");
        }
    }

    private TokenResponse issueTokens(StoreAccount account) {
        return new TokenResponse(
                jwtService.generateAccessToken(account),
                jwtService.generateRefreshToken(account),
                "Bearer",
                account.getAccountId(),
                account.getUsername(),
                account.getRole());
    }
}
