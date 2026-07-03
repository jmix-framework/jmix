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

package io.jmix.saml.mapper.user;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.jmix.saml.SamlProperties;
import io.jmix.saml.mapper.role.SamlAssertionRolesMapper;
import io.jmix.saml.user.DefaultJmixSamlUserDetails;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.core.OpenSamlInitializationService;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaseSamlUserMapperTest {

    @BeforeAll
    static void initOpenSaml() {
        OpenSamlInitializationService.initialize();
    }

    @Test
    void testDelegatePrincipalCarriesSessionIndexesAndRegistrationId() {
        // The LogoutRequest resolver reads session indexes and the registration id from the authenticated
        // principal, so the mapper must propagate them from the assertion into the delegate.
        DefaultSamlUserMapper mapper = createMapper();
        Assertion assertion = assertion("john", "index-1");

        DefaultJmixSamlUserDetails user = mapper.toJmixUser(assertion, responseToken("okta"));

        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.getSessionIndexes()).containsExactly("index-1");
        assertThat(user.getRelyingPartyRegistrationId()).isEqualTo("okta");
    }

    @Test
    void testSessionIndexesAreEmptyWhenAssertionContainsNone() {
        DefaultSamlUserMapper mapper = createMapper();
        Assertion assertion = assertion("john", null);

        DefaultJmixSamlUserDetails user = mapper.toJmixUser(assertion, responseToken("okta"));

        assertThat(user.getSessionIndexes()).isEmpty();
    }

    @Test
    void testMissingNameIdFailsWithDiagnosableError() {
        // An assertion without a plain subject NameID (e.g. only an EncryptedID) must produce an explicit
        // error instead of NPE.
        DefaultSamlUserMapper mapper = createMapper();
        Assertion assertion = new AssertionBuilder().buildObject();

        assertThatThrownBy(() -> mapper.toJmixUser(assertion, responseToken("okta")))
                .isInstanceOf(Saml2Exception.class)
                .hasMessageContaining("NameID");
    }

    @Test
    void testUsernameIsTakenFromConfiguredAssertionAttribute() {
        // With 'jmix.saml.username-attribute' set, a stable attribute identifies the user even if the
        // NameID is transient.
        DefaultSamlUserMapper mapper = createMapper("mail");
        Assertion assertion = assertion("opaque-per-session-id", "index-1");
        assertion.getAttributeStatements().add(attributeStatement("mail", "john@example.com"));

        DefaultJmixSamlUserDetails user = mapper.toJmixUser(assertion, responseToken("okta"));

        assertThat(user.getUsername()).isEqualTo("john@example.com");
    }

    @Test
    void testMissingConfiguredUsernameAttributeFailsWithDiagnosableError() {
        DefaultSamlUserMapper mapper = createMapper("mail");
        Assertion assertion = assertion("john", null);

        assertThatThrownBy(() -> mapper.toJmixUser(assertion, responseToken("okta")))
                .isInstanceOf(Saml2Exception.class)
                .hasMessageContaining("jmix.saml.username-attribute");
    }

    @Test
    void testTransientNameIdFormatProducesWarning() {
        Logger logger = (Logger) LoggerFactory.getLogger(BaseSamlUserMapper.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            DefaultSamlUserMapper mapper = createMapper();
            Assertion assertion = assertion("opaque-per-session-id", null);
            assertion.getSubject().getNameID().setFormat(NameIDType.TRANSIENT);

            mapper.toJmixUser(assertion, responseToken("okta"));

            assertThat(appender.list).anySatisfy(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.WARN);
                assertThat(event.getFormattedMessage()).contains("transient");
            });
        } finally {
            logger.detachAppender(appender);
        }
    }

    private DefaultSamlUserMapper createMapper() {
        return createMapper(null);
    }

    private DefaultSamlUserMapper createMapper(@Nullable String usernameAttribute) {
        DefaultSamlUserMapper mapper = new DefaultSamlUserMapper();
        mapper.samlProperties = new SamlProperties(true, 128, "/", true, usernameAttribute,
                new SamlProperties.DefaultSamlAssertionRolesMapperConfig("Role", "", ""),
                new SamlProperties.FilterChain(true, null));
        SamlAssertionRolesMapper rolesMapper = mock(SamlAssertionRolesMapper.class);
        when(rolesMapper.toGrantedAuthorities(any())).thenReturn(List.of());
        mapper.rolesMapper = rolesMapper;
        mapper.initLocks();
        return mapper;
    }

    private AttributeStatement attributeStatement(String name, String value) {
        XSString attributeValue = new XSStringBuilder()
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attributeValue.setValue(value);
        Attribute attribute = new AttributeBuilder().buildObject();
        attribute.setName(name);
        attribute.getAttributeValues().add(attributeValue);
        AttributeStatement statement = new AttributeStatementBuilder().buildObject();
        statement.getAttributes().add(attribute);
        return statement;
    }

    private Assertion assertion(String username, String sessionIndex) {
        Assertion assertion = new AssertionBuilder().buildObject();

        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setValue(username);
        Subject subject = new SubjectBuilder().buildObject();
        subject.setNameID(nameId);
        assertion.setSubject(subject);

        if (sessionIndex != null) {
            AuthnStatement authnStatement = new AuthnStatementBuilder().buildObject();
            authnStatement.setSessionIndex(sessionIndex);
            assertion.getAuthnStatements().add(authnStatement);
        }
        return assertion;
    }

    private OpenSaml5AuthenticationProvider.ResponseToken responseToken(String registrationId) {
        RelyingPartyRegistration registration = RelyingPartyRegistration.withRegistrationId(registrationId)
                .entityId("{baseUrl}/saml2/service-provider-metadata/{registrationId}")
                .assertionConsumerServiceLocation("{baseUrl}/login/saml2/sso/{registrationId}")
                .assertingPartyMetadata(party -> party
                        .entityId("https://idp.example.com/" + registrationId)
                        .singleSignOnServiceLocation("https://idp.example.com/sso/" + registrationId))
                .build();
        Saml2AuthenticationToken token = new Saml2AuthenticationToken(registration, "<Response/>");

        OpenSaml5AuthenticationProvider.ResponseToken responseToken =
                mock(OpenSaml5AuthenticationProvider.ResponseToken.class);
        when(responseToken.getToken()).thenReturn(token);
        return responseToken;
    }
}
