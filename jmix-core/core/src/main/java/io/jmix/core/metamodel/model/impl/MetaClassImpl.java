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

package io.jmix.core.metamodel.model.impl;

import io.jmix.core.metamodel.model.*;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class MetaClassImpl extends MetadataObjectImpl implements MetaClass {

    private volatile Map<String, MetaProperty> propertyByName = Collections.emptyMap();
    private volatile Collection<MetaProperty> properties = Collections.emptyList();
    private volatile Map<String, MetaProperty> ownPropertyByName = Collections.emptyMap();
    private volatile Collection<MetaProperty> ownProperties = Collections.emptyList();

    private final Session session;
    private Class<?> javaClass;
    private Store store;

    protected List<MetaClass> ancestors = Collections.emptyList();
    protected Collection<MetaClass> descendants = Collections.emptySet();

    public MetaClassImpl(Session session, String className) {
		super();

		this.session = session;
        this.name = className;
    }

    @Override
    @Nullable
    public MetaClass getAncestor() {
        if (ancestors.isEmpty()) {
            return null;
        } else  {
            return ancestors.get(0);
        }
    }

    @Override
    public List<MetaClass> getAncestors() {
        return new ArrayList<>(ancestors);
    }

    @Override
    public Collection<MetaClass> getDescendants() {
        return new ArrayList<>(descendants);
    }

	@Override
    public Session getSession() {
		return session;
	}

    @SuppressWarnings("unchecked")
    @Override
    public <T> Class<T> getJavaClass() {
        return (Class<T>) javaClass;
    }

    @Override
    public Collection<MetaProperty> getProperties() {
        return properties;
    }

    @Override
    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    @Override
    @Nullable
    public MetaProperty findProperty(String name) {
        return propertyByName.get(name);
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
        String[] properties = propertyPath.split("\\."); // split should not create java.util.regex.Pattern

        // do not use ArrayList, leads to excessive memory allocation
        MetaProperty[] metaProperties = new MetaProperty[properties.length];

        MetaProperty currentProperty;
        MetaClass currentClass = this;

        for (int i = 0; i < properties.length; i++) {
            if (currentClass == null) {
                return null;
            }
            currentProperty = currentClass.findProperty(properties[i]);
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
        return ownProperties;
    }

    public void setJavaClass(Class<?> javaClass) {
        this.javaClass = javaClass;
    }

    public void addAncestor(MetaClass ancestorClass) {
        if (!ancestors.contains(ancestorClass)) {
            List<MetaClass> newAncestors = new ArrayList<>(ancestors.size() + 1);
            newAncestors.addAll(ancestors);
            newAncestors.add(ancestorClass);
            ancestors = Collections.unmodifiableList(newAncestors);
            rebuildPropertySnapshots();
            refreshDescendantSnapshots();
        }
        ((MetaClassImpl) ancestorClass).addDescendant(this);
    }

    public void registerProperty(MetaProperty metaProperty) {
        LinkedHashMap<String, MetaProperty> newOwnProperties = new LinkedHashMap<>(ownPropertyByName);
        newOwnProperties.remove(metaProperty.getName());
        newOwnProperties.put(metaProperty.getName(), metaProperty);
        publishOwnPropertySnapshots(newOwnProperties);
        rebuildPropertySnapshots();
        refreshDescendantSnapshots();
    }

    public void unregisterProperty(MetaProperty metaProperty) {
        if (ownPropertyByName.containsKey(metaProperty.getName())) {
            LinkedHashMap<String, MetaProperty> newOwnProperties = new LinkedHashMap<>(ownPropertyByName);
            newOwnProperties.remove(metaProperty.getName());
            publishOwnPropertySnapshots(newOwnProperties);
            rebuildPropertySnapshots();
            refreshDescendantSnapshots();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Replaces all resolved and own properties of the meta class.
     *
     * <p>Intended for rebuilding cloned metadata snapshots without mutating published property maps in place.</p>
     *
     * @param properties all properties visible on the meta class
     * @param ownProperties properties declared directly on the meta class
     */
    public void replaceProperties(Collection<MetaProperty> properties, Collection<MetaProperty> ownProperties) {
        publishPropertySnapshots(properties);
        publishOwnPropertySnapshots(ownProperties);
    }

    /**
     * Replaces ancestor and descendant relations of the meta class.
     *
     * <p>Intended for rebuilding a cloned metadata hierarchy before the new snapshot is published.</p>
     *
     * @param ancestors ancestor chain of the meta class
     * @param descendants descendant meta classes
     */
    public void replaceHierarchy(List<MetaClass> ancestors, Collection<MetaClass> descendants) {
        this.ancestors = Collections.unmodifiableList(new ArrayList<>(ancestors));
        this.descendants = Collections.unmodifiableSet(new LinkedHashSet<>(descendants));
    }

    protected void addDescendant(MetaClass descendantClass) {
        if (!descendants.contains(descendantClass)) {
            LinkedHashSet<MetaClass> newDescendants = new LinkedHashSet<>(descendants);
            newDescendants.add(descendantClass);
            descendants = Collections.unmodifiableSet(newDescendants);
        }
    }

    protected void rebuildPropertySnapshots() {
        LinkedHashMap<String, MetaProperty> resolvedProperties = new LinkedHashMap<>();

        for (int i = ancestors.size() - 1; i >= 0; i--) {
            MetaClass ancestorClass = ancestors.get(i);
            for (MetaProperty ancestorProperty : ancestorClass.getOwnProperties()) {
                MetaProperty property = getAncestorProperty(ancestorProperty);
                if (property != null) {
                    resolvedProperties.remove(property.getName());
                    resolvedProperties.put(property.getName(), property);
                }
            }
        }

        for (MetaProperty ownProperty : ownProperties) {
            resolvedProperties.remove(ownProperty.getName());
            resolvedProperties.put(ownProperty.getName(), ownProperty);
        }

        publishPropertySnapshots(resolvedProperties);
    }

    protected void refreshDescendantSnapshots() {
        for (MetaClass descendant : descendants) {
            MetaClassImpl descendantMetaClass = (MetaClassImpl) descendant;
            descendantMetaClass.rebuildPropertySnapshots();
            descendantMetaClass.refreshDescendantSnapshots();
        }
    }

    @Nullable
    protected MetaProperty getAncestorProperty(MetaProperty ancestorProperty) {
        MetaProperty ownProperty = ownPropertyByName.get(ancestorProperty.getName());
        if (ownProperty != null) {
            return ownProperty;
        }
        if (ancestorProperty instanceof CloneableMetaProperty cloneableMetaProperty) {
            MetaProperty clone = cloneableMetaProperty.makeClone(this);
            // Preserve the store of the previously resolved clone: it is assigned per meta class by the
            // metadata loader and may differ from the ancestor's store (e.g. UNDEFINED on a mapped
            // superclass vs. MAIN on this concrete entity). Recreating the clone from the ancestor on a
            // snapshot rebuild must not reset it.
            MetaProperty previousProperty = propertyByName.get(ancestorProperty.getName());
            if (previousProperty != null && previousProperty.getDomain() == this
                    && clone instanceof MetaPropertyImpl cloneImpl) {
                cloneImpl.setStore(previousProperty.getStore());
            }
            return clone;
        }
        return null;
    }

    protected void publishPropertySnapshots(Map<String, MetaProperty> properties) {
        this.propertyByName = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
        this.properties = Collections.unmodifiableList(new ArrayList<>(properties.values()));
    }

    protected void publishPropertySnapshots(Collection<MetaProperty> properties) {
        LinkedHashMap<String, MetaProperty> propertiesByName = new LinkedHashMap<>();
        for (MetaProperty property : properties) {
            propertiesByName.put(property.getName(), property);
        }
        publishPropertySnapshots(propertiesByName);
    }

    protected void publishOwnPropertySnapshots(Map<String, MetaProperty> ownProperties) {
        this.ownPropertyByName = Collections.unmodifiableMap(new LinkedHashMap<>(ownProperties));
        this.ownProperties = Collections.unmodifiableList(new ArrayList<>(ownProperties.values()));
    }

    protected void publishOwnPropertySnapshots(Collection<MetaProperty> ownProperties) {
        LinkedHashMap<String, MetaProperty> propertiesByName = new LinkedHashMap<>();
        for (MetaProperty property : ownProperties) {
            propertiesByName.put(property.getName(), property);
        }
        publishOwnPropertySnapshots(propertiesByName);
    }
}
