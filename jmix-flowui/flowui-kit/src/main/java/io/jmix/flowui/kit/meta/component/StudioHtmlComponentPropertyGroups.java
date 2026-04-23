/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.meta.component;

import io.jmix.flowui.kit.meta.StudioAPI;
import io.jmix.flowui.kit.meta.StudioPropertyGroup;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;

@StudioAPI
final class StudioHtmlComponentPropertyGroups {

    private StudioHtmlComponentPropertyGroups() {
    }

    @StudioPropertyGroup
    public interface BaseHtmlComponent extends StudioPropertyGroups.BaseSizedComponentWithClassNames,
            StudioPropertyGroups.Title {
    }

    @StudioPropertyGroup
    public interface BaseHtmlContainer extends BaseHtmlComponent, StudioPropertyGroups.Enabled,
            StudioPropertyGroups.DataBindingAttributes, StudioPropertyGroups.TextAttributes,
            StudioPropertyGroups.HtmlComponentThemeNames {
    }

    @StudioPropertyGroup
    public interface BaseClickableHtmlContainer extends BaseHtmlContainer, StudioPropertyGroups.ClickShortcut {
    }

    @StudioPropertyGroup
    public interface HtmlContainerWithAria extends BaseHtmlContainer, StudioPropertyGroups.HasAriaLabel {
    }

    @StudioPropertyGroup
    public interface ClickableHtmlContainerWithAria extends HtmlContainerWithAria, StudioPropertyGroups.ClickShortcut {
    }

    @StudioPropertyGroup
    public interface NativeLabelHtmlContainer extends BaseHtmlComponent, StudioPropertyGroups.Enabled,
            StudioPropertyGroups.DataBindingAttributes, StudioPropertyGroups.TextAttributes,
            StudioPropertyGroups.NativeLabelThemeNames {
    }

    @StudioPropertyGroup
    public interface ImageHtmlComponent extends BaseHtmlComponent, StudioPropertyGroups.Enabled,
            StudioPropertyGroups.DataBindingAttributes, StudioPropertyGroups.TextAttributes,
            StudioPropertyGroups.ClickShortcut, StudioPropertyGroups.HasAriaLabel,
            StudioPropertyGroups.AlternateText, StudioPropertyGroups.ImageResource,
            StudioPropertyGroups.ImageThemeNames {
    }

    @StudioPropertyGroup
    public interface AccessibleBaseClickableHtmlContainer extends ClickableHtmlContainerWithAria {
    }

    @StudioPropertyGroup
    public interface TextHtmlComponentDefaultProperties extends BaseClickableHtmlContainer {
    }

    @StudioPropertyGroup
    public interface AccessibleTextHtmlComponentDefaultProperties extends AccessibleBaseClickableHtmlContainer {
    }

    @StudioPropertyGroup
    public interface TitleHtmlComponentDefaultProperties extends BaseHtmlComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HREF,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TARGET,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.GENERAL,
                            classFqn = "com.vaadin.flow.component.html.AnchorTarget",
                            defaultValue = "DEFAULT",
                            options = {"DEFAULT", "SELF", "BLANK", "PARENT", "TOP"})
            }
    )
    public interface AnchorComponent extends HtmlContainerWithAria, StudioPropertyGroups.HasFocusableAttributes {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DATA,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface HtmlObjectComponent extends BaseClickableHtmlContainer,
            StudioPropertyGroups.HasFocusableAttributes, StudioPropertyGroups.StringType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ALLOW,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.IMPORTANCE,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.GENERAL,
                            classFqn = "com.vaadin.flow.component.html.IFrame.ImportanceType",
                            defaultValue = "AUTO",
                            options = {"AUTO", "HIGH", "LOW"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RESOURCE_DOC,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SANDBOX,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.GENERAL,
                            classFqn = "com.vaadin.flow.component.html.IFrame.SandboxType",
                            defaultValue = "RESTRICT_ALL",
                            options = {"RESTRICT_ALL", "ALLOW_FORMS", "ALLOW_MODALS", "ALLOW_ORIENTATION_LOCK",
                                    "ALLOW_POINTER_LOCK", "ALLOW_POPUPS", "ALLOW_POPUPS_TO_ESCAPE_SANDBOX",
                                    "ALLOW_PRESENTATION", "ALLOW_SAME_ORIGIN", "ALLOW_SCRIPTS",
                                    "ALLOW_STORAGE_ACCESS_BY_USER_ACTIVATION", "ALLOW_TOP_NAVIGATION",
                                    "ALLOW_TOP_NAVIGATION_BY_USER_ACTIVATION"})
            }
    )
    public interface IframeComponent extends BaseHtmlComponent, StudioPropertyGroups.Name,
            StudioPropertyGroups.Resource {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPE,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"button", "checkbox", "color", "date", "datetime-local", "file", "hidden", "image", "month",
                    "number", "password", "radio", "range", "reset", "search", "submit", "tel", "text", "time", "url",
                    "week"}))
    public interface InputComponent extends BaseHtmlComponent,
            StudioPropertyGroups.HasAriaLabelAndFocusableAttributes, StudioPropertyGroups.ValueChangeModeAttributesWithGeneralCategory,
            StudioPropertyGroups.Enabled, StudioPropertyGroups.Placeholder {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NUMBERING_TYPE,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            classFqn = "com.vaadin.flow.component.html.OrderedList.NumberingType",
            options = {"NUMBER", "UPPERCASE_LETTER", "LOWERCASE_LETTER", "UPPERCASE_ROMAN", "LOWERCASE_ROMAN"}))
    public interface OrderedListComponent extends BaseClickableHtmlContainer {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface ParamComponent extends BaseHtmlComponent, StudioPropertyGroups.StringValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ORIENTATION,
            type = StudioPropertyType.ENUMERATION,
            category = StudioProperty.Category.GENERAL,
            classFqn = "com.vaadin.flow.component.html.RangeInput$Orientation",
            defaultValue = "HORIZONTAL",
            options = {"HORIZONTAL", "VERTICAL"}))
    public interface RangeInputComponent extends StudioPropertyGroups.SizedComponentDefaultProperties,
            StudioPropertyGroups.HasAriaLabelAndFocusableAttributes, StudioPropertyGroups.ValueChangeModeAttributesWithGeneralCategory,
            StudioPropertyGroups.Step, StudioPropertyGroups.DoubleMinWithGeneralCategory,
            StudioPropertyGroups.DoubleMaxWithGeneralCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SET_FOR,
            type = StudioPropertyType.COMPONENT_REF,
            category = StudioProperty.Category.GENERAL))
    public interface NativeLabelComponent extends NativeLabelHtmlContainer {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OPEN,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface NativeDetailsComponent extends BaseHtmlComponent, StudioPropertyGroups.DetailsSummaryText,
            StudioPropertyGroups.ClickShortcut {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LEGEND_TEXT,
            type = StudioPropertyType.LOCALIZED_STRING,
            category = StudioProperty.Category.GENERAL))
    public interface FieldSetComponent extends HtmlContainerWithAria {
    }

}
