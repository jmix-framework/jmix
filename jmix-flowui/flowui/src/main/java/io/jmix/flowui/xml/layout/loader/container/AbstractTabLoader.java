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

package io.jmix.flowui.xml.layout.loader.container;

import com.vaadin.flow.component.tabs.Tab;
import org.dom4j.Element;

public abstract class AbstractTabLoader extends AbstractContainerLoader<Tab>{

    @Override
    public void loadComponent() {
        loadDouble(element, "flexGrow", resultComponent::setFlexGrow);

        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadAriaLabel(resultComponent, element);

        loadSubComponents();
    }

    @Override
    protected boolean isChildElementIgnored(Element subElement) {
        return "tooltip".equals(subElement.getName());
    }
}
