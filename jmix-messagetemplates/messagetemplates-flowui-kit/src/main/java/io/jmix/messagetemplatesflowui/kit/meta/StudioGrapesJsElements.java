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

@StudioUiKit
public interface StudioGrapesJsElements {

    @StudioElement(
            name = "Plugin",
            classFqn = "io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsPluginElement",
            xmlElement = "plugin",
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/elementsgroup/plugins.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.STRING, required = true,
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
            xmlElement = "block",
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/element/block.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "attributes", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "category", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "content", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
            }
    )
    void block();

    @StudioElement(
            name = "Content",
            xmlElement = "content",
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            target = {"io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsBlockElement"},
            unlimitedCount = false,
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/element/content.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.CDATA)
            }
    )
    void content();

    @StudioElement(
            name = "Attributes",
            xmlElement = "attributes",
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            target = {"io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsBlockElement"},
            unlimitedCount = false,
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/element/attributes.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.CDATA)
            }
    )
    void attributes();
}
