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

    public String getEntityName() {
        return entityName;
    }

    public List<String> getLocalizedNames() {
        return localizedNames;
    }

    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public List<String> getPropertyLocalizedNames() {
        return propertyLocalizedNames;
    }
}
