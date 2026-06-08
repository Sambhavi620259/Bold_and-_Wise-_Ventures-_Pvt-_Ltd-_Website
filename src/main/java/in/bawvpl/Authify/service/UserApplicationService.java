package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.entity.AppStatus;
import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.repository.UserApplicationRepository;
import in.bawvpl.Authify.repository.ApplicationRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApplicationService {

    private final UserApplicationRepository userAppRepository;

    private final UserRepository userRepository;

    private final ApplicationRepository applicationRepository;

    private final ActivityService activityService;

    // =====================================================
    // CONSTANTS
    // =====================================================

    private static final String STATUS_APPLIED =
            "APPLIED";

    // =====================================================
    // APPLY APP
    // =====================================================

    @Transactional
    public UserApplicationEntity applyApp(

            String email,

            Long appId
    ) {

        try {

            final String normalizedEmail =
                    normalizeEmail(email);

            UserEntity user =
                    getUserByEmail(normalizedEmail);

            // =====================================================
            // KYC CHECK DISABLED TEMPORARILY
            // =====================================================

            // if (
            //         !Boolean.TRUE.equals(
            //                 user.getIsKycVerified()
            //         )
            // ) {
            //
            //     throw new ResponseStatusException(
            //
            //             HttpStatus.FORBIDDEN,
            //
            //             "KYC verification required"
            //     );
            // }

            ApplicationEntity app =
                    getAppById(appId);

            // =====================================================
            // APP STATUS CHECK
            // =====================================================

            if (app.getStatus() == null) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Application status unavailable"
                );
            }

            // =====================================================
            // ONLY PUBLISHED APPS
            // =====================================================

            boolean validStatus =

                    app.getStatus() ==
                            AppStatus.PUBLISHED;

            if (!validStatus) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Application is not active"
                );
            }

            // =====================================================
            // PRIVATE APP CHECK
            // =====================================================

            if (

                    app.getVisibility() != null &&

                            "PRIVATE".equalsIgnoreCase(
                                    app.getVisibility()
                            )
            ) {

                throw new ResponseStatusException(

                        HttpStatus.FORBIDDEN,

                        "Private application"
                );
            }

            // =====================================================
            // DUPLICATE CHECK
            // =====================================================

            if (
                    userAppRepository
                            .existsByUser_IdAndApp_AppId(

                                    user.getId(),

                                    appId
                            )
            ) {

                return userAppRepository
                        .findByUser_IdAndApp_AppId(

                                user.getId(),

                                appId
                        )
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.BAD_REQUEST,

                                        "User already applied for this application"
                                )
                        );
            }

            // =====================================================
            // CREATE USER APPLICATION
            // =====================================================

            UserApplicationEntity entity =
                    UserApplicationEntity.builder()

                            .user(user)

                            .app(app)

                            .subscriptionStatus(
                                    STATUS_APPLIED
                            )

                            .visitCounter(0)

                            .active(true)

                            .createdAt(
                                    LocalDateTime.now()
                            )

                            .updatedAt(
                                    LocalDateTime.now()
                            )

                            .build();

            UserApplicationEntity saved =
                    userAppRepository.save(entity);

            log.info(
                    "USER APPLICATION SAVED: {}",
                    saved.getId()
            );

            // =====================================================
            // UPDATE APP STATS
            // =====================================================

            Long activeUsers =
                    app.getActiveUsers() != null

                            ? app.getActiveUsers()

                            : 0L;

            app.setActiveUsers(
                    activeUsers + 1
            );

            app.setUpdatedAt(
                    LocalDateTime.now()
            );

            applicationRepository.save(app);

            log.info(

                    "User {} applied for app {}",

                    user.getEmail(),

                    app.getAppId()
            );

            // =====================================================
            // ACTIVITY LOG
            // =====================================================

            log.info(
                    "ACTIVITY LOG STARTED"
            );

            activityService.log(

                    user.getEmail(),

                    "subscription",

                    "Subscribed to app: " + app.getName()
            );

            return saved;

        } catch (DataIntegrityViolationException e) {

            log.error(
                    "Duplicate application detected",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Application already exists"
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Apply app failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    e.getMessage()
            );
        }
    }

    // =====================================================
    // GET USER APP
    // =====================================================

    public UserApplicationEntity getUserApp(

            String email,

            Long appId
    ) {

        final String normalizedEmail =
                normalizeEmail(email);

        UserEntity user =
                getUserByEmail(normalizedEmail);

        return userAppRepository
                .findByUser_IdAndApp_AppId(

                        user.getId(),

                        appId
                )
                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "Application not found"
                        )
                );
    }

    // =====================================================
    // GET ALL PUBLISHED APPS
    // =====================================================

    public List<ApplicationEntity> getAllApps(

            int page,

            int size
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 10;
        }

        if (size > 100) {
            size = 100;
        }

        Pageable pageable =
                PageRequest.of(page, size);

        return applicationRepository
                .findByStatus(
                        AppStatus.PUBLISHED,
                        pageable
                )
                .getContent()
                .stream()
                .filter(Objects::nonNull)
                .filter(app ->

                        app.getVisibility() == null ||

                                !"PRIVATE"
                                        .equalsIgnoreCase(
                                                app.getVisibility()
                                        )
                )
                .collect(Collectors.toList());
    }

    // =====================================================
    // GET USER APPLICATIONS
    // =====================================================

    public List<UserApplicationEntity> getUserApplications(
            String email
    ) {

        final String normalizedEmail =
                normalizeEmail(email);

        UserEntity user =
                getUserByEmail(normalizedEmail);

        List<UserApplicationEntity> rows =

                userAppRepository
                        .findByUser_IdAndActiveTrue(
                                user.getId()
                        );

        return rows.stream()

                .filter(Objects::nonNull)

                .collect(

                        Collectors.toMap(

                                row -> row.getApp().getAppId(),

                                row -> row,

                                (first, second) -> first
                        )
                )

                .values()

                .stream()

                .collect(Collectors.toList());
    }

    // =====================================================
    // GET RECENT USER APPS
    // =====================================================

    public List<UserApplicationEntity> getRecentApplications(
            String email
    ) {

        final String normalizedEmail =
                normalizeEmail(email);

        UserEntity user =
                getUserByEmail(normalizedEmail);

        return userAppRepository
                .findRecentlyOpenedAppsByUserId(
                        user.getId()
                );
    }

    // =====================================================
    // CHECK ACCESS
    // =====================================================

    public boolean hasAccess(

            String email,

            Long appId
    ) {

        final String normalizedEmail =
                normalizeEmail(email);

        UserEntity user =
                getUserByEmail(normalizedEmail);

        return userAppRepository
                .existsByUser_IdAndApp_AppId(

                        user.getId(),

                        appId
                );
    }

    // =====================================================
    // RECORD APP OPEN
    // =====================================================

    @Transactional
    public void recordAppOpen(
            String email,
            Long appId
    ) {

        String normalizedEmail =
                normalizeEmail(email);

        UserEntity user =
                getUserByEmail(normalizedEmail);

        UserApplicationEntity ua =
                userAppRepository
                        .findByUser_IdAndApp_AppId(
                                user.getId(),
                                appId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Application not found"
                                )
                        );

        ua.setLastOpenedAt(
                LocalDateTime.now()
        );

        ua.setUpdatedAt(
                LocalDateTime.now()
        );

        userAppRepository.save(ua);

        activityService.log(
                user.getEmail(),
                "app_open",
                "Opened app: " + ua.getApp().getName()
        );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String normalizeEmail(
            String email
    ) {

        if (

                email == null ||

                        email.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Email required"
            );
        }

        return email
                .trim()
                .toLowerCase();
    }

    private UserEntity getUserByEmail(
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

    private ApplicationEntity getAppById(
            Long appId
    ) {

        if (appId == null) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "appId required"
            );
        }

        return applicationRepository
                .findById(appId)
                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "App not found"
                        )
                );
    }
}