package vn.vnsky.bcss.admin.service.impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import vn.vnsky.bcss.admin.config.MinioProperties;
import vn.vnsky.bcss.admin.service.StorageService;

import java.io.InputStream;

@Slf4j
@Service
public class MinioStorageService implements StorageService {

    private final MinioProperties minioProperties;

    private final MinioClient minioClient;

    @Autowired
    public MinioStorageService(MinioProperties minioProperties, MinioClient minioClient) {
        this.minioProperties = minioProperties;
        this.minioClient = minioClient;
    }

    @Override
    public void upload(InputStream is, String serverPath) {
        FileTransferOption defaultOption = FileTransferOption.builder()
                .toPublic(false)
                .generateTimestamp(true)
                .build();
        this.upload(is, serverPath, defaultOption);
    }

    @SneakyThrows
    @Override
    public void upload(InputStream is, String serverPath, FileTransferOption option) {
        String bucketName = option.isToPublic() ? this.minioProperties.getBucketPublic() : this.minioProperties.getBucketPrivate();
        this.minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(serverPath)
                .stream(is, is.available(), 0L)
                .build());
    }

    @Override
    public Resource download(String serverPath) {
        FileTransferOption defaultOption = FileTransferOption.builder()
                .fromPublic(false)
                .generateTimestamp(true)
                .build();
        return this.download(serverPath, defaultOption);
    }

    @SneakyThrows
    @Override
    public Resource download(String serverPath, FileTransferOption option) {
        String bucketName = option.isFromPublic() ? this.minioProperties.getBucketPublic() : this.minioProperties.getBucketPrivate();
        InputStream inputStream = this.minioClient.getObject(GetObjectArgs.builder()
                .object(serverPath)
                .bucket(bucketName)
                .build());
        return new InputStreamResource(inputStream);
    }

    @Override
    public void delete(String serverPath) {
        FileTransferOption defaultOption = FileTransferOption.builder()
                .fromPublic(false)
                .generateTimestamp(true)
                .build();
        this.delete(serverPath, defaultOption);
    }

    @SneakyThrows
    @Override
    public void delete(String serverPath, FileTransferOption option) {
        String bucketName = option.isFromPublic() ? this.minioProperties.getBucketPublic() : this.minioProperties.getBucketPrivate();
        this.minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(serverPath)
                .build());
    }
}
