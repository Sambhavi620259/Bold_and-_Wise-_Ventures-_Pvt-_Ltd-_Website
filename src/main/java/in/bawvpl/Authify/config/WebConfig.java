package in.bawvpl.Authify.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;

import org.springframework.http.CacheControl;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    // =====================================================
    // UPLOAD DIRECTORY
    //
    // IMPORTANT:
    //
    // Serves:
    // - logos
    // - banners
    // - thumbnails
    // - uploaded assets
    //
    // Frontend expects:
    // /uploads/**
    // =====================================================

    private static final String UPLOAD_DIR =

            "file:" +

                    Paths.get(

                            System.getProperty("user.dir"),

                            "uploads"

                    ).toAbsolutePath() + "/";

    // =====================================================
    // STATIC RESOURCE HANDLER
    // =====================================================

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        log.info(
                "Serving uploads from: {}",
                UPLOAD_DIR
        );

        // =====================================================
        // UPLOADS
        //
        // Example:
        // /uploads/apps/logo.png
        // =====================================================

        registry

                .addResourceHandler(
                        "/uploads/**"
                )

                .addResourceLocations(
                        UPLOAD_DIR
                )

                .setCacheControl(

                        CacheControl

                                .maxAge(
                                        1,
                                        TimeUnit.HOURS
                                )

                                .cachePublic()
                )

                .resourceChain(true);

        // =====================================================
        // OPTIONAL FILES PATH
        //
        // Example:
        // /files/document.pdf
        // =====================================================

        registry

                .addResourceHandler(
                        "/files/**"
                )

                .addResourceLocations(
                        UPLOAD_DIR
                )

                .setCacheControl(

                        CacheControl

                                .maxAge(
                                        1,
                                        TimeUnit.HOURS
                                )

                                .cachePublic()
                )

                .resourceChain(true);

        // =====================================================
        // OPTIONAL IMAGE PATH
        //
        // Example:
        // /images/logo.png
        //
        // Frontend fallback compatibility.
        // =====================================================

        registry

                .addResourceHandler(
                        "/images/**"
                )

                .addResourceLocations(
                        UPLOAD_DIR
                )

                .setCacheControl(

                        CacheControl

                                .maxAge(
                                        1,
                                        TimeUnit.HOURS
                                )

                                .cachePublic()
                )

                .resourceChain(true);
    }

    // =====================================================
    // ROOT MAPPING
    // =====================================================

    @Override
    public void addViewControllers(
            ViewControllerRegistry registry
    ) {

        registry.addRedirectViewController(

                "/",

                "/swagger-ui/index.html"
        );
    }
}