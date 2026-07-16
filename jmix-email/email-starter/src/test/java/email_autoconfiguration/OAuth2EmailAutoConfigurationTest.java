/*
 * Copyright 2026 Haulmont.
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

package email_autoconfiguration;

import io.jmix.autoconfigure.email.EmailAutoConfiguration;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.authentication.OAuth2TokenProvider;
import io.jmix.email.entity.RefreshToken;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2EmailAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestBaseConfiguration.class, EmailAutoConfiguration.OAuth2Configuration.class);

    @Test
    void testNoBeansWhenOAuth2Disabled() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(JavaMailSender.class);
            assertThat(context).doesNotHaveBean(OAuth2TokenProvider.class);
        });
    }

    @Test
    void testGoogleSenderConfigured() {
        contextRunner
                .withPropertyValues(
                        "jmix.email.oauth2.enabled=true",
                        "jmix.email.oauth2.provider=google",
                        "jmix.email.oauth2.client-id=test-client",
                        "jmix.email.oauth2.secret=test-secret",
                        "spring.mail.username=mailbox@example.com",
                        "spring.mail.password=ignored-password")
                .run(context -> {
                    assertThat(context).hasBean("email_JavaMailSender");
                    JavaMailSenderImpl sender = (JavaMailSenderImpl) context.getBean(JavaMailSender.class);
                    assertThat(sender.getPassword()).isNull();
                    Properties sessionProperties = sender.getSession().getProperties();
                    assertThat(sessionProperties.getProperty("mail.smtp.auth")).isEqualTo("true");
                    assertThat(sessionProperties.getProperty("mail.smtp.auth.mechanisms")).isEqualTo("XOAUTH2");
                });
    }

    @Test
    void testExplicitAuthMechanismsAreRespected() {
        contextRunner
                .withPropertyValues(
                        "jmix.email.oauth2.enabled=true",
                        "jmix.email.oauth2.provider=google",
                        "jmix.email.oauth2.client-id=test-client",
                        "jmix.email.oauth2.secret=test-secret",
                        "spring.mail.username=mailbox@example.com",
                        "spring.mail.properties.mail.smtp.auth.mechanisms=LOGIN PLAIN")
                .run(context -> {
                    JavaMailSenderImpl sender = (JavaMailSenderImpl) context.getBean(JavaMailSender.class);
                    Properties sessionProperties = sender.getSession().getProperties();
                    assertThat(sessionProperties.getProperty("mail.smtp.auth.mechanisms")).isEqualTo("LOGIN PLAIN");
                });
    }

    @Test
    void testMissingProviderProducesActionableError() {
        contextRunner
                .withPropertyValues(
                        "jmix.email.oauth2.enabled=true",
                        "spring.mail.username=mailbox@example.com")
                .run(context -> {
                    assertThat(context).hasFailed();
                    Throwable cause = NestedExceptionUtils.getMostSpecificCause(context.getStartupFailure());
                    assertThat(cause).isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("jmix.email.oauth2.provider");
                });
    }

    @Test
    void testMissingMsalDependencyProducesActionableError() {
        contextRunner
                .withClassLoader(new FilteredClassLoader("com.microsoft.aad.msal4j"))
                .withPropertyValues(
                        "jmix.email.oauth2.enabled=true",
                        "jmix.email.oauth2.provider=microsoft",
                        "jmix.email.oauth2.client-id=test-client",
                        "jmix.email.oauth2.secret=test-secret",
                        "spring.mail.username=mailbox@example.com")
                .run(context -> {
                    assertThat(context).hasFailed();
                    Throwable cause = NestedExceptionUtils.getMostSpecificCause(context.getStartupFailure());
                    assertThat(cause).hasMessageContaining("msal4j");
                });
    }

    @Test
    void testMissingClientIdProducesActionableError() {
        contextRunner
                .withPropertyValues(
                        "jmix.email.oauth2.enabled=true",
                        "jmix.email.oauth2.provider=google",
                        "jmix.email.oauth2.secret=test-secret",
                        "spring.mail.username=mailbox@example.com")
                .run(context -> {
                    assertThat(context).hasFailed();
                    Throwable cause = NestedExceptionUtils.getMostSpecificCause(context.getStartupFailure());
                    assertThat(cause).hasMessageContaining("jmix.email.oauth2.client-id");
                });
    }

    @Test
    void testMissingUsernameProducesActionableError() {
        contextRunner
                .withPropertyValues(
                        "jmix.email.oauth2.enabled=true",
                        "jmix.email.oauth2.provider=google",
                        "jmix.email.oauth2.client-id=test-client",
                        "jmix.email.oauth2.secret=test-secret")
                .run(context -> {
                    assertThat(context).hasFailed();
                    Throwable cause = NestedExceptionUtils.getMostSpecificCause(context.getStartupFailure());
                    assertThat(cause).hasMessageContaining("spring.mail.username");
                });
    }

    @Test
    void testCustomTokenProviderIsRespected() {
        contextRunner
                .withUserConfiguration(CustomTokenProviderConfiguration.class)
                .withPropertyValues(
                        "jmix.email.oauth2.enabled=true",
                        "spring.mail.username=mailbox@example.com")
                .run(context -> {
                    assertThat(context).hasBean("email_JavaMailSender");
                    assertThat(context.getBean(OAuth2TokenProvider.class))
                            .isInstanceOf(TestCustomTokenProvider.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({MailProperties.class, EmailerProperties.class})
    static class TestBaseConfiguration {

        @Bean
        EmailRefreshTokenManager emailRefreshTokenManager() {
            return new EmailRefreshTokenManager() {
                private String value;

                @Override
                public RefreshToken storeRefreshTokenValue(String refreshTokenValue) {
                    value = refreshTokenValue;
                    RefreshToken token = new RefreshToken();
                    token.setTokenValue(refreshTokenValue);
                    return token;
                }

                @Override
                public String getRefreshTokenValue() {
                    return value != null ? value : "test-refresh-token";
                }

                @Override
                @Nullable
                public RefreshToken loadRefreshToken() {
                    return null;
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomTokenProviderConfiguration {

        @Bean
        @ConditionalOnMissingBean(OAuth2TokenProvider.class)
        OAuth2TokenProvider customTokenProvider() {
            return new TestCustomTokenProvider();
        }
    }

    static class TestCustomTokenProvider implements OAuth2TokenProvider {

        @Override
        public String getAccessToken() {
            return "custom-access-token";
        }

        @Override
        public String getRefreshToken() {
            return "custom-refresh-token";
        }
    }
}
