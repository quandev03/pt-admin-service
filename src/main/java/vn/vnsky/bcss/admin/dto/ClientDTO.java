package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClientDTO extends AbstractAuditingDTO implements Serializable {

    public static final int CLIENT_CODE_MAXLENGTH = 5;

    @Schema(title = "ID đối tác", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(title = "Mã đối tác", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = CLIENT_CODE_MAXLENGTH)
    private String code;

    @Schema(title = "Tên đối tác", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(title = "Tên người đại diện")
    private String contactName;

    @Schema(title = "Chức danh người đại diện")
    private String contactPosition;

    @Schema(title = "Địa chỉ thư điện tử người đại diện")
    private String contactEmail;

    @Schema(title = "Số điện thoại người đại diện")
    private String contactPhone;

    @Schema(title = "Địa chỉ chi tiết đường phố")
    private String permanentAddress;

    @Schema(title = "Địa chỉ ID cấp tỉnh")
    private Long permanentProvinceId;

    @Schema(title = "Địa chỉ ID cấp huyện")
    private Long permanentDistrictId;

    @Schema(title = "Địa chỉ ID cấp xã")
    private Long permanentWardId;

    @Schema(title = "Trạng thái đối tác", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer status;

    @Schema(title = "Danh sách user của đối tác", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<UserDTO> users;

}
