/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.component.delegate.ValueBindingDelegate.ValueBindingChangeEvent;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.SupportsDataProvider;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.DataViewBinding;
import io.jmix.flowui.data.binding.SuspendableBinding;
import io.jmix.flowui.data.binding.SuspendableBindingAware;
import io.jmix.flowui.data.binding.impl.DataViewBindingImpl;
import io.jmix.flowui.data.items.ContainerDataProvider;
import io.jmix.flowui.data.items.EnumDataProvider;
import io.jmix.flowui.data.items.InMemoryDataProviderWrapper;
import io.jmix.flowui.kit.event.EventBus;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.lang.Nullable;

import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public abstract class AbstractDataViewDelegate<C extends Component
        & HasDataView<V, ?, ?> & HasListDataView<V, ?> & SupportsDataProvider<V>, V>
        extends AbstractComponentDelegate<C>
        implements ValueBindingChangeObserver<Object> {

    protected DataViewBinding<C, V> binding;

    protected Registration dataChangeRegistration;

    protected EventBus eventBus;

    public AbstractDataViewDelegate(C component) {
        super(component);
    }

    public void bind(@Nullable DataProvider<V, ?> dataProvider) {
        if (this.binding != null) {
            this.binding.unbind();
            this.binding = null;
        }

        unsubscribeDataChangeListener();

        if (dataProvider != null) {
            //noinspection unchecked
            this.binding = new DataViewBindingImpl<>(component,
                    dataProvider instanceof InMemoryDataProviderWrapper<?>
                            ? ((InMemoryDataProviderWrapper<V>) dataProvider).getDataProvider()
                            : dataProvider
            );
            this.binding.bind();

            subscribeDataChangeListener(dataProvider);
        }
    }

    @Nullable
    public DataProvider<V, ?> getDataProvider() {
        return binding != null ? binding.getDataProvider() : null;
    }

    @Override
    public void valueBindingChanged(ValueBindingChangeEvent<?> event) {
        if (binding instanceof SuspendableBindingAware
                && event.getValueBinding() instanceof SuspendableBinding) {
            ((SuspendableBindingAware) binding).setSuspendableBinding(((SuspendableBinding) event.getValueBinding()));
        }

        ValueSource<?> valueSource = event.getValueSource();
        if (valueSource instanceof EntityValueSource) {
            setupItems(((EntityValueSource<?, ?>) valueSource));
        }
    }

    protected void setupItems(EntityValueSource<?, ?> valueSource) {
        MetaPropertyPath propertyPath = valueSource.getMetaPropertyPath();
        MetaProperty metaProperty = propertyPath.getMetaProperty();

        Range range = metaProperty.getRange();
        if (range.isEnum()) {
            //noinspection unchecked
            setItems(range.asEnumeration().getJavaClass());
        }
    }

    public void setItems(CollectionContainer<V> container) {
        component.setItems(new ContainerDataProvider<>(container));
    }


    public void setItems(Class<V> itemsEnum) {
        checkNotNullArgument(itemsEnum);

        if (!itemsEnum.isEnum()
                || !EnumClass.class.isAssignableFrom(itemsEnum)) {
            throw new IllegalArgumentException(
                    String.format("Items class '%s' must be enumeration and implement %s",
                            itemsEnum, EnumClass.class.getSimpleName()));
        }

        //noinspection unchecked,rawtypes
        ((HasListDataView) component).setItems(new EnumDataProvider(itemsEnum));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Registration addDataRefreshListener(Consumer<DataChangeEvent.DataRefreshEvent<V>> listener) {
        return getEventBus().addListener(DataChangeEvent.DataRefreshEvent.class, (Consumer) listener);
    }

    protected void unsubscribeDataChangeListener() {
        if (dataChangeRegistration != null) {
            dataChangeRegistration.remove();
            dataChangeRegistration = null;
        }
    }

    protected void subscribeDataChangeListener(DataProvider<V, ?> dataProvider) {
        dataChangeRegistration = dataProvider.addDataProviderListener(this::onDataProviderDataChange);
    }

    protected void onDataProviderDataChange(DataChangeEvent<V> event) {
        getEventBus().fireEvent(event);
    }

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }
}
