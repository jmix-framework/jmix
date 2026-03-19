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

import io.jmix.saml.mapper.role.SamlAssertionRolesMapper;
import io.jmix.saml.user.DefaultJmixSamlUserDetails;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;

import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Default implementation of {@link SamlUserMapper} that converts SAML assertion into {@link DefaultJmixSamlUserDetails}.
 */
public class DefaultSamlUserMapper extends BaseSamlUserMapper<DefaultJmixSamlUserDetails> {

    private static final Logger log = getLogger(DefaultSamlUserMapper.class);

    protected final SamlAssertionRolesMapper rolesMapper;

    public DefaultSamlUserMapper(SamlAssertionRolesMapper rolesMapper) {
        this.rolesMapper = rolesMapper;
    }

    @Override
    protected DefaultJmixSamlUserDetails initJmixUser(Assertion assertion) {
        return new DefaultJmixSamlUserDetails();
    }

    @Override
    protected void populateUserAttributes(Assertion assertion,
                                          OpenSaml4AuthenticationProvider.ResponseToken responseToken,
                                          DefaultJmixSamlUserDetails jmixUser) {
    }

    @Override
    protected void populateUserAuthorities(Assertion assertion, DefaultJmixSamlUserDetails jmixUser) {
        Collection<? extends GrantedAuthority> grantedAuthorities = rolesMapper.toGrantedAuthorities(assertion);
        jmixUser.setAuthorities(grantedAuthorities);
    }
}
