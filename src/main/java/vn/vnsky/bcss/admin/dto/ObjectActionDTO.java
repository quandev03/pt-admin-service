package vn.vnsky.bcss.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.vnsky.bcss.admin.util.DbColumnMapper;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectActionDTO {
    @DbColumnMapper("id")
    private String id;

    @DbColumnMapper("object_id")
    private String objectId;

    @DbColumnMapper("object_code")
    private String objectCode;

    @DbColumnMapper("object_name")
    private String objectName;

    @DbColumnMapper("action_id")
    private String actionId;

    @DbColumnMapper("action_code")
    private String actionCode;

    @DbColumnMapper("action_name")
    private String actionName;

}
