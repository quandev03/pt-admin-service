package vn.vnsky.bcss.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmTokenDTO {

    private String token;

    private LocalDateTime createdTime;

    private LocalDateTime lastAccessTime;

}
