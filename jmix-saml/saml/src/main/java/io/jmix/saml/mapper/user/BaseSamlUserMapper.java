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
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Abstract class implements {@link SamlUserMapper} and may be used as super-class for your own {@link SamlUserMapper}.
 *
 * @param <T> class of Jmix user
 */
public abstract class BaseSamlUserMapper<T extends JmixSamlUserDetails> implements SamlUserMapper<T> {

    private static final Logger log = getLogger(BaseSamlUserMapper.class);

    @Autowired
    protected SamlProperties samlProperties;

    protected Striped<Lock> locks;

    @PostConstruct
    protected void initLocks() {
        this.locks = Striped.lock(samlProperties.getMaxConcurrentUserMapping());
    }

    /**
     * Returns an instance of Jmix user, which is enriched with additional data.
     *
     * @param assertion     SAML assertion
     * @param responseToken the object that stores information about the authentication response from SAML provider
     * @return Jmix user instance
     */
    @Override
    public T toJmixUser(Assertion assertion, OpenSaml5AuthenticationProvider.ResponseToken responseToken) {
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

    /**
     * Extracts username from SAML assertion.
     *
     * @param assertion SAML assertion
     * @return username
     */
    protected String getSamlUsername(Assertion assertion) {
        String username = SamlAssertionUtils.getUsername(assertion);
        if (username == null) {
            throw new IllegalStateException("SAML assertion doesn't contain username");
        }
        return username;
    }

    /**
     * Returns an instance of Jmix user, which may be either a new instance or an instance loaded from the user
     * repository. Attributes and authorities will lately be filled in other methods. The responsibility of the current
     * method is just to create or load an existing instance.
     *
     * @param assertion SAML assertion
     * @return new Jmix user instance or Jmix user loaded from user repository
     */
    protected abstract T initJmixUser(Assertion assertion);

    /**
     * Fills attributes of {@code jmixUser} based on information from the {@code assertion}
     *
     * @param assertion     SAML assertion
     * @param responseToken the object that stores information about the authentication response from SAML provider
     * @param jmixUser      Jmix user instance
     */
    protected abstract void populateUserAttributes(Assertion assertion, OpenSaml5AuthenticationProvider.ResponseToken responseToken, T jmixUser);

    /**
     * Fills authorities of {@code jmixUser} based on information from the {@code assertion}
     *
     * @param assertion SAML assertion
     * @param jmixUser  Jmix user instance
     */
    protected abstract void populateUserAuthorities(Assertion assertion, T jmixUser);

    /**
     * Performs additional modifications of Jmix user instance. Override this method in case you want to do some
     * additional attribute values computations or if you want to do some operations with Jmix user instance, e.g., to
     * store it in the database, like it is done in the {@link SynchronizingSamlUserMapper}
     *
     * @param assertion     SAML assertion
     * @param responseToken the object that stores information about the authentication response from SAML provider
     * @param jmixUser      Jmix user instance
     */
    protected void performAdditionalModifications(Assertion assertion, OpenSaml5AuthenticationProvider.ResponseToken responseToken, T jmixUser) {
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
