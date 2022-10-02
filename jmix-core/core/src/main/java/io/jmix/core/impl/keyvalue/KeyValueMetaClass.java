/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.impl.keyvalue;

import io.jmix.core.Stores;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.SerializationContext;
import io.jmix.core.metamodel.model.*;
import io.jmix.core.metamodel.model.impl.MetadataObjectImpl;
import org.springframework.beans.factory.BeanFactory;

import javax.annotation.Nullable;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

/**
 * MetaClass for {@link KeyValueEntity}.
 */
public class KeyValueMetaClass extends MetadataObjectImpl implements MetaClass, Externalizable {

    private transient Map<String, MetaProperty> properties = new LinkedHashMap<>();

    private transient Store store;

    public void addProperty(MetaProperty property) {
        properties.put(property.getName(), property);
    }

    public void removeProperty(String propertyName) {
        properties.remove(propertyName);
    }

    public KeyValueMetaClass() {
        name = "sys_KeyValueEntity";
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
    public Session getSession() {
        return null; // temporary metaclass
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Class<T> getJavaClass() {
        return (Class<T>) KeyValueEntity.class;
    }

    @Override
    public MetaProperty findProperty(String name) {
        return properties.get(name);
    }

    @Override
    public MetaProperty getProperty(String name) {
        MetaProperty property = findProperty(name);
        if (property == null)
            throw new IllegalArgumentException("Property '" + name + "' not found in " + getName());
        return property;
    }

    @Override
    public MetaPropertyPath getPropertyPath(String propertyPath) {
        String[] properties = propertyPath.split("\\."); // split should not create java.util.regex.Pattern

        // do not use ArrayList, leads to excessive memory allocation
        MetaProperty[] metaProperties = new MetaProperty[properties.length];

        MetaProperty currentProperty;
        MetaClass currentClass = this;

        for (int i = 0; i < properties.length; i++) {
            if (currentClass == null) {
                return null;
            }
            currentProperty = currentClass.getProperty(properties[i]);
            if (currentProperty == null) {
                return null;
            }

            Range range = currentProperty.getRange();
            currentClass = range.isClass() ? range.asClass() : null;

            metaProperties[i] = currentProperty;
        }

        return new MetaPropertyPath(this, metaProperties);
    }

    @Override
    public Collection<MetaProperty> getOwnProperties() {
        return properties.values();
    }

    @Override
    public Collection<MetaProperty> getProperties() {
        return properties.values();
    }

    @Override
    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
        for (MetaProperty metaProperty : properties.values()) {
            ((KeyValueMetaProperty) metaProperty).setStore(store);
        }
    }

    @Override
    public String toString() {
        return "KeyValueMetaClass{" +
                "properties=" + properties.keySet() +
                '}';
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeObject(annotations);

        out.writeObject(store != null ? store.getName() : null);

        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, MetaProperty> entry : properties.entrySet()) {
            MetaProperty metaProperty = entry.getValue();
            if (metaProperty.getRange().isClass() && metaProperty.getRange().asClass() instanceof KeyValueMetaClass)
                map.put(entry.getKey(), metaProperty.getRange().asClass());
            else
                map.put(entry.getKey(), metaProperty.getJavaType());
        }
        out.writeObject(map);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.name = in.readUTF();
        this.annotations = (Map<String, Object>) in.readObject();

        BeanFactory beanFactory = SerializationContext.getThreadLocalBeanFactory();
        Stores stores = beanFactory.getBean(Stores.class);
        KeyValueMetaClassFactory metaClassFactory = beanFactory.getBean(KeyValueMetaClassFactory.class);

        String storeName = (String) in.readObject();
        if (storeName != null)
            this.setStore(stores.get(storeName));

        Map<String, Object> map = (Map<String, Object>) in.readObject();
        this.properties = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof KeyValueMetaClass)
                metaClassFactory.configurer(this).addProperty(name, (KeyValueMetaClass) value);
            else
                metaClassFactory.configurer(this).addProperty(name, (Class<?>) value);
        }
    }
}
