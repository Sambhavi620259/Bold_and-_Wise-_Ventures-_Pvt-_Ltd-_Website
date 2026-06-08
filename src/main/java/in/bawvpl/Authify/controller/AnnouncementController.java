package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.AnnouncementEntity;

import in.bawvpl.Authify.io.ApiResponse;

import in.bawvpl.Authify.service.AnnouncementService;

import in.bawvpl.Authify.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;



import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final S3Service s3Service;

    // =====================================================
    // ACTIVE ANNOUNCEMENTS
    // =====================================================

    @GetMapping("/api/v1.0/announcements/active")
    public ResponseEntity<ApiResponse<List<AnnouncementEntity>>>
    getActiveAnnouncements() {

        return ResponseEntity.ok(

                ApiResponse.<List<AnnouncementEntity>>builder()

                        .success(true)

                        .status(200)

                        .message("Active announcements fetched successfully")

                        .data(
                                announcementService
                                        .getActiveAnnouncements()
                        )

                        .build()
        );
    }

    // =====================================================
    // ADMIN LIST
    // =====================================================

    @GetMapping("/api/v1.0/admin/announcements")
    public ResponseEntity<ApiResponse<Page<AnnouncementEntity>>>
    getAllAnnouncements(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return ResponseEntity.ok(

                ApiResponse.<Page<AnnouncementEntity>>builder()

                        .success(true)

                        .status(200)

                        .message("Announcements fetched successfully")

                        .data(

                                announcementService.getAll(

                                        page,

                                        size
                                )
                        )

                        .build()
        );
    }

    // =====================================================
    // CREATE
    // =====================================================

    @PostMapping("/api/v1.0/admin/announcements")
    public ResponseEntity<ApiResponse<AnnouncementEntity>>
    createAnnouncement(

            @RequestBody
            Map<String, Object> body
    ) {

        return ResponseEntity.ok(

                ApiResponse.<AnnouncementEntity>builder()

                        .success(true)

                        .status(200)

                        .message("Announcement created successfully")

                        .data(
                                announcementService
                                        .create(body)
                        )

                        .build()
        );
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @PatchMapping("/api/v1.0/admin/announcements/{id}")
    public ResponseEntity<ApiResponse<AnnouncementEntity>>
    updateAnnouncement(

            @PathVariable
            Long id,

            @RequestBody
            Map<String, Object> body
    ) {

        return ResponseEntity.ok(

                ApiResponse.<AnnouncementEntity>builder()

                        .success(true)

                        .status(200)

                        .message("Announcement updated successfully")

                        .data(

                                announcementService.update(

                                        id,

                                        body
                                )
                        )

                        .build()
        );
    }

    // =====================================================
    // DELETE
    // =====================================================

    @DeleteMapping("/api/v1.0/admin/announcements/{id}")
    public ResponseEntity<ApiResponse<Object>>
    deleteAnnouncement(

            @PathVariable
            Long id
    ) {

        announcementService.delete(id);

        return ResponseEntity.ok(

                ApiResponse.builder()

                        .success(true)

                        .status(200)

                        .message("Announcement deleted successfully")

                        .data(null)

                        .build()
        );
    }

    // =====================================================
    // UPLOAD ASSET
    // =====================================================

    @PostMapping(
            value = "/api/v1.0/admin/announcements/{id}/assets",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<AnnouncementEntity>>
    uploadBanner(

            @PathVariable
            Long id,

            @RequestParam("file")
            MultipartFile file
    ) {

        try {

            // =====================================================
            // VALIDATION
            // =====================================================

            if (
                    file == null ||
                            file.isEmpty()
            ) {

                throw new RuntimeException(
                        "File required"
                );
            }

            // =====================================================
            // UPLOAD TO S3
            // =====================================================

            String bannerUrl =
                    s3Service.uploadAnnouncementImage(
                            file
                    );

            // =====================================================
            // UPDATE DATABASE
            // =====================================================

            AnnouncementEntity updated =
                    announcementService.updateBanner(
                            id,
                            bannerUrl
                    );

            log.info(
                    "Announcement banner uploaded: {}",
                    bannerUrl
            );

            return ResponseEntity.ok(

                    ApiResponse.<AnnouncementEntity>builder()

                            .success(true)

                            .status(200)

                            .message(
                                    "Announcement asset uploaded successfully"
                            )

                            .data(updated)

                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Announcement banner upload failed",
                    e
            );

            return ResponseEntity.internalServerError()

                    .body(

                            ApiResponse.<AnnouncementEntity>builder()

                                    .success(false)

                                    .status(500)

                                    .message(
                                            "Announcement asset upload failed"
                                    )

                                    .build()
                    );
        }
    }
}