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

import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.authentication.CoreAuthenticationToken;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.RowLevelPolicy;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SecuredAuthenticationToken extends CoreAuthenticationToken implements SecuredAuthentication {

    protected Collection<ResourcePolicy> resourcePolicies;
    protected Collection<RowLevelPolicy> rowLevelPolicies;

    protected Map<Class<?>, ResourcePolicyIndex> resourceIndexes;
    protected Map<Class<?>, RowLevelPolicyIndex> rowLevelIndexes;

    public SecuredAuthenticationToken(BaseUser user, Collection<? extends GrantedAuthority> authorities) {
        super(user, authorities);
    }

    @Override
    public Collection<ResourcePolicy> getResourcePolicies() {
        return resourcePolicies;
    }

    public void setResourcePolicies(Collection<ResourcePolicy> resourcePolicies) {
        this.resourcePolicies = resourcePolicies;
        this.resourceIndexes = null;
    }

    @Override
    public <I extends ResourcePolicyIndex> Collection<ResourcePolicy> getResourcePoliciesByIndex(
            Class<I> indexClass, Function<I, Collection<ResourcePolicy>> extractor) {
        if (resourceIndexes == null) {
            resourceIndexes = new HashMap<>();
        }

        ResourcePolicyIndex index = resourceIndexes.computeIfAbsent(indexClass, newIndexClass -> {
            try {
                ResourcePolicyIndex newIndex = (ResourcePolicyIndex) ReflectionHelper.newInstance(newIndexClass);
                newIndex.indexAll(resourcePolicies);
                return newIndex;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to indexing resource policies", e);
            }
        });

        //noinspection unchecked
        Collection<ResourcePolicy> result = extractor.apply((I) index);
        return result == null ? Collections.emptyList() : result;
    }

    @Override
    public Collection<RowLevelPolicy> getRowLevelPolicies() {
        return rowLevelPolicies;
    }

    public void setRowLevelPolicies(Collection<RowLevelPolicy> rowLevelPolicies) {
        this.rowLevelPolicies = rowLevelPolicies;
        this.rowLevelIndexes = null;
    }

    @Override
    public <I extends RowLevelPolicyIndex> Collection<RowLevelPolicy> getRowLevelPoliciesByIndex(
            Class<I> indexClass, Function<I, Collection<RowLevelPolicy>> extractor) {
        if (rowLevelIndexes == null) {
            rowLevelIndexes = new HashMap<>();
        }

        RowLevelPolicyIndex index = rowLevelIndexes.computeIfAbsent(indexClass, newIndexClass -> {
            try {
                RowLevelPolicyIndex newIndex = (RowLevelPolicyIndex) ReflectionHelper.newInstance(newIndexClass);
                newIndex.indexAll(rowLevelPolicies);
                return newIndex;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to indexing resource policies", e);
            }
        });

        //noinspection unchecked
        Collection<RowLevelPolicy> result = extractor.apply((I) index);
        return result == null ? Collections.emptyList() : result;
    }
}
