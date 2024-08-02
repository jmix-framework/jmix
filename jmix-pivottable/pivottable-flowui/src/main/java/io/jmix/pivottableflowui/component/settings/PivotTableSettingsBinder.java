/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.component.settings;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.binder.ComponentSettingsBinder;
import io.jmix.pivottableflowui.component.PivotTable;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.LinkedList;
import java.util.List;

@org.springframework.stereotype.Component("flowui_PivotTableSettingsBinder")
public class PivotTableSettingsBinder implements ComponentSettingsBinder<PivotTable, PivotTableSettings> {

    private static final Logger log = LoggerFactory.getLogger(PivotTableSettingsBinder.class);

    @Override
    public Class<? extends Component> getComponentClass() {
        return PivotTable.class;
    }

    @Override
    public Class<? extends Settings> getSettingsClass() {
        return PivotTableSettings.class;
    }

    protected PivotTableSettings createSettings() {
        return new PivotTableSettings();
    }

    @Override
    public void applySettings(PivotTable component, PivotTableSettings settings) {
        if (CollectionUtils.isNotEmpty(settings.getRows())) {
            List<String> newRows = new LinkedList<>();
            for (String row : settings.getRows()) {
                if (CollectionUtils.isNotEmpty(component.getRows()) && component.getRows().contains(row)) {
                    newRows.add(row);
                }
            }
            component.setRows(newRows);
        }

        if (CollectionUtils.isNotEmpty(settings.getCols())) {
            List<String> newCols = new LinkedList<>();
            for (String col : settings.getCols()) {
                if (CollectionUtils.isNotEmpty(component.getCols()) && component.getCols().contains(col)) {
                    newCols.add(col);
                }
            }
            component.setCols(newCols);
        }
    }

    @Override
    public boolean saveSettings(PivotTable component, PivotTableSettings settings) {
        boolean changed = false;
        if (propertiesOrderChanged(component.getRows(), settings.getRows())) {
            settings.setRows(component.getRows());
            changed = true;
        }
        if (propertiesOrderChanged(component.getCols(), settings.getCols())) {
            settings.setCols(component.getCols());
            changed = true;
        }

        return changed;
    }

    private boolean propertiesOrderChanged(@Nullable List<String> componentProperties,
                                           @Nullable List<String> settingsProperties) {
        if (componentProperties == null && settingsProperties == null) {
            return false;
        } else if (componentProperties != null) {
            return !componentProperties.equals(settingsProperties);
        }
        return true;
    }

    @Override
    public PivotTableSettings getSettings(PivotTable component) {
        PivotTableSettings settings = createSettings();
        settings.setId(component.getId().orElse(null));

        settings.setCols(component.getCols());
        settings.setRows(component.getRows());
        settings.setRendererName(component.getRenderers().getSelectedRenderer().getId());
        settings.setAggregatorName(component.getAggregations().getSelectedAggregation().getId());
//        settings.setVals(component.getVals());
//        settings.setInclusions(component.getInclusions());
//        settings.setExclusions(component.getExclusions());
        settings.setRowOrder(component.getRowOrder().getId());
        settings.setRowOrder(component.getRowOrder().getId());

        return settings;
    }
}
