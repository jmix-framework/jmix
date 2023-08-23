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

import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerBase;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;
import io.jmix.flowui.xml.layout.support.PrefixSuffixLoaderSupport;

public abstract class AbstractValuePickerLoader<T extends ValuePickerBase<?, ?>> extends AbstractComponentLoader<T> {

    protected DataLoaderSupport dataLoaderSupport;
    protected ActionLoaderSupport actionLoaderSupport;
    protected PrefixSuffixLoaderSupport prefixSuffixLoaderSupport;

    @Override
    public void initComponent() {
        super.initComponent();

        getPrefixSuffixLoaderSupport().createPrefixSuffixComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        if (resultComponent instanceof SupportsValueSource) {
            getDataLoaderSupport().loadData(((SupportsValueSource<?>) resultComponent), element);
        }

        getPrefixSuffixLoaderSupport().loadPrefixSuffixComponents();

        componentLoader().loadFormatter(resultComponent, element);
        componentLoader().loadPlaceholder(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadAutofocus(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadAriaLabel(resultComponent, element);

        getActionLoaderSupport().loadActions(resultComponent, element);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }

    protected PrefixSuffixLoaderSupport getPrefixSuffixLoaderSupport() {
        if (prefixSuffixLoaderSupport == null) {
            prefixSuffixLoaderSupport = applicationContext.getBean(PrefixSuffixLoaderSupport.class, context);
        }
        return prefixSuffixLoaderSupport;
    }
}
