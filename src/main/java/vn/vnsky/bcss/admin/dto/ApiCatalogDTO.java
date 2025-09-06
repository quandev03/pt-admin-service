package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCatalogDTO {

    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("uri_pattern")
    private String uriPattern;

    @JsonProperty("method")
    private String method;

    @JsonProperty("status")
    private Integer status;

    private String serviceCode;
}
