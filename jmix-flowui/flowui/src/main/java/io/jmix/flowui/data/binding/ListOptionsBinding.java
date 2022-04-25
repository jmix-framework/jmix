package io.jmix.flowui.data.binding;

import com.vaadin.flow.data.provider.HasListDataView;

import java.util.Collection;

public interface ListOptionsBinding<V> extends OptionsBinding<V> {

    @Override
    HasListDataView<V, ?> getComponent();

    @FunctionalInterface
    interface ListOptionsTarget<V> {
        void setOptions(Collection<V> options);
    }
}
