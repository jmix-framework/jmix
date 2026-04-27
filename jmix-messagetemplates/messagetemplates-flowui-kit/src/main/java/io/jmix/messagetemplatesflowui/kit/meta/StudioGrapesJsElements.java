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

package io.jmix.messagetemplatesflowui.kit.meta;

import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
public interface StudioGrapesJsElements {

    @StudioElement(
            name = "Plugin",
            classFqn = "io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsPluginElement",
            xmlElement = StudioXmlElements.PLUGIN,
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/elementsgroup/plugins.svg",
            propertyGroups = StudioMessageTemplatesPropertyGroups.PluginComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING, required = true,
                            options = {"grapesjs-blocks-basic", "grapesjs-blocks-flexbox", "grapesjs-custom-code",
                                    "grapesjs-plugin-forms", "grapesjs-preset-newsletter", "grapesjs-parser-postcss",
                                    "grapesjs-style-filter", "grapesjs-tabs", "grapesjs-tooltip",
                                    "grapesjs-tui-image-editor"})
            }
    )
    void plugin();

    @StudioElement(
            name = "Block",
            classFqn = "io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsBlockElement",
            xmlElement = StudioXmlElements.BLOCK,
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/element/block.svg",
            propertyGroups = StudioMessageTemplatesPropertyGroups.BlockComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ATTRIBUTES, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CATEGORY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CONTENT, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, type = StudioPropertyType.COMPONENT_ID, category = StudioProperty.Category.GENERAL, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL, type = StudioPropertyType.LOCALIZED_STRING, category = StudioProperty.Category.GENERAL),
            }
    )
    void block();

    @StudioElement(
            name = "Content",
            xmlElement = StudioXmlElements.CONTENT,
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            target = {"io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsBlockElement"},
            unlimitedCount = false,
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/element/content.svg",
            propertyGroups = StudioMessageTemplatesPropertyGroups.ContentComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE, type = StudioPropertyType.CDATA)
            }
    )
    void content();

    @StudioElement(
            name = "Attributes",
            xmlElement = StudioXmlElements.ATTRIBUTES,
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            target = {"io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsBlockElement"},
            unlimitedCount = false,
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/element/attributes.svg",
            propertyGroups = StudioMessageTemplatesPropertyGroups.AttributesComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE, type = StudioPropertyType.CDATA)
            }
    )
    void attributes();
}
