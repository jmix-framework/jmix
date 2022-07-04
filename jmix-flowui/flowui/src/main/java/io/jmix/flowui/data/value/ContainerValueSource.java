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

package io.jmix.flowui.data.value;

import com.google.common.base.Joiner;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.kit.event.EventBus;
import io.jmix.flowui.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class ContainerValueSource<E, V> implements EntityValueSource<E, V>, ApplicationContextAware {

    protected final InstanceContainer<E> container;

    protected MetaPropertyPath metaPropertyPath;
    protected String property;

    protected BindingState state = BindingState.INACTIVE;

    protected EventBus events = new EventBus();

    protected boolean dataModelSecurityEnabled = true;

    public ContainerValueSource(InstanceContainer<E> container, String property) {
        checkNotNullArgument(container);
        checkNotNullArgument(property);

        this.container = container;
        this.property = property;
    }

    /**
     * Sets data model security enabled for data binding.
     * <br>
     * Caller may set false in order to disable built-in security check on data binding.
     *
     * @param enabled enabled flag
     */
    public void setDataModelSecurityEnabled(boolean enabled) {
        this.dataModelSecurityEnabled = enabled;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        MetaClass metaClass = container.getEntityMetaClass();

        MetadataTools metadataTools = applicationContext.getBean(MetadataTools.class);
        this.metaPropertyPath = metadataTools.resolveMetaPropertyPath(metaClass, property);

        this.container.addItemChangeListener(this::containerItemChanged);
        this.container.addItemPropertyChangeListener(this::containerItemPropertyChanged);

        InstanceContainer<?> parentCont = container;

        for (int i = 1; i < this.metaPropertyPath.length(); i++) {
            MetaPropertyPath intermediatePath = new MetaPropertyPath(this.metaPropertyPath.getMetaClass(),
                    Arrays.copyOf(this.metaPropertyPath.getMetaProperties(), i));
            String pathToTarget = Joiner.on('.').join(
                    Arrays.copyOfRange(this.metaPropertyPath.getPath(), i, this.metaPropertyPath.length()));

            DataComponents dataComponents = applicationContext.getBean(DataComponents.class);
            @SuppressWarnings("unchecked")
            InstanceContainer<Object> propertyCont = dataComponents.createInstanceContainer(intermediatePath.getRangeJavaClass());

            parentCont.addItemChangeListener(event -> {
                if (event.getItem() != null) {
                    setState(BindingState.ACTIVE);
                } else {
                    setState(BindingState.INACTIVE);
                }
                propertyCont.setItem(event.getItem() != null ?
                        EntityValues.getValueEx(event.getItem(), intermediatePath.getMetaProperty().getName()) : null);
            });

            parentCont.addItemPropertyChangeListener(event -> {
                if (Objects.equals(event.getProperty(), intermediatePath.getMetaProperty().getName())) {
                    Object entity = event.getValue();
                    Object prevEntity = event.getPrevValue();
                    propertyCont.setItem(entity);

                    V prevValue = prevEntity != null ? EntityValues.getValueEx(prevEntity, pathToTarget) : null;
                    V value = entity != null ? EntityValues.getValueEx(entity, pathToTarget) : null;
                    events.fireEvent(new ValueChangeEvent<>(this, prevValue, value));
                }
            });

            if (i == this.metaPropertyPath.length() - 1) {
                propertyCont.addItemPropertyChangeListener(event -> {
                    if (Objects.equals(event.getProperty(), this.metaPropertyPath.getMetaProperty().getName())) {
                        events.fireEvent(new ValueChangeEvent<>(this, (V) event.getPrevValue(), (V) event.getValue()));
                    }
                });
            }

            parentCont = propertyCont;
        }
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public MetaPropertyPath getMetaPropertyPath() {
        return metaPropertyPath;
    }

    @Nullable
    @Override
    public E getItem() {
        return container.getItemOrNull();
    }

    @Override
    public boolean isDataModelSecurityEnabled() {
        return dataModelSecurityEnabled;
    }

    @Nullable
    @Override
    public V getValue() {
        E item = container.getItemOrNull();
        if (item != null) {
            return EntityValues.getValueEx(item, metaPropertyPath);
        }
        return null;
    }

    @Override
    public void setValue(@Nullable V value) {
        E item = container.getItemOrNull();
        if (item != null) {
            // Do not set collection value if it's not changed. As
            // EntityValues#propertyValueEquals() returns true only if
            // the new value is the same instance as old one, there may be
            // false unsaved changes in the detail view.
            if (isCollectionPropertyType() && isEqualCollectionValue(value)) {
                return;
            }

            if (canUpdateMasterRefs()) {
                updateMasterRefs(value);
            } else {
                EntityValues.setValueEx(item, metaPropertyPath.toPathString(), value);
            }
        }
    }

    @Override
    public boolean isReadOnly() {
        return metaPropertyPath.getMetaProperty().isReadOnly();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<V> getType() {
        return (Class<V>) metaPropertyPath.getMetaProperty().getJavaType();
    }

    @Override
    public BindingState getState() {
        return container.getItemOrNull() == null ? BindingState.INACTIVE : BindingState.ACTIVE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Registration addInstanceChangeListener(Consumer<InstanceChangeEvent<E>> listener) {
        return events.addListener(InstanceChangeEvent.class, (Consumer) listener);
    }

    @Override
    public Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return events.addListener(StateChangeEvent.class, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Registration addValueChangeListener(Consumer<ValueChangeEvent<V>> listener) {
        return events.addListener(ValueChangeEvent.class, (Consumer) listener);
    }

    protected void setState(BindingState state) {
        if (this.state != state) {
            this.state = state;

            events.fireEvent(new StateChangeEvent(this, BindingState.ACTIVE));
        }
    }

    @SuppressWarnings("unchecked")
    protected void containerItemChanged(InstanceContainer.ItemChangeEvent e) {
        if (e.getItem() != null) {
            setState(BindingState.ACTIVE);
        } else {
            setState(BindingState.INACTIVE);
        }

        events.fireEvent(new InstanceChangeEvent(this, e.getPrevItem(), e.getItem()));
    }

    @SuppressWarnings("unchecked")
    protected void containerItemPropertyChanged(InstanceContainer.ItemPropertyChangeEvent e) {
        if (Objects.equals(e.getProperty(), metaPropertyPath.toPathString())) {
            events.fireEvent(new ValueChangeEvent<>(this, (V) e.getPrevValue(), (V) e.getValue()));
        }
    }

    protected boolean canUpdateMasterRefs() {
        MetaPropertyPath mpp = getEntityMetaClass().getPropertyPath(metaPropertyPath.toPathString());
        if (mpp == null) {
            return false;
        }

        if (!mpp.getMetaProperty().getRange().getCardinality().isMany()) {
            return false;
        }

        MetaProperty inverseProperty = mpp.getMetaProperty().getInverse();

        return inverseProperty != null
                && inverseProperty.getType() == MetaProperty.Type.ASSOCIATION
                && !inverseProperty.getRange().getCardinality().isMany();
    }

    protected void updateMasterRefs(@Nullable V value) {
        DataContext dataContext = getDataContext();
        if (dataContext == null) {
            return;
        }

        MetaProperty inverseProperty = getInverseProperty();
        if (inverseProperty == null) {
            return;
        }

        if (!(value instanceof Collection)) {
            return;
        }

        //noinspection unchecked
        Collection<V> newValue = (Collection<V>) value;

        Collection<? extends V> itemValue = EntityValues.getValueEx(getItem(), metaPropertyPath.toPathString());
        Collection<? extends V> oldValue = copyPropertyCollection(itemValue);

        EntityValues.setValueEx(getItem(), metaPropertyPath.toPathString(), value);

        container.mute();

        if (CollectionUtils.isNotEmpty(newValue)) {
            for (V v : newValue) {
                if (CollectionUtils.isEmpty(oldValue) || !oldValue.contains(v)) {
                    EntityValues.setValue(dataContext.merge((Object) v), inverseProperty.getName(), getItem());
                }
            }
        }

        if (CollectionUtils.isNotEmpty(oldValue)) {
            for (V v : oldValue) {
                if (CollectionUtils.isEmpty(newValue) || !newValue.contains(v)) {
                    EntityValues.setValue(dataContext.merge((Object) v), inverseProperty.getName(), null);
                }
            }
        }

        container.unmute();
    }

    @Nullable
    protected Collection<? extends V> copyPropertyCollection(@Nullable Collection<? extends V> propertyValue) {
        if (propertyValue == null) {
            return null;
        }

        Class<?> propertyCollectionType = metaPropertyPath.getMetaProperty().getJavaType();

        if (Set.class.isAssignableFrom(propertyCollectionType)) {
            return new LinkedHashSet<>(propertyValue);
        } else if (List.class.isAssignableFrom(propertyCollectionType)) {
            return new ArrayList<>(propertyValue);
        }

        return new LinkedHashSet<>(propertyValue);
    }

    @Nullable
    protected DataContext getDataContext() {
        DataLoader loader = container instanceof HasLoader
                ? ((HasLoader) container).getLoader()
                : null;

        return loader != null
                ? loader.getDataContext()
                : null;
    }

    @Nullable
    protected MetaProperty getInverseProperty() {
        MetaPropertyPath mpp = getEntityMetaClass().getPropertyPath(metaPropertyPath.toPathString());
        if (mpp == null) {
            return null;
        }

        return mpp.getMetaProperty().getInverse();
    }

    public InstanceContainer<E> getContainer() {
        return container;
    }

    protected boolean isCollectionPropertyType() {
        Class<?> propertyType = metaPropertyPath.getMetaProperty().getJavaType();
        return Collection.class.isAssignableFrom(propertyType);
    }

    protected boolean isEqualCollectionValue(@Nullable V a) {
        Collection<V> newValue = (Collection<V>) a;
        Collection<V> oldValue = EntityValues.getValueEx(getItem(), metaPropertyPath.toPathString());

        if (CollectionUtils.isEmpty(newValue) && CollectionUtils.isEmpty(oldValue)) {
            return true;
        }

        if ((CollectionUtils.isEmpty(newValue) && CollectionUtils.isNotEmpty(oldValue))
                || (CollectionUtils.isNotEmpty(newValue) && CollectionUtils.isEmpty(oldValue))) {
            return false;
        }

        //noinspection ConstantConditions
        return CollectionUtils.isEqualCollection(newValue, oldValue);
    }
}
