package vn.vnsky.bcss.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyCacheConfigDTO {

    private Boolean userInfoCacheOn;

    private Boolean userPolicyCacheOn;

    private Boolean apiAclCacheOn;

}
