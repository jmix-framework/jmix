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

package io.jmix.charts.loader;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.data.impl.ContainerDataProvider;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.charts.component.StockChart;
import io.jmix.charts.model.*;
import io.jmix.charts.model.axis.*;
import io.jmix.charts.model.balloon.BalloonPointerOrientation;
import io.jmix.charts.model.chart.impl.StockPanel;
import io.jmix.charts.model.cursor.CursorPosition;
import io.jmix.charts.model.dataset.DataSet;
import io.jmix.charts.model.dataset.DataSetSelector;
import io.jmix.charts.model.dataset.FieldMapping;
import io.jmix.charts.model.graph.Graph;
import io.jmix.charts.model.graph.GraphType;
import io.jmix.charts.model.period.Period;
import io.jmix.charts.model.period.PeriodSelector;
import io.jmix.charts.model.period.PeriodType;
import io.jmix.charts.model.settings.*;
import io.jmix.charts.model.stock.*;
import io.jmix.charts.model.JsFunction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockChartLoader extends ChartModelLoader<StockChart> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(StockChart.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);
        loadStyleName(resultComponent, element);

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadCss(resultComponent, element);

        loadConfiguration(resultComponent, element);
        loadChartData(resultComponent, element);
    }

    protected void loadConfiguration(StockChart chart, Element element) {
        loadCategoryAxesSettings(chart, element);
        loadChartCursorSettings(chart, element);
        loadChartScrollbarSettings(chart, element);
        loadDataSetSelector(chart, element);
        loadLegendSettings(chart, element);
        loadPanels(chart, element);
        loadPanelsSettings(chart, element);
        loadPeriodSelector(chart, element);
        loadStockEventsSettings(chart, element);
        loadValueAxesSettings(chart, element);

        String addClassNames = element.attributeValue("addClassNames");
        if (StringUtils.isNotEmpty(addClassNames)) {
            chart.setAddClassNames(Boolean.valueOf(addClassNames));
        }

        String animationPlayed = element.attributeValue("animationPlayed");
        if (StringUtils.isNotEmpty(animationPlayed)) {
            chart.setAnimationPlayed(Boolean.valueOf(animationPlayed));
        }

        String autoResize = element.attributeValue("autoResize");
        if (StringUtils.isNotEmpty(autoResize)) {
            chart.setAutoResize(Boolean.valueOf(autoResize));
        }

        Element balloonElement = element.element("balloon");
        if (balloonElement != null) {
            chart.setBalloon(loadBalloon(balloonElement));
        }

        String classNamePrefix = element.attributeValue("classNamePrefix");
        if (StringUtils.isNotEmpty(classNamePrefix)) {
            chart.setClassNamePrefix(classNamePrefix);
        }

        Element colorsElement = element.element("colors");
        if (colorsElement != null) {
            List<Color> colors = loadColors(colorsElement);
            if (CollectionUtils.isNotEmpty(colors)) {
                chart.setColors(colors);
            }
        }

        String comparedDataSets = element.attributeValue("comparedDataSets");
        if (comparedDataSets != null) {
            chart.setComparedDataSets(Arrays.asList(comparedDataSets.split(",")));
        }

        String dataDateFormat = element.attributeValue("dataDateFormat");
        if (StringUtils.isNotEmpty(dataDateFormat)) {
            chart.setDataDateFormat(dataDateFormat);
        }

        Element dataSetsElement = element.element("dataSets");
        if (dataSetsElement != null) {
            chart.setDataSets(loadDataSets(dataSetsElement));
        }

        Element exportElement = element.element("export");
        if (exportElement != null) {
            chart.setExport(loadExport(exportElement));
        }

        String extendToFullPeriod = element.attributeValue("extendToFullPeriod");
        if (StringUtils.isNotEmpty(extendToFullPeriod)) {
            chart.setExtendToFullPeriod(Boolean.valueOf(extendToFullPeriod));
        }

        String firstDayOfWeek = element.attributeValue("firstDayOfWeek");
        if (StringUtils.isNotEmpty(firstDayOfWeek)) {
            chart.setFirstDayOfWeek(Integer.valueOf(firstDayOfWeek));
        }

        String glueToTheEnd = element.attributeValue("glueToTheEnd");
        if (StringUtils.isNotEmpty(glueToTheEnd)) {
            chart.setGlueToTheEnd(Boolean.valueOf(glueToTheEnd));
        }

        String language = element.attributeValue("language");
        if (StringUtils.isNotEmpty(language)) {
            chart.setLanguage(language);
        }

        String mainDataSet = element.attributeValue("mainDataSet");
        if (StringUtils.isNotEmpty(mainDataSet)) {
            chart.setMainDataSet(mainDataSet);
        }

        String mouseWheelScrollEnabled = element.attributeValue("mouseWheelScrollEnabled");
        if (StringUtils.isNotEmpty(mouseWheelScrollEnabled)) {
            chart.setMouseWheelScrollEnabled(Boolean.valueOf(mouseWheelScrollEnabled));
        }

        String processTimeout = element.attributeValue("processTimeout");
        if (StringUtils.isNotEmpty(processTimeout)) {
            chart.setProcessTimeout(Integer.valueOf(processTimeout));
        }

        String theme = element.attributeValue("theme");
        if (StringUtils.isNotEmpty(theme)) {
            chart.setTheme(ChartTheme.valueOf(theme));
        }

        String zoomOutOnDataSetChange = element.attributeValue("zoomOutOnDataSetChange");
        if (StringUtils.isNotEmpty(zoomOutOnDataSetChange)) {
            chart.setZoomOutOnDataSetChange(Boolean.valueOf(zoomOutOnDataSetChange));
        }

        Element nativeJson = element.element("nativeJson");
        if (nativeJson != null) {
            String nativeJsonString = nativeJson.getTextTrim();
            try {
                JsonParser parser = new JsonParser();
                parser.parse(nativeJsonString);
            } catch (JsonSyntaxException e) {
                throw new GuiDevelopmentException("Unable to parse JSON from XML chart configuration", context);
            }

            resultComponent.setNativeJson(nativeJsonString);
        }
    }

    protected void loadChartData(StockChart chart, Element element) {
        Element dataProvider = element.element("data");
        if (dataProvider != null) {
            for (Object data : dataProvider.elements("dataSet")) {
                Element dataElement = (Element) data;

                String dataSetId = dataElement.attributeValue("id");
                if (StringUtils.isNotEmpty(dataSetId)) {
                    ListDataProvider listDataProvider = new ListDataProvider();

                    for (Object item : dataElement.elements("item")) {
                        Element itemElement = (Element) item;
                        MapDataItem mapDataItem = new MapDataItem();

                        for (Element property : itemElement.elements("property")) {
                            mapDataItem = loadDataItem(property, mapDataItem);
                        }

                        listDataProvider.addItem(mapDataItem);
                    }

                    chart.getDataSet(dataSetId).setDataProvider(listDataProvider);
                }

            }
        }
    }

    protected void loadPanels(StockChart chart, Element element) {
        Element panelsElement = element.element("panels");
        if (panelsElement != null) {
            for (Object panelItem : panelsElement.elements("panel")) {
                Element panelElement = (Element) panelItem;
                StockPanel stockPanel = new StockPanel();
                loadStockPanel(stockPanel, panelElement);
                chart.addPanels(stockPanel);
            }
        }
    }

    protected List<DataSet> loadDataSets(Element dataSetsElement) {
        List<DataSet> dataSets = new ArrayList<>();

        for (Object dataSetItem : dataSetsElement.elements("dataSet")) {
            Element dataSetElement = (Element) dataSetItem;
            DataSet dataSet = new DataSet();
            loadDataSet(dataSet, dataSetElement);
            dataSets.add(dataSet);
        }

        return dataSets;
    }

    protected void loadDataSet(DataSet dataSet, Element dataSetElement) {

        loadFieldMappings(dataSet, dataSetElement);
        loadStockEvents(dataSet, dataSetElement);

        String id = dataSetElement.attributeValue("id");
        if (StringUtils.isNotEmpty(id)) {
            dataSet.setId(id);
        }

        checkMultipleDatasources(dataSetElement);

        String categoryField = dataSetElement.attributeValue("categoryField");
        if (StringUtils.isNotEmpty(categoryField)) {
            dataSet.setCategoryField(categoryField);
        }

        String color = dataSetElement.attributeValue("color");
        if (StringUtils.isNotEmpty(color)) {
            dataSet.setColor(Color.valueOf(color));
        }

        String compared = dataSetElement.attributeValue("compared");
        if (StringUtils.isNotEmpty(compared)) {
            dataSet.setCompared(Boolean.valueOf(compared));
        }

        String dataContainerId = dataSetElement.attributeValue("dataContainer");
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

            dataSet.setDataProvider(new ContainerDataProvider(dataContainer));
        }

        String showInCompare = dataSetElement.attributeValue("showInCompare");
        if (StringUtils.isNotEmpty(showInCompare)) {
            dataSet.setShowInCompare(Boolean.valueOf(showInCompare));
        }

        String showInSelect = dataSetElement.attributeValue("showInSelect");
        if (StringUtils.isNotEmpty(showInSelect)) {
            dataSet.setShowInSelect(Boolean.valueOf(showInSelect));
        }

        String title = dataSetElement.attributeValue("title");
        if (StringUtils.isNotEmpty(title)) {
            dataSet.setTitle(title);
        }
    }

    protected void checkMultipleDatasources(Element dataSetElement) {
        String datasource = dataSetElement.attributeValue("datasource");
        String dataContainer = dataSetElement.attributeValue("dataContainer");
        Element dataElement = element.element("data");

        boolean isDatasourceProperty = StringUtils.isNotEmpty(datasource);
        boolean isDataContainerProperty = StringUtils.isNotEmpty(dataContainer);

        if ((isDatasourceProperty && isDataContainerProperty)
                || (dataElement != null && (isDatasourceProperty || isDataContainerProperty))) {
            throw new GuiDevelopmentException(
                    String.format("You cannot use chart '%s' with simultaneously defined: data element, datasource and "
                            + "dataContainer properties", resultComponent.getId()), context
            );
        }
    }

    protected void loadFieldMappings(DataSet dataSet, Element dataSetElement) {
        Element fieldMappingsElement = dataSetElement.element("fieldMappings");
        if (fieldMappingsElement != null) {
            for (Object fieldMappingItem : fieldMappingsElement.elements("fieldMapping")) {
                Element fieldMappingElement = (Element) fieldMappingItem;
                FieldMapping fieldMapping = new FieldMapping();
                loadFieldMapping(fieldMapping, fieldMappingElement);
                dataSet.addFieldMappings(fieldMapping);
            }
        }
    }

    protected void loadFieldMapping(FieldMapping fieldMapping, Element fieldMappingElement) {
        String fromField = fieldMappingElement.attributeValue("fromField");
        if (StringUtils.isNotEmpty(fromField)) {
            fieldMapping.setFromField(fromField);
        }

        String toField = fieldMappingElement.attributeValue("toField");
        if (StringUtils.isNotEmpty(toField)) {
            fieldMapping.setToField(toField);
        }
    }

    protected void loadStockEvents(DataSet dataSet, Element dataSetElement) {
        Element stockEventsElement = dataSetElement.element("stockEvents");
        if (stockEventsElement != null) {
            for (Object stockEventItem : stockEventsElement.elements("stockEvent")) {
                Element stockEventElement = (Element) stockEventItem;
                StockEvent stockEvent = new StockEvent();
                loadStockEvent(stockEvent, stockEventElement);
                dataSet.addStockEvents(stockEvent);
            }
        }
    }

    protected void loadStockEvent(StockEvent stockEvent, Element stockEventElement) {

        String backgroundAlpha = stockEventElement.attributeValue("backgroundAlpha");
        if (StringUtils.isNotEmpty(backgroundAlpha)) {
            stockEvent.setBackgroundAlpha(Double.valueOf(backgroundAlpha));
        }

        String backgroundColor = stockEventElement.attributeValue("backgroundColor");
        if (StringUtils.isNotEmpty(backgroundColor)) {
            stockEvent.setBackgroundColor(Color.valueOf(backgroundColor));
        }

        String borderAlpha = stockEventElement.attributeValue("borderAlpha");
        if (StringUtils.isNotEmpty(borderAlpha)) {
            stockEvent.setBorderAlpha(Double.valueOf(borderAlpha));
        }

        String borderColor = stockEventElement.attributeValue("borderColor");
        if (StringUtils.isNotEmpty(borderColor)) {
            stockEvent.setBorderColor(Color.valueOf(borderColor));
        }

        String color = stockEventElement.attributeValue("color");
        if (StringUtils.isNotEmpty(color)) {
            stockEvent.setColor(Color.valueOf(color));
        }

        String date = stockEventElement.attributeValue("date");
        if (StringUtils.isNotEmpty(date)) {
            stockEvent.setDate(loadDate(date));
        }

        String description = stockEventElement.attributeValue("description");
        if (StringUtils.isNotEmpty(description)) {
            stockEvent.setDescription(description);
        }

        String fontSize = stockEventElement.attributeValue("fontSize");
        if (StringUtils.isNotEmpty(fontSize)) {
            stockEvent.setFontSize(Integer.valueOf(fontSize));
        }

        String graph = stockEventElement.attributeValue("graph");
        if (StringUtils.isNotEmpty(graph)) {
            stockEvent.setGraph(graph);
        }

        String rollOverColor = stockEventElement.attributeValue("rollOverColor");
        if (StringUtils.isNotEmpty(rollOverColor)) {
            stockEvent.setRollOverColor(Color.valueOf(rollOverColor));
        }

        String showAt = stockEventElement.attributeValue("showAt");
        if (StringUtils.isNotEmpty(showAt)) {
            stockEvent.setShowAt(showAt);
        }

        String showBullet = stockEventElement.attributeValue("showBullet");
        if (StringUtils.isNotEmpty(showBullet)) {
            stockEvent.setShowBullet(Boolean.valueOf(showBullet));
        }

        String showOnAxis = stockEventElement.attributeValue("showOnAxis");
        if (StringUtils.isNotEmpty(showOnAxis)) {
            stockEvent.setShowOnAxis(Boolean.valueOf(showOnAxis));
        }

        String text = stockEventElement.attributeValue("text");
        if (StringUtils.isNotEmpty(text)) {
            stockEvent.setText(text);
        }

        String type = stockEventElement.attributeValue("type");
        if (StringUtils.isNotEmpty(type)) {
            stockEvent.setType(StockEventType.valueOf(type));
        }

        String url = stockEventElement.attributeValue("url");
        if (StringUtils.isNotEmpty(url)) {
            stockEvent.setUrl(url);
        }

        String urlTarget = stockEventElement.attributeValue("urlTarget");
        if (StringUtils.isNotEmpty(urlTarget)) {
            stockEvent.setUrlTarget(urlTarget);
        }

        String value = stockEventElement.attributeValue("value");
        if (StringUtils.isNotEmpty(value)) {
            stockEvent.setValue(Double.valueOf(value));
        }
    }

    protected void loadLegendSettings(StockChart chart, Element element) {
        Element legendSettingsElement = element.element("legendSettings");
        if (legendSettingsElement != null) {
            LegendSettings legendSettings = new LegendSettings();

            String align = legendSettingsElement.attributeValue("align");
            if (StringUtils.isNotEmpty(align)) {
                legendSettings.setAlign(Align.valueOf(align));
            }

            String equalWidths = legendSettingsElement.attributeValue("equalWidths");
            if (StringUtils.isNotEmpty(equalWidths)) {
                legendSettings.setEqualWidths(Boolean.valueOf(equalWidths));
            }

            String horizontalGap = legendSettingsElement.attributeValue("horizontalGap");
            if (StringUtils.isNotEmpty(horizontalGap)) {
                legendSettings.setHorizontalGap(Integer.valueOf(horizontalGap));
            }

            String labelText = legendSettingsElement.attributeValue("labelText");
            if (StringUtils.isNotEmpty(labelText)) {
                legendSettings.setLabelText(labelText);
            }

            String marginBottom = legendSettingsElement.attributeValue("marginBottom");
            if (StringUtils.isNotEmpty(marginBottom)) {
                legendSettings.setMarginBottom(Integer.valueOf(marginBottom));
            }

            String marginTop = legendSettingsElement.attributeValue("marginTop");
            if (StringUtils.isNotEmpty(marginTop)) {
                legendSettings.setMarginTop(Integer.valueOf(marginTop));
            }

            String markerBorderAlpha = legendSettingsElement.attributeValue("markerBorderAlpha");
            if (StringUtils.isNotEmpty(markerBorderAlpha)) {
                legendSettings.setMarkerBorderAlpha(Double.valueOf(markerBorderAlpha));
            }

            String markerBorderColor = legendSettingsElement.attributeValue("markerBorderColor");
            if (StringUtils.isNotEmpty(markerBorderColor)) {
                legendSettings.setMarkerBorderColor(Color.valueOf(markerBorderColor));
            }

            String markerBorderThickness = legendSettingsElement.attributeValue("markerBorderThickness");
            if (StringUtils.isNotEmpty(markerBorderThickness)) {
                legendSettings.setMarkerBorderThickness(Integer.valueOf(markerBorderThickness));
            }

            String markerDisabledColor = legendSettingsElement.attributeValue("markerDisabledColor");
            if (StringUtils.isNotEmpty(markerDisabledColor)) {
                legendSettings.setMarkerDisabledColor(Color.valueOf(markerDisabledColor));
            }

            String markerLabelGap = legendSettingsElement.attributeValue("markerLabelGap");
            if (StringUtils.isNotEmpty(markerLabelGap)) {
                legendSettings.setMarkerLabelGap(Integer.valueOf(markerLabelGap));
            }

            String markerSize = legendSettingsElement.attributeValue("markerSize");
            if (StringUtils.isNotEmpty(markerSize)) {
                legendSettings.setMarkerSize(Integer.valueOf(markerSize));
            }

            String markerType = legendSettingsElement.attributeValue("markerType");
            if (StringUtils.isNotEmpty(markerType)) {
                legendSettings.setMarkerType(MarkerType.valueOf(markerType));
            }

            String position = legendSettingsElement.attributeValue("position");
            if (StringUtils.isNotEmpty(position)) {
                legendSettings.setPosition(LegendSettingsPosition.valueOf(position));
            }

            String reversedOrder = legendSettingsElement.attributeValue("reversedOrder");
            if (StringUtils.isNotEmpty(reversedOrder)) {
                legendSettings.setReversedOrder(Boolean.valueOf(reversedOrder));
            }

            String rollOverColor = legendSettingsElement.attributeValue("rollOverColor");
            if (StringUtils.isNotEmpty(rollOverColor)) {
                legendSettings.setRollOverColor(Color.valueOf(rollOverColor));
            }

            String rollOverGraphAlpha = legendSettingsElement.attributeValue("rollOverGraphAlpha");
            if (StringUtils.isNotEmpty(rollOverGraphAlpha)) {
                legendSettings.setRollOverGraphAlpha(Double.valueOf(rollOverGraphAlpha));
            }

            String switchable = legendSettingsElement.attributeValue("switchable");
            if (StringUtils.isNotEmpty(switchable)) {
                legendSettings.setSwitchable(Boolean.valueOf(switchable));
            }

            String switchColor = legendSettingsElement.attributeValue("switchColor");
            if (StringUtils.isNotEmpty(switchColor)) {
                legendSettings.setSwitchColor(Color.valueOf(switchColor));
            }

            String switchType = legendSettingsElement.attributeValue("switchType");
            if (StringUtils.isNotEmpty(switchType)) {
                legendSettings.setSwitchType(SwitchType.valueOf(switchType));
            }

            String textClickEnabled = legendSettingsElement.attributeValue("textClickEnabled");
            if (StringUtils.isNotEmpty(textClickEnabled)) {
                legendSettings.setTextClickEnabled(Boolean.valueOf(textClickEnabled));
            }

            String useMarkerColorForLabels = legendSettingsElement.attributeValue("useMarkerColorForLabels");
            if (StringUtils.isNotEmpty(useMarkerColorForLabels)) {
                legendSettings.setUseMarkerColorForLabels(Boolean.valueOf(useMarkerColorForLabels));
            }

            String valueTextComparing = legendSettingsElement.attributeValue("valueTextComparing");
            if (StringUtils.isNotEmpty(valueTextComparing)) {
                legendSettings.setValueTextComparing(valueTextComparing);
            }

            String valueTextRegular = legendSettingsElement.attributeValue("valueTextRegular");
            if (StringUtils.isNotEmpty(valueTextRegular)) {
                legendSettings.setValueTextRegular(valueTextRegular);
            }

            String valueWidth = legendSettingsElement.attributeValue("valueWidth");
            if (StringUtils.isNotEmpty(valueWidth)) {
                legendSettings.setValueWidth(Integer.valueOf(valueWidth));
            }

            String verticalGap = legendSettingsElement.attributeValue("verticalGap");
            if (StringUtils.isNotEmpty(verticalGap)) {
                legendSettings.setVerticalGap(Integer.valueOf(verticalGap));
            }

            chart.setLegendSettings(legendSettings);
        }
    }

    protected void loadDataSetSelector(StockChart chart, Element element) {
        Element dataSetSelectorElement = element.element("dataSetSelector");
        if (dataSetSelectorElement != null) {
            DataSetSelector dataSetSelector = new DataSetSelector();

            String comboBoxSelectText = dataSetSelectorElement.attributeValue("comboBoxSelectText");
            if (StringUtils.isNotEmpty(comboBoxSelectText)) {
                dataSetSelector.setComboBoxSelectText(comboBoxSelectText);
            }

            String compareText = dataSetSelectorElement.attributeValue("compareText");
            if (StringUtils.isNotEmpty(compareText)) {
                dataSetSelector.setCompareText(compareText);
            }
            String listHeight = dataSetSelectorElement.attributeValue("listHeight");
            if (StringUtils.isNotEmpty(listHeight)) {
                dataSetSelector.setListHeight(Integer.valueOf(listHeight));
            }

            String position = dataSetSelectorElement.attributeValue("position");
            if (StringUtils.isNotEmpty(position)) {
                dataSetSelector.setPosition(Position.valueOf(position));
            }

            String selectText = dataSetSelectorElement.attributeValue("selectText");
            if (StringUtils.isNotEmpty(selectText)) {
                dataSetSelector.setSelectText(selectText);
            }

            String width = dataSetSelectorElement.attributeValue("width");
            if (StringUtils.isNotEmpty(width)) {
                dataSetSelector.setWidth(Integer.valueOf(width));
            }

            chart.setDataSetSelector(dataSetSelector);
        }
    }

    protected void loadChartCursorSettings(StockChart chart, Element element) {
        Element settingsElement = element.element("chartCursorSettings");
        if (settingsElement != null) {
            ChartCursorSettings chartCursorSettings = new ChartCursorSettings();

            Element categoryBalloonDateFormatsElement = element.element("categoryBalloonDateFormats");
            if (categoryBalloonDateFormatsElement != null) {
                chartCursorSettings.setCategoryBalloonDateFormats(loadDateFormats(categoryBalloonDateFormatsElement));
            }

            String balloonPointerOrientation = settingsElement.attributeValue("balloonPointerOrientation");
            if (StringUtils.isNotEmpty(balloonPointerOrientation)) {
                chartCursorSettings.setBalloonPointerOrientation(BalloonPointerOrientation.valueOf(balloonPointerOrientation));
            }

            String bulletsEnabled = settingsElement.attributeValue("bulletsEnabled");
            if (StringUtils.isNotEmpty(bulletsEnabled)) {
                chartCursorSettings.setBulletsEnabled(Boolean.valueOf(bulletsEnabled));
            }

            String bulletSize = settingsElement.attributeValue("bulletSize");
            if (StringUtils.isNotEmpty(bulletSize)) {
                chartCursorSettings.setBulletSize(Integer.valueOf(bulletSize));
            }

            String categoryBalloonAlpha = settingsElement.attributeValue("categoryBalloonAlpha");
            if (StringUtils.isNotEmpty(categoryBalloonAlpha)) {
                chartCursorSettings.setCategoryBalloonAlpha(Double.valueOf(categoryBalloonAlpha));
            }

            String categoryBalloonColor = settingsElement.attributeValue("categoryBalloonColor");
            if (StringUtils.isNotEmpty(categoryBalloonColor)) {
                chartCursorSettings.setCategoryBalloonColor(Color.valueOf(categoryBalloonColor));
            }

            String categoryBalloonEnabled = settingsElement.attributeValue("categoryBalloonEnabled");
            if (StringUtils.isNotEmpty(categoryBalloonEnabled)) {
                chartCursorSettings.setCategoryBalloonEnabled(Boolean.valueOf(categoryBalloonEnabled));
            }

            String categoryBalloonText = settingsElement.attributeValue("categoryBalloonText");
            if (StringUtils.isNotEmpty(categoryBalloonText)) {
                chartCursorSettings.setCategoryBalloonText(categoryBalloonText);
            }

            String color = settingsElement.attributeValue("color");
            if (StringUtils.isNotEmpty(color)) {
                chartCursorSettings.setColor(Color.valueOf(color));
            }

            String cursorAlpha = settingsElement.attributeValue("cursorAlpha");
            if (StringUtils.isNotEmpty(cursorAlpha)) {
                chartCursorSettings.setCursorAlpha(Double.valueOf(cursorAlpha));
            }

            String cursorColor = settingsElement.attributeValue("cursorColor");
            if (StringUtils.isNotEmpty(cursorColor)) {
                chartCursorSettings.setCursorColor(Color.valueOf(cursorColor));
            }

            String cursorPosition = settingsElement.attributeValue("cursorPosition");
            if (StringUtils.isNotEmpty(cursorPosition)) {
                chartCursorSettings.setCursorPosition(CursorPosition.valueOf(cursorPosition));
            }

            String enabled = settingsElement.attributeValue("enabled");
            if (StringUtils.isNotEmpty(enabled)) {
                chartCursorSettings.setEnabled(Boolean.valueOf(enabled));
            }

            String fullWidth = settingsElement.attributeValue("fullWidth");
            if (StringUtils.isNotEmpty(fullWidth)) {
                chartCursorSettings.setFullWidth(Boolean.valueOf(fullWidth));
            }

            String graphBulletSize = settingsElement.attributeValue("graphBulletSize");
            if (StringUtils.isNotEmpty(graphBulletSize)) {
                chartCursorSettings.setGraphBulletSize(Double.valueOf(graphBulletSize));
            }

            String leaveAfterTouch = settingsElement.attributeValue("leaveAfterTouch");
            if (StringUtils.isNotEmpty(leaveAfterTouch)) {
                chartCursorSettings.setLeaveAfterTouch(Boolean.valueOf(leaveAfterTouch));
            }

            String leaveCursor = settingsElement.attributeValue("leaveCursor");
            if (StringUtils.isNotEmpty(leaveCursor)) {
                chartCursorSettings.setLeaveCursor(Boolean.valueOf(leaveCursor));
            }

            String onePanelOnly = settingsElement.attributeValue("onePanelOnly");
            if (StringUtils.isNotEmpty(onePanelOnly)) {
                chartCursorSettings.setOnePanelOnly(Boolean.valueOf(onePanelOnly));
            }

            String pan = settingsElement.attributeValue("pan");
            if (StringUtils.isNotEmpty(pan)) {
                chartCursorSettings.setPan(Boolean.valueOf(pan));
            }

            String valueBalloonsEnabled = settingsElement.attributeValue("valueBalloonsEnabled");
            if (StringUtils.isNotEmpty(valueBalloonsEnabled)) {
                chartCursorSettings.setValueBalloonsEnabled(Boolean.valueOf(valueBalloonsEnabled));
            }

            String valueLineAlpha = settingsElement.attributeValue("valueLineAlpha");
            if (StringUtils.isNotEmpty(valueLineAlpha)) {
                chartCursorSettings.setValueLineAlpha(Double.valueOf(valueLineAlpha));
            }

            String valueLineBalloonEnabled = settingsElement.attributeValue("valueLineBalloonEnabled");
            if (StringUtils.isNotEmpty(valueLineBalloonEnabled)) {
                chartCursorSettings.setValueLineBalloonEnabled(Boolean.valueOf(valueLineBalloonEnabled));
            }

            String valueLineEnabled = settingsElement.attributeValue("valueLineEnabled");
            if (StringUtils.isNotEmpty(valueLineEnabled)) {
                chartCursorSettings.setValueLineEnabled(Boolean.valueOf(valueLineEnabled));
            }

            String zoomable = settingsElement.attributeValue("zoomable");
            if (StringUtils.isNotEmpty(zoomable)) {
                chartCursorSettings.setZoomable(Boolean.valueOf(zoomable));
            }

            String selectWithoutZooming = settingsElement.attributeValue("selectWithoutZooming");
            if (StringUtils.isNotEmpty(selectWithoutZooming)) {
                chartCursorSettings.setSelectWithoutZooming(Boolean.valueOf(selectWithoutZooming));
            }

            String showNextAvailable = settingsElement.attributeValue("showNextAvailable");
            if (StringUtils.isNotEmpty(showNextAvailable)) {
                chartCursorSettings.setShowNextAvailable(Boolean.valueOf(showNextAvailable));
            }

            chart.setChartCursorSettings(chartCursorSettings);
        }
    }

    protected void loadChartScrollbarSettings(StockChart chart, Element element) {
        Element settingsElement = element.element("chartScrollbarSettings");
        if (settingsElement != null) {
            ChartScrollbarSettings chartScrollbarSettings = new ChartScrollbarSettings();

            String autoGridCount = settingsElement.attributeValue("autoGridCount");
            if (StringUtils.isNotEmpty(autoGridCount)) {
                chartScrollbarSettings.setAutoGridCount(Boolean.valueOf(autoGridCount));
            }

            String backgroundAlpha = settingsElement.attributeValue("backgroundAlpha");
            if (StringUtils.isNotEmpty(backgroundAlpha)) {
                chartScrollbarSettings.setBackgroundAlpha(Double.valueOf(backgroundAlpha));
            }

            String backgroundColor = settingsElement.attributeValue("backgroundColor");
            if (StringUtils.isNotEmpty(backgroundColor)) {
                chartScrollbarSettings.setBackgroundColor(Color.valueOf(backgroundColor));
            }

            String color = settingsElement.attributeValue("color");
            if (StringUtils.isNotEmpty(color)) {
                chartScrollbarSettings.setColor(Color.valueOf(color));
            }

            String dragIconHeight = settingsElement.attributeValue("dragIconHeight");
            if (StringUtils.isNotEmpty(dragIconHeight)) {
                chartScrollbarSettings.setDragIconHeight(Integer.valueOf(dragIconHeight));
            }

            String dragIconWidth = settingsElement.attributeValue("dragIconWidth");
            if (StringUtils.isNotEmpty(dragIconWidth)) {
                chartScrollbarSettings.setDragIconWidth(Integer.valueOf(dragIconWidth));
            }

            String enabled = settingsElement.attributeValue("enabled");
            if (StringUtils.isNotEmpty(enabled)) {
                chartScrollbarSettings.setEnabled(Boolean.valueOf(enabled));
            }

            String fontSize = settingsElement.attributeValue("fontSize");
            if (StringUtils.isNotEmpty(fontSize)) {
                chartScrollbarSettings.setFontSize(Integer.valueOf(fontSize));
            }

            String graph = settingsElement.attributeValue("graph");
            if (StringUtils.isNotEmpty(graph)) {
                chartScrollbarSettings.setGraph(graph);
            }

            String graphFillAlpha = settingsElement.attributeValue("graphFillAlpha");
            if (StringUtils.isNotEmpty(graphFillAlpha)) {
                chartScrollbarSettings.setGraphFillAlpha(Double.valueOf(graphFillAlpha));
            }

            String graphFillColor = settingsElement.attributeValue("graphFillColor");
            if (StringUtils.isNotEmpty(graphFillColor)) {
                chartScrollbarSettings.setGraphFillColor(Color.valueOf(graphFillColor));
            }

            String graphLineAlpha = settingsElement.attributeValue("graphLineAlpha");
            if (StringUtils.isNotEmpty(graphLineAlpha)) {
                chartScrollbarSettings.setGraphLineAlpha(Double.valueOf(graphLineAlpha));
            }

            String graphLineColor = settingsElement.attributeValue("graphLineColor");
            if (StringUtils.isNotEmpty(graphLineColor)) {
                chartScrollbarSettings.setGraphLineColor(Color.valueOf(graphLineColor));
            }

            String graphType = settingsElement.attributeValue("graphType");
            if (StringUtils.isNotEmpty(graphType)) {
                chartScrollbarSettings.setGraphType(GraphType.valueOf(graphType));
            }

            String gridAlpha = settingsElement.attributeValue("gridAlpha");
            if (StringUtils.isNotEmpty(gridAlpha)) {
                chartScrollbarSettings.setGridAlpha(Double.valueOf(gridAlpha));
            }

            String gridColor = settingsElement.attributeValue("gridColor");
            if (StringUtils.isNotEmpty(gridColor)) {
                chartScrollbarSettings.setGridColor(Color.valueOf(gridColor));
            }

            String gridCount = settingsElement.attributeValue("gridCount");
            if (StringUtils.isNotEmpty(gridCount)) {
                chartScrollbarSettings.setGridCount(Integer.valueOf(gridCount));
            }

            String height = settingsElement.attributeValue("height");
            if (StringUtils.isNotEmpty(height)) {
                chartScrollbarSettings.setHeight(Integer.valueOf(height));
            }

            String hideResizeGrips = settingsElement.attributeValue("hideResizeGrips");
            if (StringUtils.isNotEmpty(hideResizeGrips)) {
                chartScrollbarSettings.setHideResizeGrips(Boolean.valueOf(hideResizeGrips));
            }

            String markPeriodChange = settingsElement.attributeValue("markPeriodChange");
            if (StringUtils.isNotEmpty(markPeriodChange)) {
                chartScrollbarSettings.setMarkPeriodChange(Boolean.valueOf(markPeriodChange));
            }

            String position = settingsElement.attributeValue("position");
            if (StringUtils.isNotEmpty(position)) {
                chartScrollbarSettings.setPosition(ChartScrollbarPosition.valueOf(position));
            }

            String resizeEnabled = settingsElement.attributeValue("resizeEnabled");
            if (StringUtils.isNotEmpty(resizeEnabled)) {
                chartScrollbarSettings.setResizeEnabled(Boolean.valueOf(resizeEnabled));
            }

            String scrollDuration = settingsElement.attributeValue("scrollDuration");
            if (StringUtils.isNotEmpty(scrollDuration)) {
                chartScrollbarSettings.setScrollDuration(Double.valueOf(scrollDuration));
            }

            String selectedBackgroundAlpha = settingsElement.attributeValue("selectedBackgroundAlpha");
            if (StringUtils.isNotEmpty(selectedBackgroundAlpha)) {
                chartScrollbarSettings.setSelectedBackgroundAlpha(Double.valueOf(selectedBackgroundAlpha));
            }

            String selectedBackgroundColor = settingsElement.attributeValue("selectedBackgroundColor");
            if (StringUtils.isNotEmpty(selectedBackgroundColor)) {
                chartScrollbarSettings.setSelectedBackgroundColor(Color.valueOf(selectedBackgroundColor));
            }

            String selectedGraphFillAlpha = settingsElement.attributeValue("selectedGraphFillAlpha");
            if (StringUtils.isNotEmpty(selectedGraphFillAlpha)) {
                chartScrollbarSettings.setSelectedGraphFillAlpha(Double.valueOf(selectedGraphFillAlpha));
            }

            String selectedGraphFillColor = settingsElement.attributeValue("selectedGraphFillColor");
            if (StringUtils.isNotEmpty(selectedGraphFillColor)) {
                chartScrollbarSettings.setSelectedGraphFillColor(Color.valueOf(selectedGraphFillColor));
            }

            String selectedGraphLineAlpha = settingsElement.attributeValue("selectedGraphLineAlpha");
            if (StringUtils.isNotEmpty(selectedGraphLineAlpha)) {
                chartScrollbarSettings.setSelectedGraphLineAlpha(Double.valueOf(selectedGraphLineAlpha));
            }

            String selectedGraphLineColor = settingsElement.attributeValue("selectedGraphLineColor");
            if (StringUtils.isNotEmpty(selectedGraphLineColor)) {
                chartScrollbarSettings.setSelectedGraphLineColor(Color.valueOf(selectedGraphLineColor));
            }

            String updateOnReleaseOnly = settingsElement.attributeValue("updateOnReleaseOnly");
            if (StringUtils.isNotEmpty(updateOnReleaseOnly)) {
                chartScrollbarSettings.setUpdateOnReleaseOnly(Boolean.valueOf(updateOnReleaseOnly));
            }

            String usePeriod = settingsElement.attributeValue("usePeriod");
            if (StringUtils.isNotEmpty(usePeriod)) {
                chartScrollbarSettings.setUsePeriod(usePeriod);
            }

            chart.setChartScrollbarSettings(chartScrollbarSettings);
        }
    }

    protected void loadCategoryAxesSettings(StockChart chart, Element element) {
        Element settingsElement = element.element("categoryAxesSettings");
        if (settingsElement != null) {
            CategoryAxesSettings categoryAxesSettings = new CategoryAxesSettings();

            Element dateFormatsElement = element.element("dateFormats");
            if (dateFormatsElement != null) {
                categoryAxesSettings.setDateFormats(loadDateFormats(dateFormatsElement));
            }

            String alwaysGroup = settingsElement.attributeValue("alwaysGroup");
            if (StringUtils.isNotEmpty(alwaysGroup)) {
                categoryAxesSettings.setAlwaysGroup(Boolean.valueOf(alwaysGroup));
            }

            String autoGridCount = settingsElement.attributeValue("autoGridCount");
            if (StringUtils.isNotEmpty(autoGridCount)) {
                categoryAxesSettings.setAutoGridCount(Boolean.valueOf(autoGridCount));
            }

            String axisAlpha = settingsElement.attributeValue("axisAlpha");
            if (StringUtils.isNotEmpty(axisAlpha)) {
                categoryAxesSettings.setAxisAlpha(Double.valueOf(axisAlpha));
            }

            String axisColor = settingsElement.attributeValue("axisColor");
            if (StringUtils.isNotEmpty(axisColor)) {
                categoryAxesSettings.setAxisColor(Color.valueOf(axisColor));
            }

            String axisHeight = settingsElement.attributeValue("axisHeight");
            if (StringUtils.isNotEmpty(axisHeight)) {
                categoryAxesSettings.setAxisHeight(Integer.valueOf(axisHeight));
            }

            String axisThickness = settingsElement.attributeValue("axisThickness");
            if (StringUtils.isNotEmpty(axisThickness)) {
                categoryAxesSettings.setAxisThickness(Integer.valueOf(axisThickness));
            }

            String boldLabels = settingsElement.attributeValue("boldLabels");
            if (StringUtils.isNotEmpty(boldLabels)) {
                categoryAxesSettings.setBoldLabels(Boolean.valueOf(boldLabels));
            }

            String boldPeriodBeginning = settingsElement.attributeValue("boldPeriodBeginning");
            if (StringUtils.isNotEmpty(boldPeriodBeginning)) {
                categoryAxesSettings.setBoldPeriodBeginning(Boolean.valueOf(boldPeriodBeginning));
            }

            String color = settingsElement.attributeValue("color");
            if (StringUtils.isNotEmpty(color)) {
                categoryAxesSettings.setColor(Color.valueOf(color));
            }

            String dashLength = settingsElement.attributeValue("dashLength");
            if (StringUtils.isNotEmpty(dashLength)) {
                categoryAxesSettings.setDashLength(Integer.valueOf(dashLength));
            }

            String equalSpacing = settingsElement.attributeValue("equalSpacing");
            if (StringUtils.isNotEmpty(equalSpacing)) {
                categoryAxesSettings.setEqualSpacing(Boolean.valueOf(equalSpacing));
            }

            String fillAlpha = settingsElement.attributeValue("fillAlpha");
            if (StringUtils.isNotEmpty(fillAlpha)) {
                categoryAxesSettings.setFillAlpha(Double.valueOf(fillAlpha));
            }

            String fillColor = settingsElement.attributeValue("fillColor");
            if (StringUtils.isNotEmpty(fillColor)) {
                categoryAxesSettings.setFillColor(Color.valueOf(fillColor));
            }

            String fontSize = settingsElement.attributeValue("fontSize");
            if (StringUtils.isNotEmpty(fontSize)) {
                categoryAxesSettings.setFontSize(Integer.valueOf(fontSize));
            }

            String gridAlpha = settingsElement.attributeValue("gridAlpha");
            if (StringUtils.isNotEmpty(gridAlpha)) {
                categoryAxesSettings.setGridAlpha(Double.valueOf(gridAlpha));
            }

            String gridColor = settingsElement.attributeValue("gridColor");
            if (StringUtils.isNotEmpty(gridColor)) {
                categoryAxesSettings.setGridColor(Color.valueOf(gridColor));
            }

            String gridCount = settingsElement.attributeValue("gridCount");
            if (StringUtils.isNotEmpty(gridCount)) {
                categoryAxesSettings.setGridCount(Integer.valueOf(gridCount));
            }

            String gridThickness = settingsElement.attributeValue("gridThickness");
            if (StringUtils.isNotEmpty(gridThickness)) {
                categoryAxesSettings.setGridThickness(Integer.valueOf(gridThickness));
            }

            String groupToPeriods = settingsElement.attributeValue("groupToPeriods");
            if (StringUtils.isNotEmpty(groupToPeriods)) {
                categoryAxesSettings.addGroupToPeriods(groupToPeriods.split(","));
            }

            String inside = settingsElement.attributeValue("inside");
            if (StringUtils.isNotEmpty(inside)) {
                categoryAxesSettings.setInside(Boolean.valueOf(inside));
            }

            String labelOffset = settingsElement.attributeValue("labelOffset");
            if (StringUtils.isNotEmpty(labelOffset)) {
                categoryAxesSettings.setLabelOffset(Integer.valueOf(labelOffset));
            }

            String labelRotation = settingsElement.attributeValue("labelRotation");
            if (StringUtils.isNotEmpty(labelRotation)) {
                categoryAxesSettings.setLabelRotation(Integer.valueOf(labelRotation));
            }

            String labelsEnabled = settingsElement.attributeValue("labelsEnabled");
            if (StringUtils.isNotEmpty(labelsEnabled)) {
                categoryAxesSettings.setLabelsEnabled(Boolean.valueOf(labelsEnabled));
            }

            String markPeriodChange = settingsElement.attributeValue("markPeriodChange");
            if (StringUtils.isNotEmpty(markPeriodChange)) {
                categoryAxesSettings.setMarkPeriodChange(Boolean.valueOf(markPeriodChange));
            }

            String maxSeries = settingsElement.attributeValue("maxSeries");
            if (StringUtils.isNotEmpty(maxSeries)) {
                categoryAxesSettings.setMaxSeries(Integer.valueOf(maxSeries));
            }

            String minHorizontalGap = settingsElement.attributeValue("minHorizontalGap");
            if (StringUtils.isNotEmpty(minHorizontalGap)) {
                categoryAxesSettings.setMinHorizontalGap(Integer.valueOf(minHorizontalGap));
            }

            String minorGridAlpha = settingsElement.attributeValue("minorGridAlpha");
            if (StringUtils.isNotEmpty(minorGridAlpha)) {
                categoryAxesSettings.setMinorGridAlpha(Double.valueOf(minorGridAlpha));
            }

            String minorGridEnabled = settingsElement.attributeValue("minorGridEnabled");
            if (StringUtils.isNotEmpty(minorGridEnabled)) {
                categoryAxesSettings.setMinorGridEnabled(Boolean.valueOf(minorGridEnabled));
            }

            String minPeriod = settingsElement.attributeValue("minPeriod");
            if (StringUtils.isNotEmpty(minPeriod)) {
                categoryAxesSettings.setMinPeriod(minPeriod);
            }

            String position = settingsElement.attributeValue("position");
            if (StringUtils.isNotEmpty(position)) {
                categoryAxesSettings.setPosition(CategoryAxesPosition.valueOf(position));
            }

            String startOnAxis = settingsElement.attributeValue("startOnAxis");
            if (StringUtils.isNotEmpty(startOnAxis)) {
                categoryAxesSettings.setStartOnAxis(Boolean.valueOf(startOnAxis));
            }

            String tickLength = settingsElement.attributeValue("tickLength");
            if (StringUtils.isNotEmpty(tickLength)) {
                categoryAxesSettings.setTickLength(Integer.valueOf(tickLength));
            }

            String twoLineMode = settingsElement.attributeValue("twoLineMode");
            if (StringUtils.isNotEmpty(twoLineMode)) {
                categoryAxesSettings.setTwoLineMode(Boolean.valueOf(twoLineMode));
            }

            chart.setCategoryAxesSettings(categoryAxesSettings);
        }
    }

    protected void loadPeriodSelector(StockChart chart, Element element) {
        Element periodSelectorElement = element.element("periodSelector");
        if (periodSelectorElement != null) {

            PeriodSelector periodSelector = new PeriodSelector();

            loadPeriods(periodSelector, periodSelectorElement);

            String dateFormat = periodSelectorElement.attributeValue("dateFormat");
            if (StringUtils.isNotEmpty(dateFormat)) {
                periodSelector.setDateFormat(dateFormat);
            }

            String fromText = periodSelectorElement.attributeValue("fromText");
            if (StringUtils.isNotEmpty(fromText)) {
                periodSelector.setFromText(fromText);
            }

            String hideOutOfScopePeriods = periodSelectorElement.attributeValue("hideOutOfScopePeriods");
            if (StringUtils.isNotEmpty(hideOutOfScopePeriods)) {
                periodSelector.setHideOutOfScopePeriods(Boolean.valueOf(hideOutOfScopePeriods));
            }

            String inputFieldsEnabled = periodSelectorElement.attributeValue("inputFieldsEnabled");
            if (StringUtils.isNotEmpty(inputFieldsEnabled)) {
                periodSelector.setInputFieldsEnabled(Boolean.valueOf(inputFieldsEnabled));
            }

            String inputFieldWidth = periodSelectorElement.attributeValue("inputFieldWidth");
            if (StringUtils.isNotEmpty(inputFieldWidth)) {
                periodSelector.setInputFieldWidth(Integer.valueOf(inputFieldWidth));
            }

            String periodsText = periodSelectorElement.attributeValue("periodsText");
            if (StringUtils.isNotEmpty(periodsText)) {
                periodSelector.setPeriodsText(periodsText);
            }

            String position = periodSelectorElement.attributeValue("position");
            if (StringUtils.isNotEmpty(position)) {
                periodSelector.setPosition(Position.valueOf(position));
            }

            String selectFromStart = periodSelectorElement.attributeValue("selectFromStart");
            if (StringUtils.isNotEmpty(selectFromStart)) {
                periodSelector.setSelectFromStart(Boolean.valueOf(selectFromStart));
            }

            String toText = periodSelectorElement.attributeValue("toText");
            if (StringUtils.isNotEmpty(toText)) {
                periodSelector.setToText(toText);
            }

            String width = periodSelectorElement.attributeValue("width");
            if (StringUtils.isNotEmpty(width)) {
                periodSelector.setWidth(Integer.valueOf(width));
            }

            chart.setPeriodSelector(periodSelector);
        }
    }

    protected void loadPeriod(Period period, Element periodElement) {
        String periodType = periodElement.attributeValue("period");
        if (StringUtils.isNotEmpty(periodType)) {
            period.setPeriod(PeriodType.valueOf(periodType));
        }

        String count = periodElement.attributeValue("count");
        if (StringUtils.isNotEmpty(count)) {
            period.setCount(Integer.valueOf(count));
        }

        String label = periodElement.attributeValue("label");
        if (StringUtils.isNotEmpty(label)) {
            period.setLabel(label);
        }

        String selected = periodElement.attributeValue("selected");
        if (StringUtils.isNotEmpty(selected)) {
            period.setSelected(Boolean.valueOf(selected));
        }
    }

    protected void loadPeriods(PeriodSelector periodSelector, Element periodSelectorElement) {
        Element periodsElement = periodSelectorElement.element("periods");
        if (periodsElement != null) {
            for (Object periodItem : periodsElement.elements("period")) {
                Element periodElement = (Element) periodItem;
                Period period = new Period();
                loadPeriod(period, periodElement);
                periodSelector.addPeriods(period);
            }
        }
    }

    protected void loadPanelsSettings(StockChart chart, Element element) {
        Element panelSettingsElement = element.element("panelsSettings");
        if (panelSettingsElement != null) {
            PanelsSettings panelsSettings = new PanelsSettings();

            loadMargins(panelsSettings, panelSettingsElement);
            loadStartEffect(panelsSettings, panelSettingsElement);

            String angel = panelSettingsElement.attributeValue("angle");
            if (StringUtils.isNotEmpty(angel)) {
                panelsSettings.setAngle(Integer.valueOf(angel));
            }

            String backgroundAlpha = panelSettingsElement.attributeValue("backgroundAlpha");
            if (StringUtils.isNotEmpty(backgroundAlpha)) {
                panelsSettings.setBackgroundAlpha(Double.valueOf(backgroundAlpha));
            }

            String backgroundColor = panelSettingsElement.attributeValue("backgroundColor");
            if (StringUtils.isNotEmpty(backgroundColor)) {
                panelsSettings.setBackgroundColor(Color.valueOf(backgroundColor));
            }

            String columnSpacing = panelSettingsElement.attributeValue("columnSpacing");
            if (StringUtils.isNotEmpty(columnSpacing)) {
                panelsSettings.setColumnSpacing(Integer.valueOf(columnSpacing));
            }

            String columnWidth = panelSettingsElement.attributeValue("columnWidth");
            if (StringUtils.isNotEmpty(columnWidth)) {
                panelsSettings.setColumnWidth(Integer.valueOf(columnWidth));
            }

            panelsSettings.setCreditsPosition(loadCreditsPosition(panelSettingsElement));

            String decimalSeparator = panelSettingsElement.attributeValue("decimalSeparator");
            if (StringUtils.isNotEmpty(decimalSeparator)) {
                panelsSettings.setDecimalSeparator(decimalSeparator);
            }

            String depth3D = panelSettingsElement.attributeValue("depth3D");
            if (StringUtils.isNotEmpty(depth3D)) {
                panelsSettings.setDepth3D(Integer.valueOf(depth3D));
            }

            String fontFamily = panelSettingsElement.attributeValue("fontFamily");
            if (StringUtils.isNotEmpty(fontFamily)) {
                panelsSettings.setFontFamily(fontFamily);
            }

            String fontSize = panelSettingsElement.attributeValue("fontSize");
            if (StringUtils.isNotEmpty(fontSize)) {
                panelsSettings.setFontSize(Integer.valueOf(fontSize));
            }

            String maxSelectedTime = panelSettingsElement.attributeValue("maxSelectedTime");
            if (StringUtils.isNotEmpty(maxSelectedTime)) {
                panelsSettings.setMaxSelectedTime(Long.valueOf(maxSelectedTime));
            }

            String minSelectedTime = panelSettingsElement.attributeValue("minSelectedTime");
            if (StringUtils.isNotEmpty(minSelectedTime)) {
                panelsSettings.setMinSelectedTime(Long.valueOf(minSelectedTime));
            }

            String panelSpacing = panelSettingsElement.attributeValue("panelSpacing");
            if (StringUtils.isNotEmpty(panelSpacing)) {
                panelsSettings.setPanelSpacing(Integer.valueOf(panelSpacing));
            }

            String panEventsEnabled = panelSettingsElement.attributeValue("panEventsEnabled");
            if (StringUtils.isNotEmpty(panEventsEnabled)) {
                panelsSettings.setPanEventsEnabled(Boolean.valueOf(panEventsEnabled));
            }

            String percentPrecision = panelSettingsElement.attributeValue("percentPrecision");
            if (StringUtils.isNotEmpty(percentPrecision)) {
                panelsSettings.setPercentPrecision(Double.valueOf(percentPrecision));
            }

            String plotAreaBorderAlpha = panelSettingsElement.attributeValue("plotAreaBorderAlpha");
            if (StringUtils.isNotEmpty(plotAreaBorderAlpha)) {
                panelsSettings.setPlotAreaBorderAlpha(Double.valueOf(plotAreaBorderAlpha));
            }

            String plotAreaBorderColor = panelSettingsElement.attributeValue("plotAreaBorderColor");
            if (StringUtils.isNotEmpty(plotAreaBorderColor)) {
                panelsSettings.setPlotAreaBorderColor(Color.valueOf(plotAreaBorderColor));
            }

            String plotAreaFillAlphas = panelSettingsElement.attributeValue("plotAreaFillAlphas");
            if (StringUtils.isNotEmpty(plotAreaFillAlphas)) {
                panelsSettings.setPlotAreaFillAlphas(Double.valueOf(plotAreaFillAlphas));
            }

            Element plotAreaFillColorsElement = element.element("plotAreaFillColors");
            if (plotAreaFillColorsElement != null) {
                List<Color> colors = loadColors(plotAreaFillColorsElement);
                if (CollectionUtils.isNotEmpty(colors)) {
                    panelsSettings.setPlotAreaFillColors(colors);
                }
            }

            String precision = panelSettingsElement.attributeValue("precision");
            if (StringUtils.isNotEmpty(precision)) {
                panelsSettings.setPrecision(Double.valueOf(precision));
            }

            String recalculateToPercents = panelSettingsElement.attributeValue("recalculateToPercents");
            if (StringUtils.isNotEmpty(recalculateToPercents)) {
                panelsSettings.setRecalculateToPercents(RecalculateToPercents.valueOf(recalculateToPercents));
            }

            String sequencedAnimation = panelSettingsElement.attributeValue("sequencedAnimation");
            if (StringUtils.isNotEmpty(sequencedAnimation)) {
                panelsSettings.setSequencedAnimation(Boolean.valueOf(sequencedAnimation));
            }

            String startAlpha = panelSettingsElement.attributeValue("startAlpha");
            if (StringUtils.isNotEmpty(startAlpha)) {
                panelsSettings.setStartAlpha(Double.valueOf(startAlpha));
            }

            String thousandsSeparator = panelSettingsElement.attributeValue("thousandsSeparator");
            if (StringUtils.isNotEmpty(thousandsSeparator)) {
                panelsSettings.setThousandsSeparator(thousandsSeparator);
            }

            String usePrefixes = panelSettingsElement.attributeValue("usePrefixes");
            if (StringUtils.isNotEmpty(usePrefixes)) {
                panelsSettings.setUsePrefixes(Boolean.valueOf(usePrefixes));
            }

            String zoomOutAxes = panelSettingsElement.attributeValue("zoomOutAxes");
            if (StringUtils.isNotEmpty(zoomOutAxes)) {
                panelsSettings.setZoomOutAxes(Boolean.valueOf(zoomOutAxes));
            }

            chart.setPanelsSettings(panelsSettings);
        }
    }

    protected void loadValueAxesSettings(StockChart chart, Element element) {
        Element valueAxesSettingsElement = element.element("valueAxesSettings");
        if (valueAxesSettingsElement != null) {
            ValueAxesSettings valueAxesSettings = new ValueAxesSettings();

            String autoGridCount = valueAxesSettingsElement.attributeValue("autoGridCount");
            if (StringUtils.isNotEmpty(autoGridCount)) {
                valueAxesSettings.setAutoGridCount(Boolean.valueOf(autoGridCount));
            }

            String axisAlpha = valueAxesSettingsElement.attributeValue("axisAlpha");
            if (StringUtils.isNotEmpty(axisAlpha)) {
                valueAxesSettings.setAxisAlpha(Double.valueOf(axisAlpha));
            }

            String axisColor = valueAxesSettingsElement.attributeValue("axisColor");
            if (StringUtils.isNotEmpty(axisColor)) {
                valueAxesSettings.setAxisColor(Color.valueOf(axisColor));
            }

            String axisThickness = valueAxesSettingsElement.attributeValue("axisThickness");
            if (StringUtils.isNotEmpty(axisThickness)) {
                valueAxesSettings.setAxisThickness(Integer.valueOf(axisThickness));
            }

            String color = valueAxesSettingsElement.attributeValue("color");
            if (StringUtils.isNotEmpty(color)) {
                valueAxesSettings.setColor(Color.valueOf(color));
            }

            String dashLength = valueAxesSettingsElement.attributeValue("dashLength");
            if (StringUtils.isNotEmpty(dashLength)) {
                valueAxesSettings.setDashLength(Integer.valueOf(dashLength));
            }

            String fillAlpha = valueAxesSettingsElement.attributeValue("fillAlpha");
            if (StringUtils.isNotEmpty(fillAlpha)) {
                valueAxesSettings.setFillAlpha(Double.valueOf(fillAlpha));
            }

            String fillColor = valueAxesSettingsElement.attributeValue("fillColor");
            if (StringUtils.isNotEmpty(fillColor)) {
                valueAxesSettings.setFillColor(Color.valueOf(fillColor));
            }

            String gridAlpha = valueAxesSettingsElement.attributeValue("gridAlpha");
            if (StringUtils.isNotEmpty(gridAlpha)) {
                valueAxesSettings.setGridAlpha(Double.valueOf(gridAlpha));
            }

            String gridColor = valueAxesSettingsElement.attributeValue("gridColor");
            if (StringUtils.isNotEmpty(gridColor)) {
                valueAxesSettings.setGridColor(Color.valueOf(gridColor));
            }

            String gridCount = valueAxesSettingsElement.attributeValue("gridCount");
            if (StringUtils.isNotEmpty(gridCount)) {
                valueAxesSettings.setGridCount(Integer.valueOf(gridCount));
            }

            String gridThickness = valueAxesSettingsElement.attributeValue("gridThickness");
            if (StringUtils.isNotEmpty(gridThickness)) {
                valueAxesSettings.setGridThickness(Integer.valueOf(gridThickness));
            }

            String includeGuidesInMinMax = valueAxesSettingsElement.attributeValue("includeGuidesInMinMax");
            if (StringUtils.isNotEmpty(includeGuidesInMinMax)) {
                valueAxesSettings.setIncludeGuidesInMinMax(Boolean.valueOf(includeGuidesInMinMax));
            }

            String includeHidden = valueAxesSettingsElement.attributeValue("includeHidden");
            if (StringUtils.isNotEmpty(includeHidden)) {
                valueAxesSettings.setIncludeHidden(Boolean.valueOf(includeHidden));
            }

            String inside = valueAxesSettingsElement.attributeValue("inside");
            if (StringUtils.isNotEmpty(inside)) {
                valueAxesSettings.setInside(Boolean.valueOf(inside));
            }

            String integersOnly = valueAxesSettingsElement.attributeValue("integersOnly");
            if (StringUtils.isNotEmpty(integersOnly)) {
                valueAxesSettings.setIntegersOnly(Boolean.valueOf(integersOnly));
            }

            String labelFrequency = valueAxesSettingsElement.attributeValue("labelFrequency");
            if (StringUtils.isNotEmpty(labelFrequency)) {
                valueAxesSettings.setLabelFrequency(Double.valueOf(labelFrequency));
            }

            String labelOffset = valueAxesSettingsElement.attributeValue("labelOffset");
            if (StringUtils.isNotEmpty(labelOffset)) {
                valueAxesSettings.setLabelOffset(Integer.valueOf(labelOffset));
            }

            String labelsEnabled = valueAxesSettingsElement.attributeValue("labelsEnabled");
            if (StringUtils.isNotEmpty(labelsEnabled)) {
                valueAxesSettings.setLabelsEnabled(Boolean.valueOf(labelsEnabled));
            }

            String logarithmic = valueAxesSettingsElement.attributeValue("logarithmic");
            if (StringUtils.isNotEmpty(logarithmic)) {
                valueAxesSettings.setLogarithmic(Boolean.valueOf(logarithmic));
            }

            String maximum = valueAxesSettingsElement.attributeValue("maximum");
            if (StringUtils.isNotEmpty(maximum)) {
                valueAxesSettings.setMaximum(Double.valueOf(maximum));
            }

            String minimum = valueAxesSettingsElement.attributeValue("minimum");
            if (StringUtils.isNotEmpty(minimum)) {
                valueAxesSettings.setMinimum(Double.valueOf(minimum));
            }

            String minMaxMultiplier = valueAxesSettingsElement.attributeValue("minMaxMultiplier");
            if (StringUtils.isNotEmpty(minMaxMultiplier)) {
                valueAxesSettings.setMinMaxMultiplier(Double.valueOf(minMaxMultiplier));
            }

            String minorGridAlpha = valueAxesSettingsElement.attributeValue("minorGridAlpha");
            if (StringUtils.isNotEmpty(minorGridAlpha)) {
                valueAxesSettings.setMinorGridAlpha(Double.valueOf(minorGridAlpha));
            }

            String minorGridEnabled = valueAxesSettingsElement.attributeValue("minorGridEnabled");
            if (StringUtils.isNotEmpty(minorGridEnabled)) {
                valueAxesSettings.setMinorGridEnabled(Boolean.valueOf(minorGridEnabled));
            }

            String minVerticalGap = valueAxesSettingsElement.attributeValue("minVerticalGap");
            if (StringUtils.isNotEmpty(minVerticalGap)) {
                valueAxesSettings.setMinVerticalGap(Integer.valueOf(minVerticalGap));
            }

            String offset = valueAxesSettingsElement.attributeValue("offset");
            if (StringUtils.isNotEmpty(offset)) {
                valueAxesSettings.setOffset(Integer.valueOf(offset));
            }

            String position = valueAxesSettingsElement.attributeValue("position");
            if (StringUtils.isNotEmpty(position)) {
                valueAxesSettings.setPosition(ValueAxisPosition.valueOf(position));
            }

            String precision = valueAxesSettingsElement.attributeValue("precision");
            if (StringUtils.isNotEmpty(precision)) {
                valueAxesSettings.setPrecision(Integer.valueOf(precision));
            }

            String reversed = valueAxesSettingsElement.attributeValue("reversed");
            if (StringUtils.isNotEmpty(reversed)) {
                valueAxesSettings.setReversed(Boolean.valueOf(reversed));
            }

            String showFirstLabel = valueAxesSettingsElement.attributeValue("showFirstLabel");
            if (StringUtils.isNotEmpty(showFirstLabel)) {
                valueAxesSettings.setShowFirstLabel(Boolean.valueOf(showFirstLabel));
            }

            String showLastLabel = valueAxesSettingsElement.attributeValue("showLastLabel");
            if (StringUtils.isNotEmpty(showLastLabel)) {
                valueAxesSettings.setShowLastLabel(Boolean.valueOf(showLastLabel));
            }

            String stackType = valueAxesSettingsElement.attributeValue("stackType");
            if (StringUtils.isNotEmpty(stackType)) {
                valueAxesSettings.setStackType(StackType.valueOf(stackType));
            }

            String strictMinMax = valueAxesSettingsElement.attributeValue("strictMinMax");
            if (StringUtils.isNotEmpty(strictMinMax)) {
                valueAxesSettings.setStrictMinMax(Boolean.valueOf(strictMinMax));
            }

            String tickLength = valueAxesSettingsElement.attributeValue("tickLength");
            if (StringUtils.isNotEmpty(tickLength)) {
                valueAxesSettings.setTickLength(Integer.valueOf(tickLength));
            }

            String unit = valueAxesSettingsElement.attributeValue("unit");
            if (StringUtils.isNotEmpty(unit)) {
                valueAxesSettings.setUnit(unit);
            }

            String unitPosition = valueAxesSettingsElement.attributeValue("unitPosition");
            if (StringUtils.isNotEmpty(unitPosition)) {
                valueAxesSettings.setUnitPosition(UnitPosition.valueOf(unitPosition));
            }

            chart.setValueAxesSettings(valueAxesSettings);
        }
    }

    protected void loadStockEventsSettings(StockChart chart, Element element) {
        Element stockEventsSettingsElement = element.element("stockEventsSettings");
        if (stockEventsSettingsElement != null) {
            StockEventsSettings stockEventsSettings = new StockEventsSettings();

            String backgroundAlpha = stockEventsSettingsElement.attributeValue("backgroundAlpha");
            if (StringUtils.isNotEmpty(backgroundAlpha)) {
                stockEventsSettings.setBackgroundAlpha(Double.valueOf(backgroundAlpha));
            }

            String backgroundColor = stockEventsSettingsElement.attributeValue("backgroundColor");
            if (StringUtils.isNotEmpty(backgroundColor)) {
                stockEventsSettings.setBackgroundColor(Color.valueOf(backgroundColor));
            }

            String balloonColor = stockEventsSettingsElement.attributeValue("balloonColor");
            if (StringUtils.isNotEmpty(balloonColor)) {
                stockEventsSettings.setBalloonColor(Color.valueOf(balloonColor));
            }

            String borderAlpha = stockEventsSettingsElement.attributeValue("borderAlpha");
            if (StringUtils.isNotEmpty(borderAlpha)) {
                stockEventsSettings.setBorderAlpha(Double.valueOf(borderAlpha));
            }

            String borderColor = stockEventsSettingsElement.attributeValue("borderColor");
            if (StringUtils.isNotEmpty(borderColor)) {
                stockEventsSettings.setBorderColor(Color.valueOf(borderColor));
            }

            String rollOverColor = stockEventsSettingsElement.attributeValue("rollOverColor");
            if (StringUtils.isNotEmpty(rollOverColor)) {
                stockEventsSettings.setRollOverColor(Color.valueOf(rollOverColor));
            }

            String showAt = stockEventsSettingsElement.attributeValue("showAt");
            if (StringUtils.isNotEmpty(showAt)) {
                stockEventsSettings.setShowAt(showAt);
            }

            String type = stockEventsSettingsElement.attributeValue("type");
            if (StringUtils.isNotEmpty(type)) {
                stockEventsSettings.setType(StockEventType.valueOf(type));
            }

            chart.setStockEventsSettings(stockEventsSettings);
        }
    }

    protected void loadStockPanel(StockPanel chart, Element element) {
        loadBaseProperties(chart, element);
        loadCoordinateProperties(chart, element);
        loadRectangularProperties(chart, element);
        loadSeriesBasedProperties(chart, element);

        loadStockGraphs(chart, element);
        loadStockLegend(chart, element);

        String id = element.attributeValue("id");
        if (StringUtils.isNotEmpty(id)) {
            chart.setId(id);
        }

        String allowTurningOff = element.attributeValue("allowTurningOff");
        if (StringUtils.isNotEmpty(allowTurningOff)) {
            chart.setAllowTurningOff(Boolean.valueOf(allowTurningOff));
        }

        String drawingIconsEnabled = element.attributeValue("drawingIconsEnabled");
        if (StringUtils.isNotEmpty(drawingIconsEnabled)) {
            chart.setDrawingIconsEnabled(Boolean.valueOf(drawingIconsEnabled));
        }

        Element drawOnAxisElement = element.element("drawOnAxis");
        if (drawOnAxisElement != null) {
            chart.setDrawOnAxis(loadValueAxis(drawOnAxisElement));
        }

        String eraseAll = element.attributeValue("eraseAll");
        if (StringUtils.isNotEmpty(eraseAll)) {
            chart.setEraseAll(Boolean.valueOf(eraseAll));
        }

        String iconSize = element.attributeValue("iconSize");
        if (StringUtils.isNotEmpty(iconSize)) {
            chart.setIconSize(Integer.valueOf(iconSize));
        }

        String percentHeight = element.attributeValue("percentHeight");
        if (StringUtils.isNotEmpty(percentHeight)) {
            chart.setPercentHeight(Integer.valueOf(percentHeight));
        }

        String recalculateFromDate = element.attributeValue("recalculateFromDate");
        if (StringUtils.isNotEmpty(recalculateFromDate)) {
            chart.setRecalculateFromDate(loadDate(recalculateFromDate));
        }

        String recalculateToPercents = element.attributeValue("recalculateToPercents");
        if (StringUtils.isNotEmpty(recalculateToPercents)) {
            chart.setRecalculateToPercents(recalculateToPercents);
        }

        String showCategoryAxis = element.attributeValue("showCategoryAxis");
        if (StringUtils.isNotEmpty(showCategoryAxis)) {
            chart.setShowCategoryAxis(Boolean.valueOf(showCategoryAxis));
        }

        String showComparedOnTop = element.attributeValue("showComparedOnTop");
        if (StringUtils.isNotEmpty(showComparedOnTop)) {
            chart.setShowComparedOnTop(Boolean.valueOf(showComparedOnTop));
        }

        String title = element.attributeValue("title");
        if (StringUtils.isNotEmpty(title)) {
            chart.setTitle(title);
        }

        String trendLineAlpha = element.attributeValue("trendLineAlpha");
        if (StringUtils.isNotEmpty(trendLineAlpha)) {
            chart.setTrendLineAlpha(Double.valueOf(trendLineAlpha));
        }

        String trendLineColor = element.attributeValue("trendLineColor");
        if (StringUtils.isNotEmpty(trendLineColor)) {
            chart.setTrendLineColor(Color.valueOf(trendLineColor));
        }

        String trendLineDashLength = element.attributeValue("trendLineDashLength");
        if (StringUtils.isNotEmpty(trendLineDashLength)) {
            chart.setTrendLineDashLength(Integer.valueOf(trendLineDashLength));
        }

        String trendLineThickness = element.attributeValue("trendLineThickness");
        if (StringUtils.isNotEmpty(trendLineThickness)) {
            chart.setTrendLineThickness(Integer.valueOf(trendLineThickness));
        }

        String bezierX = element.attributeValue("bezierX");
        if (StringUtils.isNotEmpty(bezierX)) {
            chart.setBezierX(Integer.valueOf(bezierX));
        }

        String bezierY = element.attributeValue("bezierY");
        if (StringUtils.isNotEmpty(bezierY)) {
            chart.setBezierY(Integer.valueOf(bezierY));
        }
    }

    protected void loadStockLegend(StockPanel chart, Element element) {
        Element stockLegendElement = element.element("stockLegend");
        if (stockLegendElement != null) {
            StockLegend stockLegend = new StockLegend();
            loadLegend(stockLegend, stockLegendElement);

            String periodValueTextComparing = stockLegendElement.attributeValue("periodValueTextComparing");
            if (StringUtils.isNotEmpty(periodValueTextComparing)) {
                stockLegend.setPeriodValueTextComparing(periodValueTextComparing);
            }

            String periodValueTextRegular = stockLegendElement.attributeValue("periodValueTextRegular");
            if (StringUtils.isNotEmpty(periodValueTextRegular)) {
                stockLegend.setPeriodValueTextRegular(periodValueTextRegular);
            }

            String valueTextComparing = stockLegendElement.attributeValue("valueTextComparing");
            if (StringUtils.isNotEmpty(valueTextComparing)) {
                stockLegend.setValueTextComparing(valueTextComparing);
            }

            String valueTextRegular = stockLegendElement.attributeValue("valueTextRegular");
            if (StringUtils.isNotEmpty(valueTextRegular)) {
                stockLegend.setValueTextRegular(valueTextRegular);
            }

            chart.setStockLegend(stockLegend);
        }
    }

    protected void loadStockGraphs(StockPanel chart, Element element) {
        Element stockGraphsElement = element.element("stockGraphs");
        if (stockGraphsElement != null) {
            for (Object stockGraphItem : stockGraphsElement.elements("stockGraph")) {
                Element stockGraphElement = (Element) stockGraphItem;
                StockGraph stockGraph = new StockGraph();
                loadStockGraph(stockGraph, stockGraphElement);
                chart.addStockGraphs(stockGraph);
            }
        }
    }

    protected void loadStockGraph(StockGraph stockGraph, Element stockGraphElement) {
        loadGraph(stockGraph, stockGraphElement);

        String comparable = stockGraphElement.attributeValue("comparable");
        if (StringUtils.isNotEmpty(comparable)) {
            stockGraph.setComparable(Boolean.valueOf(comparable));
        }

        String compareField = stockGraphElement.attributeValue("compareField");
        if (StringUtils.isNotEmpty(compareField)) {
            stockGraph.setCompareField(compareField);
        }

        String compareFromStart = stockGraphElement.attributeValue("compareFromStart");
        if (StringUtils.isNotEmpty(compareFromStart)) {
            stockGraph.setCompareFromStart(Boolean.valueOf(compareFromStart));
        }

        Element compareGraphElement = stockGraphElement.element("compareGraph");
        if (compareGraphElement != null) {
            Graph compareGraph = new Graph();
            loadGraph(compareGraph, compareGraphElement);
            stockGraph.setCompareGraph(compareGraph);
        }

        String compareGraphBalloonColor = stockGraphElement.attributeValue("compareGraphBalloonColor");
        if (StringUtils.isNotEmpty(compareGraphBalloonColor)) {
            stockGraph.setCompareGraphBalloonColor(Color.valueOf(compareGraphBalloonColor));
        }

        String compareGraphBalloonFunction = stockGraphElement.elementText("compareGraphBalloonFunction");
        if (StringUtils.isNotBlank(compareGraphBalloonFunction)) {
            stockGraph.setCompareGraphBalloonFunction(new JsFunction(compareGraphBalloonFunction));
        }

        String compareGraphBalloonText = stockGraphElement.attributeValue("compareGraphBalloonText");
        if (StringUtils.isNotEmpty(compareGraphBalloonText)) {
            stockGraph.setCompareGraphBalloonText(compareGraphBalloonText);
        }

        String compareGraphBullet = stockGraphElement.attributeValue("compareGraphBullet");
        if (StringUtils.isNotEmpty(compareGraphBullet)) {
            stockGraph.setCompareGraphBullet(compareGraphBullet);
        }

        String compareGraphBulletBorderAlpha = stockGraphElement.attributeValue("compareGraphBulletBorderAlpha");
        if (StringUtils.isNotEmpty(compareGraphBulletBorderAlpha)) {
            stockGraph.setCompareGraphBulletBorderAlpha(Double.valueOf(compareGraphBulletBorderAlpha));
        }

        String compareGraphBulletBorderColor = stockGraphElement.attributeValue("compareGraphBulletBorderColor");
        if (StringUtils.isNotEmpty(compareGraphBulletBorderColor)) {
            stockGraph.setCompareGraphBulletBorderColor(Color.valueOf(compareGraphBulletBorderColor));
        }

        String compareGraphBulletBorderThickness = stockGraphElement.attributeValue("compareGraphBulletBorderThickness");
        if (StringUtils.isNotEmpty(compareGraphBulletBorderThickness)) {
            stockGraph.setCompareGraphBulletBorderThickness(Integer.valueOf(compareGraphBulletBorderThickness));
        }

        String compareGraphBulletColor = stockGraphElement.attributeValue("compareGraphBulletColor");
        if (StringUtils.isNotEmpty(compareGraphBulletColor)) {
            stockGraph.setCompareGraphBulletColor(Color.valueOf(compareGraphBulletColor));
        }

        String compareGraphBulletSize = stockGraphElement.attributeValue("compareGraphBulletSize");
        if (StringUtils.isNotEmpty(compareGraphBulletSize)) {
            stockGraph.setCompareGraphBulletSize(Integer.valueOf(compareGraphBulletSize));
        }

        String compareGraphCornerRadiusTop = stockGraphElement.attributeValue("compareGraphCornerRadiusTop");
        if (StringUtils.isNotEmpty(compareGraphCornerRadiusTop)) {
            stockGraph.setCompareGraphCornerRadiusTop(Integer.valueOf(compareGraphCornerRadiusTop));
        }

        String compareGraphDashLength = stockGraphElement.attributeValue("compareGraphDashLength");
        if (StringUtils.isNotEmpty(compareGraphDashLength)) {
            stockGraph.setCompareGraphDashLength(Integer.valueOf(compareGraphDashLength));
        }

        String compareGraphFillAlphas = stockGraphElement.attributeValue("compareGraphFillAlphas");
        if (StringUtils.isNotEmpty(compareGraphFillAlphas)) {
            stockGraph.setCompareGraphFillAlphas(Double.valueOf(compareGraphFillAlphas));
        }

        Element compareGraphFillColors = stockGraphElement.element("compareGraphFillColors");
        if (compareGraphFillColors != null) {
            List<Color> colors = loadColors(compareGraphFillColors);
            if (CollectionUtils.isNotEmpty(colors)) {
                stockGraph.setCompareGraphFillColors(colors);
            }
        }

        String compareGraphLineAlpha = stockGraphElement.attributeValue("compareGraphLineAlpha");
        if (StringUtils.isNotEmpty(compareGraphLineAlpha)) {
            stockGraph.setCompareGraphLineAlpha(Double.valueOf(compareGraphLineAlpha));
        }

        String compareGraphLineColor = stockGraphElement.attributeValue("compareGraphLineColor");
        if (StringUtils.isNotEmpty(compareGraphLineColor)) {
            stockGraph.setCompareGraphLineColor(Color.valueOf(compareGraphLineColor));
        }

        String compareGraphLineThickness = stockGraphElement.attributeValue("compareGraphLineThickness");
        if (StringUtils.isNotEmpty(compareGraphLineThickness)) {
            stockGraph.setCompareGraphLineThickness(Integer.valueOf(compareGraphLineThickness));
        }

        String compareGraphType = stockGraphElement.attributeValue("compareGraphType");
        if (StringUtils.isNotEmpty(compareGraphType)) {
            stockGraph.setCompareGraphType(GraphType.valueOf(compareGraphType));
        }

        String compareGraphVisibleInLegend = stockGraphElement.attributeValue("compareGraphVisibleInLegend");
        if (StringUtils.isNotEmpty(compareGraphVisibleInLegend)) {
            stockGraph.setCompareGraphVisibleInLegend(Boolean.valueOf(compareGraphVisibleInLegend));
        }

        String periodValue = stockGraphElement.attributeValue("periodValue");
        if (StringUtils.isNotEmpty(periodValue)) {
            stockGraph.setPeriodValue(StockGraphValue.valueOf(periodValue));
        }

        String recalculateValue = stockGraphElement.attributeValue("recalculateValue");
        if (StringUtils.isNotEmpty(recalculateValue)) {
            stockGraph.setRecalculateValue(StockGraphValue.valueOf(recalculateValue));
        }

        String showEventsOnComparedGraphs = stockGraphElement.attributeValue("showEventsOnComparedGraphs");
        if (StringUtils.isNotEmpty(showEventsOnComparedGraphs)) {
            stockGraph.setShowEventsOnComparedGraphs(Boolean.valueOf(showEventsOnComparedGraphs));
        }

        String useDataSetColors = stockGraphElement.attributeValue("useDataSetColors");
        if (StringUtils.isNotEmpty(useDataSetColors)) {
            stockGraph.setUseDataSetColors(Boolean.valueOf(useDataSetColors));
        }
    }
}