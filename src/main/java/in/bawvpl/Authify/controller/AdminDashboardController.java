package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.AdminApiResponse;

import in.bawvpl.Authify.service.AdminActivityService;
import in.bawvpl.Authify.service.AdminDashboardService;
import in.bawvpl.Authify.service.AdminKycService;
import in.bawvpl.Authify.service.AdminTicketService;
import in.bawvpl.Authify.service.AdminUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    private final AdminUserService userService;

    private final AdminTicketService ticketService;

    private final AdminActivityService activityService;

    private final AdminKycService adminKycService;

    // =====================================================
    // DASHBOARD SUMMARY
    // =====================================================

    @GetMapping("/dashboard/summary")
    public AdminApiResponse<?> getSummary() {

        return AdminApiResponse.success(

                dashboardService.getSummary(

                        userService.totalUsers(),

                        ticketService.openTicketsCount()
                )
        );
    }

    // =====================================================
    // USER GROWTH
    // =====================================================

    @GetMapping("/dashboard/user-growth")
    public AdminApiResponse<?> getUserGrowth() {

        return AdminApiResponse.success(

                userService.getUserGrowth()
        );
    }

    // =====================================================
    // ACTIVITY
    // =====================================================

    @GetMapping("/activity")
    public AdminApiResponse<?> getActivity() {

        return AdminApiResponse.success(

                activityService.getActivities()
        );
    }

    // =====================================================
    // RECENT USERS
    // =====================================================

    @GetMapping("/users/recent")
    public AdminApiResponse<?> getRecentUsers(

            @RequestParam(defaultValue = "5")
            int limit
    ) {

        // =====================================================
        // SAFETY
        // =====================================================

        if (limit <= 0) {

            limit = 5;
        }

        if (limit > 50) {

            limit = 50;
        }

        return AdminApiResponse.success(

                userService.getRecentUsers(limit)
        );
    }

    // =====================================================
// UPDATE USER
// =====================================================


    @PatchMapping("/users/{userId}")
    public AdminApiResponse<?> updateUser(

            @PathVariable
            String userId,

            @RequestBody
            java.util.Map<String, Object> body
    ) {

        return AdminApiResponse.success(

                userService.updateUser(

                        userId,

                        body
                )
        );
    }


    // =====================================================
    // ADMIN TICKETS
    // =====================================================

    @GetMapping("/tickets")
    public AdminApiResponse<?> getTickets(

            @RequestParam(defaultValue = "OPEN")
            String status,

            @RequestParam(required = false)
            Long userId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        // =====================================================
        // NORMALIZATION
        // =====================================================

        if (page < 0) {

            page = 0;
        }

        if (size <= 0) {

            size = 20;
        }

        if (size > 100) {

            size = 100;
        }

        if (
                status != null &&
                        !status.isBlank()
        ) {

            status =
                    status
                            .trim()
                            .toUpperCase();
        }

        return AdminApiResponse.success(

                ticketService.getTickets(

                        status,

                        userId,

                        page,

                        size
                )
        );
    }

    // =====================================================
    // RESOLVE TICKET
    // =====================================================

    @PatchMapping("/tickets/{id}/status")
    public AdminApiResponse<?> updateTicketStatus(

            @PathVariable
            String id,

            @RequestBody
            java.util.Map<String, String> body
    ) {

        String status = body.get("status");

        if (
                status == null ||
                        status.isBlank()
        ) {

            status = "RESOLVED";
        }

        ticketService.updateTicketStatus(

                id,

                status.trim()
                        .toUpperCase()
        );

        return AdminApiResponse.successMessage(
                "Ticket status updated successfully"
        );
    }

    // =====================================================
    // DASHBOARD KYC ALL
    // =====================================================

    @GetMapping("/dashboard/kyc/all")
    public AdminApiResponse<?> getAllKyc() {

        return AdminApiResponse.success(

                adminKycService.getAllKyc()
        );
    }

    // =====================================================
    // DASHBOARD KYC PENDING
    // =====================================================

    @GetMapping("/dashboard/kyc/pending")
    public AdminApiResponse<?> getPendingKyc() {

        return AdminApiResponse.success(

                adminKycService.getPendingKyc()
        );
    }

    // =====================================================
    // DASHBOARD VERIFY KYC
    // =====================================================

    @PutMapping("/dashboard/kyc/verify/{id}")
    public AdminApiResponse<?> verifyKyc(

            @PathVariable
            Long id
    ) {

        adminKycService.verifyKyc(id);

        return AdminApiResponse.successMessage(
                "KYC verified successfully"
        );
    }

    // =====================================================
    // DASHBOARD REJECT KYC
    // =====================================================

    @PutMapping("/dashboard/kyc/reject/{id}")
    public AdminApiResponse<?> rejectKyc(

            @PathVariable
            Long id,

            @RequestParam(required = false)
            String reason
    ) {

        // =====================================================
        // DEFAULT REASON
        // =====================================================

        if (
                reason == null ||
                        reason.isBlank()
        ) {

            reason = "KYC rejected";
        }

        adminKycService.rejectKyc(

                id,

                reason.trim()
        );

        return AdminApiResponse.successMessage(
                "KYC rejected successfully"
        );
    }
}