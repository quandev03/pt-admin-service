package vn.vnsky.bcss.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupUserRequestDTO {

    @NotBlank
    private String groupId;

    @NotBlank
    private String userId;
}

