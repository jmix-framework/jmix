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

import com.vaadin.flow.component.applayout.DrawerToggle;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.PrefixSuffixLoaderSupport;

import static com.vaadin.flow.dom.ElementConstants.ARIA_LABEL_PROPERTY_NAME;

public class DrawerToggleLoader extends AbstractComponentLoader<DrawerToggle> {

    protected PrefixSuffixLoaderSupport prefixSuffixLoaderSupport;

    @Override
    protected DrawerToggle createComponent() {
        return factory.create(DrawerToggle.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        getPrefixSuffixLoaderSupport().createPrefixSuffixComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        getLoaderSupport().loadResourceString(element, "ariaLabel",
                getComponentContext().getMessageGroup(), ariaLabel ->
                        resultComponent.getElement().setAttribute(ARIA_LABEL_PROPERTY_NAME, ariaLabel));
        getPrefixSuffixLoaderSupport().loadPrefixSuffixComponents();

        componentLoader().loadIcon(element, resultComponent::setIcon);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadAriaLabel(resultComponent, element);
    }

    protected PrefixSuffixLoaderSupport getPrefixSuffixLoaderSupport() {
        if (prefixSuffixLoaderSupport == null) {
            prefixSuffixLoaderSupport = applicationContext.getBean(PrefixSuffixLoaderSupport.class, context);
        }
        return prefixSuffixLoaderSupport;
    }
}
