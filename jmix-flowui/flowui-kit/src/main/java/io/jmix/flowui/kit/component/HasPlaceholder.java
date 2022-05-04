package io.jmix.flowui.kit.component;

import com.google.common.base.Strings;
import com.vaadin.flow.component.HasElement;

import javax.annotation.Nullable;

public interface HasPlaceholder extends HasElement {

    String PLACEHOLDER_PROPERTY_NAME = "placeholder";

    @Nullable
    default String getPlaceholder() {
        return getElement().getProperty(PLACEHOLDER_PROPERTY_NAME);
    }

    default void setPlaceholder(@Nullable String placeholder) {
        getElement().setProperty(PLACEHOLDER_PROPERTY_NAME, Strings.nullToEmpty(placeholder));
    }
}
