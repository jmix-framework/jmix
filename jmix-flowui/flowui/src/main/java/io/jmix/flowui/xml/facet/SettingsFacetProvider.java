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

import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.impl.SettingsFacetImpl;
import io.jmix.flowui.facet.settings.ViewSettingsComponentManager;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.sys.ViewControllerReflectionInspector;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("flowui_SettingsFacetProvider")
public class SettingsFacetProvider implements FacetProvider<SettingsFacet> {

    protected LoaderSupport loaderSupport;
    protected ViewControllerReflectionInspector reflectionInspector;
    protected ViewSettingsComponentManager settingsManager;
    protected UserSettingsCache userSettingsCache;

    public SettingsFacetProvider(LoaderSupport loaderSupport,
                                 ViewControllerReflectionInspector reflectionInspector,
                                 UserSettingsCache userSettingsCache,
                                 @Autowired(required = false) ViewSettingsComponentManager settingsManager) {
        this.loaderSupport = loaderSupport;
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
        return new SettingsFacetImpl(reflectionInspector, userSettingsCache, settingsManager);
    }

    @Override
    public String getFacetTag() {
        return "settings";
    }

    @Override
    public void loadFromXml(SettingsFacet facet, Element element, ComponentLoader.ComponentContext context) {
        loaderSupport.loadString(element, "id", facet::setId);
        loaderSupport.loadBoolean(element, "auto", facet::setAuto);
    }
}
