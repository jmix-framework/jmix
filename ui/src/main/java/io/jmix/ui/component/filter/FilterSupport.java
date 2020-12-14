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

import io.jmix.core.annotation.Internal;
import io.jmix.ui.Actions;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.Screens;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.action.filter.FilterClearValuesAction;
import io.jmix.ui.action.filter.FilterCopyAction;
import io.jmix.ui.action.filter.FilterEditAction;
import io.jmix.ui.app.filter.configuration.FilterConfigurationFormFragment;
import io.jmix.ui.component.Filter;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.sys.ScreensHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Internal
@Component("ui_FilterSupport")
public class FilterSupport {

    @Autowired
    protected Actions actions;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected FilterConditionsBuilder builder;
    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected ScreensHelper screensHelper;

    public List<FilterAction> getDefaultFilterActions(Filter filter) {
        List<FilterAction> filterActions = new ArrayList<>();
        filterActions.add(createFilterAction(FilterEditAction.class, filter));
        filterActions.add(createFilterAction(FilterCopyAction.class, filter));
        filterActions.add(createFilterAction(FilterClearValuesAction.class, filter));
        return filterActions;
    }

    public List<Filter.Configuration> getConfigurations(Filter filter) {
        return Collections.emptyList();
    }

    public boolean filterConfigurationExists(String configurationCode, Filter filter) {
        return filter.getConfigurations().stream()
                .anyMatch(configuration -> configurationCode.equals(configuration.getCode()));
    }

    public void removeFilterConfiguration(Filter.Configuration configuration, Filter filter) {
        filter.removeConfiguration(configuration);
    }

    public Class<? extends ScreenFragment> getConfigurationFormFragmentClass() {
        return FilterConfigurationFormFragment.class;
    }

    protected FilterAction createFilterAction(Class<? extends FilterAction> filterActionClass,
                                              Filter filter) {
        FilterAction filterAction = actions.create(filterActionClass);
        filterAction.setFilter(filter);
        return filterAction;
    }
}
