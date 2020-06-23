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
import java.util.HashMap;
import java.util.Map;

/**
 * Class is a container for security policies.
 * <p>
 * Resource policies define permissions for resources. A resource may be a screen, entity CRUD operation, entity
 * attribute, etc.
 * <p>
 * Row-level policy restrict which data a user can read or modify.
 * <p>
 * Role objects may be created from different sources:
 * <ul>
 *     <li>from interfaces annotated with {@link io.jmix.security.role.annotation.Role}</li>
 *     <li>from database Role entities</li>
 *     <li>created explicitly by the application</li>
 * </ul>
 */
public class Role {

    private String name;
    private String code;
    private String source;
    private Collection<ResourcePolicy> resourcePolicies = new ArrayList<>();
    private Collection<RowLevelPolicy> rowLevelPolicies = new ArrayList<>();
    private Map<String, String> customProperties = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Collection<ResourcePolicy> getResourcePolicies() {
        return resourcePolicies;
    }

    public void setResourcePolicies(Collection<ResourcePolicy> resourcePolicies) {
        this.resourcePolicies = resourcePolicies;
    }

    public Collection<RowLevelPolicy> getRowLevelPolicies() {
        return rowLevelPolicies;
    }

    public void setRowLevelPolicies(Collection<RowLevelPolicy> rowLevelPolicies) {
        this.rowLevelPolicies = rowLevelPolicies;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }
}
