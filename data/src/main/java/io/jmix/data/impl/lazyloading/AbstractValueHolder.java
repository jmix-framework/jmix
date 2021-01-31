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

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlans;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.indirection.WeavedAttributeValueHolderInterface;
import org.springframework.beans.factory.BeanFactory;

import java.io.Serializable;
import java.util.Objects;

public abstract class AbstractValueHolder implements ValueHolderInterface, WeavedAttributeValueHolderInterface,
        Cloneable, Serializable {
    private static final long serialVersionUID = 7624285533412298485L;

    protected transient BeanFactory beanFactory;
    private final ValueHolderInterface originalValueHolder;
    private final Object owner;
    private final MetaPropertyInfo propertyInfo;
    protected volatile boolean isInstantiated;
    protected volatile Object value;
    private LoadOptions loadOptions;

    public AbstractValueHolder(BeanFactory beanFactory,
                               ValueHolderInterface originalValueHolder,
                               Object owner,
                               MetaProperty metaProperty) {
        this.beanFactory = beanFactory;
        this.originalValueHolder = originalValueHolder;
        this.owner = owner;
        this.propertyInfo = new MetaPropertyInfo(metaProperty);
    }

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
            throw new RuntimeException("Unable to clone value holder", e);
        }
    }

    @Override
    public boolean isInstantiated() {
        return isInstantiated;
    }

    @Override
    public Object getValue() {
        if (!isInstantiated) {
            synchronized (this) {
                value = loadValue();
                afterLoadValue(value);
            }
            isInstantiated = true;
        }
        return value;
    }

    protected abstract Object loadValue();

    protected abstract void afterLoadValue(Object value);

    @Override
    public void setValue(Object value) {
        this.value = value;
        this.isInstantiated = true;
    }

    public void setLoadOptions(LoadOptions loadOptions) {
        this.loadOptions = loadOptions;
    }

    public LoadOptions getLoadOptions() {
        return loadOptions;
    }

    public Object getOwner() {
        return owner;
    }

    public MetaPropertyInfo getPropertyInfo() {
        return propertyInfo;
    }

    protected void replaceToExistingReferences(Object entity, MetaProperty property, Object owner) {
        if (property.getRange().getCardinality().isMany()) {
            Object valueHolder = ValueHoldersSupport.getCollectionValueHolder(entity, property.getName());

            if (valueHolder instanceof CollectionValuePropertyHolder) {
                CollectionValuePropertyHolder casted = (CollectionValuePropertyHolder) valueHolder;
                casted.setRootEntity(owner);
            }
        } else {
            Object valueHolder = ValueHoldersSupport.getSingleValueHolder(entity, property.getName());

            if (valueHolder instanceof SingleValueMappedByPropertyHolder) {
                SingleValueMappedByPropertyHolder casted = (SingleValueMappedByPropertyHolder) valueHolder;
                if (Objects.equals(casted.getOwner(), value)) {
                    casted.setValue(owner);
                }
            } else if (valueHolder instanceof SingleValueOwningPropertyHolder) {
                SingleValueOwningPropertyHolder casted = (SingleValueOwningPropertyHolder) valueHolder;
                if (Objects.equals(casted.getEntityId(), EntityValues.getId(owner))) {
                    casted.setValue(owner);
                }
            }
        }
    }

    protected void replaceLoadOptions(Object entity, MetaProperty property) {
        if (property.getRange().getCardinality().isMany()) {
            Object valueHolder = ValueHoldersSupport.getCollectionValueHolder(entity, property.getName());

            if (valueHolder instanceof AbstractValueHolder) {
                ((AbstractValueHolder) valueHolder).setLoadOptions(LoadOptions.with(getLoadOptions()));
            }
        } else {
            Object valueHolder = ValueHoldersSupport.getSingleValueHolder(entity, property.getName());

            if (valueHolder instanceof AbstractValueHolder) {
                ((AbstractValueHolder) valueHolder).setLoadOptions(LoadOptions.with(getLoadOptions()));
            }
        }
    }

    protected Metadata getMetadata() {
        return beanFactory.getBean(Metadata.class);
    }

    protected MetadataTools getMetadataTools() {
        return beanFactory.getBean(MetadataTools.class);
    }

    protected DataManager getDataManager() {
        return beanFactory.getBean(DataManager.class);
    }

    protected FetchPlans getFetchPlans() {
        return beanFactory.getBean(FetchPlans.class);
    }

}
