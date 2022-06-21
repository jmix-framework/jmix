package io.jmix.flowui.data;

import com.vaadin.flow.data.provider.DataProvider;

import javax.annotation.Nullable;

public interface SupportsDataProvider<V> {

    @Nullable
    DataProvider<V, ?> getDataProvider();
}
