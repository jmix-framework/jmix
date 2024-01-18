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

import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;
import io.jmix.flowui.xml.layout.support.PrefixSuffixLoaderSupport;

public class SelectLoader extends AbstractComponentLoader<JmixSelect<?>> {

    protected DataLoaderSupport dataLoaderSupport;
    protected PrefixSuffixLoaderSupport prefixSuffixLoaderSupport;

    @Override
    protected JmixSelect<?> createComponent() {
        return factory.create(JmixSelect.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        getPrefixSuffixLoaderSupport().createPrefixSuffixComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadItems(resultComponent, element);
        getDataLoaderSupport().loadData(resultComponent, element);
        getPrefixSuffixLoaderSupport().loadPrefixSuffixComponents();

        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadResourceString(element, "emptySelectionCaption", context.getMessageGroup(),
                resultComponent::setEmptySelectionCaption);
        loadBoolean(element, "emptySelectionAllowed", resultComponent::setEmptySelectionAllowed);

        componentLoader().loadPlaceholder(resultComponent, element);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadOverlayClass(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
        componentLoader().loadAriaLabel(resultComponent, element);
        componentLoader().loadDatatype(resultComponent, element);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }

    protected PrefixSuffixLoaderSupport getPrefixSuffixLoaderSupport() {
        if (prefixSuffixLoaderSupport == null) {
            prefixSuffixLoaderSupport = applicationContext.getBean(PrefixSuffixLoaderSupport.class, context);
        }
        return prefixSuffixLoaderSupport;
    }
}
