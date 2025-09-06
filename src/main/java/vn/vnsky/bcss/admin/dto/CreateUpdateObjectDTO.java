package vn.vnsky.bcss.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUpdateObjectDTO {

    @NotBlank(message = "Name không được bỏ trống")
    private String name;

    private String url;

    @Builder.Default
    private Boolean isPartner = false;

    @Builder.Default
    private Boolean isMobile = false;

    @NotNull(message = "ordinal không được bỏ trống")
    private Integer ordinal;

    @NotBlank(message = "Code không được bỏ trống")
    private String code;

    private String parentId;

    private List<String> actionIds;
}