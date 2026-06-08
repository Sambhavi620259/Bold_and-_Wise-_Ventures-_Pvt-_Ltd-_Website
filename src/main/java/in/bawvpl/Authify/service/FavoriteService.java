package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.entity.FavoriteEntity;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.FavoriteResponse;

import in.bawvpl.Authify.repository.ApplicationRepository;
import in.bawvpl.Authify.repository.FavoriteRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    private final UserRepository userRepository;

    private final ApplicationRepository applicationRepository;

    // =====================================================
    // ADD FAVORITE
    // =====================================================

    @Transactional
    public void add(

            String email,

            Long appId
    ) {

        UserEntity user =
                getUser(email);

        ApplicationEntity app =
                getApp(appId);

        boolean exists =
                favoriteRepository
                        .existsByUser_IdAndApp_AppId(

                                user.getId(),

                                appId
                        );

        if (exists) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Already in favorites"
            );
        }

        FavoriteEntity favorite =
                FavoriteEntity.builder()

                        .user(user)

                        .app(app)

                        .build();

        favoriteRepository.save(favorite);

        log.info(

                "Added to favorites: user={}, appId={}",

                user.getEmail(),

                appId
        );
    }

    // =====================================================
    // TOGGLE FAVORITE
    // =====================================================

    @Transactional
    public String toggleFavorite(

            String email,

            Long appId
    ) {

        UserEntity user =
                getUser(email);

        ApplicationEntity app =
                getApp(appId);

        boolean exists =
                favoriteRepository
                        .existsByUser_IdAndApp_AppId(

                                user.getId(),

                                appId
                        );

        // =====================================================
        // REMOVE
        // =====================================================

        if (exists) {

            favoriteRepository
                    .deleteByUser_IdAndApp_AppId(

                            user.getId(),

                            appId
                    );

            log.info(

                    "Removed from favorites: user={}, appId={}",

                    user.getEmail(),

                    appId
            );

            return "Removed from favorites";
        }

        // =====================================================
        // ADD
        // =====================================================

        favoriteRepository.save(

                FavoriteEntity.builder()

                        .user(user)

                        .app(app)

                        .build()
        );

        log.info(

                "Added to favorites: user={}, appId={}",

                user.getEmail(),

                appId
        );

        return "Added to favorites";
    }

    // =====================================================
    // GET FAVORITES
    // =====================================================

    @Transactional(readOnly = true)
    public List<FavoriteResponse> get(
            String email
    ) {

        UserEntity user =
                getUser(email);

        List<FavoriteEntity> favorites =
                favoriteRepository
                        .findByUser_Id(
                                user.getId()
                        );

        if (

                favorites == null ||

                        favorites.isEmpty()
        ) {

            return Collections.emptyList();
        }

        return favorites
                .stream()

                .filter(f ->

                        f != null &&

                                f.getApp() != null
                )

                .map(f -> {

                    ApplicationEntity app =
                            f.getApp();

                    return FavoriteResponse.builder()

                            .appId(
                                    app.getAppId()
                            )

                            // =====================================================
                            // FIX:
                            // OLD -> getAppName()
                            // NEW -> getName()
                            // =====================================================

                            .appName(
                                    app.getName()
                            )

                            // =====================================================
                            // FIX:
                            // OLD -> getAppLogo()
                            // NEW -> getLogoUrl()
                            // =====================================================

                            .appLogo(
                                    app.getLogoUrl()
                            )

                            .appUrl(
                                    app.getAppUrl()
                            )

                            .build();
                })

                // =====================================================
                // FIX:
                // safer generic inference
                // =====================================================

                .collect(Collectors.toList());
    }

    // =====================================================
    // REMOVE FAVORITE
    // =====================================================

    @Transactional
    public void remove(

            String email,

            Long appId
    ) {

        UserEntity user =
                getUser(email);

        boolean exists =
                favoriteRepository
                        .existsByUser_IdAndApp_AppId(

                                user.getId(),

                                appId
                        );

        // =====================================================
        // SAFE DELETE
        // =====================================================

        if (!exists) {

            log.warn(

                    "Favorite not found: user={}, appId={}",

                    user.getEmail(),

                    appId
            );

            return;
        }

        favoriteRepository
                .deleteByUser_IdAndApp_AppId(

                        user.getId(),

                        appId
                );

        log.info(

                "Removed from favorites: user={}, appId={}",

                user.getEmail(),

                appId
        );
    }

    // =====================================================
    // GET USER
    // =====================================================

    private UserEntity getUser(
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

        return userRepository
                .findByEmailIgnoreCase(

                        email
                                .trim()
                                .toLowerCase()
                )
                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "User not found"
                        )
                );
    }

    // =====================================================
    // GET APP
    // =====================================================

    private ApplicationEntity getApp(
            Long appId
    ) {

        if (appId == null) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "appId required"
            );
        }

        return applicationRepository
                .findById(appId)
                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "Application not found"
                        )
                );
    }
}