/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.presentation.facet;

import com.google.common.base.Strings;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.presentation.PresentationsManager;
import io.jmix.ui.xml.FacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Internal
@org.springframework.stereotype.Component("ui_PresentationsFacetProvider")
public class PresentationsFacetProvider implements FacetProvider<PresentationsFacet> {

    protected PresentationsManager presentationsManager;

    public PresentationsFacetProvider(@Autowired(required = false) PresentationsManager presentationsManager) {
        this.presentationsManager = presentationsManager;
    }

    @Override
    public Class<PresentationsFacet> getFacetClass() {
        return PresentationsFacet.class;
    }

    @Override
    public PresentationsFacet create() {
        return new PresentationsFacetImpl();
    }

    @Override
    public String getFacetTag() {
        return "presentations";
    }

    @Override
    public void loadFromXml(PresentationsFacet facet, Element element, ComponentLoader.ComponentContext context) {
        loadId(element).ifPresent(facet::setId);

        loadAuto(element).ifPresent(facet::setAuto);

        if (!facet.isAuto()) {
            List<String> ids = loadComponentIds(context, element);
            facet.addComponentIds(ids.toArray(new String[0]));
        }

        if (isPresentationsAvailable()) {
            context.addPostInitTask((context1, window) -> {
                Collection<Component> components = facet.getComponents();
                for (Component component : components) {
                    if (component instanceof HasTablePresentations) {
                        ((HasTablePresentations) component).loadPresentations();
                    }
                }
            });
        }
    }

    protected Optional<String> loadId(Element element) {
        String id = element.attributeValue("id");

        return Optional.ofNullable(id);
    }

    protected Optional<Boolean> loadAuto(Element element) {
        String auto = element.attributeValue("auto");
        if (!Strings.isNullOrEmpty(auto)) {
            return Optional.of(Boolean.parseBoolean(auto));
        }

        return Optional.empty();
    }

    protected List<String> loadComponentIds(ComponentLoader.ComponentContext context, Element root) {
        Element componentsElement = root.element("components");
        if (componentsElement == null) {
            return Collections.emptyList();
        }

        List<Element> components = componentsElement.elements("component");
        List<String> result = new ArrayList<>(components.size());

        for (Element element : components) {
            String id = element.attributeValue("id");
            if (id == null) {
                throw new GuiDevelopmentException("ScreenSettings component does not define an id", context);
            }

            result.add(id);
        }

        return result;
    }

    protected boolean isPresentationsAvailable() {
        return presentationsManager != null;
    }
}
