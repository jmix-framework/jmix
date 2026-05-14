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

package io.jmix.flowui.view.template.impl;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.view.template.ViewTemplateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Default {@link ViewTemplateHelper} implementation used by built-in view templates.
 *
 * @see #getProperties(MetaClass, List, List)
 */
@Component("flowui_DefaultViewTemplateHelper")
public class DefaultViewTemplateHelper implements ViewTemplateHelper {

    @Autowired
    protected MetadataTools metadataTools;

    /**
     * Returns direct single-value properties for the entity in metadata order.
     * <p>
     * Collection-valued and embedded properties are ignored. System, secret, identifier, version,
     * generated, and audit properties are excluded by default. Properties listed in
     * {@code includeProperties} are added back when they are supported direct properties, and
     * properties listed in {@code excludeProperties} are removed from the final result.
     *
     * @param metaClass entity metadata
     * @param includeProperties property names to include explicitly
     * @param excludeProperties property names to exclude explicitly
     * @return filtered entity properties in metadata order
     * @throws IllegalArgumentException if a property path is provided instead of a direct property name
     */
    @Override
    public List<MetaProperty> getProperties(MetaClass metaClass, List<String> includeProperties, List<String> excludeProperties) {
        Set<String> includedPropertyNames = validatePropertyNames(metaClass, includeProperties);
        Set<String> excludedPropertyNames = validatePropertyNames(metaClass, excludeProperties);

        return metaClass.getProperties().stream()
                .filter(this::isSupportedProperty)
                .filter(property -> includedPropertyNames.contains(property.getName()) || !isDefaultExcluded(property))
                .filter(property -> !excludedPropertyNames.contains(property.getName()))
                .toList();
    }

    protected Set<String> validatePropertyNames(MetaClass metaClass, Collection<String> propertyNames) {
        if (propertyNames == null || propertyNames.isEmpty()) {
            return Set.of();
        }

        Set<String> result = new LinkedHashSet<>();
        for (String propertyName : propertyNames) {
            if (propertyName.contains(".")) {
                throw new IllegalArgumentException("Property paths are not supported: " + propertyName);
            }

            metaClass.getProperty(propertyName);
            result.add(propertyName);
        }

        return result;
    }

    protected boolean isSupportedProperty(MetaProperty metaProperty) {
        return !metaProperty.getRange().getCardinality().isMany()
                && metaProperty.getType() != MetaProperty.Type.EMBEDDED;
    }

    protected boolean isDefaultExcluded(MetaProperty metaProperty) {
        return metadataTools.isSystemLevel(metaProperty)
                || metadataTools.isSystem(metaProperty)
                || metadataTools.isSecret(metaProperty);
    }
}
