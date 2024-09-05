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

package io.jmix.pivottableflowui.kit.component.model;

import java.util.*;
import java.util.function.Consumer;

public class PivotTableOptions extends PivotTableOptionsObservable {

    protected Map<String, String> properties;
    protected List<String> rows;
    protected List<String> cols;
    protected Aggregation aggregation;
    protected Renderer renderer;
    protected List<String> aggregationProperties;
    protected Aggregations aggregations;
    protected Renderers renderers;
    protected List<String> hiddenProperties;
    protected List<String> hiddenFromAggregations;
    protected List<String> hiddenFromDragDrop;
    protected Order colOrder;
    protected Order rowOrder;
    protected Integer menuLimit;
    protected Boolean autoSortUnusedProperties;
    protected UnusedPropertiesVertical unusedPropertiesVertical;
    protected JsFunction filterFunction;
    protected JsFunction sortersFunction;
    protected RendererOptions rendererOptions;
    protected Map<String, List<String>> inclusions;
    protected Map<String, List<String>> exclusions;
    protected DerivedProperties derivedProperties;
    protected String localeCode;
    protected Boolean showUI;
    protected Boolean rowTotals;
    protected Boolean colTotals;
    protected String emptyDataMessage;
    protected Map<String, Object> localizedStrings;
    protected String nativeJson;

    @Override
    public void setPivotTableObjectChangeListener(Consumer<ObjectChangeEvent> listener) {
        super.setPivotTableObjectChangeListener(listener);
    }

    /**
     * @return data provider properties to serialize
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Sets data provider properties to serialize.
     *
     * @param properties data provider properties to serialize
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
        markAsChanged();
    }

    /**
     * Adds data provider properties to serialize.
     *
     * @param properties data provider properties to serialize
     */
    public void addProperties(Map<String, String> properties) {
        if (properties != null) {
            if (this.properties == null) {
                this.properties = new HashMap<>();
            }
            this.properties.putAll(properties);
            markAsChanged();
        }
    }

    /**
     * Sets data provider property to serialize.
     *
     * @param property data provider property to serialize
     * @param value    a property value
     */
    public void addProperty(String property, String value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(property, value);
        markAsChanged();
    }

    /**
     * @return a collection of attribute names to use as rows
     */
    public List<String> getRows() {
        return rows;
    }

    /**
     * Sets a collection of attribute names to use as rows.
     *
     * @param rows a collection of attribute names to use as rows
     */
    public void setRows(List<String> rows) {
        this.rows = rows;
        markAsChanged();
    }

    /**
     * Adds an array of attribute names to use as rows.
     *
     * @param rows an array of attribute names to add
     */
    public void addRows(String... rows) {
        if (rows != null) {
            if (this.rows == null) {
                this.rows = new ArrayList<>();
            }
            this.rows.addAll(Arrays.asList(rows));
            markAsChanged();
        }
    }

    /**
     * @return a collection of attribute names to use as columns
     */
    public List<String> getCols() {
        return cols;
    }

    /**
     * Sets a collection of attribute names to use as columns.
     *
     * @param cols a collection of attribute names to use as columns
     */
    public void setCols(List<String> cols) {
        this.cols = cols;
        markAsChanged();
    }

    /**
     * Adds an array of attribute names to use as columns.
     *
     * @param cols an array of attribute names to add
     */
    public void addCols(String... cols) {
        if (cols != null) {
            if (this.cols == null) {
                this.cols = new ArrayList<>();
            }
            this.cols.addAll(Arrays.asList(cols));
            markAsChanged();
        }
    }

    /**
     * @return an object which will aggregate results per cell
     */
    public Aggregation getAggregation() {
        return aggregation;
    }

