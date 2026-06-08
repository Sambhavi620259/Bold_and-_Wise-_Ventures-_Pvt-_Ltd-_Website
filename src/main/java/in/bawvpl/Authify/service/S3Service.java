package in.bawvpl.Authify.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String uploadFile(MultipartFile file);

    String uploadProfileImage(MultipartFile file);

    String uploadAppImage(MultipartFile file);

    String uploadAppBanner(MultipartFile file);

    String uploadAnnouncementImage(
            MultipartFile file
    );

    String generatePresignedGetUrl(
            String storedUrl
    );
}
