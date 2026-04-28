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
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import io.jmix.flowui.kit.component.sidepanellayout.JmixSidePanelLayout;
import io.jmix.flowui.kit.component.gridlayout.JmixGridLayout;
import io.jmix.flowui.kit.meta.*;
import io.jmix.flowui.kit.meta.StudioAvailableChildrenInfo.ClassInfo;
import io.jmix.flowui.kit.meta.StudioAvailableChildrenInfo.TagInfo;
import io.jmix.flowui.kit.meta.StudioXmlElementInitializer.AttributeInitializer;
import io.jmix.flowui.kit.meta.StudioXmlElementInitializer.ChildXmlElementInitializer;

import static io.jmix.flowui.kit.meta.StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN;

@StudioUiKit
interface StudioLayouts {

    @StudioComponent(
            name = "Details",
            classFqn = "io.jmix.flowui.component.details.JmixDetails",
            category = "Layouts",
            xmlElement = StudioXmlElements.DETAILS,
            icon = "io/jmix/flowui/kit/meta/icon/layout/details.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/details.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "div"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "hbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "vbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "flexLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
            }),
            propertyGroups = StudioPropertyGroups.DetailsDefaultProperties.class)
    Details details();

    @StudioComponent(
            name = "HBox",
            classFqn = "com.vaadin.flow.component.orderedlayout.HorizontalLayout",
            category = "Layouts",
            xmlElement = StudioXmlElements.HBOX,
            icon = "io/jmix/flowui/kit/meta/icon/layout/hbox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/hbox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "div"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "vbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "details"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "flexLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
            }),
            propertyGroups = {
                    StudioPropertyGroups.ComponentLayout.class,
                    StudioPropertyGroups.PaddingWithFalseDefaultValue.class,
                    StudioPropertyGroups.WidthWithDefaultValueAuto.class,
            })
    HorizontalLayout hbox();

    @StudioComponent(
            name = "VBox",
            classFqn = "com.vaadin.flow.component.orderedlayout.VerticalLayout",
            category = "Layouts",
            xmlElement = StudioXmlElements.VBOX,
            icon = "io/jmix/flowui/kit/meta/icon/layout/vbox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/vbox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "div"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "hbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "details"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "flexLayout"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
            }),
            propertyGroups = {
                    StudioPropertyGroups.ComponentLayout.class,
                    StudioPropertyGroups.PaddingWithTrueDefaultValue.class,
                    StudioPropertyGroups.WidthWithDefaultValue100.class,
            })
    VerticalLayout vbox();

    @StudioComponent(
            name = "FlexLayout",
            classFqn = "com.vaadin.flow.component.orderedlayout.FlexLayout",
            category = "Layouts",
            xmlElement = StudioXmlElements.FLEX_LAYOUT,
            icon = "io/jmix/flowui/kit/meta/icon/layout/hbox.svg",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "div"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "hbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "vbox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "details"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "formLayout"),
            }),
            propertyGroups = StudioPropertyGroups.FlexLayoutComponent.class)
    FlexLayout flexLayout();

    @StudioComponent(
            name = "Scroller",
            classFqn = "io.jmix.flowui.component.scroller.JmixScroller",
            category = "Layouts",
            xmlElement = StudioXmlElements.SCROLLER,
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
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @ClassInfo(qualifiedName = FLOW_COMPONENT_FQN, maxCount = 1)
            ),
            propertyGroups = StudioPropertyGroups.ScrollerComponent.class)
    Scroller scroller();

    @StudioComponent(
            name = "Split",
            classFqn = "io.jmix.flowui.component.splitlayout.JmixSplitLayout",
            category = "Layouts",
            xmlElement = StudioXmlElements.SPLIT,
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
            propertyGroups = StudioPropertyGroups.SplitLayoutComponent.class)
    SplitLayout splitLayout();

    @StudioComponent(
            name = "Accordion",
            classFqn = "io.jmix.flowui.component.accordion.JmixAccordion",
            category = "Layouts",
            xmlElement = StudioXmlElements.ACCORDION,
            icon = "io/jmix/flowui/kit/meta/icon/layout/accordion.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/accordion.html",
            propertyGroups = StudioPropertyGroups.AutoWidthLayoutDefaultProperties.class)
    Accordion accordion();

    @StudioComponent(
            name = "SidePanelLayout",
            classFqn = "io.jmix.flowui.component.sidepanellayout.SidePanelLayout",
            category = "Layouts",
            xmlElement = StudioXmlElements.SIDE_PANEL_LAYOUT,
            icon = "io/jmix/flowui/kit/meta/icon/layout/sidePanelLayout.svg",
            propertyGroups = StudioPropertyGroups.SidePanelLayoutComponent.class,
            availableChildren = @StudioAvailableChildrenInfo(totalChildrenCount = 2),
            xmlElementInitializer = @StudioXmlElementInitializer(
                    preview = """
                            <sidePanelLayout height="100%" width="100%">
                                <vbox id="contentBox"/>
                                <vbox id="sidePanelBox" height="100%" width="100%">
                                    <hbox id="headerBox" width="100%">
                                        <sidePanelLayoutCloser/>
                                    </hbox>
                                </vbox>
                            </sidePanelLayout>
                            """,
                    childElementInitializers = {
                            @ChildXmlElementInitializer(
                                    qualifiedName = "vbox",
                                    path = "contentBox",
                                    attributeInitializers = {
                                            @AttributeInitializer(qualifiedName = "id", attributeValue = "contentBox")
                                    }
                            ),
                            @ChildXmlElementInitializer(
                                    qualifiedName = "vbox",
                                    path = "sidePanelBox",
                                    attributeInitializers = {
                                            @AttributeInitializer(qualifiedName = "id", attributeValue = "sidePanelBox"),
                                            @AttributeInitializer(qualifiedName = "width", attributeValue = "100%"),
                                            @AttributeInitializer(qualifiedName = "height", attributeValue = "100%")
                                    }
                            ),
                            @ChildXmlElementInitializer(
                                    qualifiedName = "hbox",
                                    path = "headerBox",
                                    parentPath = "sidePanelBox",
                                    attributeInitializers = {
                                            @AttributeInitializer(qualifiedName = "id", attributeValue = "headerBox"),
                                            @AttributeInitializer(qualifiedName = "width", attributeValue = "100%")
                                    }
                            ),
                            @ChildXmlElementInitializer(
                                    path = "sidePanelLayoutCloser",
                                    parentPath = "headerBox",
                                    qualifiedName = "sidePanelLayoutCloser"
                            )
                    })
                    )
    JmixSidePanelLayout sidePanelLayout();

    @StudioComponent(
            name = "FormLayout",
            classFqn = "io.jmix.flowui.component.formlayout.JmixFormLayout",
            category = "Layouts",
            xmlElement = StudioXmlElements.FORM_LAYOUT,
            icon = "io/jmix/flowui/kit/meta/icon/layout/formLayout.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/formLayout.html",
            propertyGroups = StudioPropertyGroups.FormLayoutComponent.class)
    FormLayout formLayout();

    @StudioComponent(
            name = "TabSheet",
            classFqn = "io.jmix.flowui.component.tabsheet.JmixTabSheet",
            category = "Layouts",
            xmlElement = StudioXmlElements.TAB_SHEET,
            icon = "io/jmix/flowui/kit/meta/icon/component/tabSheet.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/tabSheet.html",
            propertyGroups = StudioPropertyGroups.TabSheetComponent.class)
    TabSheet tabSheet();

    @StudioComponent(
            name = "Card",
            classFqn = "io.jmix.flowui.component.card.JmixCard",
            category = "Layouts",
            xmlElement = StudioXmlElements.CARD,
            propertyGroups = StudioPropertyGroups.CardComponent.class,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = {
                            @TagInfo(qualifiedName = "title", maxCount = 1),
                            @TagInfo(qualifiedName = "subtitle", maxCount = 1),
                            @TagInfo(qualifiedName = "media", maxCount = 1),
                            @TagInfo(qualifiedName = "content", maxCount = 1),
                            @TagInfo(qualifiedName = "headerPrefix", maxCount = 1),
                            @TagInfo(qualifiedName = "header", maxCount = 1),
                            @TagInfo(qualifiedName = "headerSuffix", maxCount = 1),
                            @TagInfo(qualifiedName = "footer", maxCount = 1),
                    }
            )
    )
    Card card();

    @StudioComponent(
            name = "GridLayout",
            classFqn = "io.jmix.flowui.component.gridlayout.GridLayout",
            category = "Layouts",
            xmlElement = StudioXmlElements.GRID_LAYOUT,
            icon = "io/jmix/flowui/kit/meta/icon/layout/gridLayout.svg",
            propertyGroups = StudioPropertyGroups.GridLayoutComponent.class,
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.ComponentRenderer"
                    )
            }
    )
    JmixGridLayout<?> gridLayout();
}
