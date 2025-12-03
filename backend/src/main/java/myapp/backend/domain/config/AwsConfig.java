package myapp.backend.domain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // 경빈 S3 추가 - 기본 자격 증명 체인을 사용해 EB IAM 역할/로컬 credentials 자동 인식
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}


