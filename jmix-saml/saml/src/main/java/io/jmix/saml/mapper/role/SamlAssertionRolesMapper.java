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

package io.jmix.saml.mapper.role;

import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface SamlAssertionRolesMapper {

    Collection<ResourceRole> toResourceRoles(Assertion assertion);

    Collection<RowLevelRole> toRowLevelRoles(Assertion assertion);

    Collection<? extends GrantedAuthority> toGrantedAuthorities(Assertion assertion);
}
