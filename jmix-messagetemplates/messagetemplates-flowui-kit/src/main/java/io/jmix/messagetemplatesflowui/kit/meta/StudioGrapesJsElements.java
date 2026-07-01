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
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
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
            propertyGroups = StudioMessageTemplatesPropertyGroups.PluginComponent.class)
    void plugin();

    @StudioElement(
            name = "Block",
            classFqn = "io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsBlockElement",
            xmlElement = StudioXmlElements.BLOCK,
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/element/block.svg",
            propertyGroups = StudioMessageTemplatesPropertyGroups.BlockComponent.class)
    void block();

    @StudioElement(
            name = "Content",
            xmlElement = StudioXmlElements.CONTENT,
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            target = {"io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsBlockElement"},
            unlimitedCount = false,
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/element/content.svg",
            propertyGroups = StudioMessageTemplatesPropertyGroups.ContentComponent.class)
    void content();

    @StudioElement(
            name = "Attributes",
            xmlElement = StudioXmlElements.ATTRIBUTES,
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            target = {"io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsBlockElement"},
            unlimitedCount = false,
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/element/attributes.svg",
            propertyGroups = StudioMessageTemplatesPropertyGroups.AttributesComponent.class)
    void attributes();
}
