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

import com.google.common.collect.ForwardingMap;
import io.jmix.core.metamodel.model.*;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.function.Consumer;

public class MetaPropertyImpl extends MetadataObjectImpl implements MetaProperty {

    private Store store;
    private MetaClass domain;
    private final Session session;

    private boolean mandatory;
    private boolean readOnly;
    private Type type;
    private Range range;

    private MetaProperty inverse;

    private AnnotatedElement annotatedElement;
    private Class<?> javaType;
    private Class<?> declaringClass;

    public MetaPropertyImpl(MetaClass domain, String name) {
        this.domain = domain;
        this.session = domain.getSession();
        this.name = name;

        ((MetaClassImpl) domain).registerProperty(this);
    }

    public MetaPropertyImpl(MetaPropertyImpl prototype) {
        name = prototype.name;

        store = prototype.store;
        domain = prototype.domain;
        session = prototype.session;
        mandatory = prototype.mandatory;
        readOnly = prototype.readOnly;
        type = prototype.type;
        range = prototype.range;
        inverse = prototype.inverse;
        annotatedElement = prototype.annotatedElement;
        javaType = prototype.javaType;
        declaringClass = prototype.declaringClass;
    }

    @Override
    public MetaClass getDomain() {
        return domain;
    }

    public void setDomain(MetaClass domain) {
        this.domain = domain;
    }

    @Override
    public MetaProperty getInverse() {
        return inverse;
    }

    public void setInverse(MetaProperty inverse) {
        this.inverse = inverse;
        withClones(clone -> clone.inverse = inverse);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
        withClones(clone -> clone.range = range);
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    public void setAnnotatedElement(AnnotatedElement annotatedElement) {
        this.annotatedElement = annotatedElement;
        withClones(clone -> clone.annotatedElement = annotatedElement);
    }

    @Override
    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
        withClones(clone -> clone.javaType = javaType);
    }

    @Override
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setDeclaringClass(Class<?> declaringClass) {
        this.declaringClass = declaringClass;
        withClones(clone -> clone.declaringClass = declaringClass);

    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        withClones(clone -> clone.type = type);
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
        withClones(clone -> clone.mandatory = mandatory);
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        withClones(clone -> clone.readOnly = readOnly);
    }

    @Override
    public Map<String, Object> getAnnotations() {
        if (domain.getDescendants().isEmpty()) {
            return super.getAnnotations();
        } else {
            return new ForwardingMap<String, Object>() {
                @Override
                protected Map<String, Object> delegate() {
                    return MetaPropertyImpl.super.getAnnotations();
                }

                @Override
                public Object put(String key, Object value) {
                    Object prevVal = super.put(key, value);
                    withClones(clone -> clone.getAnnotations().put(key, value));
                    return prevVal;
                }

                @Override
                public void putAll(Map<? extends String, ?> map) {
                    super.putAll(map);
                    withClones(clone -> clone.getAnnotations().putAll(map));
                }
            };
        }
    }

    protected void withClones(Consumer<MetaPropertyImpl> consumer) {
        for (MetaClass descendant : domain.getDescendants()) {
            MetaPropertyImpl clone = (MetaPropertyImpl) descendant.getProperty(name);
            consumer.accept(clone);
        }
    }

    @Override
    public String toString() {
        return domain.getName() + "." + name;
    }
}
