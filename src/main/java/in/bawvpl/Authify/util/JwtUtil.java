package in.bawvpl.Authify.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import java.security.Key;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    // =====================================================
    // CONFIG
    // =====================================================

    @Value("${auth.jwt.secret}")
    private String secret;

    @Value("${auth.jwt.expiration}")
    private long accessTokenExpiry;

    @Value("${auth.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiry;

    // =====================================================
    // SIGNING KEY
    // =====================================================

    private Key signingKey;

    // =====================================================
    // INIT
    // =====================================================

    @PostConstruct
    public void init() {

        if (

                secret == null ||

                        secret.trim().length() < 32
        ) {

            throw new RuntimeException(
                    "JWT secret must be at least 32 characters"
            );
        }

        signingKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        log.info("JWT initialized successfully");
    }

    // =====================================================
    // ACCESS TOKEN
    // =====================================================

    public String generateAccessToken(

            String username,

            Integer tokenVersion,

            String role
    ) {

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "tokenVersion",
                tokenVersion == null
                        ? 0
                        : tokenVersion
        );

        // =====================================================
        // DEFAULT ROLE
        // =====================================================

        if (

                role == null ||

                        role.isBlank()
        ) {

            role = "ROLE_USER";
        }

        role =
                role
                        .trim()
                        .toUpperCase();

        if (!role.startsWith("ROLE_")) {

            role = "ROLE_" + role;
        }

        claims.put(
                "role",
                role
        );

        claims.put(
                "authorities",
                List.of(role)
        );

        log.info(

                "Generating JWT for {} with role {}",

                username,

                role
        );

        return buildToken(

                username,

                claims,

                accessTokenExpiry
        );
    }

    // =====================================================
    // REFRESH TOKEN
    // =====================================================

    public String generateRefreshToken(
            String username
    ) {

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "type",
                "REFRESH"
        );

        return buildToken(

                username,

                claims,

                refreshTokenExpiry
        );
    }

    // =====================================================
    // LEGACY TOKEN
    // =====================================================

    public String generateToken(
            String username
    ) {

        return generateAccessToken(

                username,

                0,

                "ROLE_USER"
        );
    }

    // =====================================================
    // TOKEN BUILDER
    // =====================================================

    private String buildToken(

            String username,

            Map<String, Object> claims,

            long expiry
    ) {

        Date now =
                new Date();

        Date expiryDate =
                new Date(
                        System.currentTimeMillis()
                                + expiry
                );

        return Jwts.builder()

                .setClaims(claims)

                .setSubject(username)

                .setIssuer("Authify")

                .setIssuedAt(now)

                .setExpiration(expiryDate)

                .signWith(

                        signingKey,

                        SignatureAlgorithm.HS256
                )

                .compact();
    }

    // =====================================================
    // USERNAME
    // =====================================================

    public String extractUsername(
            String token
    ) {

        try {

            return extractClaim(

                    token,

                    Claims::getSubject
            );

        } catch (Exception e) {

            log.warn(

                    "Failed to extract username: {}",

                    e.getMessage()
            );

            return null;
        }
    }

    // =====================================================
    // ROLE
    // =====================================================

    public String extractRole(
            String token
    ) {

        try {

            Claims claims =
                    extractAllClaims(token);

            String role =
                    claims.get(

                            "role",

                            String.class
                    );

            if (

                    role == null ||

                            role.isBlank()
            ) {

                return "ROLE_USER";
            }

            role =
                    role
                            .trim()
                            .toUpperCase();

            if (!role.startsWith("ROLE_")) {

                role = "ROLE_" + role;
            }

            return role;

        } catch (Exception e) {

            log.warn(

                    "Failed to extract role: {}",

                    e.getMessage()
            );

            return "ROLE_USER";
        }
    }

    // =====================================================
    // TOKEN VERSION
    // =====================================================

    public Integer extractTokenVersion(
            String token
    ) {

        try {

            Claims claims =
                    extractAllClaims(token);

            Integer version =
                    claims.get(

                            "tokenVersion",

                            Integer.class
                    );

            return version == null
                    ? 0
                    : version;

        } catch (Exception e) {

            return 0;
        }
    }

    // =====================================================
    // TOKEN TYPE
    // =====================================================

    public String extractTokenType(
            String token
    ) {

        try {

            Claims claims =
                    extractAllClaims(token);

            String type =
                    claims.get(

                            "type",

                            String.class
                    );

            return type == null
                    ? "ACCESS"
                    : type;

        } catch (Exception e) {

            return "ACCESS";
        }
    }

    // =====================================================
    // VALIDATE TOKEN WITH USERNAME
    // =====================================================

    public boolean validateToken(

            String token,

            String username
    ) {

        try {

            if (

                    token == null ||

                            token.isBlank()
            ) {

                return false;
            }

            Claims claims =
                    extractAllClaims(token);

            String extractedUsername =
                    claims.getSubject();

            return

                    extractedUsername != null &&

                            username != null &&

                            extractedUsername.equalsIgnoreCase(username)

                            &&

                            !isTokenExpired(claims);

        } catch (ExpiredJwtException e) {

            log.warn("JWT expired");

        } catch (

                JwtException |

                IllegalArgumentException e
        ) {

            log.warn(

                    "JWT invalid: {}",

                    e.getMessage()
            );
        }

        return false;
    }

    // =====================================================
    // VALIDATE TOKEN
    // =====================================================

    public boolean validateToken(
            String token
    ) {

        try {

            if (

                    token == null ||

                            token.isBlank()
            ) {

                return false;
            }

            Claims claims =
                    extractAllClaims(token);

            return !isTokenExpired(claims);

        } catch (Exception e) {

            log.warn(

                    "JWT validation failed: {}",

                    e.getMessage()
            );

            return false;
        }
    }

    // =====================================================
    // EXTRACT CLAIM
    // =====================================================

    public <T> T extractClaim(

            String token,

            Function<Claims, T> resolver
    ) {

        Claims claims =
                extractAllClaims(token);

        return resolver.apply(claims);
    }

    // =====================================================
    // TOKEN EXPIRED
    // =====================================================

    private boolean isTokenExpired(
            Claims claims
    ) {

        return claims

                .getExpiration()

                .before(new Date());
    }

    public boolean isTokenExpired(
            String token
    ) {

        try {

            return isTokenExpired(
                    extractAllClaims(token)
            );

        } catch (Exception e) {

            return true;
        }
    }

    // =====================================================
    // CLEAN TOKEN
    // =====================================================

    private String cleanToken(
            String token
    ) {

        if (token == null) {

            return null;
        }

        token =
                token.trim();

        if (token.startsWith("Bearer ")) {

            token =
                    token.substring(7);
        }

        return token.trim();
    }

    // =====================================================
    // EXTRACT ALL CLAIMS
    // =====================================================

    private Claims extractAllClaims(
            String token
    ) {

        String cleaned =
                cleanToken(token);

        if (

                cleaned == null ||

                        cleaned.isBlank()
        ) {

            throw new JwtException(
                    "JWT token is empty"
            );
        }

        return Jwts.parserBuilder()

                .setSigningKey(signingKey)

                .build()

                .parseClaimsJws(cleaned)

                .getBody();
    }
}