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

package io.jmix.charts.model.dataset;

import com.google.gson.annotations.Expose;
import io.jmix.ui.data.DataProvider;
import io.jmix.charts.model.*;
import io.jmix.charts.model.chart.impl.StockChartGroup;
import io.jmix.charts.model.stock.StockEvent;
import io.jmix.charts.model.stock.StockGraph;
import io.jmix.ui.meta.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Holds all information about data.
 * <br>
 * See documentation for properties of DataSet JS object.
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/DataSet">http://docs.amcharts.com/3/javascriptstockchart/DataSet</a>
 */
@StudioElement(
        caption = "DataSet",
        xmlElement = "dataSet",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
@StudioProperties(groups = {
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "categoryField"})
})
public class DataSet extends AbstractChartObject {

    private static final long serialVersionUID = -5376050190482065219L;

    @Expose(serialize = false, deserialize = false)
    private List<DataProviderChangeListener> dataProviderChangeListeners;

    private String id;

    private String categoryField;

    private Color color;

    private Boolean compared;

    private DataProvider dataProvider;

    private List<FieldMapping> fieldMappings;

    private Boolean showInCompare;

    private Boolean showInSelect;

    private List<StockEvent> stockEvents;

    private String title;

    public DataSet() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    @StudioProperty
    public DataSet setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return category field name
     */
    public String getCategoryField() {
        return categoryField;
    }

    /**
     * Sets category field name in your data provider. It needs to contains a date/time value. If you are specifying
     * dates as strings in your data, i.e. "2015-01-05", it is strongly recommend setting dataDateFormat as well.
     *
     * @param categoryField category field name
     * @return data set
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public DataSet setCategoryField(String categoryField) {
        this.categoryField = categoryField;
        return this;
    }

    /**
     * @return color of the data set
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets color of the data set. One of colors from {@link StockChartGroup#colors colors} array will be used if not
     * set.
     *
     * @param color color
     * @return data set
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public DataSet setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return true if data set is selected for comparing
     */
    public Boolean getCompared() {
        return compared;
    }

    /**
     * Set to true if this data set selected for comparing. If not set the default value is false.
     *
     * @param compared compared option
     * @return data set
     */
    @StudioProperty(defaultValue = "false")
    public DataSet setCompared(Boolean compared) {
        this.compared = compared;
        return this;
    }

    /**
     * @return data provider
     */
    public DataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * Sets data provider to the data set. An array of data points to be used as data. Important, the data points
     * needs to come pre-ordered in ascending order. Data with incorrect order might result in visual and functional
     * glitches on the chart.
     *
     * @param dataProvider data provider
     * @return data set
     */
    @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF)
    public DataSet setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        fireDataProviderChanged();
        return this;
    }

    protected void fireDataProviderChanged() {
        if (CollectionUtils.isNotEmpty(dataProviderChangeListeners)) {
            DataProviderChangeEvent event = new DataProviderChangeEvent(this);
            for (DataProviderChangeListener listener : new ArrayList<>(dataProviderChangeListeners)) {
                listener.onChange(event);
            }
        }
    }

    /**
     * @return list of field mappings
     */
    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    /**
     * Sets list of field mappings. Field mapping is an object with fromField and toField properties. fromField is a
     * name of your value field in data provider. toField might be chosen freely, it will be used to set
     * value/open/close/high/low fields for the {@link StockGraph}.
     *
     * @param fieldMappings list of field mappings
     * @return data set
     */
    @StudioElementsGroup(caption = "Field Mappings", xmlElement = "fieldMappings")
    public DataSet setFieldMappings(List<FieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings;
        return this;
    }

    /**
     * Adds field mappings.
     *
     * @param fieldMappings field mappings
     * @return data set
     */
    public DataSet addFieldMappings(FieldMapping... fieldMappings) {
        if (fieldMappings != null) {
            if (this.fieldMappings == null) {
                this.fieldMappings = new ArrayList<>();
            }
            this.fieldMappings.addAll(Arrays.asList(fieldMappings));
        }
        return this;
    }

    /**
     * @return true if this data set is visible in "compare to" list
     */
    public Boolean getShowInCompare() {
        return showInCompare;
    }

    /**
     * Set showInCompare to false if this data set shouldn't be visible in "compare to" list. If not set the default
     * value is true.
     *
     * @param showInCompare showInCompare option
     * @return data set
     */
    @StudioProperty(defaultValue = "true")
    public DataSet setShowInCompare(Boolean showInCompare) {
        this.showInCompare = showInCompare;
        return this;
    }

    /**
     * @return true if this data set is visible in "select" dropdown
     */
    public Boolean getShowInSelect() {
        return showInSelect;
    }

    /**
     * Set showInSelect to false if this data set shouldn't be visible in "select" dropdown. If not set the default
     * value is true.
     *
     * @param showInSelect showInSelect option
     * @return data set
     */
    @StudioProperty(defaultValue = "true")
    public DataSet setShowInSelect(Boolean showInSelect) {
        this.showInSelect = showInSelect;
        return this;
    }

    /**
     * @return list of stock events
     */
    public List<StockEvent> getStockEvents() {
        return stockEvents;
    }

    /**
     * Sets list of stock events.
     *
     * @param stockEvents list of stock events
     * @return data set
     */
    @StudioElementsGroup(caption = "Stock Events", xmlElement = "stockEvents")
    public DataSet setStockEvents(List<StockEvent> stockEvents) {
        this.stockEvents = stockEvents;
        return this;
    }

    /**
     * Adds stock events.
     *
     * @param stockEvents stock events
     * @return data set
     */
    public DataSet addStockEvents(StockEvent... stockEvents) {
        if (stockEvents != null) {
            if (this.stockEvents == null) {
                this.stockEvents = new ArrayList<>();
            }
            this.stockEvents.addAll(Arrays.asList(stockEvents));
        }
        return this;
    }

    /**
     * @return data set title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets data set title.
     *
     * @param title title
     * @return data set
     */
    @StudioProperty
    public DataSet setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * @return fields that are bound to the data set
     */
    public List<String> getWiredFields() {
        List<String> wiredFields = new ArrayList<>();
        if (StringUtils.isNotEmpty(categoryField)) {
            wiredFields.add(categoryField);
        }
        if (fieldMappings != null) {
            for (FieldMapping mapping : fieldMappings) {
                if (StringUtils.isNotEmpty(mapping.getFromField())) {
                    wiredFields.add(mapping.getFromField());
                }

                if (StringUtils.isNotEmpty(mapping.getToField())) {
                    wiredFields.add(mapping.getToField());
                }
            }
        }

        return wiredFields;
    }

    public void addDataProviderChangeListener(DataProviderChangeListener listener) {
        if (dataProviderChangeListeners == null) {
            dataProviderChangeListeners = new ArrayList<>();
        }
        dataProviderChangeListeners.add(listener);
    }

    public void removeDataProviderChangeListener(DataProviderChangeListener listener) {
        if (dataProviderChangeListeners != null) {
            dataProviderChangeListeners.remove(listener);
        }
    }

    public interface DataProviderChangeListener {
        void onChange(DataProviderChangeEvent event);
    }

    public static class DataProviderChangeEvent {
        private final DataSet dataSet;

        public DataProviderChangeEvent(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public DataSet getDataSet() {
            return dataSet;
        }
    }
}
