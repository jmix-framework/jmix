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

import io.jmix.flowui.kit.meta.StudioElementsGroup;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioGrapesJsElementsGroups {

    @StudioElementsGroup(
            name = "Plugins",
            elementClassFqn = "io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsPluginElement",
            xmlElement = "plugins",
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/elementsgroup/plugins.svg",
            target = {"io.jmix.messagetemplatesflowui.kit.component.JmixGrapesJs"}
    )
    void plugins();

    @StudioElementsGroup(
            name = "Blocks",
            elementClassFqn = "io.jmix.messagetemplatesflowui.kit.meta.stub.StudioGrapesJsBlockElement",
            xmlElement = "blocks",
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/elementsgroup/blocks.svg",
            target = {"io.jmix.messagetemplatesflowui.kit.component.JmixGrapesJs"}
    )
    void blocks();
}
