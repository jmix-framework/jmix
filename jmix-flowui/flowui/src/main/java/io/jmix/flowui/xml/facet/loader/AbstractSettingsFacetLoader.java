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

package io.jmix.flowui.xml.facet.loader;

import com.google.common.base.Strings;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.impl.FacetsImpl;
import io.jmix.flowui.xml.facet.FacetProvider;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.dom4j.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractSettingsFacetLoader extends AbstractFacetLoader<SettingsFacet<?>> {

    @Override
    public void loadFacet() {
        // for backward compatibility, should be removed in future releases
        if (facets instanceof FacetsImpl facetsImpl) {
            FacetProvider<SettingsFacet> provider = facetsImpl.getProvider(SettingsFacet.class);

            if (provider != null && context instanceof ComponentLoader.ComponentContext componentContext) {
                provider.loadFromXml(resultFacet, element, componentContext);
                return;
            }
        }

        loaderSupport.loadString(element, "id", resultFacet::setId);
        loaderSupport.loadBoolean(element, "auto", resultFacet::setAuto);

        loadComponents(element);
    }

    protected void loadComponents(Element element) {
        Map<String, Boolean> components = loadComponents(context, element);

        List<String> excludedIds = filterExcludedIds(components);
        resultFacet.addExcludedComponentIds(excludedIds.toArray(new String[0]));

        if (!resultFacet.isAuto()) {
            List<String> ids = filterIncludedIds(components);
            resultFacet.addComponentIds(ids.toArray(new String[0]));
        }
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

    protected Map<String, Boolean> loadComponents(ComponentLoader.Context context, Element root) {
        List<Element> components = root.elements("component");
        if (CollectionUtils.isEmpty(components)) {
            return Collections.emptyMap();
        }

        Map<String, Boolean> result = new HashMap<>(components.size());

        for (Element element : components) {
            String id = element.attributeValue("componentId");
            if (id == null) {
                throw new GuiDevelopmentException("Component in " + SettingsFacet.class.getSimpleName()
                        + " does not define an id", context);
            }
            String enabled = element.attributeValue("enabled");
            result.put(id, Strings.isNullOrEmpty(enabled) || Boolean.parseBoolean(enabled));
        }

        return result;
    }
}
