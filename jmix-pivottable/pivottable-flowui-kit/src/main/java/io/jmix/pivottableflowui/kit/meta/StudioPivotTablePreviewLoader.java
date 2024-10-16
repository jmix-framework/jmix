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

package io.jmix.pivottableflowui.kit.meta;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import io.jmix.pivottableflowui.kit.component.model.*;
import jakarta.annotation.Nullable;
import org.dom4j.Element;

import java.util.*;

public class StudioPivotTablePreviewLoader implements StudioPreviewComponentLoader {

    @Override
    public boolean isSupported(Element element) {
        return "http://jmix.io/schema/pvttbl/ui".equals(element.getNamespaceURI())
                && "pivotTable".equals(element.getName());
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        JmixPivotTable<StudioPivotTableShape> resultComponent = new JmixPivotTable<>();
        createData(resultComponent);

        loadClassNames(resultComponent, componentElement);
        loadEnabled(resultComponent, componentElement);
        loadSizeAttributes(resultComponent, componentElement);

        loadOptions(resultComponent, componentElement);

        return resultComponent;
    }

    protected void createData(JmixPivotTable<StudioPivotTableShape> resultComponent) {
        resultComponent.setItems(new StudioListPivotTableItems(List.of(
                new StudioPivotTableShape(1L, "Circle", "Blue", "Middle"),
                new StudioPivotTableShape(2L, "Circle", "Green", "Small"),
                new StudioPivotTableShape(3L, "Ellipse", "Yellow", "Small"),
                new StudioPivotTableShape(4L, "Ellipse", "Green", "Big"),
                new StudioPivotTableShape(5L, "Square", "Blue", "Middle"),
                new StudioPivotTableShape(6L, "Square", "Green", "Big"),
                new StudioPivotTableShape(7L, "Rhombus", "Blue", "Big"),
                new StudioPivotTableShape(8L, "Rhombus", "Yellow", "Small"),
                new StudioPivotTableShape(8L, "Circle", "Yellow", "Small"),
                new StudioPivotTableShape(8L, "Square", "Green", "Small"))));
    }

    protected List<String> loadListOfStrings(Element itemsElement, String elementName, String attributeName) {
        List<String> items = new ArrayList<>();
        for (Element itemElement : itemsElement.elements(elementName)) {
            loadString(itemElement, attributeName, items::add);
        }
        return items;
    }

    protected void loadOptions(JmixPivotTable<?> resultComponent, Element element) {
        loadProperties(resultComponent, element);
        loadRows(resultComponent, element);
        loadColumns(resultComponent, element);
        loadAggregationProperties(resultComponent, element);
        loadHiddenProperties(resultComponent, element);
        loadHiddenFromAggregations(resultComponent, element);
        loadHiddenFromDragDrop(resultComponent, element);
        loadRenderers(resultComponent, element);
        loadAggregation(resultComponent, element);
        loadAggregations(resultComponent, element);
        loadRendererOptions(resultComponent, element);
        loadInclusions(resultComponent, element);
        loadExclusions(resultComponent, element);
        loadDerivedProperties(resultComponent, element);

        loadString(element, "emptyDataMessage", resultComponent::setEmptyDataMessage);
        loadEnum(element, Renderer.class, "renderer", resultComponent::setRenderer);
        loadEnum(element, Order.class, "rowOrder", resultComponent::setRowOrder);
        loadEnum(element, Order.class, "columnOrder", resultComponent::setColumnOrder);
        loadInteger(element, "menuLimit", resultComponent::setMenuLimit);
        loadString(element, "unusedPropertiesVertical",
                value -> resultComponent.setUnusedPropertiesVertical(UnusedPropertiesVertical.valueOf(value)));

        String filterFunction = element.elementText("filterFunction");
        if (!Strings.isNullOrEmpty(filterFunction)) {
            resultComponent.setFilterFunction(new JsFunction(filterFunction));
        }

        String sortersFunction = element.elementText("sortersFunction");
        if (!Strings.isNullOrEmpty(sortersFunction)) {
            resultComponent.setSortersFunction(new JsFunction(sortersFunction));
        }

        loadBoolean(element, "autoSortUnusedProperties", resultComponent::setAutoSortUnusedProperties);
        loadBoolean(element, "showUI", resultComponent::setShowUI);
        loadBoolean(element, "showRowTotals", resultComponent::setShowRowTotals);
        loadBoolean(element, "showColumnTotals", resultComponent::setShowColumnTotals);
    }

