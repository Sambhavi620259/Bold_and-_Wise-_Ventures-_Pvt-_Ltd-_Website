package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.io.ApplicationCreateRequest;
import in.bawvpl.Authify.io.ApplicationResponse;
import in.bawvpl.Authify.io.ApplicationUpdateRequest;

import in.bawvpl.Authify.service.AppService;
import org.springframework.web.multipart.MultipartFile;
import in.bawvpl.Authify.service.S3Service;
import in.bawvpl.Authify.service.UserApplicationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/application")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final AppService applicationService;
    private final UserApplicationService userApplicationService;
    private final S3Service s3Service;

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
    // CREATE APP
    // =====================================================

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> create(

            Authentication auth,

            @Valid
            @RequestBody
            ApplicationCreateRequest request
    ) {

        try {

            request.normalize();

            ApplicationResponse response =
                    applicationService.createApp(

                            request,

                            getEmail(auth)
                    );

            if (response != null) {
                response.normalize();
            }

            return ResponseEntity.status(HttpStatus.CREATED)

                    .body(

                            ApiResponse.<ApplicationResponse>builder()

                                    .status(201)

                                    .message(
                                            "Application created successfully"
                                    )

                                    .data(response)

                                    .build()
                    );

        } catch (ResponseStatusException e) {

            log.error(
                    "Create app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(e.getStatusCode())

                    .body(

                            ApiResponse.<ApplicationResponse>builder()

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
                    "Create app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<ApplicationResponse>builder()

                                    .status(500)

                                    .message(
                                            "Unable to create application"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // GET ALL APPS
    // =====================================================

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        try {

            if (page < 0) {
                page = 0;
            }

            if (size <= 0) {
                size = 10;
            }

            if (size > 100) {
                size = 100;
            }

            Page<ApplicationResponse> result =
                    applicationService.getAllApps(
                            page,
                            size
                    );

            List<ApplicationResponse> apps =
                    result != null
                            ? result.getContent()
                            : List.of();

            if (apps != null) {

                apps.stream()
                        .filter(app -> app != null)
                        .forEach(ApplicationResponse::normalize);
            }

            Map<String, Object> meta =
                    new HashMap<>();

            meta.put(
                    "page",
                    result != null
                            ? result.getNumber()
                            : 0
            );

            meta.put(
                    "size",
                    result != null
                            ? result.getSize()
                            : 0
            );

            meta.put(
                    "totalPages",
                    result != null
                            ? result.getTotalPages()
                            : 0
            );

            meta.put(
                    "totalElements",
                    result != null
                            ? result.getTotalElements()
                            : 0
            );

            meta.put(
                    "hasNext",
                    result != null &&
                            result.hasNext()
            );

            meta.put(
                    "hasPrevious",
                    result != null &&
                            result.hasPrevious()
            );

            Map<String, Object> payload =
                    new HashMap<>();

            payload.put("content", apps);
            payload.put("apps", apps);

            return ResponseEntity.ok(

                    ApiResponse.<Map<String, Object>>builder()

                            .status(200)

                            .message(
                                    "Applications fetched successfully"
                            )

                            .data(payload)

                            .meta(meta)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Fetch applications failed: {}",
                    e.getMessage(),
                    e
            );

            Map<String, Object> payload =
                    new HashMap<>();

            payload.put("content", List.of());
            payload.put("apps", List.of());

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<Map<String, Object>>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch applications"
                                    )

                                    .data(payload)

                                    .build()
                    );
        }
    }


    // =====================================================
    // MY APPS
    // =====================================================

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Map<String, Object>>> myApps(

            Authentication auth,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        try {

            if (page < 0) {
                page = 0;
            }

            if (size <= 0) {
                size = 10;
            }

            if (size > 100) {
                size = 100;
            }

            Page<ApplicationResponse> result =
                    applicationService.getAppsByUser(

                            getEmail(auth),

                            page,

                            size
                    );

            List<ApplicationResponse> apps =
                    result != null
                            ? result.getContent()
                            : List.of();

            if (apps != null) {

                apps.stream()
                        .filter(app -> app != null)
                        .forEach(ApplicationResponse::normalize);
            }

            Map<String, Object> meta =
                    new HashMap<>();

            meta.put(
                    "page",
                    result != null
                            ? result.getNumber()
                            : 0
            );

            meta.put(
                    "size",
                    result != null
                            ? result.getSize()
                            : 0
            );

            meta.put(
                    "totalPages",
                    result != null
                            ? result.getTotalPages()
                            : 0
            );

            meta.put(
                    "totalElements",
                    result != null
                            ? result.getTotalElements()
                            : 0
            );

            meta.put(
                    "hasNext",
                    result != null &&
                            result.hasNext()
            );

            meta.put(
                    "hasPrevious",
                    result != null &&
                            result.hasPrevious()
            );

            Map<String, Object> payload =
                    new HashMap<>();

            payload.put("content", apps);
            payload.put("apps", apps);

            return ResponseEntity.ok(

                    ApiResponse.<Map<String, Object>>builder()

                            .status(200)

                            .message(
                                    "My applications fetched successfully"
                            )

                            .data(payload)

                            .meta(meta)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Fetch my apps failed: {}",
                    e.getMessage(),
                    e
            );

            Map<String, Object> payload =
                    new HashMap<>();

            payload.put("content", List.of());
            payload.put("apps", List.of());

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<Map<String, Object>>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch user applications"
                                    )

                                    .data(payload)

                                    .build()
                    );
        }
    }

    // =====================================================
    // PUBLIC APPS
    // =====================================================

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Map<String, Object>>> publicApps(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        try {

            if (page < 0) {
                page = 0;
            }

            if (size <= 0) {
                size = 10;
            }

            if (size > 100) {
                size = 100;
            }

            Page<ApplicationResponse> result =
                    applicationService.getAllApps(
                            page,
                            size
                    );

            List<ApplicationResponse> apps =
                    result != null
                            ? result.getContent()
                            : List.of();

            if (apps != null) {

                apps.stream()
                        .filter(app -> app != null)
                        .forEach(ApplicationResponse::normalize);
            }

            Map<String, Object> meta =
                    new HashMap<>();

            meta.put(
                    "page",
                    result != null
                            ? result.getNumber()
                            : 0
            );

            meta.put(
                    "size",
                    result != null
                            ? result.getSize()
                            : 0
            );

            meta.put(
                    "totalPages",
                    result != null
                            ? result.getTotalPages()
                            : 0
            );

            meta.put(
                    "totalElements",
                    result != null
                            ? result.getTotalElements()
                            : 0
            );

            meta.put(
                    "hasNext",
                    result != null &&
                            result.hasNext()
            );

            meta.put(
                    "hasPrevious",
                    result != null &&
                            result.hasPrevious()
            );

            Map<String, Object> payload =
                    new HashMap<>();

            payload.put("content", apps);
            payload.put("apps", apps);

            return ResponseEntity.ok(

                    ApiResponse.<Map<String, Object>>builder()

                            .status(200)

                            .message(
                                    "Public applications fetched successfully"
                            )

                            .data(payload)

                            .meta(meta)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Fetch public apps failed: {}",
                    e.getMessage(),
                    e
            );

            Map<String, Object> payload =
                    new HashMap<>();

            payload.put("content", List.of());
            payload.put("apps", List.of());

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<Map<String, Object>>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch public applications"
                                    )

                                    .data(payload)

                                    .build()
                    );
        }
    }

    // =====================================================
    // GET SINGLE APP
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> one(
            @PathVariable Long id
    ) {

        try {

            ApplicationResponse response =
                    applicationService.getApp(id);

            if (response != null) {
                response.normalize();
            }

            return ResponseEntity.ok(

                    ApiResponse.<ApplicationResponse>builder()

                            .status(200)

                            .message(
                                    "Application fetched successfully"
                            )

                            .data(response)

                            .build()
            );

        } catch (ResponseStatusException e) {

            log.error(
                    "Fetch app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(e.getStatusCode())

                    .body(

                            ApiResponse.<ApplicationResponse>builder()

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
                    "Fetch app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<ApplicationResponse>builder()

                                    .status(500)

                                    .message(
                                            "Unable to fetch application"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // UPDATE APP
    // =====================================================

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> update(

            @PathVariable Long id,

            @Valid
            @RequestBody
            ApplicationUpdateRequest request
    ) {

        try {

            request.normalize();

            ApplicationResponse response =
                    applicationService.updateApp(
                            id,
                            request
                    );

            if (response != null) {
                response.normalize();
            }

            return ResponseEntity.ok(

                    ApiResponse.<ApplicationResponse>builder()

                            .status(200)

                            .message(
                                    "Application updated successfully"
                            )

                            .data(response)

                            .build()
            );

        } catch (ResponseStatusException e) {

            log.error(
                    "Update app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(e.getStatusCode())

                    .body(

                            ApiResponse.<ApplicationResponse>builder()

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
                    "Update app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.<ApplicationResponse>builder()

                                    .status(500)

                                    .message(
                                            "Unable to update application"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // DELETE APP
    // =====================================================

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(
            @PathVariable Long id
    ) {

        try {

            applicationService.deleteApp(id);

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .status(200)

                            .message(
                                    "Application deleted successfully"
                            )

                            .data(null)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Delete app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.builder()

                                    .status(500)

                                    .message(
                                            "Unable to delete application"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // OPEN APP
    // =====================================================

    @PostMapping("/open")
    public ResponseEntity<ApiResponse<Object>> openApp(

            Authentication auth,

            @RequestBody
            Map<String, Long> body
    ) {

        try {

            if (body == null) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Request body required"
                );
            }

            Long appId =
                    body.get("appId");

            if (appId == null) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "appId is required"
                );
            }

            applicationService.openApp(
                    appId,
                    getEmail(auth)
            );

            userApplicationService.recordAppOpen(
                    getEmail(auth),
                    appId
            );

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status(200)
                            .message(
                                    "Application opened successfully"
                            )
                            .data(null)
                            .build()
            );

        } catch (ResponseStatusException e) {

            log.error(
                    "Open app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(e.getStatusCode())

                    .body(

                            ApiResponse.builder()

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
                    "Open app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.builder()

                                    .status(500)

                                    .message(
                                            "Unable to open application"
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
// UNSUBSCRIBE APP
// =====================================================

    @DeleteMapping("/my/{appId}")
    public ResponseEntity<ApiResponse<Object>> unsubscribe(

            Authentication auth,

            @PathVariable Long appId
    ) {

        try {

            applicationService.unsubscribeApp(

                    appId,

                    getEmail(auth)
            );

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .status(200)

                            .message(
                                    "Application unsubscribed successfully"
                            )

                            .data(null)

                            .build()
            );

        } catch (ResponseStatusException e) {

            log.error(
                    "Unsubscribe app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(e.getStatusCode())

                    .body(

                            ApiResponse.builder()

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
                    "Unsubscribe app failed: {}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity

                    .status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(

                            ApiResponse.builder()

                                    .status(500)

                                    .message(
                                            "Unable to unsubscribe application"
                                    )

                                    .build()
                    );
        }
    }
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @PostMapping("/{id}/upload-logo")
    public ResponseEntity<ApiResponse<ApplicationResponse>> uploadLogo(

            @PathVariable Long id,

            @RequestParam("file")
            MultipartFile file
    ) {

        try {

            String logoUrl =
                    s3Service.uploadAppImage(file);

            ApplicationResponse response =
                    applicationService.updateAssets(
                            id,
                            logoUrl,
                            null
                    );

            return ResponseEntity.ok(

                    ApiResponse.<ApplicationResponse>builder()

                            .status(200)

                            .message("Logo uploaded successfully")

                            .data(response)

                            .build()
            );

        } catch (Exception e) {

            return ResponseEntity.status(500)

                    .body(
                            ApiResponse.<ApplicationResponse>builder()

                                    .status(500)

                                    .message("Logo upload failed")

                                    .build()
                    );
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @PostMapping("/{id}/upload-banner")
    public ResponseEntity<ApiResponse<ApplicationResponse>> uploadBanner(

            @PathVariable Long id,

            @RequestParam("file")
            MultipartFile file
    ) {

        try {

            String bannerUrl =
                    s3Service.uploadAppBanner(file);

            ApplicationResponse response =
                    applicationService.updateAssets(
                            id,
                            null,
                            bannerUrl
                    );

            return ResponseEntity.ok(

                    ApiResponse.<ApplicationResponse>builder()

                            .status(200)

                            .message("Banner uploaded successfully")

                            .data(response)

                            .build()
            );

        } catch (Exception e) {

            return ResponseEntity.status(500)

                    .body(
                            ApiResponse.<ApplicationResponse>builder()

                                    .status(500)

                                    .message("Banner upload failed")

                                    .build()
                    );
        }
    }

    // =====================================================
    // HEALTH CHECK
    // =====================================================

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {

        Map<String, Object> map =
                new HashMap<>();

        map.put(
                "service",
                "application-service"
        );

        map.put(
                "status",
                "UP"
        );

        return ResponseEntity.ok(

                ApiResponse.<Map<String, Object>>builder()

                        .status(200)

                        .message(
                                "Application service healthy"
                        )

                        .data(map)

                        .build()
        );
    }
}