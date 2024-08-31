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

package io.jmix.pivottableflowui.kit.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.pivottableflowui.kit.component.model.*;
import io.jmix.pivottableflowui.kit.component.serialization.JmixPivotTableSerializer;
import io.jmix.pivottableflowui.kit.data.DataItem;
import io.jmix.pivottableflowui.kit.data.PivotTableListDataSet;
import io.jmix.pivottableflowui.kit.event.PivotTableCellClickEvent;
import io.jmix.pivottableflowui.kit.event.PivotTableRefreshEvent;

import java.util.*;
import java.util.stream.Collectors;

@Tag("jmix-pivot-table")
@JsModule("./src/pivot-table/jmix-pivot-table.js")
public class JmixPivotTable extends Component implements HasEnabled, HasSize {

    protected DataProvider<DataItem, ?> dataProvider;
    protected Registration dataProviderItemSetChangeRegistration;

    protected PivotTableOptions options;
    protected JmixPivotTableSerializer serializer;

    protected volatile boolean clientReady;
    protected final List<PendingJsFunction> functions = new ArrayList<>();

    protected StateTree.ExecutionRegistration synchronizeOptionsExecution;
    protected StateTree.ExecutionRegistration synchronizeItemsExecution;

    protected Map<String, Registration> eventRegistrations = new HashMap<>();

    public JmixPivotTable() {
        initComponent();
    }

    public DataProvider<DataItem, ?> getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(DataProvider<DataItem, ?> dataProvider) {
        if (Objects.equals(dataProvider, this.dataProvider)) {
            return;
        }

        this.dataProvider = dataProvider;
        onDataProviderChange();
    }

    public void setData(DataItem... dataItems) {
        if (dataItems != null) {
            setDataProvider(new PivotTableListDataSet<>(Arrays.asList(dataItems)));
        }
    }

    public void setItems(List<DataItem> items) {
        setDataProvider(new PivotTableListDataSet<>(items));
    }

    /**
     * Adds a listener to the pivot table refresh events. Fired only for editable PivotTable.
     *
     * @param listener a listener to add
     * @return subscription
     */
    public Registration addRefreshEventListener(ComponentEventListener<PivotTableRefreshEvent> listener) {
        Registration eventRegistration = getEventBus().addListener(PivotTableRefreshEvent.class, listener);
        eventRegistrations.put(PivotTableRefreshEvent.EVENT_NAME, eventRegistration);

        return getRemovalCallback(PivotTableRefreshEvent.EVENT_NAME, PivotTableRefreshEvent.class);
    }

    /**
     * Adds a listener to the pivot table cell click events. Fired only for table
     * renderers (table, heatmap, table barchart, col heatmap, row heatmap).
     *
     * @param listener a listener to add
     * @return subscription
     */
    public Registration addCellClickListener(ComponentEventListener<PivotTableCellClickEvent> listener) {
        Registration eventRegistration = getEventBus().addListener(PivotTableCellClickEvent.class, listener);
        eventRegistrations.put(PivotTableCellClickEvent.EVENT_NAME, eventRegistration);

        return getRemovalCallback(PivotTableCellClickEvent.EVENT_NAME, PivotTableCellClickEvent.class);
    }

    public Map<String, String> getProperties() {
        return options.getProperties();
    }

    public void setProperties(Map<String, String> properties) {
        options.setProperties(properties);
    }

    public void addProperties(Map<String, String> properties) {
        options.addProperties(properties);
    }

    public void addProperty(String property, String value) {
        options.addProperty(property, value);
    }

    public List<String> getRows() {
        return options.getRows();
    }

    public void setRows(List<String> rows) {
        options.setRows(rows);
    }

    public void addRows(String... rows) {
        options.addRows(rows);
    }

    public List<String> getCols() {
        return options.getCols();
    }

    public void setCols(List<String> cols) {
        options.setCols(cols);
    }

    public void addColumns(String... cols) {
        options.addCols(cols);
    }

    public Aggregation getAggregation() {
        return options.getAggregation();
    }

    public void setAggregation(Aggregation aggregation) {
        options.setAggregation(aggregation);
    }

    public Renderer getRenderer() {
        return options.getRenderer();
    }

    public void setRenderer(Renderer renderer) {
        options.setRenderer(renderer);
    }

    public List<String> getAggregationProperties() {
        return options.getAggregationProperties();
    }

    public void setAggregationProperties(List<String> aggregationProperties) {
        options.setAggregationProperties(aggregationProperties);
    }

    public void addAggregationProperties(String... aggregationProperties) {
        options.addAggregationProperties(aggregationProperties);
    }

    public Aggregations getAggregations() {
        return options.getAggregations();
    }

    public void setAggregations(Aggregations aggregations) {
        options.setAggregations(aggregations);
    }

    public Renderers getRenderers() {
        return options.getRenderers();
    }

    public void setRenderers(Renderers renderers) {
        options.setRenderers(renderers);
    }

