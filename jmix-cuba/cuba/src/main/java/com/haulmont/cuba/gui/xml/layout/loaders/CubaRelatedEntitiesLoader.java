/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.RelatedEntities;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.sys.PropertyOption;
import io.jmix.ui.xml.layout.loader.RelatedEntitiesLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class CubaRelatedEntitiesLoader extends RelatedEntitiesLoader {

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(RelatedEntities.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadFocusable(resultComponent, element);
    }

    @Override
    protected void loadPropertyOption(Element routeElement) {
        String property = loadString(routeElement, "name")
                .orElseThrow(() ->
                        new GuiDevelopmentException("Name attribute for related entities property is not specified",
                                context, "componentId", resultComponent.getId()));

        String caption = loadResourceString(routeElement.attributeValue("caption"));
        String filterCaption = loadResourceString(routeElement.attributeValue("filterCaption"));
        String screen = routeElement.attributeValue("screen");

        if (StringUtils.isNotEmpty(screen)) {
            if (getWindowConfig().findWindowInfo(screen) == null) {
                throw new GuiDevelopmentException("Screen for custom route in related entities not found",
                        context, "componentId", resultComponent.getId());
            }
        }

        resultComponent.addPropertyOption(new PropertyOption(property, screen, caption, filterCaption));
    }
}
