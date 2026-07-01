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

package io.jmix.saml.user;

import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

/**
 * Interface to be implemented by classes that represent application user and that wrap the {@link Saml2AuthenticatedPrincipal}.
 * Such classes delegate some method invocations to the wrapped {@code Saml2AuthenticatedPrincipal}.
 */
public interface HasSamlPrincipalDelegate {

    Saml2AuthenticatedPrincipal getDelegate();

    void setDelegate(Saml2AuthenticatedPrincipal delegate);
}