    protected void loadProperties(JmixPivotTable<?> resultComponent, Element element) {
        Element propertiesElement = element.element("properties");
        if (propertiesElement != null) {
            for (Element propertyElement : propertiesElement.elements("property")) {
                loadString(propertyElement, "name").ifPresent(name -> {
                    if (!Strings.isNullOrEmpty(name)) {
                            resultComponent.addProperty(name, loadString(propertyElement, "localizedName")
                                    .orElse(name));
                        }
                    }
                );
            }
        }
    }

    protected void loadRows(JmixPivotTable<?> resultComponent, Element element) {
        Element rowsElement = element.element("rows");
        if (rowsElement != null) {
            List<String> rows = loadListOfStrings(rowsElement, "row", "value");
            if (!rows.isEmpty()) {
                resultComponent.setRows(rows);
            }
        }
    }

    protected void loadColumns(JmixPivotTable<?> resultComponent,Element element) {
        Element columnsElement = element.element("columns");
        if (columnsElement != null) {
            List<String> columns = loadListOfStrings(columnsElement, "column", "value");
            if (!columns.isEmpty()) {
                resultComponent.setColumns(columns);
            }
        }
    }


    protected void loadAggregationProperties(JmixPivotTable<?> resultComponent, Element element) {
        Element aggregationPropertiesElement = element.element("aggregationProperties");
        if (aggregationPropertiesElement != null) {
            List<String> properties = loadListOfStrings(aggregationPropertiesElement, "property", "name");
            if (!properties.isEmpty()) {
                resultComponent.setAggregationProperties(properties);
            }
        }
    }

    protected void loadHiddenProperties(JmixPivotTable<?> resultComponent, Element element) {
        Element hiddenPropertiesElement = element.element("hiddenProperties");
        if (hiddenPropertiesElement != null) {
            List<String> properties = loadListOfStrings(hiddenPropertiesElement, "property", "name");
            if (!properties.isEmpty()) {
                resultComponent.setHiddenProperties(properties);
            }
        }
    }

    protected void loadHiddenFromAggregations(JmixPivotTable<?> resultComponent, Element element) {
        Element hiddenFromAggregationsElement = element.element("hiddenFromAggregations");
        if (hiddenFromAggregationsElement != null) {
            List<String> properties = loadListOfStrings(hiddenFromAggregationsElement, "property", "name");
            if (!properties.isEmpty()) {
                resultComponent.setHiddenFromAggregations(properties);
            }
        }
    }

    protected void loadHiddenFromDragDrop(JmixPivotTable<?> resultComponent, Element element) {
        Element hiddenFromDragDropElement = element.element("hiddenFromDragDrop");
        if (hiddenFromDragDropElement != null) {
            List<String> properties = loadListOfStrings(hiddenFromDragDropElement, "property", "name");
            if (!properties.isEmpty()) {
                resultComponent.setHiddenFromDragDrop(properties);
            }
        }
    }


    protected void loadRenderers(JmixPivotTable<?> resultComponent, Element element) {
        Element renderersElement = element.element("renderers");
        if (renderersElement != null) {
            Renderers renderers = new Renderers();

            for (Element rendererElement : renderersElement.elements("renderer")) {
                loadEnum(rendererElement, Renderer.class, "type", renderers::addRenderers);
            }

            Optional<String> selectedRendererOptional = loadString(renderersElement, "selected");
            Optional<String> defaultRendererOptional = loadString(renderersElement, "default");

            renderers.setSelectedRenderer(selectedRendererOptional.map(Renderer::valueOf)
                    .orElseGet(() -> defaultRendererOptional.map(Renderer::valueOf).orElse(null)));

            resultComponent.setRenderers(renderers);
        }
    }

    protected Aggregation loadAggregationAttributes(Element aggregationElement) {
        Aggregation aggregation = new Aggregation();

        loadString(aggregationElement, "caption", aggregation::setCaption);
        loadBoolean(aggregationElement, "custom", aggregation::setCustom);
        loadEnum(aggregationElement, AggregationMode.class, "mode", aggregation::setMode);

        Element function = aggregationElement.element("function");
        if (function != null) {
            aggregation.setFunction(new JsFunction(function.getText()));
        }

        List<String> properties = loadListOfStrings(aggregationElement, "property", "name");
        if (!properties.isEmpty()) {
            aggregation.setProperties(properties);
        }

        return aggregation;
    }

    protected void loadAggregation(JmixPivotTable<?> resultComponent, Element element) {
        Element aggregationElement = element.element("aggregation");
        if (aggregationElement != null) {
            Aggregation aggregation = loadAggregationAttributes(aggregationElement);
            resultComponent.setAggregation(aggregation);
        }
    }

