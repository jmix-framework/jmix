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
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.Role;
import io.jmix.security.model.RowLevelPolicy;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class RoleGrantedAuthority implements SecuredGrantedAuthority {
    private static final long serialVersionUID = 2024837359721996022L;

    protected final String code;
    protected final Collection<ResourcePolicy> resourcePolicies;
    protected final Collection<RowLevelPolicy> rowLevelPolicies;
    protected Map<Class<?>, ResourcePolicyIndex> resourceIndexes = new HashMap<>();
    protected Map<Class<?>, RowLevelPolicyIndex> rowLevelIndexes = new HashMap<>();

    public RoleGrantedAuthority(Role role) {
        this.code = role.getCode();
        this.resourcePolicies = Collections.unmodifiableCollection(new ArrayList<>(role.getResourcePolicies()));
        this.rowLevelPolicies = Collections.unmodifiableCollection(new ArrayList<>(role.getRowLevelPolicies()));
    }

    @Override
    public Collection<ResourcePolicy> getResourcePolicies() {
        return resourcePolicies;
    }

    @Override
    public Collection<RowLevelPolicy> getRowLevelPolicies() {
        return rowLevelPolicies;
    }

    @Override
    public <I extends ResourcePolicyIndex> Stream<ResourcePolicy> getResourcePoliciesByIndex(Class<I> indexClass,
                                                                                             Function<I, Stream<ResourcePolicy>> extractor) {
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
        Stream<ResourcePolicy> result = extractor.apply((I) index);
        return result == null ? Stream.empty() : result;
    }

    @Override
    public <I extends RowLevelPolicyIndex> Stream<RowLevelPolicy> getRowLevelPoliciesByIndex(Class<I> indexClass, Function<I, Stream<RowLevelPolicy>> extractor) {
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
        Stream<RowLevelPolicy> result = extractor.apply((I) index);
        return result == null ? Stream.empty() : result;
    }

    @Override
    public String getAuthority() {
        return code;
    }
}
