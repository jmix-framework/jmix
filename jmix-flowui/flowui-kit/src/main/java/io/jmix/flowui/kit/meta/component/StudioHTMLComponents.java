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

package io.jmix.flowui.kit.meta.component;

import com.vaadin.flow.component.html.*;
import io.jmix.flowui.kit.meta.*;

@StudioUiKit
interface StudioHTMLComponents {

    @StudioComponent(
            name = "Anchor",
            classFqn = "com.vaadin.flow.component.html.Anchor",
            category = "HTML",
            xmlElement = StudioXmlElements.ANCHOR,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.AnchorComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HREF, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TAB_INDEX, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOCUS_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TARGET, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.html.AnchorTarget", defaultValue = "DEFAULT",
                            options = {"DEFAULT", "SELF", "BLANK", "PARENT", "TOP"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Anchor anchor();

    @StudioComponent(
            name = "Article",
            classFqn = "com.vaadin.flow.component.html.Article",
            category = "HTML",
            xmlElement = StudioXmlElements.ARTICLE,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Article article();

    @StudioComponent(
            name = "Aside",
            classFqn = "com.vaadin.flow.component.html.Aside",
            category = "HTML",
            xmlElement = StudioXmlElements.ASIDE,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Aside aside();

    @StudioComponent(
            name = "DescriptionList",
            classFqn = "com.vaadin.flow.component.html.DescriptionList",
            category = "HTML",
            xmlElement = StudioXmlElements.DESCRIPTION_LIST,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    DescriptionList descriptionList();

    @StudioComponent(
            name = "Div",
            classFqn = "com.vaadin.flow.component.html.Div",
            category = "HTML",
            xmlElement = StudioXmlElements.DIV,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/div.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "hbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "vbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "flexLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "details"),
            }),
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Div div();

    @StudioComponent(
            name = "Emphasis",
            classFqn = "com.vaadin.flow.component.html.Emphasis",
            category = "HTML",
            xmlElement = StudioXmlElements.EMPHASIS,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Emphasis emphasis();

    @StudioComponent(
            name = "Footer",
            classFqn = "com.vaadin.flow.component.html.Footer",
            category = "HTML",
            xmlElement = StudioXmlElements.FOOTER,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Footer footer();

    @StudioComponent(
            name = "H1",
            classFqn = "com.vaadin.flow.component.html.H1",
            category = "HTML",
            xmlElement = StudioXmlElements.H1,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/h1-h6.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h2"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h3"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h4"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h5"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h6"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "span"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "nativeLabel"),
            }),
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0,
                            enableInspection = false
                    )
            ),
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    H1 h1();

    @StudioComponent(
            name = "H2",
            classFqn = "com.vaadin.flow.component.html.H2",
            category = "HTML",
            xmlElement = StudioXmlElements.H2,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/h1-h6.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h1"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h3"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h4"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h5"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h6"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "span"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "nativeLabel"),
            }),
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0,
                            enableInspection = false
                    )
            ),
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    H2 h2();

