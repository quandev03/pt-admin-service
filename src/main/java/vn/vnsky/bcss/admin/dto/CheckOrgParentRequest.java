package vn.vnsky.bcss.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckOrgParentRequest {
    private String orgId;
    private String clientId;
    private String currentUserId;
}
