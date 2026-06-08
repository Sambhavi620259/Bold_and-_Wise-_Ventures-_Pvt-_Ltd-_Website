package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AuditLog;

import in.bawvpl.Authify.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    // =====================================================
    // SAVE AUDIT LOG
    // =====================================================

    @Override
    public void log(

            Long userId,

            String action,

            String metadata,

            HttpServletRequest request
    ) {

        try {

            String ip =
                    getClientIp(request);

            String device =
                    getUserAgent(request);

            AuditLog logEntity =
                    AuditLog.builder()

                            .userId(userId)

                            .action(

                                    action != null

                                            ? action

                                            : "UNKNOWN_ACTION"
                            )

                            .metadata(

                                    metadata != null

                                            ? metadata

                                            : ""
                            )

                            .ip(ip)

                            .device(device)

                            .build();

            auditLogRepository.save(logEntity);

            log.info(

                    "AUDIT SAVED | userId={} | action={} | ip={}",

                    userId,

                    action,

                    ip
            );

        } catch (Exception e) {

            log.error(

                    "Audit logging failed",

                    e
            );
        }
    }

    // =====================================================
    // CLIENT IP
    // =====================================================

    private String getClientIp(
            HttpServletRequest request
    ) {

        try {

            if (request == null) {

                return "UNKNOWN_IP";
            }

            // =====================================================
            // X-FORWARDED-FOR
            // =====================================================

            String forwarded =
                    request.getHeader(
                            "X-Forwarded-For"
                    );

            if (

                    forwarded != null &&

                            !forwarded.isBlank() &&

                            !"unknown".equalsIgnoreCase(
                                    forwarded
                            )
            ) {

                return forwarded
                        .split(",")[0]
                        .trim();
            }

            // =====================================================
            // X-REAL-IP
            // =====================================================

            String realIp =
                    request.getHeader(
                            "X-Real-IP"
                    );

            if (

                    realIp != null &&

                            !realIp.isBlank() &&

                            !"unknown".equalsIgnoreCase(
                                    realIp
                            )
            ) {

                return realIp.trim();
            }

            // =====================================================
            // FALLBACK
            // =====================================================

            return request.getRemoteAddr();

        } catch (Exception e) {

            log.error(
                    "IP extraction failed",
                    e
            );

            return "UNKNOWN_IP";
        }
    }

    // =====================================================
    // USER AGENT
    // =====================================================

    private String getUserAgent(
            HttpServletRequest request
    ) {

        try {

            if (request == null) {

                return "UNKNOWN_DEVICE";
            }

            String userAgent =
                    request.getHeader(
                            "User-Agent"
                    );

            if (

                    userAgent == null ||

                            userAgent.isBlank()
            ) {

                return "UNKNOWN_DEVICE";
            }

            return userAgent;

        } catch (Exception e) {

            log.error(
                    "User-Agent extraction failed",
                    e
            );

            return "UNKNOWN_DEVICE";
        }
    }
}