/*
 * Copyright 2024 Haulmont.
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

import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
public interface StudioSupersetElements {

    @StudioElement(
            name = "DatasetConstraint",
            classFqn = "io.jmix.supersetflowui.component.dataconstraint.DatasetConstraint",
            xmlElement = StudioXmlElements.DATASET_CONSTRAINT,
            xmlns = "http://jmix.io/schema/superset/ui",
            xmlnsAlias = "superset",
            icon = "io/jmix/supersetflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioSupersetPropertyGroups.DatasetConstraintComponent.class)
    void datasetConstraint();
}
