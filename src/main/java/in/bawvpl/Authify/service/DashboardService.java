package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.ActivityResponse;
import in.bawvpl.Authify.io.DashboardSummaryResponse;
import in.bawvpl.Authify.io.NotificationResponse;
import in.bawvpl.Authify.io.TransactionResponse;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * =====================================================
 * DASHBOARD SERVICE
 * =====================================================
 *
 * JWT-based dashboard APIs
 *
 * Provides:
 *
 * - Summary
 * - Transactions
 * - Notifications
 * - Activity
 * - Recent apps
 * - Usage analytics
 * - Usage charts
 * - Usage timeseries
 *
 * =====================================================
 *
 * IMPORTANT FRONTEND CONTRACTS
 * =====================================================
 *
 * DashboardSummaryResponse MUST provide:
 *
 * - totalApps
 * - activeSubscriptions
 * - myTickets
 * - kycStatus
 * - totalTransactions
 * - referralCount
 * - totalSpent
 * - totalReceived
 * - walletBalance
 *
 * IMPORTANT:
 *
 * totalApps:
 * - global PUBLIC + PUBLISHED apps
 * - NOT user subscriptions
 *
 * activeSubscriptions:
 * - current user subscribed apps count
 *
 * =====================================================
 *
 * Recent apps MUST provide:
 *
 * - id
 * - name
 * - logoUrl
 * - routePath
 * - externalUrl
 * - lastAccessedAt
 *
 * Compatibility aliases:
 *
 * - appId
 * - appName
 * - appLogo
 * - imageUrl
 * - lastOpenedAt
 * - visitCount
 *
 * IMPORTANT:
 *
 * Recent apps MUST ONLY include
 * actually opened apps.
 *
 * Subscription alone MUST NOT create recents.
 *
 * =====================================================
 *
 * Notifications SHOULD provide:
 *
 * - totalUnread
 *
 * =====================================================
 *
 * Usage Analytics Expected Shape
 *
 * [
 *   {
 *     "appId": "4",
 *     "appName": "Spotify",
 *     "usageCount": 12,
 *     "lastUsedAt": "2026-05-21T12:10:00"
 *   }
 * ]
 *
 * =====================================================
 *
 * Usage Timeseries Expected Shape
 *
 * [
 *   {
 *     "date": "2026-05-21",
 *     "opens": 12
 *   }
 * ]
 *
 * OR hourly buckets.
 *
 * =====================================================
 */
public interface DashboardService {

    // =====================================================
    // SUMMARY
    // =====================================================

    DashboardSummaryResponse getSummaryByEmail(
            String email
    );

    // =====================================================
    // TRANSACTIONS
    //
    // IMPORTANT:
    //
    // Spring pagination contract:
    //
    // - content
    // - totalElements
    // =====================================================

    Page<TransactionResponse> getTransactionsByEmail(

            String email,

            int page,

            int size
    );

    // =====================================================
    // NOTIFICATIONS
    //
    // IMPORTANT:
    //
    // frontend may derive unread count
    // from first page if totalUnread absent
    // =====================================================

    Page<NotificationResponse> getNotificationsByEmail(

            String email,

            int page,

            int size
    );

    // =====================================================
    // ACTIVITY
    // =====================================================

    Page<ActivityResponse> getActivity(

            String email,

            int page,

            int size
    );

    // =====================================================
    // RECENT APPS
    //
    // IMPORTANT:
    //
    // ONLY actual app opens
    // should appear in recents.
    //
    // Apps subscribed but never opened
    // MUST NOT appear.
    // =====================================================

    List<Map<String, Object>> getRecentApps(
            String email
    );

    // =====================================================
    // APP USAGE ANALYTICS
    //
    // IMPORTANT:
    //
    // Used by:
    // - dashboard analytics
    // - usage cards
    // - app usage widgets
    // =====================================================

    List<Map<String, Object>> getUsageAnalytics(
            String email
    );

    // =====================================================
    // USAGE CHART
    //
    // supported ranges:
    // - 24h
    // - 7d
    // - 30d
    // =====================================================

    Map<String, Object> getUsageChart(

            String email,

            String range
    );

    // =====================================================
    // USAGE TIMESERIES
    //
    // appId
    //
    // supported ranges:
    // - 24h
    // - 7d
    // - 30d
    //
    // supported intervals:
    // - hour
    // - day
    //
    // IMPORTANT:
    //
    // response rows should support:
    //
    // - opens
    // - usage
    // - count
    //
    // frontend normalizer supports aliases.
    // =====================================================

    List<Map<String, Object>> getUsageTimeseries(

            String email,

            String appId,

            String range,

            String interval
    );
}