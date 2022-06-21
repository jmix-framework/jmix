package io.jmix.flowui.data.binding.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Entity;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.data.EntityItems;
import io.jmix.flowui.data.binding.DataViewBinding;

import javax.annotation.Nullable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class DataViewBindingImpl<C extends Component & HasDataView<V, ?, ?>, V> implements DataViewBinding<C, V> {

    protected DataProvider<V, ?> dataProvider;
    protected C component;

    protected Registration componentValueChangeRegistration;

    public DataViewBindingImpl(C component,
                               DataProvider<V, ?> dataProvider) {
        checkNotNullArgument(dataProvider);
        checkNotNullArgument(component);

        this.dataProvider = dataProvider;
        this.component = component;
    }

    @Override
    public C getComponent() {
        return component;
    }

    @Override
    public DataProvider<V, ?> getDataProvider() {
        return dataProvider;
    }

    @Override
    public void bind() {
        if (dataProvider instanceof EntityItems && component instanceof HasValue) {
            this.componentValueChangeRegistration = addComponentValueChangeListener();
        }
    }

    @Override
    public void unbind() {
        if (this.componentValueChangeRegistration != null) {
            this.componentValueChangeRegistration.remove();
            this.componentValueChangeRegistration = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected Registration addComponentValueChangeListener() {
        if (component instanceof SupportsTypedValue) {
            ((SupportsTypedValue<?, ?, V, ?>) component).addTypedValueChangeListener(event ->
                    componentValueChanged(event.getValue()));
        } else if (component instanceof HasValue) {
            ((HasValue<?, V>) component).addValueChangeListener(event ->
                    componentValueChanged(event.getValue()));
        }
        return null;
    }

    protected void componentValueChanged(@Nullable V value) {
        // value could be List / Set / something else
        if (value instanceof Entity || value == null) {
            //noinspection unchecked
            EntityItems<V> entityItemsSource = (EntityItems<V>) this.dataProvider;
            entityItemsSource.setSelectedItem(value);
        }
    }
}
