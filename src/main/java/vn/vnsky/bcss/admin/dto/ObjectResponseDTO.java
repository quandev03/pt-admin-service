package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.vnsky.bcss.admin.util.DbColumnMapper;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectResponseDTO {

    @DbColumnMapper("id")
    private String key;

    @DbColumnMapper("name")
    private String title;

    @DbColumnMapper("uri")
    private String uri;

    @DbColumnMapper("parent_id")
    private String parentId;

    @DbColumnMapper("joined_actions")
    @JsonIgnore
    private String joinedActions;

    private String code;

    private Integer ordinal;

    private List<ObjectResponseDTO> children;
}
