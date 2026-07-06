package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.KycStatus;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.AdminApiResponse;
import in.bawvpl.Authify.io.AdminKycResponse;

import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import in.bawvpl.Authify.service.S3Service;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v1.0/admin/kyc")
@RequiredArgsConstructor
//@CrossOrigin("*")
public class AdminKycController {

    private final KycRepository kycRepository;

    private final UserRepository userRepository;

    private final S3Service s3Service;

    private static final String BASE_URL =
            "http://43.205.116.38:8080";

    // =====================================================
    // GET ALL KYC
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/all")
    public ResponseEntity<AdminApiResponse<List<AdminKycResponse>>> getAllKyc() {

        List<AdminKycResponse> list =

                kycRepository
                        .findAllByOrderByUploadedAtDesc()
                        .stream()
                        .map(this::mapResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(
                AdminApiResponse.success(list)
        );
    }

    // =====================================================
    // GET PENDING KYC
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/pending")
    public ResponseEntity<AdminApiResponse<List<AdminKycResponse>>> getPendingKyc() {

        List<AdminKycResponse> list =

                kycRepository
                        .findByStatus(KycStatus.PENDING)
                        .stream()
                        .map(this::mapResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(
                AdminApiResponse.success(list)
        );
    }

    // =====================================================
    // GET KYC BY USER ID
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/{kycId:\\d+}")
    public ResponseEntity<AdminApiResponse<AdminKycResponse>> getKycById(
            @PathVariable Long kycId
    ) {

        KycEntity kyc =
                kycRepository.findById(kycId)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        NOT_FOUND,

                                        "KYC not found"
                                )
                        );


        return ResponseEntity.ok(

                AdminApiResponse.success(
                        mapResponse(kyc)
                )
        );
    }

