package vn.vnsky.bcss.admin.service;

import org.springframework.http.HttpHeaders;
import vn.vnsky.bcss.admin.config.ApplicationProperties;

public interface DeviceService {

    boolean getIsMobile(HttpHeaders httpHeaders);
    ApplicationProperties.OAuth2WebClientInfo getWebClientInfo(Boolean isPartner, Boolean isMobile);
}