    @StudioComponent(
            name = "H3",
            classFqn = "com.vaadin.flow.component.html.H3",
            category = "HTML",
            xmlElement = StudioXmlElements.H3,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/h1-h6.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h1"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h3"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h4"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h5"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h6"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "span"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "nativeLabel"),
            }),
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0,
                            enableInspection = false
                    )
            ),
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    H3 h3();

    @StudioComponent(
            name = "H4",
            classFqn = "com.vaadin.flow.component.html.H4",
            category = "HTML",
            xmlElement = StudioXmlElements.H4,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/h1-h6.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h1"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h2"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h3"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h5"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h6"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "span"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "nativeLabel"),
            }),
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0,
                            enableInspection = false
                    )
            ),
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    H4 h4();

    @StudioComponent(
            name = "H5",
            classFqn = "com.vaadin.flow.component.html.H5",
            category = "HTML",
            xmlElement = StudioXmlElements.H5,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/h1-h6.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h1"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h2"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h3"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h4"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h6"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "span"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "nativeLabel"),
            }),
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0,
                            enableInspection = false
                    )
            ),
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    H5 h5();

    @StudioComponent(
            name = "H6",
            classFqn = "com.vaadin.flow.component.html.H6",
            category = "HTML",
            xmlElement = StudioXmlElements.H6,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/h1-h6.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h1"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h2"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h3"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h4"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h5"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "span"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "nativeLabel"),
            }),
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0,
                            enableInspection = false
                    )
            ),
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    H6 h6();

    @StudioComponent(
            name = "Header",
            classFqn = "com.vaadin.flow.component.html.Header",
            category = "HTML",
            xmlElement = StudioXmlElements.HEADER,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Header header();

    @StudioComponent(
            name = "Hr",
            classFqn = "com.vaadin.flow.component.html.Hr",
            category = "HTML",
            xmlElement = StudioXmlElements.HR,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseHtmlComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    Hr hr();

    @StudioComponent(
            name = "HtmlObject",
            classFqn = "com.vaadin.flow.component.html.HtmlObject",
            category = "HTML",
            xmlElement = StudioXmlElements.HTML_OBJECT,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.HtmlObjectComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TAB_INDEX, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOCUS_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TYPE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    HtmlObject htmlObject();

    @StudioComponent(
            name = "IFrame",
            classFqn = "com.vaadin.flow.component.html.IFrame",
            category = "HTML",
            icon = "io/jmix/flowui/kit/meta/icon/html/iFrame.svg",
            xmlElement = StudioXmlElements.IFRAME,
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.IframeComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALLOW, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.IMPORTANCE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.html.IFrame.ImportanceType", defaultValue = "AUTO",
                            options = {"AUTO", "HIGH", "LOW"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RESOURCE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RESOURCE_DOC, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SANDBOX, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.html.IFrame.SandboxType",
                            defaultValue = "RESTRICT_ALL",
                            options = {"RESTRICT_ALL", "ALLOW_FORMS", "ALLOW_MODALS", "ALLOW_ORIENTATION_LOCK",
                                    "ALLOW_POINTER_LOCK", "ALLOW_POPUPS", "ALLOW_POPUPS_TO_ESCAPE_SANDBOX",
                                    "ALLOW_PRESENTATION", "ALLOW_SAME_ORIGIN", "ALLOW_SCRIPTS",
                                    "ALLOW_STORAGE_ACCESS_BY_USER_ACTIVATION", "ALLOW_TOP_NAVIGATION",
                                    "ALLOW_TOP_NAVIGATION_BY_USER_ACTIVATION"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    IFrame iframe();

    @StudioComponent(
            name = "Image",
            classFqn = "io.jmix.flowui.component.image.JmixImage",
            category = "HTML",
            xmlElement = StudioXmlElements.IMAGE,
            icon = "io/jmix/flowui/kit/meta/icon/html/image.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/image.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.ImageHtmlComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALTERNATE_TEXT, type = StudioPropertyType.LOCALIZED_STRING,
                            setMethod = "setAlt"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RESOURCE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING,
                            setMethod = "setSrc"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"fill", "contain", "cover", "scale-down"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Image image();

    @StudioComponent(
            name = "Input",
            classFqn = "com.vaadin.flow.component.html.Input",
            category = "HTML",
            xmlElement = StudioXmlElements.INPUT,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.InputComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PLACEHOLDER, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TAB_INDEX, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOCUS_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TYPE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING,
                            options = {"button", "checkbox", "color", "date", "datetime-local", "file", "hidden",
                                    "image", "month", "number", "password", "radio", "range", "reset", "search",
                                    "submit", "tel", "text", "time", "url", "week"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE_CHANGE_MODE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode",
                            options = {"EAGER", "LAZY", "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE_CHANGE_TIMEOUT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    Input input();

    @StudioComponent(
            name = "ListItem",
            classFqn = "com.vaadin.flow.component.html.ListItem",
            category = "HTML",
            xmlElement = StudioXmlElements.LIST_ITEM,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    ListItem listItem();

    @StudioComponent(
            name = "Main",
            classFqn = "com.vaadin.flow.component.html.Main",
            category = "HTML",
            xmlElement = StudioXmlElements.MAIN,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.AccessibleBaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Main main();

    @StudioComponent(
            name = "Nav",
            classFqn = "com.vaadin.flow.component.html.Nav",
            category = "HTML",
            xmlElement = StudioXmlElements.NAV,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.AccessibleBaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Nav nav();

    @StudioComponent(
            name = "OrderedList",
            classFqn = "com.vaadin.flow.component.html.OrderedList",
            category = "HTML",
            xmlElement = StudioXmlElements.ORDERED_LIST,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.OrderedListComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NUMBERING_TYPE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.html.OrderedList.NumberingType",
                            options = {"NUMBER", "UPPERCASE_LETTER", "LOWERCASE_LETTER", "UPPERCASE_ROMAN",
                                    "LOWERCASE_ROMAN"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    OrderedList orderedList();

    @StudioComponent(
            name = "Paragraph",
            classFqn = "com.vaadin.flow.component.html.Paragraph",
            category = "HTML",
            xmlElement = StudioXmlElements.P,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Paragraph paragraph();

    @StudioComponent(
            name = "Param",
            classFqn = "com.vaadin.flow.component.html.Param",
            category = "HTML",
            xmlElement = StudioXmlElements.PARAM,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.ParamComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    Param param();

    @StudioComponent(
            name = "Pre",
            classFqn = "com.vaadin.flow.component.html.Pre",
            category = "HTML",
            xmlElement = StudioXmlElements.PRE,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Pre pre();

    @StudioComponent(
            name = "Code",
            classFqn = "com.vaadin.flow.component.html.Code",
            category = "HTML",
            xmlElement = StudioXmlElements.CODE,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Code code();

    @StudioComponent(
            name = "RangeInput",
            classFqn = "com.vaadin.flow.component.html.RangeInput",
            category = "HTML",
            xmlElement = StudioXmlElements.RANGE_INPUT,
            icon = "io/jmix/flowui/kit/meta/icon/html/rangeInput.svg",
            propertyGroups = StudioHtmlComponentPropertyGroups.RangeInputComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ORIENTATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.html.RangeInput$Orientation",
                            defaultValue = "HORIZONTAL",
                            options = {"HORIZONTAL", "VERTICAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.STEP, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TAB_INDEX, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOCUS_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE_CHANGE_MODE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode",
                            options = {"EAGER", "LAZY", "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE_CHANGE_TIMEOUT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    RangeInput rangeInput();

    @StudioComponent(
            name = "Section",
            classFqn = "com.vaadin.flow.component.html.Section",
            category = "HTML",
            xmlElement = StudioXmlElements.SECTION,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Section section();

    @StudioComponent(
            name = "Span",
            classFqn = "com.vaadin.flow.component.html.Span",
            category = "HTML",
            xmlElement = StudioXmlElements.SPAN,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/span.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h1"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h2"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h3"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h4"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h5"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "nativeLabel"),
            }),
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0,
                            enableInspection = false
                    )
            ),
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Span span();

    @StudioComponent(
            name = "UnorderedList",
            classFqn = "com.vaadin.flow.component.html.UnorderedList",
            category = "HTML",
            xmlElement = StudioXmlElements.UNORDERED_LIST,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    UnorderedList unorderedList();

    @StudioComponent(
            name = "NativeLabel",
            classFqn = "com.vaadin.flow.component.html.NativeLabel",
            category = "HTML",
            xmlElement = StudioXmlElements.NATIVE_LABEL,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/nativeLabel.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h1"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h2"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h3"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h4"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "h5"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "span"),
            }),
            propertyGroups = StudioHtmlComponentPropertyGroups.NativeLabelComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SET_FOR, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0,
                            enableInspection = false
                    )
            ),
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    NativeLabel nativeLabel();

    @StudioComponent(
            name = "NativeButton",
            classFqn = "com.vaadin.flow.component.html.NativeButton",
            category = "HTML",
            xmlElement = StudioXmlElements.NATIVE_BUTTON,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = {
                    StudioHtmlComponentPropertyGroups.NativeLabelHtmlContainer.class,
                    StudioPropertyGroups.HasAriaLabelAndFocusableAttributes.class,
                    StudioPropertyGroups.ClickShortcutWithGeneralCategory.class,
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TAB_INDEX, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOCUS_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    NativeButton nativeButton();

    @StudioComponent(
            name = "Term",
            classFqn = "com.vaadin.flow.component.html.DescriptionList.Term",
            category = "HTML",
            xmlElement = StudioXmlElements.TERM,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    DescriptionList.Term term();

    @StudioComponent(
            name = "Description",
            classFqn = "com.vaadin.flow.component.html.DescriptionList.Description",
            category = "HTML",
            xmlElement = StudioXmlElements.DESCRIPTION,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    DescriptionList.Description description();

    @StudioComponent(
            name = "NativeDetails",
            classFqn = "com.vaadin.flow.component.html.NativeDetails",
            category = "HTML",
            xmlElement = StudioXmlElements.NATIVE_DETAILS,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.NativeDetailsComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPEN, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SUMMARY_TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    NativeDetails nativeDetails();

    @StudioComponent(
            name = "FieldSet",
            classFqn = "com.vaadin.flow.component.html.FieldSet",
            category = "HTML",
            xmlElement = StudioXmlElements.FIELD_SET,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/fieldSet.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "hbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "vbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "flexLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "details"),
            }),
            propertyGroups = StudioHtmlComponentPropertyGroups.FieldSetComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"normal", "success", "warning", "error", "contrast", "primary", "small", "pill", "badge"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LEGEND_TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING)
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    FieldSet fieldSet();
}