    /**
     * Original property name: {@code aggregator}.
     * <p>
     * Sets a descriptor of an object which will aggregate results per cell
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Aggregators">documentation</a>).
     * <p>
     * Applies only when {@code editable=false}.
     *
     * @param aggregation an object which will aggregate results per cell
     */
    public void setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
        markAsChanged();
    }

    /**
     * @return an object which will generate output from pivot data structure
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Sets a descriptor of an object which will generate output from pivot data structure
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Renderers">documentation</a>).
     * <p>
     * Applies only when {@code editable=false}.
     *
     * @param renderer an object which will generate output from pivot data structure
     */
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
        markAsChanged();
    }

    /**
     * @return attribute names to prepopulate in vals area
     */
    public List<String> getAggregationProperties() {
        return aggregationProperties;
    }

    /**
     * Original property name: {@code vals}.
     * <p>
     * Sets attribute names to prepopulate in vals area (gets passed to aggregator generating function).
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param aggregationProperties attribute names to prepopulate in vals area
     */
    public void setAggregationProperties(List<String> aggregationProperties) {
        this.aggregationProperties = aggregationProperties;
        markAsChanged();
    }

    /**
     * Original property name: {@code vals}.
     * <p>
     * Adds attribute names to prepopulate in vals area (gets passed to aggregator generating function).
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param aggregationProperties attribute names to prepopulate in vals area
     */
    public void addAggregationProperties(String... aggregationProperties) {
        if (aggregationProperties != null) {
            if (this.aggregationProperties == null) {
                this.aggregationProperties = new ArrayList<>();
            }
            this.aggregationProperties.addAll(Arrays.asList(aggregationProperties));
            markAsChanged();
        }
    }

    /**
     * @return an object that represents a list of generators for aggregation functions in dropdown
     */
    public Aggregations getAggregations() {
        return aggregations;
    }

    /**
     * Original property name: {@code aggregators}.
     * <p>
     * Sets an object that represents a list of generators for aggregation functions in dropdown
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Aggregators">documentation</a>).
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param aggregations an object that represents a list of generators for aggregation functions in dropdown
     */
    public void setAggregations(Aggregations aggregations) {
        this.aggregations = aggregations;
        markAsChanged();
    }

    /**
     * @return n object that represents a list of rendering functions
     */
    public Renderers getRenderers() {
        return renderers;
    }

    /**
     * Sets an object that represents a list of rendering functions
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Renderers">documentation</a>).
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param renderers n object that represents a list of rendering functions
     */
    public void setRenderers(Renderers renderers) {
        this.renderers = renderers;
        markAsChanged();
    }

    /**
     * @return attribute names to omit from the UI
     */
    public List<String> getHiddenProperties() {
        return hiddenProperties;
    }

    /**
     * Sets attribute names to omit from the UI.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param hiddenProperties attribute names to omit from the UI
     */
    public void setHiddenProperties(List<String> hiddenProperties) {
        this.hiddenProperties = hiddenProperties;
        markAsChanged();
    }

    /**
     * Adds attribute names to omit from the UI.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param hiddenProperties attribute names to omit from the UI
     */
    public void addHiddenProperties(String... hiddenProperties) {
        if (hiddenProperties != null) {
            if (this.hiddenProperties == null) {
                this.hiddenProperties = new ArrayList<>();
            }
            this.hiddenProperties.addAll(Arrays.asList(hiddenProperties));
            markAsChanged();
        }
    }

    /**
     * @return attribute names to omit from the aggregation arguments dropdowns
     */
    public List<String> getHiddenFromAggregations() {
        return hiddenFromAggregations;
    }

    /**
     * Sets attribute names to omit from the aggregation arguments dropdowns.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param hiddenFromAggregations attribute names to omit from the aggregation arguments dropdowns
     */
    public void setHiddenFromAggregations(List<String> hiddenFromAggregations) {
        this.hiddenFromAggregations = hiddenFromAggregations;
        markAsChanged();
    }

    /**
     * Adds attribute names to omit from the aggregation arguments dropdowns.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param hiddenFromAggregations attribute names to omit from the aggregation arguments dropdowns
     */
    public void addHiddenFromAggregations(String... hiddenFromAggregations) {
        if (hiddenFromAggregations != null) {
            if (this.hiddenFromAggregations == null) {
                this.hiddenFromAggregations = new ArrayList<>();
            }
            this.hiddenFromAggregations.addAll(Arrays.asList(hiddenFromAggregations));
            markAsChanged();
        }
    }

    /**
     * @return attribute names to omit from the drag'n'drop portion of the UI
     */
    public List<String> getHiddenFromDragDrop() {
        return hiddenFromDragDrop;
    }

    /**
     * Sets attribute names to omit from the drag'n'drop portion of the UI.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param hiddenFromDragDrop attribute names to omit from the drag'n'drop portion of the UI
     */
    public void setHiddenFromDragDrop(List<String> hiddenFromDragDrop) {
        this.hiddenFromDragDrop = hiddenFromDragDrop;
        markAsChanged();
    }

    /**
     * Adds attribute names to omit from the drag'n'drop portion of the UI.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param hiddenFromDragDrop attribute names to omit from the drag'n'drop portion of the UI
     */
    public void addHiddenFromDragDrop(String... hiddenFromDragDrop) {
        if (hiddenFromDragDrop != null) {
            if (this.hiddenFromDragDrop == null) {
                this.hiddenFromDragDrop = new ArrayList<>();
            }
            this.hiddenFromDragDrop.addAll(Arrays.asList(hiddenFromDragDrop));
            markAsChanged();
        }
    }

    /**
     * @return the order in which column data is provided to the renderer
     */
    public Order getColOrder() {
        return colOrder;
    }

    /**
     * Sets the order in which column data is provided to the renderer.
     * <p>
     * Ordering by value orders by column total.
     *
     * @param colOrder the order in which column data is provided to the renderer
     */
    public void setColOrder(Order colOrder) {
        this.colOrder = colOrder;
        markAsChanged();
    }

    /**
     * @return the order in which row data is provided to the renderer
     */
    public Order getRowOrder() {
        return rowOrder;
    }

    /**
     * Sets the order in which row data is provided to the renderer.
     * <p>
     * Ordering by value orders by row total.
     *
     * @param rowOrder the order in which row data is provided to the renderer
     */
    public void setRowOrder(Order rowOrder) {
        this.rowOrder = rowOrder;
        markAsChanged();
    }

    /**
     * @return the maximum number of values to list in the double click menu
     */
    public Integer getMenuLimit() {
        return menuLimit;
    }

    /**
     * Sets the maximum number of values to list in the double click menu.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param menuLimit the maximum number of values to list in the double click menu
     */
    public void setMenuLimit(Integer menuLimit) {
        this.menuLimit = menuLimit;
        markAsChanged();
    }

    /**
     * @return whether unused attributes are kept sorted in the UI
     */
    public Boolean getAutoSortUnusedProperties() {
        return autoSortUnusedProperties;
    }

    /**
     * Original property name: {@code autoSortUnusedAttrs}.
     * <p>
     * Sets whether unused attributes are kept sorted in the UI.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param autoSortUnusedProperties whether unused attributes are kept sorted in the UI
     */
    public void setAutoSortUnusedProperties(Boolean autoSortUnusedProperties) {
        this.autoSortUnusedProperties = autoSortUnusedProperties;
        markAsChanged();
    }

    /**
     * @return whether unused attributes are shown vertically
     */
    public UnusedPropertiesVertical getUnusedPropertiesVertical() {
        return unusedPropertiesVertical;
    }

    /**
     * Original property name: {@code unusedAttrsVertical}.
     * <p>
     * Sets whether unused attributes are shown vertically
     * instead of the default which is horizontally. {@code true} means
     * always vertical, {@code false} means always horizontal. If set to
     * a number (as is the default) then if the attributes' names' combined
     * length in characters exceeds the number then the attributes will be shown vertically.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param unusedPropertiesVertical whether unused attributes are shown vertically
     */
    public void setUnusedPropertiesVertical(UnusedPropertiesVertical unusedPropertiesVertical) {
        this.unusedPropertiesVertical = unusedPropertiesVertical;
        markAsChanged();
    }

    /**
     * @return a filter function that is called on each record
     */
    public JsFunction getFilterFunction() {
        return filterFunction;
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
        this.filterFunction = filter;
        markAsChanged();
    }

    /**
     * @return a sorter function
     */
    public JsFunction getSortersFunction() {
        return sortersFunction;
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
     */
    public void setSortersFunction(JsFunction sorters) {
        this.sortersFunction = sorters;
        markAsChanged();
    }

    /**
     * @return an object that is passed through to renderer as options
     */
    public RendererOptions getRendererOptions() {
        return rendererOptions;
    }

    /**
     * Sets an object that is passed through to renderer as options.
     *
     * @param rendererOptions an object that is passed through to renderer as options
     */
    public void setRendererOptions(RendererOptions rendererOptions) {
        this.rendererOptions = rendererOptions;
        markAsChanged();
    }

    /**
     * @return a map whose keys are attribute names and values are arrays of attribute values
     * @see #getExclusions()
     */
    public Map<String, List<String>> getInclusions() {
        return inclusions;
    }

    /**
     * Sets a map whose keys are attribute names and values are arrays of attribute values
     * which denote records to include in rendering; used to prepopulate the filter menus
     * that appear on double-click (overrides {@link #exclusions}).
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param inclusions a map whose keys are attribute names and values are arrays of attribute values
     * @see #setExclusions(Map)
     */
    public void setInclusions(Map<String, List<String>> inclusions) {
        this.inclusions = inclusions;
        markAsChanged();
    }

    /**
     * Sets a list whose values are arrays of attribute values
     * which denote records to include in rendering; used to prepopulate the filter menus
     * that appear on double-click (overrides {@link #exclusions}).
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param property   a property for which set inclusions
     * @param inclusions a list of property values
     * @see #setExclusions(String, List)
     */
    public void setInclusions(String property, List<String> inclusions) {
        if (this.inclusions == null) {
            this.inclusions = new HashMap<>();
        }
        this.inclusions.put(property, inclusions);
        markAsChanged();
    }

    /**
     * Adds property values to a given property
     * which denote records to include in rendering; used to prepopulate the filter menus
     * that appear on double-click (overrides {@link #exclusions}).
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param property   a property for which set inclusions
     * @param inclusions an array of property values
     * @see #addExclusions(String, String...)
     */
    public void addInclusions(String property, String... inclusions) {
        if (inclusions != null) {
            if (this.inclusions == null) {
                this.inclusions = new HashMap<>();
            }
            if (this.inclusions.containsKey(property)) {
                this.inclusions.get(property).addAll(Arrays.asList(inclusions));
            } else {
                this.inclusions.put(property, new ArrayList<>(Arrays.asList(inclusions)));
            }
            markAsChanged();
        }
    }

    /**
     * @return a map whose keys are attribute names and values are arrays of attribute values
     */
    public Map<String, List<String>> getExclusions() {
        return exclusions;
    }

    /**
     * Sets a map whose keys are attribute names and values are arrays of attribute values
     * which denote records to exclude from rendering; used to prepopulate the filter menus
     * that appear on double-click.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param exclusions a map whose keys are attribute names and values are arrays of attribute values
     * @see #setInclusions(Map)
     */
    public void setExclusions(Map<String, List<String>> exclusions) {
        this.exclusions = exclusions;
        markAsChanged();
    }

    /**
     * Sets a list whose values are arrays of attribute values
     * which denote records to exclude from rendering; used to prepopulate the filter menus
     * that appear on double-click.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param property   a property for which set exclusions
     * @param exclusions a map whose keys are attribute names and values are arrays of attribute values
     * @see #setInclusions(Map)
     */
    public void setExclusions(String property, List<String> exclusions) {
        if (this.exclusions == null) {
            this.exclusions = new HashMap<>();
        }
        this.exclusions.put(property, exclusions);
        markAsChanged();
    }

    /**
     * Adds property values to a given property
     * which denote records to exclude from rendering; used to prepopulate the filter menus
     * that appear on double-click.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param property   a property for which set exclusions
     * @param exclusions a map whose keys are attribute names and values are arrays of attribute values
     * @see #setInclusions(Map)
     */
    public void addExclusions(String property, String... exclusions) {
        if (exclusions != null) {
            if (this.exclusions == null) {
                this.exclusions = new HashMap<>();
            }
            if (this.exclusions.containsKey(property)) {
                this.exclusions.get(property).addAll(Arrays.asList(exclusions));
            } else {
                this.exclusions.put(property, new ArrayList<>(Arrays.asList(exclusions)));
            }
            markAsChanged();
        }
    }

    /**
     * @return an object that represents derived properties
     */
    public DerivedProperties getDerivedProperties() {
        return derivedProperties;
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
        this.derivedProperties = derivedProperties;
        markAsChanged();
    }

    public String getEmptyDataMessage() {
        return emptyDataMessage;
    }

    public void setEmptyDataMessage(String emptyDataMessage) {
        this.emptyDataMessage = emptyDataMessage;
    }

    /**
     * @return whether to show UI
     */
    public Boolean isShowUI() {
        return showUI;
    }

    /**
     * Shows or hides UI. {@code true} by default.
     * <br>
     * Applies only when {@code editable=true}.
     *
     * @param showUI show UI option
     */
    public void setShowUI(Boolean showUI) {
        this.showUI = showUI;
        markAsChanged();
    }

    /**
     * @return whether to show row totals
     */
    public Boolean isShowRowTotals() {
        return rowTotals;
    }

    /**
     * Shows or hides row totals. {@code true} by default.
     *
     * @param rowTotals row totals option
     */
    public void setShowRowTotals(Boolean rowTotals) {
        this.rowTotals = rowTotals;
        markAsChanged();
    }

    /**
     * @return whether to show col totals is shown
     */
    public Boolean isShowColTotals() {
        return colTotals;
    }

    /**
     * Shows or hides col totals. {@code true} by default.
     *
     * @param colTotals col total options
     */
    public void setShowColTotals(Boolean colTotals) {
        this.colTotals = colTotals;
        markAsChanged();
    }

    /**
     * @return a locale code
     */
    public String getLocaleCode() {
        return localeCode;
    }

    /**
     * Sets a locale code
     *
     * @param localeCode a locale code
     */
    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
        markAsChanged();
    }

    public Map<String, Object> getLocalizedStrings() {
        return localizedStrings;
    }

    public void setLocalizedStrings(Map<String, Object> localizedStrings) {
        this.localizedStrings = localizedStrings;
    }

    public String getNativeJson() {
        return nativeJson;
    }

    public void setNativeJson(String nativeJson) {
        this.nativeJson = nativeJson;
    }
}
