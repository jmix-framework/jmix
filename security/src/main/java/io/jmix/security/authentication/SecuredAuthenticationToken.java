/*
 * Copyright 2020 Haulmont.
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

package io.jmix.security.authentication;

import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.authentication.CoreAuthenticationToken;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.RowLevelPolicy;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SecuredAuthenticationToken extends CoreAuthenticationToken implements SecuredAuthentication {

    protected Collection<ResourcePolicy> resourcePolicies;
    protected Collection<RowLevelPolicy> rowLevelPolicies;

    public SecuredAuthenticationToken(BaseUser user, Collection<? extends GrantedAuthority> authorities) {
        super(user, authorities);
    }

    @Override
    public Collection<ResourcePolicy> getResourcePolicies() {
        return resourcePolicies;
    }

    public void setResourcePolicies(Collection<ResourcePolicy> resourcePolicies) {
        this.resourcePolicies = resourcePolicies;
    }

    @Override
    public Collection<RowLevelPolicy> getRowLevelPolicies() {
        return rowLevelPolicies;
    }

    public void setRowLevelPolicies(Collection<RowLevelPolicy> rowLevelPolicies) {
        this.rowLevelPolicies = rowLevelPolicies;
    }
}
