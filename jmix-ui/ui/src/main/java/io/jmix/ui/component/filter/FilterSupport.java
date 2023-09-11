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

package io.jmix.ui.component.filter;

import com.google.common.collect.ImmutableSet;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.Actions;
import io.jmix.ui.Fragments;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.action.filter.FilterClearValuesAction;
import io.jmix.ui.action.filter.FilterCopyAction;
import io.jmix.ui.action.filter.FilterEditAction;
import io.jmix.ui.app.filter.configuration.FilterConfigurationModelFragment;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.SingleFilterComponent;
import io.jmix.ui.component.filter.configuration.RunTimeConfiguration;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Internal
@Component("ui_FilterSupport")
public class FilterSupport {

    @Autowired
    protected Actions actions;

    public List<FilterAction> getDefaultFilterActions(Filter filter) {
        List<FilterAction> filterActions = new ArrayList<>();
        for (String actionId : getDefaultFilterActionIds()) {
            filterActions.add(createFilterAction(actionId, filter));
        }
        return filterActions;
    }

    public Map<Filter.Configuration, Boolean> getConfigurationsMap(Filter filter) {
        return Collections.emptyMap();
    }

    public Filter.Configuration saveCurrentFilterConfiguration(Filter.Configuration configuration,
                                                               boolean isNewConfiguration,
                                                               LogicalFilterComponent rootFilterComponent,
                                                               ScreenFragment configurationFragment) {
        String id = "";
        String name = "";
        if (configurationFragment instanceof FilterConfigurationModelFragment) {
            id = ((FilterConfigurationModelFragment) configurationFragment).getConfigurationId();
            name = ((FilterConfigurationModelFragment) configurationFragment).getConfigurationName();
        }

        return initFilterConfiguration(id, name, configuration, isNewConfiguration, rootFilterComponent);
    }

    public void removeCurrentFilterConfiguration(Filter filter) {
        filter.removeConfiguration(filter.getCurrentConfiguration());
    }

    public ScreenFragment createFilterConfigurationFragment(FrameOwner owner,
                                                            boolean isNewConfiguration,
                                                            Filter filter) {
        Fragments fragments = UiControllerUtils.getScreenContext(owner).getFragments();
        FilterConfigurationModelFragment fragment = fragments.create(owner, FilterConfigurationModelFragment.class);
        initFilterConfigurationFragment(fragment, isNewConfiguration, filter.getCurrentConfiguration());
        return fragment;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> initConfigurationValuesMap(Filter.Configuration configuration) {
        Map<String, Object> valuesMap = new HashMap<>();
        LogicalFilterComponent rootLogicalComponent = configuration.getRootLogicalFilterComponent();
        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponent) {
                String parameterName = ((SingleFilterComponent<?>) filterComponent).getParameterName();
                valuesMap.put(parameterName, ((SingleFilterComponent<?>) filterComponent).getValue());
                ((SingleFilterComponent) filterComponent).setValue(configuration.getFilterComponentDefaultValue(parameterName));
            }
        }

        return valuesMap;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void resetConfigurationValuesMap(Filter.Configuration configuration, Map<String, Object> valuesMap) {
        LogicalFilterComponent rootLogicalComponent = configuration.getRootLogicalFilterComponent();
        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponent) {
                ((SingleFilterComponent) filterComponent).setValue(
                        valuesMap.get(((SingleFilterComponent<?>) filterComponent).getParameterName()));
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void refreshConfigurationValuesMap(Filter.Configuration configuration, Map<String, Object> valuesMap) {
        LogicalFilterComponent rootLogicalComponent = configuration.getRootLogicalFilterComponent();
        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponent) {
                String parameterName = ((SingleFilterComponent<?>) filterComponent).getParameterName();
                Object value = valuesMap.get(parameterName);
                Object defaultValue = configuration.getFilterComponentDefaultValue(parameterName);

                if (value == null && defaultValue != null) {
                    ((SingleFilterComponent) filterComponent).setValue(defaultValue);
                } else {
                    try {
                        ((SingleFilterComponent) filterComponent).setValue(value);
                    } catch (ClassCastException e) {
                        ((SingleFilterComponent) filterComponent).setValue(defaultValue);
                    }
                }
            }
        }
    }

    public void refreshConfigurationDefaultValues(Filter.Configuration configuration) {
        configuration.resetAllDefaultValues();
        LogicalFilterComponent rootLogicalComponent = configuration.getRootLogicalFilterComponent();
        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponent) {
                configuration.setFilterComponentDefaultValue(
                        ((SingleFilterComponent<?>) filterComponent).getParameterName(),
                        ((SingleFilterComponent<?>) filterComponent).getValue());
            }
        }
    }

    protected Filter.Configuration initFilterConfiguration(String id,
                                                           String name,
                                                           Filter.Configuration existedConfiguration,
                                                           boolean isNewConfiguration,
                                                           LogicalFilterComponent rootFilterComponent) {
        Filter.Configuration resultConfiguration;
        if (isNewConfiguration) {
            resultConfiguration = new RunTimeConfiguration(id, rootFilterComponent, existedConfiguration.getOwner());
        } else if (!existedConfiguration.getId().equals(id)) {
            Filter owner = existedConfiguration.getOwner();
            resultConfiguration = new RunTimeConfiguration(id, rootFilterComponent, owner);
            owner.removeConfiguration(existedConfiguration);
        } else {
            resultConfiguration = existedConfiguration;
            resultConfiguration.setRootLogicalFilterComponent(rootFilterComponent);
        }
        resultConfiguration.setName(name);

        return resultConfiguration;
    }

    protected void initFilterConfigurationFragment(ScreenFragment fragment,
                                                   boolean isNewConfiguration,
                                                   Filter.Configuration currentConfiguration) {
        if (fragment instanceof FilterConfigurationModelFragment) {
            if (!isNewConfiguration) {
                ((FilterConfigurationModelFragment) fragment).setConfigurationId(currentConfiguration.getId());
            }

            ((FilterConfigurationModelFragment) fragment).setConfigurationName(currentConfiguration.getName());
        }
    }

    protected Set<String> getDefaultFilterActionIds() {
        return ImmutableSet.of(
                FilterEditAction.ID,
                FilterCopyAction.ID,
                FilterClearValuesAction.ID
        );
    }

    protected FilterAction createFilterAction(String filterActionId,
                                              Filter filter) {
        FilterAction filterAction = actions.create(filterActionId);
        filterAction.setFilter(filter);
        return filterAction;
    }
}
