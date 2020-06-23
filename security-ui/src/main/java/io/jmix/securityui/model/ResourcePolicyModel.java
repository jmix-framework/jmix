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

import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import io.jmix.data.entity.BaseUuidEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Non-persistent entity used to display resource policies in UI
 */
@ModelObject(name = "sec_ResourcePolicyModel")
public class ResourcePolicyModel extends BaseUuidEntity {

    @ModelProperty(mandatory = true)
    private String type;

    @ModelProperty(mandatory = true)
    private String resource;

    @ModelProperty(mandatory = true)
    private String action;

    @ModelProperty
    private String effect;

    @ModelProperty
    private Map<String, String> customProperties = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }
}
