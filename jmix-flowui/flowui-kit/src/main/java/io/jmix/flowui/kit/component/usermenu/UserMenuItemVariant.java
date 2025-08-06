package io.jmix.flowui.kit.component.usermenu;

import com.vaadin.flow.component.shared.ThemeVariant;

public enum UserMenuItemVariant implements ThemeVariant {

    NON_CHECKABLE("non-checkable");

    private final String variant;

    UserMenuItemVariant(String variant) {
        this.variant = variant;
    }

    /**
     * Gets the variant name.
     *
     * @return variant name
     */
    public String getVariantName() {
        return variant;
    }
}
