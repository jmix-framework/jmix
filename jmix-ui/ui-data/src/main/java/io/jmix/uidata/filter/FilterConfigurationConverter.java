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

package io.jmix.uidata.filter;


import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.component.filter.configuration.RunTimeConfiguration;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.entity.GroupFilterCondition;
import io.jmix.ui.entity.LogicalFilterCondition;
import io.jmix.uidata.entity.FilterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.jmix.ui.component.filter.FilterUtils.generateFilterPath;

@Internal
@Component("ui_FilterConfigurationConverter")
public class FilterConfigurationConverter {

    @Autowired
    protected FilterComponents filterComponents;

    @Autowired
    private EntityFieldCreationSupport entityFieldCreationSupport;

    public FilterConfiguration toConfigurationModel(Filter.Configuration configuration,
                                                    FilterConfiguration configurationModel) {
        Filter filter = configuration.getOwner();
        String filterComponentId = generateFilterPath(filter);
        configurationModel.setComponentId(filterComponentId);

        LogicalFilterComponent rootLogicalFilterComponent = configuration.getRootLogicalFilterComponent();
        FilterConverter converter = filterComponents.getConverterByComponentClass(rootLogicalFilterComponent.getClass(),
                configuration.getOwner());
        configurationModel.setRootCondition(
                (GroupFilterCondition) converter.convertToModel(rootLogicalFilterComponent));

        return configurationModel;
    }

    public Filter.Configuration toConfiguration(FilterConfiguration configurationModel, Filter filter) {
        String id = configurationModel.getConfigurationId();
        LogicalFilterCondition rootCondition = configurationModel.getRootCondition();
        FilterConverter converter = filterComponents.getConverterByModelClass(rootCondition.getClass(), filter);
        LogicalFilterComponent logicalFilterComponent = (LogicalFilterComponent) converter.convertToComponent(rootCondition);
        Filter.Configuration configuration = new RunTimeConfiguration(id, logicalFilterComponent, filter);
        configuration.setName(configurationModel.getName());
        configuration.setAvailableForAllUsers(configurationModel.getUsername() == null);

        for (FilterComponent filterComponent : logicalFilterComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponent) {
                configuration.setFilterComponentDefaultValue(((SingleFilterComponent<?>) filterComponent).getParameterName(),
                        ((SingleFilterComponent<?>) filterComponent).getValue());
            }
        }

        return configuration;
    }
}
