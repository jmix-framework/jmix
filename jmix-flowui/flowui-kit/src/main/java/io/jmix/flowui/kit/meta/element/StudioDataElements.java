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

package io.jmix.flowui.kit.meta.element;

import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.datacomponent.StudioDataComponentPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
interface StudioDataElements {

    @StudioElement(
            name = "Instance",
            classFqn = "io.jmix.flowui.model.InstancePropertyContainer",
            xmlElement = StudioXmlElements.INSTANCE,
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/instance.svg",
            target = "io.jmix.flowui.model.InstanceContainer",
            unsupportedTarget = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
            documentationLink = "%VERSION%/flow-ui/data/property-containers.html",
            propertyGroups = StudioDataComponentPropertyGroups.NestedDataContainerDefaultProperties.class)
    void nestedInstance();

    @StudioElement(
            name = "Collection",
            classFqn = "io.jmix.flowui.model.CollectionPropertyContainer",
            xmlElement = StudioXmlElements.COLLECTION,
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/collection.svg",
            target = {"io.jmix.flowui.model.InstanceContainer"},
            documentationLink = "%VERSION%/flow-ui/data/property-containers.html",
            unsupportedTarget = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
            propertyGroups = StudioDataComponentPropertyGroups.NestedDataContainerDefaultProperties.class)
    void nestedCollection();

    @StudioElement(
            name = "Loader",
            classFqn = "io.jmix.flowui.model.InstanceLoader",
            xmlElement = StudioXmlElements.LOADER,
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/instanceLoader.svg",
            target = "io.jmix.flowui.model.InstanceContainer",
            unsupportedTarget = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer",
                    "io.jmix.flowui.model.CollectionContainer"},
            documentationLink = "%VERSION%/flow-ui/data/data-loaders.html",
            propertyGroups = StudioDataComponentPropertyGroups.InstanceLoaderComponent.class)
    void instanceLoader();

    @StudioElement(
            name = "Loader",
            classFqn = "io.jmix.flowui.model.CollectionLoader",
            xmlElement = StudioXmlElements.LOADER,
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/collectionLoader.svg",
            target = "io.jmix.flowui.model.CollectionContainer",
            unsupportedTarget = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
            documentationLink = "%VERSION%/flow-ui/data/data-loaders.html",
            propertyGroups = StudioDataComponentPropertyGroups.CollectionLoaderComponent.class)
    void collectionLoader();

    @StudioElement(
            name = "Loader",
            classFqn = "io.jmix.flowui.model.KeyValueInstanceLoader",
            xmlElement = StudioXmlElements.LOADER,
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/keyValueLoader.svg",
            target = "io.jmix.flowui.model.KeyValueContainer",
            unsupportedTarget = {"io.jmix.flowui.model.CollectionContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
            documentationLink = "%VERSION%/flow-ui/data/data-loaders.html",
            propertyGroups = {
                    StudioPropertyGroups.Id.class,
                    StudioPropertyGroups.Store.class,
                    StudioPropertyGroups.Query.class
            })
    void keyValueInstanceLoader();

    @StudioElement(
            name = "Loader",
            classFqn = "io.jmix.flowui.model.KeyValueCollectionLoader",
            xmlElement = StudioXmlElements.LOADER,
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/keyValueLoader.svg",
            target = "io.jmix.flowui.model.KeyValueCollectionContainer",
            documentationLink = "%VERSION%/flow-ui/data/data-loaders.html",
            propertyGroups = StudioPropertyGroups.KeyValueCollectionLoaderDefaultProperties.class)
    void keyValueCollectionLoader();

    @StudioElement(
            name = "Property",
            classFqn = "io.jmix.core.impl.keyvalue.KeyValueMetaProperty",
            xmlElement = StudioXmlElements.PROPERTY,
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
            propertyGroups = StudioDataComponentPropertyGroups.PropertyComponent.class)
    void property();

    @StudioElement(
            name = "OnViewEventLoadTrigger",
            classFqn = "io.jmix.flowui.facet.dataloadcoordinator.OnViewEventLoadTrigger",
            xmlElement = StudioXmlElements.ON_VIEW_EVENT,
            icon = "io/jmix/flowui/kit/meta/icon/element/onViewEventLoadTrigger.svg",
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TYPE, type = StudioPropertyType.ENUMERATION,
                            options = {"Init", "BeforeShow", "Ready"}, required = true)
            }
    )
    void onViewEventLoadTrigger();

    @StudioElement(
            name = "OnFragmentEventLoadTrigger",
            classFqn = "io.jmix.flowui.facet.dataloadcoordinator.OnFragmentEventLoadTrigger",
            xmlElement = StudioXmlElements.ON_FRAGMENT_EVENT,
            icon = "io/jmix/flowui/kit/meta/icon/element/onFragmentEventLoadTrigger.svg",
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TYPE, type = StudioPropertyType.ENUMERATION,
                            options = {"Ready", "Host.Init", "Host.BeforeShow", "Host.Ready"}, required = true)
            }
    )
    void onFragmentEventLoadTrigger();

    @StudioElement(
            name = "OnComponentValueChangedLoadTrigger",
            classFqn = "io.jmix.flowui.facet.dataloadcoordinator.OnComponentValueChangedLoadTrigger",
            xmlElement = StudioXmlElements.ON_COMPONENT_VALUE_CHANGED,
            icon = "io/jmix/flowui/kit/meta/icon/element/onComponentValueChangedLoadTrigger.svg",
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PARAM, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIKE_CLAUSE, type = StudioPropertyType.ENUMERATION,
                            options = {"NONE", "CASE_SENSITIVE", "CASE_INSENSITIVE"}, defaultValue = "NONE"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COMPONENT, type = StudioPropertyType.COMPONENT_REF, required = true)
            }
    )
    void onComponentValueChangedLoadTrigger();

    @StudioElement(
            name = "OnContainerItemChangedLoadTrigger",
            classFqn = "io.jmix.flowui.facet.dataloadcoordinator.OnContainerItemChangedLoadTrigger",
            xmlElement = StudioXmlElements.ON_CONTAINER_ITEM_CHANGED,
            icon = "io/jmix/flowui/kit/meta/icon/element/onContainerItemChangedLoadTrigger.svg",
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PARAM, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CONTAINER, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF, required = true)
            }
    )
    void onContainerItemChangedLoadTrigger();
}
