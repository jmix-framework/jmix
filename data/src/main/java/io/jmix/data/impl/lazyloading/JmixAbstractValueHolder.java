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

package io.jmix.data.impl.lazyloading;

import io.jmix.core.JmixEntity;
import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.indirection.IndirectCollection;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.indirection.WeavedAttributeValueHolderInterface;

import java.io.Serializable;
import java.lang.reflect.Field;

public abstract class JmixAbstractValueHolder implements ValueHolderInterface, WeavedAttributeValueHolderInterface,
        Cloneable, Serializable {
    protected volatile boolean isInstantiated;
    protected volatile Object value;

    @Override
    public boolean isCoordinatedWithProperty() {
        return false;
    }

    @Override
    public void setIsCoordinatedWithProperty(boolean coordinated) {

    }

    @Override
    public boolean isNewlyWeavedValueHolder() {
        return false;
    }

    @Override
    public void setIsNewlyWeavedValueHolder(boolean isNew) {

    }

    @Override
    public boolean shouldAllowInstantiationDeferral() {
        return false;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInstantiated() {
        return isInstantiated;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
        this.isInstantiated = true;
    }

    protected void visitEntity(JmixEntity entity, MetaProperty property, JmixEntity parentEntity) {
        switch (property.getRange().getCardinality()) {
            case ONE_TO_ONE:
            case MANY_TO_ONE:
                try {
                    Field declaredField = entity.getClass().getDeclaredField("_persistence_" + property.getName() + "_vh");
                    boolean accessible = declaredField.isAccessible();
                    declaredField.setAccessible(true);
                    Object fieldInstance = declaredField.get(entity);
                    if (fieldInstance instanceof JmixSingleValueHolder) {
                        JmixSingleValueHolder vh = (JmixSingleValueHolder) fieldInstance;
                        if (vh.getParentEntity() != null && vh.getParentEntity().equals(value)) {
                            vh.setValue(parentEntity);
                        }
                    } else if (fieldInstance instanceof JmixWrappingValueHolder) {
                        JmixWrappingValueHolder vh = (JmixWrappingValueHolder) fieldInstance;
                        if (vh.getEntityId() != null && vh.getEntityId().equals(parentEntity.__getEntityEntry().getEntityId())) {
                            vh.setValue(parentEntity);
                        }
                    }
                    declaredField.setAccessible(accessible);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                }
                break;
            case ONE_TO_MANY:
            case MANY_TO_MANY:
                IndirectCollection fieldValue = entity.__getEntityEntry().getAttributeValue(property.getName());
                if (fieldValue != null && fieldValue.getValueHolder() instanceof JmixCollectionValueHolder) {
                    JmixCollectionValueHolder vh = (JmixCollectionValueHolder) fieldValue.getValueHolder();
                    vh.setRootEntity(parentEntity);
                }
                break;
            default:
                break;
        }
    }
}
