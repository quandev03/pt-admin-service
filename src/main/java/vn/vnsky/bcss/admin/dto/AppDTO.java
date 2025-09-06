package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppDTO {

    private String id;

    private String code;

    private String name;

    private String ssoProvider;

}
