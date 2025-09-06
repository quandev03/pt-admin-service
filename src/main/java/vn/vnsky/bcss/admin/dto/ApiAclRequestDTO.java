package vn.vnsky.bcss.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiAclRequestDTO {

    private String serviceCode;

    private String uriPattern;

    private String method;

}
