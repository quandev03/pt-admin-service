package vn.vnsky.bcss.admin.dto;

import lombok.*;
import com.vnsky.excel.annotation.XlsxColumn;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UploadAclPolicyRequestDTO {
    @XlsxColumn(readIndex = 0, writeIndex = 0, header =  "URL_PATTERN")
    private String urlPattern;

    @XlsxColumn(readIndex = 1, writeIndex = 1, header =  "METHOD")
    private String method;

    @XlsxColumn(readIndex = 2, writeIndex = 2, header =  "OBJECT_CODE")
    private String objectCode;

    @XlsxColumn(readIndex = 3, writeIndex = 3, header =  "ACTION_CODE")
    private String actionCode;

    @XlsxColumn(readIndex = 4, writeIndex = 4, header =  "CLIENT_ID")
    private String clientId;

    @XlsxColumn(readIndex = 5, writeIndex = 5, header =  "SERVICE_CODE")
    private String serviceCode;

    @XlsxColumn(readIndex = 6, writeIndex = 6, header =  "Thành công")
    private String result;

}