    // =====================================================
    // VERIFY KYC
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @PutMapping("/verify/{kycId:\\d+}")
    public ResponseEntity<AdminApiResponse<String>> verifyKyc(

            @PathVariable Long kycId,

            Authentication authentication
    ) {

        KycEntity kyc =
                kycRepository.findById(kycId)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        NOT_FOUND,

                                        "KYC not found"
                                )
                        );

        UserEntity user =
                kyc.getUser();

        if (user == null) {

            throw new ResponseStatusException(

                    NOT_FOUND,

                    "Associated user not found"
            );
        }

        // =====================================================
        // UPDATE KYC
        // =====================================================

        kyc.setStatus(
                KycStatus.VERIFIED
        );

        kyc.setCompleted(true);

        kyc.setVerifiedAt(
                LocalDateTime.now()
        );

        kyc.setReviewedAt(
                LocalDateTime.now()
        );

        if (

                authentication != null &&

                        authentication.getName() != null
        ) {

            kyc.setVerifiedBy(
                    authentication.getName()
            );

            kyc.setReviewedBy(
                    authentication.getName()
            );
        }

        kyc.setRejectionReason(null);

        kycRepository.save(kyc);

        // =====================================================
        // UPDATE USER
        // =====================================================

        user.setIsKycVerified(true);

        userRepository.save(user);

        return ResponseEntity.ok(

                AdminApiResponse.successMessage(
                        "KYC VERIFIED SUCCESSFULLY"
                )
        );
    }

    // =====================================================
    // REJECT KYC
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @PutMapping("/reject/{kycId:\\d+}")
    public ResponseEntity<AdminApiResponse<String>> rejectKyc(

            @PathVariable Long kycId,

            @RequestParam(required = false)
            String reason,

            Authentication authentication
    ) {

        KycEntity kyc =
                kycRepository.findById(kycId)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        NOT_FOUND,

                                        "KYC not found"
                                )
                        );

        UserEntity user =
                kyc.getUser();

        if (user == null) {

            throw new ResponseStatusException(

                    NOT_FOUND,

                    "Associated user not found"
            );
        }

        // =====================================================
        // UPDATE KYC
        // =====================================================

        kyc.setStatus(
                KycStatus.REJECTED
        );

        kyc.setCompleted(false);

        kyc.setReviewedAt(
                LocalDateTime.now()
        );

        if (

                authentication != null &&

                        authentication.getName() != null
        ) {

            kyc.setReviewedBy(
                    authentication.getName()
            );
        }

        // =====================================================
        // REJECTION REASON
        // =====================================================

        if (

                reason != null &&

                        !reason.isBlank()
        ) {

            kyc.setRejectionReason(
                    reason.trim()
            );

        } else {

            kyc.setRejectionReason(
                    "KYC rejected"
            );
        }

        kycRepository.save(kyc);

        // =====================================================
        // UPDATE USER
        // =====================================================

        user.setIsKycVerified(false);

        userRepository.save(user);

        return ResponseEntity.ok(

                AdminApiResponse.successMessage(
                        "KYC REJECTED"
                )
        );
    }

    // =====================================================
    // RESPONSE MAPPER
    // =====================================================

    private AdminKycResponse mapResponse(
            KycEntity kyc
    ) {

        UserEntity user =
                kyc.getUser();

        String frontUrl =
                buildSecureFileUrl(
                        kyc.getFrontDocumentUrl()
                );

        String backUrl =
                buildSecureFileUrl(
                        kyc.getBackDocumentUrl()
                );

        String selfieUrl =
                buildSecureFileUrl(
                        kyc.getSelfieUrl()
                );

        String applicantName =
                user != null &&
                        user.getEntityName() != null
                        ? user.getEntityName()
                        : "";

        String phoneNumber =
                user != null &&
                        user.getPhoneNumber() != null
                        ? user.getPhoneNumber()
                        : "";

        String email =
                user != null &&
                        user.getEmail() != null
                        ? user.getEmail()
                        : "";

        String userId =
                user != null
                        ? user.getUserId()
                        : null;

        String profilePhoto =
                user != null &&
                        user.getPhotoUrl() != null
                        ? buildFileUrl(user.getPhotoUrl())
                        : null;

        String kycStatus =
                kyc.getStatus() != null
                        ? kyc.getStatus().name()
                        : "PENDING";

        AdminKycResponse.Applicant applicant =

                AdminKycResponse.Applicant.builder()

                        .userId(userId)

                        .fullName(applicantName)

                        .name(applicantName)

                        .applicantName(applicantName)

                        .email(email)

                        .phoneNumber(phoneNumber)

                        .mobile(phoneNumber)

                        .address(null)

                        .profilePhotoUrl(profilePhoto)

                        .build();

        return AdminKycResponse.builder()

                .id(
                        kyc.getId()
                )

                .userId(userId)

                .applicantName(applicantName)

                .kycStatus(kycStatus)

                .name(applicantName)

                .email(email)

                .phoneNumber(phoneNumber)

                .address(null)

                .applicant(applicant)

                .user(applicant)

                .documentType(
                        kyc.getDocumentType()
                )

                .documentNumber(
                        kyc.getDocumentNumber()
                )

                .status(kycStatus)

                .completed(
                        kyc.getCompleted() != null
                                ? kyc.getCompleted()
                                : false
                )

                .rejectionReason(
                        kyc.getRejectionReason()
                )

                .uploadedAt(
                        kyc.getUploadedAt()
                )

                .verifiedAt(
                        kyc.getVerifiedAt()
                )

                .reviewedAt(
                        kyc.getReviewedAt()
                )

                .submittedAt(
                        kyc.getSubmittedAt()
                )

                .verifiedBy(
                        kyc.getVerifiedBy()
                )

                .reviewedBy(
                        kyc.getReviewedBy()
                )

                .frontDocumentUrl(frontUrl)

                .backDocumentUrl(backUrl)

                .selfieUrl(selfieUrl)

                .documentUrl(
                        buildSecureFileUrl(
                                kyc.getFilePath()
                        )
                )

                .documentFile(
                        buildSecureFileUrl(
                                kyc.getFilePath()
                        )
                )

                .aadhaarFrontUrl(
                        buildSecureFileUrl(
                                kyc.getAadhaarFrontUrl()
                        )
                )

                .aadhaarBackUrl(
                        buildSecureFileUrl(
                                kyc.getAadhaarBackUrl()
                        )
                )

                .panCardUrl(
                        buildSecureFileUrl(
                                kyc.getPanCardUrl()
                        )
                )

                .passportUrl(
                        buildSecureFileUrl(
                                kyc.getPassportUrl()
                        )
                )

                .drivingLicenseUrl(
                        buildSecureFileUrl(
                                kyc.getDrivingLicenseUrl()
                        )
                )

                .livePhotoUrl(
                        buildSecureFileUrl(
                                kyc.getLivePhotoUrl()
                        )
                )

                .mimeType(
                        kyc.getMimeType()
                )

                .contentType(
                        kyc.getContentType()
                )

                .riskFlag(
                        kyc.getRiskFlag()
                )

                .build();
    }

    // =====================================================
    // FILE URL BUILDER
    // =====================================================

    private String buildFileUrl(
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

    private String buildSecureFileUrl(
            String path
    ) {

        if (
                path == null ||
                        path.isBlank()
        ) {
            return null;
        }

        try {

            if (
                    path.startsWith("https://") &&
                            path.contains(".s3.")
            ) {

                return s3Service.generatePresignedGetUrl(
                        path
                );
            }

            return buildFileUrl(path);

        } catch (Exception e) {

            return buildFileUrl(path);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/presign")
    public ResponseEntity<AdminApiResponse<String>> getPresignedUrl(
            @RequestParam String storedUrl
    ) {

        try {

            String presignedUrl =
                    s3Service.generatePresignedGetUrl(
                            storedUrl
                    );

            return ResponseEntity.ok(
                    AdminApiResponse.success(
                            presignedUrl
                    )
            );

        } catch (Exception e) {

            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Failed to generate presigned URL",
                    e
            );
        }
    }
}