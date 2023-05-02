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

package io.jmix.eclipselink.impl.lazyloading;

import io.jmix.core.FetchPlans;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.indirection.WeavedAttributeValueHolderInterface;
import org.eclipse.persistence.internal.indirection.UnitOfWorkValueHolder;
import org.eclipse.persistence.internal.indirection.WrappingValueHolder;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.springframework.beans.factory.BeanFactory;

import java.io.Serializable;
import java.rmi.server.ObjID;
import java.util.Objects;

public abstract class AbstractValueHolder extends UnitOfWorkValueHolder implements ValueHolderInterface, WeavedAttributeValueHolderInterface,
        WrappingValueHolder, Cloneable, Serializable {
    private static final long serialVersionUID = 7624285533412298485L;

    protected transient BeanFactory beanFactory;
    private final UnitOfWorkValueHolder originalValueHolder;
    private final Object owner;
    private final MetaPropertyInfo propertyInfo;
    private volatile boolean isInstantiated;
    private volatile Object value;
    private LoadOptions loadOptions;

    public AbstractValueHolder(BeanFactory beanFactory,
                               ValueHolderInterface originalValueHolder,
                               Object owner,
                               MetaProperty metaProperty) {
        this.beanFactory = beanFactory;
        this.originalValueHolder = (UnitOfWorkValueHolder) originalValueHolder;
        this.owner = owner;
        this.propertyInfo = new MetaPropertyInfo(metaProperty);
    }

    @Override
    public ValueHolderInterface getWrappedValueHolder() {
        return LazyLoadingContext.isDisabled() ? originalValueHolder : this;
    }

    @Override
    public boolean isCoordinatedWithProperty() {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.isCoordinatedWithProperty();
        }
        return false;
    }

    @Override
    public void setIsCoordinatedWithProperty(boolean coordinated) {
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.setIsCoordinatedWithProperty(coordinated);
        }
    }

    @Override
    public boolean isNewlyWeavedValueHolder() {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.isNewlyWeavedValueHolder();
        }
        return false;
    }

    @Override
    public void setIsNewlyWeavedValueHolder(boolean isNew) {
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.setIsNewlyWeavedValueHolder(isNew);
        }
    }

    @Override
    public boolean shouldAllowInstantiationDeferral() {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.shouldAllowInstantiationDeferral();
        }
        return false;
    }

    @Override
    public DatabaseMapping getMapping() {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.getMapping();
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public boolean isEasilyInstantiated() {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.isEasilyInstantiated();
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public boolean isPessimisticLockingValueHolder() {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.isPessimisticLockingValueHolder();
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public ObjID getWrappedValueHolderRemoteID() {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.getWrappedValueHolderRemoteID();
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public boolean isSerializedRemoteUnitOfWorkValueHolder() {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.isSerializedRemoteUnitOfWorkValueHolder();
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public Object instantiateForUnitOfWorkValueHolder(UnitOfWorkValueHolder unitOfWorkValueHolder) {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.instantiateForUnitOfWorkValueHolder(unitOfWorkValueHolder);
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public void releaseWrappedValueHolder(AbstractSession targetSession) {
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.releaseWrappedValueHolder(targetSession);
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public void setBackupValueHolder(ValueHolderInterface backupValueHolder) {
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.setBackupValueHolder(backupValueHolder);
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public AbstractRecord getRow() {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.getRow();
        }

        // rework in case of NPE or another problems somewhere
        return null; // return null in order to force loading through getValue()
    }

    @Override
    public AbstractSession getSession() {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.getSession();
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public Object getValue(UnitOfWorkImpl uow) {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.getValue(uow);
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public void postInstantiate() {
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.postInstantiate();
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public void privilegedSetValue(Object value) {
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.privilegedSetValue(value);
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public void setInstantiated() {
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.setInstantiated();
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public void setRow(AbstractRecord row) {
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.setRow(row);
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public void setSession(AbstractSession session) {
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.setSession(session);
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public void setUninstantiated() {
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.setUninstantiated();
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public Object buildCloneFor(Object originalAttributeValue) {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.buildCloneFor(originalAttributeValue);
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    protected Object buildBackupCloneFor(Object cloneAttributeValue) {
        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.buildCloneFor(cloneAttributeValue);
        }
        throw new RuntimeException("Unsupported by lazy loading");
    }

    @Override
    public boolean isInstantiated() {
        if (isInstantiated) {
            return true;
        } else {
            if (LazyLoadingContext.isDisabled()) {
                return originalValueHolder.isInstantiated();
            }
        }
        return false;
    }

    @Override
    public Object getValue() {
        if (!isInstantiated) {
            if (LazyLoadingContext.isDisabled()) {
                value = originalValueHolder.getValue();
            } else {
                synchronized (this) {
                    value = loadValue();
                    afterLoadValue(value);
                }
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
        if (LazyLoadingContext.isDisabled()) {
            originalValueHolder.setValue(value);
        }
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

    protected UnconstrainedDataManager getDataManager() {
        return beanFactory.getBean(UnconstrainedDataManager.class);
    }

    protected FetchPlans getFetchPlans() {
        return beanFactory.getBean(FetchPlans.class);
    }
}
