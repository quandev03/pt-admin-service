package vn.vnsky.bcss.admin.service;

import lombok.*;
import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface StorageService {

    void upload(InputStream is, String serverPath);

    void upload(InputStream is, String serverPath, FileTransferOption option);

    Resource download(String serverPath);

    Resource download(String serverPath, FileTransferOption option);

    void delete(String serverPath);

    void delete(String serverPath, FileTransferOption option);

    @ToString
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    class FileTransferOption {

        @Builder.Default
        private boolean toPublic = false;

        @Builder.Default
        private boolean fromPublic = false;

        private String ext;

        @Builder.Default
        private boolean generateTimestamp = false;
    }

}
