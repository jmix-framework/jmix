package io.jmix.flowui.kit.component;

import com.vaadin.flow.component.HasValue;

import javax.annotation.Nullable;

public interface SupportsUserAction<V> {

    /**
     * Sets the value of this component as if the user had set it.
     * The user originated attribute is only used for the {@link HasValue.ValueChangeEvent}.
     *
     * @param value the new value
     */
    void setValueFromClient(@Nullable V value);
}
