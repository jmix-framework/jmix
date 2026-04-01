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

@StudioUiKit
interface StudioDataElements {

    @StudioElement(
            name = "Instance",
            classFqn = "io.jmix.flowui.model.InstancePropertyContainer",
            xmlElement = "instance",
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/instance.svg",
            target = "io.jmix.flowui.model.InstanceContainer",
            unsupportedTarget = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
            documentationLink = "%VERSION%/flow-ui/data/property-containers.html",
            propertyGroups = StudioDataComponentPropertyGroups.NestedDataContainerDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.STRING, required = true),
            }
    )
    void nestedInstance();

    @StudioElement(
            name = "Collection",
            classFqn = "io.jmix.flowui.model.CollectionPropertyContainer",
            xmlElement = "collection",
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/collection.svg",
            target = {"io.jmix.flowui.model.InstanceContainer"},
            documentationLink = "%VERSION%/flow-ui/data/property-containers.html",
            unsupportedTarget = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
            propertyGroups = StudioDataComponentPropertyGroups.NestedDataContainerDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.STRING, required = true),
            }
    )
    void nestedCollection();

    @StudioElement(
            name = "Loader",
            classFqn = "io.jmix.flowui.model.InstanceLoader",
            xmlElement = "loader",
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/instanceLoader.svg",
            target = "io.jmix.flowui.model.InstanceContainer",
            unsupportedTarget = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer",
                    "io.jmix.flowui.model.CollectionContainer"},
            documentationLink = "%VERSION%/flow-ui/data/data-loaders.html",
            propertyGroups = StudioDataComponentPropertyGroups.QueryLoaderDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "firstResult", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "maxResults", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY)
            }
    )
    void instanceLoader();

    @StudioElement(
            name = "Loader",
            classFqn = "io.jmix.flowui.model.CollectionLoader",
            xmlElement = "loader",
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/collectionLoader.svg",
            target = "io.jmix.flowui.model.CollectionContainer",
            unsupportedTarget = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
            documentationLink = "%VERSION%/flow-ui/data/data-loaders.html",
            propertyGroups = StudioDataComponentPropertyGroups.QueryLoaderDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "firstResult", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "maxResults", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "cacheable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY)
            }
    )
    void collectionLoader();

    @StudioElement(
            name = "Loader",
            classFqn = "io.jmix.flowui.model.KeyValueInstanceLoader",
            xmlElement = "loader",
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/keyValueLoader.svg",
            target = "io.jmix.flowui.model.KeyValueContainer",
            unsupportedTarget = {"io.jmix.flowui.model.CollectionContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
            documentationLink = "%VERSION%/flow-ui/data/data-loaders.html",
            propertyGroups = {
                    StudioPropertyGroups.Store.class,
                    StudioPropertyGroups.Query.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "store", type = StudioPropertyType.STORE),
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY)
            }
    )
    void keyValueInstanceLoader();

    @StudioElement(
            name = "Loader",
            classFqn = "io.jmix.flowui.model.KeyValueCollectionLoader",
            xmlElement = "loader",
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/keyValueLoader.svg",
            target = "io.jmix.flowui.model.KeyValueCollectionContainer",
            documentationLink = "%VERSION%/flow-ui/data/data-loaders.html",
            propertyGroups = {
                    StudioPropertyGroups.FirstResult.class,
                    StudioPropertyGroups.MaxResults.class,
                    StudioPropertyGroups.Store.class,
                    StudioPropertyGroups.Query.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "firstResult", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "maxResults", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "store", type = StudioPropertyType.STORE),
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY)
            }
    )
    void keyValueCollectionLoader();

    @StudioElement(
            name = "Property",
            classFqn = "io.jmix.core.impl.keyvalue.KeyValueMetaProperty",
            xmlElement = "property",
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
            propertyGroups = {
                    StudioPropertyGroups.EntityClass.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "datatype", type = StudioPropertyType.DATATYPE_ID),
                    @StudioProperty(xmlAttribute = "class", type = StudioPropertyType.ENTITY_CLASS)
            }
    )
    void property();

    @StudioElement(
            name = "OnViewEventLoadTrigger",
            classFqn = "io.jmix.flowui.facet.dataloadcoordinator.OnViewEventLoadTrigger",
            xmlElement = "onViewEvent",
            icon = "io/jmix/flowui/kit/meta/icon/element/onViewEventLoadTrigger.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            options = {"Init", "BeforeShow", "Ready"}, required = true)
            }
    )
    void onViewEventLoadTrigger();

    @StudioElement(
            name = "OnFragmentEventLoadTrigger",
            classFqn = "io.jmix.flowui.facet.dataloadcoordinator.OnFragmentEventLoadTrigger",
            xmlElement = "onFragmentEvent",
            icon = "io/jmix/flowui/kit/meta/icon/element/onFragmentEventLoadTrigger.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            options = {"Ready", "Host.Init", "Host.BeforeShow", "Host.Ready"}, required = true)
            }
    )
    void onFragmentEventLoadTrigger();

    @StudioElement(
            name = "OnComponentValueChangedLoadTrigger",
            classFqn = "io.jmix.flowui.facet.dataloadcoordinator.OnComponentValueChangedLoadTrigger",
            xmlElement = "onComponentValueChanged",
            icon = "io/jmix/flowui/kit/meta/icon/element/onComponentValueChangedLoadTrigger.svg",
            propertyGroups = {
                    StudioPropertyGroups.Param.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = "param", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "likeClause", type = StudioPropertyType.ENUMERATION,
                            options = {"NONE", "CASE_SENSITIVE", "CASE_INSENSITIVE"}, defaultValue = "NONE"),
                    @StudioProperty(xmlAttribute = "component", type = StudioPropertyType.COMPONENT_REF, required = true)
            }
    )
    void onComponentValueChangedLoadTrigger();

    @StudioElement(
            name = "OnContainerItemChangedLoadTrigger",
            classFqn = "io.jmix.flowui.facet.dataloadcoordinator.OnContainerItemChangedLoadTrigger",
            xmlElement = "onContainerItemChanged",
            icon = "io/jmix/flowui/kit/meta/icon/element/onContainerItemChangedLoadTrigger.svg",
            propertyGroups = {
                    StudioPropertyGroups.Param.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = "param", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "container", type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF, required = true)
            }
    )
    void onContainerItemChangedLoadTrigger();
}