    public List<String> getHiddenProperties() {
        return options.getHiddenProperties();
    }

    public void setHiddenProperties(List<String> hiddenProperties) {
        options.setHiddenProperties(hiddenProperties);
    }

    public void addHiddenProperties(String... hiddenProperties) {
        options.addHiddenProperties(hiddenProperties);
    }

    public List<String> getHiddenFromAggregations() {
        return options.getHiddenFromAggregations();
    }

    public void setHiddenFromAggregations(List<String> hiddenFromAggregations) {
        options.setHiddenFromAggregations(hiddenFromAggregations);
    }

    public void addHiddenFromAggregations(String... hiddenFromAggregations) {
        options.addHiddenFromAggregations(hiddenFromAggregations);
    }

    public List<String> getHiddenFromDragDrop() {
        return options.getHiddenFromDragDrop();
    }

    public void setHiddenFromDragDrop(List<String> hiddenFromDragDrop) {
        options.setHiddenFromDragDrop(hiddenFromDragDrop);
    }

    public void addHiddenFromDragDrop(String... hiddenFromDragDrop) {
        options.addHiddenFromDragDrop(hiddenFromDragDrop);
    }

    public Order getColOrder() {
        return options.getColOrder();
    }

    public void setColOrder(Order colOrder) {
        options.setColOrder(colOrder);
    }

    public Order getRowOrder() {
        return options.getRowOrder();
    }

    public void setRowOrder(Order rowOrder) {
        options.setRowOrder(rowOrder);
    }

    public Integer getMenuLimit() {
        return options.getMenuLimit();
    }

    public void setMenuLimit(Integer menuLimit) {
        options.setMenuLimit(menuLimit);
    }

    public Boolean getAutoSortUnusedProperties() {
        return options.getAutoSortUnusedProperties();
    }

    public void setAutoSortUnusedProperties(Boolean autoSortUnusedProperties) {
        options.setAutoSortUnusedProperties(autoSortUnusedProperties);
    }

    public UnusedPropertiesVertical getUnusedPropertiesVertical() {
        return options.getUnusedPropertiesVertical();
    }

    public void setUnusedPropertiesVertical(UnusedPropertiesVertical unusedPropertiesVertical) {
        options.setUnusedPropertiesVertical(unusedPropertiesVertical);
    }

    public JsFunction getFilterFunction() {
        return options.getFilterFunction();
    }

    public void setFilterFunction(JsFunction filter) {
        options.setFilterFunction(filter);
    }

    public JsFunction getSortersFunction() {
        return options.getSortersFunction();
    }

    public void setSortersFunction(JsFunction sorters) {
        options.setSortersFunction(sorters);
    }

    public RendererOptions getRendererOptions() {
        return options.getRendererOptions();
    }

    public void setRendererOptions(RendererOptions rendererOptions) {
        options.setRendererOptions(rendererOptions);
    }

    public Map<String, List<String>> getInclusions() {
        return options.getInclusions();
    }

    public void setInclusions(Map<String, List<String>> inclusions) {
        options.setInclusions(inclusions);
    }

    public void setInclusions(String property, List<String> inclusions) {
        options.setInclusions(property, inclusions);
    }

    public void addInclusions(String property, String... inclusions) {
        options.addInclusions(property, inclusions);
    }

    public Map<String, List<String>> getExclusions() {
        return options.getExclusions();
    }

    public void setExclusions(Map<String, List<String>> exclusions) {
        options.setExclusions(exclusions);
    }

    public void setExclusions(String property, List<String> exclusions) {
        options.setExclusions(property, exclusions);
    }

    public void addExclusions(String property, String... exclusions) {
        options.addExclusions(property, exclusions);
    }

    public DerivedProperties getDerivedProperties() {
        return options.getDerivedProperties();
    }

    public void setDerivedProperties(DerivedProperties derivedProperties) {
        options.setDerivedProperties(derivedProperties);
    }

    public void setEmptyDataMessage(String emptyDataMessage) {
        options.setEmptyDataMessage(emptyDataMessage);
    }

    public String getEmptyDataMessage() {
        return options.getEmptyDataMessage();
    }

    public void setShowUI(Boolean showUI) {
        options.setShowUI(showUI);
    }

    public Boolean isShowUI() {
        return options.isShowUI();
    }

    public void setShowRowTotals(Boolean showRowTotals) {
        options.setShowRowTotals(showRowTotals);
    }

    public Boolean isRowTotalsShown() {
        return options.isShowRowTotals();
    }

    public void setShowColTotals(Boolean showColTotals) {
        options.setShowColTotals(showColTotals);
    }

    public Boolean isColTotalsShown() {
        return options.isShowColTotals();
    }

    protected Registration getRemovalCallback(String eventName, Class<? extends ComponentEvent<?>> eventClass) {
        return () -> {
            eventRegistrations.get(eventName).remove();
            if (!getEventBus().hasListener(eventClass)
                    && eventRegistrations.get(eventName) != null) {
                eventRegistrations.get(eventName).remove();
                eventRegistrations.remove(eventName);
            }
        };
    }

