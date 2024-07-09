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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.combobox.ComboBox;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.PrefixSuffixLoaderSupport;

public abstract class AbstractComboBoxLoader<T extends ComboBox<?>> extends AbstractComponentLoader<T> {

    protected PrefixSuffixLoaderSupport prefixSuffixLoaderSupport;

    @Override
    public void initComponent() {
        super.initComponent();

        getPrefixSuffixLoaderSupport().createPrefixSuffixComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        getPrefixSuffixLoaderSupport().loadPrefixSuffixComponents();

        loadBoolean(element, "opened", resultComponent::setOpened);
        loadString(element, "pattern", resultComponent::setPattern);
        loadInteger(element, "pageSize", resultComponent::setPageSize);
        loadBoolean(element, "autoOpen", resultComponent::setAutoOpen);
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadBoolean(element, "allowCustomValue", resultComponent::setAllowCustomValue);
        loadString(element, "overlayWidth", resultComponent::setOverlayWidth);

        componentLoader().loadPlaceholder(resultComponent, element);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadOverlayClass(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
        componentLoader().loadAllowedCharPattern(resultComponent, element, context);
        componentLoader().loadAriaLabel(resultComponent, element);
    }

    protected PrefixSuffixLoaderSupport getPrefixSuffixLoaderSupport() {
        if (prefixSuffixLoaderSupport == null) {
            prefixSuffixLoaderSupport = applicationContext.getBean(PrefixSuffixLoaderSupport.class, context);
        }
        return prefixSuffixLoaderSupport;
    }
}
