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
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
interface StudioFacets {

    @StudioFacet(
            name = "DataLoadCoordinator",
            classFqn = "io.jmix.flowui.facet.ViewDataLoadCoordinator",
            category = "Facets",
            xmlElement = StudioXmlElements.DATA_LOAD_COORDINATOR,
            icon = "io/jmix/flowui/kit/meta/icon/facet/dataLoadCoordinator.svg",
            documentationLink = "%VERSION%/flow-ui/facets/dataLoadCoordinator.html",
            propertyGroups = StudioPropertyGroups.DataLoadCoordinatorDefaultProperties.class)
    void dataLoadCoordinator();

    @StudioFacet(
            name = "FragmentDataLoadCoordinator",
            classFqn = "io.jmix.flowui.facet.FragmentDataLoadCoordinator",
            category = "Facets",
            xmlElement = StudioXmlElements.FRAGMENT_DATA_LOAD_COORDINATOR,
            icon = "io/jmix/flowui/kit/meta/icon/facet/dataLoadCoordinator.svg",
            documentationLink = "%VERSION%/flow-ui/facets/dataLoadCoordinator.html",
            propertyGroups = StudioPropertyGroups.DataLoadCoordinatorDefaultProperties.class)
    void fragmentDataLoadCoordinator();

    @StudioFacet(
            name = "UrlQueryParameters",
            classFqn = "io.jmix.flowui.facet.UrlQueryParametersFacet",
            category = "Facets",
            xmlElement = StudioXmlElements.URL_QUERY_PARAMETERS,
            icon = "io/jmix/flowui/kit/meta/icon/facet/urlQueryParameters.svg",
            documentationLink = "%VERSION%/flow-ui/facets/urlQueryParameters.html",
            propertyGroups = StudioPropertyGroups.Id.class)
    void queryParameters();

    @StudioFacet(
            name = "Timer",
            classFqn = "io.jmix.flowui.facet.Timer",
            category = "Facets",
            xmlElement = StudioXmlElements.TIMER,
            icon = "io/jmix/flowui/kit/meta/icon/facet/timer.svg",
            documentationLink = "%VERSION%/flow-ui/facets/timer.html",
            propertyGroups = StudioPropertyGroups.TimerComponent.class)
    void timer();

    @StudioFacet(
            name = "Settings",
            classFqn = "io.jmix.flowui.facet.ViewSettingsFacet",
            category = "Facets",
            xmlElement = StudioXmlElements.SETTINGS,
            icon = "io/jmix/flowui/kit/meta/icon/facet/settings.svg",
            propertyGroups = StudioPropertyGroups.SettingsDefaultProperties.class)
    void settings();

    @StudioFacet(
            name = "FragmentSettings",
            classFqn = "io.jmix.flowui.facet.FragmentSettingsFacet",
            category = "Facets",
            xmlElement = StudioXmlElements.FRAGMENT_SETTINGS,
            icon = "io/jmix/flowui/kit/meta/icon/facet/settings.svg",
            propertyGroups = StudioPropertyGroups.SettingsDefaultProperties.class)
    void fragmentSettings();
}
