package in.bawvpl.Authify.util;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Component;

import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class UrlValidatorUtil {

    // =====================================================
    // VALIDATE ROUTE PATH
    // =====================================================

    public void validateRoutePath(
            String routePath
    ) {

        if (

                routePath == null ||

                        routePath.isBlank()
        ) {

            return;
        }

        String normalized =
                routePath.trim();

        // =====================================================
        // MUST START WITH /
        // =====================================================

        if (!normalized.startsWith("/")) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "routePath must start with '/'"
            );
        }

        // =====================================================
        // BLOCK DOUBLE SLASH
        // =====================================================

        if (normalized.contains("//")) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Invalid routePath"
            );
        }

        // =====================================================
        // BLOCK SPACES
        // =====================================================

        if (normalized.contains(" ")) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "routePath cannot contain spaces"
            );
        }
    }

    // =====================================================
    // VALIDATE EXTERNAL URL
    // =====================================================

    public void validateExternalUrl(
            String externalUrl
    ) {

        if (

                externalUrl == null ||

                        externalUrl.isBlank()
        ) {

            return;
        }

        String normalized =
                externalUrl.trim();

        try {

            URI uri =
                    new URI(normalized);

            // =====================================================
            // MUST BE HTTPS
            // =====================================================

            if (

                    uri.getScheme() == null ||

                            !uri.getScheme()
                                    .equalsIgnoreCase("https")
            ) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "externalUrl must use HTTPS"
                );
            }

            // =====================================================
            // HOST REQUIRED
            // =====================================================

            if (

                    uri.getHost() == null ||

                            uri.getHost().isBlank()
            ) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Invalid externalUrl"
                );
            }

        } catch (URISyntaxException ex) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Invalid externalUrl format"
            );
        }
    }

    // =====================================================
    // VALIDATE APP URL
    // =====================================================

    public void validateAppUrl(
            String appUrl
    ) {

        if (

                appUrl == null ||

                        appUrl.isBlank()
        ) {

            return;
        }

        String normalized =
                appUrl.trim();

        // =====================================================
        // INTERNAL ROUTE
        // =====================================================

        if (normalized.startsWith("/")) {

            validateRoutePath(normalized);

            return;
        }

        // =====================================================
        // EXTERNAL URL
        // =====================================================

        validateExternalUrl(normalized);
    }

    // =====================================================
    // VALIDATE NAVIGATION TARGETS
    // =====================================================

    public void validateNavigationTargets(

            String routePath,

            String externalUrl,

            String appUrl
    ) {

        boolean hasRoutePath =

                routePath != null &&

                        !routePath.isBlank();

        boolean hasExternalUrl =

                externalUrl != null &&

                        !externalUrl.isBlank();

        boolean hasAppUrl =

                appUrl != null &&

                        !appUrl.isBlank();

        // =====================================================
        // REQUIRE AT LEAST ONE
        // =====================================================

        if (

                !hasRoutePath &&

                        !hasExternalUrl &&

                        !hasAppUrl
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "At least one navigation target is required"
            );
        }

        // =====================================================
        // VALIDATE EACH
        // =====================================================

        validateRoutePath(routePath);

        validateExternalUrl(externalUrl);

        validateAppUrl(appUrl);
    }
}