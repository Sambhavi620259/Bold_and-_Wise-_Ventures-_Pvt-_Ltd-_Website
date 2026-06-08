package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.repository.UserApplicationRepository;
import in.bawvpl.Authify.io.RecentAppDto;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecentAppService {

    private final UserApplicationRepository userAppRepo;

    // =====================================================
    // GET RECENT APPS
    // =====================================================

    public List<RecentAppDto> getRecentApps(
            Long userId
    ) {

        if (userId == null) {

            return Collections.emptyList();
        }

        List<UserApplicationEntity> list =
                userAppRepo.findAllByUser_Id(userId);

        return list.stream()

                // =====================================================
                // SAFETY
                // =====================================================

                .filter(ua ->

                        ua != null &&

                                ua.getApp() != null &&

                                ua.getUpdatedAt() != null
                )

                // =====================================================
                // SORT
                // =====================================================

                .sorted(

                        Comparator.comparing(
                                UserApplicationEntity::getUpdatedAt
                        ).reversed()
                )

                // =====================================================
                // LIMIT
                // =====================================================

                .limit(5)

                // =====================================================
                // DTO
                // =====================================================

                .map(ua -> {

                    ApplicationEntity app =
                            ua.getApp();

                    return new RecentAppDto(

                            app.getAppId(),

                            // =====================================================
                            // FIX:
                            // OLD -> getAppName()
                            // NEW -> getName()
                            // =====================================================

                            app.getName(),

                            // =====================================================
                            // FIX:
                            // OLD -> getAppLogo()
                            // NEW -> getLogoUrl()
                            // =====================================================

                            app.getLogoUrl(),

                            app.getAppUrl(),

                            ua.getVisitCounter(),

                            ua.getUpdatedAt()
                    );
                })

                // =====================================================
                // FIX:
                // safer generic handling
                // =====================================================

                .collect(Collectors.toList());
    }
}