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

package io.jmix.flowui.kit.meta.component;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.component.main.UserIndicator;
import io.jmix.flowui.kit.meta.StudioAvailableChildrenInfo;
import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
interface StudioMainViewComponents {

    @StudioComponent(
            name = "AppLayout",
            classFqn = "com.vaadin.flow.component.applayout.AppLayout",
            category = "Main View",
            xmlElement = StudioXmlElements.APP_LAYOUT,
            icon = "io/jmix/flowui/kit/meta/icon/mainview/appLayout.svg",
            availablePlaceRegExp = "^mainView$",
            propertyGroups = StudioPropertyGroups.AppLayoutComponent.class)
    AppLayout appLayout();

    @StudioComponent(
            name = "UserIndicator",
            classFqn = "io.jmix.flowui.component.main.JmixUserIndicator",
            category = "Main View",
            xmlElement = StudioXmlElements.USER_INDICATOR,
            icon = "io/jmix/flowui/kit/meta/icon/mainview/userIndicator.svg",
            availablePlaceRegExp = "(^(mainView/appLayout)?((/drawerLayout)|(/navigationBar))$)" +
                    "|(^((mainView/appLayout)?((/drawerLayout)|(/navigationBar)))?(/hasComponents)*$)",
            propertyGroups = StudioPropertyGroups.UserIndicatorDefaultProperties.class)
    UserIndicator userIndicator();

    @StudioComponent(
            name = "MainView",
            xmlElement = StudioXmlElements.MAIN_VIEW,
            classFqn = "io.jmix.flowui.app.main.StandardMainView",
            icon = "io/jmix/flowui/kit/meta/icon/mainview/mainView.svg",
            availablePlaceRegExp = "",
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "data", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "facets", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "actions", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "appLayout", maxCount = 1)
                    }
            ),
            propertyGroups = StudioPropertyGroups.MessagesGroupAndTitle.class)
    VerticalLayout mainView();
}
