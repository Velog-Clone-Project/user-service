package com.example.userservice.service;

import com.example.userservice.config.MinioConfig;
import com.example.userservice.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioConfig minioConfig;
    private final MinioProperties minioProperties;

    public String upload(MultipartFile file) {
        try {
            MinioClient minioClient = minioConfig.minioClient();

            try (InputStream is = file.getInputStream()) {

                String objectName = UUID.randomUUID().toString();

                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(objectName)
                        .stream(is, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

                return minioProperties.getUrl() + "/" + minioProperties.getBucket() + "/" + objectName;

            } catch (IOException e) {
                throw new RuntimeException("MinIO upload failed" + e);
            } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                     InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteIfUploaded(String imageUrl) {
        try {
            String prefix = minioProperties.getUrl() + "/" + minioProperties.getBucket() + "/";
            if (imageUrl.startsWith(prefix)) {
                String objectName = imageUrl.substring(prefix.length());
                MinioClient client = MinioClient.builder()
                        .endpoint(minioProperties.getUrl())
                        .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                        .build();
                client.removeObject(RemoveObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(objectName)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete unused image", e);
        }
    }
}
