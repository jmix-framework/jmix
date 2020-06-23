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

package io.jmix.securityui.model;

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import io.jmix.data.entity.BaseUuidEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Non-persistent entity used to display roles in UI
 */
@ModelObject(name = "sec_RoleModel")
public class RoleModel extends BaseUuidEntity {

    @ModelProperty(mandatory = true)
    protected String code;

    @ModelProperty(mandatory = true)
    protected String name;

    @ModelProperty
    private String source;

    @Composition
    @ModelProperty
    private Collection<ResourcePolicyModel> resourcePolicies;

    @Composition
    @ModelProperty
    private Collection<RowLevelPolicyModel> rowLevelPolicies;

    @ModelProperty
    private Map<String, String> customProperties = new HashMap<>();

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<ResourcePolicyModel> getResourcePolicies() {
        return resourcePolicies;
    }

    public void setResourcePolicies(Collection<ResourcePolicyModel> resourcePolicies) {
        this.resourcePolicies = resourcePolicies;
    }

    public Collection<RowLevelPolicyModel> getRowLevelPolicies() {
        return rowLevelPolicies;
    }

    public void setRowLevelPolicies(Collection<RowLevelPolicyModel> rowLevelPolicies) {
        this.rowLevelPolicies = rowLevelPolicies;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }
}
