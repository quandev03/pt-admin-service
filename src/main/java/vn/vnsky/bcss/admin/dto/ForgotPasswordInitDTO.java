package vn.vnsky.bcss.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public class ForgotPasswordInitDTO {

    @NotBlank(groups = {CasePartner.class})
    private String clientIdentity;

    @NotBlank(groups = {CaseInternal.class, CasePartner.class})
    private String email;

    private String username;

    @NotBlank(groups = {CaseInternal.class, CasePartner.class})
    private String callbackUri;

    public interface CasePartner {}

    public interface CaseInternal {}

}
