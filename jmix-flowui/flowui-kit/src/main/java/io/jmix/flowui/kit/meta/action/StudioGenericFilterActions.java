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

import io.jmix.flowui.kit.meta.*;

@StudioUiKit
interface StudioGenericFilterActions {

    @StudioAction(
            type = "genericFilter_addCondition",
            description = "Adds condition to current filter configuration",
            classFqn = "io.jmix.flowui.action.genericfilter.GenericFilterAddConditionAction",
            propertyGroups = StudioActionPropertyGroups.AddConditionActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", defaultValue = "PLUS"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "genericFilter_addCondition"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void addConditionAction();

    @StudioAction(
            type = "genericFilter_clearValues",
            description = "Clears the filter condition values",
            classFqn = "io.jmix.flowui.action.genericfilter.GenericFilterClearValuesAction",
            propertyGroups = StudioActionPropertyGroups.ClearValuesActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", defaultValue = "ERASER"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "genericFilter_clearValues"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void clearValuesAction();

    @StudioAction(
            type = "genericFilter_copy",
            description = "Copies all conditions from design-time configuration to run-time configuration",
            classFqn = "io.jmix.flowui.action.genericfilter.GenericFilterCopyAction",
            propertyGroups = StudioActionPropertyGroups.CopyActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", defaultValue = "COPY"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "genericFilter_copy"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void copyAction();

    @StudioAction(
            type = "genericFilter_edit",
            description = "Edits current configuration",
            classFqn = "io.jmix.flowui.action.genericfilter.GenericFilterEditAction",
            propertyGroups = StudioActionPropertyGroups.GenericFilterEditActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", defaultValue = "PENCIL"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "genericFilter_edit"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void editAction();

    @StudioAction(
            type = "genericFilter_reset",
            description = "Resets current configuration",
            classFqn = "io.jmix.flowui.action.genericfilter.GenericFilterResetAction",
            propertyGroups = StudioActionPropertyGroups.ResetActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "genericFilter_reset"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void resetAction();
}
