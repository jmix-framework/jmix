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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.pivottableflowui.kit.component.model.*;
import io.jmix.pivottableflowui.kit.component.serialization.JmixPivotTableSerializer;
import io.jmix.pivottableflowui.kit.data.JmixEmptyPivotTableItems;
import io.jmix.pivottableflowui.kit.data.JmixPivotTableItems;
import io.jmix.pivottableflowui.kit.event.PivotTableCellClickEvent;
import io.jmix.pivottableflowui.kit.event.PivotTableRefreshEvent;
import io.jmix.pivottableflowui.kit.event.PivotTableRefreshEventDetail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base component for implementing pivot table.
 *
 * @param <T> type of items contained
 */
@Tag("jmix-pivot-table")
@JsModule("./src/pivot-table/jmix-pivot-table.js")
public class JmixPivotTable<T> extends Component implements HasEnabled, HasSize {

    protected static final String DATA_ITEM_ID_PROPERTY_NAME = "$k";

    protected JmixPivotTableItems<T> jmixPivotTableItems = JmixEmptyPivotTableItems.getInstance();
    protected Registration jmixPivotTableItemsChangeRegistration;

    protected PivotTableOptions options;
    protected JmixPivotTableSerializer serializer;

    protected volatile boolean clientReady;
    protected final List<PendingJsFunction> functions = new ArrayList<>();

    protected StateTree.ExecutionRegistration synchronizeOptionsExecution;
    protected StateTree.ExecutionRegistration synchronizeItemsExecution;

    public JmixPivotTable() {
        initComponent();
    }

    public JmixPivotTableItems<T> getItems() {
        return jmixPivotTableItems;
    }

    /**
     * Adds a listener to the pivot table refresh events. Fired only for editable PivotTable.
     *
     * @param listener a listener to add
     * @return subscription
     */
    public Registration addRefreshEventListener(ComponentEventListener<PivotTableRefreshEvent> listener) {
        return getEventBus().addListener(PivotTableRefreshEvent.class, listener);
    }

    /**
     * Adds a listener to the pivot table cell click events. Fired only for table
     * renderers (table, heatmap, table barchart, col heatmap, row heatmap).
     *
     * @param listener a listener to add
     * @return subscription
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addCellClickListener(ComponentEventListener<PivotTableCellClickEvent<T>> listener) {
        return getEventBus().addListener(PivotTableCellClickEvent.class, (ComponentEventListener) listener);
    }

    /**
     * @return map of properties and their localized values
     */
    public Map<String, String> getProperties() {
        return options.getProperties();
    }

    /**
     * Sets properties and their localized values
     * @param properties map with properties and their localized values
     */
    public void setProperties(Map<String, String> properties) {
        options.setProperties(properties);
    }

    /**
     * Adds properties and their localized values
     * @param properties map with properties and their localized values
     */
    public void addProperties(Map<String, String> properties) {
        options.addProperties(properties);
    }

    /**
     * Adds property and its localized value
     * @param property property name
     * @param value property localized value
     */
    public void addProperty(String property, String value) {
        options.addProperty(property, value);
    }

    /**
     * @return a collection of attribute names to use as rows
     */
    public List<String> getRows() {
        return options.getRows();
    }

    /**
     * Sets a collection of attribute names to use as rows.
     *
     * @param rows a collection of attribute names to use as rows
     */
    public void setRows(List<String> rows) {
        options.setRows(rows);
    }

    /**
     * Adds an array of attribute names to use as rows.
     *
     * @param rows an array of attribute names to add
     */
    public void addRows(String... rows) {
        options.addRows(rows);
    }

    /**
     * @return a collection of attribute names to use as cols
     */
    public List<String> getColumns() {
        return options.getColumns();
    }

    /**
     * Sets a collection of attribute names to use as cols.
     *
     * @param columns a collection of attribute names to use as cols
     */
    public void setColumns(List<String> columns) {
        options.setColumns(columns);
    }

    /**
     * Adds an array of attribute names to use as cols.
     *
     * @param columns an array of attribute names to add
     */
    public void addColumns(String... columns) {
        options.addColumns(columns);
    }

    /**
     * @return an object which will aggregate results per cell
     */
    public Aggregation getAggregation() {
        return options.getAggregation();
    }

    /**
     * Original property name: {@code aggregator}.
     * <p>
     * Sets a descriptor of an object which will aggregate results per cell
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Aggregators">documentation</a>).
     * <p>
     * Applies only when {@code this.isShowUI() == false}.
     *
     * @param aggregation an object which will aggregate results per cell
     */
    public void setAggregation(Aggregation aggregation) {
        options.setAggregation(aggregation);
    }

