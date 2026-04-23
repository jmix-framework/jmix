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

package io.jmix.supersetflowui.kit.meta;

import io.jmix.flowui.kit.meta.*;

@StudioAPI
final class StudioSupersetPropertyGroups {

    private StudioSupersetPropertyGroups() {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATASET_ID,
                            type = StudioPropertyType.INTEGER,
                            required = true),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CONSTRAINT,
                            type = StudioPropertyType.CDATA)
            }
    )
    public interface DatasetConstraintComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CHART_CONTROLS_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EMBEDDED_ID,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FILTERS_EXPANDED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TITLE_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    public interface SupersetDashboardComponent extends StudioPropertyGroups.AddonComponentDefaultProperties {
    }

}