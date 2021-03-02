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

package io.jmix.pivottable.model;

import com.google.gson.annotations.Expose;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.DataProvider;
import io.jmix.ui.data.impl.ListDataProvider;
import org.apache.commons.collections4.MapUtils;

import java.util.*;

/**
 * Description of PivotTable configuration. <br>
 * <a href="https://github.com/nicolaskruchten/pivottable/">https://github.com/nicolaskruchten/pivottable/</a>
 * <br>
 * See documentation for properties of Pivot() and PivotUI() functions.<br>
 * <a href="https://github.com/nicolaskruchten/pivottable/wiki/Parameters">https://github.com/nicolaskruchten/pivottable/wiki/Parameters</a>
 */
public class PivotTableModel extends AbstractPivotObject {
    private static final long serialVersionUID = -1569394634634321813L;

    private boolean editable = false;                           // Haulmont API

    @Expose(serialize = false, deserialize = false)
    private Map<String, String> properties;                     // Haulmont API

    private List<String> rows;                                  // pivot() and pivotUI()

    private List<String> cols;                                  // pivot() and pivotUI()

    private Aggregation aggregation;                            // pivot()

    private Renderer renderer;                                  // pivot()

    private List<String> aggregationProperties;                 // pivotUI()

    private Aggregations aggregations;                          // pivotUI()

    private Renderers renderers;                                // pivotUI()

    private List<String> hiddenProperties;                      // pivotUI()

    private List<String> hiddenFromAggregations;                // pivotUI()

    private List<String> hiddenFromDragDrop;                    // pivotUI()

    private ColumnOrder columnOrder;                            // pivot() and pivotUI()

    private RowOrder rowOrder;                                  // pivot() and pivotUI()

    private Integer menuLimit;                                  // pivotUI()

    private Boolean autoSortUnusedProperties;                   // pivotUI()

    private UnusedPropertiesVertical unusedPropertiesVertical;  // pivotUI()

    private JsFunction filterFunction;                          // pivot() and pivotUI()

    private JsFunction sortersFunction;                         // pivot() and pivotUI()

    private RendererOptions rendererOptions;                    // pivot() and pivotUI()

    private Map<String, List<String>> inclusions;               // pivotUI()

    private Map<String, List<String>> exclusions;               // pivotUI()

    private DerivedProperties derivedProperties;                // pivot() and pivotUI()

    private String localeCode;                                  // Haulmont API

    private Boolean showUI;                                     // pivotUI()

    private Boolean rowTotals;                                  // pivot() and pivotUI()

    private Boolean colTotals;                                  // pivot() and pivotUI()

    @Expose(serialize = false, deserialize = false)
    private DataProvider dataProvider;                          // Haulmont API - object to be serialized as input data

    public PivotTableModel() {
    }

    /**
     * @return if {@code false} then {@code pivot()} function will
     * be called to generate PivotTable, {@code pivotUI()} otherwise
     */
    public boolean getEditable() {
        return editable;
    }

