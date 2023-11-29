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

package io.jmix.flowui.kit.meta.element;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.meta.*;

@StudioUiKit
public interface StudioMainViewElements {

    @StudioElement(
            name = "NavigationBar",
            classFqn = "com.vaadin.flow.component.html.Div",
            xmlElement = "navigationBar",
            target = {"com.vaadin.flow.component.applayout.AppLayout"},
            icon = "io/jmix/flowui/kit/meta/icon/element/navigationBar.svg",
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "touchOptimized", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    Div navigationBar();

    @StudioElement(
            name = "DrawerLayout",
            classFqn = "com.vaadin.flow.component.html.Div",
            xmlElement = "drawerLayout",
            icon = "io/jmix/flowui/kit/meta/icon/element/drawerLayout.svg",
            unlimitedCount = false,
            target = {"com.vaadin.flow.component.applayout.AppLayout"}
    )
    Div drawerLayout();

    @StudioElement(
            name = "InitialLayout",
            classFqn = "com.vaadin.flow.component.orderedlayout.VerticalLayout",
            xmlElement = "initialLayout",
            icon = "io/jmix/flowui/kit/meta/icon/layout/vbox.svg",
            target = {"com.vaadin.flow.component.applayout.AppLayout"},
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "alignItems", type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "STRETCH",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "boxSizing", type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            classFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            defaultValue = "UNDEFINED",
                            options = {"UNDEFINED", "CONTENT_BOX", "BORDER_BOX"}),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "expand", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "justifyContent", type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                            setMethod = "setJustifyContentMode", defaultValue = "START",
                            options = {"START", "END", "CENTER", "BETWEEN", "AROUND", "EVENLY"}),
                    @StudioProperty(xmlAttribute = "margin", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "padding", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "spacing", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"spacing-xs", "spacing-s", "spacing", "spacing-l", "spacing-xl"}),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE, defaultValue = "100%")
            }
    )
    VerticalLayout initialLayout();
}
