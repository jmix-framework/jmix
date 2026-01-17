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
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
interface StudioMainViewComponents {
    @StudioComponent(
            name = "AppLayout",
            classFqn = "com.vaadin.flow.component.applayout.AppLayout",
            category = "Main View",
            xmlElement = "appLayout",
            icon = "io/jmix/flowui/kit/meta/icon/mainview/appLayout.svg",
            availablePlaceRegExp = "^mainView$",
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "drawerOpened", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "primarySection", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.applayout.AppLayout$Section",
                            setParameterFqn = "com.vaadin.flow.component.applayout.AppLayout$Section",
                            options = {"DRAWER", "NAVBAR"}),
            }
    )
    AppLayout appLayout();

    @StudioComponent(
            name = "UserIndicator",
            classFqn = "io.jmix.flowui.component.main.JmixUserIndicator",
            category = "Main View",
            xmlElement = "userIndicator",
            icon = "io/jmix/flowui/kit/meta/icon/mainview/userIndicator.svg",
            availablePlaceRegExp = "(^(mainView/appLayout)?((/drawerLayout)|(/navigationBar))$)" +
                    "|(^((mainView/appLayout)?((/drawerLayout)|(/navigationBar)))?(/hasComponents)*$)",
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "title",  type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    UserIndicator userIndicator();

    @StudioComponent(
            name = "MainView",
            xmlElement = "mainView",
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
            properties = {
                    @StudioProperty(xmlAttribute = "messagesGroup", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "title",  type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    VerticalLayout mainView();
}
