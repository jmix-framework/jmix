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

import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.RuntimePropsDatasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import io.jmix.core.Entity;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.meta.EntityValueSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Objects;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class DatasourceValueSource<E extends Entity, V> implements EntityValueSource<E, V>, ApplicationContextAware {
    protected final Datasource<E> datasource;

    protected MetaPropertyPath metaPropertyPath;
    protected String property;

    protected BindingState state = BindingState.INACTIVE;

    protected EventHub events = new EventHub();

    protected boolean dataModelSecurityEnabled = true;

    public DatasourceValueSource(Datasource<E> datasource, String property) {
        checkNotNullArgument(datasource);
        checkNotNullArgument(property);

        this.datasource = datasource;
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
        MetaClass metaClass = datasource instanceof RuntimePropsDatasource ?
                ((RuntimePropsDatasource<E>) datasource).resolveCategorizedEntityClass() :
                datasource.getMetaClass();

        MetadataTools metadataTools = applicationContext.getBean(MetadataTools.class);
        this.metaPropertyPath = metadataTools.resolveMetaPropertyPath(metaClass, property);

        this.datasource.addStateChangeListener(this::datasourceStateChanged);
        this.datasource.addItemChangeListener(this::datasourceItemChanged);
        this.datasource.addItemPropertyChangeListener(this::datasourceItemPropertyChanged);

        if (datasource.getState() == Datasource.State.VALID) {
            setState(BindingState.ACTIVE);
        }
    }

    public void setState(BindingState state) {
        if (this.state != state) {
            this.state = state;

            events.publish(StateChangeEvent.class, new StateChangeEvent(this,  state));
        }
    }

    public Datasource getDatasource() {
        return datasource;
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return datasource.getMetaClass();
    }

    @Override
    public MetaPropertyPath getMetaPropertyPath() {
        return metaPropertyPath;
    }

    @Override
    public E getItem() {
        return datasource.getItem();
    }

    @Override
    public boolean isDataModelSecurityEnabled() {
        return dataModelSecurityEnabled;
    }

    @Override
    public V getValue() {
        E item = datasource.getItem();
        if (item != null) {
            return EntityValues.getValueEx(item, metaPropertyPath);
        }
        return null;
    }

    @Override
    public void setValue(Object value) {
        E item = datasource.getItem();
        if (item != null) {
            EntityValues.setValueEx(item, metaPropertyPath, value);
        }
    }

    @Override
    public boolean isReadOnly() {
        // todo what about security ?
        return metaPropertyPath.getMetaProperty().isReadOnly();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<V> getType() {
        return (Class<V>) metaPropertyPath.getMetaProperty().getJavaType();
    }

    @Override
    public BindingState getState() {
        if (datasource.getState() == Datasource.State.VALID
                && datasource.getItem() != null) {
            return BindingState.ACTIVE;
        }

        return BindingState.INACTIVE;
    }

    public boolean isModified() {
        return datasource.isModified();
    }

    public void setModified(boolean modified) {
        ((DatasourceImplementation) datasource).setModified(modified);
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

    @SuppressWarnings("unchecked")
    protected void datasourceItemChanged(Datasource.ItemChangeEvent e) {
        if (e.getItem() != null && datasource.getState() == Datasource.State.VALID) {
            setState(BindingState.ACTIVE);
        } else {
            setState(BindingState.INACTIVE);
        }

        events.publish(InstanceChangeEvent.class, new InstanceChangeEvent(this, e.getPrevItem(), e.getItem()));
    }

    protected void datasourceStateChanged(Datasource.StateChangeEvent<E> e) {
        if (e.getState() == Datasource.State.VALID) {
            setState(BindingState.ACTIVE);
        } else {
            setState(BindingState.INACTIVE);
        }
    }

    @SuppressWarnings("unchecked")
    protected void datasourceItemPropertyChanged(Datasource.ItemPropertyChangeEvent e) {
        if (Objects.equals(e.getProperty(), metaPropertyPath.toPathString())) {
            events.publish(ValueChangeEvent.class, new ValueChangeEvent<>(this, (V)e.getPrevValue(), (V)e.getValue()));
        }
    }
}
