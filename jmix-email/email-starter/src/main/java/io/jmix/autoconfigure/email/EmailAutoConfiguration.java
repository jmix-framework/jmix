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
import io.jmix.email.authentication.impl.MicrosoftOAuth2TokenProvider;
import jakarta.mail.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

@AutoConfiguration
@EnableConfigurationProperties(MailProperties.class)
@Import({CoreConfiguration.class, DataConfiguration.class, EmailConfiguration.class})
public class EmailAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(EmailAutoConfiguration.class);

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBooleanProperty(name = "jmix.email.oauth2.enabled")
    public static class OAuth2Configuration {

        @Bean("email_MicrosoftOAuth2TokenProvider")
        @ConditionalOnProperty(name = "jmix.email.oauth2.provider", havingValue = "microsoft")
        @ConditionalOnMissingBean(OAuth2TokenProvider.class)
        public OAuth2TokenProvider microsoftOAuth2TokenProvider(EmailerProperties emailerProperties,
                                                                EmailRefreshTokenManager refreshTokenManager) {
            log.debug("Create MicrosoftOAuth2TokenProvider");
            return new MicrosoftOAuth2TokenProvider(emailerProperties, refreshTokenManager);
        }

        @Bean("email_GoogleOAuth2TokenProvider")
        @ConditionalOnProperty(name = "jmix.email.oauth2.provider", havingValue = "google")
        @ConditionalOnMissingBean(OAuth2TokenProvider.class)
        public OAuth2TokenProvider googleOAuth2TokenProvider(EmailerProperties emailerProperties,
                                                             EmailRefreshTokenManager refreshTokenManager) {
            log.debug("Create GoogleOAuth2TokenProvider");
            return new GoogleOAuth2TokenProvider(emailerProperties, refreshTokenManager);
        }

        @Bean("email_JavaMailSender")
        public JavaMailSender javaMailSender(MailProperties mailProperties,
                                             OAuth2TokenProvider tokenProvider,
                                             ObjectProvider<SslBundles> sslBundles) {
            log.debug("Create JavaMailSender with OAuth2 support");

            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            applyProperties(sender, mailProperties, sslBundles.getIfAvailable());

            OAuth2Authenticator authenticator = new OAuth2Authenticator(mailProperties.getUsername(), tokenProvider);

            Session session = Session.getInstance(sender.getJavaMailProperties(), authenticator);
            sender.setSession(session);

            return sender;
        }

        protected void applyProperties(JavaMailSenderImpl sender, MailProperties properties, SslBundles sslBundles) {
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

        protected Properties asProperties(Map<String, String> source) {
            Properties properties = new Properties();
            properties.putAll(source);
            return properties;
        }
    }
}
