package io.jmix.flowui.component;


import io.jmix.flowui.data.SupportsValueSource;

public interface PickerComponent<V> extends SupportsValueSource<V>, HasActions, SupportsUserAction<V> {
}
