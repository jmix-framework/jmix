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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.ClassUtils;
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
    public static class OAuth2Configuration implements BeanClassLoaderAware {

        protected static final String MSAL_CLASS = "com.microsoft.aad.msal4j.ConfidentialClientApplication";
        protected static final String GOOGLE_AUTH_CLASS = "com.google.auth.oauth2.UserCredentials";

        protected ClassLoader beanClassLoader;

        @Override
        public void setBeanClassLoader(ClassLoader classLoader) {
            this.beanClassLoader = classLoader;
        }

        @Bean("email_MicrosoftOAuth2TokenProvider")
        @ConditionalOnProperty(name = "jmix.email.oauth2.provider", havingValue = "microsoft")
        @ConditionalOnClass(name = MSAL_CLASS)
        @ConditionalOnMissingBean(OAuth2TokenProvider.class)
        public OAuth2TokenProvider microsoftOAuth2TokenProvider(EmailerProperties emailerProperties,
                                                                EmailRefreshTokenManager refreshTokenManager) {
            log.debug("Create MicrosoftOAuth2TokenProvider");
            validateCredentialProperties(emailerProperties);
            return new MicrosoftOAuth2TokenProvider(emailerProperties, refreshTokenManager);
        }

        @Bean("email_GoogleOAuth2TokenProvider")
        @ConditionalOnProperty(name = "jmix.email.oauth2.provider", havingValue = "google")
        @ConditionalOnClass(name = GOOGLE_AUTH_CLASS)
        @ConditionalOnMissingBean(OAuth2TokenProvider.class)
        public OAuth2TokenProvider googleOAuth2TokenProvider(EmailerProperties emailerProperties,
                                                             EmailRefreshTokenManager refreshTokenManager) {
            log.debug("Create GoogleOAuth2TokenProvider");
            validateCredentialProperties(emailerProperties);
            return new GoogleOAuth2TokenProvider(emailerProperties, refreshTokenManager);
        }

        @Bean("email_JavaMailSender")
        public JavaMailSender javaMailSender(MailProperties mailProperties,
                                             ObjectProvider<OAuth2TokenProvider> tokenProviders,
                                             EmailerProperties emailerProperties,
                                             ObjectProvider<SslBundles> sslBundles) {
            OAuth2TokenProvider tokenProvider = tokenProviders.getIfAvailable();
            if (tokenProvider == null) {
                throw new IllegalStateException(
                        buildMissingTokenProviderMessage(emailerProperties.getOAuth2().getProvider()));
            }
            if (!StringUtils.hasText(mailProperties.getUsername())) {
                throw new IllegalStateException("'spring.mail.username' must be set when OAuth2 authentication" +
                        " is enabled. It is used as the identity for SMTP XOAUTH2 authentication");
            }

            log.debug("Create JavaMailSender with OAuth2 support");

            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            applyProperties(sender, mailProperties, sslBundles.getIfAvailable());

            Properties javaMailProperties = sender.getJavaMailProperties();
            String protocol = StringUtils.hasLength(mailProperties.getProtocol())
                    ? mailProperties.getProtocol()
                    : "smtp";
            // The XOAUTH2 SASL mechanism is not included in the Jakarta Mail defaults,
            // so enable it unless explicitly configured by the application.
            javaMailProperties.putIfAbsent("mail." + protocol + ".auth", "true");
            javaMailProperties.putIfAbsent("mail." + protocol + ".auth.mechanisms", "XOAUTH2");

            OAuth2Authenticator authenticator = new OAuth2Authenticator(mailProperties.getUsername(), tokenProvider);

            Session session = Session.getInstance(javaMailProperties, authenticator);
            sender.setSession(session);

            return sender;
        }

        protected void validateCredentialProperties(EmailerProperties emailerProperties) {
            EmailerProperties.OAuth2 oauth2 = emailerProperties.getOAuth2();
            if (!StringUtils.hasText(oauth2.getClientId())) {
                throw new IllegalStateException(
                        "'jmix.email.oauth2.client-id' must be set when OAuth2 authentication is enabled");
            }
            if (!StringUtils.hasText(oauth2.getSecret())) {
                throw new IllegalStateException(
                        "'jmix.email.oauth2.secret' must be set when OAuth2 authentication is enabled");
            }
        }

        protected String buildMissingTokenProviderMessage(@Nullable String provider) {
            String baseMessage = "OAuth2 authentication is enabled ('jmix.email.oauth2.enabled=true')" +
                    " but no OAuth2TokenProvider is available. ";
            if (!StringUtils.hasText(provider)) {
                return baseMessage + "Set 'jmix.email.oauth2.provider' to 'google' or 'microsoft'," +
                        " or define a custom OAuth2TokenProvider bean";
            }
            return switch (provider) {
                case "microsoft" -> baseMessage + (isClassMissing(MSAL_CLASS)
                        ? "Add the 'com.microsoft.azure:msal4j' dependency to the application"
                        : "Check the 'jmix.email.oauth2' configuration");
                case "google" -> baseMessage + (isClassMissing(GOOGLE_AUTH_CLASS)
                        ? "Add the 'com.google.auth:google-auth-library-oauth2-http' dependency to the application"
                        : "Check the 'jmix.email.oauth2' configuration");
                default -> baseMessage + "Unknown provider '" + provider + "'." +
                        " Supported values: 'google', 'microsoft'; or define a custom OAuth2TokenProvider bean";
            };
        }

        protected boolean isClassMissing(String className) {
            return !ClassUtils.isPresent(className, beanClassLoader);
        }

        protected void applyProperties(JavaMailSenderImpl sender, MailProperties properties, SslBundles sslBundles) {
            sender.setHost(properties.getHost());
            if (properties.getPort() != null) {
                sender.setPort(properties.getPort());
            }
            sender.setUsername(properties.getUsername());
            if (properties.getPassword() != null) {
                log.warn("'spring.mail.password' is ignored because OAuth2 authentication is enabled");
            }
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
