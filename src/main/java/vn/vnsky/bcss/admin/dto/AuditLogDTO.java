package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogDTO {

    private String subSystem;

    private ZonedDateTime actionTime;

    private String actionType;

    private String targetType;

    private Object preValue;

    private Object postValue;

    private String clientIp;

    private Integer status;

}
