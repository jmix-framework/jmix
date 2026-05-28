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
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.LoadedPropertiesInfo;
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
    private transient volatile boolean loading;
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
        // Called e.g. by not-instantiated IndirectList.add()
        return originalValueHolder.getMapping();
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
        }

        if (LazyLoadingContext.isDisabled()) {
            return originalValueHolder.isInstantiated();
        }

        synchronized (this) {
            return isInstantiated || loading && shouldDelegateOnRecursiveLoad();
        }
    }

    @Override
    public Object getValue() {
        if (isInstantiated) {
            return value;
        }

        synchronized (this) {
            if (isInstantiated) {
                return value;
            }

            if (LazyLoadingContext.isDisabled()) {
                value = originalValueHolder.getValue();
                isInstantiated = true;
                return value;
            }

            // A recursive load can reach the same holder for a managed owner. Delegate to EclipseLink only to
            // break the cycle; the outer load will publish the final Jmix value and loaded-state.
            if (loading && shouldDelegateOnRecursiveLoad()) {
                return originalValueHolder.getValue();
            }

            loading = true;
            try {
                value = loadValue();
                // Only the outer Jmix load may complete the holder. Recursive delegated values are temporary.
                afterLoadValue(value);
                registerLoadedProperty(getOwner(), getPropertyInfo().getName());
                isInstantiated = true;
            } catch (RuntimeException | Error e) {
                value = null;
                isInstantiated = false;
                throw e;
            } finally {
                loading = false;
            }
        }

        return value;
    }

    /**
     * Allows a reentrant {@link #getValue()} call, made while this holder is already loading, to delegate to the
     * original EclipseLink holder instead of starting the same Jmix load again.
     * <p>
     * Keep this opt-in: it is needed for collection holders whose owner is managed. During the internal owner reload,
     * EclipseLink may resolve the same owner instance from the persistence context and touch the same collection holder.
     * For detached owners the reload uses another managed instance, so the original holder is not expected to re-enter.
     *
     * @return true only when delegating is needed to break same-holder recursion
     */
    protected boolean shouldDelegateOnRecursiveLoad() {
        return false;
    }

    protected abstract Object loadValue();

    protected abstract void afterLoadValue(Object value);

    protected void registerLoadedProperty(Object entity, String property) {
        LoadedPropertiesInfo loadedPropertiesInfo = EntitySystemAccess.getEntityEntry(entity).getLoadedPropertiesInfo();
        if (loadedPropertiesInfo != null) {
            loadedPropertiesInfo.registerProperty(property, true);
        }
    }

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

            if (valueHolder instanceof CollectionValuePropertyHolder holder) {
                holder.setRootEntity(owner);
            }
        } else {
            Object valueHolder = ValueHoldersSupport.getSingleValueHolder(entity, property.getName());

            if (valueHolder instanceof SingleValueMappedByPropertyHolder holder) {
                if (Objects.equals(holder.getOwner(), value)
                        && property.getName().equals(getPropertyInfo().getInversePropertyName())) {
                    holder.setValue(owner);
                    registerLoadedProperty(entity, property.getName());
                }
            } else if (valueHolder instanceof SingleValueOwningPropertyHolder holder) {
                if (Objects.equals(holder.getEntityId(), EntityValues.getId(owner))) {
                    holder.setValue(owner);
                    registerLoadedProperty(entity, property.getName());
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
