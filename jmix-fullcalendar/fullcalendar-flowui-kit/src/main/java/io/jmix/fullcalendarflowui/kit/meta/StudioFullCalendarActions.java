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

package io.jmix.fullcalendarflowui.kit.meta;

import io.jmix.flowui.kit.meta.*;

@StudioUiKit
public interface StudioFullCalendarActions {

    @StudioAction(
            type = "fcalen_daysOfWeekEdit",
            description = "Displays a dialog window with days of week to select",
            classFqn = "io.jmix.fullcalendarflowui.action.DaysOfWeekEditAction",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/action/action.svg",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#days-of-week-datatype",
            propertyGroups = StudioFullCalendarPropertyGroups.DaysOfWeekEditActionComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true,
                            initialValue = "daysOfWeekEdit"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg://io.jmix.datatoolsflowui.action/showEntityInfoAction.title"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.ENABLED_BY_UI_PERMISSIONS, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.VISIBLE_BY_UI_PERMISSIONS, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void daysOfWeekEditAction();
}
