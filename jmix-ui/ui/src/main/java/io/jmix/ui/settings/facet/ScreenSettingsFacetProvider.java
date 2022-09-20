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

package io.jmix.ui.settings.facet;

import com.google.common.base.Strings;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.xml.FacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;

import java.util.*;
import java.util.stream.Collectors;

@Internal
@org.springframework.stereotype.Component("ui_ScreenSettingsFacetProvider")
public class ScreenSettingsFacetProvider implements FacetProvider<ScreenSettingsFacet> {

    @Override
    public Class<ScreenSettingsFacet> getFacetClass() {
        return ScreenSettingsFacet.class;
    }

    @Override
    public ScreenSettingsFacet create() {
        return new ScreenSettingsFacetImpl();
    }

    @Override
    public String getFacetTag() {
        return "screenSettings";
    }

    @Override
    public void loadFromXml(ScreenSettingsFacet facet, Element element, ComponentLoader.ComponentContext context) {
        loadId(element).ifPresent(facet::setId);

        loadAuto(element).ifPresent(facet::setAuto);

        Map<String, Boolean> components = loadComponents(context, element);

        List<String> excludedIds = filterExcludedIds(components);
        facet.excludeComponentIds(excludedIds.toArray(new String[0]));

        if (!facet.isAuto()) {
            List<String> ids = filterIncludedIds(components);
            facet.addComponentIds(ids.toArray(new String[0]));
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

    protected List<String> filterIncludedIds(Map<String, Boolean> components) {
        return components.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    protected List<String> filterExcludedIds(Map<String, Boolean> components) {
        return components.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    protected Map<String, Boolean> loadComponents(ComponentLoader.ComponentContext context, Element root) {
        Element componentsElement = root.element("components");
        if (componentsElement == null) {
            return Collections.emptyMap();
        }

        List<Element> components = componentsElement.elements("component");
        Map<String, Boolean> result = new HashMap<>(components.size());

        for (Element element : components) {
            String id = element.attributeValue("id");
            if (id == null) {
                throw new GuiDevelopmentException("ScreenSettings component does not define an id", context);
            }
            String enabled = element.attributeValue("enabled");
            result.put(id, Strings.isNullOrEmpty(enabled) || Boolean.parseBoolean(enabled));
        }

        return result;
    }
}
