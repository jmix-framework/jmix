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

package io.jmix.ui.component.data.value;

import com.google.common.base.Joiner;
import org.springframework.context.ApplicationContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.EntityValues;
import org.springframework.context.ApplicationContextAware;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.model.*;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class ContainerValueSource<E extends JmixEntity, V> implements EntityValueSource<E, V>, ApplicationContextAware {

    protected final InstanceContainer<E> container;

    protected MetaPropertyPath metaPropertyPath;
    protected String property;

    protected BindingState state = BindingState.INACTIVE;

    protected EventHub events = new EventHub();

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

        MetadataTools metadataTools = (MetadataTools) applicationContext.getBean(MetadataTools.NAME);
        this.metaPropertyPath = metadataTools.resolveMetaPropertyPath(metaClass, property);

        this.container.addItemChangeListener(this::containerItemChanged);
        this.container.addItemPropertyChangeListener(this::containerItemPropertyChanged);

        @SuppressWarnings("unchecked")
        InstanceContainer<JmixEntity> parentCont = (InstanceContainer<JmixEntity>) container;

        for (int i = 1; i < this.metaPropertyPath.length(); i++) {
            MetaPropertyPath intermediatePath = new MetaPropertyPath(this.metaPropertyPath.getMetaClass(),
                    Arrays.copyOf(this.metaPropertyPath.getMetaProperties(), i));
            String pathToTarget = Joiner.on('.').join(
                    Arrays.copyOfRange(this.metaPropertyPath.getPath(), i, this.metaPropertyPath.length()));

            DataComponents dataComponents = applicationContext.getBean(DataComponents.class);
            @SuppressWarnings("unchecked")
            InstanceContainer<JmixEntity> propertyCont = dataComponents.createInstanceContainer(intermediatePath.getRangeJavaClass());

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
                    JmixEntity entity = (JmixEntity) event.getValue();
                    JmixEntity prevEntity = (JmixEntity) event.getPrevValue();
                    propertyCont.setItem(entity);

                    V prevValue = prevEntity != null ? EntityValues.getValueEx(prevEntity, pathToTarget) : null;
                    V value = entity != null ? EntityValues.getValueEx(entity, pathToTarget) : null;
                    events.publish(ValueChangeEvent.class,
                            new ValueChangeEvent<>(this, prevValue, value));
                }
            });

            if (i == this.metaPropertyPath.length() - 1) {
                propertyCont.addItemPropertyChangeListener(event -> {
                    if (Objects.equals(event.getProperty(), this.metaPropertyPath.getMetaProperty().getName())) {
                        events.publish(ValueChangeEvent.class,
                                new ValueChangeEvent<>(this, (V) event.getPrevValue(), (V) event.getValue()));
                    }
                });
            }

            parentCont = propertyCont;
        }
    }

    @Nullable
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
    public Subscription addInstanceChangeListener(Consumer<InstanceChangeEvent<E>> listener) {
        return events.subscribe(InstanceChangeEvent.class, (Consumer) listener);
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return events.subscribe(StateChangeEvent.class, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<V>> listener) {
        return events.subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    protected void setState(BindingState state) {
        if (this.state != state) {
            this.state = state;

            events.publish(StateChangeEvent.class, new StateChangeEvent(this,  BindingState.ACTIVE));
        }
    }

    @SuppressWarnings("unchecked")
    protected void containerItemChanged(InstanceContainer.ItemChangeEvent e) {
        if (e.getItem() != null) {
            setState(BindingState.ACTIVE);
        } else {
            setState(BindingState.INACTIVE);
        }

        events.publish(InstanceChangeEvent.class, new InstanceChangeEvent(this, e.getPrevItem(), e.getItem()));
    }

    @SuppressWarnings("unchecked")
    protected void containerItemPropertyChanged(InstanceContainer.ItemPropertyChangeEvent e) {
        if (Objects.equals(e.getProperty(), metaPropertyPath.toPathString())) {
            events.publish(ValueChangeEvent.class, new ValueChangeEvent<>(this, (V) e.getPrevValue(), (V) e.getValue()));
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
                    JmixEntity entity = (JmixEntity) v;
                    EntityValues.setValue(dataContext.merge(entity), inverseProperty.getName(), getItem());
                }
            }
        }

        if (CollectionUtils.isNotEmpty(oldValue)) {
            for (V v : oldValue) {
                if (CollectionUtils.isEmpty(newValue) || !newValue.contains(v)) {
                    JmixEntity entity = (JmixEntity) v;
                    EntityValues.setValue(dataContext.merge(entity), inverseProperty.getName(), null);
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
}
