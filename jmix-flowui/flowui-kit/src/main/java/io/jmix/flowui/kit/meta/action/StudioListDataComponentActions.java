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
public interface StudioListDataComponentActions {

    @StudioAction(
            type = "list_create",
            description = "Creates an entity instance using its detail view",
            classFqn = "io.jmix.flowui.action.list.CreateAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            availableInViewWizard = true,
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "PRIMARY", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "PLUS",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "create"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Create"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "viewId", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "viewClass", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "openMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.OpenMode", setParameterFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"DIALOG", "NAVIGATION"}),
            }
    )
    void createAction();

    @StudioAction(
            type = "list_edit",
            description = "Edits an entity instance using its detail view",
            classFqn = "io.jmix.flowui.action.list.EditAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            availableInViewWizard = true,
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "PENCIL",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "edit"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION,
                            defaultValue = "ENTER"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Edit"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "viewId", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "viewClass", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "openMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.OpenMode", setParameterFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"DIALOG", "NAVIGATION"}),
                    @StudioPropertiesItem(xmlAttribute = "constraintEntityOp", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.security.EntityOp",
                            setParameterFqn = "io.jmix.core.security.EntityOp",
                            defaultValue = "UPDATE",
                            options = {"READ", "CREATE", "UPDATE", "DELETE"}),
            }
    )
    void editAction();

    @StudioAction(
            type = "list_remove",
            description = "Removes an entity instance from the list and from the database",
            classFqn = "io.jmix.flowui.action.list.RemoveAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            availableInViewWizard = true,
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DANGER", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "CLOSE",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "remove"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Remove"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "constraintEntityOp", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.security.EntityOp",
                            setParameterFqn = "io.jmix.core.security.EntityOp",
                            defaultValue = "DELETE",
                            options = {"READ", "CREATE", "UPDATE", "DELETE"}),
                    @StudioPropertiesItem(xmlAttribute = "confirmation", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "confirmationText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioPropertiesItem(xmlAttribute = "confirmationHeader", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void removeAction();

    @StudioAction(
            type = "list_add",
            description = "Adds entities to the list using a lookup view",
            classFqn = "io.jmix.flowui.action.list.AddAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            availableInViewWizard = true,
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "PRIMARY", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "PLUS",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "add"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Add"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "viewId", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "viewClass", type = StudioPropertyType.STRING),
            }
    )
    void addAction();

    @StudioAction(
            type = "list_exclude",
            description = "Excludes entities from the list. The excluded entities are not deleted.",
            classFqn = "io.jmix.flowui.action.list.ExcludeAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            availableInViewWizard = true,
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DANGER", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "CLOSE",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "exclude"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Exclude"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "constraintEntityOp", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.security.EntityOp",
                            setParameterFqn = "io.jmix.core.security.EntityOp",
                            options = {"READ", "CREATE", "UPDATE", "DELETE"}),
                    @StudioPropertiesItem(xmlAttribute = "confirmation", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "confirmationText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioPropertiesItem(xmlAttribute = "confirmationHeader", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void excludeAction();

    @StudioAction(
            type = "list_read",
            description = "Opens a detail view for an entity instance in read-only mode",
            classFqn = "io.jmix.flowui.action.list.ReadAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            availableInViewWizard = true,
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "EYE",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "read"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION,
                            defaultValue = "ENTER"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Read"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "viewId", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "viewClass", type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = "openMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.OpenMode", setParameterFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"DIALOG", "NAVIGATION"}),
                    @StudioPropertiesItem(xmlAttribute = "constraintEntityOp", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.security.EntityOp",
                            setParameterFqn = "io.jmix.core.security.EntityOp",
                            defaultValue = "READ",
                            options = {"READ", "CREATE", "UPDATE", "DELETE"}),
            }
    )
    void readAction();

    @StudioAction(
            type = "list_refresh",
            description = "Reloads a list of entities from the database",
            classFqn = "io.jmix.flowui.action.list.RefreshAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "REFRESH",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "refresh"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Refresh"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }
    )
    void refreshAction();

    @StudioAction(
            type = "list_itemTracking",
            description = "Tracks the selected item from the bound ListDataComponent",
            classFqn = "io.jmix.flowui.action.list.ItemTrackingAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "itemTracking"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "enabledByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "visibleByUiPermissions", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = "constraintEntityOp", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.security.EntityOp",
                            setParameterFqn = "io.jmix.core.security.EntityOp",
                            options = {"READ", "CREATE", "UPDATE", "DELETE"}),
            }
    )
    void itemTrackingAction();

    @StudioAction(
            type = "list_bulkEdit",
            description = "Changes attribute values for several entity instances at once",
            classFqn = "io.jmix.flowui.action.list.BulkEditAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
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
                    @StudioPropertiesItem(xmlAttribute = "columnsMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.app.bulk.ColumnsMode", defaultValue = "TWO_COLUMNS",
                            options = {"ONE_COLUMN", "TWO_COLUMNS"}),
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
