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

package io.jmix.flowui.component.genericfilter.builder;

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.entity.filter.HeaderFilterCondition;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("flowui_PredefinedConditionBuilder")
public class PredefinedConditionBuilder extends AbstractConditionBuilder {

    protected final Messages messages;
    protected final FilterComponents filterComponents;

    public PredefinedConditionBuilder(Metadata metadata,
                                      Messages messages,
                                      FilterComponents filterComponents) {
        super(metadata);

        this.messages = messages;
        this.filterComponents = filterComponents;
    }

    @Override
    public List<FilterCondition> build(GenericFilter filter) {
        List<FilterCondition> conditions = createFilterConditions(filter);

        return conditions.size() > 1
                ? conditions
                : Collections.emptyList();
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 20;
    }

    protected List<FilterCondition> createFilterConditions(GenericFilter filter) {
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
