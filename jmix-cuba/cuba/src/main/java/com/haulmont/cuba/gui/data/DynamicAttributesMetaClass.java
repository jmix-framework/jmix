/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.data;

import com.google.common.collect.Collections2;
import io.jmix.core.metamodel.model.*;
import io.jmix.core.metamodel.model.impl.MetadataObjectImpl;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;

import javax.annotation.Nullable;
import java.util.*;

public class DynamicAttributesMetaClass extends MetadataObjectImpl implements MetaClass {

    private Map<String, MetaProperty> properties = new LinkedHashMap<>();
    private Map<String, CategoryAttribute> attributes = new LinkedHashMap<>();
    private Store store;

    public DynamicAttributesMetaClass(Store store) {
        this.store = store;
    }

    public void addProperty(MetaProperty property, CategoryAttribute attribute) {
        properties.put(property.getName(), property);
        attributes.put(property.getName(), attribute);
    }

    @Nullable
    @Override
    public Session getSession() {
        return null;
    }

    @Override
    public Store getStore() {
        return store;
    }

    @Nullable
    @Override
    public MetaClass getAncestor() {
        return null;
    }

    @Override
    public List<MetaClass> getAncestors() {
        return Collections.emptyList();
    }

    @Override
    public Collection<MetaClass> getDescendants() {
        return Collections.emptyList();
    }

    @Override
    public Class getJavaClass() {
        return Object.class;
    }

    @Override
    public MetaProperty findProperty(String name) {
        return properties.get(name);
    }

    @Override
    public MetaProperty getProperty(String name) {
        MetaProperty property = findProperty(name);
        if (property == null) {
            throw new IllegalArgumentException(String.format("Property '%s' not found in %s", name, getName()));
        }
        return property;
    }

    @Override
    public MetaPropertyPath getPropertyPath(String propertyPath) {
        MetaProperty currentProperty;

        currentProperty = this.getProperty(propertyPath);
        if (currentProperty == null) return null;

        return new MetaPropertyPath(this, currentProperty);
    }

    @Override
    public Collection<MetaProperty> getOwnProperties() {
        return properties.values();
    }

    @Override
    public Collection<MetaProperty> getProperties() {
        return properties.values();
    }

    public Collection<MetaProperty> getPropertiesFilteredByCategory(Category category) {
        return Collections2.filter(getProperties(), input -> {
            if (input != null && category != null) {
                CategoryAttribute categoryAttribute = attributes.get(input.getName());
                return category.equals(categoryAttribute.getCategory());
            } else {
                return false;
            }
        });
    }
}