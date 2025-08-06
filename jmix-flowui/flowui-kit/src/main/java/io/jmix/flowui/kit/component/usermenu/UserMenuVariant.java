package io.jmix.flowui.kit.component.usermenu;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@link JmixUserMenu} component.
 */
public enum UserMenuVariant implements ThemeVariant {

    /**
     * Represents a variant that removes the checkmark area from all items.
     */
    NON_CHECKABLE("non-checkable"),

    /**
     * Represents a variant that makes the {@link JmixUserMenu}'s button appear in a tertiary style.
     */
    TERTIARY("tertiary"),

    /**
     * Represents a variant that aligns this component to the end of the parent.
     */
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