    protected void loadAggregations(JmixPivotTable<?> resultComponent, Element element) {
        Element aggregationsElement = element.element("aggregations");
        if (aggregationsElement != null) {
            Aggregations aggregations = new Aggregations();

            for (Element aggregationElement : aggregationsElement.elements("aggregation")) {
                Aggregation aggregation = loadAggregationAttributes(aggregationElement);
                aggregations.addAggregations(aggregation);
            }

            Optional<String> selectedAggregationOptional = loadString(aggregationsElement, "selected");
            Optional<String> defaultAggregationOptional = loadString(aggregationsElement, "default");

            aggregations.setSelectedAggregation(selectedAggregationOptional.map(AggregationMode::valueOf)
                    .orElseGet(() -> defaultAggregationOptional.map(AggregationMode::valueOf).orElse(null)));

            resultComponent.setAggregations(aggregations);
        }
    }


    protected void loadRendererOptions(JmixPivotTable<?> resultComponent, Element element) {
        Element rendererOptionsElement = element.element("rendererOptions");
        if (rendererOptionsElement != null) {
            RendererOptions rendererOptions = new RendererOptions();

            loadHeatmapRendererOptions(rendererOptions, rendererOptionsElement);
            loadC3RendererOptions(rendererOptions, rendererOptionsElement);

            resultComponent.setRendererOptions(rendererOptions);
        }
    }

    protected void loadHeatmapRendererOptions(RendererOptions rendererOptions, Element rendererOptionsElement) {
        Element heatmapElement = rendererOptionsElement.element("heatmap");
        if (heatmapElement != null) {
            HeatmapRendererOptions heatmap = new HeatmapRendererOptions();
            String colorScaleGeneratorFunction = heatmapElement.elementText("colorScaleGeneratorFunction");
            if (!Strings.isNullOrEmpty(colorScaleGeneratorFunction)) {
                heatmap.setColorScaleGeneratorFunction(new JsFunction(colorScaleGeneratorFunction));
            }
            rendererOptions.setHeatmap(heatmap);
        }
    }

    protected void loadC3RendererOptions(RendererOptions rendererOptions, Element rendererOptionsElement) {
        Element c3Element = rendererOptionsElement.element("c3");
        if (c3Element != null) {
            C3RendererOptions c3 = new C3RendererOptions();

            Element sizeElement = c3Element.element("size");
            if (sizeElement != null) {
                Size size = new Size();

                loadDouble(sizeElement, "width", size::setWidth);
                loadDouble(sizeElement, "height", size::setHeight);

                c3.setSize(size);
            }

            rendererOptions.setC3(c3);
        }
    }

    protected Map<String, List<String>> loadMapOfListsOfStrings(Element element) {
        Map<String, List<String>> map = new HashMap<>();
        for (Element propertyElement : element.elements("property")) {
            Optional<String> nameOptional = loadString(propertyElement, "name");
            if (nameOptional.isPresent()) {
                List<String> values = loadListOfStrings(propertyElement, "value", "value");
                if (!values.isEmpty()) {
                    map.put(nameOptional.get(), values);
                }
            }
        }
        return map;
    }

    protected void loadInclusions(JmixPivotTable<?> resultComponent, Element element) {
        Element inclusionsElement = element.element("inclusions");
        if (inclusionsElement != null) {
            Map<String, List<String>> inclusions = loadMapOfListsOfStrings(inclusionsElement);
            if (!inclusions.isEmpty()) {
                resultComponent.setInclusions(inclusions);
            }
        }
    }

    protected void loadExclusions(JmixPivotTable<?> resultComponent, Element element) {
        Element exclusionsElement = element.element("exclusions");
        if (exclusionsElement != null) {
            Map<String, List<String>> exclusions = loadMapOfListsOfStrings(exclusionsElement);
            if (!exclusions.isEmpty()) {
                resultComponent.setExclusions(exclusions);
            }
        }
    }

    protected void loadDerivedProperties(JmixPivotTable<?> resultComponent, Element element) {
        Element derivedPropertiesElement = element.element("derivedProperties");
        if (derivedPropertiesElement != null) {
            DerivedProperties derivedProperties = new DerivedProperties();
            for (Element derivedAttributeElement : derivedPropertiesElement.elements("derivedProperty")) {
                Optional<String> captionOptional = loadString(derivedAttributeElement, "caption");
                if (captionOptional.isPresent()) {
                    String code = derivedAttributeElement.elementText("function");
                    if (!Strings.isNullOrEmpty(code)) {
                        derivedProperties.addAttribute(captionOptional.get(), new JsFunction(code));
                    }
                }
            }
            if (derivedProperties.getProperties() != null && !derivedProperties.getProperties().isEmpty()) {
                resultComponent.setDerivedProperties(derivedProperties);
            }
        }
    }
}
