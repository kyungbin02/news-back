package myapp.backend.domain.board.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.folder:columns}")
    private String defaultFolder;

    @Value("${cloud.aws.region}")
    private String region;

    public S3Uploader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String upload(MultipartFile multipartFile, String folderName) throws IOException {
        String folder = StringUtils.hasText(folderName) ? folderName : defaultFolder;
        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "image"; // 경빈 S3 추가 - 널 파일명 기본값 처리
        }
        originalFilename = StringUtils.cleanPath(originalFilename);
        String uniqueName = UUID.randomUUID() + "-" + originalFilename;
        String key = folder + "/" + uniqueName;

        // 경빈 S3 추가 - 버킷 정책으로 퍼블릭 읽기 허용되어 있으므로 ACL 설정 불필요
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(multipartFile.getContentType())
                .build();

        // 경빈 S3 추가 - 이미지 파일을 S3로 업로드
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    public void delete(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return;
        }

        String key = extractKey(imageUrl);
        if (!StringUtils.hasText(key)) {
            return;
        }

        // 경빈 S3 추가 - 기존 이미지 정리
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    private String extractKey(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return null;
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}