    /**
     * @return an object which will generate output from pivot data structure
     */
    public Renderer getRenderer() {
        return options.getRenderer();
    }

    /**
     * Sets a descriptor of an object which will generate output from pivot data structure
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Renderers">documentation</a>).
     * <p>
     * Applies only when {@code this.isShowUI() == false}.
     *
     * @param renderer an object which will generate output from pivot data structure
     */
    public void setRenderer(Renderer renderer) {
        options.setRenderer(renderer);
    }

    /**
     * @return attribute names to prepopulate in vals area
     */
    public List<String> getAggregationProperties() {
        return options.getAggregationProperties();
    }

    /**
     * Original property name: {@code vals}.
     * <p>
     * Sets attribute names to prepopulate in vals area (gets passed to aggregator generating function).
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param aggregationProperties attribute names to prepopulate in vals area
     */
    public void setAggregationProperties(List<String> aggregationProperties) {
        options.setAggregationProperties(aggregationProperties);
    }

    /**
     * Original property name: {@code vals}.
     * <p>
     * Adds attribute names to prepopulate in vals area (gets passed to aggregator generating function).
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param aggregationProperties attribute names to prepopulate in vals area
     */
    public void addAggregationProperties(String... aggregationProperties) {
        options.addAggregationProperties(aggregationProperties);
    }

    /**
     * @return an object that represents a list of generators for aggregation functions in dropdown
     */
    public Aggregations getAggregations() {
        return options.getAggregations();
    }

    /**
     * Original property name: {@code aggregators}.
     * <p>
     * Sets an object that represents a list of generators for aggregation functions in dropdown
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Aggregators">documentation</a>).
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param aggregations an object that represents a list of generators for aggregation functions in dropdown
     */
    public void setAggregations(Aggregations aggregations) {
        options.setAggregations(aggregations);
    }

    /**
     * @return n object that represents a list of rendering functions
     */
    public Renderers getRenderers() {
        return options.getRenderers();
    }

    /**
     * Sets an object that represents a list of rendering functions
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Renderers">documentation</a>).
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param renderers n object that represents a list of rendering functions
     */
    public void setRenderers(Renderers renderers) {
        options.setRenderers(renderers);
    }

    /**
     * @return attribute names to omit from the UI
     */
    public List<String> getHiddenProperties() {
        return options.getHiddenProperties();
    }

    /**
     * Sets attribute names to omit from the UI.
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param hiddenProperties attribute names to omit from the UI
     */
    public void setHiddenProperties(List<String> hiddenProperties) {
        options.setHiddenProperties(hiddenProperties);
    }

    /**
     * Adds attribute names to omit from the UI.
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param hiddenProperties attribute names to omit from the UI
     */
    public void addHiddenProperties(String... hiddenProperties) {
        options.addHiddenProperties(hiddenProperties);
    }

    /**
     * @return attribute names to omit from the aggregation arguments dropdowns
     */
    public List<String> getHiddenFromAggregations() {
        return options.getHiddenFromAggregations();
    }

    /**
     * Sets attribute names to omit from the aggregation arguments dropdowns.
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param hiddenFromAggregations attribute names to omit from the aggregation arguments dropdowns
     */
    public void setHiddenFromAggregations(List<String> hiddenFromAggregations) {
        options.setHiddenFromAggregations(hiddenFromAggregations);
    }

    /**
     * Adds attribute names to omit from the aggregation arguments dropdowns.
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param hiddenFromAggregations attribute names to omit from the aggregation arguments dropdowns
     */
    public void addHiddenFromAggregations(String... hiddenFromAggregations) {
        options.addHiddenFromAggregations(hiddenFromAggregations);
    }

    /**
     * @return attribute names to omit from the drag'n'drop portion of the UI
     */
    public List<String> getHiddenFromDragDrop() {
        return options.getHiddenFromDragDrop();
    }

    /**
     * Sets attribute names to omit from the drag'n'drop portion of the UI.
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param hiddenFromDragDrop attribute names to omit from the drag'n'drop portion of the UI
     */
    public void setHiddenFromDragDrop(List<String> hiddenFromDragDrop) {
        options.setHiddenFromDragDrop(hiddenFromDragDrop);
    }

