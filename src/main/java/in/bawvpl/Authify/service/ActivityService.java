package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ActivityLog;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.ActivityResponse;
import in.bawvpl.Authify.repository.ActivityLogRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;

    private final UserRepository userRepository;

    // =====================================================
    // LOG ACTIVITY
    // =====================================================

    @Transactional
    public void log(

            String email,

            String action,

            String description
    ) {

        try {

            final String normalizedEmail =
                    normalizeEmail(email);

            UserEntity user =
                    userRepository
                            .findByEmailIgnoreCase(normalizedEmail)
                            .orElseThrow(() ->

                                    new ResponseStatusException(

                                            HttpStatus.NOT_FOUND,

                                            "User not found"
                                    )
                            );

            ActivityLog activityLog =
                    ActivityLog.builder()

                            .user(user)

                            .action(action)

                            .description(description)

                            .build();



            // =====================================================
            // SAVE
            // =====================================================

            ActivityLog saved =
                    activityLogRepository.save(activityLog);



            log.info(

                    "Activity saved for user {}",

                    user.getEmail()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to save activity",
                    e
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // GET USER ACTIVITIES
    // =====================================================

    public Page<ActivityResponse> getActivities(

            String email,

            int page,

            int size
    ) {

        final String normalizedEmail =
                normalizeEmail(email);

        UserEntity user =
                userRepository
                        .findByEmailIgnoreCase(normalizedEmail)
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "User not found"
                                )
                        );

        if (page < 0) {

            page = 0;
        }

        if (size <= 0) {

            size = 10;
        }

        Pageable pageable =
                PageRequest.of(

                        page,

                        size,

                        Sort.by("timestamp")
                                .descending()
                );

        return activityLogRepository

                .findByUser_IdOrderByTimestampDesc(

                        user.getId(),

                        pageable
                )

                .map(this::toResponse);
    }

    // =====================================================
    // RESPONSE MAPPER
    // =====================================================

    private ActivityResponse toResponse(
            ActivityLog log
    ) {

        return ActivityResponse.builder()

                .action(
                        log.getAction()
                )

                .description(
                        log.getDescription()
                )

                .timestamp(
                        log.getTimestamp()
                )

                .build();
    }

    // =====================================================
    // EMAIL NORMALIZER
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
}