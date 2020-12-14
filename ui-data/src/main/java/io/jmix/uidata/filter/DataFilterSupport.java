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

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.action.filter.FilterClearValuesAction;
import io.jmix.ui.action.filter.FilterCopyAction;
import io.jmix.ui.action.filter.FilterEditAction;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.filter.FilterSupport;
import io.jmix.uidata.action.filter.FilterRemoveAction;
import io.jmix.uidata.action.filter.FilterSaveAction;
import io.jmix.uidata.action.filter.FilterSaveAsAction;
import io.jmix.uidata.entity.FilterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Internal
public class DataFilterSupport extends FilterSupport {

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected FilterConfigurationConverter filterConfigurationConverter;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Override
    public List<FilterAction> getDefaultFilterActions(Filter filter) {
        List<FilterAction> filterActions = new ArrayList<>();
        filterActions.add(createFilterAction(FilterSaveAction.class, filter));
        filterActions.add(createFilterAction(FilterSaveAsAction.class, filter));
        filterActions.add(createFilterAction(FilterEditAction.class, filter));
        filterActions.add(createFilterAction(FilterRemoveAction.class, filter));
        filterActions.add(createFilterAction(FilterCopyAction.class, filter));
        filterActions.add(createFilterAction(FilterClearValuesAction.class, filter));
        return filterActions;
    }

    @Override
    public List<Filter.Configuration> getConfigurations(Filter filter) {
        List<FilterConfiguration> filterConfigurations = loadFilterConfigurations(filter);

        List<Filter.Configuration> configurations = new ArrayList<>();
        for (FilterConfiguration filterConfiguration : filterConfigurations) {
            Filter.Configuration configuration =
                    filterConfigurationConverter.toConfiguration(filterConfiguration, filter);
            configurations.add(configuration);
        }
        return configurations;
    }

    @Nullable
    public FilterConfiguration loadFilterConfiguration(Filter.Configuration configuration) {
        Filter filterComponent = configuration.getOwner();
        String filterComponentId = filterConfigurationConverter.generateFilterComponentId(filterComponent);

        return dataManager.load(FilterConfiguration.class)
                .condition(LogicalCondition.and()
                        .add(PropertyCondition.equal("componentId", filterComponentId))
                        .add(PropertyCondition.equal("code", configuration.getCode()))
                        .add(LogicalCondition.or()
                                .add(PropertyCondition.equal("username", ""))
                                .add(PropertyCondition.equal("username",
                                        currentAuthentication.getUser().getUsername()))))
                .optional()
                .orElse(null);
    }

    @Override
    public boolean filterConfigurationExists(String configurationCode, Filter filter) {
        String filterComponentId = filterConfigurationConverter.generateFilterComponentId(filter);

        return dataManager.load(FilterConfiguration.class)
                .condition(LogicalCondition.and()
                        .add(PropertyCondition.equal("componentId", filterComponentId))
                        .add(PropertyCondition.equal("code", configurationCode))
                        .add(LogicalCondition.or()
                                .add(PropertyCondition.equal("username", ""))
                                .add(PropertyCondition.equal("username",
                                        currentAuthentication.getUser().getUsername()))))
                .optional()
                .isPresent();
    }

    @Override
    public void removeCurrentFilterConfiguration(Filter filter) {
        super.removeCurrentFilterConfiguration(filter);

        FilterConfiguration filterConfiguration = loadFilterConfiguration(filter.getCurrentConfiguration());
        if (filterConfiguration != null) {
            dataManager.remove(filterConfiguration);
        }
    }

    public void saveFilterConfiguration(Filter.Configuration configuration) {
        FilterConfiguration filterConfiguration;
        FilterConfiguration existedFilterConfiguration = loadFilterConfiguration(configuration);
        if (existedFilterConfiguration != null) {
            filterConfiguration =
                    filterConfigurationConverter.toFilterConfiguration(configuration, existedFilterConfiguration);
        } else {
            filterConfiguration = filterConfigurationConverter.toFilterConfiguration(configuration);
        }
        dataManager.save(filterConfiguration);
    }

    protected List<FilterConfiguration> loadFilterConfigurations(Filter filter) {
        String filterComponentId = filterConfigurationConverter.generateFilterComponentId(filter);
        return dataManager.load(FilterConfiguration.class)
                .condition(LogicalCondition.and()
                        .add(PropertyCondition.equal("componentId", filterComponentId))
                        .add(LogicalCondition.or()
                                .add(PropertyCondition.equal("username", ""))
                                .add(PropertyCondition.equal("username",
                                        currentAuthentication.getUser().getUsername()))))
                .list();
    }
}