    /**
     * Adds attribute names to omit from the drag'n'drop portion of the UI.
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param hiddenFromDragDrop attribute names to omit from the drag'n'drop portion of the UI
     */
    public void addHiddenFromDragDrop(String... hiddenFromDragDrop) {
        options.addHiddenFromDragDrop(hiddenFromDragDrop);
    }

    /**
     * @return the order in which column data is provided to the renderer
     */
    public Order getColumnOrder() {
        return options.getColumnOrder();
    }

    /**
     * Sets the order in which column data is provided to the renderer.
     * <p>
     * Ordering by value orders by column total.
     *
     * @param columnOrder the order in which column data is provided to the renderer
     */
    public void setColumnOrder(Order columnOrder) {
        options.setColumnOrder(columnOrder);
    }

    /**
     * @return the order in which row data is provided to the renderer
     */
    public Order getRowOrder() {
        return options.getRowOrder();
    }

    /**
     * Sets the order in which row data is provided to the renderer.
     * <p>
     * Ordering by value orders by row total.
     *
     * @param rowOrder the order in which row data is provided to the renderer
     */
    public void setRowOrder(Order rowOrder) {
        options.setRowOrder(rowOrder);
    }

    /**
     * @return the maximum number of values to list in the attribute values dialog
     */
    public Integer getMenuLimit() {
        return options.getMenuLimit();
    }

    /**
     * Sets the maximum number of values to list in the attribute values dialog
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param menuLimit the maximum number of values to list in the attribute values dialog
     * @return a reference to this object
     */
    public void setMenuLimit(Integer menuLimit) {
        options.setMenuLimit(menuLimit);
    }

    /**
     * @return whether or not unused attributes are kept sorted in the UI
     */
    public Boolean getAutoSortUnusedProperties() {
        return options.getAutoSortUnusedProperties();
    }

    /**
     * Original property name: {@code autoSortUnusedAttrs}.
     * <p>
     * Sets whether or not unused attributes are kept sorted in the UI.
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param autoSortUnusedProperties whether or not unused attributes are kept sorted in the UI
     */
    public void setAutoSortUnusedProperties(Boolean autoSortUnusedProperties) {
        options.setAutoSortUnusedProperties(autoSortUnusedProperties);
    }

    /**
     * @return whether or not unused attributes are shown vertically
     */
    public UnusedPropertiesVertical getUnusedPropertiesVertical() {
        return options.getUnusedPropertiesVertical();
    }

    /**
     * Original property name: {@code unusedAttrsVertical}.
     * <p>
     * Sets whether or not unused attributes are shown vertically
     * instead of the default which is horizontally. {@code true} means
     * always vertical, {@code false} means always horizontal. If set to
     * a number (as is the default) then if the attributes' names' combined
     * length in characters exceeds the number then the attributes will be shown vertically.
     * <p>
     * Applies only when  {@code this.isShowUI() == true}.
     *
     * @param unusedPropertiesVertical whether or not unused attributes are shown vertically
     */
    public void setUnusedPropertiesVertical(UnusedPropertiesVertical unusedPropertiesVertical) {
        options.setUnusedPropertiesVertical(unusedPropertiesVertical);
    }

    /**
     * @return a filter function that is called on each record
     */
    public JsFunction getFilterFunction() {
        return options.getFilterFunction();
    }

    /**
     * Original property name: {@code filter}.
     * <p>
     * Sets a filter function that is called on each record, returns {@code false} if the record
     * is to be excluded from the input before rendering or {@code true} otherwise.
     *
     * @param filter a filter function that is called on each record
     */
    public void setFilterFunction(JsFunction filter) {
        options.setFilterFunction(filter);
    }

    /**
     * @return a sorter function
     */
    public JsFunction getSortersFunction() {
        return options.getSortersFunction();
    }

    /**
     * Original property name: {@code sorters}.
     * <p>
     * Sets a sorter function that is called with an attribute name and can return
     * a function which can be used as an argument to {@code Array.sort} for output
     * purposes. If no function is returned, the default sorting mechanism is a built-in
     * "natural sort" implementation. Useful for sorting attributes like month names.
     *
     * @param sorters a sorter function
     * @return a reference to this object
     */
    public void setSortersFunction(JsFunction sorters) {
        options.setSortersFunction(sorters);
    }

    /**
     * @return an object that is passed through to renderer as options
     */
    public RendererOptions getRendererOptions() {
        return options.getRendererOptions();
    }

