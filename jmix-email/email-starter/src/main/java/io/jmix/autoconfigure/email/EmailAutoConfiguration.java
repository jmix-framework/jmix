/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.autoconfigure.email;

import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.email.EmailConfiguration;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.authentication.OAuth2Authenticator;
import io.jmix.email.authentication.OAuth2TokenProvider;
import io.jmix.email.authentication.impl.GoogleOAuth2TokenProvider;
import jakarta.mail.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@AutoConfiguration
@EnableConfigurationProperties(MailProperties.class)
@Import({CoreConfiguration.class, DataConfiguration.class, EmailConfiguration.class})
public class EmailAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(EmailAutoConfiguration.class);

    /*@Bean
    public OAuth2TokenProvider tokenProvider(EmailerProperties emailerProperties*//*,
            @Value("${azure.client-id}") String clientId,
            @Value("${azure.client-secret}") String clientSecret,
            @Value("${azure.tenant-id}") String tenantId*//*
    ) throws Exception {
        return new MicrosoftTokenProvider(
                emailerProperties.getEntraClientId(),
                emailerProperties.getEntraSecret(),
                emailerProperties.getEntraTenantId()
        );
    }*/


    // TODO IVGA: check MailSenderPropertiesConfiguration, MailSenderJndiConfiguration
    /*@Bean
    public JavaMailSender mailSender(
            OAuth2TokenProvider tokenProvider,
            MailProperties mailProperties
    ) {
        OAuth2MailSender sender = new OAuth2MailSender(tokenProvider);

        sender.setJavaMailProperties(mailProperties.toProperties());
        return sender;
    }*/

    /*@Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender mailSender(MailOAuth2AuthorizedClientManager tokenProvider,
                                     MailProperties properties,
                                     EmailerProperties emailerProperties,
                                     ObjectProvider<SslBundles> sslBundles) {
        JavaMailSenderImpl sender = new OAuth2MailSender(tokenProvider, emailerProperties.getRegistrationId(), properties.getUsername());
        applyProperties(properties, sender, sslBundles.getIfAvailable());
        return sender;
    }*/

    //TODO nested google configuration?
    @Bean("email_OAuth2TokenProvider") //TODO email_GoogleOAuth2TokenProvider?
    @ConditionalOnMissingBean(OAuth2TokenProvider.class)
    public OAuth2TokenProvider googleOAuth2TokenProvider(EmailerProperties emailerProperties,
                                                         EmailRefreshTokenManager refreshTokenManager) {
        return new GoogleOAuth2TokenProvider(emailerProperties, refreshTokenManager);
    }

    @Bean("email_JavaMailSender")
    //@ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender(MailProperties mailProperties,
                                         OAuth2TokenProvider tokenProvider,
                                         ObjectProvider<SslBundles> sslBundles) {
        log.info("[IVGA] Create email_JavaMailSender");

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        applyProperties(sender, mailProperties, sslBundles.getIfAvailable());

        OAuth2Authenticator authenticator = new OAuth2Authenticator(mailProperties.getUsername(), tokenProvider);

        Session session = Session.getInstance(
                sender.getJavaMailProperties(), authenticator
                /*new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        String token = tokenProvider.getAccessToken();
                        return new PasswordAuthentication(mailProperties.getUsername(), token);
                    }
                }*/);
        sender.setSession(session);

        /*sender.setHost(host);
        sender.setPort(port);
        sender.setDefaultEncoding("UTF-8");*/

        return sender;

        /*JavaMailSenderImpl mailSender = new JavaMailSenderImpl() {
            @Override
            public Session getSession() {
                try {
                    String accessToken = tokenProvider.getAccessToken();
                    Properties props = new Properties();
                    props.putAll(javaMailPropertiesGoogle());

                    return Session.getInstance(props,
                            new OAuth2Authenticator(email, accessToken));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create mail session", e);
                }
            }
        };

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setDefaultEncoding("UTF-8");

        return mailSender;*/
    }

    protected void applyProperties(JavaMailSenderImpl sender, MailProperties properties, SslBundles sslBundles) {
        log.info("[IVGA] Apply email_JavaMailSender properties");

        sender.setHost(properties.getHost());
        if (properties.getPort() != null) {
            sender.setPort(properties.getPort());
        }
        sender.setUsername(properties.getUsername());
        sender.setPassword(properties.getPassword());
        sender.setProtocol(properties.getProtocol());
        if (properties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(properties.getDefaultEncoding().name());
        }
        Properties javaMailProperties = asProperties(properties.getProperties());
        String protocol = properties.getProtocol();
        protocol = (!StringUtils.hasLength(protocol)) ? "smtp" : protocol;
        MailProperties.Ssl ssl = properties.getSsl();
        if (ssl.isEnabled()) {
            javaMailProperties.setProperty("mail." + protocol + ".ssl.enable", "true");
        }
        if (ssl.getBundle() != null) {
            SslBundle sslBundle = sslBundles.getBundle(ssl.getBundle());
            javaMailProperties.put("mail." + protocol + ".ssl.socketFactory",
                    sslBundle.createSslContext().getSocketFactory());
        }
        if (!javaMailProperties.isEmpty()) {
            sender.setJavaMailProperties(javaMailProperties);
        }
    }

    private Properties asProperties(Map<String, String> source) {
        Properties properties = new Properties();
        properties.putAll(source);
        return properties;
    }
}
