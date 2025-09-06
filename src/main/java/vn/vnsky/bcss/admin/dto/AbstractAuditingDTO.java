package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.vnsky.bcss.admin.constant.ResponseView;
import vn.vnsky.bcss.admin.util.DbColumnMapper;

import java.time.LocalDateTime;

/**
 * A DTO for the {@link vn.vnsky.bcss.admin.entity.AbstractAuditingEntity} entity
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractAuditingDTO {

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    @DbColumnMapper("created_by")
    private String createdBy;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    @DbColumnMapper("created_date")
    private LocalDateTime createdDate;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    @DbColumnMapper("last_modified_by")
    private String lastModifiedBy;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    @DbColumnMapper("last_modified_date")
    private LocalDateTime lastModifiedDate;

}
