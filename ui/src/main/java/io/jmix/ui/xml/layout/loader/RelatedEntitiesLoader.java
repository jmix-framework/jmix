/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.RelatedEntities;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.sys.PropertyOption;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class RelatedEntitiesLoader extends AbstractComponentLoader<RelatedEntities> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(RelatedEntities.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        loadCaption(resultComponent, element);
        loadIcon(resultComponent, element);
        loadWidth(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadEnable(resultComponent, element);
        loadVisible(resultComponent, element);
        loadAlign(resultComponent, element);
        loadCss(resultComponent, element);

        loadOpenMode(resultComponent, element);

        loadString(element, "exclude", resultComponent::setExcludePropertiesRegex);

        for (Element routeObject : element.elements("property")) {
            loadPropertyOption(routeObject);
        }

        String listComponentId = loadString(element, "for")
                .orElseThrow(() ->
                        new GuiDevelopmentException("'for' attribute of related entities is not specified",
                                context, "componentId", resultComponent.getId()));

        getComponentContext().addPostInitTask((context1, window) -> {
            if (resultComponent.getListComponent() == null) {
                Component bindComponent = resultComponent.getFrame().getComponent(listComponentId);
                if (!(bindComponent instanceof ListComponent)) {
                    throw new GuiDevelopmentException("Specify 'for' attribute: id of table or tree",
                            context1, "componentId", resultComponent.getId());
                }

                resultComponent.setListComponent((ListComponent) bindComponent);
            }
        });

        loadTabIndex(resultComponent, element);
    }

    protected void loadOpenMode(RelatedEntities resultComponent, Element element) {
        String openMode = element.attributeValue("openMode");
        if (StringUtils.isNotEmpty(openMode)) {
            resultComponent.setOpenMode(OpenMode.valueOf(openMode));
        }
    }

    protected void loadPropertyOption(Element routeElement) {
        String property = loadString(routeElement, "name")
                .orElseThrow(() ->
                        new GuiDevelopmentException("Name attribute for related entities property is not specified",
                                context, "componentId", resultComponent.getId()));

        String caption = loadResourceString(routeElement.attributeValue("caption"));
        String configurationName = loadResourceString(routeElement.attributeValue("configurationName"));
        String screen = routeElement.attributeValue("screen");

        if (StringUtils.isNotEmpty(screen)) {
            if (getWindowConfig().findWindowInfo(screen) == null) {
                throw new GuiDevelopmentException("Screen for custom route in related entities not found",
                        context, "componentId", resultComponent.getId());
            }
        }

        resultComponent.addPropertyOption(new PropertyOption(property, caption, configurationName, screen));
    }

    protected WindowConfig getWindowConfig() {
        return applicationContext.getBean(WindowConfig.class);
    }
}