    protected void onDataProviderChange() {
        if (dataProviderItemSetChangeRegistration != null) {
            dataProviderItemSetChangeRegistration.remove();
            dataProviderItemSetChangeRegistration = null;
        }

        if (dataProvider == null) {
            return;
        }

        requestUpdateItems();
        dataProviderItemSetChangeRegistration = dataProvider.addDataProviderListener(
                (DataProviderListener<DataItem>) event -> requestUpdateItems());
    }

    protected void initComponent() {
        options = createOptions();
        serializer = createSerializer();

        initOptionsChangeListener();

        Div div = new Div();
        div.setId("div-id");

        SlotUtils.addToSlot(this, "output", div);
    }

    protected PivotTableOptions createOptions() {
        return new PivotTableOptions();
    }

    protected JmixPivotTableSerializer createSerializer() {
        return new JmixPivotTableSerializer();
    }

    protected void initOptionsChangeListener() {
        options.setPivotTableObjectChangeListener(this::onOptionsChange);
    }

    protected void onOptionsChange(PivotTableOptionsObservable.ObjectChangeEvent event) {
        requestUpdateOptions();
    }

    protected void requestUpdateOptions() {
        // Do not call if it's still updating
        if (synchronizeOptionsExecution != null) {
            return;
        }

        getUI().ifPresent(ui ->
                synchronizeOptionsExecution = ui.beforeClientResponse(this, this::performUpdateOptions));
    }

    protected void requestUpdateItems() {
        // Do not call if it's still updating
        if (synchronizeItemsExecution != null || getDataProvider() == null) {
            return;
        }

        getUI().ifPresent(ui ->
                synchronizeItemsExecution = ui.beforeClientResponse(this, this::performUpdateItems));
    }

    protected void performUpdateOptions(ExecutionContext context) {
        JsonObject resultJson = new JreJsonFactory().createObject();
        JsonValue optionsJson = serializer.serializeOptions(options);
        resultJson.put("options", optionsJson);
        callPendingJsFunction("_updateOptions", resultJson);

        synchronizeOptionsExecution = null;
    }

    protected void performUpdateItems(ExecutionContext context) {
        JsonObject resultJson = new JreJsonFactory().createObject();
        List<DataItem> dataItems = getDataProvider().fetch(new Query<>()).toList();
        JsonValue dataJson = serializer.serializeItems(dataItems.stream()
                .map(dataItem ->{
                        Map<String, Object> values = new HashMap<>();
                    for (Map.Entry<String, String> property : options.getProperties().entrySet()) {
                            values.put(property.getValue(), dataItem.getValue(property.getKey()));
                        }
                        return values;
                })
                .collect(Collectors.toList()));
        resultJson.put("dataSet", dataJson);
        callPendingJsFunction("_updateDataSet", resultJson);

        synchronizeItemsExecution = null;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        requestUpdateOptions();
        requestUpdateItems();
    }

    @ClientCallable
    protected void onClientOptionsChanged(String clientChangedOptionsJson) {
        options.setChangedFromClient(true);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PivotTableOptions clientOptions = objectMapper.readValue(clientChangedOptionsJson, PivotTableOptions.class);
            options.setRows(clientOptions.getRows());
            options.setCols(clientOptions.getCols());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        options.setChangedFromClient(false);
    }

    @ClientCallable
    protected void ready() {
        synchronized (functions) {
            while (!functions.isEmpty()) {
                PendingJsFunction function = functions.remove(0);
                callJsFunction(function.getFunction(), function.getResultJson());
            }
            clientReady = true;
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        clientReady = false;
        super.onDetach(detachEvent);
    }

    /**
     * Execute JavaScript function with the {@code resultJson} passed. Execution will be delayed till
     * the client-side is ready.
     *
     * @param function   JavaScript function to execute
     * @param resultJson resultJson
     */
    protected synchronized void callPendingJsFunction(String function, JsonObject resultJson) {
        if (clientReady) {
            callJsFunction(function, resultJson);
        } else {
            synchronized (functions) {
                if (clientReady) {
                    callJsFunction(function, resultJson);
                } else {
                    functions.add(new PendingJsFunction(function, resultJson));
                }
            }
        }
    }

    protected void callJsFunction(String function, JsonObject resultJson) {
        getElement().callJsFunction(function, resultJson);
    }

    @SuppressWarnings("ClassCanBeRecord")
    protected static class PendingJsFunction {
        protected final String function;
        protected final JsonObject resultJson;

        public PendingJsFunction(String function, JsonObject resultJson) {
            this.function = function;
            this.resultJson = resultJson;
        }

        public String getFunction() {
            return function;
        }

        public JsonObject getResultJson() {
            return resultJson;
        }
    }
}

