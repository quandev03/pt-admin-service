package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import vn.vnsky.bcss.admin.constant.ResponseView;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DepartmentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8142407635038246236L;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    private Long id;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    private String code;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    private String name;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    private Integer status;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    private String email;
}
