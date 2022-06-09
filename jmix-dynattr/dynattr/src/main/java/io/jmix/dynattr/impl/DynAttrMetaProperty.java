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

package io.jmix.dynattr.impl;

import io.jmix.core.metamodel.model.*;
import io.jmix.core.metamodel.model.impl.MetadataObjectImpl;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

/**
 * Note that transient attributes should be specified after deserialization and before usage
 */
public class DynAttrMetaProperty extends MetadataObjectImpl implements MetaProperty, Serializable {
    private static final long serialVersionUID = 839160118855669248L;

    protected String name; // for serialization
    private final String ownerMetaClassName; // to obtain metaclass after deserialization

    private transient MetaClass ownerMetaClass;
    private transient Range range;

    private final Class<?> javaClass;

    protected final AnnotatedElement annotatedElement = new FakeAnnotatedElement();
    protected final Type type;

    public DynAttrMetaProperty(String name,
                               MetaClass ownerMetaClass,
                               Class<?> javaClass,
                               Range range,
                               Type type) {
        this.ownerMetaClassName = ownerMetaClass.getName();
        this.ownerMetaClass = ownerMetaClass;
        this.javaClass = javaClass;
        this.name = name;
        this.type = type;
        this.range = range;
    }

    @Override
    public String getName() {//should be overridden to use current field
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    @Override
    public Session getSession() {
        return ownerMetaClass.getSession();
    }

    @Override
    public MetaClass getDomain() {
        return ownerMetaClass;
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
        return false;
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
        return ownerMetaClass.getStore();
    }

    //package-local methods for transient attributes filling
    String getOwnerMetaClassName() {
        return ownerMetaClassName;
    }

    @Nullable
    MetaClass getOwnerMetaClass() {
        return ownerMetaClass;
    }

    void setOwnerMetaClass(MetaClass ownerMetaClass) {
        this.ownerMetaClass = ownerMetaClass;
    }

    void setRange(Range range) {
        this.range = range;
    }


    private static class FakeAnnotatedElement implements AnnotatedElement, Serializable {

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

        if (!(o instanceof DynAttrMetaProperty)) return false;

        DynAttrMetaProperty that = (DynAttrMetaProperty) o;

        return Objects.equals(ownerMetaClassName, that.ownerMetaClassName) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return 31 * ownerMetaClassName.hashCode() + name.hashCode();
    }
}
