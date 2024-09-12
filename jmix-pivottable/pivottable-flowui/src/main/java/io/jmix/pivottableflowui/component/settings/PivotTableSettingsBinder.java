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
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            component.setInclusions(changePropertiesToLocalizedNamesWithValues(
                    settings.getInclusions(), component.getProperties()));
        }

        if (settings.getExclusions() != null && !settings.getExclusions().isEmpty()) {
            component.setExclusions(changePropertiesToLocalizedNamesWithValues(
                    settings.getExclusions(), component.getProperties()));
        }

        component.setRowOrder(Order.fromId(settings.getRowOrder()));
        component.setColOrder(Order.fromId(settings.getColOrder()));
    }

    @Nullable
    private List<String> getLocalizedPropertiesByNames(@Nullable List<String> propertiesNames,
                                                       Map<String, String> propertyToLocalizedName) {
        return propertiesNames != null ? propertiesNames.stream().map(propertyToLocalizedName::get).toList() : null;
    }

    @Nullable
    private List<String> getPropertiesByLocalizedNames(@Nullable List<String> localizedProperties,
                                                       Map<String, String> localizedNameToProperty) {
        return localizedProperties != null
                ? localizedProperties.stream().map(localizedNameToProperty::get).toList()
                : null;
    }

    @Override
    public boolean saveSettings(PivotTable component, PivotTableSettings settings) {
        Map<String, String> localizedNameToProperty = reflectProperties(component.getProperties());

        boolean changed = false;
        List<String> rowProperties = getPropertiesByLocalizedNames(component.getRows(), localizedNameToProperty);
        if (!listsEqual(rowProperties, settings.getRows())) {
            settings.setRows(rowProperties);
            changed = true;
        }
        List<String> colProperties = getPropertiesByLocalizedNames(component.getCols(), localizedNameToProperty);
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
                component.getAggregationProperties(), localizedNameToProperty);
        if (!listsEqual(aggregationProperties, settings.getAggregationProperties())) {
            settings.setAggregationProperties(aggregationProperties);
            changed = true;
        }
        Map<String, List<String>> inclusions = changeLocalizedNamesToPropertiesWithValues(
                component.getInclusions(), localizedNameToProperty);
        if (!mapsEqual(inclusions, settings.getInclusions())) {
            settings.setInclusions(inclusions);
            changed = true;
        }
        Map<String, List<String>> exclusions = changeLocalizedNamesToPropertiesWithValues(
                component.getExclusions(), localizedNameToProperty);
        if (!mapsEqual(exclusions, settings.getExclusions())) {
            settings.setExclusions(exclusions);
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

    private Map<String, String> reflectProperties(Map<String, String> properties) {
        return properties.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (key1, key2) -> {
                    throw new IllegalStateException("Duplicate localized name with keys: " + key1 + ", " + key2);
                }));
    }

    @Nullable
    private Map<String, List<String>> changeLocalizedNamesToPropertiesWithValues(
            @Nullable Map<String, List<String>> localizedPropertyToValues, Map<String, String> localizedNameToProperty) {
        if (localizedPropertyToValues == null) {
            return null;
        }
        Map<String, List<String>> propertyToValues = new HashMap<>();
        for (Map.Entry<String, List<String>> localizedEntry : localizedPropertyToValues.entrySet()) {
            propertyToValues.put(localizedNameToProperty.get(localizedEntry.getKey()), localizedEntry.getValue());
        }
        return propertyToValues;
    }

    @Nullable
    private Map<String, List<String>> changePropertiesToLocalizedNamesWithValues(
            @Nullable Map<String, List<String>> propertyWithValues, Map<String, String> propertyToLocalizedName) {
        if (propertyWithValues == null) {
            return null;
        }

        Map<String, List<String>> propertyToValues = new HashMap<>();
        for (Map.Entry<String, List<String>> localizedEntry : propertyWithValues.entrySet()) {
            propertyToValues.put(propertyToLocalizedName.get(localizedEntry.getKey()), localizedEntry.getValue());
        }
        return propertyToValues;
    }

    protected boolean listsEqual(@Nullable List<String> componentProperties,
                                 @Nullable List<String> settingsProperties) {
        if (componentProperties == null && settingsProperties == null) {
            return true;
        } else if (componentProperties != null) {
            return componentProperties.equals(settingsProperties);
        }
        return false;
    }

    protected boolean mapsEqual(@Nullable Map<String, List<String>> componentProperties,
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
