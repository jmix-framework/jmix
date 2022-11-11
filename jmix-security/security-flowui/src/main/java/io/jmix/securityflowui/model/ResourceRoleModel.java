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

package io.jmix.securityflowui.model;

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.util.Collection;
import java.util.Set;

/**
 * Non-persistent entity used to display resource roles in UI
 */
@JmixEntity(name = "sec_ResourceRoleModel")
public class ResourceRoleModel extends BaseRoleModel {

    @Composition
    @JmixProperty
    private Collection<ResourcePolicyModel> resourcePolicies;

    @JmixProperty
    private Set<String> scopes;

    public Collection<ResourcePolicyModel> getResourcePolicies() {
        return resourcePolicies;
    }

    public void setResourcePolicies(Collection<ResourcePolicyModel> resourcePolicies) {
        this.resourcePolicies = resourcePolicies;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }
}
