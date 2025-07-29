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

package io.jmix.vaadincommercialcomponents.kit.meta;

import com.vaadin.flow.component.board.Board;
import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit(studioClassloaderDependencies = "io.jmix.flowui:vaadin-commercial-components-kit")
public interface StudioVaadinCommercialComponents {

    @StudioComponent(
            name = "Board",
            classFqn = "com.vaadin.flow.component.board.Board",
            category = "Vaadin Commercial",
            xmlElement = "board",
            xmlns = "http://jmix.io/schema/vaadin-commercial-components/ui",
            xmlnsAlias = "vcc",
            icon = "io/jmix/vaadincommercialcomponents/kit/meta/icon/component/board.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.POSITION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.POSITION),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE,
                            defaultValue = "100%")
            }
    )
    Board board();
}
