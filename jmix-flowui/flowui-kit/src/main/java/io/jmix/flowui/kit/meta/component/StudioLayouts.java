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

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioConvertStrategy;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioLayouts {

    @StudioComponent(
            name = "Details",
            classFqn = "io.jmix.flowui.component.details.JmixDetails",
            category = "Layouts",
            xmlElement = "details",
            icon = "io/jmix/flowui/kit/meta/icon/layout/details.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/details.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "div"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "hbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "vbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "flexLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "opened", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "summaryText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"filled", "reverse", "small"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    Details details();

    @StudioComponent(
            name = "HBox",
            classFqn = "com.vaadin.flow.component.orderedlayout.HorizontalLayout",
            category = "Layouts",
            xmlElement = "hbox",
            icon = "io/jmix/flowui/kit/meta/icon/layout/hbox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/hbox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "div"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "vbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "details"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "flexLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "alignItems", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "START",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "boxSizing", category = StudioProperty.Category.SIZE, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            classFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            defaultValue = "UNDEFINED",
                            options = {"UNDEFINED", "CONTENT_BOX", "BORDER_BOX"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clickShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "expand", category = StudioProperty.Category.POSITION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "justifyContent", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                            setMethod = "setJustifyContentMode", defaultValue = "START",
                            options = {"START", "END", "CENTER", "BETWEEN", "AROUND", "EVENLY"}),
                    @StudioProperty(xmlAttribute = "margin", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "padding", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "spacing", category = StudioProperty.Category.POSITION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"spacing-xs", "spacing-s", "spacing", "spacing-l", "spacing-xl"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    HorizontalLayout hbox();

    @StudioComponent(
            name = "VBox",
            classFqn = "com.vaadin.flow.component.orderedlayout.VerticalLayout",
            category = "Layouts",
            xmlElement = "vbox",
            icon = "io/jmix/flowui/kit/meta/icon/layout/vbox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/vbox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "div"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "hbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "details"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "flexLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "alignItems", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "START",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "boxSizing", category = StudioProperty.Category.SIZE, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            classFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            defaultValue = "UNDEFINED",
                            options = {"UNDEFINED", "CONTENT_BOX", "BORDER_BOX"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clickShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "expand", category = StudioProperty.Category.POSITION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "justifyContent", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                            setMethod = "setJustifyContentMode", defaultValue = "START",
                            options = {"START", "END", "CENTER", "BETWEEN", "AROUND", "EVENLY"}),
                    @StudioProperty(xmlAttribute = "margin", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "padding", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "spacing", category = StudioProperty.Category.POSITION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"spacing-xs", "spacing-s", "spacing", "spacing-l", "spacing-xl"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "100%")
            }
    )
    VerticalLayout vbox();

    @StudioComponent(
            name = "FlexLayout",
            classFqn = "com.vaadin.flow.component.orderedlayout.FlexLayout",
            category = "Layouts",
            xmlElement = "flexLayout",
            icon = "io/jmix/flowui/kit/meta/icon/layout/hbox.svg",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "div"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "hbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "vbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "details"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clickShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "expand", category = StudioProperty.Category.POSITION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "alignItems", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "START",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "justifyContent", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                            setMethod = "setJustifyContentMode", defaultValue = "START",
                            options = {"START", "END", "CENTER", "BETWEEN", "AROUND", "EVENLY"}),
                    @StudioProperty(xmlAttribute = "contentAlignment", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setAlignContent",
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexLayout$ContentAlignment",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexLayout$ContentAlignment",
                            defaultValue = "START",
                            options = {"START", "END", "CENTER", "STRETCH", "SPACE_BETWEEN", "SPACE_AROUND"}),
                    @StudioProperty(xmlAttribute = "flexDirection", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexLayout$FlexDirection",
                            defaultValue = "ROW",
                            options = {"ROW", "ROW_REVERSE", "COLUMN", "COLUMN_REVERSE"}),
                    @StudioProperty(xmlAttribute = "flexWrap", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexLayout$FlexWrap",
                            defaultValue = "NOWRAP",
                            options = {"NOWRAP", "WRAP", "WRAP_REVERSE"})
            }
    )
    FlexLayout flexLayout();

    @StudioComponent(
            name = "Scroller",
            classFqn = "io.jmix.flowui.component.scroller.JmixScroller",
            category = "Layouts",
            xmlElement = "scroller",
            icon = "io/jmix/flowui/kit/meta/icon/layout/scroller.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/scroller.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "div"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "hbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "vbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "details"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "flexLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "scrollBarsDirection", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.Scroller$ScrollDirection",
                            setMethod = "setScrollDirection", defaultValue = "VERTICAL",
                            options = {"VERTICAL", "HORIZONTAL", "BOTH", "NONE"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "100%")
            }
    )
    Scroller scroller();

    @StudioComponent(
            name = "Split",
            classFqn = "io.jmix.flowui.component.splitlayout.JmixSplitLayout",
            category = "Layouts",
            xmlElement = "split",
            icon = "io/jmix/flowui/kit/meta/icon/layout/split.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/split.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "div"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "hbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "vbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "details"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "flexLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clickShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "orientation", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.splitlayout.SplitLayout$Orientation",
                            setMethod = "setOrientation", defaultValue = "HORIZONTAL",
                            options = {"VERTICAL", "HORIZONTAL"}),
                    @StudioProperty(xmlAttribute = "splitterPosition", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "minimal", "splitter-spacing"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "100%")
            }
    )
    SplitLayout splitLayout();

    @StudioComponent(
            name = "Accordion",
            classFqn = "io.jmix.flowui.component.accordion.JmixAccordion",
            category = "Layouts",
            xmlElement = "accordion",
            icon = "io/jmix/flowui/kit/meta/icon/layout/accordion.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/accordion.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "100%")
            }
    )
    Accordion accordion();

    @StudioComponent(
            name = "FormLayout",
            classFqn = "io.jmix.flowui.component.formlayout.JmixFormLayout",
            category = "Layouts",
            xmlElement = "formLayout",
            icon = "io/jmix/flowui/kit/meta/icon/layout/formLayout.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/formLayout.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "labelsPosition", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.formlayout.FormLayout$ResponsiveStep$LabelsPosition",
                            options = {"ASIDE", "TOP"}),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "100%")
            }
    )
    FormLayout formLayout();

    @StudioComponent(
            name = "TabSheet",
            classFqn = "io.jmix.flowui.component.tabsheet.JmixTabSheet",
            category = "Layouts",
            xmlElement = "tabSheet",
            icon = "io/jmix/flowui/kit/meta/icon/component/tabSheet.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/tabSheet.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"icon-on-top", "centered", "small", "minimal",
                                    "hide-scroll-buttons", "equal-width-tabs", "bordered", "no-padding"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    TabSheet tabSheet();
}
