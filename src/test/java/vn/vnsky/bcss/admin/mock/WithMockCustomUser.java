package vn.vnsky.bcss.admin.mock;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    String id() default "01JKFEPH79BE0HAVTHQ0S7PD8Y";

    String username() default "rob";

    String fullname() default "Rob Winch";

    int gender() default 1;

    String email() default "rob_winch@mailinator.com";

    String phoneNumber() default "";

    String clientId() default "000000000000";

    String clientCode() default "VNSKY";

    String clientName() default "VNSKY";

    String appId() default "22667948-9645-46fe-8ff3-5396fa93bf91";

    String appCode() default "vnsky-internal";

    String appName() default "Web BCSS nội bộ";

}