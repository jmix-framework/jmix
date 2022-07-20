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
import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioMainViewElements {

    @StudioElement(
            name = "NavigationBar",
            classFqn = "com.vaadin.flow.component.html.Div",
            xmlElement = "navigationBar",
            target = {"com.vaadin.flow.component.applayout.AppLayout"},
            icon = "io/jmix/flowui/kit/meta/icon/element/navigationBar.svg",
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
            target = {"com.vaadin.flow.component.applayout.AppLayout"}
    )
    Div drawerLayout();
}