    /**
     * Sets an object that is passed through to renderer as options.
     *
     * @param rendererOptions an object that is passed through to renderer as options
     */
    public void setRendererOptions(RendererOptions rendererOptions) {
        options.setRendererOptions(rendererOptions);
    }

    /**
     * @return a map whose keys are attribute names and values are arrays of attribute values
     * @see #getExclusions()
     */
    public Map<String, List<String>> getInclusions() {
        return options.getInclusions();
    }

    /**
     * Sets a map whose keys are attribute names and values are arrays of attribute values
     * which denote records to include in rendering; used to prepopulate the filter menus
     * that appear on double-click (overrides {@link #getExclusions()}).
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param inclusions a map whose keys are attribute names and values are arrays of attribute values
     * @see #setExclusions(Map)
     */
    public void setInclusions(Map<String, List<String>> inclusions) {
        options.setInclusions(inclusions);
    }

    /**
     * Sets a list whose values are arrays of attribute values
     * which denote records to include in rendering; used to prepopulate the filter menus
     * that appear on double-click (overrides {@link #getExclusions()}).
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param property   a property for which set inclusions
     * @param inclusions a list of property values
     * @see #setExclusions(String, List)
     */
    public void setInclusions(String property, List<String> inclusions) {
        options.setInclusions(property, inclusions);
    }

    /**
     * Adds property values to a given property
     * which denote records to include in rendering; used to prepopulate the filter menus
     * that appear on double-click (overrides {@link #getExclusions()}).
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param property   a property for which set inclusions
     * @param inclusions an array of property values
     * @see #addExclusions(String, String...)
     */
    public void addInclusions(String property, String... inclusions) {
        options.addInclusions(property, inclusions);
    }

    /**
     * @return a map whose keys are attribute names and values are arrays of attribute values
     */
    public Map<String, List<String>> getExclusions() {
        return options.getExclusions();
    }

    /**
     * Sets a map whose keys are attribute names and values are arrays of attribute values
     * which denote records to exclude from rendering; used to prepopulate the filter menus
     * that appear on double-click.
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param exclusions a map whose keys are attribute names and values are arrays of attribute values
     * @see #setInclusions(Map)
     */
    public void setExclusions(Map<String, List<String>> exclusions) {
        options.setExclusions(exclusions);
    }

    /**
     * Sets a list whose values are arrays of attribute values
     * which denote records to exclude from rendering; used to prepopulate the filter menus
     * that appear on double-click.
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param property   a property for which set exclusions
     * @param exclusions a map whose keys are attribute names and values are arrays of attribute values
     * @see #setInclusions(Map)
     */
    public void setExclusions(String property, List<String> exclusions) {
        options.setExclusions(property, exclusions);
    }

    /**
     * Adds property values to a given property
     * which denote records to exclude from rendering; used to prepopulate the filter menus
     * that appear on double-click.
     * <p>
     * Applies only when {@code this.isShowUI() == true}.
     *
     * @param property   a property for which set exclusions
     * @param exclusions a map whose keys are attribute names and values are arrays of attribute values
     * @see #setInclusions(Map)
     */
    public void addExclusions(String property, String... exclusions) {
        options.addExclusions(property, exclusions);
    }

    /**
     * @return an object that represents derived properties
     */
    public DerivedProperties getDerivedProperties() {
        return options.getDerivedProperties();
    }

    /**
     * Original property name: {@code derivedAttributes}.
     * <p>
     * Sets an object that represents derived properties
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Derived-Attributes">documentation</a>).
     *
     * @param derivedProperties an object that represents derived properties
     */
    public void setDerivedProperties(DerivedProperties derivedProperties) {
        options.setDerivedProperties(derivedProperties);
    }

    /**
     * Sets the message that will be displayed in case of empty data.
     *
     * @param emptyDataMessage the message that will be displayed in case of empty data
     */
    public void setEmptyDataMessage(String emptyDataMessage) {
        options.setEmptyDataMessage(emptyDataMessage);
    }

    /**
     * @return the message that will be displayed in case of empty data
     */
    public String getEmptyDataMessage() {
        return options.getEmptyDataMessage();
    }

    /**
     * Show pivot table in the UI mode.
     *
     * @param showUI {@code true} if pivot table UI is shown, {@code false} if not.
     */
    public void setShowUI(Boolean showUI) {
        options.setShowUI(showUI);
    }

    /**
     * @return {@code true} if pivot table UI is shown, {@code false} if not.
     */
    public Boolean isShowUI() {
        return options.isShowUI();
    }

