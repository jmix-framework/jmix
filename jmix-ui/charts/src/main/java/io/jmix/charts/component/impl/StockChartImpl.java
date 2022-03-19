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

package io.jmix.charts.component.impl;

import com.google.common.base.Strings;
import io.jmix.core.LocaleResolver;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.impl.AbstractComponent;
import io.jmix.charts.ChartLocaleHelper;
import io.jmix.charts.component.StockChart;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.axis.CategoryAxesSettings;
import io.jmix.charts.model.axis.ValueAxesSettings;
import io.jmix.charts.model.balloon.Balloon;
import io.jmix.charts.model.chart.impl.StockChartGroup;
import io.jmix.charts.model.chart.impl.StockPanel;
import io.jmix.charts.model.dataset.DataSet;
import io.jmix.charts.model.dataset.DataSetSelector;
import io.jmix.charts.model.date.DatePeriod;
import io.jmix.charts.model.export.Export;
import io.jmix.charts.model.period.PeriodSelector;
import io.jmix.charts.model.period.PeriodType;
import io.jmix.charts.model.settings.*;
import io.jmix.charts.model.stock.StockEvent;
import io.jmix.charts.model.stock.StockEventsSettings;
import io.jmix.charts.model.stock.StockGraph;
import io.jmix.charts.serialization.JmixStockChartSerializer;
import io.jmix.charts.widget.amcharts.JmixAmStockChartScene;
import io.jmix.charts.widget.amcharts.JmixAmchartsIntegration;
import io.jmix.charts.widget.amcharts.events.dataset.listener.DataSetSelectorCompareListener;
import io.jmix.charts.widget.amcharts.events.dataset.listener.DataSetSelectorSelectListener;
import io.jmix.charts.widget.amcharts.events.dataset.listener.DataSetSelectorUnCompareListener;
import io.jmix.charts.widget.amcharts.events.period.PeriodSelectorChangeListener;
import io.jmix.charts.widget.amcharts.events.stock.listener.*;
import io.jmix.charts.widget.amcharts.serialization.ChartJsonSerializationContext;
import io.jmix.charts.widget.amcharts.serialization.StockChartSerializer;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Consumer;

