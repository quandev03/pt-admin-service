package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientEntity extends AbstractAuditingEntity implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_position")
    private String contactPosition;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @Column(name = "permanent_province_id")
    private Long permanentProvinceId;

    @Column(name = "permanent_district_id")
    private Long permanentDistrictId;

    @Column(name = "permanent_ward_id")
    private Long permanentWardId;

    @Column(name = "status")
    private Integer status;

}
