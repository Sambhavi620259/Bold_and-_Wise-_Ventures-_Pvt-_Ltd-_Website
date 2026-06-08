package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.KycStatus;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.AdminKycResponse;
import in.bawvpl.Authify.io.ApiResponse;

import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import in.bawvpl.Authify.service.S3Service; // Assuming target service package structure

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1.0/kyc")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin("*")
public class KycController {

    private final KycRepository kycRepository;

    private final UserRepository userRepository;

    private final S3Service s3Service;

    private static final String BASE_URL =
            "http://43.205.116.38:8080";

    // =====================================================
    // HELPERS
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

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateKycInput(
            String documentType,
            String documentNumber,
            MultipartFile file
    ) {

        if (
                documentType == null ||
                        documentType.trim().isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document type required"
            );
        }

        if (
                documentNumber == null ||
                        documentNumber.trim().isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document number required"
            );
        }

        if (
                file == null ||
                        file.isEmpty()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document file required"
            );
        }
    }

    // =====================================================
    // UPLOAD
    // =====================================================

    @Transactional
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> upload(
            Authentication auth,
            @RequestParam
            String documentType,
            @RequestParam
            String documentNumber,
            @RequestParam
            MultipartFile file
    ) {

        try {

            validateKycInput(
                    documentType,
                    documentNumber,
                    file
            );

            String email =
                    getEmail(auth);

            UserEntity user =
                    getUser(email);

            KycEntity kyc =
                    kycRepository
                            .findByUser(user)
                            .orElse(new KycEntity());

            String savedPath =
                    s3Service.uploadFile(file);

            kyc.setUser(user);
            kyc.setDocumentType(documentType.trim());
            kyc.setDocumentNumber(documentNumber.trim());
            kyc.setFilePath(savedPath);
            kyc.setFrontDocumentUrl(savedPath);
            kyc.setBackDocumentUrl(savedPath);
            kyc.setStatus(KycStatus.PENDING);
            kyc.setCompleted(false);
            kyc.setUploadedAt(LocalDateTime.now());
            kyc.setReviewedAt(null);
            kyc.setVerifiedAt(null);
            kyc.setReviewedBy(null);
            kyc.setVerifiedBy(null);
            kyc.setRejectionReason(null);

            kycRepository.save(kyc);

            user.setIsKycVerified(false);
            user.setKycStatus("PENDING");
            userRepository.save(user);

            return ok(
                    "KYC uploaded successfully",
                    savedPath
            );

        } catch (Exception e) {
            log.error("KYC upload failed", e);
            return error(e);
        }
    }

    // =====================================================
    // RESUBMIT
    // =====================================================

    @PostMapping("/resubmit")
    @Transactional
    public ResponseEntity<ApiResponse<String>> resubmit(
            Authentication auth
    ) {

        try {
            String email = getEmail(auth);

            UserEntity user = getUser(email);

            KycEntity kyc = kycRepository
                    .findByUser(user)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "KYC not found"
                            )
                    );

            kyc.setStatus(KycStatus.PENDING);
            kyc.setReviewedAt(null);
            kyc.setVerifiedAt(null);
            kyc.setRejectionReason(null);

            kycRepository.save(kyc);

            return ok(
                    "KYC resubmitted successfully",
                    null
            );

        } catch (Exception e) {
            log.error("KYC resubmit failed", e);
            return error(e);
        }
    }

    // =====================================================
    // GET MY KYC
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AdminKycResponse>> getMyKyc(
            Authentication auth
    ) {

        try {

            String email =
                    getEmail(auth);

            UserEntity user =
                    getUser(email);

            KycEntity kyc =
                    kycRepository
                            .findByUser(user)
                            .orElse(null);

            if (kyc == null) {
                return ok(
                        "No KYC found",
                        null
                );
            }

            return ok(
                    "KYC fetched successfully",
                    mapKycResponse(kyc)
            );

        } catch (Exception e) {
            log.error("Fetch KYC failed", e);
            return error(e);
        }
    }


    @GetMapping("/presign-url")
    public ResponseEntity<ApiResponse<String>> getPresignedUrl(
            Authentication auth,
            @RequestParam String storedUrl
    ) {

        try {

            String email =
                    getEmail(auth);

            getUser(email);

            String presignedUrl =
                    s3Service.generatePresignedGetUrl(
                            storedUrl
                    );

            return ok(
                    "Presigned URL generated",
                    presignedUrl
            );

        } catch (Exception e) {

            log.error(
                    "Presigned URL generation failed",
                    e
            );

            return error(e);
        }
    }
    // =====================================================
    // MAPPER
    // =====================================================

    private AdminKycResponse mapKycResponse(
            KycEntity kyc
    ) {

        UserEntity user =
                kyc.getUser();

        String documentUrl = null;

        if (
                kyc.getFilePath() != null &&
                        !kyc.getFilePath().isBlank()
        ) {

            documentUrl =
                    kyc.getFilePath();
        }

        return AdminKycResponse.builder()
                .id(kyc.getId())
                .userId(user != null ? user.getUserId() : null)
                .name(user != null ? user.getEntityName() : "")
                .applicantName(user != null ? user.getEntityName() : "")
                .email(user != null ? user.getEmail() : "")
                .phoneNumber(user != null ? user.getPhoneNumber() : "")
                .documentType(kyc.getDocumentType())
                .documentNumber(kyc.getDocumentNumber())
                .status(kyc.getStatus().name())
                .kycStatus(kyc.getStatus().name())
                .completed(Boolean.TRUE.equals(kyc.getCompleted()))
                .rejectionReason(kyc.getRejectionReason())
                .uploadedAt(kyc.getUploadedAt())
                .verifiedAt(kyc.getVerifiedAt())
                .reviewedAt(kyc.getReviewedAt())
                .submittedAt(kyc.getSubmittedAt())
                .verifiedBy(kyc.getVerifiedBy())
                .reviewedBy(kyc.getReviewedBy())
                .documentUrl(
                        secureUrl(documentUrl)
                )

                .documentFile(
                        secureUrl(documentUrl)
                )

                .frontDocumentUrl(
                        secureUrl(
                                kyc.getFrontDocumentUrl()
                        )
                )

                .backDocumentUrl(
                        secureUrl(
                                kyc.getBackDocumentUrl()
                        )
                )

                .aadhaarFrontUrl(
                        secureUrl(
                                kyc.getAadhaarFrontUrl()
                        )
                )

                .aadhaarBackUrl(
                        secureUrl(
                                kyc.getAadhaarBackUrl()
                        )
                )

                .panCardUrl(
                        secureUrl(
                                kyc.getPanCardUrl()
                        )
                )

                .passportUrl(
                        secureUrl(
                                kyc.getPassportUrl()
                        )
                )

                .drivingLicenseUrl(
                        secureUrl(
                                kyc.getDrivingLicenseUrl()
                        )
                )

                .selfieUrl(
                        secureUrl(
                                kyc.getSelfieUrl()
                        )
                )

                .livePhotoUrl(
                        secureUrl(
                                kyc.getLivePhotoUrl()
                        )
                )
                .mimeType(kyc.getMimeType())
                .contentType(kyc.getContentType())
                .riskFlag(kyc.getRiskFlag())
                .build();
    }



    // =====================================================
    // RESPONSE
    // =====================================================

    private <T> ResponseEntity<ApiResponse<T>> ok(
            String message,
            T data
    ) {

        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .success(true)
                        .status(200)
                        .message(message)
                        .data(data)
                        .build()
        );
    }

    private <T> ResponseEntity<ApiResponse<T>> error(
            Exception e
    ) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse.<T>builder()
                                .success(false)
                                .status(500)
                                .message(e.getMessage())
                                .build()
                );
    }

    private String secureUrl(
            String url
    ) {

        if (
                url == null ||
                        url.isBlank()
        ) {
            return null;
        }

        try {

            if (
                    url.startsWith("https://") &&
                            url.contains(".s3.")
            ) {

                return s3Service.generatePresignedGetUrl(
                        url
                );
            }

            return url;

        } catch (Exception e) {

            log.warn(
                    "Failed to presign URL: {}",
                    url,
                    e
            );

            return url;
        }
    }
}