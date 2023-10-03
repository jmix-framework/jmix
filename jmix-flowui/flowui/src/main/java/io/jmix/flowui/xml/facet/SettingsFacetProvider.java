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

package io.jmix.flowui.xml.facet;

import com.google.common.base.Strings;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.impl.SettingsFacetImpl;
import io.jmix.flowui.facet.settings.SettingsFacetUrlQueryParametersHelper;
import io.jmix.flowui.facet.settings.ViewSettingsComponentManager;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.sys.ViewControllerReflectionInspector;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.apache.commons.collections4.CollectionUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("flowui_SettingsFacetProvider")
public class SettingsFacetProvider implements FacetProvider<SettingsFacet> {

    protected LoaderSupport loaderSupport;
    protected SettingsFacetUrlQueryParametersHelper settingsHelper;
    protected ViewControllerReflectionInspector reflectionInspector;
    protected ViewSettingsComponentManager settingsManager;
    protected UserSettingsCache userSettingsCache;

    public SettingsFacetProvider(LoaderSupport loaderSupport,
                                 SettingsFacetUrlQueryParametersHelper settingsHelper,
                                 ViewControllerReflectionInspector reflectionInspector,
                                 @Autowired(required = false) UserSettingsCache userSettingsCache,
                                 @Autowired(required = false) ViewSettingsComponentManager settingsManager) {
        this.loaderSupport = loaderSupport;
        this.settingsHelper = settingsHelper;
        this.reflectionInspector = reflectionInspector;
        this.settingsManager = settingsManager;
        this.userSettingsCache = userSettingsCache;
    }

    @Override
    public Class<SettingsFacet> getFacetClass() {
        return SettingsFacet.class;
    }

    @Override
    public SettingsFacet create() {
        return new SettingsFacetImpl(settingsHelper, reflectionInspector, userSettingsCache, settingsManager);
    }

    @Override
    public String getFacetTag() {
        return "settings";
    }

    @Override
    public void loadFromXml(SettingsFacet facet, Element element, ComponentLoader.ComponentContext context) {
        loaderSupport.loadString(element, "id", facet::setId);
        loaderSupport.loadBoolean(element, "auto", facet::setAuto);

        Map<String, Boolean> components = loadComponents(context, element);

        List<String> excludedIds = filterExcludedIds(components);
        facet.addExcludedComponentIds(excludedIds.toArray(new String[0]));

        if (!facet.isAuto()) {
            List<String> ids = filterIncludedIds(components);
            facet.addComponentIds(ids.toArray(new String[0]));
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

    protected Map<String, Boolean> loadComponents(ComponentLoader.ComponentContext context, Element root) {
        List<Element> components = root.elements("component");
        if (CollectionUtils.isEmpty(components)) {
            return Collections.emptyMap();
        }

        Map<String, Boolean> result = new HashMap<>(components.size());

        for (Element element : components) {
            String id = element.attributeValue("id");
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
