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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseRole implements Serializable {
    private String name;
    private String code;
    private String source;
    private String description;
    private String nameMessageKey;
    private String descriptionMessageKey;
    private String localizedNames;
    private String localizedDescriptions;
    private Set<String> childRoles;
    private Map<String, String> customProperties = new HashMap<>();
    private String tenantId;

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

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the key of the message that localizes the name, when the role declares its name as a
     * {@code msg://} reference; {@link #getName()} then holds the message in the default locale, or the key when that
     * message is missing or blank
     */
    public String getNameMessageKey() {
        return nameMessageKey;
    }

    public void setNameMessageKey(String nameMessageKey) {
        this.nameMessageKey = nameMessageKey;
    }

    /**
     * @return the key of the message that localizes the description, when the role declares its description as a
     * {@code msg://} reference; {@link #getDescription()} then holds the message in the default locale, or the key
     * when that message is missing
     */
    public String getDescriptionMessageKey() {
        return descriptionMessageKey;
    }

    public void setDescriptionMessageKey(String descriptionMessageKey) {
        this.descriptionMessageKey = descriptionMessageKey;
    }

    /**
     * @return the name localized to other locales, in {@link java.util.Properties} format keyed by locale, such as
     * {@code de=Leiter}
     */
    public String getLocalizedNames() {
        return localizedNames;
    }

    public void setLocalizedNames(String localizedNames) {
        this.localizedNames = localizedNames;
    }

    /**
     * @return the description localized to other locales, in {@link java.util.Properties} format keyed by locale
     */
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
