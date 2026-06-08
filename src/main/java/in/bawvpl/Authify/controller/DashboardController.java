package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ActivityResponse;
import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.io.DashboardSummaryResponse;
import in.bawvpl.Authify.io.NotificationResponse;
import in.bawvpl.Authify.io.TransactionResponse;

import in.bawvpl.Authify.service.DashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/dashboard")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin("*")
public class DashboardController {

    private final DashboardService dashboardService;

    // =====================================================
    // HELPER
    // =====================================================

    private String getEmail(
            Authentication auth
    ) {

        if (

                auth == null ||

                        auth.getName() == null ||

                        auth.getName().isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.UNAUTHORIZED,

                    "Unauthorized"
            );
        }

        return auth.getName()
                .trim()
                .toLowerCase();
    }

    // =====================================================
    // MAIN DASHBOARD
    // =====================================================

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> dashboard(
            Authentication auth
    ) {

        try {

            log.info("Dashboard API hit");

            String email =
                    getEmail(auth);

            DashboardSummaryResponse data =
                    dashboardService.getSummaryByEmail(email);

            if (data != null) {
                data.normalize();
            }

            return ResponseEntity.ok(

                    ApiResponse.<DashboardSummaryResponse>builder()

                            .status(200)

                            .message(
                                    "Dashboard fetched successfully"
                            )

                            .data(data)

                            .build()
            );

        } catch (ResponseStatusException e) {

            log.error(
                    "Dashboard fetch failed",
                    e
            );

            return ResponseEntity

                    .status(e.getStatusCode())

                    .body(

                            ApiResponse.<DashboardSummaryResponse>builder()

                                    .status(
                                            e.getStatusCode().value()
                                    )

                                    .message(
                                            e.getReason()
                                    )

                                    .build()
                    );

        } catch (Exception e) {

            log.error(
                    "Dashboard fetch failed",
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<DashboardSummaryResponse>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch dashboard"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // SUMMARY
    // =====================================================

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            Authentication auth
    ) {

        try {

            log.info("Dashboard summary API hit");

            String email =
                    getEmail(auth);

            DashboardSummaryResponse summary =
                    dashboardService.getSummaryByEmail(email);

            if (summary != null) {
                summary.normalize();
            }

            return ResponseEntity.ok(

                    ApiResponse.<DashboardSummaryResponse>builder()

                            .status(200)

                            .message(
                                    "Dashboard summary fetched successfully"
                            )

                            .data(summary)

                            .build()
            );

        } catch (ResponseStatusException e) {

            log.error(
                    "Dashboard summary failed",
                    e
            );

            return ResponseEntity

                    .status(e.getStatusCode())

                    .body(

                            ApiResponse.<DashboardSummaryResponse>builder()

                                    .status(
                                            e.getStatusCode().value()
                                    )

                                    .message(
                                            e.getReason()
                                    )

                                    .build()
                    );

        } catch (Exception e) {

            log.error(
                    "Dashboard summary failed",
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<DashboardSummaryResponse>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch dashboard summary"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // TRANSACTIONS
    // =====================================================

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Object>> getTransactions(

            Authentication auth,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        try {

            log.info(
                    "Transactions API hit page={} size={}",
                    page,
                    size
            );

            page = Math.max(page, 0);

            size = Math.max(size, 1);

            size = Math.min(size, 50);

            String email =
                    getEmail(auth);

            Page<TransactionResponse> data =

                    dashboardService.getTransactionsByEmail(

                            email,

                            page,

                            size
                    );

            Map<String, Object> meta =
                    buildPaginationMeta(data);

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .status(200)

                            .message(
                                    "Transactions fetched successfully"
                            )

                            .data(
                                    data.getContent()
                            )

                            .meta(meta)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Transactions fetch failed",
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch transactions"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // NOTIFICATIONS
    // =====================================================

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Object>> getNotifications(

            Authentication auth,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        try {

            log.info(
                    "Notifications API hit page={} size={}",
                    page,
                    size
            );

            page = Math.max(page, 0);

            size = Math.max(size, 1);

            size = Math.min(size, 50);

            String email =
                    getEmail(auth);

            Page<NotificationResponse> data =

                    dashboardService.getNotificationsByEmail(

                            email,

                            page,

                            size
                    );

            data.getContent()
                    .forEach(notification -> {

                        if (notification != null) {
                            notification.normalize();
                        }
                    });

            long unreadCount =

                    data.getContent()

                            .stream()

                            .filter(n ->

                                    n != null &&

                                            !Boolean.TRUE.equals(
                                                    n.getRead()
                                            )
                            )

                            .count();

            Map<String, Object> meta =
                    buildPaginationMeta(data);

            meta.put(
                    "totalUnread",
                    unreadCount
            );

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .status(200)

                            .message(
                                    "Notifications fetched successfully"
                            )

                            .data(
                                    data.getContent()
                            )

                            .meta(meta)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Notifications fetch failed",
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch notifications"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // ACTIVITY
    // =====================================================

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<Object>> getActivity(

            Authentication auth,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        try {

            log.info(
                    "Activity API hit page={} size={}",
                    page,
                    size
            );

            page = Math.max(page, 0);

            size = Math.max(size, 1);

            size = Math.min(size, 50);

            String email =
                    getEmail(auth);

            Page<ActivityResponse> data =
                    dashboardService.getActivity(
                            email,
                            page,
                            size
                    );

            Map<String, Object> meta =
                    buildPaginationMeta(data);

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .status(200)

                            .message(
                                    "Activity fetched successfully"
                            )

                            .data(
                                    data.getContent()
                            )

                            .meta(meta)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Activity fetch failed",
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch activity"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // RECENT APPS
    // =====================================================

    @GetMapping("/recent-apps")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentApps(
            Authentication auth
    ) {

        try {

            log.info("Recent apps API hit");

            String email =
                    getEmail(auth);

            List<Map<String, Object>> apps =
                    dashboardService.getRecentApps(email);

            return ResponseEntity.ok(

                    ApiResponse.<List<Map<String, Object>>>builder()

                            .status(200)

                            .message(
                                    "Recent apps fetched successfully"
                            )

                            .data(apps)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Recent apps fetch failed",
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<List<Map<String, Object>>>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch recent apps"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // APP USAGE ANALYTICS
    //
    // IMPORTANT:
    //
    // Used by:
    // - dashboard analytics cards
    // - frontend usage widgets
    // - app usage overview
    // =====================================================

    @GetMapping("/app-usage")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAppUsage(

            Authentication auth
    ) {

        try {

            log.info("App usage analytics API hit");

            String email =
                    getEmail(auth);

            List<Map<String, Object>> data =

                    dashboardService
                            .getUsageAnalytics(
                                    email
                            );

            return ResponseEntity.ok(

                    ApiResponse.<List<Map<String, Object>>>builder()

                            .status(200)

                            .message(
                                    "App usage fetched successfully"
                            )

                            .data(data)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "App usage fetch failed",
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<List<Map<String, Object>>>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch app usage"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // USAGE CHART
    // =====================================================

    @GetMapping("/usage-chart")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsageChart(

            Authentication auth,

            @RequestParam(defaultValue = "7d")
            String range
    ) {

        try {

            log.info(
                    "Usage chart API hit range={}",
                    range
            );

            String email =
                    getEmail(auth);

            Map<String, Object> chart =
                    dashboardService.getUsageChart(
                            email,
                            range
                    );

            return ResponseEntity.ok(

                    ApiResponse.<Map<String, Object>>builder()

                            .status(200)

                            .message(
                                    "Usage chart fetched successfully"
                            )

                            .data(chart)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Usage chart fetch failed",
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<Map<String, Object>>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch usage chart"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // APP USAGE TIMESERIES
    // =====================================================

    @GetMapping("/app-usage-timeseries")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUsageTimeseries(

            Authentication auth,

            @RequestParam
            String appId,

            @RequestParam(defaultValue = "7d")
            String range,

            @RequestParam(defaultValue = "day")
            String interval
    ) {

        try {

            log.info(
                    "Usage timeseries API hit appId={} range={} interval={}",
                    appId,
                    range,
                    interval
            );

            String email =
                    getEmail(auth);

            List<Map<String, Object>> data =

                    dashboardService.getUsageTimeseries(

                            email,

                            appId,

                            range,

                            interval
                    );

            return ResponseEntity.ok(

                    ApiResponse.<List<Map<String, Object>>>builder()

                            .status(200)

                            .message(
                                    "Usage timeseries fetched successfully"
                            )

                            .data(data)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Usage timeseries fetch failed",
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<List<Map<String, Object>>>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch usage timeseries"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // PAGINATION META
    // =====================================================

    private Map<String, Object> buildPaginationMeta(
            Page<?> page
    ) {

        Map<String, Object> meta =
                new HashMap<>();

        meta.put(
                "page",
                page.getNumber()
        );

        meta.put(
                "size",
                page.getSize()
        );

        meta.put(
                "totalPages",
                page.getTotalPages()
        );

        meta.put(
                "totalElements",
                page.getTotalElements()
        );

        meta.put(
                "hasNext",
                page.hasNext()
        );

        meta.put(
                "hasPrevious",
                page.hasPrevious()
        );

        return meta;
    }
}