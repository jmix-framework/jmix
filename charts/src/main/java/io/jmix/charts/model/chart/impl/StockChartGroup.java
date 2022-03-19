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

package io.jmix.charts.model.chart.impl;

import com.google.gson.annotations.Expose;
import io.jmix.charts.model.*;
import io.jmix.charts.model.axis.CategoryAxesSettings;
import io.jmix.charts.model.axis.ValueAxesSettings;
import io.jmix.charts.model.balloon.Balloon;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.chart.StockChartModel;
import io.jmix.charts.model.dataset.DataSet;
import io.jmix.charts.model.dataset.DataSetSelector;
import io.jmix.charts.model.export.Export;
import io.jmix.charts.model.period.PeriodSelector;
import io.jmix.charts.model.settings.*;
import io.jmix.charts.model.stock.StockEventsSettings;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * See documentation for properties of AmStockChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/AmStockChart">http://docs.amcharts.com/3/javascriptstockchart/AmStockChart</a>
 */
public class StockChartGroup extends ChartModelImpl
        implements StockChartModel<StockChartGroup>, HasColors<StockChartGroup>, DataSet.DataProviderChangeListener {

    private static final long serialVersionUID = -8514686195948609709L;

    @Expose(serialize = false, deserialize = false)
    private List<DataSetDataProviderChangeListener> dataSetDataProviderChangeListeners;
    @Expose(serialize = false, deserialize = false)
    private List<DataSetsChangeListener> dataSetsChangeListeners;

    private Boolean addClassNames;

    private Export export;

    private Boolean animationPlayed;

    private Boolean autoResize;

    private Balloon balloon;

    private CategoryAxesSettings categoryAxesSettings;

    private ChartCursorSettings chartCursorSettings;

    private ChartScrollbarSettings chartScrollbarSettings;

    private String classNamePrefix;

    private List<Color> colors;

    private List<String> comparedDataSets;

    private String dataDateFormat;

    private List<DataSet> dataSets;

    private DataSetSelector dataSetSelector;

    private Boolean extendToFullPeriod;

    private Integer firstDayOfWeek;

    private Boolean glueToTheEnd;

    private String language;

    private LegendSettings legendSettings;

    private String mainDataSet;

    private Boolean mouseWheelScrollEnabled;

    private List<StockPanel> panels;

    private PanelsSettings panelsSettings;

    private String path;

    private String pathToImages;

    private PeriodSelector periodSelector;

    private Integer processTimeout;

    private StockEventsSettings stockEventsSettings;

    private ChartTheme theme;

    private ChartType type = ChartType.STOCK;

    private ValueAxesSettings valueAxesSettings;

    private Boolean zoomOutOnDataSetChange;

    @Expose(serialize = false, deserialize = false)
    private List<String> additionalFields;

    @Override
    public Boolean getAddClassNames() {
        return addClassNames;
    }

    @Override
    public StockChartGroup setAddClassNames(Boolean addClassNames) {
        this.addClassNames = addClassNames;
        return this;
    }

    @Override
    public Export getExport() {
        return export;
    }

    @Override
    public StockChartGroup setExport(Export export) {
        this.export = export;
        return this;
    }

    @Override
    public Boolean getAnimationPlayed() {
        return animationPlayed;
    }

    @Override
    public StockChartGroup setAnimationPlayed(Boolean animationPlayed) {
        this.animationPlayed = animationPlayed;
        return this;
    }

    @Override
    public Boolean getAutoResize() {
        return autoResize;
    }

    @Override
    public StockChartGroup setAutoResize(Boolean autoResize) {
        this.autoResize = autoResize;
        return this;
    }

    @Override
    public Balloon getBalloon() {
        return balloon;
    }

    @Override
    public StockChartGroup setBalloon(Balloon balloon) {
        this.balloon = balloon;
        return this;
    }

    @Override
    public CategoryAxesSettings getCategoryAxesSettings() {
        return categoryAxesSettings;
    }

    @Override
    public StockChartGroup setCategoryAxesSettings(CategoryAxesSettings categoryAxesSettings) {
        this.categoryAxesSettings = categoryAxesSettings;
        return this;
    }

    @Override
    public ChartCursorSettings getChartCursorSettings() {
        return chartCursorSettings;
    }

    @Override
    public StockChartGroup setChartCursorSettings(ChartCursorSettings chartCursorSettings) {
        this.chartCursorSettings = chartCursorSettings;
        return this;
    }

    @Override
    public ChartScrollbarSettings getChartScrollbarSettings() {
        return chartScrollbarSettings;
    }

    @Override
    public StockChartGroup setChartScrollbarSettings(ChartScrollbarSettings chartScrollbarSettings) {
        this.chartScrollbarSettings = chartScrollbarSettings;
        return this;
    }

    @Override
    public String getClassNamePrefix() {
        return classNamePrefix;
    }

    @Override
    public StockChartGroup setClassNamePrefix(String classNamePrefix) {
        this.classNamePrefix = classNamePrefix;
        return this;
    }

    @Override
    public List<Color> getColors() {
        return colors;
    }

    @Override
    public StockChartGroup setColors(List<Color> colors) {
        this.colors = colors;
        return this;
    }

    @Override
    public StockChartGroup addColors(Color... colors) {
        if (colors != null) {
            if (this.colors == null) {
                this.colors = new ArrayList<>();
            }
            this.colors.addAll(Arrays.asList(colors));
        }
        return this;
    }

    @Override
    public List<String> getComparedDataSets() {
        return comparedDataSets;
    }

    @Override
    public StockChartGroup setComparedDataSets(List<String> comparedDataSets) {
        this.comparedDataSets = comparedDataSets;
        return this;
    }

    @Override
    public StockChartGroup addComparedDataSets(String... comparedDataSets) {
        if (comparedDataSets != null) {
            if (this.comparedDataSets == null) {
                this.comparedDataSets = new ArrayList<>();
            }
            this.comparedDataSets.addAll(Arrays.asList(comparedDataSets));
        }
        return this;
    }

    @Override
    public String getDataDateFormat() {
        return dataDateFormat;
    }

    @Override
    public StockChartGroup setDataDateFormat(String dataDateFormat) {
        this.dataDateFormat = dataDateFormat;
        return this;
    }

    @Override
    public List<DataSet> getDataSets() {
        return dataSets;
    }

    @Override
    public StockChartGroup setDataSets(List<DataSet> dataSets) {
        if (CollectionUtils.isNotEmpty(this.dataSets)) {
            for (DataSet dataSet : this.dataSets) {
                dataSet.removeDataProviderChangeListener(this);
            }
        }

        if (CollectionUtils.isNotEmpty(dataSets)) {
            for (DataSet dataSet : dataSets) {
                dataSet.addDataProviderChangeListener(this);
            }
        }

        this.dataSets = dataSets;
        fireDataSetsChanged(dataSets, Operation.SET);
        return this;
    }

    @Override
    public StockChartGroup addDataSets(DataSet... dataSets) {
        if (dataSets != null) {
            if (this.dataSets == null) {
                this.dataSets = new ArrayList<>();
            }
            List<DataSet> dataSetList = Arrays.asList(dataSets);
            for (DataSet dataSet : dataSetList) {
                dataSet.addDataProviderChangeListener(this);
            }
            this.dataSets.addAll(Arrays.asList(dataSets));
            fireDataSetsChanged(dataSetList, Operation.ADD);
        }
        return this;
    }

    protected void fireDataSetsChanged(List<DataSet> dataSets, Operation operation) {
        if (CollectionUtils.isNotEmpty(dataSetsChangeListeners)) {
            DataSetsChangeEvent event = new DataSetsChangeEvent(
                    dataSets != null ? new ArrayList<>(dataSets) : Collections.emptyList(),
                    operation);
            for (DataSetsChangeListener listener : new ArrayList<>(dataSetsChangeListeners)) {
                listener.dataSetsChanged(event);
            }
        }
    }

    @Override
    public DataSetSelector getDataSetSelector() {
        return dataSetSelector;
    }

    @Override
    public StockChartGroup setDataSetSelector(DataSetSelector dataSetSelector) {
        this.dataSetSelector = dataSetSelector;
        return this;
    }

    @Override
    public Boolean getExtendToFullPeriod() {
        return extendToFullPeriod;
    }

    @Override
    public StockChartGroup setExtendToFullPeriod(Boolean extendToFullPeriod) {
        this.extendToFullPeriod = extendToFullPeriod;
        return this;
    }

    @Override
    public Integer getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    @Override
    public StockChartGroup setFirstDayOfWeek(Integer firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
        return this;
    }

    @Override
    public Boolean getGlueToTheEnd() {
        return glueToTheEnd;
    }

    @Override
    public StockChartGroup setGlueToTheEnd(Boolean glueToTheEnd) {
        this.glueToTheEnd = glueToTheEnd;
        return this;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public StockChartGroup setLanguage(String language) {
        this.language = language;
        return this;
    }

    @Override
    public LegendSettings getLegendSettings() {
        return legendSettings;
    }

    @Override
    public StockChartGroup setLegendSettings(LegendSettings legendSettings) {
        this.legendSettings = legendSettings;
        return this;
    }

    @Override
    public String getMainDataSet() {
        return mainDataSet;
    }

    @Override
    public StockChartGroup setMainDataSet(String mainDataSet) {
        this.mainDataSet = mainDataSet;
        return this;
    }

    @Override
    public Boolean getMouseWheelScrollEnabled() {
        return mouseWheelScrollEnabled;
    }

    @Override
    public StockChartGroup setMouseWheelScrollEnabled(Boolean mouseWheelScrollEnabled) {
        this.mouseWheelScrollEnabled = mouseWheelScrollEnabled;
        return this;
    }

    @Override
    public List<StockPanel> getPanels() {
        return panels;
    }

    @Override
    public StockChartGroup setPanels(List<StockPanel> panels) {
        this.panels = panels;
        return this;
    }

    @Override
    public StockChartGroup addPanels(StockPanel... panels) {
        if (panels != null) {
            if (this.panels == null) {
                this.panels = new ArrayList<>();
            }
            this.panels.addAll(Arrays.asList(panels));
        }
        return this;
    }

    @Override
    public PanelsSettings getPanelsSettings() {
        return panelsSettings;
    }

    @Override
    public StockChartGroup setPanelsSettings(PanelsSettings panelsSettings) {
        this.panelsSettings = panelsSettings;
        return this;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public StockChartGroup setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public String getPathToImages() {
        return pathToImages;
    }

    @Override
    public StockChartGroup setPathToImages(String pathToImages) {
        this.pathToImages = pathToImages;
        return this;
    }

    @Override
    public PeriodSelector getPeriodSelector() {
        return periodSelector;
    }

    @Override
    public StockChartGroup setPeriodSelector(PeriodSelector periodSelector) {
        this.periodSelector = periodSelector;
        return this;
    }

    @Override
    public StockEventsSettings getStockEventsSettings() {
        return stockEventsSettings;
    }

    @Override
    public StockChartGroup setStockEventsSettings(StockEventsSettings stockEventsSettings) {
        this.stockEventsSettings = stockEventsSettings;
        return this;
    }

    @Override
    public ChartTheme getTheme() {
        return theme;
    }

    @Override
    public StockChartGroup setTheme(ChartTheme theme) {
        this.theme = theme;
        return this;
    }

    @Override
    public ValueAxesSettings getValueAxesSettings() {
        return valueAxesSettings;
    }

    @Override
    public StockChartGroup setValueAxesSettings(ValueAxesSettings valueAxesSettings) {
        this.valueAxesSettings = valueAxesSettings;
        return this;
    }

    @Override
    public Boolean getZoomOutOnDataSetChange() {
        return zoomOutOnDataSetChange;
    }

    @Override
    public StockChartGroup setZoomOutOnDataSetChange(Boolean zoomOutOnDataSetChange) {
        this.zoomOutOnDataSetChange = zoomOutOnDataSetChange;
        return this;
    }

    @Override
    public DataSet getDataSet(String id) {
        for (DataSet dataSet : dataSets) {
            if (id.equals(dataSet.getId())) {
                return dataSet;
            }
        }
        return null;
    }

    public ChartType getType() {
        return type;
    }

    @Override
    public List<String> getAdditionalFields() {
        return additionalFields;
    }

    @Override
    public StockChartGroup setAdditionalFields(List<String> additionalFields) {
        this.additionalFields = additionalFields;
        return this;
    }

    @Override
    public Integer getProcessTimeout() {
        return processTimeout;
    }

    @Override
    public StockChartGroup setProcessTimeout(Integer processTimeout) {
        this.processTimeout = processTimeout;
        return this;
    }

    @Override
    public List<String> getWiredFields() {
        List<String> wiredFields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(getAdditionalFields())) {
            wiredFields.addAll(getAdditionalFields());
        }
        if (CollectionUtils.isNotEmpty(panels)) {
            for (StockPanel panel : panels) {
                wiredFields.addAll(panel.getWiredFields());
            }
        }
        if (CollectionUtils.isNotEmpty(dataSets)) {
            for (DataSet dataSet : dataSets) {
                if (dataSet.getDataProvider() != null) {
                    wiredFields.addAll(dataSet.getWiredFields());
                }
            }
        }
        return wiredFields;
    }

    @Override
    public void onChange(DataSet.DataProviderChangeEvent event) {
        if (CollectionUtils.isNotEmpty(dataSetDataProviderChangeListeners)) {
            DataSetDataProviderChangeEvent e = new DataSetDataProviderChangeEvent(event.getDataSet());
            for (DataSetDataProviderChangeListener listener : new ArrayList<>(dataSetDataProviderChangeListeners)) {
                listener.onChange(e);
            }
        }
    }

    public void addDataSetDataProviderChangeListener(DataSetDataProviderChangeListener listener) {
        if (dataSetDataProviderChangeListeners == null) {
            dataSetDataProviderChangeListeners = new ArrayList<>();
        }
        dataSetDataProviderChangeListeners.add(listener);
    }

    public void removeDataSetDataProviderChangeListener(DataSetDataProviderChangeListener listener) {
        if (dataSetDataProviderChangeListeners != null) {
            dataSetDataProviderChangeListeners.remove(listener);
        }
    }

    public void addDataSetsChangeListener(DataSetsChangeListener listener) {
        if (dataSetsChangeListeners == null) {
            dataSetsChangeListeners = new ArrayList<>();
        }
        dataSetsChangeListeners.add(listener);
    }

    public void removeDataSetsChangeListener(DataSetsChangeListener listener) {
        if (dataSetsChangeListeners != null) {
            dataSetsChangeListeners.remove(listener);
        }
    }

    public interface DataSetDataProviderChangeListener {
        void onChange(DataSetDataProviderChangeEvent event);
    }

    public static class DataSetDataProviderChangeEvent {
        private final DataSet dataSet;

        public DataSetDataProviderChangeEvent(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public DataSet getDataSet() {
            return dataSet;
        }
    }

    public interface DataSetsChangeListener {
        void dataSetsChanged(DataSetsChangeEvent event);
    }

    public enum Operation {
        ADD,
        REMOVE,
        SET
    }

    public static class DataSetsChangeEvent {
        private final List<DataSet> dataSets;
        private final Operation operation;

        public DataSetsChangeEvent(List<DataSet> dataSets, Operation operation) {
            this.dataSets = dataSets;
            this.operation = operation;
        }

        public Operation getOperation() {
            return operation;
        }

        public List<DataSet> getDataSets() {
            return dataSets;
        }
    }
}