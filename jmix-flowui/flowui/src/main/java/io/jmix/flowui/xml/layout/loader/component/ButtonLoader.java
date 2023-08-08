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

import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.xml.layout.inittask.AssignActionInitTask;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.PrefixSuffixLoaderSupport;
import org.dom4j.Element;

public class ButtonLoader extends AbstractComponentLoader<JmixButton> {

    protected PrefixSuffixLoaderSupport prefixSuffixLoaderSupport;

    @Override
    protected JmixButton createComponent() {
        return factory.create(JmixButton.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        getPrefixSuffixLoaderSupport().createPrefixSuffixComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        getPrefixSuffixLoaderSupport().loadPrefixSuffixComponents();

        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadBoolean(element, "iconAfterText", resultComponent::setIconAfterText);
        loadBoolean(element, "disableOnClick", resultComponent::setDisableOnClick);

        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadText(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadIcon(element, resultComponent::setIcon);
        componentLoader().loadWhiteSpace(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadAriaLabel(resultComponent, element);

        loadAction(resultComponent, element);
    }

    protected void loadAction(JmixButton component, Element element) {
        loadString(element, "action")
                .ifPresent(actionId -> getComponentContext().addInitTask(
                        new AssignActionInitTask<>(component, actionId, getComponentContext().getView())
                ));
    }

    protected PrefixSuffixLoaderSupport getPrefixSuffixLoaderSupport() {
        if (prefixSuffixLoaderSupport == null) {
            prefixSuffixLoaderSupport = applicationContext.getBean(PrefixSuffixLoaderSupport.class, context);
        }
        return prefixSuffixLoaderSupport;
    }
}
