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
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component("pvttbl_PivotTableSettingsBinder")
public class PivotTableSettingsBinder implements ComponentSettingsBinder<PivotTable<?>, PivotTableSettings> {

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
    public void applySettings(PivotTable<?> component, PivotTableSettings settings) {
        component.setRows(settings.getRows());
        component.setColumns(settings.getCols());

        if (!Strings.isNullOrEmpty(settings.getRendererName())) {
            if (component.getRenderers() != null) {
                component.getRenderers().setSelectedRenderer(Renderer.fromId(settings.getRendererName()));
            } else {
                component.setRenderer(Renderer.fromId(settings.getRendererName()));
            }
        }

        if (!Strings.isNullOrEmpty(settings.getAggregatorName())) {
            if (component.getAggregations() != null) {
                component.getAggregations().setSelectedAggregation(
                        AggregationMode.fromId(settings.getAggregatorName()));
            } else {
                Aggregation aggregation = component.getAggregation();
                if (aggregation == null) {
                    aggregation = new Aggregation();
                    component.setAggregation(aggregation);
                }
                aggregation.setMode(AggregationMode.fromId(settings.getAggregatorName()));
            }
        }

        component.setAggregationProperties(settings.getAggregationProperties());
        component.setInclusions(settings.getInclusions());
        component.setExclusions(settings.getExclusions());

        component.setRowOrder(Order.fromId(settings.getRowOrder()));
        component.setColumnOrder(Order.fromId(settings.getColOrder()));
    }

    @Override
    public boolean saveSettings(PivotTable<?> component, PivotTableSettings settings) {
        boolean changed = false;

        List<String> rowProperties = component.getRows();
        if (listsNotEqual(rowProperties, settings.getRows())) {
            settings.setRows(rowProperties);
            changed = true;
        }

        List<String> colProperties = component.getColumns();
        if (listsNotEqual(colProperties, settings.getCols())) {
            settings.setCols(colProperties);
            changed = true;
        }

        Renderer selectedRenderer = null;
        if (component.getRenderers() != null) {
            selectedRenderer = component.getRenderers().getSelectedRenderer();
        }
        if (selectedRenderer == null) {
            selectedRenderer = component.getRenderer();
        }
        if (selectedRenderer != null && !selectedRenderer.getId().equals(settings.getRendererName())) {
            settings.setRendererName(selectedRenderer.getId());
            changed = true;
        }

        AggregationMode selectedAggregation = null;
        if (component.getAggregations() != null) {
            selectedAggregation = component.getAggregations().getSelectedAggregation();
        }
        if (selectedAggregation == null && component.getAggregation() != null) {
            selectedAggregation = component.getAggregation().getMode();
        }
        if (selectedAggregation != null && !selectedAggregation.getId().equals(settings.getAggregatorName())) {
            settings.setAggregatorName(selectedAggregation.getId());
            changed = true;
        }

        List<String> aggregationProperties = component.getAggregationProperties();
        if (listsNotEqual(aggregationProperties, settings.getAggregationProperties())) {
            settings.setAggregationProperties(aggregationProperties);
            changed = true;
        }

        Map<String, List<String>> inclusions = component.getInclusions();
        if (mapsNotEqual(inclusions, settings.getInclusions())) {
            settings.setInclusions(inclusions);
            changed = true;
        }

        Map<String, List<String>> exclusions = component.getExclusions();
        if (mapsNotEqual(exclusions, settings.getExclusions())) {
            settings.setExclusions(exclusions);
            changed = true;
        }

        if (component.getRowOrder() != Order.fromId(settings.getRowOrder())) {
            settings.setRowOrder(component.getRowOrder().getId());
            changed = true;
        }

        if (component.getColumnOrder() != Order.fromId(settings.getColOrder())) {
            settings.setColOrder(component.getColumnOrder().getId());
            changed = true;
        }

        return changed;
    }

    protected boolean listsNotEqual(@Nullable List<String> componentProperties,
                                    @Nullable List<String> settingsProperties) {
        if (componentProperties == null && settingsProperties == null) {
            return false;
        } else if (componentProperties != null) {
            return !componentProperties.equals(settingsProperties);
        }
        return true;
    }

    protected boolean mapsNotEqual(@Nullable Map<String, List<String>> componentProperties,
                                   @Nullable Map<String, List<String>> settingsProperties) {
        if (componentProperties == null && settingsProperties == null) {
            return false;
        } else if (componentProperties != null && settingsProperties != null &&
                componentProperties.keySet().equals(settingsProperties.keySet())) {
            for (Map.Entry<String, List<String>> entry : componentProperties.entrySet()) {
                if (entry.getValue() != null && listsNotEqual(entry.getValue(), settingsProperties.get(entry.getKey()))) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public PivotTableSettings getSettings(PivotTable<?> component) {
        PivotTableSettings settings = createSettings();
        settings.setId(component.getId().orElse(null));

        settings.setCols(component.getColumns());
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
