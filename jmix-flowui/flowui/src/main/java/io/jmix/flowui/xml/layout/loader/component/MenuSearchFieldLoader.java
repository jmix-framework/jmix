/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.menusearchfield.MenuSearchField;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.menu.provider.HasMenuItemProvider;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.PrefixSuffixLoaderSupport;

import java.util.Optional;

public class MenuSearchFieldLoader extends AbstractComponentLoader<MenuSearchField> {

    protected PrefixSuffixLoaderSupport prefixSuffixLoaderSupport;

    @Override
    protected MenuSearchField createComponent() {
        return factory.create(MenuSearchField.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        getPrefixSuffixLoaderSupport().createPrefixSuffixComponents(resultComponent, element);
    }

    protected PrefixSuffixLoaderSupport getPrefixSuffixLoaderSupport() {
        if (prefixSuffixLoaderSupport == null) {
            prefixSuffixLoaderSupport = applicationContext.getBean(PrefixSuffixLoaderSupport.class, context);
        }
        return prefixSuffixLoaderSupport;
    }

    @Override
    public void loadComponent() {
        getPrefixSuffixLoaderSupport().loadPrefixSuffixComponents();

        loadBoolean(element, "autoselect", resultComponent::setAutoselect);
        loadBoolean(element, "clearButtonVisible", resultComponent::setClearButtonVisible);

        componentLoader().loadAutofocus(resultComponent, element);
        componentLoader().loadPlaceholder(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadAutocapitalize(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueChangeMode(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadAriaLabel(resultComponent, element);

        String menuId = loadString(element, "menu")
                .orElseThrow(() ->
                        new GuiDevelopmentException("Menu id is required for menu search field component", context));

        Optional<String> valueOptional = loadString(element, "value");

        getComponentContext().addInitTask((context, view) -> {
            Component menuComponent = UiComponentUtils.findComponent(view, menuId).orElse(null);

            if (!(menuComponent instanceof HasMenuItemProvider<?> hasMenuItemProvider)) {
                throw new GuiDevelopmentException("Failed to find a menu with item provider", context, "Menu", menuId);
            }
            resultComponent.setMenuItemProvider(hasMenuItemProvider.getMenuItemProvider());

            valueOptional.ifPresent(resultComponent::setValue);
        });
    }
}
