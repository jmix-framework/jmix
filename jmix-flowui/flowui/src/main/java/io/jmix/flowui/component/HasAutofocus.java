package io.jmix.flowui.component;

import com.vaadin.flow.component.HasElement;

public interface HasAutofocus extends HasElement {

    String AUTOFOCUS_PROPERTY_NAME = "autofocus";

    default boolean isAutofocus() {
        return getElement().getProperty(AUTOFOCUS_PROPERTY_NAME, false);
    }

    default void setAutofocus(boolean autofocus) {
        getElement().setProperty(AUTOFOCUS_PROPERTY_NAME, autofocus);
    }
}