public class StockChartImpl extends AbstractComponent<JmixAmStockChartScene>
        implements StockChart, InitializingBean {

    protected Messages messages;
    protected MessageTools messageTools;
    protected Metadata metadata;
    protected CurrentAuthentication currentAuthentication;
    protected ChartLocaleHelper chartLocaleHelper;
    protected FormatStringsRegistry formatStringsRegistry;

    protected StockChartEventsForwarder stockChartEventsForwarder = new StockChartEventsForwarder();

    public StockChartImpl() {
        component = createComponent();
    }

    protected StockChartGroup createConfiguration() {
        return new StockChartGroup();
    }

    protected JmixAmStockChartScene createComponent() {
        return new JmixAmStockChartScene();
    }

    protected StockChartSerializer createChartSerializer() {
        return applicationContext.getBean(JmixStockChartSerializer.class);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setChartLocaleHelper(ChartLocaleHelper chartLocaleHelper) {
        this.chartLocaleHelper = chartLocaleHelper;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setFormatStringsRegistry(FormatStringsRegistry formatStringsRegistry) {
        this.formatStringsRegistry = formatStringsRegistry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        component.setChartSerializer(createChartSerializer());
        initLocale();

        StockChartGroup configuration = createConfiguration();
        setupDefaults(configuration);

        component.drawChart(configuration);
    }

    protected void initLocale() {
        JmixAmchartsIntegration amchartsIntegration = JmixAmchartsIntegration.get();
        if (amchartsIntegration.getSettings() == null
                || !Objects.equals(currentAuthentication.getLocale(), amchartsIntegration.getLocale())) {

            Settings settings = new Settings();
            Locale locale = currentAuthentication.getLocale();

            // chart
            String localeString = LocaleResolver.localeToString(locale);
            amchartsIntegration.setChartMessages(localeString, chartLocaleHelper.getChartLocaleMap(locale));

            // export
            amchartsIntegration.setExportMessages(localeString, chartLocaleHelper.getExportLocaleMap(locale));

            amchartsIntegration.setSettings(settings);
            amchartsIntegration.setLocale(currentAuthentication.getLocale());
        }
    }

    protected void setupDefaults(StockChartGroup chart) {
        chart.setLanguage(LocaleResolver.localeToString(currentAuthentication.getLocale()));

        chart.setDataDateFormat(ChartJsonSerializationContext.DEFAULT_JS_DATE_FORMAT);
    }

    protected void setupFormatStrings(List<StockPanel> panels) {
        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale());
        if (formatStrings != null) {
            for (StockPanel panel : panels) {
                DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();

                if (panel.getPrecision() == null) {
                    panel.setPrecision(-1);
                }

                if (panel.getPercentPrecision() == null) {
                    panel.setPercentPrecision(2);
                }

                if (panel.getDecimalSeparator() == null) {
                    panel.setDecimalSeparator(Character.toString(formatSymbols.getDecimalSeparator()));
                }

                if (panel.getThousandsSeparator() == null) {
                    panel.setThousandsSeparator(Character.toString(formatSymbols.getGroupingSeparator()));
                }
            }
        }
    }

    public StockChartGroup getConfiguration() {
        return component.getChart();
    }

    protected StockChartGroup getModel() {
        return component.getChart();
    }

    @Override
    public void repaint() {
        component.drawChart();
    }

    protected StockEvent getStockEvent(String stockEventId) {
        UUID eventId = UUID.fromString(stockEventId);
        for (DataSet dataSet : component.getChart().getDataSets()) {
            for (StockEvent stockEvent : dataSet.getStockEvents()) {
                if (stockEvent.getId().equals(eventId)) {
                    return stockEvent;
                }
            }
        }
        return null;
    }

    @Override
    public Subscription addClickListener(Consumer<StockChartClickEvent> clickListener) {
        if (!hasSubscriptions(StockChartClickEvent.class)) {
            component.addChartClickListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockChartClickEvent.class, clickListener);
    }

    @Override
    public Subscription addRightClickListener(Consumer<StockChartRightClickEvent> clickListener) {
        if (!hasSubscriptions(StockChartRightClickEvent.class)) {
            component.addChartRightClickListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockChartRightClickEvent.class, clickListener);
    }


    @Override
    public Subscription addStockEventClickListener(Consumer<StockEventClickEvent> clickListener) {
        if (!hasSubscriptions(StockEventClickEvent.class)) {
            component.addStockEventClickListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockEventClickEvent.class, clickListener);
    }

    @Override
    public Subscription addStockEventRollOutListener(Consumer<StockEventRollOutEvent> rollOutListener) {
        if (!hasSubscriptions(StockEventRollOutEvent.class)) {
            component.addStockEventRollOutListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockEventRollOutEvent.class, rollOutListener);
    }


    @Override
    public Subscription addStockEventRollOverListener(Consumer<StockEventRollOverEvent> rollOverListener) {
        if (!hasSubscriptions(StockEventRollOverEvent.class)) {
            component.addStockEventRollOverListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockEventRollOverEvent.class, rollOverListener);
    }

    @Override
    public Subscription addZoomListener(Consumer<ZoomEvent> zoomListener) {
        if (!hasSubscriptions(ZoomEvent.class)) {
            component.addStockPanelZoomListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(ZoomEvent.class, zoomListener);
    }

    @Override
    public Subscription addPeriodSelectorChangeListener(Consumer<PeriodSelectorChangeEvent> changeListener) {
        if (!hasSubscriptions(PeriodSelectorChangeEvent.class)) {
            component.addPeriodSelectorChangeListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(PeriodSelectorChangeEvent.class, changeListener);
    }

    @Override
    public Subscription addDataSetSelectorCompareListener(Consumer<DataSetSelectorCompareEvent> compareListener) {
        if (!hasSubscriptions(DataSetSelectorCompareEvent.class)) {
            component.addDataSetSelectorCompareListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(DataSetSelectorCompareEvent.class, compareListener);
    }

    @Override
    public Subscription addDataSetSelectorSelectListener(Consumer<DataSetSelectorSelectEvent> selectListener) {
        if (!hasSubscriptions(DataSetSelectorSelectEvent.class)) {
            component.addDataSetSelectorSelectListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(DataSetSelectorSelectEvent.class, selectListener);
    }


    @Override
    public Subscription addDataSetSelectorUnCompareListener(Consumer<DataSetSelectorUnCompareEvent> unCompareListener) {
        if (!hasSubscriptions(DataSetSelectorUnCompareEvent.class)) {
            component.addDataSetSelectorUnCompareListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(DataSetSelectorUnCompareEvent.class, unCompareListener);
    }


    @Override
    public Subscription addStockGraphClickListener(Consumer<StockGraphClickEvent> clickListener) {
        if (!hasSubscriptions(StockGraphClickEvent.class)) {
            component.addStockGraphClickListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockGraphClickEvent.class, clickListener);
    }


    @Override
    public Subscription addStockGraphRollOutListener(Consumer<StockGraphRollOutEvent> rollOutListener) {
        if (!hasSubscriptions(StockGraphRollOutEvent.class)) {
            component.addStockGraphRollOutListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockGraphRollOutEvent.class, rollOutListener);
    }


    @Override
    public Subscription addStockGraphRollOverListener(Consumer<StockGraphRollOverEvent> rollOverListener) {
        if (!hasSubscriptions(StockGraphRollOverEvent.class)) {
            component.addStockGraphRollOverListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockGraphRollOverEvent.class, rollOverListener);
    }

    @Override
    public Subscription addStockGraphItemClickListener(Consumer<StockGraphItemClickEvent> clickListener) {
        if (!hasSubscriptions(StockGraphItemClickEvent.class)) {
            component.addStockGraphItemClickListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockGraphItemClickEvent.class, clickListener);
    }

    @Override
    public Subscription addStockGraphItemRightClickListener(Consumer<StockGraphItemRightClickEvent> clickListener) {
        if (!hasSubscriptions(StockGraphItemRightClickEvent.class)) {
            component.addStockGraphItemRightClickListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockGraphItemRightClickEvent.class, clickListener);
    }

    @Override
    public Subscription addStockGraphItemRollOutListener(Consumer<StockGraphItemRollOutEvent> rollOutListener) {
        if (!hasSubscriptions(StockGraphItemRollOutEvent.class)) {
            component.addStockGraphItemRollOutListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockGraphItemRollOutEvent.class, rollOutListener);
    }

    @Override
    public Subscription addStockGraphItemRollOverListener(Consumer<StockGraphItemRollOverEvent> rollOverListener) {
        if (!hasSubscriptions(StockGraphItemRollOverEvent.class)) {
            component.addStockGraphItemRollOverListener(stockChartEventsForwarder);
        }
        return getEventHub().subscribe(StockGraphItemRollOverEvent.class, rollOverListener);
    }

    @Override
    public String getNativeJson() {
        return component.getJson();
    }

    @Override
    public void setNativeJson(String json) {
        component.setJson(json);
    }

    @Override
    public Boolean getAddClassNames() {
        return getModel().getAddClassNames();
    }

    @Override
    public StockChart setAddClassNames(Boolean addClassNames) {
        getModel().setAddClassNames(addClassNames);
        return this;
    }

    @Override
    public Export getExport() {
        return getModel().getExport();
    }

    @Override
    public StockChart setExport(Export export) {
        if (export != null && export.getDataDateFormat() == null) {
            export.setDateFormat(messages.getMessage("amcharts.export.dateFormat"));
        }

        getModel().setExport(export);
        return this;
    }

    @Override
    public Boolean getAnimationPlayed() {
        return getModel().getAnimationPlayed();
    }

    @Override
    public StockChart setAnimationPlayed(Boolean animationPlayed) {
        getModel().setAnimationPlayed(animationPlayed);
        return this;
    }

    @Override
    public Boolean getAutoResize() {
        return getModel().getAutoResize();
    }

    @Override
    public StockChart setAutoResize(Boolean autoResize) {
        getModel().setAutoResize(autoResize);
        return this;
    }

    @Override
    public Balloon getBalloon() {
        return getModel().getBalloon();
    }

    @Override
    public StockChart setBalloon(Balloon balloon) {
        getModel().setBalloon(balloon);
        return this;
    }

    @Override
    public CategoryAxesSettings getCategoryAxesSettings() {
        return getModel().getCategoryAxesSettings();
    }

    @Override
    public StockChart setCategoryAxesSettings(CategoryAxesSettings categoryAxesSettings) {
        getModel().setCategoryAxesSettings(categoryAxesSettings);
        return this;
    }

    @Override
    public ChartCursorSettings getChartCursorSettings() {
        return getModel().getChartCursorSettings();
    }

    @Override
    public StockChart setChartCursorSettings(ChartCursorSettings chartCursorSettings) {
        getModel().setChartCursorSettings(chartCursorSettings);
        return this;
    }

    @Override
    public ChartScrollbarSettings getChartScrollbarSettings() {
        return getModel().getChartScrollbarSettings();
    }

    @Override
    public StockChart setChartScrollbarSettings(ChartScrollbarSettings chartScrollbarSettings) {
        getModel().setChartScrollbarSettings(chartScrollbarSettings);
        return this;
    }

    @Override
    public String getClassNamePrefix() {
        return getModel().getClassNamePrefix();
    }

    @Override
    public StockChart setClassNamePrefix(String classNamePrefix) {
        getModel().setClassNamePrefix(classNamePrefix);
        return this;
    }

    @Override
    public List<Color> getColors() {
        return getModel().getColors();
    }

    @Override
    public StockChart setColors(List<Color> colors) {
        getModel().setColors(colors);
        return this;
    }

    @Override
    public StockChart addColors(Color... colors) {
        getModel().addColors(colors);
        return this;
    }

    @Override
    public List<String> getComparedDataSets() {
        return getModel().getComparedDataSets();
    }

    @Override
    public StockChart setComparedDataSets(List<String> comparedDataSets) {
        getModel().setComparedDataSets(comparedDataSets);
        return this;
    }

    @Override
    public StockChart addComparedDataSets(String... comparedDataSets) {
        getModel().addComparedDataSets(comparedDataSets);
        return this;
    }

    @Override
    public String getDataDateFormat() {
        return getModel().getDataDateFormat();
    }

    @Override
    public StockChart setDataDateFormat(String dataDateFormat) {
        getModel().setDataDateFormat(dataDateFormat);
        return this;
    }

    @Override
    public List<DataSet> getDataSets() {
        return getModel().getDataSets();
    }

    @Override
    public StockChart setDataSets(List<DataSet> dataSets) {
        getModel().setDataSets(dataSets);
        return this;
    }

    @Override
    public StockChart addDataSets(DataSet... dataSets) {
        getModel().addDataSets(dataSets);
        return this;
    }

    @Override
    public DataSetSelector getDataSetSelector() {
        return getModel().getDataSetSelector();
    }

    @Override
    public StockChart setDataSetSelector(DataSetSelector dataSetSelector) {
        getModel().setDataSetSelector(dataSetSelector);
        return this;
    }

    @Override
    public Boolean getExtendToFullPeriod() {
        return getModel().getExtendToFullPeriod();
    }

    @Override
    public StockChart setExtendToFullPeriod(Boolean extendToFullPeriod) {
        getModel().setExtendToFullPeriod(extendToFullPeriod);
        return this;
    }

    @Override
    public Integer getFirstDayOfWeek() {
        return getModel().getFirstDayOfWeek();
    }

    @Override
    public StockChart setFirstDayOfWeek(Integer firstDayOfWeek) {
        getModel().setFirstDayOfWeek(firstDayOfWeek);
        return this;
    }

    @Override
    public Boolean getGlueToTheEnd() {
        return getModel().getGlueToTheEnd();
    }

    @Override
    public StockChart setGlueToTheEnd(Boolean glueToTheEnd) {
        getModel().setGlueToTheEnd(glueToTheEnd);
        return this;
    }

    @Override
    public String getLanguage() {
        return getModel().getLanguage();
    }

    @Override
    public StockChart setLanguage(String language) {
        getModel().setLanguage(language);
        return this;
    }

    @Override
    public LegendSettings getLegendSettings() {
        return getModel().getLegendSettings();
    }

    @Override
    public StockChart setLegendSettings(LegendSettings legendSettings) {
        getModel().setLegendSettings(legendSettings);
        return this;
    }

    @Override
    public String getMainDataSet() {
        return getModel().getMainDataSet();
    }

    @Override
    public StockChart setMainDataSet(String mainDataSet) {
        getModel().setMainDataSet(mainDataSet);
        return this;
    }

    @Override
    public Boolean getMouseWheelScrollEnabled() {
        return getModel().getMouseWheelScrollEnabled();
    }

    @Override
    public StockChart setMouseWheelScrollEnabled(Boolean mouseWheelScrollEnabled) {
        getModel().setMouseWheelScrollEnabled(mouseWheelScrollEnabled);
        return this;
    }

    @Override
    public List<StockPanel> getPanels() {
        return getModel().getPanels();
    }

    @Override
    public StockChart setPanels(List<StockPanel> panels) {
        if (panels != null) {
            setupFormatStrings(panels);
        }
        getModel().setPanels(panels);
        return this;
    }

    @Override
    public StockChart addPanels(StockPanel... panels) {
        if (panels != null) {
            setupFormatStrings(Arrays.asList(panels));
        }

        getModel().addPanels(panels);
        return this;
    }

    @Override
    public PanelsSettings getPanelsSettings() {
        return getModel().getPanelsSettings();
    }

    @Override
    public StockChart setPanelsSettings(PanelsSettings panelsSettings) {
        if (panelsSettings != null) {
            // number prefixes
            if (BooleanUtils.isTrue(panelsSettings.getUsePrefixes())) {
                if (panelsSettings.getPrefixesOfBigNumbers() == null) {
                    List<BigNumberPrefix> prefixes = new ArrayList<>();
                    for (BigNumberPower power : BigNumberPower.values()) {
                        prefixes.add(new BigNumberPrefix(power,
                                messages.getMessage("amcharts.bigNumberPower." + power.name())));
                    }
                    panelsSettings.setPrefixesOfBigNumbers(prefixes);
                }
                if (panelsSettings.getPrefixesOfSmallNumbers() == null) {
                    List<SmallNumberPrefix> prefixes = new ArrayList<>();
                    for (SmallNumberPower power : SmallNumberPower.values()) {
                        prefixes.add(new SmallNumberPrefix(power,
                                messages.getMessage("amcharts.smallNumberPower." + power.name())));
                    }
                    panelsSettings.setPrefixesOfSmallNumbers(prefixes);
                }
            }
        }

        getModel().setPanelsSettings(panelsSettings);
        return this;
    }

    @Override
    public String getPath() {
        return getModel().getPath();
    }

    @Override
    public StockChart setPath(String path) {
        getModel().setPath(path);
        return this;
    }

    @Override
    public String getPathToImages() {
        return getModel().getPathToImages();
    }

    @Override
    public StockChart setPathToImages(String pathToImages) {
        getModel().setPathToImages(pathToImages);
        return this;
    }

    @Override
    public PeriodSelector getPeriodSelector() {
        return getModel().getPeriodSelector();
    }

    @Override
    public StockChart setPeriodSelector(PeriodSelector periodSelector) {
        getModel().setPeriodSelector(periodSelector);
        return this;
    }

    @Override
    public StockEventsSettings getStockEventsSettings() {
        return getModel().getStockEventsSettings();
    }

    @Override
    public StockChart setStockEventsSettings(StockEventsSettings stockEventsSettings) {
        getModel().setStockEventsSettings(stockEventsSettings);
        return this;
    }

    @Override
    public ChartTheme getTheme() {
        return getModel().getTheme();
    }

    @Override
    public StockChart setTheme(ChartTheme theme) {
        getModel().setTheme(theme);
        return this;
    }

    @Override
    public ValueAxesSettings getValueAxesSettings() {
        return getModel().getValueAxesSettings();
    }

    @Override
    public StockChart setValueAxesSettings(ValueAxesSettings valueAxesSettings) {
        getModel().setValueAxesSettings(valueAxesSettings);
        return this;
    }

    @Override
    public Boolean getZoomOutOnDataSetChange() {
        return getModel().getZoomOutOnDataSetChange();
    }

    @Override
    public StockChart setZoomOutOnDataSetChange(Boolean zoomOutOnDataSetChange) {
        getModel().setZoomOutOnDataSetChange(zoomOutOnDataSetChange);
        return this;
    }

    @Override
    public DataSet getDataSet(String id) {
        return getModel().getDataSet(id);
    }

    @Override
    public List<String> getAdditionalFields() {
        return getModel().getAdditionalFields();
    }

    @Override
    public StockChart setAdditionalFields(List<String> additionalFields) {
        getModel().setAdditionalFields(additionalFields);
        return this;
    }

    @Override
    public Integer getProcessTimeout() {
        return getModel().getProcessTimeout();
    }

    @Override
    public StockChart setProcessTimeout(Integer processTimeout) {
        getModel().setProcessTimeout(processTimeout);
        return this;
    }

    protected StockPanel getStockPanelById(String id) {
        if (!StringUtils.isNotEmpty(id)
                || (getPanels() == null || getPanels().isEmpty())) {
            return null;
        }

        for (StockPanel stockPanel : getPanels()) {
            if (id.equals(stockPanel.getId())) {
                return stockPanel;
            }
        }
        return null;
    }

    protected StockGraph getStockGraphById(String graphId) {
        if (!StringUtils.isNotEmpty(graphId)
                || (getPanels() == null || getPanels().isEmpty())) {
            return null;
        }

        for (StockPanel stockPanel : getPanels()) {
            StockGraph graph = getStockGraphById(stockPanel, graphId);
            if (graph != null) {
                return graph;
            }
        }
        return null;
    }

    protected StockGraph getStockGraphById(StockPanel panel, String graphId) {
        if (panel == null || Strings.isNullOrEmpty(graphId)) {
            return null;
        }

        if (panel.getStockGraphs() == null || panel.getStockGraphs().isEmpty()) {
            return null;
        }

        for (StockGraph stockGraph : panel.getStockGraphs()) {
            if (graphId.equals(stockGraph.getId())) {
                return stockGraph;
            }
        }
        return null;
    }

    protected class StockChartEventsForwarder
            implements StockChartClickListener,
            StockChartRightClickListener,
            StockEventClickListener,
            StockEventRollOutListener,
            StockEventRollOverListener,
            StockPanelZoomListener,
            PeriodSelectorChangeListener,
            DataSetSelectorCompareListener,
            DataSetSelectorSelectListener,
            DataSetSelectorUnCompareListener,
            StockGraphClickListener,
            StockGraphRollOutListener,
            StockGraphRollOverListener,
            StockGraphItemClickListener,
            StockGraphItemRightClickListener,
            StockGraphItemRollOutListener,
            StockGraphItemRollOverListener {

        @Override
        public void onClick(io.jmix.charts.widget.amcharts.events.stock.StockChartClickEvent e) {
            publish(StockChartClickEvent.class,
                    new StockChartClickEvent(StockChartImpl.this, e.getX(), e.getY(), e.getAbsoluteX(),
                            e.getAbsoluteY()));
        }

        @Override
        public void onClick(io.jmix.charts.widget.amcharts.events.stock.StockGraphClickEvent e) {
            StockPanel stockPanel = getStockPanelById(e.getPanelId());
            StockGraph stockGraph = getStockGraphById(stockPanel, e.getGraphId());

            publish(StockGraphClickEvent.class,
                    new StockGraphClickEvent(StockChartImpl.this, stockPanel, e.getPanelId(), stockGraph,
                            e.getGraphId(), e.getX(), e.getY(), e.getAbsoluteX(), e.getAbsoluteY()));
        }

        @Override
        public void onZoom(io.jmix.charts.widget.amcharts.events.stock.StockPanelZoomEvent e) {
            ZoomEvent jmixEvent = new ZoomEvent(StockChartImpl.this, e.getStartDate(), e.getEndDate(),
                    DatePeriod.fromId(e.getPeriod()));
            publish(ZoomEvent.class, jmixEvent);
        }

        @Override
        public void onClick(io.jmix.charts.widget.amcharts.events.stock.StockEventClickEvent e) {
            publish(StockEventClickEvent.class,
                    new StockEventClickEvent(StockChartImpl.this, getStockGraphById(e.getGraphId()),
                            e.getGraphId(), e.getDate(), getStockEvent(e.getStockEventId())));
        }

        @Override
        public void onRollOut(io.jmix.charts.widget.amcharts.events.stock.StockEventRollOutEvent e) {
            publish(StockEventRollOutEvent.class,
                    new StockEventRollOutEvent(StockChartImpl.this, getStockGraphById(e.getGraphId()),
                            e.getGraphId(), e.getDate(), getStockEvent(e.getStockEventId())));
        }

        @Override
        public void onRollOut(io.jmix.charts.widget.amcharts.events.stock.StockGraphRollOutEvent e) {
            StockPanel stockPanel = getStockPanelById(e.getPanelId());
            StockGraph stockGraph = getStockGraphById(stockPanel, e.getGraphId());

            publish(StockGraphRollOutEvent.class,
                    new StockGraphRollOutEvent(StockChartImpl.this, stockPanel, e.getPanelId(), stockGraph,
                            e.getGraphId(), e.getX(), e.getY(), e.getAbsoluteX(), e.getAbsoluteY()));
        }

        @Override
        public void onRollOver(io.jmix.charts.widget.amcharts.events.stock.StockEventRollOverEvent e) {
            publish(StockEventRollOverEvent.class,
                    new StockEventRollOverEvent(StockChartImpl.this, getStockGraphById(e.getGraphId()),
                            e.getGraphId(), e.getDate(), getStockEvent(e.getStockEventId())));
        }

        @Override
        public void onRollOver(io.jmix.charts.widget.amcharts.events.stock.StockGraphRollOverEvent e) {
            StockPanel stockPanel = getStockPanelById(e.getPanelId());
            StockGraph stockGraph = getStockGraphById(stockPanel, e.getGraphId());

            publish(StockGraphRollOverEvent.class,
                    new StockGraphRollOverEvent(StockChartImpl.this, stockPanel, e.getPanelId(), stockGraph,
                            e.getGraphId(), e.getX(), e.getY(), e.getAbsoluteX(), e.getAbsoluteY()));
        }

        @Override
        public void onChange(io.jmix.charts.widget.amcharts.events.period.PeriodSelectorChangeEvent e) {
            publish(PeriodSelectorChangeEvent.class,
                    new PeriodSelectorChangeEvent(StockChartImpl.this,
                            e.getStartDate(), e.getEndDate(), PeriodType.fromId(e.getPredefinedPeriod()),
                            e.getCount(), e.getX(), e.getY(), e.getAbsoluteX(), e.getAbsoluteY()));
        }

        @Override
        public void onClick(io.jmix.charts.widget.amcharts.events.stock.StockChartRightClickEvent e) {
            publish(StockChartRightClickEvent.class,
                    new StockChartRightClickEvent(StockChartImpl.this, e.getX(), e.getY(), e.getAbsoluteX(),
                            e.getAbsoluteY()));
        }

        @Override
        public void onClick(io.jmix.charts.widget.amcharts.events.stock.StockGraphItemClickEvent e) {
            StockPanel stockPanel = getStockPanelById(e.getPanelId());
            StockGraph stockGraph = getStockGraphById(stockPanel, e.getGraphId());

            publish(StockGraphItemClickEvent.class,
                    new StockGraphItemClickEvent(StockChartImpl.this, stockPanel,
                            e.getPanelId(), stockGraph, e.getGraphId(), e.getDataItem(), e.getItemIndex(),
                            e.getX(), e.getY(), e.getAbsoluteX(), e.getAbsoluteY()));
        }

        @Override
        public void onSelect(io.jmix.charts.widget.amcharts.events.dataset.DataSetSelectorSelectEvent e) {
            publish(DataSetSelectorSelectEvent.class,
                    new DataSetSelectorSelectEvent(StockChartImpl.this, e.getDataSetId()));
        }

        @Override
        public void onRollOut(io.jmix.charts.widget.amcharts.events.stock.StockGraphItemRollOutEvent e) {
            StockPanel stockPanel = getStockPanelById(e.getPanelId());
            StockGraph stockGraph = getStockGraphById(stockPanel, e.getGraphId());

            publish(StockGraphItemRollOutEvent.class,
                    new StockGraphItemRollOutEvent(StockChartImpl.this, stockPanel, e.getPanelId(), stockGraph,
                            e.getGraphId(), e.getDataItem(), e.getItemIndex(), e.getX(), e.getY(), e.getAbsoluteX(),
                            e.getAbsoluteY()));
        }

        @Override
        public void onCompare(io.jmix.charts.widget.amcharts.events.dataset.DataSetSelectorCompareEvent e) {
            publish(DataSetSelectorCompareEvent.class,
                    new DataSetSelectorCompareEvent(StockChartImpl.this, e.getDataSetId()));
        }

        @Override
        public void onRollOver(io.jmix.charts.widget.amcharts.events.stock.StockGraphItemRollOverEvent e) {
            StockPanel stockPanel = getStockPanelById(e.getPanelId());
            StockGraph stockGraph = getStockGraphById(stockPanel, e.getGraphId());

            publish(StockGraphItemRollOverEvent.class,
                    new StockGraphItemRollOverEvent(StockChartImpl.this, stockPanel,
                            e.getPanelId(), stockGraph, e.getGraphId(), e.getDataItem(), e.getItemIndex(),
                            e.getX(), e.getY(), e.getAbsoluteX(), e.getAbsoluteY()));
        }

        @Override
        public void onUnCompare(io.jmix.charts.widget.amcharts.events.dataset.DataSetSelectorUnCompareEvent e) {
            publish(DataSetSelectorUnCompareEvent.class,
                    new DataSetSelectorUnCompareEvent(StockChartImpl.this, e.getDataSetId()));
        }

        @Override
        public void onClick(io.jmix.charts.widget.amcharts.events.stock.StockGraphItemRightClickEvent e) {
            StockPanel stockPanel = getStockPanelById(e.getPanelId());
            StockGraph stockGraph = getStockGraphById(stockPanel, e.getGraphId());

            publish(StockGraphItemRightClickEvent.class,
                    new StockGraphItemRightClickEvent(StockChartImpl.this, stockPanel,
                            e.getPanelId(), stockGraph, e.getGraphId(), e.getDataItem(), e.getItemIndex(),
                            e.getX(), e.getY(), e.getAbsoluteX(), e.getAbsoluteY()));
        }
    }
}