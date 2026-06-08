package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.KycStatus;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminKycService {

    private final KycRepository kycRepository;

    private final UserRepository userRepository;

    // =====================================================
    // GET ALL KYC
    // =====================================================

    @Transactional(readOnly = true)
    public List<KycEntity> getAllKyc() {

        return kycRepository
                .findAllByOrderByUploadedAtDesc();
    }

    // =====================================================
    // GET PENDING KYC
    // =====================================================

    @Transactional(readOnly = true)
    public List<KycEntity> getPendingKyc() {

        return kycRepository.findByStatus(
                KycStatus.PENDING
        );
    }

    // =====================================================
    // VERIFY KYC
    // =====================================================

    @Transactional
    public void verifyKyc(
            Long id
    ) {

        KycEntity kyc =
                kycRepository.findById(id)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "KYC not found"
                                )
                        );

        // =====================================================
        // STATUS
        // =====================================================

        kyc.setStatus(
                KycStatus.VERIFIED
        );

        kyc.setCompleted(true);

        kyc.setRejectionReason(null);

        kyc.setReviewedAt(
                LocalDateTime.now()
        );

        kyc.setVerifiedAt(
                LocalDateTime.now()
        );

        // =====================================================
        // AUDIT
        // =====================================================

        kyc.setReviewedBy(
                "ADMIN"
        );

        kyc.setVerifiedBy(
                "ADMIN"
        );

        kycRepository.save(kyc);

        // =====================================================
        // USER UPDATE
        // =====================================================

        UserEntity user =
                kyc.getUser();

        if (user != null) {

            user.setIsKycVerified(true);

            userRepository.save(user);
        }
    }

    // =====================================================
    // REJECT KYC
    // =====================================================

    @Transactional
    public void rejectKyc(

            Long id,

            String reason
    ) {

        KycEntity kyc =
                kycRepository.findById(id)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "KYC not found"
                                )
                        );

        // =====================================================
        // STATUS
        // =====================================================

        kyc.setStatus(
                KycStatus.REJECTED
        );

        kyc.setCompleted(false);

        kyc.setReviewedAt(
                LocalDateTime.now()
        );

        kyc.setReviewedBy(
                "ADMIN"
        );

        // =====================================================
        // RESET VERIFY DATA
        // =====================================================

        kyc.setVerifiedAt(null);

        kyc.setVerifiedBy(null);

        // =====================================================
        // REASON
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
        // USER UPDATE
        // =====================================================

        UserEntity user =
                kyc.getUser();

        if (user != null) {

            user.setIsKycVerified(false);

            userRepository.save(user);
        }
    }

    // =====================================================
    // MARK UNDER REVIEW
    // =====================================================

    @Transactional
    public void markUnderReview(
            Long id
    ) {

        KycEntity kyc =
                kycRepository.findById(id)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "KYC not found"
                                )
                        );

        kyc.setStatus(
                KycStatus.UNDER_REVIEW
        );

        kyc.setCompleted(false);

        kyc.setReviewedAt(
                LocalDateTime.now()
        );

        kyc.setReviewedBy(
                "ADMIN"
        );

        kycRepository.save(kyc);

        // =====================================================
        // USER UPDATE
        // =====================================================

        UserEntity user =
                kyc.getUser();

        if (user != null) {

            user.setIsKycVerified(false);

            userRepository.save(user);
        }
    }

    // =====================================================
    // REUPLOAD REQUIRED
    // =====================================================

    @Transactional
    public void requireReupload(

            Long id,

            String reason
    ) {

        KycEntity kyc =
                kycRepository.findById(id)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "KYC not found"
                                )
                        );

        kyc.setStatus(
                KycStatus.REUPLOAD_REQUIRED
        );

        kyc.setCompleted(false);

        kyc.setReviewedAt(
                LocalDateTime.now()
        );

        kyc.setReviewedBy(
                "ADMIN"
        );

        // =====================================================
        // RESET VERIFY DATA
        // =====================================================

        kyc.setVerifiedAt(null);

        kyc.setVerifiedBy(null);

        // =====================================================
        // REASON
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
                    "Please re-upload documents"
            );
        }

        kycRepository.save(kyc);

        // =====================================================
        // USER UPDATE
        // =====================================================

        UserEntity user =
                kyc.getUser();

        if (user != null) {

            user.setIsKycVerified(false);

            userRepository.save(user);
        }
    }
}