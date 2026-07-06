package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.entity.AppStatus;
import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.ApplicationCreateRequest;
import in.bawvpl.Authify.io.ApplicationResponse;
import in.bawvpl.Authify.io.ApplicationUpdateRequest;

import in.bawvpl.Authify.repository.ApplicationRepository;
import in.bawvpl.Authify.repository.UserApplicationRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import in.bawvpl.Authify.entity.AppUsageLogEntity;
import in.bawvpl.Authify.repository.AppUsageLogRepository;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppService {

    private final ApplicationRepository applicationRepository;

    private final UserApplicationRepository userAppRepo;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final AppUsageLogRepository appUsageLogRepository;

    private static final String BASE_URL =
            "https://43.205.116.38:8080";

    // =====================================================
    // CREATE APP
    // =====================================================

    public ApplicationResponse createApp(

            ApplicationCreateRequest request,

            String email
    ) {

        try {

            if (request == null) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Request required"
                );
            }

            request.normalize();

            validateRequest(request);

            if (

                    applicationRepository.existsBySlug(
                            request.getSlug()
                    )
            ) {

                throw new ResponseStatusException(

                        HttpStatus.CONFLICT,

                        "Slug already exists"
                );
            }

            UserEntity user =
                    getUser(email);

            ApplicationEntity app =
                    ApplicationEntity.builder()

                            .user(user)

                            .name(
                                    request.getName()
                            )

                            .slug(
                                    request.getSlug()
                            )

                            .description(
                                    request.getDescription()
                            )

                            .category(
                                    request.getCategory()
                            )

                            .status(

                                    request.getStatus() != null

                                            ? request.getStatus()

                                            : AppStatus.DRAFT
                            )

                            .visibility(

                                    request.getVisibility() != null

                                            ? request.getVisibility()

                                            : "PUBLIC"
                            )

                            .featured(

                                    request.getFeatured() != null

                                            ? request.getFeatured()

                                            : false
                            )

                            .reserved(false)

                            .routePath(
                                    request.getRoutePath()
                            )

                            .externalUrl(
                                    request.getExternalUrl()
                            )

                            .appUrl(
                                    request.getAppUrl()
                            )

                            .logoUrl(
                                    normalizeImageUrl(
                                            request.getLogoUrl()
                                    )
                            )

                            .bannerUrl(
                                    normalizeImageUrl(
                                            request.getBannerUrl()
                                    )
                            )

                            .version(
                                    request.getVersion()
                            )

                            .createdBy(
                                    user != null
                                            ? user.getEmail()
                                            : null
                            )

                            .downloads(0L)

                            .activeUsers(0L)

                            .createdAt(
                                    LocalDateTime.now()
                            )

                            .updatedAt(
                                    LocalDateTime.now()
                            )

                            .build();

            applicationRepository.save(app);

            log.info(
                    "Application created: {}",
                    app.getName()
            );

            return toResponse(app);

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Create app failed: {}",
                    e.getMessage(),
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Unable to create application"
            );
        }
    }

    // =====================================================
    // GET ALL APPS
    // =====================================================

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAllApps(

            int page,

            int size
    ) {

        try {

            Pageable pageable =
                    buildPageable(page, size);

            Page<ApplicationEntity> result =

                    applicationRepository
                            .findByStatusAndVisibility(

                                    AppStatus.PUBLISHED,

                                    "PUBLIC",

                                    pageable
                            );

            if (

                    result == null ||

                            result.getContent() == null
            ) {

                return new PageImpl<>(

                        Collections.emptyList(),

                        pageable,

                        0
                );
            }

            List<ApplicationResponse> responses =

                    result.getContent()

                            .stream()

                            .filter(Objects::nonNull)

                            .map(this::toResponse)

                            .filter(Objects::nonNull)

                            .collect(Collectors.toList());

            return new PageImpl<>(

                    responses,

                    pageable,

                    result.getTotalElements()
            );

        } catch (Exception e) {

            log.error(

                    "Fetch applications failed:",

                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Unable to fetch applications"
            );
        }
    }

    // =====================================================
    // GET APPS BY USER
    // =====================================================

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAppsByUser(

            String email,

            int page,

            int size
    ) {

        try {

            UserEntity user =
                    getUser(email);

            Pageable pageable =
                    buildPageable(page, size);

            List<UserApplicationEntity> userApps =

                    userAppRepo
                            .findDistinctActiveAppsByUserId(

                                    user.getId()
                            );

            if (

                    userApps == null ||

                            userApps.isEmpty()
            ) {

                return new PageImpl<>(

                        Collections.emptyList(),

                        pageable,

                        0
                );
            }

            List<ApplicationResponse> responses =

                    userApps.stream()
                            .filter(Objects::nonNull)
                            .map(ua -> {

                                ApplicationResponse response =
                                        toResponse(ua.getApp());

                                if (response != null) {
                                    response.setVisitCounter(
                                            ua.getVisitCounter() == null
                                                    ? 0
                                                    : ua.getVisitCounter()
                                    );
                                }

                                return response;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            return new PageImpl<>(

                    responses,

                    pageable,

                    responses.size()
            );

        } catch (Exception e) {

            log.error(
                    "Fetch user apps failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Unable to fetch user applications"
            );
        }
    }

    // =====================================================
    // GET SINGLE APP
    // =====================================================

    public ApplicationResponse getApp(
            Long id
    ) {

        ApplicationEntity app =
                applicationRepository

                        .findById(id)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Application not found"
                                )
                        );

        return toResponse(app);
    }

    // =====================================================
    // UPDATE APP
    // =====================================================

    public ApplicationResponse updateApp(

            Long id,

            ApplicationUpdateRequest request
    ) {

        try {

            if (request == null) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Request required"
                );
            }

            request.normalize();

            ApplicationEntity app =
                    applicationRepository

                            .findById(id)

                            .orElseThrow(() ->

                                    new ResponseStatusException(

                                            HttpStatus.NOT_FOUND,

                                            "Application not found"
                                    )
                            );

            if (request.getName() != null) {
                app.setName(request.getName());
            }

            if (request.getSlug() != null) {
                app.setSlug(request.getSlug());
            }

            if (request.getDescription() != null) {
                app.setDescription(request.getDescription());
            }

            if (request.getCategory() != null) {
                app.setCategory(request.getCategory());
            }

            if (request.getStatus() != null) {
                app.setStatus(request.getStatus());
            }

            if (request.getVisibility() != null) {
                app.setVisibility(request.getVisibility());
            }

            if (request.getFeatured() != null) {
                app.setFeatured(request.getFeatured());
            }

            if (request.getRoutePath() != null) {
                app.setRoutePath(request.getRoutePath());
            }

            if (request.getExternalUrl() != null) {
                app.setExternalUrl(request.getExternalUrl());
            }

            if (request.getAppUrl() != null) {
                app.setAppUrl(request.getAppUrl());
            }

            if (request.getLogoUrl() != null) {

                app.setLogoUrl(
                        normalizeImageUrl(
                                request.getLogoUrl()
                        )
                );
            }

            if (request.getBannerUrl() != null) {

                app.setBannerUrl(
                        normalizeImageUrl(
                                request.getBannerUrl()
                        )
                );
            }

            app.setUpdatedAt(
                    LocalDateTime.now()
            );

            applicationRepository.save(app);

            return toResponse(app);

        } catch (Exception e) {

            log.error(
                    "Update app failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Unable to update application"
            );
        }
    }

    // =====================================================
    // UPDATE APP ASSETS
    // =====================================================

    public ApplicationResponse updateAssets(

            Long appId,

            String logoUrl,

            String bannerUrl
    ) {

        try {

            ApplicationEntity app =

                    applicationRepository
                            .findById(appId)

                            .orElseThrow(() ->

                                    new ResponseStatusException(

                                            HttpStatus.NOT_FOUND,

                                            "Application not found"
                                    )
                            );

            if (

                    logoUrl != null &&

                            !logoUrl.isBlank()
            ) {

                app.setLogoUrl(
                        normalizeImageUrl(logoUrl)
                );
            }

            if (

                    bannerUrl != null &&

                            !bannerUrl.isBlank()
            ) {

                app.setBannerUrl(
                        normalizeImageUrl(bannerUrl)
                );
            }

            app.setUpdatedAt(
                    LocalDateTime.now()
            );

            applicationRepository.save(app);

            return toResponse(app);

        } catch (Exception e) {

            log.error(
                    "Update assets failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Unable to update application assets"
            );
        }
    }

    // =====================================================
    // DELETE APP
    // =====================================================

    public void deleteApp(
            Long id
    ) {

        try {

            if (id == null) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Application ID required"
                );
            }

            ApplicationEntity app =

                    applicationRepository
                            .findById(id)

                            .orElseThrow(() ->

                                    new ResponseStatusException(

                                            HttpStatus.NOT_FOUND,

                                            "Application not found"
                                    )
                            );

            applicationRepository.delete(app);

            log.info(
                    "Application deleted: {}",
                    app.getName()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Delete app failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Unable to delete application"
            );
        }
    }

    // =====================================================
    // OPEN APP
    // =====================================================

    public void openApp(

            Long appId,

            String email
    ) {

        try {

            UserEntity user =
                    getUser(email);

            ApplicationEntity app =
                    applicationRepository

                            .findById(appId)

                            .orElseThrow(() ->

                                    new ResponseStatusException(

                                            HttpStatus.NOT_FOUND,

                                            "Application not found"
                                    )
                            );

            if (
                    app.getStatus() != AppStatus.PUBLISHED
            ) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Application is not published"
                );
            }

            UserApplicationEntity existing =

                    userAppRepo

                            .findByUser_IdAndApp_AppId(

                                    user.getId(),

                                    app.getAppId()
                            )

                            .orElse(null);

            if (existing != null) {

                existing.setActive(true);

                existing.setSubscriptionStatus("ACTIVE");

                existing.trackOpen();

                existing.setUpdatedAt(
                        LocalDateTime.now()
                );

                userAppRepo.save(existing);

                try {

                    AppUsageLogEntity logEntity =
                            AppUsageLogEntity.builder()
                                    .userId(user.getId())
                                    .appId(app.getAppId())
                                    .openedAt(LocalDateTime.now())
                                    .build();

                    log.info(
                            "Saving usage log user={} app={}",
                            user.getId(),
                            app.getAppId()
                    );

                    appUsageLogRepository.save(logEntity);

                } catch (Exception ex) {

                    log.error(
                            "APP_USAGE_LOG_SAVE_FAILED",
                            ex
                    );

                    throw ex;
                }

                incrementApplicationUsage(app);

                return;
            }

            UserApplicationEntity entity =
                    new UserApplicationEntity();

            entity.setUser(user);

            entity.setApp(app);

            entity.setSubscriptionStatus("ACTIVE");

            entity.setActive(true);

            entity.setCreatedAt(
                    LocalDateTime.now()
            );

            entity.trackOpen();

            entity.setUpdatedAt(
                    LocalDateTime.now()
            );

            userAppRepo.save(entity);

            try {

                AppUsageLogEntity logEntity =
                        AppUsageLogEntity.builder()
                                .userId(user.getId())
                                .appId(app.getAppId())
                                .openedAt(LocalDateTime.now())
                                .build();

                log.info(
                        "Saving usage log user={} app={}",
                        user.getId(),
                        app.getAppId()
                );

                appUsageLogRepository.save(logEntity);

            } catch (Exception ex) {

                log.error(
                        "APP_USAGE_LOG_SAVE_FAILED",
                        ex
                );

                throw ex;
            }


            incrementApplicationUsage(app);



