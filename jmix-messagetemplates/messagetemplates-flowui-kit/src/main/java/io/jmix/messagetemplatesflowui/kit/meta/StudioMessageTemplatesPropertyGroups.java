/*
 * Copyright 2026 Haulmont.
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

import io.jmix.flowui.kit.meta.*;

@StudioAPI
final class StudioMessageTemplatesPropertyGroups {

    private StudioMessageTemplatesPropertyGroups() {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            required = true,
            options = {"grapesjs-blocks-basic", "grapesjs-blocks-flexbox", "grapesjs-custom-code",
                    "grapesjs-plugin-forms", "grapesjs-preset-newsletter", "grapesjs-parser-postcss",
                    "grapesjs-style-filter", "grapesjs-tabs", "grapesjs-tooltip", "grapesjs-tui-image-editor"}))
    public interface PluginComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ATTRIBUTES,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CATEGORY,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CONTENT,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface BlockComponent extends StudioPropertyGroups.Label, StudioPropertyGroups.RequiredId {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.CDATA))
    public interface ContentComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.CDATA))
    public interface AttributesComponent {
    }

}
