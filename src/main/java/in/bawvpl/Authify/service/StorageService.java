package in.bawvpl.Authify.service;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.UUID;

@Service
public class StorageService {

    // =====================================================
    // STORAGE ROOT
    // =====================================================

    @Value("${app.storage.path:uploads}")
    private String storagePath;

    // =====================================================
    // SAVE FILE
    // =====================================================

    public String saveFile(

            MultipartFile file,

            String folder
    ) {

        try {

            // =====================================================
            // VALIDATION
            // =====================================================

            if (

                    file == null ||

                            file.isEmpty()
            ) {

                throw new RuntimeException(
                        "File is empty"
                );
            }

            // =====================================================
            // CLEAN FILE NAME
            // =====================================================

            String originalName =
                    StringUtils.cleanPath(

                            file.getOriginalFilename()
                    );

            // =====================================================
            // EXTENSION
            // =====================================================

            String extension = "";

            int dotIndex =
                    originalName.lastIndexOf(".");

            if (dotIndex >= 0) {

                extension =
                        originalName.substring(dotIndex);
            }

            // =====================================================
            // UNIQUE FILE NAME
            // =====================================================

            String fileName =

                    UUID.randomUUID()

                            + "_"

                            + LocalDateTime.now()
                            .format(

                                    DateTimeFormatter.ofPattern(
                                            "yyyyMMddHHmmss"
                                    )
                            )

                            + extension;

            // =====================================================
            // DIRECTORY
            // =====================================================

            Path uploadDir =
                    Paths.get(

                            storagePath,

                            folder
                    );

            Files.createDirectories(uploadDir);

            // =====================================================
            // TARGET FILE
            // =====================================================

            Path target =
                    uploadDir.resolve(fileName);

            // =====================================================
            // SAVE FILE
            // =====================================================

            Files.copy(

                    file.getInputStream(),

                    target,

                    StandardCopyOption.REPLACE_EXISTING
            );

            // =====================================================
            // RETURN RELATIVE PATH
            // =====================================================

            return "/"

                    + storagePath

                    + "/"

                    + folder

                    + "/"

                    + fileName;

        } catch (IOException e) {

            throw new RuntimeException(

                    "File upload failed",

                    e
            );
        }
    }

    // =====================================================
    // UPLOAD FILE (COMPATIBILITY METHOD)
    // =====================================================

    public String uploadFile(
            MultipartFile file
    ) {

        return saveFile(
                file,
                "documents"
        );
    }

    // =====================================================
    // DELETE FILE
    // =====================================================

    public void deleteFile(
            String filePath
    ) {

        try {

            if (

                    filePath == null ||

                            filePath.isBlank()
            ) {

                return;
            }

            String cleaned =
                    filePath.replaceFirst("^/", "");

            Path path =
                    Paths.get(cleaned);

            Files.deleteIfExists(path);

        } catch (Exception e) {

            // ignore delete failure
        }
    }

    // =====================================================
    // CHECK FILE EXISTS
    // =====================================================

    public boolean exists(
            String filePath
    ) {

        if (

                filePath == null ||

                        filePath.isBlank()
        ) {

            return false;
        }

        String cleaned =
                filePath.replaceFirst("^/", "");

        return Files.exists(
                Paths.get(cleaned)
        );
    }
}