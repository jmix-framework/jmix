package io.jmix.flowui.component;

import com.vaadin.flow.component.HasElement;

import javax.annotation.Nullable;

// TODO: gg, Supports?
public interface HasRequired extends HasElement {

    String PROPERTY_REQUIRED = "required";

    default boolean isRequired() {
        return getElement().getProperty(PROPERTY_REQUIRED, false);
    }

    default void setRequired(boolean required) {
        getElement().setProperty(PROPERTY_REQUIRED, required);
    }

    @Nullable
    String getRequiredMessage();

    void setRequiredMessage(@Nullable String requiredMessage);
}
