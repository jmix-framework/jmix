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

import io.jmix.core.metamodel.model.*;
import io.jmix.core.metamodel.model.impl.MetadataObjectImpl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * MetaProperty for {@link io.jmix.core.entity.KeyValueEntity}.
 */
public class KeyValueMetaProperty extends MetadataObjectImpl implements MetaProperty {

    protected final MetaClass metaClass;
    protected final Range range;
    protected final Class<?> javaClass;
    protected final Boolean mandatory;
    protected final AnnotatedElement annotatedElement = new FakeAnnotatedElement();
    protected final Type type;
    protected Store store;

    public KeyValueMetaProperty(MetaClass metaClass, String name, Class<?> javaClass, Range range, Type type) {
        this.metaClass = metaClass;
        this.name = name;
        this.range = range;
        this.javaClass = javaClass;
        this.type = type;
        this.mandatory = false;
        this.store = metaClass.getStore();
    }

    @Override
    public Session getSession() {
        return metaClass.getSession();
    }

    @Override
    public MetaClass getDomain() {
        return metaClass;
    }

    @Override
    public Range getRange() {
        return range;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isMandatory() {
        return Boolean.TRUE.equals(mandatory);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public MetaProperty getInverse() {
        return null;
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    @Override
    public Class<?> getJavaType() {
        return javaClass;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return null;
    }

    @Override
    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    protected static class FakeAnnotatedElement implements AnnotatedElement, Serializable {

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return false;
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValueMetaProperty)) return false;

        KeyValueMetaProperty that = (KeyValueMetaProperty) o;

        return metaClass.equals(that.metaClass) && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return 31 * metaClass.hashCode() + name.hashCode();
    }
}
