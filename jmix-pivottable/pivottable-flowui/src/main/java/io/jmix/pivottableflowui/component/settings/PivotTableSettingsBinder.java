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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.binder.ComponentSettingsBinder;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.kit.component.model.Aggregation;
import io.jmix.pivottableflowui.kit.component.model.AggregationMode;
import io.jmix.pivottableflowui.kit.component.model.Order;
import io.jmix.pivottableflowui.kit.component.model.Renderer;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component("pvttbl_PivotTableSettingsBinder")
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

        if (!Strings.isNullOrEmpty(settings.getRendererName())) {
            component.setRenderer(Renderer.fromId(settings.getRendererName()));
        }

        if (!Strings.isNullOrEmpty(settings.getAggregatorName())) {
            Aggregation componentAggregation = component.getAggregation();
            if (componentAggregation == null || !componentAggregation.getMode().getId().equals(settings.getAggregatorName())) {
                Aggregation settingsAggregation = new Aggregation();
                settingsAggregation.setMode(AggregationMode.fromId(settings.getAggregatorName()));

                if (CollectionUtils.isNotEmpty(settings.getAggregationProperties())) {
                    component.setAggregationProperties(settings.getAggregationProperties());
                }
            }
        }

        if (settings.getInclusions() != null && !settings.getInclusions().isEmpty()) {
            component.setInclusions(settings.getInclusions());
        }

        if (settings.getExclusions() != null && !settings.getExclusions().isEmpty()) {
            component.setExclusions(settings.getExclusions());
        }

        component.setRowOrder(Order.fromId(settings.getRowOrder()));
        component.setColOrder(Order.fromId(settings.getColOrder()));
    }

    @Override
    public boolean saveSettings(PivotTable component, PivotTableSettings settings) {
        boolean changed = false;
        if (!listsEqual(component.getRows(), settings.getRows())) {
            settings.setRows(component.getRows());
            changed = true;
        }
        if (!listsEqual(component.getCols(), settings.getCols())) {
            settings.setCols(component.getCols());
            changed = true;
        }
        if (component.getRenderer() != null && !component.getRenderer().getId().equals(settings.getRendererName())) {
            settings.setRendererName(component.getRenderer().getId());
            changed = true;
        }
        if (component.getAggregation() != null && !component.getAggregation().getMode().getId().equals(settings.getAggregatorName())) {
            settings.setAggregatorName(component.getAggregation().getMode().getId());
            changed = true;
        }
        if (!listsEqual(component.getAggregationProperties(), settings.getAggregationProperties())) {
            settings.setAggregationProperties(component.getAggregationProperties());
            changed = true;
        }
        if (!mapsEqual(component.getInclusions(), settings.getInclusions())) {
            settings.setInclusions(component.getInclusions());
            changed = true;
        }
        if (!mapsEqual(component.getExclusions(), settings.getExclusions())) {
            settings.setExclusions(component.getExclusions());
            changed = true;
        }

        return changed;
    }

    private boolean listsEqual(@Nullable List<String> componentProperties,
                               @Nullable List<String> settingsProperties) {
        if (componentProperties == null && settingsProperties == null) {
            return true;
        } else if (componentProperties != null) {
            return componentProperties.equals(settingsProperties);
        }
        return false;
    }

    private boolean mapsEqual(@Nullable Map<String, List<String>> componentProperties,
                               @Nullable Map<String, List<String>> settingsProperties) {
        if (componentProperties == null && settingsProperties == null) {
            return true;
        } else if (componentProperties != null && settingsProperties != null &&
                componentProperties.keySet().equals(settingsProperties.keySet())) {
            for (Map.Entry<String, List<String>> entry : componentProperties.entrySet()) {
                if (entry.getValue() != null && !listsEqual(entry.getValue(), settingsProperties.get(entry.getKey()))) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public PivotTableSettings getSettings(PivotTable component) {
        PivotTableSettings settings = createSettings();
        settings.setId(component.getId().orElse(null));

        settings.setCols(component.getCols());
        settings.setRows(component.getRows());
        settings.setRendererName(component.getRenderers().getSelectedRenderer().getId());
        settings.setAggregatorName(component.getAggregations().getSelectedAggregation().getId());
        settings.setAggregationProperties(component.getAggregationProperties());
        settings.setInclusions(component.getInclusions());
        settings.setExclusions(component.getExclusions());
        settings.setRowOrder(component.getRowOrder().getId());
        settings.setRowOrder(component.getRowOrder().getId());

        return settings;
    }
}
