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

import com.vaadin.flow.component.icon.Icon;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DropdownButton extends AbstractDropdownButton {

    protected static final String ATTRIBUTE_JMIX_ROLE_VALUE = "jmix-dropdown-button";

    protected Icon dropdownIcon = new Icon("lumo", "dropdown");

    public DropdownButton() {
        dropdownItem = getContent().addItem("");
        dropdownItem.add(dropdownIcon);
    }

    @Override
    protected JmixMenuBar initContent() {
        JmixMenuBar content = super.initContent();
        content.getElement().setAttribute(ATTRIBUTE_JMIX_ROLE_NAME, ATTRIBUTE_JMIX_ROLE_VALUE);

        return content;
    }

    @Override
    protected JmixMenuItem getDropdownItem() {
        return dropdownItem;
    }

    protected void updateDropdownIconSlot() {
        getDropdownItem().add(dropdownIcon);
    }

    @Override
    public void setText(String text) {
        getDropdownItem().setText(text);

        if (!explicitTitle) {
            setTitleInternal(text);
        }

        updateDropdownIconSlot();
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

    @Override
    public void setIcon(@Nullable Icon icon) {
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

    public void addThemeVariants(DropdownButtonVariant... variants) {
        List<String> variantsToAdd = Stream.of(variants)
                .map(DropdownButtonVariant::getVariantName)
                .collect(Collectors.toList());

        getThemeNames().addAll(variantsToAdd);
    }

    public void removeThemeVariants(DropdownButtonVariant... variants) {
        List<String> variantsToRemove = Stream.of(variants)
                .map(DropdownButtonVariant::getVariantName)
                .collect(Collectors.toList());

        getThemeNames().removeAll(variantsToRemove);
    }

    protected void updateIconSlot() {
        if (iconComponent != null) {
            getDropdownItem().addComponentAsFirst(iconComponent);
        }
    }
}
