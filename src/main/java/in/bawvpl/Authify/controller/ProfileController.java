package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.KycStatus;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.entity.UserProfileHistory;

import in.bawvpl.Authify.io.ProfileResponse;

import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.repository.UserProfileHistoryRepository;

import in.bawvpl.Authify.service.ProfileService;

import in.bawvpl.Authify.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.server.ResponseStatusException;



import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1.0/profile")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin("*")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserProfileHistoryRepository userProfileHistoryRepository;

    private final KycRepository kycRepository;

    private final ProfileService profileService;

    private final S3Service s3Service;

    private static final String BASE_URL =
            "http://43.205.116.38:8080";

    // =====================================================
    // AUTH EMAIL
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
    // URL NORMALIZATION
    // =====================================================

    private String normalizeUrl(
            String path
    ) {

        if (

                path == null ||

                        path.isBlank()
        ) {

            return null;
        }

        if (

                path.startsWith("http://") ||

                        path.startsWith("https://")
        ) {

            return path;
        }

        if (!path.startsWith("/")) {

            path = "/" + path;
        }

        return BASE_URL + path;
    }

    // =====================================================
    // PHOTO URL
    // =====================================================

    private String buildPhotoUrl(
            String file
    ) {

        if (

                file == null ||

                        file.isBlank()
        ) {

            return null;
        }

        if (

                file.startsWith("http://") ||

                        file.startsWith("https://")
        ) {

            return file;
        }

        if (file.startsWith("/uploads/")) {

            return BASE_URL + file;
        }

        return BASE_URL +

                "/uploads/" +

                file;
    }

    // =====================================================
    // GET PROFILE
    // =====================================================

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(
            Authentication auth
    ) {

        try {

            String email =
                    getEmail(auth);

            UserEntity user =
                    userRepository
                            .findByEmailIgnoreCase(email)

                            .orElseThrow(() ->

                                    new ResponseStatusException(

                                            HttpStatus.NOT_FOUND,

                                            "User not found"
                                    )
                            );

            Optional<KycEntity> kycOpt =
                    kycRepository.findByUser(user);

            boolean isKycVerified = false;

            String kycStatus = "NOT_SUBMITTED";

            String rejectionReason = null;

            ProfileResponse.Kyc kycData = null;

            // =====================================================
            // KYC
            // =====================================================

            if (kycOpt.isPresent()) {

                KycEntity kyc =
                        kycOpt.get();

                if (kyc.getStatus() != null) {

                    kycStatus =
                            kyc.getStatus().name();
                }

                isKycVerified =
                        kyc.getStatus() ==
                                KycStatus.VERIFIED;

                rejectionReason =
                        kyc.getRejectionReason();

                String documentUrl = null;

                if (

                        kyc.getFilePath() != null &&

                                !kyc.getFilePath().isBlank()
                ) {

                    documentUrl =
                            normalizeUrl(
                                    kyc.getFilePath()
                            );
                }

                kycData =
                        ProfileResponse.Kyc.builder()

                                .status(
                                        kycStatus
                                )

                                .documentType(
                                        kyc.getDocumentType()
                                )

                                .documentNumber(
                                        kyc.getDocumentNumber()
                                )

                                .filePath(
                                        documentUrl
                                )

                                .documentUrl(
                                        documentUrl
                                )

                                .rejectionReason(
                                        rejectionReason
                                )

                                .build();
            }

            // =====================================================
            // PHOTO
            // =====================================================

            String photoUrl =
                    buildPhotoUrl(
                            user.getPhotoUrl()
                    );

            // =====================================================
            // RESPONSE
            // =====================================================

            ProfileResponse response =
                    ProfileResponse.builder()

                            .userId(
                                    user.getUserId()
                            )

                            .name(
                                    user.getEntityName()
                            )

                            .fullName(
                                    user.getEntityName()
                            )

                            .email(
                                    user.getEmail()
                            )

                            .phoneNumber(
                                    user.getPhoneNumber()
                            )

                            .accountVerified(

                                    Boolean.TRUE.equals(
                                            user.getEmailVerified()
                                    )
                            )

                            .emailVerified(

                                    Boolean.TRUE.equals(
                                            user.getEmailVerified()
                                    )
                            )

                            .phoneVerified(

                                    Boolean.TRUE.equals(
                                            user.getPhoneVerified()
                                    )
                            )

                            .kycVerified(
                                    isKycVerified
                            )

                            .kycStatus(
                                    kycStatus
                            )

                            .referralCode(
                                    user.getReferralCode()
                            )

                            .photoUrl(
                                    photoUrl
                            )

                            .profilePhotoUrl(
                                    photoUrl
                            )

                            .avatarUrl(
                                    photoUrl
                            )

                            .kyc(
                                    kycData
                            )

                            .build();

            response.normalize();

            Map<String, Object> result =
                    new HashMap<>();

            result.put(
                    "success",
                    true
            );

            result.put(
                    "profile",
                    response
            );

            result.put(
                    "user",
                    response
            );

            result.put(
                    "kycStatus",
                    kycStatus
            );

            result.put(
                    "kycVerified",
                    isKycVerified
            );

            result.put(
                    "kycRejectionReason",
                    rejectionReason
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            log.error(
                    "Profile fetch failed",
                    e
            );

            Map<String, Object> error =
                    new HashMap<>();

            error.put(
                    "success",
                    false
            );

            error.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(

            Authentication auth,

            @RequestBody
            Map<String, String> body
    ) {

        try {

            String email =
                    getEmail(auth);

            UserEntity user =
                    userRepository
                            .findByEmailIgnoreCase(email)

                            .orElseThrow(() ->

                                    new ResponseStatusException(

                                            HttpStatus.NOT_FOUND,

                                            "User not found"
                                    )
                            );
            String oldEmail =
                    user.getEmail();

            String oldPhone =
                    user.getPhoneNumber();

            // =====================================================
            // NAME
            // =====================================================

            if (

                    body.containsKey("name") &&

                            body.get("name") != null
            ) {

                user.setEntityName(
                        body.get("name").trim()
                );
            }


            if (body.containsKey("email")
                    && body.get("email") != null
                    && !body.get("email").trim().isEmpty()) {

                String newEmail =
                        body.get("email")
                                .trim()
                                .toLowerCase();

                Optional<UserEntity> existingUser =
                        userRepository.findByEmailIgnoreCase(
                                newEmail
                        );

                if (
                        existingUser.isPresent() &&
                                !existingUser.get().getId().equals(
                                        user.getId()
                                )
                ) {

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email already registered"
                    );
                }

                user.setEmail(newEmail);
            }
            // =====================================================
            // PHONE
            // =====================================================

            if (

                    body.containsKey("phoneNumber") &&

                            body.get("phoneNumber") != null
            ) {

                user.setPhoneNumber(
                        body.get("phoneNumber").trim()
                );
            }

            // =====================================================
            // ADDRESS
            // =====================================================

            if (

                    body.containsKey("address") &&

                            body.get("address") != null
            ) {

                user.setAddress(
                        body.get("address").trim()
                );
            }

            userRepository.save(user);

            boolean emailChanged =
                    !java.util.Objects.equals(
                            oldEmail,
                            user.getEmail()
                    );

            boolean phoneChanged =
                    !java.util.Objects.equals(
                            oldPhone,
                            user.getPhoneNumber()
                    );

            if (emailChanged || phoneChanged) {

                UserProfileHistory history =
                        UserProfileHistory.builder()

                                .userId(
                                        user.getUserId()
                                )

                                .oldEmail(
                                        oldEmail
                                )

                                .newEmail(
                                        user.getEmail()
                                )

                                .oldPhone(
                                        oldPhone
                                )

                                .newPhone(
                                        user.getPhoneNumber()
                                )

                                .changedBy(
                                        user.getUserId()
                                )

                                .build();

                userProfileHistoryRepository.save(
                        history
                );
            }

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "Profile updated successfully"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error(
                    "Profile update failed",
                    e
            );

            Map<String, Object> error =
                    new HashMap<>();

            error.put(
                    "success",
                    false
            );

            error.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

    // =====================================================
    // UPLOAD PHOTO
    // =====================================================

    @PostMapping("/upload-photo")
    public ResponseEntity<Map<String, Object>> uploadPhoto(

            @RequestParam("file")
            MultipartFile file,

            Authentication auth
    ) {

        try {

            String email =
                    getEmail(auth);

            if (

                    file == null ||

                            file.isEmpty()
            ) {

                Map<String, Object> error =
                        new HashMap<>();

                error.put(
                        "success",
                        false
                );

                error.put(
                        "message",
                        "File is empty"
                );

                return ResponseEntity
                        .badRequest()
                        .body(error);
            }

            UserEntity user =
                    userRepository
                            .findByEmailIgnoreCase(email)

                            .orElseThrow(() ->

                                    new RuntimeException(
                                            "User not found"
                                    )
                            );

            String originalName =
                    Objects.requireNonNull(
                            file.getOriginalFilename()
                    );

            String type = file.getContentType();

            if (
                    type == null ||
                            (!type.equals("image/jpeg") &&
                                    !type.equals("image/png") &&
                                    !type.equals("image/jpg"))
            ) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Only JPG and PNG allowed"
                        )
                );
            }

            String photoUrl =
                    s3Service.uploadProfileImage(file);
            user.setPhotoUrl(
                    photoUrl
            );

            userRepository.save(user);

            String fullPhotoUrl =
                    user.getPhotoUrl();

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "photoUrl",
                    fullPhotoUrl
            );

            response.put(
                    "profilePhotoUrl",
                    fullPhotoUrl
            );

            response.put(
                    "avatarUrl",
                    fullPhotoUrl
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error(
                    "Photo upload failed",
                    e
            );

            Map<String, Object> error =
                    new HashMap<>();

            error.put(
                    "success",
                    false
            );

            error.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

    // =====================================================
// REUPLOAD KYC
// =====================================================

    @PostMapping("/kyc/reupload")
    public ResponseEntity<Map<String, Object>> reuploadKyc(

            @RequestParam("documentType")
            String documentType,

            @RequestParam("documentNumber")
            String documentNumber,

            @RequestParam("file")
            MultipartFile file,

            Authentication auth
    ) {

        try {

            return ResponseEntity.ok(

                    profileService.reuploadKyc(

                            getEmail(auth),

                            documentType,

                            documentNumber,

                            file
                    )
            );

        } catch (Exception e) {

            log.error(
                    "KYC reupload failed",
                    e
            );

            Map<String, Object> error =
                    new HashMap<>();

            error.put(
                    "success",
                    false
            );

            error.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

    // =====================================================
    // KYC REJECTION REASON
    // =====================================================

    @GetMapping("/kyc-reason")
    public ResponseEntity<Map<String, Object>> kycReason(
            Authentication auth
    ) {

        try {

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(

                    "reason",

                    profileService.getKycRejectionReason(
                            getEmail(auth)
                    )
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error(
                    "KYC reason fetch failed",
                    e
            );

            Map<String, Object> error =
                    new HashMap<>();

            error.put(
                    "success",
                    false
            );

            error.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }
}