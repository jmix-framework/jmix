/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.kit.meta;

import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;

import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;
import io.jmix.flowui.kit.meta.StudioXmlElements;
@StudioUiKit(studioClassloaderDependencies = "io.jmix.messagetemplates:jmix-messagetemplates-flowui-kit")
public interface StudioGrapesJsComponents {

    @StudioComponent(
            name = "GrapesJs",
            classFqn = "io.jmix.messagetemplatesflowui.kit.component.GrapesJs",
            category = "Components",
            xmlElement = StudioXmlElements.GRAPES_JS,
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioPropertyGroups.NoOptionSizedAddonComponentDefaultProperties.class,
                    StudioPropertyGroups.ReadOnly.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.POSITION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.POSITION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.READ_ONLY, type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE)
            }
    )
    GrapesJs grapesJs();
}
