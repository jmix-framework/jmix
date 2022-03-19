/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.settings.binder;

import com.google.common.base.Strings;
import com.haulmont.cuba.gui.components.Filter;
import com.haulmont.cuba.gui.components.filter.FilterDelegate;
import com.haulmont.cuba.security.entity.FilterEntity;
import com.haulmont.cuba.settings.component.CubaFilterSettings;
import com.haulmont.cuba.web.gui.components.WebFilter;
import io.jmix.core.JmixOrder;
import io.jmix.ui.component.Component;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import io.jmix.ui.settings.component.binder.DataLoadingSettingsBinder;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.core.annotation.Order;

import java.util.Objects;

import static com.haulmont.cuba.gui.components.filter.FilterDelegateSettingsUtils.*;

@Order(JmixOrder.LOWEST_PRECEDENCE)
@org.springframework.stereotype.Component(CubaFilterSettingsBinder.NAME)
public class CubaFilterSettingsBinder implements DataLoadingSettingsBinder<Filter, CubaFilterSettings> {

    public static final String NAME = "cuba_CubaFilterSettingsBinder";

    @Override
    public Class<? extends Component> getComponentClass() {
        return WebFilter.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return CubaFilterSettings.class;
    }

    @Override
    public void applySettings(Filter component, SettingsWrapper wrapper) {
        CubaFilterSettings settings = wrapper.getSettings();

        if (settings.getGroupBoxExpanded() != null) {
            component.setExpanded(settings.getGroupBoxExpanded());
        }

        FilterDelegate delegate = getFilterDelegate(component);

        if (!applyMaxResultsSettingsBeforeLoad(delegate) && settings.getMaxResults() != null) {
            applyMaxResultsSettings(delegate, settings.getMaxResults());
        }
    }

    @Override
    public void applyDataLoadingSettings(Filter component, SettingsWrapper wrapper) {
        CubaFilterSettings settings = wrapper.getSettings();
        FilterDelegate delegate = getFilterDelegate(component);

        if (applyMaxResultsSettingsBeforeLoad(delegate) && settings.getMaxResults() != null) {
            applyMaxResultsSettings(delegate, settings.getMaxResults());
        }
    }

    @Override
    public boolean saveSettings(Filter component, SettingsWrapper wrapper) {
        CubaFilterSettings settings = wrapper.getSettings();
        if (!isSettingsChanged(component, settings)) {
            return false;
        }

        FilterDelegate delegate = getFilterDelegate(component);

        if (isMaxResultsLayoutVisible(delegate)) {
            settings.setMaxResults(getMaxResultsValue(delegate));
        }

        settings.setGroupBoxExpanded(component.isExpanded());

        FilterEntity defaultFilter = getDefaultFilter(delegate);
        if (defaultFilter == null) {
            settings.setDefaultFilterId(null);
            settings.setApplyDefault(null);
        } else {
            settings.setDefaultFilterId(defaultFilter.getId().toString());
            settings.setApplyDefault(defaultFilter.getApplyDefault());
        }

        return true;
    }

    @Override
    public CubaFilterSettings getSettings(Filter component) {
        CubaFilterSettings settings = createSettings();
        settings.setId(component.getId());
        settings.setGroupBoxExpanded(component.isExpanded());

        FilterDelegate delegate = getFilterDelegate(component);
        if (isMaxResultsLayoutVisible(delegate)) {
            settings.setMaxResults(getMaxResultsValue(delegate));
        }

        FilterEntity defaultFilter = getDefaultFilter(delegate);
        if (defaultFilter != null) {
            settings.setDefaultFilterId(defaultFilter.getId().toString());
            settings.setApplyDefault(defaultFilter.getApplyDefault());
        }

        return settings;
    }

    protected boolean isSettingsChanged(Filter component, CubaFilterSettings settings) {
        FilterDelegate delegate = getFilterDelegate(component);
        Integer maxResults = getMaxResultsValue(delegate);

        if (isMaxResultsLayoutVisible(delegate)
                && !Objects.equals(maxResults, settings.getMaxResults())) {
            return true;
        }

        boolean settingsGroupBoxExpanded = BooleanUtils.toBoolean(settings.getGroupBoxExpanded());
        if (component.isExpanded() != settingsGroupBoxExpanded) {
            return true;
        }

        FilterEntity defaultFilter = getDefaultFilter(delegate);

        if (defaultFilter != null) {
            String defaultFilterId = defaultFilter.getId().toString();
            if (!defaultFilterId.equals(settings.getDefaultFilterId())) {
                return true;
            }

            if (BooleanUtils.toBoolean(defaultFilter.getApplyDefault())
                    != BooleanUtils.toBoolean(settings.getApplyDefault())) {
                return true;
            }
        } else return !Strings.isNullOrEmpty(settings.getDefaultFilterId());

        return false;
    }

    protected CubaFilterSettings createSettings() {
        return new CubaFilterSettings();
    }

    protected FilterDelegate getFilterDelegate(Filter filter) {
        return ((WebFilter) filter).getDelegate();
    }
}
