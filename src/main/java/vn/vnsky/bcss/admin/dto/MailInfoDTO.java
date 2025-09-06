package vn.vnsky.bcss.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MailInfoDTO {

    @NotBlank
    private String email;

    private String to;

    private String subject;

    private String expireTime;

    private String content;

    private String username;

    private String password;

    private String companyName;

    private String url;
}
