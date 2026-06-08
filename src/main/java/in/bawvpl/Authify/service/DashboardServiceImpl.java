package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.TransactionEntity;
import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.ActivityResponse;
import in.bawvpl.Authify.io.DashboardSummaryResponse;
import in.bawvpl.Authify.io.NotificationResponse;
import in.bawvpl.Authify.io.TransactionResponse;

import in.bawvpl.Authify.repository.ActivityLogRepository;
import in.bawvpl.Authify.repository.ApplicationRepository;
import in.bawvpl.Authify.repository.UserApplicationRepository;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.NotificationRepository;
import in.bawvpl.Authify.repository.ReferralRepository;
//import in.bawvpl.Authify.repository.SubscriptionRepository;
import in.bawvpl.Authify.repository.TicketRepository;
import in.bawvpl.Authify.repository.TransactionRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl
        implements DashboardService {

    private final TransactionRepository transactionRepository;

    private final ApplicationRepository applicationRepository;

   // private final SubscriptionRepository subscriptionRepository;

    private final ReferralRepository referralRepository;

    private final KycRepository kycRepository;

    private final NotificationRepository notificationRepository;

    private final ActivityLogRepository activityLogRepository;

    private final UserRepository userRepository;

    private final UserApplicationRepository userApplicationRepository;

    private final TicketRepository ticketRepository;

    // =====================================================
    // SUMMARY
    // =====================================================

    @Override
    public DashboardSummaryResponse getSummaryByEmail(
            String email
    ) {

        log.info(
                "Fetching dashboard summary for {}",
                email
        );

        UserEntity user =
                getUser(email);

        return getSummary(
                user.getId()
        );
    }

    private DashboardSummaryResponse getSummary(
            Long userId
    ) {

        try {

            Pageable pageable =
                    PageRequest.of(0, 100);

            // =====================================================
            // TRANSACTIONS
            // =====================================================

            Page<TransactionEntity> txPage =

                    transactionRepository
                            .findByUser_IdOrderByPaymentDateDesc(

                                    userId,

                                    pageable
                            );

            List<TransactionEntity> transactions =

                    txPage != null

                            ? txPage.getContent()

                            : new ArrayList<>();

            // =====================================================
            // TOTAL SPENT
            // =====================================================

            double totalSpent =

                    transactions.stream()

                            .filter(t ->

                                    t != null &&

                                            t.getType() != null &&

                                            "DEBIT"
                                                    .equalsIgnoreCase(
                                                            t.getType()
                                                    )
                            )

                            .mapToDouble(t ->

                                    t.getAmount() != null

                                            ? t.getAmount()

                                            : 0.0
                            )

                            .sum();

            // =====================================================
            // TOTAL RECEIVED
            // =====================================================

            double totalReceived =

                    transactions.stream()

                            .filter(t ->

                                    t != null &&

                                            t.getType() != null &&

                                            "CREDIT"
                                                    .equalsIgnoreCase(
                                                            t.getType()
                                                    )
                            )

                            .mapToDouble(t ->

                                    t.getAmount() != null

                                            ? t.getAmount()

                                            : 0.0
                            )

                            .sum();

            // =====================================================
            // TOTAL APPS
            //
            // IMPORTANT:
            //
            // MUST count:
            // - global PUBLIC apps
            // - global PUBLISHED apps
            //
            // MUST NOT count:
            // - user subscriptions
            // =====================================================

            long totalApps = 0;

            try {

                totalApps =

                        applicationRepository
                                .countPublishedPublicApps();

            } catch (Exception ex) {

                log.error(
                        "Failed to fetch global catalog apps",
                        ex
                );

                totalApps = 0;
            }

            // =====================================================
            // ACTIVE SUBSCRIPTIONS
            //
            // USER-SPECIFIC
            // =====================================================

            long activeSubs = 0;

            try {

                activeSubs =

                        userApplicationRepository
                                .countByUser_Id(userId);

            } catch (Exception ex) {

                log.error(
                        "Failed to fetch subscriptions",
                        ex
                );
            }

            // =====================================================
            // MY TICKETS
            // =====================================================

            long myTickets = 0;

            try {

                myTickets =

                        ticketRepository
                                .countByUser_Id(
                                        userId
                                );

            } catch (Exception ex) {

                log.error(
                        "Failed to fetch tickets",
                        ex
                );
            }

            // =====================================================
            // REFERRALS
            // =====================================================

            long referrals = 0;

            try {

                referrals =

                        referralRepository
                                .countByReferrer_Id(userId);

            } catch (Exception ex) {

                log.error(
                        "Failed to fetch referrals",
                        ex
                );
            }

            // =====================================================
            // WALLET BALANCE
            // =====================================================

            Double walletBalance =
                    totalReceived - totalSpent;

            if (

                    walletBalance == null ||

                            walletBalance < 0
            ) {

                walletBalance = 0.0;
            }

            // =====================================================
            // KYC STATUS
            // =====================================================

            String kycStatus =

                    kycRepository
                            .findByUser_Id(userId)

                            .map(KycEntity::getStatus)

                            .map(Enum::name)

                            .orElse("PENDING");

            // =====================================================
            // RESPONSE
            // =====================================================

            DashboardSummaryResponse response =

                    DashboardSummaryResponse.builder()

                            .totalApps(
                                    (int) totalApps
                            )

                            .activeSubscriptions(
                                    (int) activeSubs
                            )

                            .myTickets(
                                    (int) myTickets
                            )

                            .walletBalance(
                                    walletBalance
                            )

                            .totalTransactions(
                                    transactions.size()
                            )

                            .referralCount(
                                    (int) referrals
                            )

                            .kycStatus(
                                    kycStatus
                            )

                            .totalSpent(
                                    totalSpent
                            )

                            .totalReceived(
                                    totalReceived
                            )

                            .build();

            response.normalize();

            return response;

        } catch (Exception e) {

            log.error(
                    "Dashboard summary error",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Failed to fetch dashboard"
            );
        }
    }

    // =====================================================
    // TRANSACTIONS
    // =====================================================

    @Override
    public Page<TransactionResponse> getTransactionsByEmail(

            String email,

            int page,

            int size
    ) {

        UserEntity user =
                getUser(email);

        Pageable pageable =
                createPageable(

                        page,

                        size,

                        "paymentDate"
                );

        Page<TransactionEntity> txPage =

                transactionRepository
                        .findByUser_IdOrderByPaymentDateDesc(

                                user.getId(),

                                pageable
                        );

        List<TransactionResponse> content =
                new ArrayList<>();

        for (TransactionEntity tx : txPage.getContent()) {

            if (tx == null) {
                continue;
            }

            TransactionResponse response =

                    TransactionResponse.builder()

                            .id(tx.getId())

                            .amount(tx.getAmount())

                            .type(tx.getType())

                            .status(tx.getStatus())

                            .paymentDate(tx.getPaymentDate())

                            .paymentMethod(tx.getPaymentMethod())

                            .paymentSource(tx.getPaymentSource())

                            .paymentDescription(tx.getPaymentDescription())

                            .build();

            content.add(response);
        }

        return new PageImpl<>(

                content,

                pageable,

                txPage.getTotalElements()
        );
    }

    // =====================================================
    // NOTIFICATIONS
    // =====================================================

    @Override
    public Page<NotificationResponse> getNotificationsByEmail(

            String email,

            int page,

            int size
    ) {

        UserEntity user =
                getUser(email);

        Pageable pageable =
                createPageable(

                        page,

                        size,

                        "createdAt"
                );

        return notificationRepository

                .findByUser_IdOrderByCreatedAtDesc(

                        user.getId(),

                        pageable
                )

                .map(n -> {

                    NotificationResponse r =

                            NotificationResponse.builder()

                                    .id(n.getId())

                                    .title(n.getTitle())

                                    .message(n.getMessage())

                                    .body(n.getMessage())

                                    .read(n.getRead())

                                    .createdAt(n.getCreatedAt())

                                    .build();

                    r.normalize();

                    return r;
                });
    }

    // =====================================================
    // ACTIVITY
    // =====================================================

    @Override
    public Page<ActivityResponse> getActivity(

            String email,

            int page,

            int size
    ) {

        UserEntity user =
                getUser(email);

        Pageable pageable =
                createPageable(

                        page,

                        size,

                        "timestamp"
                );

        return activityLogRepository

                .findByUser_IdOrderByTimestampDesc(

                        user.getId(),

                        pageable
                )

                .map(a ->

                        ActivityResponse.builder()

                                .action(a.getAction())

                                .description(a.getDescription())

                                .timestamp(a.getTimestamp())

                                .build()
                );
    }

    // =====================================================
    // RECENT APPS
    //
    // IMPORTANT:
    //
    // ONLY ACTUAL OPENS
    // SHOULD APPEAR.
    //
    // NOT subscriptions.
    // =====================================================

    @Override
    public List<Map<String, Object>> getRecentApps(
            String email
    ) {

        UserEntity user =
                getUser(email);

        List<Map<String, Object>> apps =
                new ArrayList<>();

        try {

            List<UserApplicationEntity> userApps =

                    userApplicationRepository
                            .findRecentlyOpenedAppsByUserId(
                                    user.getId()
                            );

            for (UserApplicationEntity ua : userApps) {

                if (

                        ua == null ||

                                ua.getApp() == null
                ) {

                    continue;
                }

                String logo =
                        ua.getApp().getBannerUrl();

                if (

                        logo == null ||

                                logo.isBlank()
                ) {

                    logo = "/images/default-app.png";
                }



                Map<String, Object> map =
                        createRecentApp(

                                String.valueOf(
                                        ua.getApp().getAppId()
                                ),

                                ua.getApp().getName(),

                                logo,

                                ua.getApp().getRoutePath(),

                                ua.getApp().getExternalUrl(),

                                ua.getLastOpenedAt() != null

                                        ? ua.getLastOpenedAt().toString()

                                        : null,

                                ua.getVisitCounter() != null

                                        ? Long.valueOf(
                                        ua.getVisitCounter()
                                )

                                        : 0L
                        );

                apps.add(map);
            }

        } catch (Exception ex) {

            log.error(
                    "Recent apps fetch failed",
                    ex
            );
        }

        return apps;
    }

    // =====================================================
    // USAGE CHART
    // =====================================================

    @Override
    public Map<String, Object> getUsageChart(

            String email,

            String range
    ) {

        getUser(email);

        Map<String, Object> response =
                new HashMap<>();

        response.put("range", range);

        response.put(

                "data",

                getUsageTimeseries(

                        email,

                        null,

                        range,

                        "day"
                )
        );

        return response;
    }

    // =====================================================
    // USAGE TIMESERIES
    //
    // REAL ANALYTICS
    // =====================================================

    @Override
    public List<Map<String, Object>> getUsageTimeseries(

            String email,

            String appId,

            String range,

            String interval
    ) {

        UserEntity user = getUser(email);

        List<Map<String, Object>> data =
                new ArrayList<>();

        try {

            List<Object[]> rows;

            if ("24h".equalsIgnoreCase(range)) {

                rows =
                        userApplicationRepository
                                .getUsageTimeseries24H(
                                        user.getId(),
                                        appId
                                );
                log.debug("Rows size {}", rows.size());

            } else if ("7d".equalsIgnoreCase(range)) {



                rows =
                        userApplicationRepository
                                .getUsageTimeseries7D(
                                        user.getId(),
                                        appId
                                );
                log.debug("Rows size {}", rows.size());

            } else {

                rows =
                        userApplicationRepository
                                .getUsageTimeseries30D(
                                        user.getId(),
                                        appId
                                );
                log.debug("Rows size {}", rows.size());
            }

            for (Object[] row : rows) {

                if (

                        row == null ||

                                row.length < 2
                ) {

                    continue;
                }

                Map<String, Object> map =
                        new HashMap<>();

                map.put(
                        "date",
                        row[0]
                );

                map.put(
                        "opens",
                        row[1]
                );

                data.add(map);
            }

        } catch (Exception ex) {

            log.error(
                    "Failed to fetch usage timeseries",
                    ex
            );
        }

        return data;
    }

    // =====================================================
    // RECENT APP HELPER
    //
    // IMPORTANT:
    //
    // Frontend expects canonical keys:
    // - id
    // - name
    // - lastAccessedAt
    //
    // KEEP aliases for compatibility.
    // =====================================================

    private Map<String, Object> createRecentApp(

            String appId,

            String appName,

            String logoUrl,

            String routePath,

            String externalUrl,

            String lastOpenedAt,

            Long visitCount
    ) {

        Map<String, Object> map =
                new HashMap<>();

        // =====================================================
        // CANONICAL
        // =====================================================

        map.put("id", appId);

        map.put("name", appName);

        map.put("logoUrl", logoUrl);

        map.put("routePath", routePath);

        map.put("externalUrl", externalUrl);

        map.put("lastAccessedAt", lastOpenedAt);

        // =====================================================
        // LEGACY ALIASES
        // =====================================================

        map.put("appId", appId);

        map.put("appName", appName);

        map.put("appLogo", logoUrl);

        map.put("imageUrl", logoUrl);

        map.put("lastOpenedAt", lastOpenedAt);

        map.put("visitCount", visitCount);

        return map;
    }

    // =====================================================
    // APP USAGE ANALYTICS
    //
    // IMPORTANT:
    //
    // Used by:
    // - dashboard analytics cards
    // - app usage graphs
    // =====================================================

    @Override
    public List<Map<String, Object>> getUsageAnalytics(
            String email
    ) {

        UserEntity user = getUser(email);

        List<Map<String, Object>> data =
                new ArrayList<>();

        try {

            List<UserApplicationEntity> apps =

                    userApplicationRepository
                            .findRecentlyOpenedAppsByUserId(
                                    user.getId()
                            );

            for (UserApplicationEntity ua : apps) {

                if (

                        ua == null ||

                                ua.getApp() == null
                ) {

                    continue;
                }

                Map<String, Object> map =
                        new HashMap<>();

                map.put(
                        "appId",
                        ua.getApp().getAppId()
                );

                map.put(
                        "appName",
                        ua.getApp().getName()
                );

                map.put(
                        "usageCount",

                        ua.getVisitCounter() != null

                                ? ua.getVisitCounter()

                                : 0
                );

                map.put(
                        "lastUsedAt",

                        ua.getLastOpenedAt()
                );

                data.add(map);
            }

        } catch (Exception ex) {

            log.error(
                    "Usage analytics fetch failed",
                    ex
            );
        }

        return data;
    }



    // =====================================================
    // PAGINATION
    // =====================================================

    private Pageable createPageable(

            int page,

            int size,

            String sortField
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 10;
        }

        if (size > 50) {
            size = 50;
        }

        return PageRequest.of(

                page,

                size,

                Sort.by(sortField)
                        .descending()
        );
    }

    // =====================================================
    // USER
    // =====================================================

    private UserEntity getUser(
            String email
    ) {

        return userRepository
                .findByEmailIgnoreCase(email)

                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "User not found"
                        )
                );
    }
}