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

package io.jmix.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Class is a container for security resource policies.
 * <p>
 * Resource policies define permissions for resources. A resource may be a screen, entity CRUD operation, entity
 * attribute, etc.
 * <p>
 * Role objects may be created from different sources:
 * <ul>
 *     <li>from interfaces annotated with {@link io.jmix.security.role.annotation.ResourceRole}</li>
 *     <li>from database Role entities</li>
 *     <li>created explicitly by the application</li>
 * </ul>
 */
public class ResourceRole extends BaseRole {

    private Set<String> scopes;
    private Collection<ResourcePolicy> resourcePolicies = new ArrayList<>();
    private Collection<ResourcePolicy> allResourcePolicies;

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public Collection<ResourcePolicy> getResourcePolicies() {
        return resourcePolicies;
    }

    public void setResourcePolicies(Collection<ResourcePolicy> resourcePolicies) {
        this.resourcePolicies = resourcePolicies;
    }

    public Collection<ResourcePolicy> getAllResourcePolicies() {
        return allResourcePolicies == null ? resourcePolicies : allResourcePolicies;
    }

    public void setAllResourcePolicies(Collection<ResourcePolicy> allResourcePolicies) {
        this.allResourcePolicies = allResourcePolicies;
    }
}
