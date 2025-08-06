package io.jmix.flowui.kit.component.usermenu;

import com.vaadin.flow.component.shared.ThemeVariant;

public enum UserMenuVariant implements ThemeVariant {

    NON_CHECKABLE("non-checkable"),
    TERTIARY("tertiary"),
    END_ALIGNED("end-aligned");

    private final String variant;

    UserMenuVariant(String variant) {
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
