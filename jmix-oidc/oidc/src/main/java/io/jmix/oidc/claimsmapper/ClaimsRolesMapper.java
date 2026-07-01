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

package io.jmix.oidc.claimsmapper;

import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

/**
 * Mapper of claims received from the OpenID Provider into Jmix resource roles and row-level roles. Some {@link io.jmix.oidc.usermapper.OidcUserMapper}
 * implementations delegate roles mapping to the instance of this interface. If you want to replace standard claims mapper
 * with your own one, then register a spring bean implementing the current interface in your application.
 *
 * @see io.jmix.oidc.usermapper.OidcUserMapper
 */
@NullMarked
public interface ClaimsRolesMapper {

    Collection<ResourceRole> toResourceRoles(Map<String, Object> claims);

    Collection<RowLevelRole> toRowLevelRoles(Map<String, Object> claims);

    Collection<? extends GrantedAuthority> toGrantedAuthorities(Map<String, Object> claims);
}
