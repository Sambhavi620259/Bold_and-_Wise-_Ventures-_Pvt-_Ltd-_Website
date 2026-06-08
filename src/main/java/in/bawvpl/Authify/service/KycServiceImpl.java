package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.KycStatus;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.AdminKycResponse;
import in.bawvpl.Authify.io.KycDetailResponse;

import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KycServiceImpl implements KycService {

    private final KycRepository kycRepository;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final S3Service s3Service;

    private static final String BASE_URL =
            "http://43.205.116.38:8080";

    // =====================================================
    // UPLOAD
    // =====================================================

    @Override
    public String uploadKyc(

            String email,

            String documentType,

            String documentNumber,

            MultipartFile file
    ) {

        validateKycInput(
                documentType,
                documentNumber,
                file
        );

        UserEntity user =
                getUser(email);

        Optional<KycEntity> existing =
                kycRepository.findByUser(user);

        if (
                existing.isPresent() &&
                        existing.get().getStatus() == KycStatus.PENDING
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "KYC already under review"
            );
        }

        try {

            String filePath =
                    s3Service.uploadFile(file);

            KycEntity kyc =
                    existing.orElse(
                            new KycEntity()
                    );

            kyc.setUser(user);

            kyc.setDocumentType(
                    documentType.trim()
            );

            kyc.setDocumentNumber(
                    documentNumber.trim()
            );

            kyc.setFilePath(filePath);

            kyc.setFrontDocumentUrl(filePath);

            kyc.setBackDocumentUrl(filePath);

            kyc.setStatus(
                    KycStatus.PENDING
            );

            kyc.setCompleted(false);

            kyc.setUploadedAt(
                    LocalDateTime.now()
            );

            kyc.setSubmittedAt(
                    LocalDateTime.now()
            );

            kyc.setUpdatedAt(
                    LocalDateTime.now()
            );

            kycRepository.save(kyc);

            user.setIsKycVerified(false);

            user.setKycStatus("PENDING");

            userRepository.save(user);

            return "KYC uploaded successfully";

        } catch (Exception e) {

            log.error(
                    "Upload failed",
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
        }
    }

    // =====================================================
    // RESUBMIT
    // =====================================================

    @Override
    public String resubmitKyc(

            String email,

            String documentType,

            String documentNumber,

            MultipartFile file
    ) {

        validateKycInput(
                documentType,
                documentNumber,
                file
        );

        UserEntity user =
                getUser(email);

        KycEntity kyc =
                getKyc(user);

        try {

            String filePath =
                    s3Service.uploadFile(file);

            kyc.setDocumentType(
                    documentType.trim()
            );

            kyc.setDocumentNumber(
                    documentNumber.trim()
            );

            kyc.setFilePath(filePath);

            kyc.setFrontDocumentUrl(filePath);

            kyc.setBackDocumentUrl(filePath);

            kyc.setStatus(
                    KycStatus.PENDING
            );

            kyc.setCompleted(false);

            kyc.setRejectionReason(null);

            kyc.setUpdatedAt(
                    LocalDateTime.now()
            );

            kycRepository.save(kyc);

            user.setIsKycVerified(false);

            user.setKycStatus("PENDING");

            userRepository.save(user);

            return "KYC resubmitted successfully";

        } catch (Exception e) {

            log.error(
                    "Resubmit failed",
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
        }
    }

    // =====================================================
    // MY KYC
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public KycEntity getMyKyc(
            String email
    ) {

        UserEntity user =
                getUser(email);

        return kycRepository
                .findByUser(user)
                .orElse(null);
    }

    @Override
    public Optional<KycEntity> findByUserEmail(
            String email
    ) {

        UserEntity user =
                getUser(email);

        return kycRepository.findByUser(user);
    }

    // =====================================================
    // DETAIL RESPONSE
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public KycDetailResponse getMyKycDetail(
            String email
    ) {

        KycEntity kyc =
                getMyKyc(email);

        if (kyc == null) {
            return null;
        }

        UserEntity user =
                kyc.getUser();

        return KycDetailResponse.builder()

                .id(kyc.getId())

                .userId(user.getId())

                .fullName(user.getEntityName())

                .applicantName(user.getEntityName())

                .name(user.getEntityName())

                .email(user.getEmail())

                .phone(user.getPhoneNumber())

                .phoneNumber(user.getPhoneNumber())

                .address(user.getAddress())

                .country("India")

                .kycStatus(
                        safeStatus(
                                kyc.getStatus()
                        )
                )

                .status(
                        safeStatus(
                                kyc.getStatus()
                        )
                )

                .documentType(
                        kyc.getDocumentType()
                )

                .documentNumber(
                        kyc.getDocumentNumber()
                )

                .aadhaarFrontUrl(
                        resolveUrl(
                                kyc.getAadhaarFrontUrl()
                        )
                )

                .aadhaarBackUrl(
                        resolveUrl(
                                kyc.getAadhaarBackUrl()
                        )
                )

                .panCardUrl(
                        resolveUrl(
                                kyc.getPanCardUrl()
                        )
                )

                .passportUrl(
                        resolveUrl(
                                kyc.getPassportUrl()
                        )
                )

                .drivingLicenseUrl(
                        resolveUrl(
                                kyc.getDrivingLicenseUrl()
                        )
                )

                .selfieUrl(
                        resolveUrl(
                                kyc.getSelfieUrl()
                        )
                )

                .livePhotoUrl(
                        resolveUrl(
                                kyc.getLivePhotoUrl()
                        )
                )

                .frontDocumentUrl(
                        resolveUrl(
                                kyc.getFrontDocumentUrl()
                        )
                )

                .backDocumentUrl(
                        resolveUrl(
                                kyc.getBackDocumentUrl()
                        )
                )

                .documentFront(
                        resolveUrl(
                                kyc.getFrontDocumentUrl()
                        )
                )

                .documentBack(
                        resolveUrl(
                                kyc.getBackDocumentUrl()
                        )
                )

                .documentUrl(
                        resolveUrl(
                                kyc.getFilePath()
                        )
                )

                .documentFile(
                        resolveUrl(
                                kyc.getFilePath()
                        )
                )

                .uploadedAt(
                        kyc.getUploadedAt()
                )

                .submittedAt(
                        kyc.getSubmittedAt()
                )

                .reviewedAt(
                        kyc.getReviewedAt()
                )

                .verifiedAt(
                        kyc.getVerifiedAt()
                )

                .reviewedBy(
                        kyc.getReviewedBy()
                )

                .verifiedBy(
                        kyc.getVerifiedBy()
                )

                .rejectionReason(
                        kyc.getRejectionReason()
                )

                .riskFlag(
                        kyc.getRiskFlag()
                )

                .completed(
                        kyc.getCompleted()
                )

                .mimeType(
                        kyc.getMimeType()
                )

                .contentType(
                        kyc.getContentType()
                )

                .build();
    }

    // =====================================================
    // STATUS
    // =====================================================

    @Override
    public String getKycStatus(
            String email
    ) {

        KycEntity kyc =
                getMyKyc(email);

        return kyc != null &&
                kyc.getStatus() != null

                ? kyc.getStatus().name()

                : "PENDING";
    }

    @Override
    public boolean isKycVerified(
            String email
    ) {

        KycEntity kyc =
                getMyKyc(email);

        return kyc != null &&
                (
                        kyc.getStatus() == KycStatus.VERIFIED ||
                                kyc.getStatus() == KycStatus.APPROVED
                );
    }


    // =====================================================
    // VERIFY
    // =====================================================

    @Override
    public String verifyKyc(
            Long kycId
    ) {

        // =====================================================
        // LOAD KYC USING KYC ID
        // =====================================================

        KycEntity kyc =
                kycRepository
                        .findById(kycId)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "KYC not found"
                                )
                        );

        // =====================================================
        // LOAD ASSOCIATED USER
        // =====================================================

        UserEntity user =
                kyc.getUser();

        if (user == null) {

            throw new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Associated user not found"
            );
        }

        // =====================================================
        // UPDATE KYC
        // =====================================================

        kyc.setStatus(
                KycStatus.VERIFIED
        );
        kyc.setRejectionReason(null);
        kyc.setCompleted(true);

        kyc.setVerifiedAt(
                LocalDateTime.now()
        );

        kyc.setReviewedAt(
                LocalDateTime.now()
        );

        kyc.setVerifiedBy("ADMIN");

        kyc.setReviewedBy("ADMIN");

        kyc.setUpdatedAt(
                LocalDateTime.now()
        );

        kycRepository.save(kyc);

        // =====================================================
        // UPDATE USER
        // =====================================================

        user.setIsKycVerified(true);

        user.setKycStatus("VERIFIED");

        userRepository.save(user);

        notificationService.create(

                user.getId(),

                "KYC Verified",

                "Your KYC has been verified successfully",

                "INFO"
        );


        return "KYC verified successfully";
    }

    // =====================================================
    // REJECT
    // =====================================================

    @Override
    public String rejectKyc(

            Long kycId,

            String reason
    ) {

        // =====================================================
        // LOAD KYC USING KYC ID
        // =====================================================

        KycEntity kyc =
                kycRepository
                        .findById(kycId)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "KYC not found"
                                )
                        );

        // =====================================================
        // LOAD ASSOCIATED USER
        // =====================================================

        UserEntity user =
                kyc.getUser();

        if (user == null) {

            throw new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Associated user not found"
            );
        }

        kyc.setStatus(
                KycStatus.REJECTED
        );

        kyc.setCompleted(false);

        kyc.setRejectionReason(

                reason != null &&
                        !reason.isBlank()

                        ? reason.trim()

                        : "KYC rejected"
        );

        kyc.setReviewedAt(
                LocalDateTime.now()
        );

        kyc.setReviewedBy(
                "ADMIN"
        );

        kyc.setUpdatedAt(
                LocalDateTime.now()
        );

        kycRepository.save(kyc);

        user.setIsKycVerified(false);

        user.setKycStatus("REJECTED");

        userRepository.save(user);

        notificationService.create(

                user.getId(),

                "KYC Rejected",

                "Your KYC verification was rejected",

                "ALERT"
        );

        return "KYC rejected successfully";
    }

    // =====================================================
    // ADMIN LISTS
    // =====================================================

    @Override
    public List<KycEntity> getAllKyc() {

        return kycRepository
                .findAllByOrderByUploadedAtDesc();
    }

    @Override
    public List<KycEntity> getPendingKyc() {

        return kycRepository
                .findByStatusInOrderByUploadedAtDesc(

                        List.of(

                                KycStatus.PENDING,
                                KycStatus.UNDER_REVIEW
                        )
                );
    }

    @Override
    public List<KycEntity> getByStatus(
            KycStatus status
    ) {

        return kycRepository
                .findAllByStatusOrderByUploadedAtDesc(
                        status
                );
    }

    // =====================================================
    // ADMIN RESPONSES
    // =====================================================

    @Override
    public List<AdminKycResponse> getAdminKycResponses() {

        return getAllKyc()

                .stream()

                .map(this::mapAdminResponse)

                .toList();
    }

    @Override
    public List<AdminKycResponse>
    getPendingAdminKycResponses() {

        return getPendingKyc()

                .stream()

                .map(this::mapAdminResponse)

                .toList();
    }

    @Override
    public AdminKycResponse getAdminKycByUserId(
            Long userId
    ) {

        UserEntity user =
                getUser(userId);

        return mapAdminResponse(
                getKyc(user)
        );
    }

    // =====================================================
    // PAGINATION
    // =====================================================

    @Override
    public Page<KycEntity> getAllKyc(
            Pageable pageable
    ) {

        return kycRepository
                .findAllByOrderByUploadedAtDesc(
                        pageable
                );
    }

    @Override
    public Page<KycEntity> getByStatus(

            KycStatus status,

            Pageable pageable
    ) {

        return kycRepository
                .findByStatus(
                        status,
                        pageable
                );
    }

    // =====================================================
    // SEARCH
    // =====================================================

    @Override
    public List<KycEntity> search(
            String query
    ) {

        if (
                query == null ||
                        query.isBlank()
        ) {

            return getAllKyc();
        }

        return getAllKyc()

                .stream()

                .filter(k -> {

                    UserEntity u =
                            k.getUser();

                    if (u == null) {
                        return false;
                    }

                    String q =
                            query.toLowerCase();

                    return (

                            u.getEntityName() != null &&

                                    u.getEntityName()
                                            .toLowerCase()
                                            .contains(q)

                    ) ||

                            (

                                    u.getEmail() != null &&

                                            u.getEmail()
                                                    .toLowerCase()
                                                    .contains(q)
                            );
                })

                .toList();
    }

    // =====================================================
    // ANALYTICS
    // =====================================================

    @Override
    public long totalKyc() {

        return kycRepository.count();
    }

    @Override
    public long totalPending() {

        return kycRepository.countByStatus(
                KycStatus.PENDING
        );
    }

    @Override
    public long totalVerified() {

        return kycRepository.countByStatus(
                KycStatus.VERIFIED
        );
    }

    @Override
    public long totalRejected() {

        return kycRepository.countByStatus(
                KycStatus.REJECTED
        );
    }

    @Override
    public long totalTodayUploads() {

        return getAllKyc()

                .stream()

                .filter(k ->

                        k.getUploadedAt() != null &&

                                k.getUploadedAt()
                                        .toLocalDate()
                                        .equals(
                                                LocalDate.now()
                                        )
                )

                .count();
    }

    // =====================================================
    // HELPERS
    // =====================================================

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

    private UserEntity getUser(
            Long userId
    ) {

        return userRepository
                .findById(userId)

                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "User not found"
                        )
                );
    }

    private KycEntity getKyc(
            UserEntity user
    ) {

        return kycRepository
                .findByUser(user)

                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "KYC not found"
                        )
                );
    }

    private void validateKycInput(

            String documentType,

            String documentNumber,

            MultipartFile file
    ) {

        if (
                documentType == null ||
                        documentType.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Document type required"
            );
        }

        if (
                documentNumber == null ||
                        documentNumber.isBlank()
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

    private String resolveUrl(
            String value
    ) {

        if (
                value == null ||
                        value.isBlank()
        ) {

            return null;
        }

        value = value.trim();

        if (
                value.startsWith("http://") ||
                        value.startsWith("https://")
        ) {

            return value;
        }

        if (!value.startsWith("/")) {

            value = "/" + value;
        }

        return BASE_URL + value;
    }

    // =====================================================
    // ADMIN RESPONSE
    // =====================================================

    private AdminKycResponse mapAdminResponse(
            KycEntity kyc
    ) {

        UserEntity user =
                kyc.getUser();

        return AdminKycResponse.builder()

                .id(
                        kyc.getId()
                )

                .userId(
                        user != null
                                ? user.getUserId()
                                : null
                )

                .name(
                        user != null
                                ? user.getEntityName()
                                : ""
                )

                .email(
                        user != null
                                ? user.getEmail()
                                : ""
                )

                .phoneNumber(
                        user != null
                                ? user.getPhoneNumber()
                                : ""
                )

                .documentType(
                        kyc.getDocumentType()
                )

                .documentNumber(
                        kyc.getDocumentNumber()
                )

                .status(
                        safeStatus(
                                kyc.getStatus()
                        )
                )

                .kycStatus(
                        safeStatus(
                                kyc.getStatus()
                        )
                )

                .completed(
                        kyc.getCompleted()
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

                .verifiedBy(
                        kyc.getVerifiedBy()
                )

                .reviewedBy(
                        kyc.getReviewedBy()
                )

                // =====================================================
                // PRIMARY DOCUMENT URL
                // =====================================================

                .documentUrl(
                        resolveUrl(
                                kyc.getFilePath()
                        )
                )

                .imageUrl(
                        resolveUrl(
                                kyc.getFilePath()
                        )
                )

                .previewUrl(
                        resolveUrl(
                                kyc.getFilePath()
                        )
                )

                .filePath(
                        kyc.getFilePath()
                )

                // =====================================================
                // FRONT / BACK DOCUMENTS
                // =====================================================

                .frontDocumentUrl(
                        resolveUrl(
                                kyc.getFrontDocumentUrl()
                        )
                )

                .backDocumentUrl(
                        resolveUrl(
                                kyc.getBackDocumentUrl()
                        )
                )

                // =====================================================
                // AADHAAR
                // =====================================================

                .aadhaarFrontUrl(
                        resolveUrl(
                                kyc.getAadhaarFrontUrl()
                        )
                )

                .aadhaarBackUrl(
                        resolveUrl(
                                kyc.getAadhaarBackUrl()
                        )
                )

                // =====================================================
                // PAN
                // =====================================================

                .panCardUrl(
                        resolveUrl(
                                kyc.getPanCardUrl()
                        )
                )

                // =====================================================
                // SELFIE
                // =====================================================

                .selfieUrl(
                        resolveUrl(
                                kyc.getSelfieUrl()
                        )
                )

                .build();
    }
}