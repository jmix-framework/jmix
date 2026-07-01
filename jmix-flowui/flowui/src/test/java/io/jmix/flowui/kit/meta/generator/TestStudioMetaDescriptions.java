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

package io.jmix.flowui.kit.meta.generator;

import io.jmix.flowui.kit.meta.StudioFacet;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
public interface TestStudioMetaDescriptions {

    @StudioFacet(
            name = "TestDataLoadCoordinator",
            xmlElement = StudioXmlElements.DATA_LOAD_COORDINATOR,
            propertyGroups = TestStudioMetaPropertyGroups.DataLoadCoordinatorGeneratedProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.AUTO, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CONTAINER_PREFIX, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COMPONENT_PREFIX, type = StudioPropertyType.STRING)
            }
    )
    void dataLoadCoordinator();
}
