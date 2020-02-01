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

import java.util.*;

public class MetaClassImpl extends MetadataObjectImpl implements MetaClass {

	private Map<String, MetaProperty> propertyByName = new HashMap<>();
    private Map<String, MetaProperty> ownPropertyByName = new HashMap<>();

	private final Session session;
    private Class javaClass;
    private Store store;

    protected List<MetaClass> ancestors = new ArrayList<>(3);
    protected Collection<MetaClass> descendants = new HashSet<>(0);

    public MetaClassImpl(Session session, String className) {
		super();

		this.session = session;
        this.name = className;

        ((SessionImpl) this.session).registerClass(this);
    }

    @Override
    public MetaClass getAncestor() {
        if (ancestors.size() == 0) {
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
        return javaClass;
    }

    @Override
    public Collection<MetaProperty> getProperties() {
		return propertyByName.values();
	}

    @Override
    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    @Override
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
        return ownPropertyByName.values();
    }

    public void setJavaClass(Class javaClass) {
        this.javaClass = javaClass;
        ((SessionImpl) session).registerClass(this);
    }

    public void addAncestor(MetaClass ancestorClass) {
        if (!ancestors.contains(ancestorClass)) {
            ancestors.add(ancestorClass);
            for (MetaProperty metaProperty : ancestorClass.getProperties()) {
                propertyByName.put(metaProperty.getName(), metaProperty);
            }
        }
        if (!((MetaClassImpl) ancestorClass).descendants.contains(this))
            ((MetaClassImpl) ancestorClass).descendants.add(this);
    }

    public void registerProperty(MetaProperty metaProperty) {
        propertyByName.put(metaProperty.getName(), metaProperty);
        ownPropertyByName.put(metaProperty.getName(), metaProperty);
        for (MetaClass descendant : descendants) {
            ((MetaClassImpl) descendant).registerAncestorProperty(metaProperty);
        }
    }

    public void registerAncestorProperty(MetaProperty metaProperty) {
        MetaProperty prop = propertyByName.get(metaProperty.getName());
        if (prop == null) {
            MetaPropertyImpl clone = new MetaPropertyImpl((MetaPropertyImpl) metaProperty);
            clone.setDomain(this);
            propertyByName.put(metaProperty.getName(), clone);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
