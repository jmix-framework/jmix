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

package io.jmix.saml.util;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnStatementBuilder;
import org.springframework.security.saml2.core.OpenSamlInitializationService;

import static org.assertj.core.api.Assertions.assertThat;

class SamlAssertionUtilsTest {

    @BeforeAll
    static void initOpenSaml() {
        OpenSamlInitializationService.initialize();
    }

    @Test
    void testGetSessionIndexesReturnsIndexesFromAllAuthnStatements() {
        // Session indexes identify the IdP session and must survive the mapping so that the LogoutRequest
        // sent during single logout contains them.
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.getAuthnStatements().add(authnStatement("index-1"));
        assertion.getAuthnStatements().add(authnStatement(null));
        assertion.getAuthnStatements().add(authnStatement("index-2"));

        assertThat(SamlAssertionUtils.getSessionIndexes(assertion)).containsExactly("index-1", "index-2");
    }

    @Test
    void testGetSessionIndexesReturnsEmptyListWhenNoAuthnStatements() {
        Assertion assertion = new AssertionBuilder().buildObject();

        assertThat(SamlAssertionUtils.getSessionIndexes(assertion)).isEmpty();
    }

    private AuthnStatement authnStatement(@Nullable String sessionIndex) {
        AuthnStatement statement = new AuthnStatementBuilder().buildObject();
        statement.setSessionIndex(sessionIndex);
        return statement;
    }
}
