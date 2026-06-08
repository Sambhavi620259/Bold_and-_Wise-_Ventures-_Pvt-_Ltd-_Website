package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSummaryResponse {

    // =====================================================
    // TOTAL USERS
    // =====================================================

    private long totalUsers;

    // =====================================================
    // ACTIVE USERS
    // =====================================================

    private long activeUsers;

    // =====================================================
    // VERIFIED KYC USERS
    // =====================================================

    private long verifiedUsers;

    // =====================================================
    // TOTAL APPS
    // =====================================================

    private long totalApps;

    // =====================================================
    // OPEN SUPPORT TICKETS
    // =====================================================

    private long openTickets;
}