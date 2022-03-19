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
import io.jmix.security.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

public class RoleGrantedAuthority implements PolicyAwareGrantedAuthority {
    private static final long serialVersionUID = 2024837359721996022L;

    protected final String code;
    protected final Collection<String> scopes;
    protected final Collection<ResourcePolicy> resourcePolicies;
    protected final Collection<RowLevelPolicy> rowLevelPolicies;
    protected Map<Class<?>, ResourcePolicyIndex> resourceIndexes = new ConcurrentHashMap<>();
    protected Map<Class<?>, RowLevelPolicyIndex> rowLevelIndexes = new ConcurrentHashMap<>();

    public static class Builder {
        protected Function<String, ResourceRole> resourceRoleProvider;
        protected Function<String, RowLevelRole> rowLevelRoleProvider;
        protected List<String> resourceRoleCodes = new ArrayList<>();
        protected List<String> rowLevelRoleCodes = new ArrayList<>();

        private Builder() {
        }

        public Builder withResourceRoleProvider(Function<String, ResourceRole> resourceRoleProvider) {
            this.resourceRoleProvider = resourceRoleProvider;
            return this;
        }

        public Builder withRowLevelRoleProvider(Function<String, RowLevelRole> rowLevelRoleProvider) {
            this.rowLevelRoleProvider = rowLevelRoleProvider;
            return this;
        }

        public Builder withResourceRoles(String... codes) {
            resourceRoleCodes.addAll(Arrays.asList(codes));
            return this;
        }

        public Builder withRowLevelRoles(String... codes) {
            rowLevelRoleCodes.addAll(Arrays.asList(codes));
            return this;
        }

        public Collection<RoleGrantedAuthority> build() {
            List<RoleGrantedAuthority> authorities = new ArrayList<>();
            for (String code : resourceRoleCodes) {
                authorities.add(ofResourceRole(resourceRoleProvider.apply(code)));
            }
            for (String code : rowLevelRoleCodes) {
                authorities.add(ofRowLevelRole(rowLevelRoleProvider.apply(code)));
            }
            return authorities;
        }
    }

    public static RoleGrantedAuthority ofResourceRole(ResourceRole role) {
        return new RoleGrantedAuthority(role);
    }

    public static RoleGrantedAuthority ofRowLevelRole(RowLevelRole role) {
        return new RoleGrantedAuthority(role);
    }

    public static Builder withResourceRoleProvider(Function<String, ResourceRole> roleProvider) {
        Builder builder = new Builder();
        return builder.withResourceRoleProvider(roleProvider);
    }

    public static Builder withRowLevelRoleProvider(Function<String, RowLevelRole> roleProvider) {
        Builder builder = new Builder();
        return builder.withRowLevelRoleProvider(roleProvider);
    }

    private RoleGrantedAuthority(ResourceRole role) {
        this.code = role.getCode();
        this.scopes = Collections.unmodifiableCollection(role.getScopes());
        this.resourcePolicies = Collections.unmodifiableCollection(new ArrayList<>(role.getAllResourcePolicies()));
        this.rowLevelPolicies = Collections.emptyList();
    }

    private RoleGrantedAuthority(RowLevelRole role) {
        this.code = String.format("row_level_role:%s", role.getCode());
        this.scopes = Collections.emptyList();
        this.resourcePolicies = Collections.emptyList();
        this.rowLevelPolicies = Collections.unmodifiableCollection(new ArrayList<>(role.getAllRowLevelPolicies()));
    }

    @Override
    public Collection<String> getScopes() {
        return scopes;
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