    /**
     * Shows an additional column of totals for each row
     *
     * @param showRowTotals {@code true} if row totals are shown, {@code false} if not.
     */
    public void setShowRowTotals(Boolean showRowTotals) {
        options.setShowRowTotals(showRowTotals);
    }

    /**
     * @return {@code true} if row totals are shown, {@code false} if not.
     */
    public Boolean isShowRowTotals() {
        return options.isShowRowTotals();
    }

    /**
     * Shows an additional column of totals for each col
     *
     * @param showColumnTotals {@code true} if col totals are shown, {@code false} if not.
     */
    public void setShowColumnTotals(Boolean showColumnTotals) {
        options.setShowColumnTotals(showColumnTotals);
    }

    /**
     * @return {@code true} if col totals are shown, {@code false} if not.
     */
    public Boolean isShowColumnTotals() {
        return options.isShowColumnTotals();
    }

    /**
     * Sets the {@link JmixPivotTableItems} instance to use as data provider.
     *
     * @param items the {@link JmixPivotTableItems} instance to use
     */
    public void setItems(JmixPivotTableItems<T> items) {
        if (this.jmixPivotTableItems != JmixEmptyPivotTableItems.getInstance()) {
            if (jmixPivotTableItemsChangeRegistration != null) {
                jmixPivotTableItemsChangeRegistration.remove();
                jmixPivotTableItemsChangeRegistration = null;
            }
        }

        if (items != null) {
            this.jmixPivotTableItems = items;
            jmixPivotTableItemsChangeRegistration = items.addItemsChangeListener(e -> requestUpdateItems());
        } else {
            this.jmixPivotTableItems = JmixEmptyPivotTableItems.getInstance();
        }

        requestUpdateItems();
    }

    protected void initComponent() {
        options = createOptions();
        serializer = createSerializer();

        initComponentListeners();
    }

    protected PivotTableOptions createOptions() {
        return new PivotTableOptions();
    }

    protected JmixPivotTableSerializer createSerializer() {
        return new JmixPivotTableSerializer();
    }

    protected void initComponentListeners() {
        options.setPivotTableObjectChangeListener(this::onOptionsChange);

        getEventBus().addListener(PivotTableRefreshEvent.class, this::onRefresh);
    }

    protected void onOptionsChange(PivotTableOptionsObservable.ObjectChangeEvent event) {
        requestUpdateOptions();
    }

    protected void onRefresh(PivotTableRefreshEvent event) {
        updateOptionsAfterRefresh(event.getDetail());
    }

    protected void updateOptionsAfterRefresh(PivotTableRefreshEventDetail detail) {
        options.setChangedFromClient(true);

        options.setRows(detail.getRows());
        options.setColumns(detail.getColumns());
        if (options.getRenderers() != null) {
            options.getRenderers().setSelectedRenderer(detail.getRenderer());
        } else {
            options.setRenderer(detail.getRenderer());
        }
        if (options.getAggregations() != null) {
            options.getAggregations().setSelectedAggregation(detail.getAggregationMode());
        } else {

            // If we get a refresh event, the component shows with UI.
            // So create an empty aggregation to store the aggregation mode received from the client.
            // Now it can be saved in the settings.
            Aggregation aggregation = new Aggregation();
            aggregation.setMode(detail.getAggregationMode());
            options.setAggregation(aggregation);
        }
        options.setAggregationProperties(detail.getAggregationProperties());
        options.setInclusions(detail.getInclusions());
        options.setExclusions(detail.getExclusions());
        options.setColumnOrder(detail.getColumnOrder());
        options.setRowOrder(detail.getRowOrder());

        options.setChangedFromClient(false);
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
        if (synchronizeItemsExecution != null || getItems().getItems().isEmpty()) {
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
        Collection<T> items = jmixPivotTableItems.getItems();
        JsonValue dataJson = serializer.serializeItems(items.stream().map(item -> {
                    Map<String, Object> propertyWithValue = options.getProperties()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getValue,
                                    e -> jmixPivotTableItems.getItemValue(item, e.getKey())));
                    propertyWithValue.put(DATA_ITEM_ID_PROPERTY_NAME, jmixPivotTableItems.getItemId(item));
                    return propertyWithValue;
                })
                .toList());
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
    protected void ready() {
        synchronized (functions) {
            while (!functions.isEmpty()) {
                PendingJsFunction function = functions.remove(0);
                callJsFunction(function.function(), function.resultJson());
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

    protected record PendingJsFunction(String function, JsonObject resultJson) {
    }
}