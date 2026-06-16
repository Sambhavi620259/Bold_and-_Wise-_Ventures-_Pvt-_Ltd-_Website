package in.bawvpl.Authify.filter;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.UserStatus;

import in.bawvpl.Authify.repository.UserRepository;

import in.bawvpl.Authify.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    // =====================================================
    // SKIP FILTER
    // =====================================================

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        // =====================================================
        // OPTIONS
        // =====================================================

        if (

                HttpMethod.OPTIONS.matches(
                        request.getMethod()
                )
        ) {

            return true;
        }

        String path =
                request.getRequestURI();

        if (path == null) {

            return false;
        }

        return

                // =====================================================
                // AUTH
                // =====================================================

                path.startsWith("/api/v1.0/login")

                        || path.startsWith("/api/v1.0/register")

                        || path.startsWith("/api/v1.0/verify-email")

                        || path.startsWith("/api/v1.0/verify-otp")

                        || path.startsWith("/api/v1.0/forgot-password")

                        || path.startsWith("/api/v1.0/reset-password")

                        || path.startsWith("/api/v1.0/resend-verification-email")

                        || path.startsWith("/api/v1.0/admin/auth/login")

                        //|| path.startsWith("/api/v1.0/admin/auth/register")

                        || (
                        HttpMethod.GET.matches(request.getMethod())
                                &&
                                path.matches("/api/v1.0/admin/invite/[^/]+")
                )

                        || (
                        HttpMethod.POST.matches(request.getMethod())
                                &&
                                path.equals("/api/v1.0/admin/invite/complete")
                )

                        || path.startsWith("/api/v1.0/admin/auth/verify-otp")

                        // =====================================================
                        // PUBLIC
                        // =====================================================

                        || path.startsWith("/api/v1.0/public")

                        || path.startsWith("/api/v1.0/application/list")

                        || path.startsWith("/api/v1.0/application/public")

                        // =====================================================
                        // STATIC
                        // =====================================================

                        || path.startsWith("/uploads/")

                        || path.startsWith("/files/")

                        || path.startsWith("/images/")

                        // =====================================================
                        // SWAGGER
                        // =====================================================

                        || path.startsWith("/swagger")

                        || path.startsWith("/swagger-ui")

                        || path.startsWith("/v3/api-docs")

                        // =====================================================
                        // ACTUATOR
                        // =====================================================

                        || path.startsWith("/actuator")

                        // =====================================================
                        // ROOT
                        // =====================================================

                        || path.equals("/")

                        || path.startsWith("/error");
    }

    // =====================================================
    // FILTER
    // =====================================================

    @Override
    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain chain

    ) throws ServletException, IOException {

        try {

            // =====================================================
            // EXISTING AUTH
            // =====================================================

            if (

                    SecurityContextHolder

                            .getContext()

                            .getAuthentication() != null
            ) {

                chain.doFilter(
                        request,
                        response
                );

                return;
            }

            // =====================================================
            // AUTH HEADER
            // =====================================================

            String authHeader =
                    request.getHeader(
                            HttpHeaders.AUTHORIZATION
                    );

            if (

                    authHeader == null ||

                            authHeader.isBlank() ||

                            !authHeader.startsWith("Bearer ")
            ) {

                chain.doFilter(
                        request,
                        response
                );

                return;
            }

            // =====================================================
            // TOKEN
            // =====================================================

            String jwt =
                    authHeader
                            .substring(7)
                            .trim();

            if (jwt.isBlank()) {

                unauthorized(
                        response,
                        "Token missing"
                );

                return;
            }

            // =====================================================
            // EMAIL
            // =====================================================

            String email;

            try {

                email =
                        jwtUtil.extractUsername(jwt);

            } catch (Exception e) {

                log.error(
                        "JWT parse failed",
                        e
                );

                unauthorized(
                        response,
                        "Invalid token"
                );

                return;
            }

            if (

                    email == null ||

                            email.isBlank()
            ) {

                unauthorized(
                        response,
                        "Invalid token"
                );

                return;
            }

            email =
                    email
                            .trim()
                            .toLowerCase();

            // =====================================================
            // TOKEN VALIDATION
            // =====================================================

            try {

                if (

                        !jwtUtil.validateToken(
                                jwt,
                                email
                        )
                ) {

                    unauthorized(
                            response,
                            "Invalid token"
                    );

                    return;
                }

            } catch (Exception e) {

                log.error(
                        "JWT validation failed",
                        e
                );

                unauthorized(
                        response,
                        "Invalid token"
                );

                return;
            }

            // =====================================================
            // USER FETCH
            // =====================================================

            UserEntity user =
                    userRepository
                            .findByEmailIgnoreCase(email)
                            .orElse(null);

            if (user == null) {

                unauthorized(
                        response,
                        "User not found"
                );

                return;
            }

            // =====================================================
            // STATUS CHECK
            // =====================================================

            UserStatus status =
                    user.getUserStatus();

            if (

                    status == UserStatus.BLOCKED ||

                            status == UserStatus.SUSPENDED ||

                            status == UserStatus.DELETED
            ) {

                unauthorized(
                        response,
                        "Account restricted"
                );

                return;
            }

            // =====================================================
            // TOKEN VERSION
            // =====================================================

            Integer tokenVersion = 0;

            try {

                tokenVersion =
                        jwtUtil.extractTokenVersion(jwt);

            } catch (Exception ignored) {
            }

            Integer currentVersion =
                    user.getTokenVersion();

            if (currentVersion == null) {

                currentVersion = 0;
            }

            if (!currentVersion.equals(tokenVersion)) {

                unauthorized(
                        response,
                        "Session expired"
                );

                return;
            }

            // =====================================================
            // ROLE
            // =====================================================

            String role = "ROLE_USER";

            try {

                if (user.getAdminRole() != null) {

                    role = user.getAdminRole().name();
                }

            } catch (Exception ignored) {
            }
            // =====================================================
            // AUTHORITIES
            // =====================================================

            List<SimpleGrantedAuthority> authorities =
                    Collections.singletonList(

                            new SimpleGrantedAuthority(
                                    role
                            )
                    );

            // =====================================================
            // AUTH OBJECT
            // =====================================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(

                            email,

                            null,

                            authorities
                    );

            authentication.setDetails(

                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder

                    .getContext()

                    .setAuthentication(authentication);

            // =====================================================
            // LOG
            // =====================================================

            log.debug(
                    "Authenticated user: {} with role {}",
                    email,
                    role
            );

            chain.doFilter(
                    request,
                    response
            );

        } catch (Exception e) {

            log.error(
                    "JWT FILTER ERROR",
                    e
            );

            SecurityContextHolder.clearContext();

            unauthorized(
                    response,
                    "Authentication failed"
            );
        }
    }

    // =====================================================
    // UNAUTHORIZED
    // =====================================================

    private void unauthorized(

            HttpServletResponse response,

            String message

    ) throws IOException {

        if (response.isCommitted()) {

            return;
        }

        response.resetBuffer();

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write(
                """
                {
                  "success": false,
                  "status": 401,
                  "message": "%s"
                }
                """.formatted(message)
        );

        response.getWriter().flush();
    }
}