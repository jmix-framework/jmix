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
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseHtmlComponent.class)
    Hr hr();

    @StudioComponent(
            name = "HtmlObject",
            classFqn = "com.vaadin.flow.component.html.HtmlObject",
            category = "HTML",
            xmlElement = StudioXmlElements.HTML_OBJECT,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.HtmlObjectComponent.class,
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
            propertyGroups = StudioHtmlComponentPropertyGroups.IframeComponent.class)
    IFrame iframe();

    @StudioComponent(
            name = "Image",
            classFqn = "io.jmix.flowui.component.image.JmixImage",
            category = "HTML",
            xmlElement = StudioXmlElements.IMAGE,
            icon = "io/jmix/flowui/kit/meta/icon/html/image.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/image.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.ImageHtmlComponent.class,
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
            propertyGroups = StudioHtmlComponentPropertyGroups.InputComponent.class)
    Input input();

    @StudioComponent(
            name = "ListItem",
            classFqn = "com.vaadin.flow.component.html.ListItem",
            category = "HTML",
            xmlElement = StudioXmlElements.LIST_ITEM,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
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
            propertyGroups = StudioHtmlComponentPropertyGroups.ParamComponent.class)
    Param param();

    @StudioComponent(
            name = "Pre",
            classFqn = "com.vaadin.flow.component.html.Pre",
            category = "HTML",
            xmlElement = StudioXmlElements.PRE,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
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
            propertyGroups = StudioHtmlComponentPropertyGroups.RangeInputComponent.class)
    RangeInput rangeInput();

    @StudioComponent(
            name = "Section",
            classFqn = "com.vaadin.flow.component.html.Section",
            category = "HTML",
            xmlElement = StudioXmlElements.SECTION,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            propertyGroups = StudioHtmlComponentPropertyGroups.BaseClickableHtmlContainer.class,
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
            propertyGroups = StudioHtmlComponentPropertyGroups.NativeDetailsComponent.class)
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
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    FieldSet fieldSet();
}
