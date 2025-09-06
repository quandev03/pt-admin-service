package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "FCM_USER_TOKEN")
public class FcmTokenEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;

}
