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

package io.jmix.flowui.kit.meta.pallete;

import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.meta.StudioFlowComponent;
import io.jmix.flowui.kit.meta.StudioFlowComponents;
import io.jmix.flowui.kit.meta.StudioFlowProperty;
import io.jmix.flowui.kit.meta.StudioFlowPropertyType;

@StudioFlowComponents
public interface StudioFlowPaletteComponents {

    @StudioFlowComponent(
            name = "Button",
            classFqn = "io.jmix.flowui.kit.component.button.JmixButton",
            category = "Components",
            xmlElement = "button",
            icon = "io/jmix/flowui/kit/meta/component/button.svg",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "autofocus", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "disableOnClick", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "icon", type = StudioFlowPropertyType.ICON),
                    @StudioFlowProperty(xmlAttribute = "iconAfterText", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline", "primary", "success", "error",
                                    "contrast", "icon", "contained", "outlined"}),
                    @StudioFlowProperty(xmlAttribute = "title", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "visible", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "whiteSpace", type = StudioFlowPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioFlowProperty(xmlAttribute = "width", type = StudioFlowPropertyType.SIZE)
            }
    )
    JmixButton button();
}
