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
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.entity.filter.HeaderFilterCondition;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component("flowui_ConfigurationConditionBuilder")
public class ConfigurationConditionBuilder extends AbstractConditionBuilder {

    protected final Messages messages;
    protected final FilterComponents filterComponents;

    public ConfigurationConditionBuilder(Metadata metadata,
                                         Messages messages,
                                         FilterComponents filterComponents) {
        super(metadata);

        this.messages = messages;
        this.filterComponents = filterComponents;
    }

    @Override
    public List<FilterCondition> build(GenericFilter filter) {
        List<Configuration> configurations = filter.getConfigurations().stream()
                .filter(configuration -> configuration != filter.getCurrentConfiguration())
                .collect(Collectors.toList());

        List<FilterCondition> conditions = createFilterConditionsByConfigurations(configurations);

        return conditions.size() > 1
                ? conditions
                : Collections.emptyList();
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 30;
    }

    protected List<FilterCondition> createFilterConditionsByConfigurations(List<Configuration> configurations) {
        List<FilterCondition> conditions = new ArrayList<>();

        HeaderFilterCondition configurationsHeaderCondition = createHeaderFilterCondition(
                messages.getMessage(ConfigurationConditionBuilder.class,
                        "configurationConditionBuilder.headerCaption"));
        conditions.add(configurationsHeaderCondition);

        for (Configuration configuration : configurations) {
            conditions.addAll(createFilterConditionsByConfiguration(configuration,
                    configurationsHeaderCondition, true));
        }

        return conditions;
    }

    protected List<FilterCondition> createFilterConditionsByConfiguration(Configuration configuration,
                                                                          @Nullable FilterCondition parent,
                                                                          boolean addHeaderCondition) {
        List<FilterCondition> conditions = new ArrayList<>();

        LogicalFilterComponent rootLogicalFilterComponent = configuration.getRootLogicalFilterComponent();
        FilterConverter converter = filterComponents.getConverterByComponentClass(rootLogicalFilterComponent.getClass(),
                configuration.getOwner());
        LogicalFilterCondition rootGroupCondition =
                (LogicalFilterCondition) converter.convertToModel(rootLogicalFilterComponent);

        FilterCondition configurationHeaderCondition = null;
        if (addHeaderCondition) {
            configurationHeaderCondition = createHeaderFilterCondition(configuration.getName());
            configurationHeaderCondition.setParent(parent);
            conditions.add(configurationHeaderCondition);
        }

        List<FilterCondition> groupConditions = createFilterConditionsByLogicalFilterCondition(rootGroupCondition,
                addHeaderCondition ? configurationHeaderCondition : parent,
                true);
        conditions.addAll(groupConditions);

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
