package vn.vnsky.bcss.admin.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldsValidationEntry {

    private String field;

    private String detail;
}
