package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AnnouncementEntity;

import in.bawvpl.Authify.repository.AnnouncementRepository;

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

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    // =====================================================
    // ACTIVE ANNOUNCEMENTS
    // =====================================================

    @Transactional(readOnly = true)
    public List<AnnouncementEntity> getActiveAnnouncements() {

        return announcementRepository
                .findByPublishedTrueOrderByCreatedAtDesc();
    }

    // =====================================================
    // ADMIN LIST
    // =====================================================

    @Transactional(readOnly = true)
    public Page<AnnouncementEntity> getAll(

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

        Pageable pageable =
                PageRequest.of(

                        page,

                        size,

                        Sort.by("createdAt")
                                .descending()
                );

        return announcementRepository
                .findAllByOrderByCreatedAtDesc(
                        pageable
                );
    }

    // =====================================================
    // CREATE
    // =====================================================

    public AnnouncementEntity create(
            Map<String, Object> body
    ) {

        String title =
                body.get("title") != null

                        ? body.get("title")
                        .toString()
                        .trim()

                        : null;

        String message =
                body.get("message") != null

                        ? body.get("message")
                        .toString()
                        .trim()

                        : null;

        if (

                title == null ||

                        title.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Title required"
            );
        }

        if (

                message == null ||

                        message.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Message required"
            );
        }

        AnnouncementEntity announcement =
                AnnouncementEntity.builder()

                        .title(title)

                        .message(message)

                        .published(true)

                        .build();

        // =====================================================
        // OPTIONAL BANNER
        // =====================================================

        Object bannerObj =
                body.get("bannerUrl");

        if (bannerObj != null) {

            announcement.setBannerUrl(
                    bannerObj.toString()
                            .trim()
            );
        }

        // =====================================================
        // OPTIONAL PUBLISHED
        // =====================================================

        Object publishedObj =
                body.get("published");

        if (publishedObj != null) {

            announcement.setPublished(

                    Boolean.parseBoolean(
                            publishedObj.toString()
                    )
            );
        }

        announcement =
                announcementRepository.save(
                        announcement
                );

        log.info(
                "Announcement created: {}",
                announcement.getId()
        );

        return announcement;
    }

    // =====================================================
    // UPDATE
    // =====================================================

    public AnnouncementEntity update(

            Long id,

            Map<String, Object> body
    ) {

        AnnouncementEntity announcement =
                announcementRepository
                        .findById(id)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Announcement not found"
                                )
                        );

        // =====================================================
        // TITLE
        // =====================================================

        Object titleObj =
                body.get("title");

        if (

                titleObj != null &&

                        !titleObj.toString()
                                .isBlank()
        ) {

            announcement.setTitle(

                    titleObj.toString()
                            .trim()
            );
        }

        // =====================================================
        // MESSAGE
        // =====================================================

        Object messageObj =
                body.get("message");

        if (

                messageObj != null &&

                        !messageObj.toString()
                                .isBlank()
        ) {

            announcement.setMessage(

                    messageObj.toString()
                            .trim()
            );
        }

        // =====================================================
        // BANNER
        // =====================================================

        Object bannerObj =
                body.get("bannerUrl");

        if (bannerObj != null) {

            announcement.setBannerUrl(

                    bannerObj.toString()
                            .trim()
            );
        }

        // =====================================================
        // PUBLISHED
        // =====================================================

        Object publishedObj =
                body.get("published");

        if (publishedObj != null) {

            announcement.setPublished(

                    Boolean.parseBoolean(
                            publishedObj.toString()
                    )
            );
        }

        announcement =
                announcementRepository.save(
                        announcement
                );

        log.info(
                "Announcement updated: {}",
                announcement.getId()
        );

        return announcement;
    }

    // =====================================================
    // DELETE
    // =====================================================

    public void delete(
            Long id
    ) {

        AnnouncementEntity announcement =
                announcementRepository
                        .findById(id)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Announcement not found"
                                )
                        );

        announcementRepository.delete(
                announcement
        );

        log.info(
                "Announcement deleted: {}",
                id
        );
    }

    // =====================================================
    // UPDATE BANNER
    // =====================================================

    public AnnouncementEntity updateBanner(

            Long id,

            String bannerUrl
    ) {

        AnnouncementEntity announcement =
                announcementRepository
                        .findById(id)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Announcement not found"
                                )
                        );

        announcement.setBannerUrl(
                bannerUrl
        );

        announcement =
                announcementRepository.save(
                        announcement
                );

        return announcement;
    }
}