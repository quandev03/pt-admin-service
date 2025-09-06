package vn.vnsky.bcss.admin.mock;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.config.auth.AclAuthorizationInterceptor;
import vn.vnsky.bcss.admin.service.ObjectService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@TestConfiguration
public class AuthorizationMockConfig {

    @Bean
    public AclAuthorizationInterceptor aclAuthorizationInterceptor(@Value("${spring.application.name}") String serviceCode, ApplicationProperties applicationProperties,
                                                                   ObjectService objectService) {
        AclAuthorizationInterceptor aclAuthorizationInterceptor = spy(new AclAuthorizationInterceptor(serviceCode, applicationProperties, objectService));
        doReturn(true)
                .when(aclAuthorizationInterceptor)
                .preHandle(any(HttpServletRequest.class),
                        any(HttpServletResponse.class), any());
        return aclAuthorizationInterceptor;
    }

}
