package in.bawvpl.Authify.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.profile-bucket}")
    private String profileBucket;

    @Value("${aws.s3.app-bucket}")
    private String appBucket;

    @Value("${aws.region}")
    private String region;

    // =====================================================
    // KYC FILE UPLOAD
    // =====================================================
    @Override
    public String uploadFile(MultipartFile file) {
        return uploadToBucket(file, bucketName, "kyc/", "Failed to upload file to S3");
    }

    @Override
    public String uploadAnnouncementImage(
            MultipartFile file
    ) {

        return uploadToBucket(
                file,
                appBucket,
                "announcements/",
                "Announcement image upload failed"
        );
    }

    // =====================================================
    // PROFILE IMAGE UPLOAD
    // =====================================================
    @Override
    public String uploadProfileImage(MultipartFile file) {
        return uploadToBucket(file, profileBucket, "profiles/", "Profile image upload failed");
    }

    // =====================================================
    // APP IMAGE UPLOAD
    // =====================================================
    @Override
    public String uploadAppImage(MultipartFile file) {
        return uploadToBucket(file, appBucket, "app-images/", "App image upload failed");
    }

    // =====================================================
    // APP BANNER UPLOAD
    // =====================================================
    @Override
    public String uploadAppBanner(MultipartFile file) {
        return uploadToBucket(file, appBucket, "app-banners/", "App banner upload failed");
    }

    // =====================================================
    // GENERIC HELPER METHOD FOR S3 UPLOAD
    // =====================================================
    private String uploadToBucket(MultipartFile file, String bucket, String folderPath, String errorMsg) {
        try {
            String originalFileName = file.getOriginalFilename();
            String extension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // फ़ोल्डर पाथ + यूनिक UUID + फाइल एक्सटेंशन
            String fileName = folderPath + UUID.randomUUID() + extension;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            // S3 डायरेक्ट पब्लिक URL रिटर्न कर रहा है
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);

        } catch (Exception e) {
            log.error("{}: {}", errorMsg, e.getMessage(), e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    @Override
    public String generatePresignedGetUrl(
            String storedUrl
    ) {

        try {

            if (
                    storedUrl == null ||
                            storedUrl.isBlank()
            ) {

                throw new IllegalArgumentException(
                        "Stored URL is required"
                );
            }

            URI uri =
                    URI.create(storedUrl);

            String host =
                    uri.getHost();

            if (
                    host == null ||
                            !host.contains(".s3.")
            ) {

                throw new IllegalArgumentException(
                        "Invalid S3 URL"
                );
            }

            String bucket =
                    host.substring(
                            0,
                            host.indexOf(".s3.")
                    );

            String key =
                    uri.getPath()
                            .replaceFirst("^/", "");

            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(
                                    Duration.ofMinutes(15)
                            )
                            .getObjectRequest(
                                    getObjectRequest
                            )
                            .build();

            PresignedGetObjectRequest presignedRequest =
                    s3Presigner.presignGetObject(
                            presignRequest
                    );

            return presignedRequest
                    .url()
                    .toString();

        } catch (Exception e) {

            log.error(
                    "Failed to generate presigned URL",
                    e
            );

            throw new RuntimeException(
                    "Failed to generate presigned URL",
                    e
            );
        }
    }
}