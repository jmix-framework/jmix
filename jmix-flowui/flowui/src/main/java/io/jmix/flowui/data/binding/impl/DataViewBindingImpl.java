package io.jmix.flowui.data.binding.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Entity;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.EntityItems;
import io.jmix.flowui.data.binding.DataViewBinding;
import io.jmix.flowui.data.binding.SuspendableBinding;
import io.jmix.flowui.data.binding.SuspendableBindingAware;

import javax.annotation.Nullable;

import java.util.Collection;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class DataViewBindingImpl<C extends Component & HasDataView<V, ?, ?>, V>
        implements DataViewBinding<C, V>, SuspendableBindingAware {

    protected DataProvider<V, ?> dataProvider;
    protected C component;

    protected SuspendableBinding suspendableBinding;

    protected Registration componentValueChangeRegistration;
    protected Registration itemsChangeRegistration;

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

    @SuppressWarnings("unchecked")
    @Override
    public void bind() {
        if (dataProvider instanceof EntityItems && component instanceof HasValue) {
            this.componentValueChangeRegistration = addComponentValueChangeListener();
            this.itemsChangeRegistration = ((EntityItems<V>) dataProvider).addItemsChangeListener(this::onItemsChanged);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void onItemsChanged(EntityItems.ItemsChangeEvent<V> itemsChangeEvent) {
        // Almost all HasDataView components clears their value, every time
        // DataChangeEvent is fired (dataProvider.refreshAll()). We need to
        // return the previous value if it is possible and prevent
        // DataContext from changing the modified state.
        if (component instanceof HasValue) {
            Object value = UiComponentUtils.getValue(((HasValue) component));

            if (suspendableBinding != null) {
                suspendableBinding.suspend();
            }

            dataProvider.refreshAll();

            if (value != null && contains(itemsChangeEvent.getItems(), value)) {
                UiComponentUtils.setValue(((HasValue) component), value);
            }

            if (suspendableBinding != null) {
                suspendableBinding.resume();
            }
        } else {
            dataProvider.refreshAll();
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    protected boolean contains(List<V> items, Object value) {
        if (value instanceof Collection) {
            for (Object item : ((Collection<?>) value)) {
                if (!items.contains(item)) {
                    return false;
                }
            }
            return true;
        } else {
            return items.contains(value);
        }
    }

    @Override
    public void setSuspendableBinding(@Nullable SuspendableBinding suspendableBinding) {
        this.suspendableBinding = suspendableBinding;
    }
}
