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

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Non-persistent entity used to display resource policies in UI
 */
@JmixEntity(name = "sec_ResourcePolicyModel")
public class ResourcePolicyModel {

    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty(mandatory = true)
    private ResourcePolicyType type;

    @JmixProperty(mandatory = true)
    private String resource;

    @JmixProperty(mandatory = true)
    private String action;

    @JmixProperty
    private String effect;

    @JmixProperty
    private String scope;

    @JmixProperty
    private String policyGroup;

    @JmixProperty
    private Map<String, String> customProperties = new HashMap<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ResourcePolicyType getType() {
        return type;
    }

    public String getTypeId() {
        return type != null ? type.getId() : null;
    }

    public void setType(ResourcePolicyType type) {
        this.type = type;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getPolicyGroup() {
        return policyGroup;
    }

    public void setPolicyGroup(String policyGroup) {
        this.policyGroup = policyGroup;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }
}
