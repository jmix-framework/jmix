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

package com.haulmont.cuba.gui.components.data.value;

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.NestedDatasource;
import com.haulmont.cuba.gui.data.impl.AbstractDatasource;
import io.jmix.core.Entity;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.ValueSource;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * This ValueSource implementation is used by Collection value components like TokenList while binding
 * with CollectionDatasource.
 *
 * @param <V> value type
 */
@SuppressWarnings("unchecked")
public class LegacyCollectionDsValueSource<V extends Entity> implements ValueSource<Collection<V>>, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(LegacyCollectionDsValueSource.class);

    protected CollectionDatasource datasource;

    protected MetaPropertyPath metaPropertyPath;

    protected EventHub events = new EventHub();

    protected BindingState state = BindingState.INACTIVE;

    protected Metadata metadata;

    public LegacyCollectionDsValueSource(CollectionDatasource datasource) {
        checkNotNullArgument(datasource);

        this.datasource = datasource;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        metadata = applicationContext.getBean(Metadata.class);

        if (datasource instanceof NestedDatasource) {
            NestedDatasource nestedDs = (NestedDatasource) this.datasource;
            MetaProperty nestedDsProperty = nestedDs.getProperty();

            MetadataTools metadataTools = applicationContext.getBean(MetadataTools.class);

            MetaClass masterDsEntityClass = nestedDs.getMaster().getMetaClass();
            MetaPropertyPath masterDsMpp = metadataTools.resolveMetaPropertyPath(masterDsEntityClass,
                    nestedDsProperty.getName());

            if (masterDsMpp.getMetaProperty() != nestedDsProperty) {
                log.debug("Master Datasource property doesn't match with specified nested datasource property");
            } else {
                metaPropertyPath = masterDsMpp;
            }
        }

        datasource.addCollectionChangeListener(this::collectionChanged);
    }

    protected void collectionChanged(CollectionDatasource.CollectionChangeEvent e) {
        events.publish(ValueChangeEvent.class, new ValueChangeEvent(this, null, datasource.getItems()));
    }

    public CollectionDatasource getDatasource() {
        return datasource;
    }

    @Override
    public Collection<V> getValue() {
        return datasource.getItems();
    }

    @Override
    public void setValue(Collection<V> value) {
        if (metaPropertyPath == null) {
            setDatasourceValue(value);
        } else {
            setPropertyDatasourceValue(value);
        }
    }

    protected void setDatasourceValue(Collection<V> value) {
        Collection<V> oldValue = new ArrayList<>(datasource.getItems());
        if (equalCollections(oldValue, value)) {
            return;
        }

        datasource.mute();

        if (CollectionUtils.isNotEmpty(oldValue)) {
            oldValue.forEach(datasource::excludeItem);
        }

        if (CollectionUtils.isNotEmpty(value)) {
            value.forEach(datasource::includeItem);
        }

        datasource.unmute(CollectionDatasource.UnmuteEventsMode.FIRE_REFRESH_EVENT);

        events.publish(ValueChangeEvent.class, new ValueChangeEvent(this, oldValue, value));
    }

    protected void setPropertyDatasourceValue(Collection<V> value) {
        Collection<V> itemValue = EntityValues.getValueEx(getMaster().getItem(), metaPropertyPath.toPathString());
        Collection<V> oldValue = copyPropertyCollection(itemValue);

        if (equalCollections(oldValue, value)) {
            return;
        }

        updateMasterCollection(metaPropertyPath.getMetaProperty(), value);

        if (!canUpdateMasterRefs()) {
            setDatasourceValue(value);
            return;
        }

        datasource.mute();

        boolean modified = false;

        MetaProperty inverseProperty = getInverseProperty();
        if (CollectionUtils.isNotEmpty(value)) {
            for (V v : value) {
                if (CollectionUtils.isEmpty(oldValue) || !oldValue.contains(v)) {
                    EntityValues.setValue(v, inverseProperty.getName(), getMaster().getItem());
                    ((AbstractDatasource) datasource).getItemsToUpdate().add(v);
                    modified = true;
                }
            }
        }

        if (CollectionUtils.isNotEmpty(oldValue)) {
            for (V v : oldValue) {
                if (CollectionUtils.isEmpty(value) || !value.contains(v)) {
                    EntityValues.setValue(v, inverseProperty.getName(), null);
                    ((AbstractDatasource) datasource).getItemsToUpdate().add(v);
                    modified = true;
                }
            }
        }

        if (modified) {
            ((AbstractDatasource) datasource).setModified(true);
        }

        datasource.unmute(CollectionDatasource.UnmuteEventsMode.FIRE_REFRESH_EVENT);

        if (!equalCollections(oldValue, value)) {
            events.publish(ValueChangeEvent.class, new ValueChangeEvent(this, oldValue, value));
        }
    }

    @Override
    public Class<Collection<V>> getType() {
        return metaPropertyPath == null
                ? null
                : (Class<Collection<V>>) metaPropertyPath.getMetaProperty().getJavaType();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public BindingState getState() {
        if (metaPropertyPath == null) {
            return datasource.getState() == Datasource.State.VALID
                    ? BindingState.ACTIVE
                    : BindingState.INACTIVE;
        } else {
            Datasource master = ((NestedDatasource) datasource).getMaster();
            boolean masterValid = master.getState() == Datasource.State.VALID;
            boolean masterEntitySpecified = master.getItem() != null;
            boolean nestedValid = datasource.getState() == Datasource.State.VALID;

            return masterValid && masterEntitySpecified && nestedValid
                    ? BindingState.ACTIVE
                    : BindingState.INACTIVE;
        }
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<Collection<V>>> listener) {
        return events.subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return events.subscribe(StateChangeEvent.class, listener);
    }

    protected boolean canUpdateMasterRefs() {
        MetaPropertyPath mpp = getMaster().getMetaClass().getPropertyPath(metaPropertyPath.toPathString());
        if (mpp == null) {
            return false;
        }

        if (!mpp.getMetaProperty().getRange().getCardinality().isMany()) {
            return false;
        }

        MetaProperty inverseProperty = mpp.getMetaProperty().getInverse();
        if (inverseProperty == null
                || inverseProperty.getType() != MetaProperty.Type.ASSOCIATION
                || inverseProperty.getRange().getCardinality().isMany()) {
            return false;
        }

        ExtendedEntities extendedEntities = metadata.getExtendedEntities();

        Class invPropEntityClass = extendedEntities.getEffectiveClass(inverseProperty.getDomain());
        Class datasourceEntityClass = datasource.getMetaClass().getJavaClass();

        //noinspection unchecked
        return invPropEntityClass.isAssignableFrom(datasourceEntityClass);
    }

    protected Collection<V> copyPropertyCollection(Collection<V> propertyValue) {
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

    protected boolean equalCollections(Collection c1, Collection c2) {
        if (c1 == null) {
            return c2 == null;
        }
        if (c2 == null) {
            return false;
        }
        return CollectionUtils.isEqualCollection(c1, c2);
    }

    protected void updateMasterCollection(MetaProperty metaProperty, @Nullable Collection<V> newCollection) {
        if (newCollection == null) {
            EntityValues.setValue(getMaster().getItem(), metaProperty.getName(), null);
        } else {
            Collection<V> masterCollection;
            if (Set.class.isAssignableFrom(metaProperty.getJavaType())) {
                masterCollection = new LinkedHashSet(newCollection);
            } else {
                masterCollection = new ArrayList(newCollection);
            }
            EntityValues.setValue(getMaster().getItem(), metaProperty.getName(), masterCollection);
        }
    }

    protected MetaProperty getInverseProperty() {
        MetaPropertyPath mpp = getMaster().getMetaClass().getPropertyPath(metaPropertyPath.toPathString());
        if (mpp == null) {
            return null;
        }

        return mpp.getMetaProperty().getInverse();
    }

    protected Datasource getMaster() {
        return ((NestedDatasource) datasource).getMaster();
    }
}
