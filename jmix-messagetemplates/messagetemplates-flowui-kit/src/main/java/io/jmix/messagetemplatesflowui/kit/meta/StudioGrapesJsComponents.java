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

import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;

import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlElements;
@StudioUiKit(studioClassloaderDependencies = "io.jmix.messagetemplates:jmix-messagetemplates-flowui-kit")
public interface StudioGrapesJsComponents {

    @StudioComponent(
            name = "GrapesJs",
            classFqn = "io.jmix.messagetemplatesflowui.kit.component.GrapesJs",
            category = "Components",
            xmlElement = StudioXmlElements.GRAPES_JS,
            xmlns = "http://jmix.io/schema/messagetemplates/ui",
            xmlnsAlias = "msgtmp",
            icon = "io/jmix/messagetemplatesflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioPropertyGroups.NoOptionSizedAddonComponentDefaultProperties.class,
                    StudioPropertyGroups.ReadOnly.class
            })
    GrapesJs grapesJs();
}
