package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessLogDTO {

    private String subSystem;

    private String actionType;

    private ZonedDateTime accessTime;

    private String clientIp;

    private Integer status;

}
