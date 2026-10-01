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

import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.security.role.RoleLocalizationSupport;

import jakarta.persistence.Id;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@JmixEntity(name = "sec_BaseRoleModel")
public abstract class BaseRoleModel {
    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty(mandatory = true)
    protected String code;

    @JmixProperty(mandatory = true)
    protected String name;

    @JmixProperty
    protected String description;

    @JmixProperty
    protected String nameMessageKey;

    @JmixProperty
    protected String descriptionMessageKey;

    @JmixProperty
    protected String localizedNames;

    @JmixProperty
    protected String localizedDescriptions;

    @JmixProperty
    private RoleSourceType source;

    @JmixProperty
    private Map<String, String> customProperties = new HashMap<>();

    @JmixProperty
    private Set<String> childRoles;

    @JmixProperty
    @TenantId
    private String tenantId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RoleSourceType getSource() {
        return source;
    }

    public void setSource(RoleSourceType source) {
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

    /**
     * @return the name in the current user's locale
     * @see RoleLocalizationSupport#getLocalizedName(BaseRoleModel)
     */
    @InstanceName
    @DependsOnProperties({"name", "nameMessageKey", "localizedNames"})
    public String getInstanceName(RoleLocalizationSupport roleLocalizationSupport) {
        return roleLocalizationSupport.getLocalizedName(this);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNameMessageKey() {
        return nameMessageKey;
    }

    public void setNameMessageKey(String nameMessageKey) {
        this.nameMessageKey = nameMessageKey;
    }

    public String getDescriptionMessageKey() {
        return descriptionMessageKey;
    }

    public void setDescriptionMessageKey(String descriptionMessageKey) {
        this.descriptionMessageKey = descriptionMessageKey;
    }

    public String getLocalizedNames() {
        return localizedNames;
    }

    public void setLocalizedNames(String localizedNames) {
        this.localizedNames = localizedNames;
    }

    public String getLocalizedDescriptions() {
        return localizedDescriptions;
    }

    public void setLocalizedDescriptions(String localizedDescriptions) {
        this.localizedDescriptions = localizedDescriptions;
    }

    public Set<String> getChildRoles() {
        return childRoles;
    }

    public void setChildRoles(Set<String> childRoles) {
        this.childRoles = childRoles;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
