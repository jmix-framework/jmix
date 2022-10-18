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

package io.jmix.flowui.kit.meta.action;

import io.jmix.flowui.kit.meta.StudioAction;
import io.jmix.flowui.kit.meta.StudioPropertiesItem;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioPickerActions {

    @StudioAction(
            classFqn = "io.jmix.flowui.kit.action.BaseAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            unsupportedTarget = {"io.jmix.flowui.view.View", "io.jmix.flowui.component.ListDataComponent"},
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", required = true),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void baseAction();

    @StudioAction(
            type = "value_clear",
            description = "Clears the value of picker component",
            classFqn = "io.jmix.flowui.action.valuepicker.ValueClearAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "CLOSE",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "valueClear"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION,
                            defaultValue = "CONTROL-ALT-C"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.valuePicker.clear.description"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void valueClearAction();

    @StudioAction(
            type = "entity_clear",
            description = "Clears the value of entity picker component",
            classFqn = "io.jmix.flowui.action.entitypicker.EntityClearAction",
            unsupportedTarget = {"io.jmix.flowui.component.valuepicker.JmixValuePicker",
                    "io.jmix.flowui.component.valuepicker.JmixMultiValuePicker"},
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "CLOSE",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "entityClear"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION,
                            defaultValue = "CONTROL-ALT-C"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.valuePicker.clear.description"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void entityClearAction();

    @StudioAction(
            type = "entity_lookup",
            description = "Sets an entity to the entity picker using the entity lookup view",
            classFqn = "io.jmix.flowui.action.entitypicker.EntityLookupAction",
            unsupportedTarget = {"io.jmix.flowui.component.valuepicker.JmixValuePicker",
                    "io.jmix.flowui.component.valuepicker.JmixMultiValuePicker"},
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "ELLIPSIS_DOTS_H",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "entityLookup"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION,
                            defaultValue = "CONTROL-ALT-L"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.entityPicker.lookup.description"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "viewId", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "viewClass", type = StudioPropertyType.STRING)
            }
    )
    void entityLookupAction();

    @StudioAction(
            type = "entity_open",
            description = "Opens an entity using the entity detail view",
            classFqn = "io.jmix.flowui.action.entitypicker.EntityOpenAction",
            unsupportedTarget = {"io.jmix.flowui.component.valuepicker.JmixValuePicker",
                    "io.jmix.flowui.component.valuepicker.JmixMultiValuePicker"},
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "SEARCH",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "entityOpen"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION,
                            defaultValue = "CONTROL-ALT-O"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.entityPicker.open.description"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "viewId", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "viewClass", type = StudioPropertyType.STRING)
            }
    )
    void entityOpenAction();

    @StudioAction(
            type = "entity_openComposition",
            description = "Opens a one-to-one composition entity using the entity detail view",
            classFqn = "io.jmix.flowui.action.entitypicker.EntityOpenCompositionAction",
            unsupportedTarget = {"io.jmix.flowui.component.valuepicker.JmixValuePicker",
                    "io.jmix.flowui.component.valuepicker.JmixMultiValuePicker"},
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "SEARCH",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "entityOpenComposition"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION,
                            defaultValue = "CONTROL-ALT-O"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.entityPicker.open.description"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "viewId", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "viewClass", type = StudioPropertyType.STRING)
            }
    )
    void entityOpenCompositionAction();
}
