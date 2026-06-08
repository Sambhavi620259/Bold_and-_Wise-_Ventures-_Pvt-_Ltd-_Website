package in.bawvpl.Authify.service;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import java.time.Instant;

import java.util.Base64;

@Service
public class SignedUrlService {

    // =====================================================
    // BASE URL
    // =====================================================

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // =====================================================
    // SECRET
    // =====================================================

    @Value("${app.signed-url.secret:AuthifySecretKey}")
    private String secret;

    // =====================================================
    // EXPIRY
    // =====================================================

    @Value("${app.signed-url.expiry-minutes:30}")
    private long expiryMinutes;

    // =====================================================
    // GENERATE SIGNED URL
    // =====================================================

    public String generateSignedUrl(
            String filePath
    ) {

        if (
                filePath == null ||
                        filePath.isBlank()
        ) {

            return null;
        }

        long expiresAt =

                Instant.now()
                        .plusSeconds(
                                expiryMinutes * 60
                        )
                        .getEpochSecond();

        // =====================================================
        // TOKEN
        // =====================================================

        String raw =

                filePath +

                        "|" +

                        expiresAt +

                        "|" +

                        secret;

        String token =

                Base64.getUrlEncoder()
                        .encodeToString(

                                raw.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        // =====================================================
        // ENCODE PATH
        // =====================================================

        String encodedPath =

                URLEncoder.encode(

                        filePath,

                        StandardCharsets.UTF_8
                );

        // =====================================================
        // RETURN URL
        // =====================================================

        return baseUrl +

                "/api/v1.0/files/view?" +

                "path=" + encodedPath +

                "&token=" + token +

                "&expires=" + expiresAt;
    }

    // =====================================================
    // VALIDATE SIGNED URL
    // =====================================================

    public boolean validateSignedUrl(

            String filePath,

            String token,

            long expires
    ) {

        try {

            // =====================================================
            // EXPIRED
            // =====================================================

            long now =
                    Instant.now()
                            .getEpochSecond();

            if (now > expires) {

                return false;
            }

            // =====================================================
            // EXPECTED TOKEN
            // =====================================================

            String raw =

                    filePath +

                            "|" +

                            expires +

                            "|" +

                            secret;

            String expected =

                    Base64.getUrlEncoder()
                            .encodeToString(

                                    raw.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            return expected.equals(token);

        } catch (Exception e) {

            return false;
        }
    }
}