    /**
     * @param editable if {@code false} then {@code pivot()} function will be
     *                 called to generate PivotTable, {@code pivotUI()} otherwise
     * @return a reference to this object
     */
    public PivotTableModel setEditable(boolean editable) {
        this.editable = editable;
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Adds data provider properties to serialize.
     *
     * @param properties data provider properties to serialize
     * @return a reference to this object
     */
    public PivotTableModel addProperties(Map<String, String> properties) {
        if (properties != null) {
            if (this.properties == null) {
                this.properties = new HashMap<>();
            }
            this.properties.putAll(properties);
        }
        return this;
    }

    /**
     * Sets data provider property to serialize.
     *
     * @param property data provider property to serialize
     * @param value    a property value
     * @return a reference to this object
     */
    public PivotTableModel addProperty(String property, String value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(property, value);

        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setRows(List<String> rows) {
        this.rows = rows;
        return this;
    }

    /**
     * Adds an array of attribute names to use as rows.
     *
     * @param rows an array of attribute names to add
     * @return a reference to this object
     */
    public PivotTableModel addRows(String... rows) {
        if (rows != null) {
            if (this.rows == null) {
                this.rows = new ArrayList<>();
            }
            this.rows.addAll(Arrays.asList(rows));
        }
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setCols(List<String> cols) {
        this.cols = cols;
        return this;
    }

    /**
     * Adds an array of attribute names to use as columns.
     *
     * @param cols an array of attribute names to add
     * @return a reference to this object
     */
    public PivotTableModel addCols(String... cols) {
        if (cols != null) {
            if (this.cols == null) {
                this.cols = new ArrayList<>();
            }
            this.cols.addAll(Arrays.asList(cols));
        }
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setRenderer(Renderer renderer) {
        this.renderer = renderer;
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setAggregationProperties(List<String> aggregationProperties) {
        this.aggregationProperties = aggregationProperties;
        return this;
    }

    /**
     * Original property name: {@code vals}.
     * <p>
     * Adds attribute names to prepopulate in vals area (gets passed to aggregator generating function).
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param aggregationProperties attribute names to prepopulate in vals area
     * @return a reference to this object
     */
    public PivotTableModel addAggregationProperties(String... aggregationProperties) {
        if (aggregationProperties != null) {
            if (this.aggregationProperties == null) {
                this.aggregationProperties = new ArrayList<>();
            }
            this.aggregationProperties.addAll(Arrays.asList(aggregationProperties));
        }
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setAggregations(Aggregations aggregations) {
        this.aggregations = aggregations;
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setRenderers(Renderers renderers) {
        this.renderers = renderers;
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setHiddenProperties(List<String> hiddenProperties) {
        this.hiddenProperties = hiddenProperties;
        return this;
    }

    /**
     * Adds attribute names to omit from the UI.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param hiddenProperties attribute names to omit from the UI
     * @return a reference to this object
     */
    public PivotTableModel addHiddenProperties(String... hiddenProperties) {
        if (hiddenProperties != null) {
            if (this.hiddenProperties == null) {
                this.hiddenProperties = new ArrayList<>();
            }
            this.hiddenProperties.addAll(Arrays.asList(hiddenProperties));
        }
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setHiddenFromAggregations(List<String> hiddenFromAggregations) {
        this.hiddenFromAggregations = hiddenFromAggregations;
        return this;
    }

    /**
     * Adds attribute names to omit from the aggregation arguments dropdowns.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param hiddenFromAggregations attribute names to omit from the aggregation arguments dropdowns
     * @return a reference to this object
     */
    public PivotTableModel addHiddenFromAggregations(String... hiddenFromAggregations) {
        if (hiddenFromAggregations != null) {
            if (this.hiddenFromAggregations == null) {
                this.hiddenFromAggregations = new ArrayList<>();
            }
            this.hiddenFromAggregations.addAll(Arrays.asList(hiddenFromAggregations));
        }
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setHiddenFromDragDrop(List<String> hiddenFromDragDrop) {
        this.hiddenFromDragDrop = hiddenFromDragDrop;
        return this;
    }

    /**
     * Adds attribute names to omit from the drag'n'drop portion of the UI.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param hiddenFromDragDrop attribute names to omit from the drag'n'drop portion of the UI
     * @return a reference to this object
     */
    public PivotTableModel addHiddenFromDragDrop(String... hiddenFromDragDrop) {
        if (hiddenFromDragDrop != null) {
            if (this.hiddenFromDragDrop == null) {
                this.hiddenFromDragDrop = new ArrayList<>();
            }
            this.hiddenFromDragDrop.addAll(Arrays.asList(hiddenFromDragDrop));
        }
        return this;
    }

    /**
     * @return the order in which column data is provided to the renderer
     */
    public ColumnOrder getColumnOrder() {
        return columnOrder;
    }

    /**
     * Sets the order in which column data is provided to the renderer.
     * <p>
     * Ordering by value orders by column total.
     *
     * @param columnOrder the order in which column data is provided to the renderer
     * @return a reference to this object
     */
    public PivotTableModel setColumnOrder(ColumnOrder columnOrder) {
        this.columnOrder = columnOrder;
        return this;
    }

    /**
     * @return the order in which row data is provided to the renderer
     */
    public RowOrder getRowOrder() {
        return rowOrder;
    }

    /**
     * Sets the order in which row data is provided to the renderer.
     * <p>
     * Ordering by value orders by row total.
     *
     * @param rowOrder the order in which row data is provided to the renderer
     * @return a reference to this object
     */
    public PivotTableModel setRowOrder(RowOrder rowOrder) {
        this.rowOrder = rowOrder;
        return this;
    }

    /**
     * @return the maximum number of values to list in the double-click menu
     */
    public Integer getMenuLimit() {
        return menuLimit;
    }

    /**
     * Sets the maximum number of values to list in the double-click menu.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param menuLimit the maximum number of values to list in the double-click menu
     * @return a reference to this object
     */
    public PivotTableModel setMenuLimit(Integer menuLimit) {
        this.menuLimit = menuLimit;
        return this;
    }

    /**
     * @return whether or not unused attributes are kept sorted in the UI
     */
    public Boolean getAutoSortUnusedProperties() {
        return autoSortUnusedProperties;
    }

    /**
     * Original property name: {@code autoSortUnusedAttrs}.
     * <p>
     * Sets whether or not unused attributes are kept sorted in the UI.
     * <p>
     * Applies only when {@code editable=true}.
     *
     * @param autoSortUnusedProperties whether or not unused attributes are kept sorted in the UI
     * @return a reference to this object
     */
    public PivotTableModel setAutoSortUnusedProperties(Boolean autoSortUnusedProperties) {
        this.autoSortUnusedProperties = autoSortUnusedProperties;
        return this;
    }

    /**
     * @return whether or not unused attributes are shown vertically
     */
    public UnusedPropertiesVertical getUnusedPropertiesVertical() {
        return unusedPropertiesVertical;
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
     * Applies only when {@code editable=true}.
     *
     * @param unusedPropertiesVertical whether or not unused attributes are shown vertically
     * @return a reference to this object
     */
    public PivotTableModel setUnusedPropertiesVertical(UnusedPropertiesVertical unusedPropertiesVertical) {
        this.unusedPropertiesVertical = unusedPropertiesVertical;
        return this;
    }

    /**
     * @return a data provider that contains items which will be shown on PivotTable
     */
    public DataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * Sets a data provider for PivotTable. Contains items which will be shown on PivotTable.
     *
     * @param dataProvider a data provider that contains items which will be shown on PivotTable
     * @return a reference to this object
     */
    public PivotTableModel setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        return this;
    }

    /**
     * Adds a data item to the data provider.
     *
     * @param dataItems a data item to add
     * @return a reference to this object
     */
    public PivotTableModel addData(DataItem... dataItems) {
        if (dataItems != null) {
            if (this.dataProvider == null) {
                this.dataProvider = new ListDataProvider();
            }
            this.dataProvider.addItems(Arrays.asList(dataItems));
        }
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setFilterFunction(JsFunction filter) {
        this.filterFunction = filter;
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setSortersFunction(JsFunction sorters) {
        this.sortersFunction = sorters;
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setRendererOptions(RendererOptions rendererOptions) {
        this.rendererOptions = rendererOptions;
        return this;
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
     * @return a reference to this object
     * @see #setExclusions(Map)
     */
    public PivotTableModel setInclusions(Map<String, List<String>> inclusions) {
        this.inclusions = inclusions;
        return this;
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
     * @return a reference to this object
     * @see #setExclusions(String, List)
     */
    public PivotTableModel setInclusions(String property, List<String> inclusions) {
        if (this.inclusions == null) {
            this.inclusions = new HashMap<>();
        }
        this.inclusions.put(property, inclusions);
        return this;
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
     * @return a reference to this object
     * @see #addExclusions(String, String...)
     */
    public PivotTableModel addInclusions(String property, String... inclusions) {
        if (inclusions != null) {
            if (this.inclusions == null) {
                this.inclusions = new HashMap<>();
            }
            if (this.inclusions.containsKey(property)) {
                this.inclusions.get(property).addAll(Arrays.asList(inclusions));
            } else {
                this.inclusions.put(property, new ArrayList<>(Arrays.asList(inclusions)));
            }
        }
        return this;
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
     * @return a reference to this object
     * @see #setInclusions(Map)
     */
    public PivotTableModel setExclusions(Map<String, List<String>> exclusions) {
        this.exclusions = exclusions;
        return this;
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
     * @return a reference to this object
     * @see #setInclusions(Map)
     */
    public PivotTableModel setExclusions(String property, List<String> exclusions) {
        if (this.exclusions == null) {
            this.exclusions = new HashMap<>();
        }
        this.exclusions.put(property, exclusions);
        return this;
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
     * @return a reference to this object
     * @see #setInclusions(Map)
     */
    public PivotTableModel addExclusions(String property, String... exclusions) {
        if (exclusions != null) {
            if (this.exclusions == null) {
                this.exclusions = new HashMap<>();
            }
            if (this.exclusions.containsKey(property)) {
                this.exclusions.get(property).addAll(Arrays.asList(exclusions));
            } else {
                this.exclusions.put(property, new ArrayList<>(Arrays.asList(exclusions)));
            }
        }
        return this;
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
     * @return a reference to this object
     */
    public PivotTableModel setDerivedProperties(DerivedProperties derivedProperties) {
        this.derivedProperties = derivedProperties;
        return this;
    }

    /**
     * @return a locale code
     */
    public String getLocaleCode() {
        return localeCode;
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
    }

    /**
     * @return whether or not UI is shown
     */
    public Boolean isShowUI() {
        return showUI;
    }

    /**
     * Shows or hides row totals. {@code true} by default.
     *
     * @param rowTotals row totals option
     */
    public void setShowRowTotals(Boolean rowTotals) {
        this.rowTotals = rowTotals;
    }

    /**
     * @return whether or not row totals is shown
     */
    public Boolean isShowRowTotals() {
        return rowTotals;
    }

    /**
     * Shows or hides col totals. {@code true} by default.
     *
     * @param colTotals col total options
     */
    public void setShowColTotals(Boolean colTotals) {
        this.colTotals = colTotals;
    }

    /**
     * @return whether or not col totals is shown
     */
    public Boolean isShowColTotals() {
        return colTotals;
    }

    /**
     * Sets a locale code
     *
     * @param localeCode a locale code
     * @return a reference to this object
     */
    public PivotTableModel setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
        return this;
    }

    public List<String> getWiredFields() {
        List<String> fields = new ArrayList<>();
        if (MapUtils.isNotEmpty(getProperties())) {
            fields.addAll(properties.keySet());
        }
        return fields;
    }
}
