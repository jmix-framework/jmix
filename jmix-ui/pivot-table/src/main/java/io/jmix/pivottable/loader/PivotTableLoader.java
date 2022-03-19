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

package io.jmix.pivottable.loader;


import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.data.impl.ContainerDataProvider;
import io.jmix.ui.data.impl.HasMetaClass;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.pivottable.component.PivotTable;
import io.jmix.pivottable.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PivotTableLoader extends AbstractComponentLoader<PivotTable> {


    @Override
    public void createComponent() {
        resultComponent = factory.create(PivotTable.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);

        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);
        loadStyleName(resultComponent, element);
        loadCss(resultComponent, element);

        loadDataContainer(resultComponent, element);

        loadConfiguration(resultComponent, element);
    }

    protected void loadDataContainer(PivotTable pivotTable, Element element) {
        String dataContainerId = element.attributeValue("dataContainer");
        if (StringUtils.isNotEmpty(dataContainerId)) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);

            CollectionContainer dataContainer;

            InstanceContainer container = screenData.getContainer(dataContainerId);
            if (container instanceof CollectionContainer) {
                dataContainer = (CollectionContainer) container;
            } else {
                throw new GuiDevelopmentException("Not a CollectionContainer: " + dataContainerId, context);
            }

            pivotTable.setDataProvider(new ContainerDataProvider(dataContainer));
        }
    }

    protected List<String> loadListOfStrings(Element itemsElement,
                                             String elementName, String attributeName, boolean resource) {
        List<String> items = new ArrayList<>();

        for (Object item : itemsElement.elements(elementName)) {
            Element element = (Element) item;

            String value = element.attributeValue(attributeName);
            if (StringUtils.isNotEmpty(value)) {
                items.add(resource ? loadResourceString(value) : value);
            }
        }

        return items;
    }

    protected void loadConfiguration(PivotTable pivot, Element element) {
        loadProperties(pivot, element);
        loadRows(pivot, element);
        loadColumns(pivot, element);
        loadAggregationProperties(pivot, element);
        loadHiddenProperties(pivot, element);
        loadHiddenFromAggregations(pivot, element);
        loadHiddenFromDragDrop(pivot, element);
        loadRenderers(pivot, element);
        loadAggregation(pivot, element);
        loadAggregations(pivot, element);
        loadRendererOptions(pivot, element);
        loadInclusions(pivot, element);
        loadExclusions(pivot, element);
        loadDerivedProperties(pivot, element);

        String editable = element.attributeValue("editable");
        if (StringUtils.isNotEmpty(editable)) {
            pivot.setEditable(Boolean.valueOf(editable));
        }

        String emptyDataMessage = element.attributeValue("emptyDataMessage");
        if (StringUtils.isNotEmpty(emptyDataMessage)) {
            pivot.setEmptyDataMessage(loadResourceString(emptyDataMessage));
        }

        String renderer = element.attributeValue("renderer");
        if (StringUtils.isNotEmpty(renderer)) {
            pivot.setRenderer(Renderer.valueOf(renderer));
        }

        String columnOrder = element.attributeValue("columnOrder");
        if (StringUtils.isNotEmpty(columnOrder)) {
            pivot.setColumnOrder(ColumnOrder.valueOf(columnOrder));
        }

        String rowOrder = element.attributeValue("rowOrder");
        if (StringUtils.isNotEmpty(rowOrder)) {
            pivot.setRowOrder(RowOrder.valueOf(rowOrder));
        }

        String menuLimit = element.attributeValue("menuLimit");
        if (StringUtils.isNotEmpty(menuLimit)) {
            pivot.setMenuLimit(Integer.valueOf(menuLimit));
        }

        String autoSortUnusedProperties = element.attributeValue("autoSortUnusedProperties");
        if (StringUtils.isNotEmpty(autoSortUnusedProperties)) {
            pivot.setAutoSortUnusedProperties(Boolean.valueOf(autoSortUnusedProperties));
        }

        String unusedPropertiesVertical = element.attributeValue("unusedPropertiesVertical");
        if (StringUtils.isNotEmpty(unusedPropertiesVertical)) {
            pivot.setUnusedPropertiesVertical(UnusedPropertiesVertical.valueOf(unusedPropertiesVertical));
        }

        String filter = element.elementText("filterFunction");
        if (StringUtils.isNotBlank(filter)) {
            pivot.setFilterFunction(new JsFunction(filter));
        }

        String sorters = element.elementText("sortersFunction");
        if (StringUtils.isNotBlank(sorters)) {
            pivot.setSortersFunction(new JsFunction(sorters));
        }

        String showUI = element.attributeValue("showUI");
        if (StringUtils.isNotBlank(showUI)) {
            pivot.setShowUI(Boolean.valueOf(showUI));
        }

        String showRowTotals = element.attributeValue("showRowTotals");
        if (StringUtils.isNotBlank(showRowTotals)) {
            pivot.setShowRowTotals(Boolean.valueOf(showRowTotals));
        }

        String showColTotals = element.attributeValue("showColTotals");
        if (StringUtils.isNotBlank(showColTotals)) {
            pivot.setShowColTotals(Boolean.valueOf(showColTotals));
        }
    }

    protected void loadProperties(PivotTable pivot, Element element) {
        Element propertiesElement = element.element("properties");
        if (propertiesElement != null) {
            for (Object item : propertiesElement.elements("property")) {
                Element propertyElement = (Element) item;

                String name = propertyElement.attributeValue("name");
                if (StringUtils.isNotEmpty(name)) {

                    MetaClass metaClass = pivot.getDataProvider() instanceof HasMetaClass ?
                            ((HasMetaClass) pivot.getDataProvider()).getMetaClass() : null;

                    checkValidProperty(metaClass, name);

                    String localizedName = propertyElement.attributeValue("localizedName");
                    if (StringUtils.isNotEmpty(localizedName)) {
                        localizedName = loadResourceString(localizedName);
                    } else if (metaClass != null) {
                        localizedName = getMessageTools().getPropertyCaption(metaClass, name);
                    } else {
                        localizedName = name;
                    }

                    pivot.addProperty(name, localizedName);
                }
            }
        }
    }

    protected void checkValidProperty(@Nullable MetaClass metaClass, String name) {
        if (metaClass != null) {
            MetaProperty property = metaClass.findProperty(name);
            if (property != null
                    && property.getRange().getCardinality() != null
                    && property.getRange().getCardinality().isMany()) {
                throw new GuiDevelopmentException(String.format("'%s' cannot be added as a property, because " +
                        "PivotTable doesn't support collections as properties", name), context);
            }
        }
    }

    protected void loadRows(PivotTable pivot, Element element) {
        Element rowsElement = element.element("rows");
        if (rowsElement != null) {
            List<String> rows = loadListOfStrings(rowsElement, "row", "value", true);
            if (CollectionUtils.isNotEmpty(rows)) {
                pivot.setRows(rows);
            }
        }
    }

    protected void loadColumns(PivotTable pivot, Element element) {
        Element columnsElement = element.element("columns");
        if (columnsElement != null) {
            List<String> columns = loadListOfStrings(columnsElement, "column", "value", true);
            if (CollectionUtils.isNotEmpty(columns)) {
                pivot.setColumns(columns);
            }
        }
    }

    protected void loadAggregationProperties(PivotTable pivot, Element element) {
        Element aggregationPropertiesElement = element.element("aggregationProperties");
        if (aggregationPropertiesElement != null) {
            List<String> properties = loadListOfStrings(aggregationPropertiesElement, "property", "name", true);
            if (CollectionUtils.isNotEmpty(properties)) {
                pivot.setAggregationProperties(properties);
            }
        }
    }

    protected void loadHiddenProperties(PivotTable pivot, Element element) {
        Element hiddenPropertiesElement = element.element("hiddenProperties");
        if (hiddenPropertiesElement != null) {
            List<String> properties = loadListOfStrings(hiddenPropertiesElement, "property", "name", true);
            if (CollectionUtils.isNotEmpty(properties)) {
                pivot.setHiddenProperties(properties);
            }
        }
    }

    protected void loadHiddenFromAggregations(PivotTable pivot, Element element) {
        Element hiddenFromAggregationsElement = element.element("hiddenFromAggregations");
        if (hiddenFromAggregationsElement != null) {
            List<String> properties = loadListOfStrings(hiddenFromAggregationsElement, "property", "name", true);
            if (CollectionUtils.isNotEmpty(properties)) {
                pivot.setHiddenFromAggregations(properties);
            }
        }
    }

    protected void loadHiddenFromDragDrop(PivotTable pivot, Element element) {
        Element hiddenFromDragDropElement = element.element("hiddenFromDragDrop");
        if (hiddenFromDragDropElement != null) {
            List<String> properties = loadListOfStrings(hiddenFromDragDropElement, "property", "name", true);
            if (CollectionUtils.isNotEmpty(properties)) {
                pivot.setHiddenFromDragDrop(properties);
            }
        }
    }

    protected void loadRenderers(PivotTable pivot, Element element) {
        Element renderersElement = element.element("renderers");
        if (renderersElement != null) {
            Renderers renderers = new Renderers();

            for (Object rendererItem : renderersElement.elements("renderer")) {
                Element rendererElement = (Element) rendererItem;
                String type = rendererElement.attributeValue("type");
                if (StringUtils.isNotEmpty(type)) {
                    renderers.addRenderers(Renderer.valueOf(type));
                }
            }

            String selectedRenderer = renderersElement.attributeValue("selected");
            if (StringUtils.isEmpty(selectedRenderer)) {
                selectedRenderer = renderersElement.attributeValue("default");
            }
            if (StringUtils.isNotEmpty(selectedRenderer)) {
                renderers.setSelectedRenderer(Renderer.valueOf(selectedRenderer));
            }

            pivot.setRenderers(renderers);
        }
    }

    protected Aggregation loadAggregation(Element aggregationElement) {
        Aggregation aggregation = new Aggregation();

        String caption = aggregationElement.attributeValue("caption");
        if (StringUtils.isNotEmpty(caption)) {
            aggregation.setCaption(loadResourceString(caption));
        }

        String custom = aggregationElement.attributeValue("custom");
        if (StringUtils.isNotEmpty(custom)) {
            aggregation.setCustom(Boolean.valueOf(custom));
        }

        String function = aggregationElement.elementText("function");
        if (StringUtils.isNotEmpty(function)) {
            aggregation.setFunction(new JsFunction(function));
        }

        String mode = aggregationElement.attributeValue("mode");
        if (StringUtils.isNotEmpty(mode)) {
            aggregation.setMode(AggregationMode.valueOf(mode));
        }

        List<String> properties = loadListOfStrings(aggregationElement, "property", "name", true);
        if (CollectionUtils.isNotEmpty(properties)) {
            aggregation.setProperties(properties);
        }

        return aggregation;
    }

    protected void loadAggregation(PivotTable pivot, Element element) {
        Element aggregationElement = element.element("aggregation");
        if (aggregationElement != null) {
            Aggregation aggregation = loadAggregation(aggregationElement);
            pivot.setAggregation(aggregation);
        }
    }

    protected void loadAggregations(PivotTable pivot, Element element) {
        Element aggregationsElement = element.element("aggregations");
        if (aggregationsElement != null) {
            Aggregations aggregations = new Aggregations();

            for (Object aggregationItem : aggregationsElement.elements("aggregation")) {
                Element aggregationElement = (Element) aggregationItem;
                Aggregation aggregation = loadAggregation(aggregationElement);
                aggregations.addAggregations(aggregation);
            }

            String selectedAggregation = aggregationsElement.attributeValue("selected");
            if (StringUtils.isEmpty(selectedAggregation)) {
                selectedAggregation = aggregationsElement.attributeValue("default");
            }
            if (StringUtils.isNotEmpty(selectedAggregation)) {
                aggregations.setSelectedAggregation(AggregationMode.valueOf(selectedAggregation));
            }

            pivot.setAggregations(aggregations);
        }
    }

    protected void loadRendererOptions(PivotTable pivot, Element element) {
        Element rendererOptionsElement = element.element("rendererOptions");
        if (rendererOptionsElement != null) {
            RendererOptions rendererOptions = new RendererOptions();

            loadHeatmapRendererOptions(rendererOptions, rendererOptionsElement);
            loadC3RendererOptions(rendererOptions, rendererOptionsElement);

            pivot.setRendererOptions(rendererOptions);
        }
    }

    protected void loadHeatmapRendererOptions(RendererOptions rendererOptions, Element rendererOptionsElement) {
        Element heatmapElement = rendererOptionsElement.element("heatmap");
        if (heatmapElement != null) {
            HeatmapRendererOptions heatmap = new HeatmapRendererOptions();
            String colorScaleGeneratorFunction = heatmapElement.elementText("colorScaleGeneratorFunction");
            if (StringUtils.isNotBlank(colorScaleGeneratorFunction)) {
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

                String width = sizeElement.attributeValue("width");
                if (StringUtils.isNotEmpty(width)) {
                    size.setWidth(Double.valueOf(width));
                }

                String height = sizeElement.attributeValue("height");
                if (StringUtils.isNotEmpty(height)) {
                    size.setHeight(Double.valueOf(height));
                }

                c3.setSize(size);
            }

            rendererOptions.setC3(c3);
        }
    }

    protected Map<String, List<String>> loadMapOfListsOfStrings(Element element) {
        Map<String, List<String>> map = new HashMap<>();
        for (Object propertyObject : element.elements("property")) {
            Element propertyElement = (Element) propertyObject;
            String propertyName = propertyElement.attributeValue("name");
            if (StringUtils.isNotEmpty(propertyName)) {
                List<String> values = loadListOfStrings(propertyElement, "value", "value", false);
                if (CollectionUtils.isNotEmpty(values)) {
                    map.put(loadResourceString(propertyName), values);
                }
            }
        }
        return map;
    }

    protected void loadInclusions(PivotTable pivot, Element element) {
        Element inclusionsElement = element.element("inclusions");
        if (inclusionsElement != null) {
            Map<String, List<String>> inclusions = loadMapOfListsOfStrings(inclusionsElement);
            if (MapUtils.isNotEmpty(inclusions)) {
                pivot.setInclusions(inclusions);
            }
        }
    }

    protected void loadExclusions(PivotTable pivot, Element element) {
        Element exclusionsElement = element.element("exclusions");
        if (exclusionsElement != null) {
            Map<String, List<String>> exclusions = loadMapOfListsOfStrings(exclusionsElement);
            if (MapUtils.isNotEmpty(exclusions)) {
                pivot.setExclusions(exclusions);
            }
        }
    }

    protected void loadDerivedProperties(PivotTable pivot, Element element) {
        Element derivedPropertiesElement = element.element("derivedProperties");
        if (derivedPropertiesElement != null) {
            DerivedProperties derivedProperties = new DerivedProperties();
            for (Object derivedAttrObj : derivedPropertiesElement.elements("derivedProperty")) {
                Element derivedAttributeElement = (Element) derivedAttrObj;

                String caption = derivedAttributeElement.attributeValue("caption");
                if (StringUtils.isNotEmpty(caption)) {
                    String code = derivedAttributeElement.elementText("function");
                    if (StringUtils.isNotEmpty(code)) {
                        derivedProperties.addAttribute(loadResourceString(caption), new JsFunction(code));
                    }
                }
            }
            if (MapUtils.isNotEmpty(derivedProperties.getProperties())) {
                pivot.setDerivedProperties(derivedProperties);
            }
        }
    }
}
