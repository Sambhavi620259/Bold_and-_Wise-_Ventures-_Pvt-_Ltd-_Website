package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AdminAuditLogEntity;
import in.bawvpl.Authify.repository.AdminAuditLogRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAuditLogServiceImpl
        implements AdminAuditLogService {

    private final AdminAuditLogRepository
            adminAuditLogRepository;

    @Override
    public void logRoleChange(
            Long performedBy,
            Long targetUserId,
            String oldRole,
            String newRole
    ) {

        AdminAuditLogEntity log =
                AdminAuditLogEntity.builder()

                        .action(
                                "ROLE_CHANGE"
                        )

                        .performedBy(
                                performedBy
                        )

                        .targetUserId(
                                targetUserId
                        )

                        .oldRole(
                                oldRole
                        )

                        .newRole(
                                newRole
                        )

                        .build();

        adminAuditLogRepository.save(
                log
        );
    }

    @Override
    public void logInviteCreated(
            Long performedBy,
            String email,
            String role
    ) {

        AdminAuditLogEntity log =
                AdminAuditLogEntity.builder()

                        .action(
                                "INVITE_CREATED:" +
                                        email +
                                        ":" +
                                        role
                        )

                        .performedBy(
                                performedBy
                        )

                        .build();

        adminAuditLogRepository.save(
                log
        );
    }
}