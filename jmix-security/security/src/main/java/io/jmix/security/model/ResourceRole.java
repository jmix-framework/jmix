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

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    private ResourcePoliciesIndex resourcePoliciesIndex;

    private ResourcePoliciesIndex allResourcePoliciesIndex;

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    /**
     * Returns a list of policies defined directly in the current role, excluding policies from child roles.
     */
    public Collection<ResourcePolicy> getResourcePolicies() {
        return resourcePolicies;
    }

    public void setResourcePolicies(Collection<ResourcePolicy> resourcePolicies) {
        this.resourcePolicies = resourcePolicies;
        this.resourcePoliciesIndex = new ResourcePoliciesIndex(resourcePolicies);
    }

    /**
     * Returns policies defined in the current role and in all its child roles.
     */
    public Collection<ResourcePolicy> getAllResourcePolicies() {
        return allResourcePolicies == null ? resourcePolicies : allResourcePolicies;
    }

    public void setAllResourcePolicies(Collection<ResourcePolicy> allResourcePolicies) {
        this.allResourcePolicies = allResourcePolicies;
        this.allResourcePoliciesIndex = new ResourcePoliciesIndex(allResourcePolicies);
    }

    /**
     * Returns an index structure that stores policies of the current role only.
     */
    public ResourcePoliciesIndex getResourcePoliciesIndex() {
        return resourcePoliciesIndex;
    }

    /**
     * Returns an index structure that stores policies of the current role and all child roles.
     */
    public ResourcePoliciesIndex getAllResourcePoliciesIndex() {
        return allResourcePoliciesIndex == null ? resourcePoliciesIndex : allResourcePoliciesIndex;
    }

    /**
     * A data structure that stores role policies grouped by policy type and resource. It simplifies operations like
     * extracting policies for entity access for a sec_User entity.
     */
    public static class ResourcePoliciesIndex implements Serializable {

        /**
         * The key of external map is policy type. The value is a map of resources to policies.
         */
        private final Map<String, Map<String, List<ResourcePolicy>>> policiesByTypeAndResource = new ConcurrentHashMap<>();

        ResourcePoliciesIndex(Collection<ResourcePolicy> policies) {
            Map<String, List<ResourcePolicy>> policiesByType = policies.stream()
                    .collect(Collectors.groupingBy(ResourcePolicy::getType));
            for (Map.Entry<String, List<ResourcePolicy>> entry : policiesByType.entrySet()) {
                Map<String, List<ResourcePolicy>> policiesByResource = entry.getValue().stream()
                        .collect(Collectors.groupingBy(ResourcePolicy::getResource));
                String policyType = entry.getKey();
                policiesByTypeAndResource.put(policyType, policiesByResource);
            }
        }

        /**
         * Returns a list of policies of the specified type and resource.
         */
        public List<ResourcePolicy> getPoliciesByTypeAndResource(String policyType, String resource) {
            Map<String, List<ResourcePolicy>> policiesWithType = policiesByTypeAndResource.getOrDefault(policyType, new HashMap<>());
            return policiesWithType.getOrDefault(resource, new ArrayList<>());
        }
    }
}
