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

package io.jmix.flowui.kit.meta.palette;

import com.vaadin.flow.component.html.*;
import io.jmix.flowui.kit.meta.StudioFlowComponent;
import io.jmix.flowui.kit.meta.StudioFlowComponents;
import io.jmix.flowui.kit.meta.StudioFlowProperty;
import io.jmix.flowui.kit.meta.StudioFlowPropertyType;

@StudioFlowComponents
public interface StudioFlowPaletteHTMLComponents {

    @StudioFlowComponent(
            name = "Anchor",
            classFqn = "com.vaadin.flow.component.html.Anchor",
            category = "HTML",
            xmlElement = "anchor",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "href", type = StudioFlowPropertyType.STRING),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "target", type = StudioFlowPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.html.AnchorTarget", defaultValue = "DEFAULT",
                            options = {"DEFAULT", "SELF", "BLANK", "PARENT", "TOP"}),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Anchor anchor();

    @StudioFlowComponent(
            name = "Article",
            classFqn = "com.vaadin.flow.component.html.Article",
            category = "HTML",
            xmlElement = "article",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Article article();

    @StudioFlowComponent(
            name = "Aside",
            classFqn = "com.vaadin.flow.component.html.Aside",
            category = "HTML",
            xmlElement = "aside",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Aside aside();

    @StudioFlowComponent(
            name = "DescriptionList",
            classFqn = "com.vaadin.flow.component.html.DescriptionList",
            category = "HTML",
            xmlElement = "descriptionList",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    DescriptionList descriptionList();

    @StudioFlowComponent(
            name = "Term",
            classFqn = "com.vaadin.flow.component.html.DescriptionList$Term",
            category = "HTML",
            xmlElement = "term",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    DescriptionList.Term term();

    @StudioFlowComponent(
            name = "Description",
            classFqn = "com.vaadin.flow.component.html.DescriptionList$Description",
            category = "HTML",
            xmlElement = "description",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    DescriptionList.Description description();

    @StudioFlowComponent(
            name = "Div",
            classFqn = "com.vaadin.flow.component.html.Div",
            category = "HTML",
            xmlElement = "div",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Div div();

    @StudioFlowComponent(
            name = "Emphasis",
            classFqn = "com.vaadin.flow.component.html.Emphasis",
            category = "HTML",
            xmlElement = "emphasis",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Emphasis emphasis();

    @StudioFlowComponent(
            name = "Footer",
            classFqn = "com.vaadin.flow.component.html.Footer",
            category = "HTML",
            xmlElement = "footer",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Footer footer();

    @StudioFlowComponent(
            name = "H1",
            classFqn = "com.vaadin.flow.component.html.H1",
            category = "HTML",
            xmlElement = "h1",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    H1 h1();

    @StudioFlowComponent(
            name = "H2",
            classFqn = "com.vaadin.flow.component.html.H2",
            category = "HTML",
            xmlElement = "h2",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    H2 h2();

    @StudioFlowComponent(
            name = "H3",
            classFqn = "com.vaadin.flow.component.html.H3",
            category = "HTML",
            xmlElement = "h3",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    H3 h3();

    @StudioFlowComponent(
            name = "H4",
            classFqn = "com.vaadin.flow.component.html.H4",
            category = "HTML",
            xmlElement = "h4",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    H4 h4();

    @StudioFlowComponent(
            name = "H5",
            classFqn = "com.vaadin.flow.component.html.H5",
            category = "HTML",
            xmlElement = "h5",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    H5 h5();

    @StudioFlowComponent(
            name = "H6",
            classFqn = "com.vaadin.flow.component.html.H6",
            category = "HTML",
            xmlElement = "h6",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    H6 h6();

    @StudioFlowComponent(
            name = "Header",
            classFqn = "com.vaadin.flow.component.html.Header",
            category = "HTML",
            xmlElement = "header",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Header header();

    @StudioFlowComponent(
            name = "Hr",
            classFqn = "com.vaadin.flow.component.html.Hr",
            category = "HTML",
            xmlElement = "hr",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "title", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "visible", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "width", type = StudioFlowPropertyType.SIZE)
            }
    )
    Hr hr();

    @StudioFlowComponent(
            name = "HtmlObject",
            classFqn = "com.vaadin.flow.component.html.HtmlObject",
            category = "HTML",
            xmlElement = "htmlObject",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "data", type = StudioFlowPropertyType.STRING),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
                    @StudioFlowProperty(xmlAttribute = "title", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "type", type = StudioFlowPropertyType.STRING),
                    @StudioFlowProperty(xmlAttribute = "visible", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "whiteSpace", type = StudioFlowPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioFlowProperty(xmlAttribute = "width", type = StudioFlowPropertyType.SIZE)
            }
    )
    HtmlObject htmlObject();

    @StudioFlowComponent(
            name = "IFrame",
            classFqn = "com.vaadin.flow.component.html.IFrame",
            category = "HTML",
            icon = "io/jmix/flowui/kit/meta/palette/html/iFrame.svg",
            xmlElement = "iframe",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "allow", type = StudioFlowPropertyType.STRING),
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "importance", type = StudioFlowPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.html.IFrame.ImportanceType", defaultValue = "AUTO",
                            options = {"AUTO", "HIGH", "LOW"}),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "name", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "resource", type = StudioFlowPropertyType.STRING),
                    @StudioFlowProperty(xmlAttribute = "resourceDoc", type = StudioFlowPropertyType.STRING),
                    @StudioFlowProperty(xmlAttribute = "sandbox", type = StudioFlowPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.html.IFrame.SandboxType",
                            defaultValue = "RESTRICT_ALL",
                            options = {"RESTRICT_ALL", "ALLOW_FORMS", "ALLOW_MODALS", "ALLOW_ORIENTATION_LOCK",
                                    "ALLOW_POINTER_LOCK", "ALLOW_POPUPS", "ALLOW_POPUPS_TO_ESCAPE_SANDBOX",
                                    "ALLOW_PRESENTATION", "ALLOW_SAME_ORIGIN", "ALLOW_SCRIPTS",
                                    "ALLOW_STORAGE_ACCESS_BY_USER_ACTIVATION", "ALLOW_TOP_NAVIGATION",
                                    "ALLOW_TOP_NAVIGATION_BY_USER_ACTIVATION"}),
                    @StudioFlowProperty(xmlAttribute = "title", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "visible", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "width", type = StudioFlowPropertyType.SIZE)
            }
    )
    IFrame iframe();

    @StudioFlowComponent(
            name = "Image",
            classFqn = "com.vaadin.flow.component.html.Image",
            category = "HTML",
            xmlElement = "image",
            icon = "io/jmix/flowui/kit/meta/palette/html/image.svg",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "alternativeText", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "ariaLabel", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "resource", type = StudioFlowPropertyType.STRING),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Image image();

    @StudioFlowComponent(
            name = "Input",
            classFqn = "com.vaadin.flow.component.html.Input",
            category = "HTML",
            xmlElement = "input",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "ariaLabel", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "placeholder", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "type", type = StudioFlowPropertyType.STRING,
                            options = {"button", "checkbox", "color", "date", "datetime-local", "file", "hidden",
                                    "image", "month", "number", "password", "radio", "range", "reset", "search",
                                    "submit", "tel", "text", "time", "url", "week"}),
                    @StudioFlowProperty(xmlAttribute = "valueChangeMode", type = StudioFlowPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode",
                            options = {"EAGER", "LAZY", "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioFlowProperty(xmlAttribute = "valueChangeTimeout", type = StudioFlowPropertyType.INTEGER),
                    @StudioFlowProperty(xmlAttribute = "title", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "visible", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "width", type = StudioFlowPropertyType.SIZE)
            }
    )
    Input input();

    @StudioFlowComponent(
            name = "ListItem",
            classFqn = "com.vaadin.flow.component.html.ListItem",
            category = "HTML",
            xmlElement = "listItem",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    ListItem listItem();

    @StudioFlowComponent(
            name = "Main",
            classFqn = "com.vaadin.flow.component.html.Main",
            category = "HTML",
            xmlElement = "main",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "ariaLabel", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Main main();

    @StudioFlowComponent(
            name = "Nav",
            classFqn = "com.vaadin.flow.component.html.Nav",
            category = "HTML",
            xmlElement = "nav",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "ariaLabel", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Nav nav();

    @StudioFlowComponent(
            name = "OrderedList",
            classFqn = "com.vaadin.flow.component.html.OrderedList",
            category = "HTML",
            xmlElement = "orderedList",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "numberingType", type = StudioFlowPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.html.OrderedList.NumberingType",
                            options = {"NUMBER", "UPPERCASE_LETTER", "LOWERCASE_LETTER", "UPPERCASE_ROMAN",
                                    "LOWERCASE_ROMAN"}),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    OrderedList orderedList();

    @StudioFlowComponent(
            name = "Paragraph",
            classFqn = "com.vaadin.flow.component.html.Paragraph",
            category = "HTML",
            xmlElement = "p",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Paragraph paragraph();

    @StudioFlowComponent(
            name = "Param",
            classFqn = "com.vaadin.flow.component.html.Param",
            category = "HTML",
            xmlElement = "param",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "name", type = StudioFlowPropertyType.STRING),
                    @StudioFlowProperty(xmlAttribute = "title", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "value", type = StudioFlowPropertyType.STRING),
                    @StudioFlowProperty(xmlAttribute = "visible", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "width", type = StudioFlowPropertyType.SIZE)
            }
    )
    Param param();

    @StudioFlowComponent(
            name = "Pre",
            classFqn = "com.vaadin.flow.component.html.Pre",
            category = "HTML",
            xmlElement = "pre",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Pre pre();

    @StudioFlowComponent(
            name = "Section",
            classFqn = "com.vaadin.flow.component.html.Section",
            category = "HTML",
            xmlElement = "section",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Section section();

    @StudioFlowComponent(
            name = "Span",
            classFqn = "com.vaadin.flow.component.html.Span",
            category = "HTML",
            xmlElement = "span",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    Span span();

    @StudioFlowComponent(
            name = "UnorderedList",
            classFqn = "com.vaadin.flow.component.html.UnorderedList",
            category = "HTML",
            xmlElement = "unorderedList",
            properties = {
                    @StudioFlowProperty(xmlAttribute = "className", type = StudioFlowPropertyType.VALUES_LIST),
                    @StudioFlowProperty(xmlAttribute = "enabled", type = StudioFlowPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioFlowProperty(xmlAttribute = "height", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "id", type = StudioFlowPropertyType.COMPONENT_ID),
                    @StudioFlowProperty(xmlAttribute = "maxHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "maxWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minHeight", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "minWidth", type = StudioFlowPropertyType.SIZE),
                    @StudioFlowProperty(xmlAttribute = "text", type = StudioFlowPropertyType.LOCALIZED_STRING),
                    @StudioFlowProperty(xmlAttribute = "themeName", type = StudioFlowPropertyType.VALUES_LIST,
                            options = {"normal", "success", "error", "contrast", "primary", "small", "pill"}),
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
    UnorderedList unorderedList();
}
