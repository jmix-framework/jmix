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

package io.jmix.flowui.component;

import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import org.apache.commons.collections4.CollectionUtils;

import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Internal
public class WrapperUtils {

    private WrapperUtils() {
    }

    public static List<FormLayout.ResponsiveStep> convertToFormLayoutResponsiveStep(
            List<SupportsResponsiveSteps.ResponsiveStep> responsiveSteps) {
        if (CollectionUtils.isEmpty(responsiveSteps)) {
            return Collections.emptyList();
        }

        return responsiveSteps.stream()
                .map(responsiveStep ->
                        new FormLayout.ResponsiveStep(responsiveStep.getMinWidth(),
                                responsiveStep.getColumns(),
                                convertToFormLayoutLabelsPosition(responsiveStep.getLabelsPosition())))
                .collect(Collectors.toUnmodifiableList());
    }

    @Nullable
    public static FormLayout.ResponsiveStep.LabelsPosition convertToFormLayoutLabelsPosition(
            @Nullable SupportsResponsiveSteps.ResponsiveStep.LabelsPosition labelsPosition) {
        if (labelsPosition == null) {
            return null;
        }

        switch (labelsPosition) {
            case TOP:
                return FormLayout.ResponsiveStep.LabelsPosition.TOP;
            case ASIDE:
                return FormLayout.ResponsiveStep.LabelsPosition.ASIDE;
            default:
                throw new IllegalArgumentException("Unknown labels position: " + labelsPosition);
        }
    }

    public static LogicalCondition.Type convertToLogicalConditionType(LogicalFilterComponent.Operation operation) {
        switch (operation) {
            case AND:
                return LogicalCondition.Type.AND;
            case OR:
                return LogicalCondition.Type.OR;
            default:
                throw new IllegalArgumentException("Unknown operation " + operation);
        }
    }
}
