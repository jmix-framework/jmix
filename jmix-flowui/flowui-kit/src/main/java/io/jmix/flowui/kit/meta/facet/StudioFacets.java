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

package io.jmix.flowui.kit.meta.facet;

import io.jmix.flowui.kit.meta.StudioFacet;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioFacets {

    @StudioFacet(
            name = "DataLoadCoordinator",
            classFqn = "io.jmix.flowui.facet.DataLoadCoordinator",
            category = "Facets",
            xmlElement = "dataLoadCoordinator",
            icon = "io/jmix/flowui/kit/meta/icon/facet/dataLoadCoordinator.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "auto", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false", initialValue = "true"),
                    @StudioProperty(xmlAttribute = "componentPrefix", type = StudioPropertyType.STRING,
                            defaultValue = "component_"),
                    @StudioProperty(xmlAttribute = "containerPrefix", type = StudioPropertyType.STRING,
                            defaultValue = "container_"),
            }
    )
    void dataLoadCoordinator();

    @StudioFacet(
            name = "UrlQueryParameters",
            classFqn = "io.jmix.flowui.facet.UrlQueryParametersFacet",
            category = "Facets",
            xmlElement = "urlQueryParameters",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
            }
    )
    void queryParameters();

    @StudioFacet(
            name = "Timer",
            classFqn = "io.jmix.flowui.facet.Timer",
            category = "Facets",
            xmlElement = "timer",
            icon = "io/jmix/flowui/kit/meta/icon/facet/timer.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "delay", type = StudioPropertyType.INTEGER, required = true),
                    @StudioProperty(xmlAttribute = "repeating", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autostart", type = StudioPropertyType.BOOLEAN, defaultValue = "false")
            }
    )
    void timer();

    @StudioFacet(
            name = "Settings",
            classFqn = "io.jmix.flowui.facet.SettingsFacet",
            category = "Facet",
            xmlElement = "settings",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "auto", type = StudioPropertyType.BOOLEAN)
            }
    )
    void settings();
}
