/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.shared.Registration;
import io.jmix.chartsflowui.kit.component.model.series.GaugeSeries;
import io.jmix.chartsflowui.kit.data.chart.ChartItems;
import io.jmix.chartsflowui.kit.data.chart.DataItem;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.jmix.chartsflowui.kit.component.ChartUpdateUtil.requestIncrementalUpdateChartDataSet;
import static io.jmix.chartsflowui.kit.component.ChartUpdateUtil.requestUpdateChartDataSet;

/**
 * DataSet component is the recommended way to provide data to a chart. All series added to the chart
 * will use the data set. The only series that requires its own data set is the {@link GaugeSeries}.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#dataset">DataSet documentation</a>
 * @see <a href="https://echarts.apache.org/handbook/en/concepts/dataset/">DataSet concept tutorial</a>
 */
public class DataSet extends ChartDataObservableObject {

    protected String id;

    protected Source<?> source;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public Source<?> getSource() {
        return source;
    }

    public void setSource(Source<?> source) {
        if (this.source == source) {
            return;
        }

        if (this.source != null) {
            removeChild(this.source);
        }

        this.source = source;

        if (source != null) {
            if (chart != null) {
                source.setChart(chart);
            }

            addChild(source);
        } else {
            markAsDirty();
        }
    }

    public DataSet withSource(Source<?> source) {
        setSource(source);
        return this;
    }

    @Override
    protected void afterChartSetup() {
        if (source != null) {
            source.setChart(chart);
        }
    }

    /**
     * Source data. May be associated with a data container or another provided data provider.
     * Serialized only in row based key-value format (object array), where the keys indicate category field.<br/>
     * Serialization example:
     * <pre>{@code
     * [
     *     // {category field}, {value field #1}, {value field #2}
     *     {"product": "Matcha Latte", "count": 823, "score": 95.8},
     *     {"product": "Milk Tea", "count": 235, "score": 81.4},
     *     {"product": "Cheese Cocoa", "count": 1042, "score": 91.2},
     *     {"product": "Walnut Brownie", "count": 988, "score": 76.9}
     * ]
     * }</pre>
     *
     * @param <T> data item class type
     * @see ChartItems
     */
    public static class Source<T extends DataItem> extends ChartDataObservableObject {

        protected DataProvider<T, ?> dataProvider;
        protected Registration dataProviderItemSetChangeRegistration;

        protected String categoryField;
        protected List<String> valueFields;

        public DataProvider<T, ?> getDataProvider() {
            return dataProvider;
        }

        public void setDataProvider(DataProvider<T, ?> dataProvider) {
            if (Objects.equals(dataProvider, this.dataProvider)) {
                return;
            }

            this.dataProvider = dataProvider;
            onDataProviderChange();
        }

        @SuppressWarnings("unchecked")
        protected void onDataProviderChange() {
            requestUpdateChartDataSet(chart);

            if (dataProviderItemSetChangeRegistration != null) {
                dataProviderItemSetChangeRegistration.remove();
                dataProviderItemSetChangeRegistration = null;
            }

            if (dataProvider instanceof ChartItems) {
                ChartItems<T> chartItems = (ChartItems<T>) dataProvider;
                dataProviderItemSetChangeRegistration =
                        chartItems.addItemSetChangeListener(this::onItemSetChangeListener);
            }
        }

        protected void onItemSetChangeListener(ChartItems.ItemSetChangeEvent<T> event) {
            requestIncrementalUpdateChartDataSet(chart, event);
        }

        public String getCategoryField() {
            return categoryField;
        }

        public void setCategoryField(String categoryField) {
            this.categoryField = categoryField;
            markAsDirty();
        }

        public List<String> getValueFields() {
            return valueFields;
        }

        public void setValueFields(String... valueFields) {
            this.valueFields = valueFields == null ? null : List.of(valueFields);
            markAsDirty();
        }

        public void addValueFields(String... valueFields) {
            for (String valueField : valueFields) {
                addValueField(valueField);
            }
        }

        public void addValueField(String field) {
            if (this.valueFields == null) {
                this.valueFields = new ArrayList<>();
            }

            if (this.valueFields.contains(field)) {
                return;
            }

            valueFields.add(field);
            markAsDirty();
        }

        public Source<T> withDataProvider(DataProvider<T, ?> dataProvider) {
            setDataProvider(dataProvider);
            return this;
        }

        public Source<T> withCategoryField(String categoryField) {
            setCategoryField(categoryField);
            return this;
        }

        public Source<T> withValueFields(String... fields) {
            setValueFields(fields);
            return this;
        }

        public Source<T> withValueField(String field) {
            addValueField(field);
            return this;
        }

        @Override
        protected void afterChartSetup() {
            if (dataProvider != null) {
                requestUpdateChartDataSet(chart);
            }
        }
    }
}
