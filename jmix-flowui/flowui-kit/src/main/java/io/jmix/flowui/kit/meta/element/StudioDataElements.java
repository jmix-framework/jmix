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

@StudioUiKit
public interface StudioDataElements {

    @StudioElement(
            name = "Instance",
            classFqn = "io.jmix.flowui.model.InstancePropertyContainer",
            xmlElement = "instance",
            icon = "io/jmix/flowui/kit/meta/icon/datacomponent/instance.svg",
            target = "io.jmix.flowui.model.InstanceContainer",
            unsupportedTarget = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
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
            unsupportedTarget = {"io.jmix.flowui.model.KeyValueContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
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
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "dynamicAttributes", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "firstResult", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "maxResults", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "provided", type = StudioPropertyType.BOOLEAN,
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
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "dynamicAttributes", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "firstResult", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "maxResults", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "cacheable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "provided", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY)
            }
    )
    void collectionLoader();

    @StudioElement(
            name = "Loader",
            classFqn = "io.jmix.flowui.model.KeyValueInstanceLoader",
            xmlElement = "loader",
            target = "io.jmix.flowui.model.KeyValueContainer",
            unsupportedTarget = {"io.jmix.flowui.model.CollectionContainer",
                    "io.jmix.flowui.model.KeyValueCollectionContainer"},
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "provided", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "store", type = StudioPropertyType.STORE)
            }
    )
    void keyValueInstanceLoader();

    @StudioElement(
            name = "Loader",
            classFqn = "io.jmix.flowui.model.KeyValueCollectionLoader",
            xmlElement = "loader",
            target = "io.jmix.flowui.model.KeyValueCollectionContainer",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "firstResult", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "maxResults", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "provided", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "store", type = StudioPropertyType.STORE)
            }
    )
    void keyValueCollectionLoader();

    @StudioElement(
            name = "Property",
            classFqn = "io.jmix.core.impl.keyvalue.KeyValueMetaProperty",
            xmlElement = "property",
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
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
            name = "OnComponentValueChangedLoadTrigger",
            classFqn = "io.jmix.flowui.facet.dataloadcoordinator.OnComponentValueChangedLoadTrigger",
            xmlElement = "onComponentValueChanged",
            icon = "io/jmix/flowui/kit/meta/icon/element/onComponentValueChangedLoadTrigger.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "param", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "likeClause", type = StudioPropertyType.ENUMERATION,
                            options = {"NONE", "CASE_SENSITIVE", "CASE_INSENSITIVE"}, defaultValue = "NONE"),
                    @StudioProperty(xmlAttribute = "component", type = StudioPropertyType.STRING, required = true)
            }
    )
    void onComponentValueChangedLoadTrigger();

    @StudioElement(
            name = "OnContainerItemChangedLoadTrigger",
            classFqn = "io.jmix.flowui.facet.dataloadcoordinator.OnContainerItemChangedLoadTrigger",
            xmlElement = "onContainerItemChanged",
            icon = "io/jmix/flowui/kit/meta/icon/element/onContainerItemChangedLoadTrigger.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "param", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "container", type = StudioPropertyType.STRING, required = true)
            }
    )
    void onContainerItemChangedLoadTrigger();
}
