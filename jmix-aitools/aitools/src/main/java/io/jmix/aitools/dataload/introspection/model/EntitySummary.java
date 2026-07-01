/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.dataload.introspection.model;

import java.util.List;

/**
 * Compact summary of an entity for listing: its name, localized names and the names (raw and
 * localized) of its properties, without full per-property details.
 */
public class EntitySummary {

    protected String entityName;
    protected List<String> localizedNames;
    protected List<String> propertyNames;
    protected List<String> propertyLocalizedNames;

    public EntitySummary(String entityName,
                         List<String> localizedNames,
                         List<String> propertyNames,
                         List<String> propertyLocalizedNames) {
        this.entityName = entityName;
        this.localizedNames = localizedNames;
        this.propertyNames = propertyNames;
        this.propertyLocalizedNames = propertyLocalizedNames;
    }

    /**
     * Returns the Jmix entity name.
     *
     * @return entity name
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns the entity captions for the configured locales.
     *
     * @return localized entity names
     */
    public List<String> getLocalizedNames() {
        return localizedNames;
    }

    /**
     * Returns the names of the entity's properties.
     *
     * @return property names
     */
    public List<String> getPropertyNames() {
        return propertyNames;
    }

    /**
     * Returns the localized captions of the entity's properties.
     *
     * @return localized property names
     */
    public List<String> getPropertyLocalizedNames() {
        return propertyLocalizedNames;
    }
}
