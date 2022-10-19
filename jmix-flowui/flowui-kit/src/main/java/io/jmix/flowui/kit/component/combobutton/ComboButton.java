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

package io.jmix.flowui.kit.component.combobutton;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasAction;
import io.jmix.flowui.kit.component.HasShortcutCombination;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.dropdownbutton.AbstractDropdownButton;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComboButton extends AbstractDropdownButton
        implements ClickNotifier<MenuItem>, HasAction, HasShortcutCombination {

    protected static final String ATTRIBUTE_JMIX_ROLE_VALUE = "jmix-combo-button-icon";

    protected JmixMenuItem buttonItem;
    protected Icon dropdownIcon = new Icon(VaadinIcon.CHEVRON_DOWN);

    protected ComboButtonActionSupport actionSupport;
    protected ShortcutRegistration shortcutRegistration;

    public ComboButton() {
        buttonItem = getContent().addItem("");
        dropdownItem = getContent().addItem(dropdownIcon);
    }

    @Override
    protected JmixMenuBar initContent() {
        JmixMenuBar content = super.initContent();
        content.getElement().setAttribute(ATTRIBUTE_JMIX_ROLE_NAME, ATTRIBUTE_JMIX_ROLE_VALUE);

        return content;
    }

    protected JmixMenuItem getButtonItem() {
        return buttonItem;
    }

    @Override
    protected JmixMenuItem getDropdownItem() {
        return dropdownItem;
    }

    @Override
    public void setText(String text) {
        getButtonItem().setText(text);

        if (!explicitTitle) {
            setTitleInternal(text);
        }

        updateIconSlot();
    }

    @Override
    public String getText() {
        return getButtonItem().getText();
    }

    @Override
    public void setWhiteSpace(WhiteSpace value) {
        getButtonItem().setWhiteSpace(value);
    }

    @Override
    public WhiteSpace getWhiteSpace() {
        return getButtonItem().getWhiteSpace();
    }

    @Override
    public void setIcon(@Nullable Icon icon) {
        if (icon != null && icon.getElement().isTextNode()) {
            throw new IllegalArgumentException(
                    "Text node can't be used as an icon.");
        }
        if (iconComponent != null) {
            getButtonItem().remove(iconComponent);
        }
        iconComponent = icon;

        updateIconSlot();
    }

    protected void updateIconSlot() {
        if (iconComponent != null) {
            getButtonItem().addComponentAsFirst(iconComponent);
        }
    }

    @Override
    public Registration addClickListener(ComponentEventListener<ClickEvent<MenuItem>> listener) {
        return getButtonItem().addClickListener(listener);
    }

    @Override
    public ShortcutRegistration addClickShortcut(Key key, KeyModifier... keyModifiers) {
        return getButtonItem().addClickShortcut(key, keyModifiers);
    }

    @Override
    public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
        getActionSupport().setAction(action, overrideComponentProperties);
    }

    @Nullable
    @Override
    public Action getAction() {
        return getActionSupport().getAction();
    }

    @Override
    public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        KeyCombination oldValue = getShortcutCombination();
        if (!Objects.equals(oldValue, shortcutCombination)) {
            if (shortcutRegistration != null) {
                shortcutRegistration.remove();
                shortcutRegistration = null;
            }

            if (shortcutCombination != null) {
                shortcutRegistration = addClickShortcut(shortcutCombination.getKey(),
                        shortcutCombination.getKeyModifiers());
            }
        }
    }

    @Nullable
    @Override
    public KeyCombination getShortcutCombination() {
        if (shortcutRegistration == null) {
            return null;
        }

        KeyModifier[] keyModifiers = shortcutRegistration.getModifiers().stream()
                .filter(key -> key instanceof KeyModifier)
                .map(key -> (KeyModifier) key)
                .toArray(KeyModifier[]::new);

        return KeyCombination.create(shortcutRegistration.getKey(), keyModifiers);
    }

    public void addThemeVariants(ComboButtonVariant... variants) {
        List<String> variantsToAdd = Stream.of(variants)
                .map(ComboButtonVariant::getVariantName)
                .collect(Collectors.toList());

        getThemeNames().addAll(variantsToAdd);
    }

    public void removeThemeVariants(ComboButtonVariant... variants) {
        List<String> variantsToRemove = Stream.of(variants)
                .map(ComboButtonVariant::getVariantName)
                .collect(Collectors.toList());

        getThemeNames().removeAll(variantsToRemove);
    }

    protected ComboButtonActionSupport getActionSupport() {
        if (actionSupport == null) {
            actionSupport = new ComboButtonActionSupport(this);
        }

        return actionSupport;
    }
}
