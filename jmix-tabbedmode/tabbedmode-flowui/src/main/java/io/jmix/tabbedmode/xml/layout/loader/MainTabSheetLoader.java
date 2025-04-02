/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.xml.layout.loader;

import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import io.jmix.flowui.xml.layout.support.PrefixSuffixLoaderSupport;
import io.jmix.tabbedmode.component.tabsheet.JmixMainTabSheet;
import io.jmix.tabbedmode.component.workarea.TabbedViewsContainer;

public class MainTabSheetLoader extends AbstractComponentLoader<JmixMainTabSheet> {

    public static final String TAG = "tabbedContainer";

    protected ActionLoaderSupport actionLoaderSupport;
    protected PrefixSuffixLoaderSupport prefixSuffixLoaderSupport;

    @Override
    protected JmixMainTabSheet createComponent() {
        return factory.create(JmixMainTabSheet.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        getPrefixSuffixLoaderSupport().createPrefixSuffixComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        getActionLoaderSupport().loadActions(resultComponent, element);
        getPrefixSuffixLoaderSupport().loadPrefixSuffixComponents();

        loadEnum(element, TabbedViewsContainer.ContentSwitchMode.class,
                "contentSwitchMode", resultComponent::setContentSwitchMode);
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
