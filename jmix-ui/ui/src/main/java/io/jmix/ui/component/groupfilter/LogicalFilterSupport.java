/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.groupfilter;

import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.LogicalFilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Internal
@Component("ui_LogicalFilterSupport")
public class LogicalFilterSupport {

    @Autowired
    protected Messages messages;

    public String getOperationCaption(LogicalFilterComponent.Operation operation) {
        return messages.getMessage("logicalFilterComponent.Operation." + operation.name());
    }

    public String getOperationCaption(LogicalFilterComponent.Operation operation, boolean operationCaptionVisible) {
        return operationCaptionVisible
                ? getOperationCaption(operation)
                : "";
    }

    public List<FilterCondition> getChildrenConditions(LogicalFilterCondition parent) {
        List<FilterCondition> filterConditions = new ArrayList<>();
        parent.getOwnFilterConditions().forEach(filterCondition -> {
            filterConditions.add(filterCondition);
            if (filterCondition instanceof LogicalFilterCondition) {
                filterConditions.addAll(getChildrenConditions((LogicalFilterCondition) filterCondition));
            }
        });
        return filterConditions;
    }

    @Nullable
    public FilterCondition findSelectedConditionFromRootFilterCondition(LogicalFilterCondition rootCondition,
                                                                        @Nullable FilterCondition selectedCondition) {
        if (selectedCondition == null) {
            return null;
        }

        List<FilterCondition> childrenCondition = getChildrenConditions(rootCondition);
        for (FilterCondition condition : childrenCondition) {
            if (condition.equals(selectedCondition)) {
                return condition;
            }

            if (condition instanceof LogicalFilterCondition) {
                return findSelectedConditionFromRootFilterCondition((LogicalFilterCondition) condition, selectedCondition);
            }
        }

        return null;
    }
}
