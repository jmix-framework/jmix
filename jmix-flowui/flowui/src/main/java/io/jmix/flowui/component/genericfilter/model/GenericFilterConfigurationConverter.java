/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.component.genericfilter.model;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.entity.filter.GroupFilterCondition;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.jmix.flowui.component.genericfilter.FilterUtils.generateFilterPath;

@Internal
@Component("flowui_GenericFilterConfigurationConverter")
public class GenericFilterConfigurationConverter {

    protected FilterComponents filterComponents;

    @Autowired
    public void setFilterComponents(FilterComponents filterComponents) {
        this.filterComponents = filterComponents;
    }

    /**
     * Configures passed entity representing filter configuration
     * using passed filter {@link Configuration} object.
     *
     * @param configuration      filter configuration to convert
     * @param configurationModel existing entity representing filter configuration
     * @return entity representing filter configuration
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public FilterConfigurationModel toConfigurationModel(Configuration configuration,
                                                    FilterConfigurationModel configurationModel) {
        GenericFilter filter = configuration.getOwner();
        String filterComponentId = generateFilterPath(filter);

        configurationModel.setComponentId(filterComponentId);

        LogicalFilterComponent rootLogicalFilterComponent = configuration.getRootLogicalFilterComponent();
        FilterConverter converter = filterComponents.getConverterByComponentClass(rootLogicalFilterComponent.getClass(),
                configuration.getOwner());

        configurationModel.setRootCondition((GroupFilterCondition) converter.convertToModel(rootLogicalFilterComponent));

        return configurationModel;
    }

    /**
     * Converts passed entity representing filter configuration to filter {@link Configuration}.
     *
     * @param configurationModel entity to convert
     * @param filter             filter component
     * @return filter {@link Configuration} object
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Configuration toConfiguration(FilterConfigurationModel configurationModel, GenericFilter filter) {
        String id = configurationModel.getConfigurationId();
        LogicalFilterCondition rootCondition = configurationModel.getRootCondition();
        FilterConverter converter = filterComponents.getConverterByModelClass(rootCondition.getClass(), filter);
        LogicalFilterComponent<?> logicalFilterComponent =
                (LogicalFilterComponent<?>) converter.convertToComponent(rootCondition);
        Configuration configuration = new RunTimeConfiguration(id, logicalFilterComponent, filter);
        configuration.setName(configurationModel.getName());
        configuration.setAvailableForAllUsers(configurationModel.getUsername() == null);

        for (FilterComponent filterComponent : logicalFilterComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponentBase) {
                SingleFilterComponentBase<?> singleFilterComponent = ((SingleFilterComponentBase<?>) filterComponent);
                configuration.setFilterComponentDefaultValue(
                        singleFilterComponent.getParameterName(),
                        singleFilterComponent.getValue()
                );
            }
        }

        return configuration;
    }
}
