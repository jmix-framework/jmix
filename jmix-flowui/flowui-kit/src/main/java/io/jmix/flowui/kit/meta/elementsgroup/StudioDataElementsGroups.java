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

package io.jmix.flowui.kit.meta.elementsgroup;

import io.jmix.flowui.kit.meta.StudioElementsGroup;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioDataElementsGroups {

    @StudioElementsGroup(
            name = "Refresh",
            elementClassFqn = "io.jmix.flowui.facet.DataLoadCoordinator.Trigger",
            xmlElement = "refresh",
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/refresh.svg",
            target = {"io.jmix.flowui.facet.DataLoadCoordinator"},
            unlimitedCount = true,
            properties = {
                    @StudioProperty(xmlAttribute = "loader", type = StudioPropertyType.STRING, required = true)
            }
    )
    void refresh();

    @StudioElementsGroup(
            name = "Properties",
            elementClassFqn = "io.jmix.core.impl.keyvalue.KeyValueMetaProperty",
            xmlElement = "properties",
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/properties.svg",
            target = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionLoader"}
    )
    void properties();
}
