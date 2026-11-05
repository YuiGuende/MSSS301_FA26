package com.mss301.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Single point of authentication: downstream services trust only the headers set here. */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TYPE_ACCESS = "access";

    private final SecretKey key;

    public JwtAuthFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // [TODO-7.3] Client-supplied identity headers are removed on every request, public ones included
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(HEADER_USER_ID);
                    headers.remove(HEADER_USER_ROLE);
                })
                .build();
        if (isPublic(request)) {
            return chain.filter(exchange.mutate().request(request).build());
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Missing bearer token");
        }

        // [TODO-7.5] Verify signature and expiry, and accept access tokens only
        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(authorization.substring(BEARER_PREFIX.length()))
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return unauthorized(exchange, "Invalid or expired token");
        }
        if (!TYPE_ACCESS.equals(claims.get("type", String.class))) {
            return unauthorized(exchange, "Token is not an access token");
        }

        ServerHttpRequest authenticated = request.mutate()
                .headers(headers -> {
                    headers.set(HEADER_USER_ID, claims.getSubject());
                    headers.set(HEADER_USER_ROLE, String.valueOf(claims.get("role", Integer.class)));
                })
                .build();
        return chain.filter(exchange.mutate().request(authenticated).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    // [TODO-7.4] Matched on method and exact path: POST, PUT and DELETE on /api/watches stay protected
    private boolean isPublic(ServerHttpRequest request) {
        String path = request.getPath().pathWithinApplication().value();
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        HttpMethod method = request.getMethod();
        if (HttpMethod.POST.equals(method)) {
            return path.equals("/api/auth/login") || path.equals("/api/auth/refresh") || path.equals("/api/auth/logout");
        }
        if (HttpMethod.GET.equals(method) && path.equals("/api/watches")) {
            return true;
        }
        return path.startsWith("/fallback/");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"success\":false,\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }
}
