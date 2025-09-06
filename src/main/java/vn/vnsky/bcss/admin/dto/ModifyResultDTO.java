package vn.vnsky.bcss.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModifyResultDTO {

    private Integer count;

    private String message;

    private Boolean constraint;

    private Object value;
}
