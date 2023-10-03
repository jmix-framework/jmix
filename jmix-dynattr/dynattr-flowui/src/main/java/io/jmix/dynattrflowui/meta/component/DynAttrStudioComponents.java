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

package io.jmix.dynattrflowui.meta.component;

import io.jmix.dynattrflowui.panel.DynamicAttributesPanel;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface DynAttrStudioComponents {
    @StudioComponent(
            name = "DynamicAttributesPanel",
            classFqn = "io.jmix.dynattrflowui.panel.DynamicAttributesPanel",
            category = "Components",
            xmlElement = "dynamicAttributesPanel",
            xmlns = "http://jmix.io/schema/dynattr/ui",
            xmlnsAlias = "dynattr",
            icon = "io/jmix/dynattrflowui/icon/component/dynamicAttributesPanel.svg",
            properties = {
                    @StudioProperty(
                            name = "dataContainer",
                            xmlAttribute = "dataContainer",
                            type = StudioPropertyType.DATA_CONTAINER_REF,
                            required = true),
                    @StudioProperty(
                            name = "cols",
                            xmlAttribute = "cols",
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            name = "rows",
                            xmlAttribute = "rows",
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            name = "fieldWidth",
                            xmlAttribute = "fieldWidth",
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            name = "fieldCaptionWidth",
                            xmlAttribute = "fieldCaptionWidth",
                            type = StudioPropertyType.INTEGER),
            })
    DynamicAttributesPanel dynamicAttributesPanel();
}
