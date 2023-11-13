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

package io.jmix.flowui.facet.settings.component.binder;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.pagination.PaginationSettingsUtils;
import io.jmix.flowui.component.pagination.SimplePagination;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.SimplePaginationSettings;
import org.springframework.lang.Nullable;

import java.util.Objects;

@org.springframework.stereotype.Component("flowui_SimplePaginationSettingsBinder")
public class SimplePaginationSettingsBinder implements
        DataLoadingSettingsBinder<SimplePagination, SimplePaginationSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return SimplePagination.class;
    }

    @Override
    public Class<? extends Settings> getSettingsClass() {
        return SimplePaginationSettings.class;
    }

    @Override
    public void applySettings(SimplePagination component, SimplePaginationSettings settings) {
        // do nothing
    }

    @Override
    public void applyDataLoadingSettings(SimplePagination component, SimplePaginationSettings settings) {
        if (component.isItemsPerPageVisible()) {
            if (settings.getItemsPerPageValue() != null) {
                PaginationSettingsUtils.setItemsPerPageValue(component, settings.getItemsPerPageValue());
            }
        }
    }

    @Override
    public boolean saveSettings(SimplePagination component, SimplePaginationSettings settings) {
        if (isItemsPerPageValueChanged(component, settings)) {
            settings.setItemsPerPageValue(getItemsPerPageValue(component));
            return true;
        }
        return false;
    }

    protected boolean isItemsPerPageValueChanged(SimplePagination component, SimplePaginationSettings settings) {
        return !Objects.equals(getItemsPerPageValue(component), settings.getItemsPerPageValue());
    }

    @Override
    public SimplePaginationSettings getSettings(SimplePagination component) {
        SimplePaginationSettings settings = createSettings();
        settings.setId(component.getId().orElse(null));
        settings.setItemsPerPageValue(getItemsPerPageValue(component));
        return settings;
    }

    protected SimplePaginationSettings createSettings() {
        return new SimplePaginationSettings();
    }

    @Nullable
    protected Integer getItemsPerPageValue(SimplePagination component) {
        return PaginationSettingsUtils.getItemsPerPageValue(component);
    }
}