// =====================================================
// ADMIN NOTIFICATION
// =====================================================

            userRepository.findAll()

                    .stream()

                    .filter(u ->

                            u.getRole() != null &&

                                    (
                                            u.getRole().equalsIgnoreCase("ROLE_ADMIN") ||

                                                    u.getRole().equalsIgnoreCase("ROLE_OWNER") ||

                                                    u.getRole().equalsIgnoreCase("ADMIN") ||

                                                    u.getRole().equalsIgnoreCase("OWNER")
                                    )
                    )

                    .forEach(admin ->

                            notificationService.create(

                                    admin.getId(),

                                    "Application Subscription",

                                    user.getEntityName()

                                            + " subscribed to "

                                            + app.getName(),

                                    "ADMIN"
                            )
                    );

        } catch (Exception e) {

            log.error(
                    "Open app failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Unable to open application"
            );
        }
    }

    // =====================================================
    // UNSUBSCRIBE APP
    // =====================================================

    @Transactional
    public void unsubscribeApp(Long appId, String email) {

        log.info("UNSUBSCRIBE START appId={} email={}", appId, email);

        final String normalizedEmail =
                email.toLowerCase().trim();

        UserEntity user =
                userRepository
                        .findByEmailIgnoreCase(normalizedEmail)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )
                        );

        log.info("USER FOUND id={}", user.getId());

        UserApplicationEntity userApp =
                userAppRepo
                        .findByUser_IdAndApp_AppId(
                                user.getId(),
                                appId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Application subscription not found"
                                )
                        );

        log.info("SUBSCRIPTION FOUND");

        try {

            userAppRepo.delete(userApp);

            userAppRepo.flush();

            log.info("DELETE SUCCESS");

        } catch (Exception ex) {

            log.error("DELETE FAILED", ex);

            throw ex;
        }
    }

    // =====================================================
    // APP USAGE COUNTERS
    // =====================================================

    private void incrementApplicationUsage(
            ApplicationEntity app
    ) {

        try {

            if (app == null) {
                return;
            }

            Long activeUsers =

                    app.getActiveUsers() != null

                            ? app.getActiveUsers()

                            : 0L;

            Long downloads =

                    app.getDownloads() != null

                            ? app.getDownloads()

                            : 0L;

            app.setActiveUsers(
                    activeUsers + 1
            );

            app.setDownloads(
                    downloads + 1
            );

            app.setUpdatedAt(
                    LocalDateTime.now()
            );

            applicationRepository.save(app);

        } catch (Exception ex) {

            log.error(
                    "Failed to update app analytics",
                    ex
            );
        }
    }

    // =====================================================
    // ADMIN APPS
    // =====================================================

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAdminApps(
            String status,
            String visibility,
            String search,
            int page,
            int size
    ) {
        try {
            Pageable pageable = buildPageable(page, size);
            Page<ApplicationEntity> result;

            boolean hasStatus = status != null && !status.isBlank();
            boolean hasVisibility = visibility != null && !visibility.isBlank();
            boolean hasSearch = search != null && !search.isBlank();

            if (hasStatus && hasVisibility && hasSearch) {
                result = applicationRepository.findByStatusAndVisibilityAndNameContainingIgnoreCase(
                        AppStatus.valueOf(status.toUpperCase()),
                        visibility.toUpperCase(),
                        search,
                        pageable
                );
            } else if (hasStatus && hasSearch) {
                result = applicationRepository.findByStatusAndNameContainingIgnoreCase(
                        AppStatus.valueOf(status.toUpperCase()),
                        search,
                        pageable
                );
            } else if (hasStatus) {
                result = applicationRepository.findByStatus(
                        AppStatus.valueOf(status.toUpperCase()),
                        pageable
                );
            } else if (hasVisibility) {
                result = applicationRepository.findByVisibilityIgnoreCase(
                        visibility.toUpperCase(),
                        pageable
                );
            } else if (hasSearch) {
                result = applicationRepository.findByNameContainingIgnoreCase(
                        search,
                        pageable
                );
            } else {
                result = applicationRepository.findAll(pageable);
            }

            List<ApplicationResponse> responses = result.getContent()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(this::toResponse)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, result.getTotalElements());

        } catch (Exception e) {
            log.error("Fetch admin apps failed", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to fetch admin apps"
            );
        }
    }

    // =====================================================
    // RESPONSE MAPPER
    // =====================================================

    private ApplicationResponse toResponse(ApplicationEntity app) {
        if (app == null) {
            return null;
        }

        ApplicationResponse response = ApplicationResponse.builder()
                .appId(app.getAppId())
                .name(app.getName())
                .appName(app.getName())
                .slug(app.getSlug())
                .description(app.getDescription())
                .appText(app.getDescription())
                .category(app.getCategory())
                .status(app.getStatus())
                .visibility(app.getVisibility())
                .featured(Boolean.TRUE.equals(app.getFeatured()))
                .routePath(app.getRoutePath())
                .externalUrl(app.getExternalUrl())
                .appUrl(app.getAppUrl())
                .logoUrl(normalizeImageUrl(app.getLogoUrl()))
                .bannerUrl(normalizeImageUrl(app.getBannerUrl()))

// =====================================================
// FRONTEND IMAGE ALIASES
// =====================================================

                .appLogo(
                        normalizeImageUrl(
                                app.getLogoUrl()
                        )
                )

                .imageUrl(
                        normalizeImageUrl(
                                app.getLogoUrl() != null
                                        ? app.getLogoUrl()
                                        : app.getBannerUrl()
                        )
                )

                .iconUrl(
                        normalizeImageUrl(
                                app.getLogoUrl()
                        )
                )

                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();

        response.normalize();
        return response;
    }

    // =====================================================
    // IMAGE URL
    // =====================================================

    private String normalizeImageUrl(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        value = value.trim();

        // =====================================================
        // BASE64 IMAGE SUPPORT
        // =====================================================

        if (value.startsWith("data:image")) {
            return value;
        }

        // =====================================================
        // FULL URL
        // =====================================================

        if (
                value.startsWith("http://") ||
                        value.startsWith("https://")
        ) {

            return value;
        }

        // =====================================================
        // STATIC FILE PATH
        // =====================================================

        if (!value.startsWith("/")) {
            value = "/" + value;
        }

        return BASE_URL + value;
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateRequest(
            ApplicationCreateRequest request
    ) {

        if (

                request.getName() == null ||

                        request.getName().isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Application name required"
            );
        }

        if (

                request.getSlug() == null ||

                        request.getSlug().isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Slug required"
            );
        }
    }

    // =====================================================
    // PAGINATION
    // =====================================================

    private Pageable buildPageable(

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

        return PageRequest.of(

                page,

                size,

                Sort.by("createdAt")
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

                .findByEmailIgnoreCase(
                        email
                )

                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "User not found"
                        )
                );
    }
}