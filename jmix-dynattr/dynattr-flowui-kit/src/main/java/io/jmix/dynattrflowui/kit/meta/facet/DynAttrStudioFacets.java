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
package io.jmix.dynattrflowui.kit.meta.facet;

import io.jmix.flowui.kit.meta.StudioFacet;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
public interface DynAttrStudioFacets {
    @StudioFacet(
            name = "DynamicAttributes",
            classFqn = "io.jmix.dynattrflowui.facet.DynAttrFacet",
            category = "Facets",
            xmlElement = StudioXmlElements.DYNAMIC_ATTRIBUTES,
            xmlns = "http://jmix.io/schema/dynattr/flowui",
            xmlnsAlias = "dynattr",
            icon = "io/jmix/dynattrflowui/icon/facet/dynamicAttributes.svg",
            propertyGroups = {
                    StudioPropertyGroups.Id.class
            },
            properties = @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID)
    )
    void dynamicAttributesFacet();
}
