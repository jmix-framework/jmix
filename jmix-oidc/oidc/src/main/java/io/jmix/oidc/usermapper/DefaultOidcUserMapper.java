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

package io.jmix.oidc.usermapper;

import io.jmix.oidc.claimsmapper.ClaimsRolesMapper;
import io.jmix.oidc.user.DefaultJmixOidcUser;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;

/**
 * The default implementation {@link OidcUserMapper} implementation that converts {@link OidcUser} into {@link
 * DefaultJmixOidcUser}.
 */
@NullMarked
public class DefaultOidcUserMapper extends BaseOidcUserMapper<DefaultJmixOidcUser> {

    protected ClaimsRolesMapper claimsRolesMapper;

    public DefaultOidcUserMapper(ClaimsRolesMapper claimsRolesMapper) {
        this.claimsRolesMapper = claimsRolesMapper;
    }

    @Override
    protected String getOidcUserUsername(OidcUser oidcUser) {
        return oidcUser.getName();
    }

    protected DefaultJmixOidcUser initJmixUser(OidcUser oidcUser) {
        return new DefaultJmixOidcUser();
    }

    @Override
    protected void populateUserAuthorities(OidcUser oidcUser, DefaultJmixOidcUser jmixUser) {
        Collection<? extends GrantedAuthority> grantedAuthorities = claimsRolesMapper.toGrantedAuthorities(oidcUser.getClaims());
        jmixUser.setAuthorities(grantedAuthorities);
    }

    @Override
    protected void populateUserAttributes(OidcUser oidcUser, DefaultJmixOidcUser jmixUser) {
    }
}
