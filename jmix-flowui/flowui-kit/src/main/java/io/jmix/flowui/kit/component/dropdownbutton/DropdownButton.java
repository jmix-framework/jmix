/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui.kit.component.dropdownbutton;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.shared.HasThemeVariant;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import org.jspecify.annotations.Nullable;

/**
 * Represents a dropdown button component. This class is used as a customizable
 * button with an associated dropdown menu.
 */
public class DropdownButton extends AbstractDropdownButton implements HasThemeVariant<DropdownButtonVariant> {

    protected static final String ATTRIBUTE_JMIX_ROLE_VALUE = "jmix-dropdown-button";

    protected boolean dropdownIndicatorVisible = true;

    public DropdownButton() {
        dropdownItem = createDropdownItem();
    }

    protected JmixMenuItem createDropdownItem() {
        return getContent().addItem("");
    }

    @Override
    protected JmixMenuBar initContent() {
        JmixMenuBar content = super.initContent();
        content.getElement().setAttribute(ATTRIBUTE_JMIX_ROLE_NAME, ATTRIBUTE_JMIX_ROLE_VALUE);

        updateDropdownIndicator();

        return content;
    }

    @Override
    protected JmixMenuItem getDropdownItem() {
        return dropdownItem;
    }

    protected void updateDropdownIndicator() {
        if (dropdownIndicatorVisible) {
            removeThemeVariants(DropdownButtonVariant.AURA_NO_DROPDOWN_INDICATORS);
            addThemeVariants(DropdownButtonVariant.LUMO_DROPDOWN_INDICATORS);
        } else {
            addThemeVariants(DropdownButtonVariant.AURA_NO_DROPDOWN_INDICATORS);
            removeThemeVariants(DropdownButtonVariant.LUMO_DROPDOWN_INDICATORS);
        }
    }

    @Override
    public void setText(String text) {
        getDropdownItem().setText(text);

        updateIconSlot();
    }

    @Override
    public String getText() {
        return getDropdownItem().getText();
    }

    @Override
    public void setWhiteSpace(WhiteSpace value) {
        getDropdownItem().setWhiteSpace(value);
    }

    @Override
    public WhiteSpace getWhiteSpace() {
        return getDropdownItem().getWhiteSpace();
    }

    @Deprecated(since = "3.0", forRemoval = true)
    @Override
    public void setIconComponent(@Nullable Component icon) {
        if (icon != null && icon.getElement().isTextNode()) {
            throw new IllegalArgumentException(
                    "Text node can't be used as an icon.");
        }
        if (iconComponent != null) {
            getDropdownItem().remove(iconComponent);
        }
        iconComponent = icon;

        updateIconSlot();
    }

    /**
     * Returns whether if the dropdown indicator is currently visible.
     *
     * @return {@code true} if the dropdown indicator is visible, {@code false} otherwise
     * @deprecated use {@link DropdownButtonVariant#LUMO_DROPDOWN_INDICATORS}
     * and {@link DropdownButtonVariant#AURA_NO_DROPDOWN_INDICATORS} instead
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public boolean isDropdownIndicatorVisible() {
        return dropdownIndicatorVisible;
    }

    /**
     * Sets the visibility of the dropdown indicator.
     *
     * @param dropdownIndicatorVisible {@code true} to display the indicator,
     *                                 or {@code false} to hide it.
     * @deprecated use {@link DropdownButtonVariant#LUMO_DROPDOWN_INDICATORS}
     * and {@link DropdownButtonVariant#AURA_NO_DROPDOWN_INDICATORS} instead
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public void setDropdownIndicatorVisible(boolean dropdownIndicatorVisible) {
        if (this.dropdownIndicatorVisible != dropdownIndicatorVisible) {
            this.dropdownIndicatorVisible = dropdownIndicatorVisible;

            updateDropdownIndicator();
        }
    }

    protected void updateIconSlot() {
        if (iconComponent != null) {
            getDropdownItem().addComponentAsFirst(iconComponent);
        }
    }
}
