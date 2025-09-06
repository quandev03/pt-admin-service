package vn.vnsky.bcss.admin.config.auth;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import vn.vnsky.bcss.admin.util.SecurityUtil;

import java.util.Optional;

/**
 * Implementation of {@link AuditorAware} based on Spring Security.
 */
@Component
public class CustomAuditorAware implements AuditorAware<String> {

    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        return SecurityUtil.getCurrentUser()
                .map(SecurityUtil::getPreferredUsername);
    }

}
