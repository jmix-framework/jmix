/*
 * Copyright 2020 Haulmont.
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

package io.jmix.dynattrui.facet;

import io.jmix.ui.component.Facet;
import io.jmix.ui.meta.StudioFacet;

@StudioFacet(
        xmlElement = "dynamicAttributes",
        caption = "DynamicAttributes",
        description = "Shows dynamic attributes on a screen",
        category = "Facets",
        icon = "io/jmix/dynattrui/icon/facet/dynamicAttributes.svg",
        xmlns = "http://jmix.io/schema/dynattr/ui",
        xmlnsAlias = "dynattr"
)
public interface DynAttrFacet extends Facet {
    String FACET_NAME = "dynamicAttributes";
}
