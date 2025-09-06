package vn.vnsky.bcss.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.service.DeviceService;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {
    private static final Set<String> DEVICES_PLATFORMS = Set.of("android", "ios");

    private static final String SEC_CH_UA_PLATFORM = "sec-ch-ua-platform";

    private final ApplicationProperties applicationProperties;

    @Override
    public boolean getIsMobile(HttpHeaders httpHeaders) {
        List<String> platforms = httpHeaders.get(SEC_CH_UA_PLATFORM);
        if(platforms == null || platforms.isEmpty()) {
            return false;
        }

        for(String platform : platforms) {
            if(DEVICES_PLATFORMS.contains(platform)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ApplicationProperties.OAuth2WebClientInfo getWebClientInfo(Boolean isPartner, Boolean isMobile) {
        if(Boolean.TRUE.equals(isPartner)){
            if(Boolean.TRUE.equals(isMobile)){
                return this.applicationProperties.getSaleAppOAuth2ClientInfo();
            }else{
                return this.applicationProperties.getPartnerWebOAuth2ClientInfo();
            }
        }else {
            return this.applicationProperties.getVnskyWebOAuth2ClientInfo();
        }
    }
}