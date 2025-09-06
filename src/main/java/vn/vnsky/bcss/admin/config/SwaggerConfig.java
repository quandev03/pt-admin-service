package vn.vnsky.bcss.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${application.version}")
    private String version;

    @Bean
    public OpenAPI adminOpenAPI() {
        return new OpenAPI()
                .info(swaggerInfo())
                .externalDocs(swaggerExternalDoc())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes
                        ("Bearer Authentication", createAPIKeyScheme()))
                ;
    }

    private Info swaggerInfo() {
        return new Info()
                .title("Admin Service API")
                .description("Admin Service API")
                .version(version)
                .license(new License().name("No license"));
    }

    private ExternalDocumentation swaggerExternalDoc() {
        return new ExternalDocumentation().description("Admin Service API Documentation");
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public GroupedOpenApi vnskyApi(ApplicationProperties applicationProperties) {
        return GroupedOpenApi.builder()
                .group("1.vnsky")
                .displayName("VNSKY API")
                .pathsToMatch(applicationProperties.getVnskyWebOAuth2ClientInfo().getApiPrefix() + "/**")
                .build();
    }

    @Bean
    public GroupedOpenApi partnerApi(ApplicationProperties applicationProperties) {
        return GroupedOpenApi.builder()
                .group("2.partner")
                .displayName("Partner API")
                .pathsToMatch(applicationProperties.getPartnerWebOAuth2ClientInfo().getApiPrefix() + "/**")
                .build();
    }

    @Bean
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder()
                .group("3.internal")
                .displayName("Internal API")
                .pathsToMatch("/internal/**")
                .build();
    }

    @Bean
    public GroupedOpenApi thirdPartyInternalApi() {
        return GroupedOpenApi.builder()
                .group("4.third-party-private")
                .displayName("Third Party Private API")
                .pathsToMatch("/third-party/private/**")
                .build();
    }
    @Bean
    public GroupedOpenApi thirdPartyPublicApi() {
        return GroupedOpenApi.builder()
                .group("3.third-party-public")
                .displayName("Third Party Public API")
                .pathsToMatch("/third-party/public/**")
                .build();
    }

}
