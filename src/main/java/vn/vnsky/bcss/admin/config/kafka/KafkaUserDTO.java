package vn.vnsky.bcss.admin.config.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(fluent = true, chain = true)
public class KafkaUserDTO {

    private String clientId;

    private String clientCode;

    private String clientName;

    private String siteId;

    private String siteCode;

    private String siteName;

    private String userId;

    private String userFullname;

    private String userUsername;

    private String userPreferredUsername;

}
