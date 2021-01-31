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

package io.jmix.ui.component.filter.builder;

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.HeaderFilterCondition;
import io.jmix.ui.entity.LogicalFilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("ui_PredefinedConditionBuilder")
public class PredefinedConditionBuilder extends AbstractConditionBuilder {

    @Autowired
    protected Messages messages;
    @Autowired
    protected FilterComponents filterComponents;

    @Override
    public List<FilterCondition> build(Filter filter) {
        List<FilterCondition> conditions = createFilterConditions(filter);

        return conditions.size() > 1
                ? conditions
                : Collections.emptyList();
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 20;
    }

    protected List<FilterCondition> createFilterConditions(Filter filter) {
        List<FilterComponent> components = filter.getConditions();
        List<FilterCondition> conditions = new ArrayList<>();

        HeaderFilterCondition conditionsHeaderCondition = createHeaderFilterCondition(
                messages.getMessage(PredefinedConditionBuilder.class, "predefinedConditionBuilder.headerCaption"));
        conditions.add(conditionsHeaderCondition);

        for (FilterComponent component : components) {
            FilterConverter converter =
                    filterComponents.getConverterByComponentClass(component.getClass(), filter);
            FilterCondition condition = converter.convertToModel(component);
            condition.setParent(conditionsHeaderCondition);
            conditions.add(condition);

            if (condition instanceof LogicalFilterCondition) {
                List<FilterCondition> children = createFilterConditionsByLogicalFilterCondition(
                        (LogicalFilterCondition) condition, condition, true);
                conditions.addAll(children);
            }
        }

        return conditions;
    }

    public List<FilterCondition> createFilterConditionsByLogicalFilterCondition(LogicalFilterCondition logicalFilterCondition,
                                                                                FilterCondition parent,
                                                                                boolean isRootGroupFilterComponent) {
        List<FilterCondition> conditions = new ArrayList<>();

        if (!isRootGroupFilterComponent) {
            logicalFilterCondition.setParent(parent);
            conditions.add(logicalFilterCondition);
        }

        if (logicalFilterCondition.getOwnFilterConditions() != null) {
            for (FilterCondition ownFilterCondition : logicalFilterCondition.getOwnFilterConditions()) {
                FilterCondition parentCondition = isRootGroupFilterComponent ? parent : logicalFilterCondition;
                if (ownFilterCondition instanceof LogicalFilterCondition) {
                    List<FilterCondition> children = createFilterConditionsByLogicalFilterCondition(
                            (LogicalFilterCondition) ownFilterCondition,
                            parentCondition,
                            false);
                    conditions.addAll(children);
                } else {
                    ownFilterCondition.setParent(parentCondition);
                    conditions.add(ownFilterCondition);
                }
            }
        }

        return conditions;
    }
}
