package io.jmix.flowui.kit.component.usermenu;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@link UserMenuItem} component.
 */
public enum UserMenuItemVariant implements ThemeVariant {

    /**
     * Represents a variant that removes the checkmark area from the item.
     */
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
