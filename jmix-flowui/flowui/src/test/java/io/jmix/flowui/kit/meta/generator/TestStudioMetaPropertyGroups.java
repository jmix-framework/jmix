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

import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyGroup;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;

class TestStudioMetaPropertyGroups {

    private TestStudioMetaPropertyGroups() {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.AUTO,
            type = StudioPropertyType.BOOLEAN))
    interface AutoWithoutDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COMPONENT_PREFIX,
            type = StudioPropertyType.STRING))
    interface ComponentPrefixWithoutDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CONTAINER_PREFIX,
            type = StudioPropertyType.STRING))
    interface ContainerPrefixWithoutDefaultValue {
    }

    @StudioPropertyGroup
    interface DataLoadCoordinatorGeneratedProperties extends StudioPropertyGroups.Id,
            AutoWithoutDefaultValue, ComponentPrefixWithoutDefaultValue, ContainerPrefixWithoutDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            required = true),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DELAY,
                            type = StudioPropertyType.INTEGER,
                            required = true),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.REPEATING,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AUTOSTART,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    interface TimerPropertiesWithDifferentRepeatingDefaultValue {
    }
}
