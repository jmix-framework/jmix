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

package io.jmix.flowui.kit.meta.datacomponent;

import io.jmix.flowui.kit.meta.StudioAPI;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyGroup;
import io.jmix.flowui.kit.meta.StudioPropertyGroups.*;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;

@StudioAPI
public final class StudioDataComponentPropertyGroups {

    @StudioPropertyGroup
    public interface DataContainerDefaultProperties extends RequiredId, RequiredEntityClass, FetchPlan {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PROPERTY,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL,
            required = true))
    public interface NestedDataContainerDefaultProperties extends RequiredId {
    }

    @StudioPropertyGroup
    public interface InstanceLoaderComponent extends Id, Query, ReadOnly, MaxResults, FirstResult {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CACHEABLE,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL,
            defaultValue = "false"))
    public interface CollectionLoaderComponent extends Id, Query, ReadOnly, MaxResults, FirstResult {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DATATYPE,
            type = StudioPropertyType.DATATYPE_ID,
            category = StudioProperty.Category.GENERAL))
    public interface PropertyComponent extends EntityClass, RequiredStringName {
    }

    private StudioDataComponentPropertyGroups() {
    }

}
