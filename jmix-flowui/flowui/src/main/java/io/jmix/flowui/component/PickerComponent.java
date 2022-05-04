package io.jmix.flowui.component;


import com.vaadin.flow.component.HasElement;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.SupportsUserAction;

public interface PickerComponent<V> extends SupportsValueSource<V>, HasActions, SupportsUserAction<V>, HasElement {
}
