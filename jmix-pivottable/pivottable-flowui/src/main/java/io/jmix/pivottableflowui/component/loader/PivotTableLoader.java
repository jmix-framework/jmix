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

package io.jmix.pivottableflowui.component.loader;

import com.google.common.base.Strings;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.accesscontext.UiEntityAttributeContext;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.data.ContainerPivotTableItems;
import io.jmix.pivottableflowui.kit.component.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.*;

public class PivotTableLoader extends AbstractComponentLoader<PivotTable<?>> {

    protected AccessManager accessManager;
    protected MessageTools messageTools;
    protected MetadataTools metadataTools;

    @Override
    public void loadComponent() {
        loadDataContainer(element);

        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadOptions(element);
    }

    protected AccessManager getAccessManager() {
        if (accessManager == null) {
            accessManager = applicationContext.getBean(AccessManager.class, context);
        }
        return accessManager;
    }

    protected MessageTools getMessageTools() {
        if (messageTools == null) {
            messageTools = applicationContext.getBean(MessageTools.class, context);
        }
        return messageTools;
    }

    protected MetadataTools getMetadataTools() {
        if (metadataTools == null) {
            metadataTools = applicationContext.getBean(MetadataTools.class, context);
        }
        return metadataTools;
    }

    @Override
    protected PivotTable<?> createComponent() {
        return factory.create(PivotTable.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void loadDataContainer(Element element) {
        loadString(element, "dataContainer")
                .ifPresent(dataContainerId -> {
                    InstanceContainer<?> container = context.getDataHolder().getContainer(dataContainerId);

                    if (container instanceof CollectionContainer<?> collectionContainer) {
                        resultComponent.setItems(new ContainerPivotTableItems(collectionContainer, UUID.class));
                    } else {
                        throw new GuiDevelopmentException("Not a CollectionContainer: " + container, context);
                    }
                });
    }

    protected List<String> loadListOfStrings(Element itemsElement, String elementName, String attributeName,
                                             boolean resource) {
        List<String> items = new ArrayList<>();
        for (Element itemElement : itemsElement.elements(elementName)) {
            if (resource) {
                loadResourceString(itemElement, attributeName, context.getMessageGroup(), items::add);
            } else {
                loadString(itemElement, attributeName, items::add);
            }
        }
        return items;
    }

    protected void loadOptions(Element element) {
        loadProperties(element);
        loadRows(element);
        loadColumns(element);
        loadAggregationProperties(element);
        loadHiddenProperties(element);
        loadHiddenFromAggregations(element);
        loadHiddenFromDragDrop(element);
        loadRenderers(element);
        loadAggregation(element);
        loadAggregations(element);
        loadRendererOptions(element);
        loadInclusions(element);
        loadExclusions(element);
        loadDerivedProperties(element);

        loadResourceString(element, "emptyDataMessage", context.getMessageGroup(), resultComponent::setEmptyDataMessage);
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

    protected void loadProperties(Element element) {
        Element propertiesElement = element.element("properties");
        if (propertiesElement != null) {
            for (Element propertyElement : propertiesElement.elements("property")) {
                loadString(propertyElement, "name").ifPresent(name -> {
                    if (!Strings.isNullOrEmpty(name)) {
                        MetaClass metaClass = resultComponent.getItems() instanceof EntityDataUnit ?
                                ((EntityDataUnit) resultComponent.getItems()).getEntityMetaClass() : null;
                        checkValidProperty(metaClass, name);
                        if (metaClass == null || hasPropertyPermission(metaClass, name)) {
                            resultComponent.addProperty(name, loadResourceString(propertyElement, "localizedName",
                                    context.getMessageGroup()).orElseGet(
                                            () -> metaClass != null
                                                    ? getMessageTools().getPropertyCaption(metaClass, name)
                                                    : name
                            ));
                        }
                    }
                });
            }
        }
    }

    protected boolean hasPropertyPermission(MetaClass metaClass, String property) {
        MetaPropertyPath metaPropertyPath = getMetadataTools().resolveMetaPropertyPath(metaClass, property);
        UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(metaPropertyPath);
        getAccessManager().applyRegisteredConstraints(attributeContext);
        return attributeContext.canView();
    }

    protected void checkValidProperty(@Nullable MetaClass metaClass, String name) {
        if (metaClass != null) {
            MetaProperty property = metaClass.findProperty(name);
            if (property != null && property.getRange().getCardinality().isMany()) {
                throw new IllegalStateException(String.format("'%s' cannot be added as a property, because " +
                        "%s doesn't support collection as property", name, PivotTable.class.getSimpleName()));
            }
        }
    }

    protected void loadRows(Element element) {
        Element rowsElement = element.element("rows");
        if (rowsElement != null) {
            List<String> rows = loadListOfStrings(rowsElement, "row", "value", true);
            if (CollectionUtils.isNotEmpty(rows)) {
                resultComponent.setRows(rows);
            }
        }
    }

    protected void loadColumns(Element element) {
        Element columnsElement = element.element("columns");
        if (columnsElement != null) {
            List<String> columns = loadListOfStrings(columnsElement, "column", "value", true);
            if (CollectionUtils.isNotEmpty(columns)) {
                resultComponent.setColumns(columns);
            }
        }
    }


    protected void loadAggregationProperties(Element element) {
        Element aggregationPropertiesElement = element.element("aggregationProperties");
        if (aggregationPropertiesElement != null) {
            List<String> properties = loadListOfStrings(aggregationPropertiesElement, "property", "name", true);
            if (CollectionUtils.isNotEmpty(properties)) {
                resultComponent.setAggregationProperties(properties);
            }
        }
    }

    protected void loadHiddenProperties(Element element) {
        Element hiddenPropertiesElement = element.element("hiddenProperties");
        if (hiddenPropertiesElement != null) {
            List<String> properties = loadListOfStrings(hiddenPropertiesElement, "property", "name", true);
            if (CollectionUtils.isNotEmpty(properties)) {
                resultComponent.setHiddenProperties(properties);
            }
        }
    }

    protected void loadHiddenFromAggregations(Element element) {
        Element hiddenFromAggregationsElement = element.element("hiddenFromAggregations");
        if (hiddenFromAggregationsElement != null) {
            List<String> properties = loadListOfStrings(hiddenFromAggregationsElement, "property", "name", true);
            if (CollectionUtils.isNotEmpty(properties)) {
                resultComponent.setHiddenFromAggregations(properties);
            }
        }
    }

    protected void loadHiddenFromDragDrop(Element element) {
        Element hiddenFromDragDropElement = element.element("hiddenFromDragDrop");
        if (hiddenFromDragDropElement != null) {
            List<String> properties = loadListOfStrings(hiddenFromDragDropElement, "property", "name", true);
            if (CollectionUtils.isNotEmpty(properties)) {
                resultComponent.setHiddenFromDragDrop(properties);
            }
        }
    }


    protected void loadRenderers(Element element) {
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

        loadResourceString(aggregationElement, "caption", context.getMessageGroup(), aggregation::setCaption);
        loadBoolean(aggregationElement, "custom", aggregation::setCustom);
        loadEnum(aggregationElement, AggregationMode.class, "mode", aggregation::setMode);

        Element function = aggregationElement.element("function");
        if (function != null) {
            aggregation.setFunction(new JsFunction(function.getText()));
        }

        List<String> properties = loadListOfStrings(aggregationElement, "property", "name", true);
        if (CollectionUtils.isNotEmpty(properties)) {
            aggregation.setProperties(properties);
        }

        return aggregation;
    }

    protected void loadAggregation(Element element) {
        Element aggregationElement = element.element("aggregation");
        if (aggregationElement != null) {
            Aggregation aggregation = loadAggregationAttributes(aggregationElement);
            resultComponent.setAggregation(aggregation);
        }
    }

    protected void loadAggregations(Element element) {
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


    protected void loadRendererOptions(Element element) {
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
            Optional<String> nameOptional = loadResourceString(propertyElement, "name", context.getMessageGroup());
            if (nameOptional.isPresent()) {
                List<String> values = loadListOfStrings(propertyElement, "value", "value", false);
                if (CollectionUtils.isNotEmpty(values)) {
                    map.put(nameOptional.get(), values);
                }
            }
        }
        return map;
    }

    protected void loadInclusions(Element element) {
        Element inclusionsElement = element.element("inclusions");
        if (inclusionsElement != null) {
            Map<String, List<String>> inclusions = loadMapOfListsOfStrings(inclusionsElement);
            if (MapUtils.isNotEmpty(inclusions)) {
                resultComponent.setInclusions(inclusions);
            }
        }
    }

    protected void loadExclusions(Element element) {
        Element exclusionsElement = element.element("exclusions");
        if (exclusionsElement != null) {
            Map<String, List<String>> exclusions = loadMapOfListsOfStrings(exclusionsElement);
            if (MapUtils.isNotEmpty(exclusions)) {
                resultComponent.setExclusions(exclusions);
            }
        }
    }

    protected void loadDerivedProperties(Element element) {
        Element derivedPropertiesElement = element.element("derivedProperties");
        if (derivedPropertiesElement != null) {
            DerivedProperties derivedProperties = new DerivedProperties();
            for (Element derivedAttributeElement : derivedPropertiesElement.elements("derivedProperty")) {
                Optional<String> captionOptional = loadResourceString(
                        derivedAttributeElement, "caption", context.getMessageGroup());
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