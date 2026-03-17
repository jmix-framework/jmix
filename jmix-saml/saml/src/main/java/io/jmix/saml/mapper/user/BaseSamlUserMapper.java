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

import com.google.common.util.concurrent.Striped;
import io.jmix.saml.SamlProperties;
import io.jmix.saml.user.HasSamlPrincipalDelegate;
import io.jmix.saml.user.JmixSamlUserDetails;
import io.jmix.saml.util.SamlAssertionUtils;
import jakarta.annotation.PostConstruct;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class BaseSamlUserMapper<T extends JmixSamlUserDetails> implements SamlUserMapper<T> {

    private static final Logger log = getLogger(BaseSamlUserMapper.class);

    @Autowired
    protected SamlProperties samlProperties;

    protected Striped<Lock> locks;

    @PostConstruct
    protected void initLocks() {
        this.locks = Striped.lock(samlProperties.getMaxConcurrentUserMapping());
    }

    @Override
    public T toJmixUser(Assertion assertion, OpenSaml4AuthenticationProvider.ResponseToken responseToken) {
        String username = getSamlUsername(assertion);
        Lock lock = locks.get(username);
        lock.lock();
        try {
            T jmixUser = initJmixUser(assertion);
            log.debug("User '{}' is initialized", username);
            populateUserAttributes(assertion, responseToken, jmixUser);
            log.debug("User attributes is populated");
            populateUserAuthorities(assertion, jmixUser);
            log.debug("User authorities is populated");
            performAdditionalModifications(assertion, responseToken, jmixUser);
            log.debug("Additional modifications are performed");
            return jmixUser;
        } finally {
            lock.unlock();
        }
    }

    protected String getSamlUsername(Assertion assertion) {
        String username = SamlAssertionUtils.getUsername(assertion);
        if (username == null) {
            throw new IllegalStateException("SAML assertion doesn't contain username");
        }
        return username;
    }

    protected abstract T initJmixUser(Assertion assertion);

    protected abstract void populateUserAttributes(Assertion assertion, OpenSaml4AuthenticationProvider.ResponseToken responseToken, T jmixUser);

    protected abstract void populateUserAuthorities(Assertion assertion, T jmixUser);

    protected void performAdditionalModifications(Assertion assertion, OpenSaml4AuthenticationProvider.ResponseToken responseToken, T jmixUser) {
        if (jmixUser instanceof HasSamlPrincipalDelegate) {
            String username = getSamlUsername(assertion);
            Map<String, List<Object>> attributes = SamlAssertionUtils.getAssertionAttributes(assertion);
            DefaultSaml2AuthenticatedPrincipal delegatePrincipal = new DefaultSaml2AuthenticatedPrincipal(username, attributes);
            String registrationId = responseToken.getToken().getRelyingPartyRegistration().getRegistrationId();
            delegatePrincipal.setRelyingPartyRegistrationId(registrationId);
            ((HasSamlPrincipalDelegate) jmixUser).setDelegate(delegatePrincipal);
        }
    }
}
