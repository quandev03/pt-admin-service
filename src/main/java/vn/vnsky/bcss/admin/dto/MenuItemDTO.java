package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.vnsky.bcss.admin.util.DbColumnMapper;

import java.util.List;

/**
 * @author thanhvt
 * @created 12/04/2023 - 10:11 SA
 * @project str-auth
 * @since 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemDTO {

    @DbColumnMapper("id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    @DbColumnMapper("code")
    private String code;

    @DbColumnMapper("name")
    private String name;

    @DbColumnMapper("parent_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String parentId;

    @DbColumnMapper("uri")
    private String uri;

    @DbColumnMapper("icon")
    private String icon;

    @DbColumnMapper("level")
    @JsonIgnore
    private Integer level;

    @DbColumnMapper("joined_actions")
    @JsonIgnore
    private String joinedActions;
    private List<MenuItemDTO> items;

    @JsonGetter("actions")
    public String[] getActions() {
        return joinedActions != null ? joinedActions.split(",") : new String[]{};
    }


}
