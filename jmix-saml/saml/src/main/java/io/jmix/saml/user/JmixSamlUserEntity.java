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

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import org.springframework.lang.Nullable;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import java.util.List;
import java.util.Map;

@JmixEntity
@MappedSuperclass
public abstract class JmixSamlUserEntity implements JmixSamlUserDetails, HasSamlPrincipalDelegate {

    @Transient
    private Saml2AuthenticatedPrincipal delegate;

    @Override
    public Saml2AuthenticatedPrincipal getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(Saml2AuthenticatedPrincipal delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    @Nullable
    public <A> A getFirstAttribute(String name) {
        return delegate.getFirstAttribute(name);
    }

    @Override
    @Nullable
    public <A> List<A> getAttribute(String name) {
        return delegate.getAttribute(name);
    }

    @Override
    public Map<String, List<Object>> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public String getRelyingPartyRegistrationId() {
        return delegate.getRelyingPartyRegistrationId();
    }

    @Override
    public List<String> getSessionIndexes() {
        return delegate.getSessionIndexes();
    }
}
