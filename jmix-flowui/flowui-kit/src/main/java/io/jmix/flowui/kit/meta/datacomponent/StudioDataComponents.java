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

package io.jmix.flowui.kit.meta.datacomponent;

import io.jmix.flowui.kit.meta.StudioDataComponent;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
interface StudioDataComponents {

    @StudioDataComponent(
            name = "Collection Container",
            classFqn = "io.jmix.flowui.model.CollectionContainer",
            category = "Data Components",
            xmlElement = StudioXmlElements.COLLECTION,
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/collection.svg",
            documentationLink = "%VERSION%/flow-ui/data/collection-container.html",
            propertyGroups = StudioDataComponentPropertyGroups.DataContainerDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENTITY_CLASS, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FETCH_PLAN, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.FETCH_PLAN)
            }
    )
    void collection();

    @StudioDataComponent(
            name = "Instance Container",
            classFqn = "io.jmix.flowui.model.InstanceContainer",
            category = "Data Components",
            xmlElement = StudioXmlElements.INSTANCE,
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/instance.svg",
            documentationLink = "%VERSION%/flow-ui/data/instance-container.html",
            propertyGroups = StudioDataComponentPropertyGroups.DataContainerDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENTITY_CLASS, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FETCH_PLAN, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.FETCH_PLAN)
            }
    )
    void instance();

    @StudioDataComponent(
            name = "KeyValueInstance Container",
            classFqn = "io.jmix.flowui.model.KeyValueContainer",
            category = "Data Components",
            xmlElement = StudioXmlElements.KEY_VALUE_INSTANCE,
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/keyValueInstance.svg",
            documentationLink = "%VERSION%/flow-ui/data/key-value-containers.html",
            propertyGroups = StudioPropertyGroups.RequiredId.class,
            properties = @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true)
    )
    void keyValueInstance();

    @StudioDataComponent(
            name = "KeyValueCollection Container",
            classFqn = "io.jmix.flowui.model.KeyValueCollectionContainer",
            category = "Data Components",
            xmlElement = StudioXmlElements.KEY_VALUE_COLLECTION,
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/keyValueCollection.svg",
            documentationLink = "%VERSION%/flow-ui/data/key-value-containers.html",
            propertyGroups = StudioPropertyGroups.RequiredId.class,
            properties = @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true)
    )
    void keyValueCollection();
}
