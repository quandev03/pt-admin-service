package vn.vnsky.bcss.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordConfirmDTO {

    @NotBlank
    private String token;
}
