package vn.vnsky.bcss.admin.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import vn.vnsky.bcss.admin.dto.MailInfoDTO;
import vn.vnsky.bcss.admin.service.MailService;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    private static final String ACTIVATE_ACCOUNT_FORM = "activate_account";

    private static final String FORGOT_PASSWORD_FORM = "forgot_password";

    private final JavaMailSender javaMailSender;

    private final ISpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${spring.mail.properties.mail-sender-display-name}")
    private String mailFromDisplay;

    @Autowired
    public MailServiceImpl(JavaMailSender javaMailSender, ISpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    private void sendEmail(String to, String subject, String contentTemp) {
        try {
            MimeMessage message = this.javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFrom, mailFromDisplay);
            helper.setTo(to);
            helper.setText(contentTemp, true);
            helper.setSubject(subject);
            this.javaMailSender.send(message);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Email could not be sent to user '{}'", to, e);
            } else {
                log.warn("Email could not be sent to user '{}': {}", to, e.getMessage());
            }
        }
    }

    private void sendEmailFromTemplate(String templateName, MailInfoDTO dto) {
        Context context = new Context();
        if (!ObjectUtils.isEmpty(dto)) {
            context.setVariable("email", dto.getEmail());
            context.setVariable("content", dto.getContent());
            context.setVariable("expireTime", dto.getExpireTime());
            context.setVariable("username", dto.getUsername());
            context.setVariable("password", dto.getPassword());
            context.setVariable("companyName", dto.getCompanyName());
            context.setVariable("url", dto.getUrl());
        }
        String contentTemp = templateEngine.process(templateName, context);
        this.sendEmail(dto.getTo(), dto.getSubject(), contentTemp);
    }

    @Async
    @Override
    public void sendTokenActivateAccount(MailInfoDTO dto) {
        this.sendEmailFromTemplate(ACTIVATE_ACCOUNT_FORM, dto);
    }

    @Async
    @Override
    public void sendForgotPassword(MailInfoDTO dto) {
        this.sendEmailFromTemplate(FORGOT_PASSWORD_FORM, dto);
    }


}
