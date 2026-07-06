package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.io.ApplicationCreateRequest;
import in.bawvpl.Authify.io.ApplicationResponse;
import in.bawvpl.Authify.io.ApplicationUpdateRequest;
import in.bawvpl.Authify.service.AppService;
import in.bawvpl.Authify.service.AuditService;
import in.bawvpl.Authify.service.S3Service; // Added S3 service import

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/admin/apps")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','OWNER')")
@Slf4j
public class AdminApplicationController {

    private final AppService appService;
    private final AuditService auditService;
    private final S3Service s3Service; // Injected via Lombok RequiredArgsConstructor

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // =====================================================
    // HELPER
    // =====================================================
    private String getEmail(Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth.getName().trim().toLowerCase();
    }

    // =====================================================
    // CREATE APP
    // =====================================================
    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApp(
            Authentication auth,
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        ApplicationResponse response = appService.createApp(request, getEmail(auth));
        log.info("Admin created application: {}", response.getName());

        return ResponseEntity.ok(
                ApiResponse.<ApplicationResponse>builder()
                        .status(200)
                        .message("Application created successfully")
                        .data(response)
                        .build()
        );
    }

    // =====================================================
    // GET ALL APPS
    // =====================================================
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllApps(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;

        Page<ApplicationResponse> result = appService.getAdminApps(status, visibility, search, page, size);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Applications fetched successfully")
                        .data(result.getContent())
                        .meta(Map.of(
                                "page", result.getNumber(),
                                "size", result.getSize(),
                                "totalPages", result.getTotalPages(),
                                "totalElements", result.getTotalElements(),
                                "hasNext", result.hasNext(),
                                "hasPrevious", result.hasPrevious()
                        ))
                        .build()
        );
    }

    // =====================================================
    // GET SINGLE APP
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApp(@PathVariable Long id) {
        ApplicationResponse response = appService.getApp(id);
        return ResponseEntity.ok(
                ApiResponse.<ApplicationResponse>builder()
                        .status(200)
                        .message("Application fetched successfully")
                        .data(response)
                        .build()
        );
    }

    // =====================================================
    // UPDATE APP
    // =====================================================
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApp(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationUpdateRequest request
    ) {
        ApplicationResponse response = appService.updateApp(id, request);
        log.info("Admin updated application: {}", response.getName());

        return ResponseEntity.ok(
                ApiResponse.<ApplicationResponse>builder()
                        .status(200)
                        .message("Application updated successfully")
                        .data(response)
                        .build()
        );
    }

    // =====================================================
    // DELETE APP
    // =====================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteApp(@PathVariable Long id) {
        appService.deleteApp(id);
        log.info("Admin deleted application id: {}", id);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Application deleted successfully")
                        .data(null)
                        .build()
        );
    }

    // =====================================================
    // UPLOAD ASSETS METHOD
    // =====================================================
    @PostMapping(
            value = "/{id}/assets",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<?>> uploadAssets(
            @PathVariable Long id,
            Authentication auth,
            HttpServletRequest request,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile,
            @RequestPart(value = "banner", required = false) MultipartFile bannerFile
    ) {
        try {
            // =====================================================
            // VALIDATION
            // =====================================================
            validateFile(logoFile);
            validateFile(bannerFile);

            // =====================================================
            // STORAGE IMPLEMENTATION
            // =====================================================
            String logoUrl = null;
            String bannerUrl = null;

            // ✅ S3 LOGO SAVE BLOCK
            if (logoFile != null && !logoFile.isEmpty()) {
                logoUrl = s3Service.uploadAppImage(logoFile);
            }

            // ✅ S3 BANNER SAVE BLOCK
            if (bannerFile != null && !bannerFile.isEmpty()) {
                bannerUrl = s3Service.uploadAppBanner(bannerFile);
            }

            // =====================================================
            // DB UPDATE
            // =====================================================
            ApplicationResponse updated = appService.updateAssets(id, logoUrl, bannerUrl);

            // =====================================================
            // RESPONSE
            // =====================================================
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("appId", id);
            responseData.put("logoUrl", updated.getLogoUrl());
            responseData.put("bannerUrl", updated.getBannerUrl());

            // =====================================================
            // AUDIT LOG
            // =====================================================
            auditService.log(1L, "APP_ASSETS_UPDATED", "Uploaded assets for app id: " + id, request);
            log.info("Assets uploaded to S3 cloud for application id: {}", id);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status(200)
                            .message("Application visual assets updated successfully.")
                            .data(responseData)
                            .build()
            );

        } catch (Exception e) {
            log.error("Asset upload failed", e);
            return ResponseEntity
                    .status(500)
                    .body(
                            ApiResponse.builder()
                                    .status(500)
                                    .message(e.getMessage())
                                    .build()
                    );
        }
    }

    // =====================================================
    // FILE VALIDATION
    // =====================================================
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        // SIZE VALIDATION
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds 5MB limit");
        }

        // MIME TYPE VALIDATION
        String contentType = file.getContentType();
        List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp");

        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG and WEBP files allowed");
        }
    }

}