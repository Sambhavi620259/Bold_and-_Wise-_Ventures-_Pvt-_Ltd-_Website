package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    // =====================================================
    // APPS
    // =====================================================

    @Builder.Default
    private int totalApps = 0;

    // =====================================================
    // SUBSCRIPTIONS
    // =====================================================

    @Builder.Default
    private int activeSubscriptions = 0;

    // =====================================================
    // TICKETS
    // =====================================================

    @Builder.Default
    private int myTickets = 0;

    // =====================================================
    // WALLET
    // =====================================================

    @Builder.Default
    private double walletBalance = 0D;

    // =====================================================
    // TRANSACTIONS
    // =====================================================

    @Builder.Default
    private int totalTransactions = 0;

    // =====================================================
    // REFERRALS
    // =====================================================

    @Builder.Default
    private int referralCount = 0;

    // =====================================================
    // KYC
    // =====================================================

    @Builder.Default
    private String kycStatus = "PENDING";

    // =====================================================
    // MONEY
    // =====================================================

    @Builder.Default
    private double totalSpent = 0D;

    @Builder.Default
    private double totalReceived = 0D;

    // =====================================================
    // FRONTEND FALLBACK ALIASES
    //
    // IMPORTANT:
    //
    // Frontend currently supports:
    // - legacy keys
    // - canonical keys
    //
    // DO NOT REMOVE.
    // =====================================================

    @Builder.Default
    private int appsCount = 0;

    @Builder.Default
    private int subscriptionsCount = 0;

    @Builder.Default
    private int ticketsCount = 0;

    @Builder.Default
    private double balance = 0D;

    @Builder.Default
    private double spentAmount = 0D;

    @Builder.Default
    private double receivedAmount = 0D;

    // =====================================================
    // NORMALIZATION
    // =====================================================

    public void normalize() {

        // =====================================================
        // SAFE NEGATIVE PROTECTION
        // =====================================================

        if (totalApps < 0) {

            totalApps = 0;
        }

        if (activeSubscriptions < 0) {

            activeSubscriptions = 0;
        }

        if (myTickets < 0) {

            myTickets = 0;
        }

        if (totalTransactions < 0) {

            totalTransactions = 0;
        }

        if (referralCount < 0) {

            referralCount = 0;
        }

        if (walletBalance < 0) {

            walletBalance = 0D;
        }

        if (totalSpent < 0) {

            totalSpent = 0D;
        }

        if (totalReceived < 0) {

            totalReceived = 0D;
        }

        // =====================================================
        // KYC DEFAULT
        // =====================================================

        if (

                kycStatus == null ||

                        kycStatus.isBlank()
        ) {

            kycStatus = "PENDING";
        }

        // =====================================================
        // FRONTEND ALIASES
        //
        // IMPORTANT:
        //
        // Keeps old frontend dashboards stable.
        // =====================================================

        appsCount =
                totalApps;

        subscriptionsCount =
                activeSubscriptions;

        ticketsCount =
                myTickets;

        balance =
                walletBalance;

        spentAmount =
                totalSpent;

        receivedAmount =
                totalReceived;
    }

    // =====================================================
    // AUTO NORMALIZED BUILDER
    // =====================================================

    public static DashboardSummaryResponse safe(

            DashboardSummaryResponse response
    ) {

        if (response == null) {

            response =
                    new DashboardSummaryResponse();
        }

        response.normalize();

        return response;
    }
}