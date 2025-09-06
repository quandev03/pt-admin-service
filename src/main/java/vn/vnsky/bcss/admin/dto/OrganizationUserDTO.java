package vn.vnsky.bcss.admin.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUserDTO {
    private String id;
    private String orgId;
    private String userId;
    private Integer isCurrent;
    private String userName;
    private String userFullname;
    private String clientId;
    private Integer status;
    private String email;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String createdBy;
    private String modifiedBy;
}
