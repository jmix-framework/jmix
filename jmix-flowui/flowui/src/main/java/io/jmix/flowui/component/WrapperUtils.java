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
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for converting and mapping values that are used in framework components
 * into corresponding types used in other components or layouts.
 * <p>
 * This class is intended for internal use and should not be used directly in application code.
 */
@Internal
public class WrapperUtils {

    private WrapperUtils() {
    }

    /**
     * Converts a list of {@link SupportsResponsiveSteps.ResponsiveStep} objects to a list of
     * {@link FormLayout.ResponsiveStep} objects.
     *
     * @param responsiveSteps the list of {@link SupportsResponsiveSteps.ResponsiveStep}
     *                        objects to convert; may be empty or {@code null}
     * @return a list of {@link FormLayout.ResponsiveStep} objects; returns an empty
     * list if the input is {@code null} or empty
     */
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

    /**
     * Converts a {@link SupportsResponsiveSteps.ResponsiveStep.LabelsPosition} value
     * into a corresponding {@link FormLayout.ResponsiveStep.LabelsPosition} value.
     *
     * @param labelsPosition the {@link SupportsResponsiveSteps.ResponsiveStep.LabelsPosition}
     *                       value to convert; may be {@code null}
     * @return the corresponding {@link FormLayout.ResponsiveStep.LabelsPosition} value,
     * or {@code null} if the input is {@code null}
     * @throws IllegalArgumentException if the provided label position is not recognized
     */
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

    /**
     * Converts a {@link LogicalFilterComponent.Operation} to its corresponding {@link LogicalCondition.Type}.
     *
     * @param operation the {@link LogicalFilterComponent.Operation} to be converted; must not be {@code null}
     * @return the corresponding {@link LogicalCondition.Type} for the given operation
     * @throws IllegalArgumentException if the provided operation is not recognized
     */
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
