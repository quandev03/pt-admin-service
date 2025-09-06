package vn.vnsky.bcss.admin.config;

import jakarta.servlet.ServletContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import vn.vnsky.bcss.admin.config.auth.AclAuthorizationInterceptor;
import vn.vnsky.bcss.admin.util.CryptoUtil;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


@Slf4j
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements WebMvcConfigurer, ServletContextInitializer, WebServerFactoryCustomizer<WebServerFactory> {

    private final Environment env;

    private final AclAuthorizationInterceptor aclAuthorizationInterceptor;

    private final CorsConfiguration corsConfiguration;

    @Autowired
    public WebConfig(Environment env, CorsConfiguration corsConfiguration,
                     AclAuthorizationInterceptor aclAuthorizationInterceptor) {
        this.env = env;
        this.corsConfiguration = corsConfiguration;
        this.aclAuthorizationInterceptor = aclAuthorizationInterceptor;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver cookieLocaleResolver = new AcceptHeaderLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(Locale.forLanguageTag("vi-VN"));
        return cookieLocaleResolver;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("i18n/messages");
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    public LocalValidatorFactoryBean validatorFactoryBean() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    @Bean
    public static CorsConfiguration corsConfiguration(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setAllowPrivateNetwork(corsProperties.isAllowPrivateNetwork());
        configuration.setExposedHeaders(corsProperties.getExposeHeaders());
        return configuration;
    }

    @Override
    public void onStartup(ServletContext servletContext) {
        if (env.getActiveProfiles().length != 0) {
            log.info("Web application configuration, using profiles: {}", (Object[]) env.getActiveProfiles());
        }
        log.info("Web application fully configured");

    }

    /**
     * Customize the Servlet engine: Mime types, the document root, the cache.
     */
    @Override
    public void customize(WebServerFactory server) {
        setMimeMappings(server);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setFallbackPageable(Pageable.unpaged());
        argumentResolvers.add(resolver);
    }

    private void setMimeMappings(WebServerFactory server) {
        if (server instanceof ConfigurableServletWebServerFactory servletWebServer) {
            MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
            // IE issue, see https://github.com/jhipster/generator-jhipster/pull/711
            mappings.add("html", MediaType.TEXT_HTML_VALUE + ";charset=" + StandardCharsets.UTF_8.name().toLowerCase());
            // CloudFoundry issue, see https://github.com/cloudfoundry/gorouter/issues/64
            mappings.add("json", MediaType.TEXT_HTML_VALUE + ";charset=" + StandardCharsets.UTF_8.name().toLowerCase());
            servletWebServer.setMimeMappings(mappings);
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.aclAuthorizationInterceptor);
    }

    /**
     * Configure the converters to use the ISO format for dates by default.
     */
    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .combine(this.corsConfiguration);
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(10);
    }

    @SneakyThrows
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, ApplicationProperties applicationProperties) {

        final SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
        if (StringUtils.hasText(applicationProperties.getCustomTrustedCert())) {
            // trust specific cert
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            X509Certificate x509Certificate = CryptoUtil.readCertificate(applicationProperties.getCustomTrustedCert());
            trustStore.setCertificateEntry("adn", x509Certificate);
            sslContextBuilder.loadTrustMaterial(trustStore, null);
        } else {
            // always trust certs
            sslContextBuilder.loadTrustMaterial((chain, authType) -> true);
        }
        final SSLContext sslContext = sslContextBuilder.build();

        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(
                        SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(sslContext)
                                .build()
                )
                .build();
        final HttpClient httpClient = HttpClientBuilder.create()
            .setConnectionManager(poolingHttpClientConnectionManager)
            .build();

        final ClientHttpRequestFactory requestFactory =
            new HttpComponentsClientHttpRequestFactory(httpClient);

        log.info("Registered SSL truststore for client requests");
        builder.requestFactory(() -> requestFactory);
        return builder.build();
    }

}
