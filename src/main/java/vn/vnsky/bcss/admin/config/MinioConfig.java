package vn.vnsky.bcss.admin.config;


import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("default")
public class MinioConfig {

    private static final String S3_PUBLIC_POLICY = """
            {
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Effect": "Allow",
                        "Principal": {
                            "AWS": [
                                "*"
                            ]
                        },
                        "Action": [
                            "s3:GetBucketLocation",
                            "s3:ListBucket"
                        ],
                        "Resource": [
                            "arn:aws:s3:::%BUCKET_NAME%"
                        ]
                    },
                    {
                        "Effect": "Allow",
                        "Principal": {
                            "AWS": [
                                "*"
                            ]
                        },
                        "Action": [
                            "s3:GetObject"
                        ],
                        "Resource": [
                            "arn:aws:s3:::%BUCKET_NAME%/*"
                        ]
                    }
                ]
            }
            """;

    @Bean
    @SneakyThrows
    public MinioClient minioClient(MinioProperties minioProperties) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getUrl())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        minioClient.setTimeout(minioProperties.getConnectTimeout(), minioProperties.getWriteTimeout(), minioProperties.getReadTimeout());
        minioClient.ignoreCertCheck();

        boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucketPublic())
                .build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucketPublic())
                    .build());

            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(minioProperties.getBucketPublic())
                    .config(S3_PUBLIC_POLICY.replace("%BUCKET_NAME%", minioProperties.getBucketPublic())).build());
        } else {
            log.info("Minio bucket exists: {}", minioProperties.getBucketPublic());
        }

        found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketPrivate()).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketPrivate()).build());
        } else {
            log.info("Minio bucket exists: {}", minioProperties.getBucketPrivate());
        }
        log.info("Minio client initialized");
        return minioClient;
    }
}
