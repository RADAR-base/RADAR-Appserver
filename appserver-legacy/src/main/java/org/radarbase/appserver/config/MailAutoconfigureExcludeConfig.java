package org.radarbase.appserver.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "radar.notification.email.enabled", havingValue = "false", matchIfMissing = true)
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration.class,
    org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration.class
})
public class MailAutoconfigureExcludeConfig { }
