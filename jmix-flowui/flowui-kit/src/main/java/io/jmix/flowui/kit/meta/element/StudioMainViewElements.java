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

package io.jmix.flowui.kit.meta.element;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.meta.*;

import static io.jmix.flowui.kit.meta.StudioComponent.EMPTY_INJECTION_IDENTIFIER;

@StudioUiKit
interface StudioMainViewElements {

    @StudioElement(
            name = "NavigationBar",
            classFqn = "com.vaadin.flow.component.html.Div",
            xmlElement = StudioXmlElements.NAVIGATION_BAR,
            target = {"com.vaadin.flow.component.applayout.AppLayout"},
            icon = "io/jmix/flowui/kit/meta/icon/element/navigationBar.svg",
            isInjectable = false,
            injectionIdentifier = EMPTY_INJECTION_IDENTIFIER,
            unlimitedCount = false,
            visible = true,
            propertyGroups = StudioPropertyGroups.NavigationBarComponent.class)
    Div navigationBar();

    @StudioElement(
            name = "DrawerLayout",
            classFqn = "com.vaadin.flow.component.html.Div",
            xmlElement = StudioXmlElements.DRAWER_LAYOUT,
            icon = "io/jmix/flowui/kit/meta/icon/element/drawerLayout.svg",
            isInjectable = false,
            injectionIdentifier = EMPTY_INJECTION_IDENTIFIER,
            unlimitedCount = false,
            visible = true,
            target = {"com.vaadin.flow.component.applayout.AppLayout"},
            propertyGroups = {
                    StudioPropertyGroups.Css.class
            })
    Div drawerLayout();

    @StudioElement(
            name = "InitialLayout",
            classFqn = "com.vaadin.flow.component.orderedlayout.VerticalLayout",
            xmlElement = StudioXmlElements.INITIAL_LAYOUT,
            icon = "io/jmix/flowui/kit/meta/icon/layout/vbox.svg",
            target = {"com.vaadin.flow.component.applayout.AppLayout", "io.jmix.tabbedmode.component.workarea.WorkArea"},
            unlimitedCount = false,
            visible = true,
            propertyGroups = StudioPropertyGroups.InitialLayoutDefaultProperties.class)
    VerticalLayout initialLayout();
}
