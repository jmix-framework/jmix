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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractLayoutLoader<T extends Component & ThemableLayout & FlexComponent>
        extends AbstractContainerLoader<T> {

    @Override
    public void initComponent() {
        super.initComponent();
        createSubComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadThemableAttributes(resultComponent, element);
        componentLoader().loadFlexibleAttributes(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        loadThemeNames(resultComponent, element);

        loadSubComponentsAndExpand(resultComponent, element);
    }

    protected void loadThemeNames(ThemableLayout resultComponent, Element element) {
        loaderSupport.loadString(element, "themeNames")
                .ifPresent(themesString -> {
                    List<String> themeNames = split(themesString);

                    if (!themeNames.isEmpty()) {
                        // To unset the previous default spacing value
                        resultComponent.setSpacing(false);

                        resultComponent.getThemeList().addAll(themeNames);
                    }
                });
    }

    protected List<String> split(String names) {
        return Arrays.stream(names.split("[\\s,]+"))
                .filter(split -> !Strings.isNullOrEmpty(split))
                .toList();
    }
}
