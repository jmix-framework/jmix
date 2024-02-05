/*
 * Copyright 2023 Haulmont.
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

@StudioUiKit(requiredDependencies = "io.jmix.bulkeditor:jmix-bulkeditor-starter")
public interface StudioBulkEditorActions {

    @StudioAction(
            type = "bulked_edit",
            description = "Changes attribute values for several entity instances at once",
            classFqn = "io.jmix.bulkeditor.action.BulkEditAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            documentationLink = "%VERSION%/bulk-edit/index.html#usage",
            availableInViewWizard = true,
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "TABLE",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "bulkEdit"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.BulkEdit"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "constraintEntityOp", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.security.EntityOp",
                            setParameterFqn = "io.jmix.core.security.EntityOp",
                            defaultValue = "UPDATE",
                            options = {"READ", "CREATE", "UPDATE", "DELETE"}),
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "exclude", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "includeProperties", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "useConfirmDialog", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void bulkEditAction();
}
