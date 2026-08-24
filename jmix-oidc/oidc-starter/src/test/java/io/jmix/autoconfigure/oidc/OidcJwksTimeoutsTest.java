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

package io.jmix.autoconfigure.oidc;

import io.jmix.oidc.usermapper.OidcUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.JwkSetUriJwtDecoderBuilderCustomizer;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.core.ResolvableType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Since Spring Security 7 the default JWK Set fetching applies Nimbus timeouts of 500 ms, which breaks
 * OIDC login and bearer token validation with identity providers that respond slower (see
 * <a href="https://github.com/spring-projects/spring-security/issues/14269">spring-security#14269</a>).
 * The add-on must fetch the JWK Set with timeouts configurable by {@code jmix.oidc.jwks.*} properties.
 */
class OidcJwksTimeoutsTest {

    /**
     * Exceeds the 500 ms Spring Security default, stays well below the add-on defaults.
     */
    private static final Duration SLOW_RESPONSE_DELAY = Duration.ofSeconds(1);

    @Test
    void testIdTokenDecoderFactoryToleratesSlowJwksEndpoint() {
        try (TestJwksServer server = TestJwksServer.start(SLOW_RESPONSE_DELAY)) {
            contextRunner().run(context -> {
                JwtDecoder decoder = idTokenDecoderFactory(context).createDecoder(clientRegistration(server));
                Jwt idToken = decoder.decode(server.signIdToken("test-client"));
                assertThat(idToken.getSubject()).isEqualTo("user1");
            });
        }
    }

    @Test
    void testIdTokenDecoderFactoryHonorsReadTimeoutProperty() {
        try (TestJwksServer server = TestJwksServer.start(Duration.ofMillis(600))) {
            contextRunner()
                    .withPropertyValues("jmix.oidc.jwks.read-timeout=100ms")
                    .run(context -> {
                        JwtDecoder decoder = idTokenDecoderFactory(context).createDecoder(clientRegistration(server));
                        assertThatThrownBy(() -> decoder.decode(server.signIdToken("test-client")))
                                .isInstanceOf(JwtException.class)
                                .hasStackTraceContaining("Read timed out");
                    });
        }
    }

    @Test
    void testIdTokenDecoderFactoryReusesDecoderPerRegistration() {
        try (TestJwksServer server = TestJwksServer.start(Duration.ZERO)) {
            contextRunner().run(context -> {
                JwtDecoderFactory<ClientRegistration> factory = idTokenDecoderFactory(context);
                ClientRegistration registration = clientRegistration(server);
                JwtDecoder decoder = factory.createDecoder(registration);
                assertThat(factory.createDecoder(registration)).isSameAs(decoder);

                decoder.decode(server.signIdToken("test-client"));
                decoder.decode(server.signIdToken("test-client"));
                assertThat(server.getRequestCount()).isEqualTo(1);
            });
        }
    }

    @Test
    void testIdTokenDecoderFactoryBacksOffWhenApplicationDefinesOne() {
        JwtDecoderFactory<ClientRegistration> applicationFactory = registration -> mock(JwtDecoder.class);
        contextRunner()
                .withBean("applicationIdTokenDecoderFactory", JwtDecoderFactory.class, () -> applicationFactory)
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtDecoderFactory.class);
                    assertThat(context.getBean(JwtDecoderFactory.class)).isSameAs(applicationFactory);
                });
    }

    @Test
    void testResourceServerJwtDecoderToleratesSlowJwksEndpoint() {
        try (TestJwksServer server = TestJwksServer.start(SLOW_RESPONSE_DELAY)) {
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(
                            OAuth2ResourceServerAutoConfiguration.class, OidcAutoConfiguration.class))
                    .withPropertyValues(
                            "jmix.oidc.use-default-jwt-configuration=false",
                            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=" + server.getJwkSetUri())
                    .withBean(OidcUserMapper.class, () -> mock(OidcUserMapper.class))
                    // Suppresses the default OAuth2 security filter chain, which needs the web security
                    // infrastructure. Only the JwtDecoder auto-configuration is under test here.
                    .withBean(SecurityFilterChain.class, () -> mock(SecurityFilterChain.class))
                    .run(context -> {
                        JwtDecoder decoder = context.getBean(JwtDecoder.class);
                        Jwt jwt = decoder.decode(server.signAccessToken());
                        assertThat(jwt.getSubject()).isEqualTo("user1");
                    });
        }
    }

    @Test
    void testApplicationCustomizerOverridesJmixResourceServerCustomizer() {
        // The Jmix customizer is ordered with JmixOrder.HIGHEST_PRECEDENCE, so an application-defined
        // customizer runs after it and gets the last word on the shared decoder builder.
        try (TestJwksServer server = TestJwksServer.start(Duration.ofMillis(600))) {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setReadTimeout(Duration.ofMillis(100));
            RestTemplate applicationRestOperations = new RestTemplate(requestFactory);
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(
                            OAuth2ResourceServerAutoConfiguration.class, OidcAutoConfiguration.class))
                    .withPropertyValues(
                            "jmix.oidc.use-default-jwt-configuration=false",
                            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=" + server.getJwkSetUri())
                    .withBean(OidcUserMapper.class, () -> mock(OidcUserMapper.class))
                    .withBean(SecurityFilterChain.class, () -> mock(SecurityFilterChain.class))
                    .withBean("applicationJwkSetUriJwtDecoderBuilderCustomizer", JwkSetUriJwtDecoderBuilderCustomizer.class,
                            () -> builder -> builder.restOperations(applicationRestOperations))
                    .run(context -> {
                        JwtDecoder decoder = context.getBean(JwtDecoder.class);
                        assertThatThrownBy(() -> decoder.decode(server.signAccessToken()))
                                .isInstanceOf(JwtException.class)
                                .hasStackTraceContaining("Read timed out");
                    });
        }
    }

    private WebApplicationContextRunner contextRunner() {
        return new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(OidcAutoConfiguration.class))
                .withPropertyValues("jmix.oidc.use-default-jwt-configuration=false")
                .withBean(OidcUserMapper.class, () -> mock(OidcUserMapper.class));
    }

    @SuppressWarnings("unchecked")
    private JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory(AssertableWebApplicationContext context) {
        // Resolves the bean the same way OAuth2LoginConfigurer does: by JwtDecoderFactory<ClientRegistration> type.
        return (JwtDecoderFactory<ClientRegistration>) context
                .getBeanProvider(ResolvableType.forClassWithGenerics(JwtDecoderFactory.class, ClientRegistration.class))
                .getObject();
    }

    private ClientRegistration clientRegistration(TestJwksServer server) {
        return ClientRegistration.withRegistrationId("test")
                .clientId("test-client")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/test")
                .authorizationUri("https://provider.example.com/auth")
                .tokenUri("https://provider.example.com/token")
                .jwkSetUri(server.getJwkSetUri())
                .build();
    }
}
