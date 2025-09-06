package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCatalogRegistrationDTO {

    @JsonProperty("service_code")
    private String serviceCode;

    @JsonProperty("api_catalogs")
    private List<ApiCatalogDTO> apiCatalogs;

}
