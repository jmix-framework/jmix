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
import io.jmix.pivottableflowui.kit.component.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@org.springframework.stereotype.Component("pvttbl_PivotTableSettingsBinder")
public class PivotTableSettingsBinder implements ComponentSettingsBinder<PivotTable, PivotTableSettings> {

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
            component.setRows(getLocalizedPropertiesByNames(settings.getRows(), component.getProperties()));
        }

        if (CollectionUtils.isNotEmpty(settings.getCols())) {
            component.setCols(getLocalizedPropertiesByNames(settings.getCols(), component.getProperties()));
        }

        if (!Strings.isNullOrEmpty(settings.getRendererName())) {
            if (component.getRenderers() != null) {
                component.getRenderers().setSelectedRenderer(Renderer.fromId(settings.getRendererName()));
            } else {
                component.setRenderer(Renderer.fromId(settings.getRendererName()));
            }
        }

        if (!Strings.isNullOrEmpty(settings.getAggregatorName())) {
            if (component.getAggregations() != null) {
                component.getAggregations().setSelectedAggregation(AggregationMode.fromId(settings.getAggregatorName()));
            } else {
                Aggregation aggregation = component.getAggregation();
                if (aggregation == null) {
                    aggregation = new Aggregation();
                    component.setAggregation(aggregation);
                }
                aggregation.setMode(AggregationMode.fromId(settings.getAggregatorName()));
            }
        }

        if (CollectionUtils.isNotEmpty(settings.getAggregationProperties())) {
            component.setAggregationProperties(getLocalizedPropertiesByNames(
                    settings.getAggregationProperties(), component.getProperties()));
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

    private List<String> getLocalizedPropertiesByNames(@Nullable List<String> propertiesNames,
                                                       Map<String, String> propertiesMapping) {
        List<String> localizedProperties = new LinkedList<>();
        if (propertiesNames != null) {
            for (String propertyName : propertiesNames) {
                for (Map.Entry<String, String> entry : propertiesMapping.entrySet()) {
                    if (propertyName.equals(entry.getKey())) {
                        localizedProperties.add(entry.getValue());
                    }
                }
            }
        }
        return localizedProperties;
    }

    private List<String> getPropertiesByLocalizedNames(@Nullable List<String> localizedProperties,
                                                       Map<String, String> propertiesMapping) {
        List<String> properties = new LinkedList<>();
        if (localizedProperties != null) {
            for (String localizedProperty : localizedProperties) {
                for (Map.Entry<String, String> entry : propertiesMapping.entrySet()) {
                    if (localizedProperty.equals(entry.getValue())) {
                        properties.add(entry.getKey());
                    }
                }
            }
        }
        return properties;
    }

    @Override
    public boolean saveSettings(PivotTable component, PivotTableSettings settings) {
        boolean changed = false;
        List<String> rowProperties = getPropertiesByLocalizedNames(component.getRows(), component.getProperties());
        if (!listsEqual(rowProperties, settings.getRows())) {
            settings.setRows(rowProperties);
            changed = true;
        }
        List<String> colProperties = getPropertiesByLocalizedNames(component.getCols(), component.getProperties());
        if (!listsEqual(colProperties, settings.getCols())) {
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
        List<String> aggregationProperties = getPropertiesByLocalizedNames(
                component.getAggregationProperties(), component.getProperties());
        if (!listsEqual(aggregationProperties, settings.getAggregationProperties())) {
            settings.setAggregationProperties(aggregationProperties);
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
        if (component.getRowOrder() != Order.fromId(settings.getRowOrder())) {
            settings.setRowOrder(component.getRowOrder().getId());
            changed = true;
        }
        if (component.getColOrder() != Order.fromId(settings.getColOrder())) {
            settings.setColOrder(component.getColOrder().getId());
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
