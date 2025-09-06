package vn.vnsky.bcss.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class ChangePasswordDTO {

    @NotBlank
    private String oldPwd;

    @NotBlank
    private String newPwd;

}
