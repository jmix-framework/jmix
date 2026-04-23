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
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;

@StudioUiKit
interface StudioListDataComponentActions {

    @StudioAction(
            type = "list_create",
            description = "Creates an entity instance using its detail view",
            classFqn = "io.jmix.flowui.action.list.CreateAction",
            documentationLink = "%VERSION%/flow-ui/actions/list-actions.html#list_create",
            availableInViewWizard = true,
            propertyGroups = StudioActionPropertyGroups.CreateActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "PRIMARY", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, defaultValue = "PLUS",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "create"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Create"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.VIEW_ID, type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.VIEW_CLASS, type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.OPEN_MODE, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.OpenMode", setParameterFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"DIALOG", "NAVIGATION"}),
            }
    )
    void createAction();

    @StudioAction(
            type = "list_edit",
            description = "Edits an entity instance using its detail view",
            classFqn = "io.jmix.flowui.action.list.EditAction",
            documentationLink = "%VERSION%/flow-ui/actions/list-actions.html#list_edit",
            availableInViewWizard = true,
            propertyGroups = StudioActionPropertyGroups.ListDataComponentEditActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, defaultValue = "PENCIL",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "edit"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION,
                            defaultValue = "ENTER"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Edit"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.VIEW_ID, type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.VIEW_CLASS, type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.OPEN_MODE, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.OpenMode", setParameterFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"DIALOG", "NAVIGATION"})
            }
    )
    void editAction();

    @StudioAction(
            type = "list_remove",
            description = "Removes an entity instance from the list and from the database",
            classFqn = "io.jmix.flowui.action.list.RemoveAction",
            documentationLink = "%VERSION%/flow-ui/actions/list-actions.html#list_remove",
            availableInViewWizard = true,
            propertyGroups = StudioActionPropertyGroups.ListDataComponentRemoveActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DANGER", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, defaultValue = "CLOSE",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "remove"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Remove"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.CONFIRMATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.CONFIRMATION_TEXT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.CONFIRMATION_HEADER, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void removeAction();

    @StudioAction(
            type = "list_add",
            description = "Adds entities to the list using a lookup view",
            classFqn = "io.jmix.flowui.action.list.AddAction",
            documentationLink = "%VERSION%/flow-ui/actions/list-actions.html#list_add",
            availableInViewWizard = true,
            propertyGroups = StudioActionPropertyGroups.AddActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "PRIMARY", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, defaultValue = "PLUS",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "add"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Add"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.VIEW_ID, type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.VIEW_CLASS, type = StudioPropertyType.STRING),
            }
    )
    void addAction();

    @StudioAction(
            type = "list_exclude",
            description = "Excludes entities from the list. The excluded entities are not deleted.",
            classFqn = "io.jmix.flowui.action.list.ExcludeAction",
            documentationLink = "%VERSION%/flow-ui/actions/list-actions.html#list_exclude",
            availableInViewWizard = true,
            propertyGroups = StudioActionPropertyGroups.ExcludeActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DANGER", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, defaultValue = "CLOSE",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "exclude"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Exclude"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.CONFIRMATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.CONFIRMATION_TEXT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.CONFIRMATION_HEADER, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void excludeAction();

    @StudioAction(
            type = "list_read",
            description = "Opens a detail view for an entity instance in read-only mode",
            classFqn = "io.jmix.flowui.action.list.ReadAction",
            documentationLink = "%VERSION%/flow-ui/actions/list-actions.html#list_read",
            availableInViewWizard = true,
            propertyGroups = StudioActionPropertyGroups.ReadActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, defaultValue = "EYE",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "read"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION,
                            defaultValue = "ENTER"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Read"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.VIEW_ID, type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.VIEW_CLASS, type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.OPEN_MODE, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.OpenMode", setParameterFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"DIALOG", "NAVIGATION"})
            }
    )
    void readAction();

    @StudioAction(
            type = "list_refresh",
            description = "Reloads a list of entities from the database",
            classFqn = "io.jmix.flowui.action.list.RefreshAction",
            documentationLink = "%VERSION%/flow-ui/actions/list-actions.html#list_refresh",
            availableInViewWizard = true,
            propertyGroups = StudioActionPropertyGroups.RefreshActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, defaultValue = "REFRESH",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "refresh"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Refresh"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void refreshAction();

    @StudioAction(
            type = "list_itemTracking",
            description = "Tracks the selected item from the bound ListDataComponent",
            classFqn = "io.jmix.flowui.action.list.ItemTrackingAction",
            propertyGroups = StudioActionPropertyGroups.ItemTrackingActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "itemTracking"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.CONSTRAINT_ENTITY_OP, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.security.EntityOp",
                            setParameterFqn = "io.jmix.core.security.EntityOp",
                            options = {"READ", "CREATE", "UPDATE", "DELETE"})
            }
    )
    void itemTrackingAction();
}
