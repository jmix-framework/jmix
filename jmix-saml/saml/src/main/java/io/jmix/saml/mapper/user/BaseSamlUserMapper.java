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

import io.jmix.saml.user.JmixSamlUserDetails;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class BaseSamlUserMapper<T extends JmixSamlUserDetails> implements SamlUserMapper<T> {

    private static final Logger log = getLogger(BaseSamlUserMapper.class);

    @Override
    public T toJmixUser(Assertion assertion, OpenSaml4AuthenticationProvider.ResponseToken responseToken) {
        log.info("[IVGA][SAML] Start toJmixUser");
        synchronized (getSamlUsername(assertion)) {
            T jmixUser = initJmixUser(assertion);
            log.debug("User '{}' is initialized", getSamlUsername(assertion));
            populateUserAttributes(assertion, responseToken, jmixUser);
            log.debug("User attributes is populated");
            populateUserAuthorities(assertion, jmixUser);
            log.debug("User authorities is populated");
            performAdditionalModifications(assertion, jmixUser);
            log.debug("Additional modifications are performed");
            return jmixUser;
        }
    }

    protected abstract String getSamlUsername(Assertion assertion);

    protected abstract T initJmixUser(Assertion assertion);

    protected abstract void populateUserAttributes(Assertion assertion, OpenSaml4AuthenticationProvider.ResponseToken responseToken, T jmixUser);

    protected abstract void populateUserAuthorities(Assertion assertion, T jmixUser);

    protected void performAdditionalModifications(Assertion assertion, T jmixUser) {
    }
}
