package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AppStatus;
import in.bawvpl.Authify.entity.KycStatus;
import in.bawvpl.Authify.entity.UserStatus;

import in.bawvpl.Authify.io.AdminSummaryResponse;

import in.bawvpl.Authify.repository.ApplicationRepository;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import in.bawvpl.Authify.entity.AdminRole;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final UserRepository userRepository;

    private final KycRepository kycRepository;

    private final ApplicationRepository applicationRepository;

    // =====================================================
    // DASHBOARD SUMMARY
    // =====================================================

    public AdminSummaryResponse getSummary(

            long totalUsers,

            long openTickets
    ) {

        // =====================================================
        // ACTIVE USERS
        //
        // IMPORTANT:
        //
        // MUST count:
        // - ACTIVE users
        // - ROLE_USER only
        //
        // MUST exclude:
        // - ROLE_ADMIN
        // - ROLE_SUPER_ADMIN
        // =====================================================

        long activeUsers = 0;

        try {

            activeUsers =

                    userRepository
                            .countByUserStatusAndAdminRole(

                                    UserStatus.ACTIVE,

                                    AdminRole.ROLE_USER
                            );

        } catch (Exception ex) {

            log.error(
                    "Failed to fetch active users",
                    ex
            );

            activeUsers = 0;
        }

        // =====================================================
        // VERIFIED KYC USERS
        // =====================================================

        long verifiedUsers = 0;

        try {

            verifiedUsers =

                    kycRepository
                            .countByStatus(
                                    KycStatus.VERIFIED
                            );

        } catch (Exception ex) {

            log.error(
                    "Failed to fetch verified users",
                    ex
            );

            verifiedUsers = 0;
        }

        // =====================================================
        // TOTAL APPS
        //
        // IMPORTANT:
        //
        // MUST count:
        // - PUBLIC
        // - PUBLISHED
        //
        // MUST NOT count:
        // - subscriptions
        // - drafts
        // - private apps
        // =====================================================

        long totalApps = 0;

        try {

            totalApps =

                    applicationRepository
                            .countByStatusAndVisibilityIgnoreCase(

                                    AppStatus.PUBLISHED,

                                    "PUBLIC"
                            );

        } catch (Exception ex) {

            log.error(
                    "Failed to fetch total apps",
                    ex
            );

            totalApps = 0;
        }

        // =====================================================
// ADMIN / OWNER / ORG COUNTS
// =====================================================

        long adminCount = 0;

        long ownerCount = 0;

        long orgCount = 0;

        try {

            adminCount =
                    userRepository.countByAdminRole(
                            AdminRole.ROLE_ADMIN
                    );

            ownerCount =
                    userRepository.countByAdminRole(
                            AdminRole.ROLE_OWNER
                    );

            orgCount =
                    userRepository.countByEntityTypeIgnoreCase(
                            "ORGANIZATION"
                    );

        } catch (Exception ex) {

            log.error(
                    "Failed to fetch admin/owner/org counts",
                    ex
            );
        }

        // =====================================================
        // RESPONSE
        // =====================================================

        AdminSummaryResponse response =
                new AdminSummaryResponse();

        response.setTotalUsers(
                totalUsers
        );

        response.setActiveUsers(
                activeUsers
        );

        response.setVerifiedUsers(
                verifiedUsers
        );

        response.setTotalApps(
                totalApps
        );

        response.setOpenTickets(
                openTickets
        );

        response.setAdminCount(
                adminCount
        );

        response.setOwnerCount(
                ownerCount
        );

        response.setOrgCount(
                orgCount
        );

        return response;
    }

}