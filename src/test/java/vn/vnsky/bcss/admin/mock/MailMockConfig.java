package vn.vnsky.bcss.admin.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class MailMockConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSenderImpl.class);
    }

}
