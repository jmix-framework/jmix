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

import com.google.common.base.Splitter;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.Component;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.ui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.charts.model.*;
import io.jmix.charts.model.animation.AnimationEffect;
import io.jmix.charts.model.animation.HasStartEffect;
import io.jmix.charts.model.axis.*;
import io.jmix.charts.model.balloon.Balloon;
import io.jmix.charts.model.balloon.PointerOrientation;
import io.jmix.charts.model.chart.ChartModel;
import io.jmix.charts.model.chart.CoordinateChartModel;
import io.jmix.charts.model.chart.RectangularChartModel;
import io.jmix.charts.model.chart.SeriesBasedChartModel;
import io.jmix.charts.model.cursor.Cursor;
import io.jmix.charts.model.cursor.CursorPosition;
import io.jmix.charts.model.date.DateFormat;
import io.jmix.charts.model.date.DatePeriod;
import io.jmix.charts.model.date.Duration;
import io.jmix.charts.model.export.*;
import io.jmix.charts.model.graph.*;
import io.jmix.charts.model.label.Label;
import io.jmix.charts.model.legend.*;
import io.jmix.charts.model.settings.ChartTheme;
import io.jmix.charts.model.settings.CreditsPosition;
import io.jmix.charts.model.trendline.Image;
import io.jmix.charts.model.trendline.TrendLine;
import io.jmix.charts.model.JsFunction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public abstract class ChartModelLoader<C extends Component> extends AbstractComponentLoader<C> {

    protected static final String CONFIG_DATE_FORMAT = "yyyy-MM-dd";
    protected static final String CONFIG_DATETIME_FORMAT = "yyyy-MM-dd HH:mm";

    protected static final FastDateFormat CONFIG_DATE_FORMATTER = FastDateFormat.getInstance(CONFIG_DATE_FORMAT);
    protected static final FastDateFormat CONFIG_DATETIME_FORMATTER = FastDateFormat.getInstance(CONFIG_DATETIME_FORMAT);

    @Override
    protected void loadWidth(Component component, Element element) {
        final String width = element.attributeValue("width");
        if ("auto".equalsIgnoreCase(width)) {
            component.setWidth("640px");
        } else if (!StringUtils.isBlank(width)) {
            component.setWidth(width);
        } else {
            component.setWidth("640px");
        }
    }

    @Override
    protected void loadHeight(Component component, Element element) {
        final String height = element.attributeValue("height");
        if ("auto".equalsIgnoreCase(height)) {
            component.setHeight("480px");
        } else if (!StringUtils.isBlank(height)) {
            component.setHeight(height);
        } else {
            component.setHeight("480px");
        }
    }

    protected MapDataItem loadDataItem(Element property, MapDataItem dataItem) {
        String name = property.attributeValue("name");
        String value = property.attributeValue("value");
        String type = property.attributeValue("type");

        if (StringUtils.isEmpty(name)) {
            throw new GuiDevelopmentException(
                    "'name' attribute does not exist",
                    context, "Chart ID", resultComponent.getId());
        }
        if (StringUtils.isEmpty(value)) {
            throw new GuiDevelopmentException(
                    "'value' attribute does not exist",
                    context, "Chart ID", resultComponent.getId());
        }

        if (type == null) {
            type = "string";
        }

        switch (type) {
            case "int":
                dataItem.add(name, Integer.parseInt(value));
                break;
            case "double":
                dataItem.add(name, Double.parseDouble(value));
                break;
            case "date":
                dataItem.add(name, loadDate(value));
                break;
            case "datetime":
                dataItem.add(name, parseDateTime(value));
                break;
            case "string":
                dataItem.add(name, value);
                break;
            case "long":
                dataItem.add(name, Long.parseLong(value));
                break;
            case "boolean":
                dataItem.add(name, Boolean.valueOf(value));
                break;
            case "uuid":
                dataItem.add(name, UUID.fromString(value));
                break;
            case "decimal":
                dataItem.add(name, new BigDecimal(value));
                break;
            default:
                dataItem.add(name, value);
                break;
        }

        return dataItem;
    }

    protected Date parseDateTime(String value) {
        FastDateFormat rangeDF;
        if (value.length() == 10) {
            rangeDF = CONFIG_DATE_FORMATTER;
        } else {
            rangeDF = CONFIG_DATETIME_FORMATTER;
        }
        try {
            return rangeDF.parse(value);
        } catch (ParseException e) {
            throw new GuiDevelopmentException(
                    "'value' parsing error for chart: " +
                            value, context, "Chart ID", resultComponent.getId());
        }
    }

    protected Balloon loadBalloon(Element balloonElement) {
        Balloon balloon = new Balloon();

        String adjustBorderColor = balloonElement.attributeValue("adjustBorderColor");
        if (StringUtils.isNotEmpty(adjustBorderColor)) {
            balloon.setAdjustBorderColor(Boolean.valueOf(adjustBorderColor));
        }

        String animationDuration = balloonElement.attributeValue("animationDuration");
        if (StringUtils.isNotEmpty(animationDuration)) {
            balloon.setAnimationDuration(Double.valueOf(animationDuration));
        }

        String borderAlpha = balloonElement.attributeValue("borderAlpha");
        if (StringUtils.isNotEmpty(borderAlpha)) {
            balloon.setBorderAlpha(Double.valueOf(borderAlpha));
        }

        String borderColor = balloonElement.attributeValue("borderColor");
        if (StringUtils.isNotEmpty(borderColor)) {
            balloon.setBorderColor(Color.valueOf(borderColor));
        }

        String borderThickness = balloonElement.attributeValue("borderThickness");
        if (StringUtils.isNotEmpty(borderThickness)) {
            balloon.setBorderThickness(Integer.valueOf(borderThickness));
        }

        String color = balloonElement.attributeValue("color");
        if (StringUtils.isNotEmpty(color)) {
            balloon.setColor(Color.valueOf(color));
        }

        String cornerRadius = balloonElement.attributeValue("cornerRadius");
        if (StringUtils.isNotEmpty(cornerRadius)) {
            balloon.setCornerRadius(Integer.valueOf(cornerRadius));
        }

        String disableMouseEvents = balloonElement.attributeValue("disableMouseEvents");
        if (StringUtils.isNotEmpty(disableMouseEvents)) {
            balloon.setDisableMouseEvents(Boolean.valueOf(disableMouseEvents));
        }

        String drop = balloonElement.attributeValue("drop");
        if (StringUtils.isNotEmpty(drop)) {
            balloon.setDrop(Boolean.valueOf(drop));
        }

        String enabled = balloonElement.attributeValue("enabled");
        if (StringUtils.isNotEmpty(enabled)) {
            balloon.setEnabled(Boolean.valueOf(enabled));
        }

        String fadeOutDuration = balloonElement.attributeValue("fadeOutDuration");
        if (StringUtils.isNotEmpty(fadeOutDuration)) {
            balloon.setFadeOutDuration(Double.valueOf(fadeOutDuration));
        }

        String fillAlpha = balloonElement.attributeValue("fillAlpha");
        if (StringUtils.isNotEmpty(fillAlpha)) {
            balloon.setFillAlpha(Double.valueOf(fillAlpha));
        }

        String fillColor = balloonElement.attributeValue("fillColor");
        if (StringUtils.isNotEmpty(fillColor)) {
            balloon.setFillColor(Color.valueOf(fillColor));
        }

        String fixedPosition = balloonElement.attributeValue("fixedPosition");
        if (StringUtils.isNotEmpty(fixedPosition)) {
            balloon.setFixedPosition(Boolean.valueOf(fixedPosition));
        }

        String fontSize = balloonElement.attributeValue("fontSize");
        if (StringUtils.isNotEmpty(fontSize)) {
            balloon.setFontSize(Integer.valueOf(fontSize));
        }

        String horizontalPadding = balloonElement.attributeValue("horizontalPadding");
        if (StringUtils.isNotEmpty(horizontalPadding)) {
            balloon.setHorizontalPadding(Integer.valueOf(horizontalPadding));
        }

        String maxWidth = balloonElement.attributeValue("maxWidth");
        if (StringUtils.isNotEmpty(maxWidth)) {
            balloon.setMaxWidth(Integer.valueOf(maxWidth));
        }

        String offsetX = balloonElement.attributeValue("offsetX");
        if (StringUtils.isNotEmpty(offsetX)) {
            balloon.setOffsetX(Integer.valueOf(offsetX));
        }

        String offsetY = balloonElement.attributeValue("offsetY");
        if (StringUtils.isNotEmpty(offsetY)) {
            balloon.setOffsetY(Integer.valueOf(offsetY));
        }

        String pointerOrientation = balloonElement.attributeValue("pointerOrientation");
        if (StringUtils.isNotEmpty(pointerOrientation)) {
            balloon.setPointerOrientation(PointerOrientation.valueOf(pointerOrientation));
        }

        String pointerWidth = balloonElement.attributeValue("pointerWidth");
        if (StringUtils.isNotEmpty(pointerWidth)) {
            balloon.setPointerWidth(Integer.valueOf(pointerWidth));
        }

        String shadowAlpha = balloonElement.attributeValue("shadowAlpha");
        if (StringUtils.isNotEmpty(shadowAlpha)) {
            balloon.setShadowAlpha(Double.valueOf(shadowAlpha));
        }

        String shadowColor = balloonElement.attributeValue("shadowColor");
        if (StringUtils.isNotEmpty(shadowColor)) {
            balloon.setShadowColor(Color.valueOf(shadowColor));
        }

        String showBullet = balloonElement.attributeValue("showBullet");
        if (StringUtils.isNotEmpty(showBullet)) {
            balloon.setShowBullet(Boolean.valueOf(showBullet));
        }

        String textAlign = balloonElement.attributeValue("textAlign");
        if (StringUtils.isNotEmpty(textAlign)) {
            balloon.setTextAlign(Align.valueOf(textAlign));
        }

        String verticalPadding = balloonElement.attributeValue("verticalPadding");
        if (StringUtils.isNotEmpty(verticalPadding)) {
            balloon.setVerticalPadding(Integer.valueOf(verticalPadding));
        }

        return balloon;
    }

    protected ExportMenuItem loadExportMenuItem(Element menuItemElement) {
        ExportMenuItem menuItem = new ExportMenuItem();

        String format = menuItemElement.attributeValue("format");
        if (StringUtils.isNotBlank(format)) {
            menuItem.setFormat(ExportFormat.valueOf(format));
        }

        String title = menuItemElement.attributeValue("title");
        if (StringUtils.isNotBlank(title)) {
            menuItem.setTitle(loadResourceString(title));
        }

        String label = menuItemElement.attributeValue("label");
        if (StringUtils.isNotBlank(label)) {
            menuItem.setLabel(loadResourceString(label));
        }

        String fileName = menuItemElement.attributeValue("fileName");
        if (StringUtils.isNotBlank(fileName)) {
            menuItem.setFileName(loadResourceString(fileName));
        }

        String pageOrientation = menuItemElement.attributeValue("pageOrientation");
        if (StringUtils.isNotBlank(pageOrientation)) {
            menuItem.setPageOrientation(PageOrientation.valueOf(pageOrientation));
        }

        String pageOrigin = menuItemElement.attributeValue("pageOrigin");
        if (StringUtils.isNotEmpty(pageOrigin)) {
            menuItem.setPageOrigin(Boolean.valueOf(pageOrigin));
        }

        String quality = menuItemElement.attributeValue("quality");
        if (StringUtils.isNotEmpty(quality)) {
            menuItem.setQuality(Double.parseDouble(quality));
        }

        String multiplier = menuItemElement.attributeValue("multiplier");
        if (StringUtils.isNotEmpty(multiplier)) {
            menuItem.setMultiplier(Double.parseDouble(multiplier));
        }

        String delay = menuItemElement.attributeValue("delay");
        if (StringUtils.isNotEmpty(delay)) {
            menuItem.setDelay(Double.parseDouble(delay));
        }

        String lossless = menuItemElement.attributeValue("lossless");
        if (StringUtils.isNotEmpty(lossless)) {
            menuItem.setLossless(Boolean.valueOf(lossless));
        }

        String delimiter = menuItemElement.attributeValue("delimiter");
        if (StringUtils.isNotEmpty(delimiter)) {
            menuItem.setDelimiter(delimiter);
        }

        String quotes = menuItemElement.attributeValue("quotes");
        if (StringUtils.isNotEmpty(quotes)) {
            menuItem.setQuotes(Boolean.valueOf(quotes));
        }

        String escape = menuItemElement.attributeValue("escape");
        if (StringUtils.isNotEmpty(escape)) {
            menuItem.setEscape(Boolean.valueOf(escape));
        }

        String withHeader = menuItemElement.attributeValue("withHeader");
        if (StringUtils.isNotEmpty(withHeader)) {
            menuItem.setWithHeader(Boolean.valueOf(withHeader));
        }

        String stringify = menuItemElement.attributeValue("stringify");
        if (StringUtils.isNotEmpty(stringify)) {
            menuItem.setStringify(Boolean.valueOf(stringify));
        }

        String dateFormat = menuItemElement.attributeValue("dateFormat");
        if (StringUtils.isNotEmpty(dateFormat)) {
            menuItem.setDateFormat(dateFormat);
        }

        String pageSize = menuItemElement.attributeValue("pageSize");
        if (StringUtils.isNotEmpty(pageSize)) {
            menuItem.setPageSize(PageSize.valueFromId(pageSize));
        }

        return menuItem;
    }

    protected void loadExportMenu(Export export, Element exportElement) {
        Element menuElement = exportElement.element("menu");
        if (menuElement != null) {
            List<ExportMenuItem> items = new ArrayList<>();

            for (Object menuElementItem : menuElement.elements("item")) {
                Element menuItemElement = (Element) menuElementItem;
                items.add(loadExportMenuItem(menuItemElement));
            }

            export.setMenu(items);
        }
    }

    protected void loadExportLibs(ExportLibs libs, Element libsElement) {
        String path = libsElement.attributeValue("path");
        if (StringUtils.isNotEmpty(path)) {
            libs.setPath(path);
        }
    }

    protected Export loadExport(Element exportElement) {
        Export export = new Export();

        loadExportMenu(export, exportElement);

        String enabled = exportElement.attributeValue("enabled");
        if (StringUtils.isNotEmpty(enabled)) {
            export.setEnabled(Boolean.valueOf(enabled));
        }

        Element libsElement = exportElement.element("libs");
        if (libsElement != null) {
            ExportLibs libs = new ExportLibs();
            loadExportLibs(libs, libsElement);
            export.setLibs(libs);
        }

        String backgroundColor = exportElement.attributeValue("backgroundColor");
        if (StringUtils.isNotEmpty(backgroundColor)) {
            export.setBackgroundColor(Color.valueOf(backgroundColor));
        }

        String fileName = exportElement.attributeValue("fileName");
        if (StringUtils.isNotEmpty(fileName)) {
            export.setFileName(fileName);
        }

        String position = exportElement.attributeValue("position");
        if (StringUtils.isNotEmpty(position)) {
            export.setPosition(ExportPosition.valueOf(position));
        }

        String removeImages = exportElement.attributeValue("removeImages");
        if (StringUtils.isNotEmpty(removeImages)) {
            export.setRemoveImages(Boolean.valueOf(removeImages));
        }

        String exportTitles = exportElement.attributeValue("exportTitles");
        if (StringUtils.isNotEmpty(exportTitles)) {
            export.setExportTitles(Boolean.valueOf(exportTitles));
        }

        String exportSelection = exportElement.attributeValue("exportSelection");
        if (StringUtils.isNotEmpty(exportSelection)) {
            export.setExportSelection(Boolean.valueOf(exportSelection));
        }

        String dataDateFormat = exportElement.attributeValue("dataDateFormat");
        if (StringUtils.isNotEmpty(dataDateFormat)) {
            export.setDataDateFormat(dataDateFormat);
        }

        String dateFormat = exportElement.attributeValue("dateFormat");
        if (StringUtils.isNotEmpty(dateFormat)) {
            export.setDateFormat(dateFormat);
        }

        String keyListener = exportElement.attributeValue("keyListener");
        if (StringUtils.isNotEmpty(keyListener)) {
            export.setKeyListener(Boolean.valueOf(keyListener));
        }

        String fileListener = exportElement.attributeValue("fileListener");
        if (StringUtils.isNotEmpty(fileListener)) {
            export.setFileListener(Boolean.valueOf(fileListener));
        }

        return export;
    }

    protected void loadLegend(AbstractLegend legend, Element legendElement) {
        loadLegendItems(legend, legendElement);

        String accessibleLabel = legendElement.attributeValue("accessibleLabel");
        if (StringUtils.isNotEmpty(accessibleLabel)) {
            legend.setAccessibleLabel(loadResourceString(accessibleLabel));
        }

        String align = legendElement.attributeValue("align");
        if (StringUtils.isNotEmpty(align)) {
            legend.setAlign(Align.valueOf(align));
        }

        String autoMargins = legendElement.attributeValue("autoMargins");
        if (StringUtils.isNotEmpty(autoMargins)) {
            legend.setAutoMargins(Boolean.valueOf(autoMargins));
        }

        String backgroundAlpha = legendElement.attributeValue("backgroundAlpha");
        if (StringUtils.isNotEmpty(backgroundAlpha)) {
            legend.setBackgroundAlpha(Double.valueOf(backgroundAlpha));
        }

        String backgroundColor = legendElement.attributeValue("backgroundColor");
        if (StringUtils.isNotEmpty(backgroundColor)) {
            legend.setBackgroundColor(Color.valueOf(backgroundColor));
        }

        String borderAlpha = legendElement.attributeValue("borderAlpha");
        if (StringUtils.isNotEmpty(borderAlpha)) {
            legend.setBorderAlpha(Double.valueOf(borderAlpha));
        }

        String borderColor = legendElement.attributeValue("borderColor");
        if (StringUtils.isNotEmpty(borderColor)) {
            legend.setBackgroundColor(Color.valueOf(borderColor));
        }

        String bottom = legendElement.attributeValue("bottom");
        if (StringUtils.isNotEmpty(bottom)) {
            legend.setBottom(Integer.parseInt(bottom));
        }

        String color = legendElement.attributeValue("color");
        if (StringUtils.isNotEmpty(color)) {
            legend.setColor(Color.valueOf(color));
        }

        String divId = legendElement.attributeValue("divId");
        if (StringUtils.isNotEmpty(divId)) {
            legend.setDivId(divId);
        }

        String enabled = legendElement.attributeValue("enabled");
        if (StringUtils.isNotEmpty(enabled)) {
            legend.setEnabled(Boolean.valueOf(enabled));
        }

        String equalWidths = legendElement.attributeValue("equalWidths");
        if (StringUtils.isNotEmpty(equalWidths)) {
            legend.setEqualWidths(Boolean.valueOf(equalWidths));
        }

        String fontSize = legendElement.attributeValue("fontSize");
        if (StringUtils.isNotEmpty(fontSize)) {
            legend.setFontSize(Integer.parseInt(fontSize));
        }

        String forceWidth = legendElement.attributeValue("forceWidth");
        if (StringUtils.isNotEmpty(forceWidth)) {
            legend.setForceWidth(Boolean.valueOf(forceWidth));
        }

        String gradientRotation = legendElement.attributeValue("gradientRotation");
        if (StringUtils.isNotEmpty(gradientRotation)) {
            legend.setGradientRotation(Integer.parseInt(gradientRotation));
        }

        String horizontalGap = legendElement.attributeValue("horizontalGap");
        if (StringUtils.isNotEmpty(horizontalGap)) {
            legend.setHorizontalGap(Integer.parseInt(horizontalGap));
        }

        String labelWidth = legendElement.attributeValue("labelWidth");
        if (StringUtils.isNotEmpty(labelWidth)) {
            legend.setLabelWidth(Integer.parseInt(labelWidth));
        }

        String labelText = legendElement.attributeValue("labelText");
        if (StringUtils.isNotEmpty(labelText)) {
            legend.setLabelText(loadResourceString(labelText));
        }

        String left = legendElement.attributeValue("left");
        if (StringUtils.isNotEmpty(left)) {
            legend.setLeft(Integer.parseInt(left));
        }

        loadMargins(legend, legendElement);

        String markerBorderAlpha = legendElement.attributeValue("markerBorderAlpha");
        if (StringUtils.isNotEmpty(markerBorderAlpha)) {
            legend.setMarkerBorderAlpha(Double.valueOf(markerBorderAlpha));
        }

        String markerBorderColor = legendElement.attributeValue("markerBorderColor");
        if (StringUtils.isNotEmpty(markerBorderColor)) {
            legend.setMarkerBorderColor(Color.valueOf(markerBorderColor));
        }

        String markerBorderThickness = legendElement.attributeValue("markerBorderThickness");
        if (StringUtils.isNotEmpty(markerBorderThickness)) {
            legend.setMarkerBorderThickness(Integer.parseInt(markerBorderThickness));
        }

        String markerDisabledColor = legendElement.attributeValue("markerDisabledColor");
        if (StringUtils.isNotEmpty(markerDisabledColor)) {
            legend.setMarkerDisabledColor(Color.valueOf(markerDisabledColor));
        }

        String markerLabelGap = legendElement.attributeValue("markerLabelGap");
        if (StringUtils.isNotEmpty(markerLabelGap)) {
            legend.setMarkerLabelGap(Integer.parseInt(markerLabelGap));
        }

        String markerSize = legendElement.attributeValue("markerSize");
        if (StringUtils.isNotEmpty(markerSize)) {
            legend.setMarkerSize(Integer.parseInt(markerSize));
        }

        String markerType = legendElement.attributeValue("markerType");
        if (StringUtils.isNotEmpty(markerType)) {
            legend.setMarkerType(MarkerType.valueOf(markerType));
        }

        String maxColumns = legendElement.attributeValue("maxColumns");
        if (StringUtils.isNotEmpty(maxColumns)) {
            legend.setMaxColumns(Integer.parseInt(maxColumns));
        }

        String periodValueText = legendElement.attributeValue("periodValueText");
        if (StringUtils.isNotEmpty(periodValueText)) {
            legend.setPeriodValueText(loadResourceString(periodValueText));
        }

        String position = legendElement.attributeValue("position");
        if (StringUtils.isNotEmpty(position)) {
            legend.setPosition(LegendPosition.valueOf(position));
        }

        String reversedOrder = legendElement.attributeValue("reversedOrder");
        if (StringUtils.isNotEmpty(reversedOrder)) {
            legend.setReversedOrder(Boolean.valueOf(reversedOrder));
        }

        String right = legendElement.attributeValue("right");
        if (StringUtils.isNotEmpty(right)) {
            legend.setRight(Integer.parseInt(right));
        }

        String rollOverColor = legendElement.attributeValue("rollOverColor");
        if (StringUtils.isNotEmpty(rollOverColor)) {
            legend.setRollOverColor(Color.valueOf(rollOverColor));
        }

        String rollOverGraphAlpha = legendElement.attributeValue("rollOverGraphAlpha");
        if (StringUtils.isNotEmpty(rollOverGraphAlpha)) {
            legend.setRollOverGraphAlpha(Double.valueOf(rollOverGraphAlpha));
        }

        String showEntries = legendElement.attributeValue("showEntries");
        if (StringUtils.isNotEmpty(showEntries)) {
            legend.setShowEntries(Boolean.valueOf(showEntries));
        }

        String spacing = legendElement.attributeValue("spacing");
        if (StringUtils.isNotEmpty(spacing)) {
            legend.setSpacing(Integer.parseInt(spacing));
        }

        String switchable = legendElement.attributeValue("switchable");
        if (StringUtils.isNotEmpty(switchable)) {
            legend.setSwitchable(Boolean.valueOf(switchable));
        }

        String switchColor = legendElement.attributeValue("switchColor");
        if (StringUtils.isNotEmpty(switchColor)) {
            legend.setSwitchColor(Color.valueOf(switchColor));
        }

        String switchType = legendElement.attributeValue("switchType");
        if (StringUtils.isNotEmpty(switchType)) {
            legend.setSwitchType(LegendSwitch.valueOf(switchType));
        }

        String textClickEnabled = legendElement.attributeValue("textClickEnabled");
        if (StringUtils.isNotEmpty(textClickEnabled)) {
            legend.setTextClickEnabled(Boolean.valueOf(textClickEnabled));
        }

        String tabIndex = legendElement.attributeValue("tabIndex");
        if (StringUtils.isNotEmpty(tabIndex)) {
            legend.setTabIndex(Integer.parseInt(tabIndex));
        }

        String top = legendElement.attributeValue("top");
        if (StringUtils.isNotEmpty(top)) {
            legend.setTop(Integer.parseInt(top));
        }

        String useGraphSettings = legendElement.attributeValue("useGraphSettings");
        if (StringUtils.isNotEmpty(useGraphSettings)) {
            legend.setUseGraphSettings(Boolean.valueOf(useGraphSettings));
        }

        String useMarkerColorForLabels = legendElement.attributeValue("useMarkerColorForLabels");
        if (StringUtils.isNotEmpty(useMarkerColorForLabels)) {
            legend.setUseMarkerColorForLabels(Boolean.valueOf(useMarkerColorForLabels));
        }

        String useMarkerColorForValues = legendElement.attributeValue("useMarkerColorForValues");
        if (StringUtils.isNotEmpty(useMarkerColorForValues)) {
            legend.setUseMarkerColorForValues(Boolean.valueOf(useMarkerColorForValues));
        }

        String valueAlign = legendElement.attributeValue("valueAlign");
        if (StringUtils.isNotEmpty(valueAlign)) {
            legend.setValueAlign(ValueAlign.valueOf(valueAlign));
        }

        String valueFunction = legendElement.elementText("valueFunction");
        if (StringUtils.isNotBlank(valueFunction)) {
            legend.setValueFunction(new JsFunction(valueFunction));
        }

        String valueText = legendElement.attributeValue("valueText");
        if (StringUtils.isNotEmpty(valueText)) {
            legend.setValueText(loadResourceString(valueText));
        }

        String valueWidth = legendElement.attributeValue("valueWidth");
        if (StringUtils.isNotEmpty(valueWidth)) {
            legend.setValueWidth(Integer.parseInt(valueWidth));
        }

        String verticalGap = legendElement.attributeValue("verticalGap");
        if (StringUtils.isNotEmpty(verticalGap)) {
            legend.setVerticalGap(Integer.parseInt(verticalGap));
        }

        String width = legendElement.attributeValue("width");
        if (StringUtils.isNotEmpty(width)) {
            legend.setWidth(Integer.parseInt(width));
        }

        String combineLegend = legendElement.attributeValue("combineLegend");
        if (StringUtils.isNotEmpty(combineLegend)) {
            legend.setCombineLegend(Boolean.valueOf(combineLegend));
        }
    }

    protected void loadLegendItems(AbstractLegend legend, Element legendElement) {
        Element legendDataElement = legendElement.element("data");
        if (legendDataElement != null) {
            for (Object dataItem : legendDataElement.elements("item")) {
                Element dataElement = (Element) dataItem;

                LegendItem legendItem = new LegendItem();

                String title = dataElement.attributeValue("title");
                if (StringUtils.isNotEmpty(title)) {
                    legendItem.setTitle(loadResourceString(title));
                }

                String color = dataElement.attributeValue("color");
                if (StringUtils.isNotEmpty(color)) {
                    legendItem.setColor(Color.valueOf(color));
                }

                String markerType = dataElement.attributeValue("markerType");
                if (StringUtils.isNotEmpty(markerType)) {
                    legendItem.setMarkerType(MarkerType.valueOf(markerType));
                }

                legend.addItems(legendItem);
            }
        }
    }

    @Nullable
    protected CreditsPosition loadCreditsPosition(Element element) {
        String creditsPosition = element.attributeValue("creditsPosition");
        if (StringUtils.isNotEmpty(creditsPosition)) {
            CreditsPosition cp = CreditsPosition.fromId(creditsPosition);
            if (cp == null) {
                cp = CreditsPosition.valueOf(creditsPosition);
            }
            return cp;
        }
        return null;
    }

    protected void loadMargins(HasMargins hasMargins, Element element) {
        String marginTop = element.attributeValue("marginTop");
        if (StringUtils.isNotEmpty(marginTop)) {
            hasMargins.setMarginTop(Integer.valueOf(marginTop));
        }

        String marginBottom = element.attributeValue("marginBottom");
        if (StringUtils.isNotEmpty(marginBottom)) {
            hasMargins.setMarginBottom(Integer.valueOf(marginBottom));
        }

        String marginLeft = element.attributeValue("marginLeft");
        if (StringUtils.isNotEmpty(marginLeft)) {
            hasMargins.setMarginLeft(Integer.valueOf(marginLeft));
        }

        String marginRight = element.attributeValue("marginRight");
        if (StringUtils.isNotEmpty(marginRight)) {
            hasMargins.setMarginRight(Integer.valueOf(marginRight));
        }
    }

    protected List<Color> loadColors(Element colorsElement) {
        List<Color> colors = new ArrayList<>();

        for (Object colorItem : colorsElement.elements("color")) {
            Element colorElement = (Element) colorItem;

            String value = colorElement.attributeValue("value");
            if (StringUtils.isNotEmpty(value)) {
                colors.add(Color.valueOf(value));
            }
        }
        return colors;
    }

    protected void loadDateFormat(DateFormat dateFormat, Element dateFormatElement) {
        String period = dateFormatElement.attributeValue("period");
        if (StringUtils.isNotEmpty(period)) {
            DatePeriod dp = DatePeriod.fromId(period);
            if (dp == null) {
                dp = DatePeriod.valueOf(period);
            }
            dateFormat.setPeriod(dp);
        }

        String format = dateFormatElement.attributeValue("format");
        if (StringUtils.isNotEmpty(format)) {
            dateFormat.setFormat(format);
        }
    }

    protected void loadGuide(Guide guide, Element guideElement) {
        String above = guideElement.attributeValue("above");
        if (StringUtils.isNotEmpty(above)) {
            guide.setAbove(Boolean.valueOf(above));
        }

        String angle = guideElement.attributeValue("angle");
        if (StringUtils.isNotEmpty(angle)) {
            guide.setAngle(Integer.valueOf(angle));
        }

        String balloonColor = guideElement.attributeValue("balloonColor");
        if (StringUtils.isNotEmpty(balloonColor)) {
            guide.setBalloonColor(Color.valueOf(balloonColor));
        }

        String balloonText = guideElement.attributeValue("balloonText");
        if (StringUtils.isNotEmpty(balloonText)) {
            guide.setBalloonText(loadResourceString(balloonText));
        }

        String boldLabel = guideElement.attributeValue("boldLabel");
        if (StringUtils.isNotEmpty(boldLabel)) {
            guide.setBoldLabel(Boolean.valueOf(boldLabel));
        }

        String category = guideElement.attributeValue("category");
        if (StringUtils.isNotEmpty(category)) {
            guide.setCategory(category);
        }

        String color = guideElement.attributeValue("color");
        if (StringUtils.isNotEmpty(color)) {
            guide.setColor(Color.valueOf(color));
        }

        String dashLength = guideElement.attributeValue("dashLength");
        if (StringUtils.isNotEmpty(dashLength)) {
            guide.setDashLength(Integer.valueOf(dashLength));
        }

        String date = guideElement.attributeValue("date");
        if (StringUtils.isNotEmpty(date)) {
            guide.setDate(loadDate(date));
        }

        String expand = guideElement.attributeValue("expand");
        if (StringUtils.isNotEmpty(expand)) {
            guide.setExpand(Boolean.valueOf(expand));
        }

        String fillAlpha = guideElement.attributeValue("fillAlpha");
        if (StringUtils.isNotEmpty(fillAlpha)) {
            guide.setFillAlpha(Double.valueOf(fillAlpha));
        }

        String fillColor = guideElement.attributeValue("fillColor");
        if (StringUtils.isNotEmpty(fillColor)) {
            guide.setFillColor(Color.valueOf(fillColor));
        }

        String fontSize = guideElement.attributeValue("fontSize");
        if (StringUtils.isNotEmpty(fontSize)) {
            guide.setFontSize(Integer.valueOf(fontSize));
        }

        String id = guideElement.attributeValue("id");
        if (StringUtils.isNotEmpty(id)) {
            guide.setId(id);
        }

        String inside = guideElement.attributeValue("inside");
        if (StringUtils.isNotEmpty(inside)) {
            guide.setInside(Boolean.valueOf(inside));
        }

        String label = guideElement.attributeValue("label");
        if (StringUtils.isNotEmpty(label)) {
            guide.setLabel(loadResourceString(label));
        }

        String labelRotation = guideElement.attributeValue("labelRotation");
        if (StringUtils.isNotEmpty(labelRotation)) {
            guide.setLabelRotation(Integer.valueOf(labelRotation));
        }

        String lineAlpha = guideElement.attributeValue("lineAlpha");
        if (StringUtils.isNotEmpty(lineAlpha)) {
            guide.setLineAlpha(Double.valueOf(lineAlpha));
        }

        String lineColor = guideElement.attributeValue("lineColor");
        if (StringUtils.isNotEmpty(lineColor)) {
            guide.setLineColor(Color.valueOf(lineColor));
        }

        String lineThickness = guideElement.attributeValue("lineThickness");
        if (StringUtils.isNotEmpty(lineThickness)) {
            guide.setLineThickness(Integer.valueOf(lineThickness));
        }

        String position = guideElement.attributeValue("position");
        if (StringUtils.isNotEmpty(position)) {
            guide.setPosition(Position.valueOf(position));
        }

        String tickLength = guideElement.attributeValue("tickLength");
        if (StringUtils.isNotEmpty(tickLength)) {
            guide.setTickLength(Integer.valueOf(tickLength));
        }

        String toAngle = guideElement.attributeValue("toAngle");
        if (StringUtils.isNotEmpty(toAngle)) {
            guide.setToAngle(Integer.valueOf(toAngle));
        }

        String toCategory = guideElement.attributeValue("toCategory");
        if (StringUtils.isNotEmpty(toCategory)) {
            guide.setToCategory(toCategory);
        }

        String toDate = guideElement.attributeValue("toDate");
        if (StringUtils.isNotEmpty(toDate)) {
            guide.setToDate(loadDate(toDate));
        }

        String toValue = guideElement.attributeValue("toValue");
        if (StringUtils.isNotEmpty(toValue)) {
            guide.setToValue(Double.valueOf(toValue));
        }

        String value = guideElement.attributeValue("value");
        if (StringUtils.isNotEmpty(value)) {
            guide.setValue(Double.valueOf(value));
        }

        String valueAxis = guideElement.attributeValue("valueAxis");
        if (StringUtils.isNotEmpty(valueAxis)) {
            guide.setValueAxis(valueAxis);
        }
    }

    protected List<Guide> loadGuides(Element guidesElement) {
        List<Guide> guides = new ArrayList<>();
        for (Object guideItem : guidesElement.elements("guide")) {
            Element guideElement = (Element) guideItem;

            Guide guide = new Guide();
            loadGuide(guide, guideElement);
            guides.add(guide);
        }
        return guides;
    }

    protected Date loadDate(String value) {
        try {
            return CONFIG_DATE_FORMATTER.parse(value);
        } catch (ParseException e) {
            throw new GuiDevelopmentException("Unable to parse date from XML chart configuration",
                    context, "date", value);
        }
    }

    protected List<DateFormat> loadDateFormats(Element dateFormatsElement) {
        List<DateFormat> dateFormats = new ArrayList<>();
        for (Object dateFormatItem : dateFormatsElement.elements("dateFormat")) {
            Element dateFormatElement = (Element) dateFormatItem;
            DateFormat dateFormat = new DateFormat();
            loadDateFormat(dateFormat, dateFormatElement);
            dateFormats.add(dateFormat);
        }
        return dateFormats;
    }

    protected void loadAbstractAxis(AbstractAxis axis, Element element) {
        String autoGridCount = element.attributeValue("autoGridCount");
        if (StringUtils.isNotEmpty(autoGridCount)) {
            axis.setAutoGridCount(Boolean.valueOf(autoGridCount));
        }

        String autoRotateAngle = element.attributeValue("autoRotateAngle");
        if (StringUtils.isNotEmpty(autoRotateAngle)) {
            axis.setAutoRotateAngle(Integer.parseInt(autoRotateAngle));
        }

        String autoRotateCount = element.attributeValue("autoRotateCount");
        if (StringUtils.isNotEmpty(autoRotateCount)) {
            axis.setAutoRotateCount(Integer.parseInt(autoRotateCount));
        }

        String axisAlpha = element.attributeValue("axisAlpha");
        if (StringUtils.isNotEmpty(axisAlpha)) {
            axis.setAxisAlpha(Double.valueOf(axisAlpha));
        }

        String axisColor = element.attributeValue("axisColor");
        if (StringUtils.isNotEmpty(axisColor)) {
            axis.setAxisColor(Color.valueOf(axisColor));
        }

        String axisThickness = element.attributeValue("axisThickness");
        if (StringUtils.isNotEmpty(axisThickness)) {
            axis.setAxisThickness(Integer.parseInt(axisThickness));
        }

        Element balloonElement = element.element("balloon");
        if (balloonElement != null) {
            axis.setBalloon(loadBalloon(balloonElement));
        }

        String boldLabels = element.attributeValue("boldLabels");
        if (StringUtils.isNotEmpty(boldLabels)) {
            axis.setCenterLabels(Boolean.valueOf(boldLabels));
        }

        String boldPeriodBeginning = element.attributeValue("boldPeriodBeginning");
        if (StringUtils.isNotEmpty(boldPeriodBeginning)) {
            axis.setBoldPeriodBeginning(Boolean.valueOf(boldPeriodBeginning));
        }

        String centerLabelOnFullPeriod = element.attributeValue("centerLabelOnFullPeriod");
        if (StringUtils.isNotEmpty(centerLabelOnFullPeriod)) {
            axis.setCenterLabelOnFullPeriod(Boolean.valueOf(centerLabelOnFullPeriod));
        }

        String centerLabels = element.attributeValue("centerLabels");
        if (StringUtils.isNotEmpty(centerLabels)) {
            axis.setBoldLabels(Boolean.valueOf(centerLabels));
        }

        String color = element.attributeValue("color");
        if (StringUtils.isNotEmpty(color)) {
            axis.setColor(Color.valueOf(color));
        }

        String dashLength = element.attributeValue("dashLength");
        if (StringUtils.isNotEmpty(dashLength)) {
            axis.setDashLength(Integer.parseInt(dashLength));
        }

        Element dateFormatsElement = element.element("dateFormats");
        if (dateFormatsElement != null) {
            axis.setDateFormats(loadDateFormats(dateFormatsElement));
        }

        String fillAlpha = element.attributeValue("fillAlpha");
        if (StringUtils.isNotEmpty(fillAlpha)) {
            axis.setFillAlpha(Double.valueOf(fillAlpha));
        }

        String fillColor = element.attributeValue("fillColor");
        if (StringUtils.isNotEmpty(fillColor)) {
            axis.setFillColor(Color.valueOf(fillColor));
        }

        String fontSize = element.attributeValue("fontSize");
        if (StringUtils.isNotEmpty(fontSize)) {
            axis.setFontSize(Integer.parseInt(fontSize));
        }

        String gridAlpha = element.attributeValue("gridAlpha");
        if (StringUtils.isNotEmpty(gridAlpha)) {
            axis.setGridAlpha(Double.valueOf(gridAlpha));
        }

        String gridColor = element.attributeValue("gridColor");
        if (StringUtils.isNotEmpty(gridColor)) {
            axis.setGridColor(Color.valueOf(gridColor));
        }

        String gridCount = element.attributeValue("gridCount");
        if (StringUtils.isNotEmpty(gridCount)) {
            axis.setGridCount(Integer.parseInt(gridCount));
        }

        String gridThickness = element.attributeValue("gridThickness");
        if (StringUtils.isNotEmpty(gridThickness)) {
            axis.setGridThickness(Integer.parseInt(gridThickness));
        }

        Element guidesElement = element.element("guides");
        if (guidesElement != null) {
            axis.setGuides(loadGuides(guidesElement));
        }

        String ignoreAxisWidth = element.attributeValue("ignoreAxisWidth");
        if (StringUtils.isNotEmpty(ignoreAxisWidth)) {
            axis.setIgnoreAxisWidth(Boolean.valueOf(ignoreAxisWidth));
        }

        String inside = element.attributeValue("inside");
        if (StringUtils.isNotEmpty(inside)) {
            axis.setInside(Boolean.valueOf(inside));
        }

        String labelFrequency = element.attributeValue("labelFrequency");
        if (StringUtils.isNotEmpty(labelFrequency)) {
            axis.setLabelFrequency(Double.valueOf(labelFrequency));
        }

        String labelOffset = element.attributeValue("labelOffset");
        if (StringUtils.isNotEmpty(labelOffset)) {
            axis.setLabelOffset(Integer.parseInt(labelOffset));
        }

        String labelRotation = element.attributeValue("labelRotation");
        if (StringUtils.isNotEmpty(labelRotation)) {
            axis.setLabelRotation(Integer.parseInt(labelRotation));
        }

        String labelsEnabled = element.attributeValue("labelsEnabled");
        if (StringUtils.isNotEmpty(labelsEnabled)) {
            axis.setLabelsEnabled(Boolean.valueOf(labelsEnabled));
        }

        String markPeriodChange = element.attributeValue("markPeriodChange");
        if (StringUtils.isNotEmpty(markPeriodChange)) {
            axis.setMarkPeriodChange(Boolean.valueOf(markPeriodChange));
        }

        String minHorizontalGap = element.attributeValue("minHorizontalGap");
        if (StringUtils.isNotEmpty(minHorizontalGap)) {
            axis.setMinHorizontalGap(Integer.parseInt(minHorizontalGap));
        }

        String minorGridAlpha = element.attributeValue("minorGridAlpha");
        if (StringUtils.isNotEmpty(minorGridAlpha)) {
            axis.setMinorGridAlpha(Double.valueOf(minorGridAlpha));
        }

        String minorGridEnabled = element.attributeValue("minorGridEnabled");
        if (StringUtils.isNotEmpty(minorGridEnabled)) {
            axis.setMinorGridEnabled(Boolean.valueOf(minorGridEnabled));
        }

        String minVerticalGap = element.attributeValue("minVerticalGap");
        if (StringUtils.isNotEmpty(minVerticalGap)) {
            axis.setMinVerticalGap(Integer.parseInt(minVerticalGap));
        }

        String minorTickLength = element.attributeValue("minorTickLength");
        if (StringUtils.isNotEmpty(minorTickLength)) {
            axis.setMinorTickLength(Integer.parseInt(minorTickLength));
        }

        String offset = element.attributeValue("offset");
        if (StringUtils.isNotEmpty(offset)) {
            axis.setOffset(Integer.parseInt(offset));
        }

        String position = element.attributeValue("position");
        if (StringUtils.isNotEmpty(position)) {
            axis.setPosition(Position.valueOf(position));
        }

        String showFirstLabel = element.attributeValue("showFirstLabel");
        if (StringUtils.isNotEmpty(showFirstLabel)) {
            axis.setShowFirstLabel(Boolean.valueOf(showFirstLabel));
        }

        String showLastLabel = element.attributeValue("showLastLabel");
        if (StringUtils.isNotEmpty(showLastLabel)) {
            axis.setShowLastLabel(Boolean.valueOf(showLastLabel));
        }

        String tickLength = element.attributeValue("tickLength");
        if (StringUtils.isNotEmpty(tickLength)) {
            axis.setTickLength(Integer.parseInt(tickLength));
        }

        String title = element.attributeValue("title");
        if (StringUtils.isNotEmpty(title)) {
            axis.setTitle(loadResourceString(title));
        }

        String titleBold = element.attributeValue("titleBold");
        if (StringUtils.isNotEmpty(titleBold)) {
            axis.setTitleBold(Boolean.valueOf(titleBold));
        }

        String titleColor = element.attributeValue("titleColor");
        if (StringUtils.isNotEmpty(titleColor)) {
            axis.setTitleColor(Color.valueOf(titleColor));
        }

        String titleFontSize = element.attributeValue("titleFontSize");
        if (StringUtils.isNotEmpty(titleFontSize)) {
            axis.setTitleFontSize(Integer.parseInt(titleFontSize));
        }

        String titleRotation = element.attributeValue("titleRotation");
        if (StringUtils.isNotEmpty(titleRotation)) {
            axis.setTitleRotation(Integer.parseInt(titleRotation));
        }

        String centerRotatedLabels = element.attributeValue("centerRotatedLabels");
        if (StringUtils.isNotEmpty(centerRotatedLabels)) {
            axis.setCenterRotatedLabels(Boolean.valueOf(centerRotatedLabels));
        }
    }

    protected ValueAxis loadValueAxis(Element valueAxisElement) {
        ValueAxis axis = new ValueAxis();

        loadAbstractAxis(axis, valueAxisElement);

        String id = valueAxisElement.attributeValue("id");
        if (StringUtils.isNotEmpty(id)) {
            axis.setId(id);
        }

        String labelFunction = valueAxisElement.elementText("labelFunction");
        if (StringUtils.isNotBlank(labelFunction)) {
            axis.setLabelFunction(new JsFunction(labelFunction));
        }

        String axisFrequency = valueAxisElement.attributeValue("axisFrequency");
        if (StringUtils.isNotEmpty(axisFrequency)) {
            axis.setAxisFrequency(Double.valueOf(axisFrequency));
        }

        String axisTitleOffset = valueAxisElement.attributeValue("axisTitleOffset");
        if (StringUtils.isNotEmpty(axisTitleOffset)) {
            axis.setAxisTitleOffset(Integer.valueOf(axisTitleOffset));
        }

        String balloonTextFunction = valueAxisElement.elementText("balloonTextFunction");
        if (StringUtils.isNotBlank(balloonTextFunction)) {
            axis.setBalloonTextFunction(new JsFunction(balloonTextFunction));
        }

        String baseValue = valueAxisElement.attributeValue("baseValue");
        if (StringUtils.isNotEmpty(baseValue)) {
            axis.setBaseValue(Double.valueOf(baseValue));
        }

        String duration = valueAxisElement.attributeValue("duration");
        if (StringUtils.isNotEmpty(duration)) {
            axis.setDuration(Duration.valueOf(duration));
        }

        String gridType = valueAxisElement.attributeValue("gridType");
        if (StringUtils.isNotEmpty(gridType)) {
            axis.setGridType(GridType.valueOf(gridType));
        }

        String includeAllValues = valueAxisElement.attributeValue("includeAllValues");
        if (StringUtils.isNotEmpty(includeAllValues)) {
            axis.setIncludeAllValues(Boolean.valueOf(includeAllValues));
        }

        String includeGuidesInMinMax = valueAxisElement.attributeValue("includeGuidesInMinMax");
        if (StringUtils.isNotEmpty(includeGuidesInMinMax)) {
            axis.setIncludeGuidesInMinMax(Boolean.valueOf(includeGuidesInMinMax));
        }

        String includeHidden = valueAxisElement.attributeValue("includeHidden");
        if (StringUtils.isNotEmpty(includeHidden)) {
            axis.setIncludeHidden(Boolean.valueOf(includeHidden));
        }

        String integersOnly = valueAxisElement.attributeValue("integersOnly");
        if (StringUtils.isNotEmpty(integersOnly)) {
            axis.setIntegersOnly(Boolean.valueOf(integersOnly));
        }

        String logarithmic = valueAxisElement.attributeValue("logarithmic");
        if (StringUtils.isNotEmpty(logarithmic)) {
            axis.setLogarithmic(Boolean.valueOf(logarithmic));
        }

        String maximum = valueAxisElement.attributeValue("maximum");
        if (StringUtils.isNotEmpty(maximum)) {
            axis.setMaximum(Double.valueOf(maximum));
        }

        String maximumDate = valueAxisElement.attributeValue("maximumDate");
        if (StringUtils.isNotEmpty(maximumDate)) {
            axis.setMaximumDate(loadDate(maximumDate));
        }

        String minimum = valueAxisElement.attributeValue("minimum");
        if (StringUtils.isNotEmpty(minimum)) {
            axis.setMinimum(Double.valueOf(minimum));
        }

        String minimumDate = valueAxisElement.attributeValue("minimumDate");
        if (StringUtils.isNotEmpty(minimumDate)) {
            axis.setMinimumDate(loadDate(minimumDate));
        }

        String minMaxMultiplier = valueAxisElement.attributeValue("minMaxMultiplier");
        if (StringUtils.isNotEmpty(minMaxMultiplier)) {
            axis.setMinMaxMultiplier(Double.valueOf(minMaxMultiplier));
        }

        String pointPosition = valueAxisElement.attributeValue("pointPosition");
        if (StringUtils.isNotEmpty(pointPosition)) {
            axis.setPointPosition(PointPosition.valueOf(pointPosition));
        }

        String precision = valueAxisElement.attributeValue("precision");
        if (StringUtils.isNotEmpty(precision)) {
            axis.setPrecision(Integer.valueOf(precision));
        }

        String radarCategoriesEnabled = valueAxisElement.attributeValue("radarCategoriesEnabled");
        if (StringUtils.isNotEmpty(radarCategoriesEnabled)) {
            axis.setRadarCategoriesEnabled(Boolean.valueOf(radarCategoriesEnabled));
        }

        String recalculateToPercents = valueAxisElement.attributeValue("recalculateToPercents");
        if (StringUtils.isNotEmpty(recalculateToPercents)) {
            axis.setRecalculateToPercents(Boolean.valueOf(recalculateToPercents));
        }

        String reversed = valueAxisElement.attributeValue("reversed");
        if (StringUtils.isNotEmpty(reversed)) {
            axis.setReversed(Boolean.valueOf(reversed));
        }

        String stackType = valueAxisElement.attributeValue("stackType");
        if (StringUtils.isNotEmpty(stackType)) {
            axis.setStackType(StackType.valueOf(stackType));
        }

        String strictMinMax = valueAxisElement.attributeValue("strictMinMax");
        if (StringUtils.isNotEmpty(strictMinMax)) {
            axis.setStrictMinMax(Boolean.valueOf(strictMinMax));
        }

        String synchronizationMultiplier = valueAxisElement.attributeValue("synchronizationMultiplier");
        if (StringUtils.isNotEmpty(synchronizationMultiplier)) {
            axis.setSynchronizationMultiplier(Double.valueOf(synchronizationMultiplier));
        }

        String synchronizeWith = valueAxisElement.attributeValue("synchronizeWith");
        if (StringUtils.isNotEmpty(synchronizeWith)) {
            axis.setSynchronizeWith(synchronizeWith);
        }

        String totalText = valueAxisElement.attributeValue("totalText");
        if (StringUtils.isNotEmpty(totalText)) {
            axis.setTotalText(loadResourceString(totalText));
        }

        String totalTextColor = valueAxisElement.attributeValue("totalTextColor");
        if (StringUtils.isNotEmpty(totalTextColor)) {
            axis.setTotalTextColor(Color.valueOf(totalTextColor));
        }

        String totalTextOffset = valueAxisElement.attributeValue("totalTextOffset");
        if (StringUtils.isNotEmpty(totalTextOffset)) {
            axis.setTotalTextOffset(Integer.valueOf(totalTextOffset));
        }

        String treatZeroAs = valueAxisElement.attributeValue("treatZeroAs");
        if (StringUtils.isNotEmpty(treatZeroAs)) {
            axis.setTreatZeroAs(Double.valueOf(treatZeroAs));
        }

        String type = valueAxisElement.attributeValue("type");
        if (StringUtils.isNotEmpty(type)) {
            axis.setType(ValueAxisType.valueOf(type));
        }

        String unit = valueAxisElement.attributeValue("unit");
        if (StringUtils.isNotEmpty(unit)) {
            axis.setUnit(unit);
        }

        String unitPosition = valueAxisElement.attributeValue("unitPosition");
        if (StringUtils.isNotEmpty(unitPosition)) {
            axis.setUnitPosition(UnitPosition.valueOf(unitPosition));
        }

        String usePrefixes = valueAxisElement.attributeValue("usePrefixes");
        if (StringUtils.isNotEmpty(usePrefixes)) {
            axis.setUsePrefixes(Boolean.valueOf(usePrefixes));
        }

        String useScientificNotation = valueAxisElement.attributeValue("useScientificNotation");
        if (StringUtils.isNotEmpty(useScientificNotation)) {
            axis.setUseScientificNotation(Boolean.valueOf(useScientificNotation));
        }

        String zeroGridAlpha = valueAxisElement.attributeValue("zeroGridAlpha");
        if (StringUtils.isNotEmpty(zeroGridAlpha)) {
            axis.setZeroGridAlpha(Double.valueOf(zeroGridAlpha));
        }

        String autoWrap = valueAxisElement.attributeValue("autoWrap");
        if (StringUtils.isNotEmpty(autoWrap)) {
            axis.setAutoWrap(Boolean.valueOf(autoWrap));
        }

        String minPeriod = valueAxisElement.attributeValue("minPeriod");
        if (StringUtils.isNotEmpty(minPeriod)) {
            DatePeriod dp = DatePeriod.fromId(minPeriod);
            if (dp == null) {
                dp = DatePeriod.valueOf(minPeriod);
            }
            axis.setMinPeriod(dp);
        }

        return axis;
    }

    protected void loadGraph(AbstractGraph graph, Element graphElement) {
        String accessibleLabel = graphElement.attributeValue("accessibleLabel");
        if (StringUtils.isNotEmpty(accessibleLabel)) {
            graph.setAccessibleLabel(loadResourceString(accessibleLabel));
        }

        String alphaField = graphElement.attributeValue("alphaField");
        if (StringUtils.isNotEmpty(alphaField)) {
            graph.setAlphaField(alphaField);
        }

        String animationPlayed = graphElement.attributeValue("animationPlayed");
        if (StringUtils.isNotEmpty(animationPlayed)) {
            graph.setAnimationPlayed(Boolean.valueOf(animationPlayed));
        }

        Element balloonElement = graphElement.element("balloon");
        if (balloonElement != null) {
            graph.setBalloon(loadBalloon(balloonElement));
        }

        String balloonColor = graphElement.attributeValue("balloonColor");
        if (StringUtils.isNotEmpty(balloonColor)) {
            graph.setBalloonColor(Color.valueOf(balloonColor));
        }

        String balloonFunction = graphElement.elementText("balloonFunction");
        if (StringUtils.isNotEmpty(balloonFunction)) {
            graph.setBalloonFunction(new JsFunction(balloonFunction));
        }

        String balloonText = graphElement.attributeValue("balloonText");
        if (StringUtils.isNotEmpty(balloonText)) {
            graph.setBalloonText(loadResourceString(balloonText));
        }

        String behindColumns = graphElement.attributeValue("behindColumns");
        if (StringUtils.isNotEmpty(behindColumns)) {
            graph.setBehindColumns(Boolean.valueOf(behindColumns));
        }

        String bullet = graphElement.attributeValue("bullet");
        if (StringUtils.isNotEmpty(bullet)) {
            graph.setBullet(BulletType.valueOf(bullet));
        }

        String bulletAlpha = graphElement.attributeValue("bulletAlpha");
        if (StringUtils.isNotEmpty(bulletAlpha)) {
            graph.setBulletAlpha(Double.valueOf(bulletAlpha));
        }

        String bulletAxis = graphElement.attributeValue("bulletAxis");
        if (StringUtils.isNotEmpty(bulletAxis)) {
            graph.setBulletAxis(bulletAxis);
        }

        String bulletBorderAlpha = graphElement.attributeValue("bulletBorderAlpha");
        if (StringUtils.isNotEmpty(bulletBorderAlpha)) {
            graph.setBulletBorderAlpha(Double.valueOf(bulletBorderAlpha));
        }

        String bulletBorderColor = graphElement.attributeValue("bulletBorderColor");
        if (StringUtils.isNotEmpty(bulletBorderColor)) {
            graph.setBulletBorderColor(Color.valueOf(bulletBorderColor));
        }

        String bulletBorderThickness = graphElement.attributeValue("bulletBorderThickness");
        if (StringUtils.isNotEmpty(bulletBorderThickness)) {
            graph.setBulletBorderThickness(Integer.valueOf(bulletBorderThickness));
        }

        String bulletColor = graphElement.attributeValue("bulletColor");
        if (StringUtils.isNotEmpty(bulletColor)) {
            graph.setBulletColor(Color.valueOf(bulletColor));
        }

        String bulletField = graphElement.attributeValue("bulletField");
        if (StringUtils.isNotEmpty(bulletField)) {
            graph.setBulletField(bulletField);
        }

        String bulletHitAreaSize = graphElement.attributeValue("bulletHitAreaSize");
        if (StringUtils.isNotEmpty(bulletHitAreaSize)) {
            graph.setBulletHitAreaSize(Integer.valueOf(bulletHitAreaSize));
        }

        String bulletOffset = graphElement.attributeValue("bulletOffset");
        if (StringUtils.isNotEmpty(bulletOffset)) {
            graph.setBulletOffset(Integer.valueOf(bulletOffset));
        }

        String bulletSize = graphElement.attributeValue("bulletSize");
        if (StringUtils.isNotEmpty(bulletSize)) {
            graph.setBulletSize(Integer.valueOf(bulletSize));
        }

        String classNameField = graphElement.attributeValue("classNameField");
        if (StringUtils.isNotEmpty(classNameField)) {
            graph.setClassNameField(classNameField);
        }

        String bulletSizeField = graphElement.attributeValue("bulletSizeField");
        if (StringUtils.isNotEmpty(bulletSizeField)) {
            graph.setBulletSizeField(bulletSizeField);
        }

        String closeField = graphElement.attributeValue("closeField");
        if (StringUtils.isNotEmpty(closeField)) {
            graph.setCloseField(closeField);
        }

        String clustered = graphElement.attributeValue("clustered");
        if (StringUtils.isNotEmpty(clustered)) {
            graph.setClustered(Boolean.valueOf(clustered));
        }

        String color = graphElement.attributeValue("color");
        if (StringUtils.isNotEmpty(color)) {
            graph.setColor(Color.valueOf(color));
        }

        String colorField = graphElement.attributeValue("colorField");
        if (StringUtils.isNotEmpty(colorField)) {
            graph.setColorField(colorField);
        }

        String columnIndexField = graphElement.attributeValue("columnIndexField");
        if (StringUtils.isNotEmpty(columnIndexField)) {
            graph.setColumnIndexField(columnIndexField);
        }

        String columnWidth = graphElement.attributeValue("columnWidth");
        if (StringUtils.isNotEmpty(columnWidth)) {
            graph.setColumnWidth(Double.valueOf(columnWidth));
        }

        String connect = graphElement.attributeValue("connect");
        if (StringUtils.isNotEmpty(connect)) {
            graph.setConnect(Boolean.valueOf(connect));
        }

        String cornerRadiusTop = graphElement.attributeValue("cornerRadiusTop");
        if (StringUtils.isNotEmpty(cornerRadiusTop)) {
            graph.setCornerRadiusTop(Integer.valueOf(cornerRadiusTop));
        }

        String cursorBulletAlpha = graphElement.attributeValue("cursorBulletAlpha");
        if (StringUtils.isNotEmpty(cursorBulletAlpha)) {
            graph.setCursorBulletAlpha(Double.valueOf(cursorBulletAlpha));
        }

        String customBullet = graphElement.attributeValue("customBullet");
        if (StringUtils.isNotEmpty(customBullet)) {
            graph.setCustomBullet(customBullet);
        }

        String customBulletField = graphElement.attributeValue("customBulletField");
        if (StringUtils.isNotEmpty(customBulletField)) {
            graph.setCustomBulletField(customBulletField);
        }

        String customMarker = graphElement.attributeValue("customMarker");
        if (StringUtils.isNotEmpty(customMarker)) {
            graph.setCustomMarker(customMarker);
        }

        String dashLength = graphElement.attributeValue("dashLength");
        if (StringUtils.isNotEmpty(dashLength)) {
            graph.setDashLength(Integer.valueOf(dashLength));
        }

        String dashLengthField = graphElement.attributeValue("dashLengthField");
        if (StringUtils.isNotEmpty(dashLengthField)) {
            graph.setDashLengthField(dashLengthField);
        }

        Element dateFormatElement = graphElement.element("dateFormat");
        if (dateFormatElement != null) {
            DateFormat dateFormat = new DateFormat();
            loadDateFormat(dateFormat, dateFormatElement);
            graph.setDateFormat(dateFormat);
        }

        String descriptionField = graphElement.attributeValue("descriptionField");
        if (StringUtils.isNotEmpty(descriptionField)) {
            graph.setDescriptionField(descriptionField);
        }

        String errorField = graphElement.attributeValue("errorField");
        if (StringUtils.isNotEmpty(errorField)) {
            graph.setErrorField(errorField);
        }

        String fillAlphas = graphElement.attributeValue("fillAlphas");
        if (StringUtils.isNotEmpty(fillAlphas)) {
            graph.setFillAlphas(Double.valueOf(fillAlphas));
        }

        Element fillColorsElement = graphElement.element("fillColors");
        if (fillColorsElement != null) {
            List<Color> colors = loadColors(fillColorsElement);
            if (CollectionUtils.isNotEmpty(colors)) {
                graph.setFillColors(colors);
            }
        }

        String fillColorsField = graphElement.attributeValue("fillColorsField");
        if (StringUtils.isNotEmpty(fillColorsField)) {
            graph.setFillColorsField(fillColorsField);
        }

        String fillToAxis = graphElement.attributeValue("fillToAxis");
        if (StringUtils.isNotEmpty(fillToAxis)) {
            graph.setFillToAxis(fillToAxis);
        }

        String fillToGraph = graphElement.attributeValue("fillToGraph");
        if (StringUtils.isNotEmpty(fillToGraph)) {
            graph.setFillToGraph(fillToGraph);
        }

        String fixedColumnWidth = graphElement.attributeValue("fixedColumnWidth");
        if (StringUtils.isNotEmpty(fixedColumnWidth)) {
            graph.setFixedColumnWidth(Integer.valueOf(fixedColumnWidth));
        }

        String fontSize = graphElement.attributeValue("fontSize");
        if (StringUtils.isNotEmpty(fontSize)) {
            graph.setFontSize(Integer.valueOf(fontSize));
        }

        String forceGap = graphElement.attributeValue("forceGap");
        if (StringUtils.isNotEmpty(forceGap)) {
            graph.setForceGap(Boolean.valueOf(forceGap));
        }

        String gapField = graphElement.attributeValue("gapField");
        if (StringUtils.isNotEmpty(gapField)) {
            graph.setGapField(gapField);
        }

        String gapPeriod = graphElement.attributeValue("gapPeriod");
        if (StringUtils.isNotEmpty(gapPeriod)) {
            graph.setGapPeriod(Double.valueOf(gapPeriod));
        }

        String gradientOrientation = graphElement.attributeValue("gradientOrientation");
        if (StringUtils.isNotEmpty(gradientOrientation)) {
            graph.setGradientOrientation(GradientOrientation.valueOf(gradientOrientation));
        }

        String hidden = graphElement.attributeValue("hidden");
        if (StringUtils.isNotEmpty(hidden)) {
            graph.setHidden(Boolean.valueOf(hidden));
        }

        String hideBulletsCount = graphElement.attributeValue("hideBulletsCount");
        if (StringUtils.isNotEmpty(hideBulletsCount)) {
            graph.setHideBulletsCount(Integer.valueOf(hideBulletsCount));
        }

        String highField = graphElement.attributeValue("highField");
        if (StringUtils.isNotEmpty(highField)) {
            graph.setHighField(highField);
        }

        String id = graphElement.attributeValue("id");
        if (StringUtils.isNotEmpty(id)) {
            graph.setId(id);
        }

        String includeInMinMax = graphElement.attributeValue("includeInMinMax");
        if (StringUtils.isNotEmpty(includeInMinMax)) {
            graph.setIncludeInMinMax(Boolean.valueOf(includeInMinMax));
        }

        String labelAnchor = graphElement.attributeValue("labelAnchor");
        if (StringUtils.isNotEmpty(labelAnchor)) {
            graph.setLabelAnchor(labelAnchor);
        }

        String labelColorField = graphElement.attributeValue("labelColorField");
        if (StringUtils.isNotEmpty(labelColorField)) {
            graph.setLabelColorField(labelColorField);
        }

        String labelFunction = graphElement.elementText("labelFunction");
        if (StringUtils.isNotBlank(labelFunction)) {
            graph.setLabelFunction(new JsFunction(labelFunction));
        }

        String labelOffset = graphElement.attributeValue("labelOffset");
        if (StringUtils.isNotEmpty(labelOffset)) {
            graph.setLabelOffset(Integer.valueOf(labelOffset));
        }

        String labelPosition = graphElement.attributeValue("labelPosition");
        if (StringUtils.isNotEmpty(labelPosition)) {
            graph.setLabelPosition(ValueLabelPosition.valueOf(labelPosition));
        }

        String labelRotation = graphElement.attributeValue("labelRotation");
        if (StringUtils.isNotEmpty(labelRotation)) {
            graph.setLabelRotation(Integer.valueOf(labelRotation));
        }

        String labelText = graphElement.attributeValue("labelText");
        if (StringUtils.isNotEmpty(labelText)) {
            graph.setLabelText(loadResourceString(labelText));
        }

        String legendAlpha = graphElement.attributeValue("legendAlpha");
        if (StringUtils.isNotEmpty(legendAlpha)) {
            graph.setLegendAlpha(Double.valueOf(legendAlpha));
        }

        String legendColor = graphElement.attributeValue("legendColor");
        if (StringUtils.isNotEmpty(legendColor)) {
            graph.setLegendColor(Color.valueOf(legendColor));
        }

        String legendColorFunction = graphElement.elementText("legendColorFunction");
        if (StringUtils.isNotBlank(legendColorFunction)) {
            graph.setLegendColorFunction(new JsFunction(legendColorFunction));
        }

        String legendPeriodValueText = graphElement.attributeValue("legendPeriodValueText");
        if (StringUtils.isNotEmpty(legendPeriodValueText)) {
            graph.setLegendPeriodValueText(loadResourceString(legendPeriodValueText));
        }

        String legendValueText = graphElement.attributeValue("legendValueText");
        if (StringUtils.isNotEmpty(legendValueText)) {
            graph.setLegendValueText(loadResourceString(legendValueText));
        }

        String lineAlpha = graphElement.attributeValue("lineAlpha");
        if (StringUtils.isNotEmpty(lineAlpha)) {
            graph.setLineAlpha(Double.valueOf(lineAlpha));
        }

        String lineColor = graphElement.attributeValue("lineColor");
        if (StringUtils.isNotEmpty(lineColor)) {
            graph.setLineColor(Color.valueOf(lineColor));
        }

        String lineColorField = graphElement.attributeValue("lineColorField");
        if (StringUtils.isNotEmpty("lineColorField")) {
            graph.setLineColorField(lineColorField);
        }

        String lineThickness = graphElement.attributeValue("lineThickness");
        if (StringUtils.isNotEmpty(lineThickness)) {
            graph.setLineThickness(Integer.valueOf(lineThickness));
        }

        String lowField = graphElement.attributeValue("lowField");
        if (StringUtils.isNotEmpty(lowField)) {
            graph.setLowField(lowField);
        }

        String markerType = graphElement.attributeValue("markerType");
        if (StringUtils.isNotEmpty(markerType)) {
            graph.setMarkerType(MarkerType.valueOf(markerType));
        }

        String maxBulletSize = graphElement.attributeValue("maxBulletSize");
        if (StringUtils.isNotEmpty(maxBulletSize)) {
            graph.setMaxBulletSize(Integer.valueOf(maxBulletSize));
        }

        String minBulletSize = graphElement.attributeValue("minBulletSize");
        if (StringUtils.isNotEmpty(minBulletSize)) {
            graph.setMinBulletSize(Integer.valueOf(minBulletSize));
        }

        String minDistance = graphElement.attributeValue("minDistance");
        if (StringUtils.isNotEmpty(minDistance)) {
            graph.setMinDistance(Integer.valueOf(minDistance));
        }

        String negativeBase = graphElement.attributeValue("negativeBase");
        if (StringUtils.isNotEmpty(negativeBase)) {
            graph.setNegativeBase(Double.valueOf(negativeBase));
        }

        String negativeFillAlphas = graphElement.attributeValue("negativeFillAlphas");
        if (StringUtils.isNotEmpty(negativeFillAlphas)) {
            graph.setNegativeFillAlphas(Double.valueOf(negativeFillAlphas));
        }

        Element negativeFillColors = graphElement.element("negativeFillColors");
        if (negativeFillColors != null) {
            List<Color> colors = loadColors(negativeFillColors);
            if (CollectionUtils.isNotEmpty(colors)) {
                graph.setNegativeFillColors(colors);
            }
        }

        String negativeLineAlpha = graphElement.attributeValue("negativeLineAlpha");
        if (StringUtils.isNotEmpty(negativeLineAlpha)) {
            graph.setNegativeLineAlpha(Double.valueOf(negativeLineAlpha));
        }

        String negativeLineColor = graphElement.attributeValue("negativeLineColor");
        if (StringUtils.isNotEmpty(negativeLineColor)) {
            graph.setNegativeLineColor(Color.valueOf(negativeLineColor));
        }

        String newStack = graphElement.attributeValue("newStack");
        if (StringUtils.isNotEmpty(newStack)) {
            graph.setNewStack(Boolean.valueOf(newStack));
        }

        String noStepRisers = graphElement.attributeValue("noStepRisers");
        if (StringUtils.isNotEmpty(noStepRisers)) {
            graph.setNoStepRisers(Boolean.valueOf(noStepRisers));
        }

        String openField = graphElement.attributeValue("openField");
        if (StringUtils.isNotEmpty(openField)) {
            graph.setOpenField(openField);
        }

        Element patternElement = graphElement.element("pattern");
        if (patternElement != null) {
            graph.setPattern(loadPattern(patternElement));
        }

        String patternField = graphElement.attributeValue("patternField");
        if (StringUtils.isNotEmpty(patternField)) {
            graph.setPatternField(patternField);
        }

        String periodSpan = graphElement.attributeValue("periodSpan");
        if (StringUtils.isNotEmpty(periodSpan)) {
            graph.setPeriodSpan(Integer.valueOf(periodSpan));
        }

        String pointPosition = graphElement.attributeValue("pointPosition");
        if (StringUtils.isNotEmpty(pointPosition)) {
            graph.setPointPosition(PointPosition.valueOf(pointPosition));
        }

        String precision = graphElement.attributeValue("precision");
        if (StringUtils.isNotEmpty(precision)) {
            graph.setPrecision(Integer.valueOf(precision));
        }

        String proCandlesticks = graphElement.attributeValue("proCandlesticks");
        if (StringUtils.isNotEmpty(proCandlesticks)) {
            graph.setProCandlesticks(Boolean.valueOf(proCandlesticks));
        }

        String showAllValueLabels = graphElement.attributeValue("showAllValueLabels");
        if (StringUtils.isNotEmpty(showAllValueLabels)) {
            graph.setShowAllValueLabels(Boolean.valueOf(showAllValueLabels));
        }

        String showBalloon = graphElement.attributeValue("showBalloon");
        if (StringUtils.isNotEmpty(showBalloon)) {
            graph.setShowBalloon(Boolean.valueOf(showBalloon));
        }

        String showBalloonAt = graphElement.attributeValue("showBalloonAt");
        if (StringUtils.isNotEmpty(showBalloonAt)) {
            graph.setShowBalloonAt(ShowPositionOnCandle.valueOf(showBalloonAt));
        }

        String showBulletsAt = graphElement.attributeValue("showBulletsAt");
        if (StringUtils.isNotEmpty(showBulletsAt)) {
            graph.setShowBulletsAt(ShowPositionOnCandle.valueOf(showBulletsAt));
        }

        String showHandOnHover = graphElement.attributeValue("showHandOnHover");
        if (StringUtils.isNotEmpty(showHandOnHover)) {
            graph.setShowHandOnHover(Boolean.valueOf(showHandOnHover));
        }

        String showOnAxis = graphElement.attributeValue("showOnAxis");
        if (StringUtils.isNotEmpty(showOnAxis)) {
            graph.setShowOnAxis(Boolean.valueOf(showOnAxis));
        }

        String stackable = graphElement.attributeValue("stackable");
        if (StringUtils.isNotEmpty(stackable)) {
            graph.setStackable(Boolean.valueOf(stackable));
        }

        String stepDirection = graphElement.attributeValue("stepDirection");
        if (StringUtils.isNotEmpty(stepDirection)) {
            graph.setStepDirection(StepDirection.valueOf(stepDirection));
        }

        String switchable = graphElement.attributeValue("switchable");
        if (StringUtils.isNotEmpty(switchable)) {
            graph.setSwitchable(Boolean.valueOf(switchable));
        }

        String tabIndex = graphElement.attributeValue("tabIndex");
        if (StringUtils.isNotEmpty(tabIndex)) {
            graph.setTabIndex(Integer.valueOf(tabIndex));
        }

        String title = graphElement.attributeValue("title");
        if (StringUtils.isNotEmpty(title)) {
            graph.setTitle(loadResourceString(title));
        }

        String topRadius = graphElement.attributeValue("topRadius");
        if (StringUtils.isNotEmpty(topRadius)) {
            graph.setTopRadius(Integer.valueOf(topRadius));
        }

        String type = graphElement.attributeValue("type");
        if (StringUtils.isNotEmpty(type)) {
            graph.setType(GraphType.valueOf(type));
        }

        String urlField = graphElement.attributeValue("urlField");
        if (StringUtils.isNotEmpty(urlField)) {
            graph.setUrlField(urlField);
        }

        String urlTarget = graphElement.attributeValue("urlTarget");
        if (StringUtils.isNotEmpty(urlTarget)) {
            graph.setUrlTarget(urlTarget);
        }

        String useLineColorForBulletBorder = graphElement.attributeValue("useLineColorForBulletBorder");
        if (StringUtils.isNotEmpty(useLineColorForBulletBorder)) {
            graph.setUseLineColorForBulletBorder(Boolean.valueOf(useLineColorForBulletBorder));
        }

        String useNegativeColorIfDown = graphElement.attributeValue("useNegativeColorIfDown");
        if (StringUtils.isNotEmpty(useNegativeColorIfDown)) {
            graph.setUseNegativeColorIfDown(Boolean.valueOf(useNegativeColorIfDown));
        }

        String valueAxis = graphElement.attributeValue("valueAxis");
        if (StringUtils.isNotEmpty(valueAxis)) {
            graph.setValueAxis(valueAxis);
        }

        String valueField = graphElement.attributeValue("valueField");
        if (StringUtils.isNotEmpty(valueField)) {
            graph.setValueField(valueField);
        }

        String visibleInLegend = graphElement.attributeValue("visibleInLegend");
        if (StringUtils.isNotEmpty(visibleInLegend)) {
            graph.setVisibleInLegend(Boolean.valueOf(visibleInLegend));
        }

        String xAxis = graphElement.attributeValue("xAxis");
        if (StringUtils.isNotEmpty(xAxis)) {
            graph.setXAxis(xAxis);
        }

        String xField = graphElement.attributeValue("xField");
        if (StringUtils.isNotEmpty(xField)) {
            graph.setXField(xField);
        }

        String yAxis = graphElement.attributeValue("yAxis");
        if (StringUtils.isNotEmpty(yAxis)) {
            graph.setYAxis(yAxis);
        }

        String yField = graphElement.attributeValue("yField");
        if (StringUtils.isNotEmpty(yField)) {
            graph.setYField(yField);
        }
    }

    protected Pattern loadPattern(Element element) {
        Pattern pattern = new Pattern();

        String url = element.attributeValue("url");
        if (StringUtils.isNotEmpty(url)) {
            pattern.setUrl(url);
        }

        String width = element.attributeValue("width");
        if (StringUtils.isNotEmpty(width)) {
            pattern.setWidth(Integer.parseInt(width));
        }

        String height = element.attributeValue("height");
        if (StringUtils.isNotEmpty(height)) {
            pattern.setHeight(Integer.parseInt(height));
        }

        return pattern;
    }

    protected void loadSeriesBasedProperties(SeriesBasedChartModel chart, Element element) {
        loadCategoryAxis(chart, element);

        String balloonDateFormat = element.attributeValue("balloonDateFormat");
        if (StringUtils.isNotEmpty(balloonDateFormat)) {
            chart.setBalloonDateFormat(loadResourceString(balloonDateFormat));
        }

        String categoryField = element.attributeValue("categoryField");
        if (StringUtils.isNotEmpty(categoryField)) {
            chart.setCategoryField(categoryField);
        }

        String columnSpacing = element.attributeValue("columnSpacing");
        if (StringUtils.isNotEmpty(columnSpacing)) {
            chart.setColumnSpacing(Integer.valueOf(columnSpacing));
        }

        String columnSpacing3D = element.attributeValue("columnSpacing3D");
        if (StringUtils.isNotEmpty(columnSpacing3D)) {
            chart.setColumnSpacing3D(Integer.valueOf(columnSpacing3D));
        }

        String columnWidth = element.attributeValue("columnWidth");
        if (StringUtils.isNotEmpty(columnWidth)) {
            chart.setColumnWidth(Double.valueOf(columnWidth));
        }

        String dataDateFormat = element.attributeValue("dataDateFormat");
        if (StringUtils.isNotEmpty(dataDateFormat)) {
            chart.setDataDateFormat(dataDateFormat);
        }

        String maxSelectedSeries = element.attributeValue("maxSelectedSeries");
        if (StringUtils.isNotEmpty(maxSelectedSeries)) {
            chart.setMaxSelectedSeries(Integer.valueOf(maxSelectedSeries));
        }

        String minSelectedTime = element.attributeValue("minSelectedTime");
        if (StringUtils.isNotEmpty(minSelectedTime)) {
            chart.setMinSelectedTime(Long.valueOf(minSelectedTime));
        }

        String maxSelectedTime = element.attributeValue("maxSelectedTime");
        if (StringUtils.isNotEmpty(maxSelectedTime)) {
            chart.setMaxSelectedTime(Long.valueOf(maxSelectedTime));
        }

        String mouseWheelScrollEnabled = element.attributeValue("mouseWheelScrollEnabled");
        if (StringUtils.isNotEmpty(mouseWheelScrollEnabled)) {
            chart.setMouseWheelScrollEnabled(Boolean.valueOf(mouseWheelScrollEnabled));
        }

        String mouseWheelZoomEnabled = element.attributeValue("mouseWheelZoomEnabled");
        if (StringUtils.isNotEmpty(mouseWheelZoomEnabled)) {
            chart.setMouseWheelZoomEnabled(Boolean.valueOf(mouseWheelZoomEnabled));
        }

        String rotate = element.attributeValue("rotate");
        if (StringUtils.isNotEmpty(rotate)) {
            chart.setRotate(Boolean.valueOf(rotate));
        }

        String synchronizeGrid = element.attributeValue("synchronizeGrid");
        if (StringUtils.isNotEmpty(synchronizeGrid)) {
            chart.setSynchronizeGrid(Boolean.parseBoolean(synchronizeGrid));
        }

        Element scrollbarElement = element.element("valueScrollbar");
        if (scrollbarElement != null) {
            chart.setValueScrollbar(loadScrollbar(scrollbarElement));
        }

        String zoomOutOnDataUpdate = element.attributeValue("zoomOutOnDataUpdate");
        if (StringUtils.isNotEmpty(zoomOutOnDataUpdate)) {
            chart.setZoomOutOnDataUpdate(Boolean.valueOf(zoomOutOnDataUpdate));
        }
    }

    protected Scrollbar loadScrollbar(Element scrollbarElement) {
        Scrollbar scrollbar = new Scrollbar();

        String accessibleLabel = scrollbarElement.attributeValue("accessibleLabel");
        if (StringUtils.isNotEmpty(accessibleLabel)) {
            scrollbar.setAccessibleLabel(loadResourceString(accessibleLabel));
        }

        String autoGridCount = scrollbarElement.attributeValue("autoGridCount");
        if (StringUtils.isNotEmpty(autoGridCount)) {
            scrollbar.setAutoGridCount(Boolean.valueOf(autoGridCount));
        }

        String backgroundAlpha = scrollbarElement.attributeValue("backgroundAlpha");
        if (StringUtils.isNotEmpty(backgroundAlpha)) {
            scrollbar.setBackgroundAlpha(Double.valueOf(backgroundAlpha));
        }

        String backgroundColor = scrollbarElement.attributeValue("backgroundColor");
        if (StringUtils.isNotEmpty(backgroundColor)) {
            scrollbar.setBackgroundColor(Color.valueOf(backgroundColor));
        }

        String color = scrollbarElement.attributeValue("color");
        if (StringUtils.isNotEmpty(color)) {
            scrollbar.setColor(Color.valueOf(color));
        }

        String dragCursorDown = scrollbarElement.attributeValue("dragCursorDown");
        if (StringUtils.isNotEmpty(dragCursorDown)) {
            scrollbar.setDragCursorDown(dragCursorDown);
        }

        String dragCursorHover = scrollbarElement.attributeValue("dragCursorHover");
        if (StringUtils.isNotEmpty(dragCursorHover)) {
            scrollbar.setDragCursorHover(dragCursorHover);
        }

        String dragIcon = scrollbarElement.attributeValue("dragIcon");
        if (StringUtils.isNotEmpty(dragIcon)) {
            scrollbar.setDragIcon(dragIcon);
        }

        String dragIconHeight = scrollbarElement.attributeValue("dragIconHeight");
        if (StringUtils.isNotEmpty(dragIconHeight)) {
            scrollbar.setDragIconHeight(Integer.valueOf(dragIconHeight));
        }

        String dragIconWidth = scrollbarElement.attributeValue("dragIconWidth");
        if (StringUtils.isNotEmpty(dragIconWidth)) {
            scrollbar.setDragIconWidth(Integer.valueOf(dragIconWidth));
        }

        String enabled = scrollbarElement.attributeValue("enabled");
        if (StringUtils.isNotEmpty(enabled)) {
            scrollbar.setEnabled(Boolean.valueOf(enabled));
        }

        String graph = scrollbarElement.attributeValue("graph");
        if (StringUtils.isNotEmpty(graph)) {
            scrollbar.setGraph(graph);
        }

        String graphFillAlpha = scrollbarElement.attributeValue("graphFillAlpha");
        if (StringUtils.isNotEmpty(graphFillAlpha)) {
            scrollbar.setGraphFillAlpha(Double.valueOf(graphFillAlpha));
        }

        String graphFillColor = scrollbarElement.attributeValue("graphFillColor");
        if (StringUtils.isNotEmpty(graphFillColor)) {
            scrollbar.setGraphFillColor(Color.valueOf(graphFillColor));
        }

        String graphLineAlpha = scrollbarElement.attributeValue("graphLineAlpha");
        if (StringUtils.isNotEmpty(graphLineAlpha)) {
            scrollbar.setGraphLineAlpha(Double.valueOf(graphLineAlpha));
        }

        String graphLineColor = scrollbarElement.attributeValue("graphLineColor");
        if (StringUtils.isNotEmpty(graphLineColor)) {
            scrollbar.setGraphLineColor(Color.valueOf(graphLineColor));
        }

        String graphType = scrollbarElement.attributeValue("graphType");
        if (StringUtils.isNotEmpty(graphType)) {
            scrollbar.setGraphType(GraphType.valueOf(graphType));
        }

        String gridAlpha = scrollbarElement.attributeValue("gridAlpha");
        if (StringUtils.isNotEmpty(gridAlpha)) {
            scrollbar.setGridAlpha(Double.valueOf(gridAlpha));
        }

        String gridColor = scrollbarElement.attributeValue("gridColor");
        if (StringUtils.isNotEmpty(gridColor)) {
            scrollbar.setGridColor(Color.valueOf(gridColor));
        }

        String gridCount = scrollbarElement.attributeValue("gridCount");
        if (StringUtils.isNotEmpty(gridCount)) {
            scrollbar.setGridCount(Integer.valueOf(gridCount));
        }

        String hideResizeGrips = scrollbarElement.attributeValue("hideResizeGrips");
        if (StringUtils.isNotEmpty(hideResizeGrips)) {
            scrollbar.setHideResizeGrips(Boolean.valueOf(hideResizeGrips));
        }

        String ignoreCustomColors = scrollbarElement.attributeValue("ignoreCustomColors");
        if (StringUtils.isNotEmpty(ignoreCustomColors)) {
            scrollbar.setIgnoreCustomColors(Boolean.valueOf(ignoreCustomColors));
        }

        String maximum = scrollbarElement.attributeValue("maximum");
        if (StringUtils.isNotEmpty(maximum)) {
            scrollbar.setMaximum(Double.valueOf(maximum));
        }

        String minimum = scrollbarElement.attributeValue("minimum");
        if (StringUtils.isNotEmpty(minimum)) {
            scrollbar.setMinimum(Double.valueOf(minimum));
        }

        String offset = scrollbarElement.attributeValue("offset");
        if (StringUtils.isNotEmpty(offset)) {
            scrollbar.setOffset(Integer.valueOf(offset));
        }

        String oppositeAxis = scrollbarElement.attributeValue("oppositeAxis");
        if (StringUtils.isNotEmpty(oppositeAxis)) {
            scrollbar.setOppositeAxis(Boolean.valueOf(oppositeAxis));
        }

        String resizeEnabled = scrollbarElement.attributeValue("resizeEnabled");
        if (StringUtils.isNotEmpty(resizeEnabled)) {
            scrollbar.setResizeEnabled(Boolean.valueOf(resizeEnabled));
        }

        String scrollbarHeight = scrollbarElement.attributeValue("scrollbarHeight");
        if (StringUtils.isNotEmpty(scrollbarHeight)) {
            scrollbar.setScrollbarHeight(Integer.valueOf(scrollbarHeight));
        }

        String scrollDuration = scrollbarElement.attributeValue("scrollDuration");
        if (StringUtils.isNotEmpty(scrollDuration)) {
            scrollbar.setScrollDuration(Double.valueOf(scrollDuration));
        }

        String selectedBackgroundAlpha = scrollbarElement.attributeValue("selectedBackgroundAlpha");
        if (StringUtils.isNotEmpty(selectedBackgroundAlpha)) {
            scrollbar.setSelectedBackgroundAlpha(Double.valueOf(selectedBackgroundAlpha));
        }

        String selectedBackgroundColor = scrollbarElement.attributeValue("selectedBackgroundColor");
        if (StringUtils.isNotEmpty(selectedBackgroundColor)) {
            scrollbar.setSelectedBackgroundColor(Color.valueOf(selectedBackgroundColor));
        }

        String selectedGraphFillAlpha = scrollbarElement.attributeValue("selectedGraphFillAlpha");
        if (StringUtils.isNotEmpty(selectedGraphFillAlpha)) {
            scrollbar.setSelectedGraphFillAlpha(Double.valueOf(selectedGraphFillAlpha));
        }

        String selectedGraphFillColor = scrollbarElement.attributeValue("selectedGraphFillColor");
        if (StringUtils.isNotEmpty(selectedGraphFillColor)) {
            scrollbar.setSelectedGraphFillColor(Color.valueOf(selectedGraphFillColor));
        }

        String selectedGraphLineAlpha = scrollbarElement.attributeValue("selectedGraphLineAlpha");
        if (StringUtils.isNotEmpty(selectedGraphLineAlpha)) {
            scrollbar.setSelectedGraphLineAlpha(Double.valueOf(selectedGraphLineAlpha));
        }

        String selectedGraphLineColor = scrollbarElement.attributeValue("selectedGraphLineColor");
        if (StringUtils.isNotEmpty(selectedGraphLineColor)) {
            scrollbar.setSelectedGraphLineColor(Color.valueOf(selectedGraphLineColor));
        }

        String tabIndex = scrollbarElement.attributeValue("tabIndex");
        if (StringUtils.isNotEmpty(tabIndex)) {
            scrollbar.setTabIndex(Integer.valueOf(tabIndex));
        }

        String updateOnReleaseOnly = scrollbarElement.attributeValue("updateOnReleaseOnly");
        if (StringUtils.isNotEmpty(updateOnReleaseOnly)) {
            scrollbar.setUpdateOnReleaseOnly(Boolean.valueOf(updateOnReleaseOnly));
        }

        String hResizeCursor = scrollbarElement.attributeValue("hResizeCursor");
        if (StringUtils.isNotEmpty(hResizeCursor)) {
            scrollbar.setHResizeCursor(hResizeCursor);
        }

        String hResizeCursorDown = scrollbarElement.attributeValue("hResizeCursorDown");
        if (StringUtils.isNotEmpty(hResizeCursorDown)) {
            scrollbar.setHResizeCursorDown(hResizeCursorDown);
        }

        String hResizeCursorHover = scrollbarElement.attributeValue("hResizeCursorHover");
        if (StringUtils.isNotEmpty(hResizeCursorHover)) {
            scrollbar.setHResizeCursorHover(hResizeCursorHover);
        }

        String vResizeCursor = scrollbarElement.attributeValue("vResizeCursor");
        if (StringUtils.isNotEmpty(vResizeCursor)) {
            scrollbar.setVResizeCursor(vResizeCursor);
        }

        String vResizeCursorDown = scrollbarElement.attributeValue("vResizeCursorDown");
        if (StringUtils.isNotEmpty(vResizeCursorDown)) {
            scrollbar.setVResizeCursorDown(vResizeCursorDown);
        }

        String vResizeCursorHover = scrollbarElement.attributeValue("vResizeCursorHover");
        if (StringUtils.isNotEmpty(vResizeCursorHover)) {
            scrollbar.setVResizeCursorHover(vResizeCursorHover);
        }

        return scrollbar;
    }

    protected void loadCategoryAxis(SeriesBasedChartModel chart, Element element) {
        Element axisElement = element.element("categoryAxis");
        if (axisElement != null) {
            CategoryAxis axis = new CategoryAxis();

            loadAbstractAxis(axis, axisElement);

            String autoWrap = axisElement.attributeValue("autoWrap");
            if (StringUtils.isNotEmpty(autoWrap)) {
                axis.setAutoWrap(Boolean.valueOf(autoWrap));
            }

            String categoryFunction = axisElement.elementText("categoryFunction");
            if (StringUtils.isNotEmpty(categoryFunction)) {
                axis.setCategoryFunction(new JsFunction(categoryFunction));
            }

            String classNameField = axisElement.attributeValue("classNameField");
            if (StringUtils.isNotEmpty(classNameField)) {
                axis.setClassNameField(classNameField);
            }

            String centerLabelOnFullPeriod = axisElement.attributeValue("centerLabelOnFullPeriod");
            if (StringUtils.isNotEmpty(centerLabelOnFullPeriod)) {
                axis.setCenterLabelOnFullPeriod(Boolean.valueOf(centerLabelOnFullPeriod));
            }

            String equalSpacing = axisElement.attributeValue("equalSpacing");
            if (StringUtils.isNotEmpty(equalSpacing)) {
                axis.setEqualSpacing(Boolean.valueOf(equalSpacing));
            }

            String forceShowField = axisElement.attributeValue("forceShowField");
            if (StringUtils.isNotEmpty(forceShowField)) {
                axis.setForceShowField(forceShowField);
            }

            String gridPosition = axisElement.attributeValue("gridPosition");
            if (StringUtils.isNotEmpty(gridPosition)) {
                axis.setGridPosition(GridPosition.valueOf(gridPosition));
            }

            String labelFunction = axisElement.elementText("labelFunction");
            if (StringUtils.isNotBlank(labelFunction)) {
                axis.setLabelFunction(new JsFunction(labelFunction));
            }

            String labelColorField = axisElement.attributeValue("labelColorField");
            if (StringUtils.isNotEmpty(labelColorField)) {
                axis.setLabelColorField(labelColorField);
            }

            String minPeriod = axisElement.attributeValue("minPeriod");
            if (StringUtils.isNotEmpty(minPeriod)) {
                DatePeriod dp = DatePeriod.fromId(minPeriod);
                if (dp == null) {
                    dp = DatePeriod.valueOf(minPeriod);
                }
                axis.setMinPeriod(dp);
            }

            String parseDates = axisElement.attributeValue("parseDates");
            if (StringUtils.isNotEmpty(parseDates)) {
                axis.setParseDates(Boolean.valueOf(parseDates));
            }

            String startOnAxis = axisElement.attributeValue("startOnAxis");
            if (StringUtils.isNotEmpty(startOnAxis)) {
                axis.setStartOnAxis(Boolean.valueOf(startOnAxis));
            }

            String tickPosition = axisElement.attributeValue("tickPosition");
            if (StringUtils.isNotEmpty(tickPosition)) {
                axis.setTickPosition(tickPosition);
            }

            String twoLineMode = axisElement.attributeValue("twoLineMode");
            if (StringUtils.isNotEmpty(twoLineMode)) {
                axis.setTwoLineMode(Boolean.valueOf(twoLineMode));
            }

            String widthField = axisElement.attributeValue("widthField");
            if (StringUtils.isNotEmpty(widthField)) {
                axis.setWidthField(widthField);
            }

            chart.setCategoryAxis(axis);
        }
    }

    protected void loadRectangularProperties(RectangularChartModel chart, Element element) {
        loadTrendLines(chart, element);
        loadCursor(chart, element);

        String angle = element.attributeValue("angle");
        if (StringUtils.isNotEmpty(angle)) {
            chart.setAngle(Integer.valueOf(angle));
        }

        String autoMarginOffset = element.attributeValue("autoMarginOffset");
        if (StringUtils.isNotEmpty(autoMarginOffset)) {
            chart.setAutoMarginOffset(Integer.valueOf(autoMarginOffset));
        }

        String autoMargins = element.attributeValue("autoMargins");
        if (StringUtils.isNotEmpty(autoMargins)) {
            chart.setAutoMargins(Boolean.valueOf(autoMargins));
        }

        Element scrollbarElement = element.element("chartScrollbar");
        if (scrollbarElement != null) {
            chart.setChartScrollbar(loadScrollbar(scrollbarElement));
        }

        String depth3D = element.attributeValue("depth3D");
        if (StringUtils.isNotEmpty(depth3D)) {
            chart.setDepth3D(Integer.valueOf(depth3D));
        }

        loadMargins(chart, element);

        String marginsUpdated = element.attributeValue("marginsUpdated");
        if (StringUtils.isNotEmpty(marginsUpdated)) {
            chart.setMarginsUpdated(Boolean.valueOf(marginsUpdated));
        }

        String maxZoomFactor = element.attributeValue("maxZoomFactor");
        if (StringUtils.isNotEmpty(maxZoomFactor)) {
            chart.setMaxZoomFactor(Integer.valueOf(maxZoomFactor));
        }

        String minMarginBottom = element.attributeValue("minMarginBottom");
        if (StringUtils.isNotEmpty(minMarginBottom)) {
            chart.setMinMarginBottom(Integer.valueOf(minMarginBottom));
        }

        String minMarginLeft = element.attributeValue("minMarginLeft");
        if (StringUtils.isNotEmpty(minMarginLeft)) {
            chart.setMinMarginLeft(Integer.valueOf(minMarginLeft));
        }

        String minMarginRight = element.attributeValue("minMarginRight");
        if (StringUtils.isNotEmpty(minMarginRight)) {
            chart.setMinMarginRight(Integer.valueOf(minMarginRight));
        }

        String minMarginTop = element.attributeValue("minMarginTop");
        if (StringUtils.isNotEmpty(minMarginTop)) {
            chart.setMinMarginTop(Integer.valueOf(minMarginTop));
        }

        String plotAreaBorderAlpha = element.attributeValue("plotAreaBorderAlpha");
        if (StringUtils.isNotEmpty(plotAreaBorderAlpha)) {
            chart.setPlotAreaBorderAlpha(Double.valueOf(plotAreaBorderAlpha));
        }

        String plotAreaBorderColor = element.attributeValue("plotAreaBorderColor");
        if (StringUtils.isNotEmpty(plotAreaBorderColor)) {
            chart.setPlotAreaBorderColor(Color.valueOf(plotAreaBorderColor));
        }

        String plotAreaFillAlphas = element.attributeValue("plotAreaFillAlphas");
        if (StringUtils.isNotEmpty(plotAreaFillAlphas)) {
            chart.setPlotAreaFillAlphas(Double.valueOf(plotAreaFillAlphas));
        }

        Element plotAreaFillColors = element.element("plotAreaFillColors");
        if (plotAreaFillColors != null) {
            List<Color> colors = loadColors(plotAreaFillColors);
            if (CollectionUtils.isNotEmpty(colors)) {
                chart.setPlotAreaFillColors(colors);
            }
        }

        String plotAreaGradientAngle = element.attributeValue("plotAreaGradientAngle");
        if (StringUtils.isNotEmpty(plotAreaGradientAngle)) {
            chart.setPlotAreaGradientAngle(Integer.valueOf(plotAreaGradientAngle));
        }

        String zoomOutButtonAlpha = element.attributeValue("zoomOutButtonAlpha");
        if (StringUtils.isNotEmpty(zoomOutButtonAlpha)) {
            chart.setZoomOutButtonAlpha(Double.valueOf(zoomOutButtonAlpha));
        }

        String zoomOutButtonColor = element.attributeValue("zoomOutButtonColor");
        if (StringUtils.isNotEmpty(zoomOutButtonColor)) {
            chart.setZoomOutButtonColor(Color.valueOf(zoomOutButtonColor));
        }

        String zoomOutButtonImage = element.attributeValue("zoomOutButtonImage");
        if (StringUtils.isNotEmpty(zoomOutButtonImage)) {
            chart.setZoomOutButtonImage(zoomOutButtonImage);
        }

        String zoomOutButtonImageSize = element.attributeValue("zoomOutButtonImageSize");
        if (StringUtils.isNotEmpty(zoomOutButtonImageSize)) {
            chart.setZoomOutButtonImageSize(Integer.valueOf(zoomOutButtonImageSize));
        }

        String zoomOutButtonPadding = element.attributeValue("zoomOutButtonPadding");
        if (StringUtils.isNotEmpty(zoomOutButtonPadding)) {
            chart.setZoomOutButtonPadding(Integer.valueOf(zoomOutButtonPadding));
        }

        String zoomOutButtonRollOverAlpha = element.attributeValue("zoomOutButtonRollOverAlpha");
        if (StringUtils.isNotEmpty(zoomOutButtonRollOverAlpha)) {
            chart.setZoomOutButtonRollOverAlpha(Double.valueOf(zoomOutButtonRollOverAlpha));
        }

        String zoomOutButtonTabIndex = element.attributeValue("zoomOutButtonTabIndex");
        if (StringUtils.isNotEmpty(zoomOutButtonTabIndex)) {
            chart.setZoomOutButtonTabIndex(Integer.valueOf(zoomOutButtonTabIndex));
        }

        String zoomOutText = element.attributeValue("zoomOutText");
        if (StringUtils.isNotEmpty(zoomOutText)) {
            chart.setZoomOutText(loadResourceString(zoomOutText));
        }
    }

    protected void loadCursor(RectangularChartModel chart, Element element) {
        Element cursorElement = element.element("chartCursor");
        if (cursorElement != null) {
            Cursor cursor = new Cursor();

            String adjustment = cursorElement.attributeValue("adjustment");
            if (StringUtils.isNotEmpty(adjustment)) {
                cursor.setAdjustment(Integer.valueOf(adjustment));
            }

            String categoryBalloonFunction = cursorElement.elementText("categoryBalloonFunction");
            if (StringUtils.isNotEmpty(categoryBalloonFunction)) {
                cursor.setCategoryBalloonFunction(new JsFunction(categoryBalloonFunction));
            }

            String animationDuration = cursorElement.attributeValue("animationDuration");
            if (StringUtils.isNotEmpty(animationDuration)) {
                cursor.setAnimationDuration(Double.valueOf(animationDuration));
            }

            String avoidBalloonOverlapping = cursorElement.attributeValue("avoidBalloonOverlapping");
            if (StringUtils.isNotEmpty(avoidBalloonOverlapping)) {
                cursor.setAvoidBalloonOverlapping(Boolean.valueOf(avoidBalloonOverlapping));
            }

            String balloonPointerOrientation = cursorElement.attributeValue("balloonPointerOrientation");
            if (StringUtils.isNotEmpty(balloonPointerOrientation)) {
                cursor.setBalloonPointerOrientation(balloonPointerOrientation);
            }

            String bulletsEnabled = cursorElement.attributeValue("bulletsEnabled");
            if (StringUtils.isNotEmpty(bulletsEnabled)) {
                cursor.setBulletsEnabled(Boolean.valueOf(bulletsEnabled));
            }

            String bulletSize = cursorElement.attributeValue("bulletSize");
            if (StringUtils.isNotEmpty(bulletSize)) {
                cursor.setBulletSize(Integer.valueOf(bulletSize));
            }

            String categoryBalloonAlpha = cursorElement.attributeValue("categoryBalloonAlpha");
            if (StringUtils.isNotEmpty(categoryBalloonAlpha)) {
                cursor.setCategoryBalloonAlpha(Double.valueOf(categoryBalloonAlpha));
            }

            String categoryBalloonColor = cursorElement.attributeValue("categoryBalloonColor");
            if (StringUtils.isNotEmpty(categoryBalloonColor)) {
                cursor.setCategoryBalloonColor(Color.valueOf(categoryBalloonColor));
            }

            String categoryBalloonDateFormat = cursorElement.attributeValue("categoryBalloonDateFormat");
            if (StringUtils.isNotEmpty(categoryBalloonDateFormat)) {
                cursor.setCategoryBalloonDateFormat(loadResourceString(categoryBalloonDateFormat));
            }

            String categoryBalloonEnabled = cursorElement.attributeValue("categoryBalloonEnabled");
            if (StringUtils.isNotEmpty(categoryBalloonEnabled)) {
                cursor.setCategoryBalloonEnabled(Boolean.valueOf(categoryBalloonEnabled));
            }

            String categoryBalloonText = cursorElement.attributeValue("categoryBalloonText");
            if (StringUtils.isNotEmpty(categoryBalloonText)) {
                cursor.setCategoryBalloonText(categoryBalloonText);
            }

            String color = cursorElement.attributeValue("color");
            if (StringUtils.isNotEmpty(color)) {
                cursor.setColor(Color.valueOf(color));
            }

            String cursorAlpha = cursorElement.attributeValue("cursorAlpha");
            if (StringUtils.isNotEmpty(cursorAlpha)) {
                cursor.setCursorAlpha(Double.valueOf(cursorAlpha));
            }

            String cursorColor = cursorElement.attributeValue("cursorColor");
            if (StringUtils.isNotEmpty(cursorColor)) {
                cursor.setCursorColor(Color.valueOf(cursorColor));
            }

            String cursorPosition = cursorElement.attributeValue("cursorPosition");
            if (StringUtils.isNotEmpty(cursorPosition)) {
                cursor.setCursorPosition(CursorPosition.valueOf(cursorPosition));
            }

            String enabled = cursorElement.attributeValue("enabled");
            if (StringUtils.isNotEmpty(enabled)) {
                cursor.setEnabled(Boolean.valueOf(enabled));
            }

            String fullWidth = cursorElement.attributeValue("fullWidth");
            if (StringUtils.isNotEmpty(fullWidth)) {
                cursor.setFullWidth(Boolean.valueOf(fullWidth));
            }

            String graphBulletAlpha = cursorElement.attributeValue("graphBulletAlpha");
            if (StringUtils.isNotEmpty(graphBulletAlpha)) {
                cursor.setGraphBulletAlpha(Double.valueOf(graphBulletAlpha));
            }

            String graphBulletSize = cursorElement.attributeValue("graphBulletSize");
            if (StringUtils.isNotEmpty(graphBulletSize)) {
                cursor.setGraphBulletSize(Double.valueOf(graphBulletSize));
            }

            String oneBalloonOnly = cursorElement.attributeValue("oneBalloonOnly");
            if (StringUtils.isNotEmpty(oneBalloonOnly)) {
                cursor.setOneBalloonOnly(Boolean.valueOf(oneBalloonOnly));
            }

            String leaveAfterTouch = cursorElement.attributeValue("leaveAfterTouch");
            if (StringUtils.isNotEmpty(leaveAfterTouch)) {
                cursor.setLeaveAfterTouch(Boolean.valueOf(leaveAfterTouch));
            }

            String leaveCursor = cursorElement.attributeValue("leaveCursor");
            if (StringUtils.isNotEmpty(leaveCursor)) {
                cursor.setLeaveCursor(Boolean.valueOf(leaveCursor));
            }

            String limitToGraph = cursorElement.attributeValue("limitToGraph");
            if (StringUtils.isNotEmpty(limitToGraph)) {
                cursor.setLimitToGraph(limitToGraph);
            }

            String pan = cursorElement.attributeValue("pan");
            if (StringUtils.isNotEmpty(pan)) {
                cursor.setPan(Boolean.valueOf(pan));
            }

            String selectionAlpha = cursorElement.attributeValue("selectionAlpha");
            if (StringUtils.isNotEmpty(selectionAlpha)) {
                cursor.setCursorAlpha(Double.valueOf(selectionAlpha));
            }

            String selectWithoutZooming = cursorElement.attributeValue("selectWithoutZooming");
            if (StringUtils.isNotEmpty(selectWithoutZooming)) {
                cursor.setSelectWithoutZooming(Boolean.valueOf(selectWithoutZooming));
            }

            String showNextAvailable = cursorElement.attributeValue("showNextAvailable");
            if (StringUtils.isNotEmpty(showNextAvailable)) {
                cursor.setShowNextAvailable(Boolean.valueOf(showNextAvailable));
            }

            String valueBalloonsEnabled = cursorElement.attributeValue("valueBalloonsEnabled");
            if (StringUtils.isNotEmpty(valueBalloonsEnabled)) {
                cursor.setValueBalloonsEnabled(Boolean.valueOf(valueBalloonsEnabled));
            }

            String valueLineAlpha = cursorElement.attributeValue("valueLineAlpha");
            if (StringUtils.isNotEmpty(valueLineAlpha)) {
                cursor.setValueLineAlpha(Double.valueOf(valueLineAlpha));
            }

            String valueLineAxis = cursorElement.attributeValue("valueLineAxis");
            if (StringUtils.isNotEmpty(valueLineAxis)) {
                cursor.setValueLineAxis(valueLineAxis);
            }

            String valueLineBalloonEnabled = cursorElement.attributeValue("valueLineBalloonEnabled");
            if (StringUtils.isNotEmpty(valueLineBalloonEnabled)) {
                cursor.setValueLineBalloonEnabled(Boolean.valueOf(valueLineBalloonEnabled));
            }

            String valueLineEnabled = cursorElement.attributeValue("valueLineEnabled");
            if (StringUtils.isNotEmpty(valueLineEnabled)) {
                cursor.setValueLineEnabled(Boolean.valueOf(valueLineEnabled));
            }

            String valueZoomable = cursorElement.attributeValue("valueZoomable");
            if (StringUtils.isNotEmpty(valueZoomable)) {
                cursor.setValueZoomable(Boolean.valueOf(valueZoomable));
            }

            String zoomable = cursorElement.attributeValue("zoomable");
            if (StringUtils.isNotEmpty(zoomable)) {
                cursor.setZoomable(Boolean.valueOf(zoomable));
            }

            String tabIndex = cursorElement.attributeValue("tabIndex");
            if (StringUtils.isNotEmpty(tabIndex)) {
                cursor.setTabIndex(Integer.parseInt(tabIndex));
            }

            chart.setChartCursor(cursor);
        }
    }

    protected void loadTrendLines(RectangularChartModel chart, Element element) {
        Element trendLinesElement = element.element("trendLines");
        if (trendLinesElement != null) {
            for (Object trendLineItem : trendLinesElement.elements("trendLine")) {

                Element trendLineElement = (Element) trendLineItem;

                TrendLine trendLine = new TrendLine();

                String balloonText = trendLineElement.attributeValue("balloonText");
                if (StringUtils.isNotEmpty(balloonText)) {
                    trendLine.setBalloonText(balloonText);
                }

                String dashLength = trendLineElement.attributeValue("dashLength");
                if (StringUtils.isNotEmpty(dashLength)) {
                    trendLine.setDashLength(Integer.valueOf(dashLength));
                }

                String finalCategory = trendLineElement.attributeValue("finalCategory");
                if (StringUtils.isNotEmpty(finalCategory)) {
                    trendLine.setFinalCategory(finalCategory);
                }

                String finalDate = trendLineElement.attributeValue("finalDate");
                if (StringUtils.isNotEmpty(finalDate)) {
                    trendLine.setFinalDate(loadDate(finalDate));
                }

                Element finalImageElement = trendLineElement.element("finalImage");
                if (finalImageElement != null) {
                    trendLine.setFinalImage(loadImage(finalImageElement));
                }

                String finalValue = trendLineElement.attributeValue("finalValue");
                if (StringUtils.isNotEmpty(finalValue)) {
                    trendLine.setFinalValue(Double.valueOf(finalValue));
                }

                String finalXValue = trendLineElement.attributeValue("finalXValue");
                if (StringUtils.isNotEmpty(finalXValue)) {
                    trendLine.setFinalXValue(Double.valueOf(finalXValue));
                }

                String id = trendLineElement.attributeValue("id");
                if (StringUtils.isNotEmpty(id)) {
                    trendLine.setId(id);
                }

                String initialCategory = trendLineElement.attributeValue("initialCategory");
                if (StringUtils.isNotEmpty(initialCategory)) {
                    trendLine.setInitialCategory(initialCategory);
                }

                String initialDate = trendLineElement.attributeValue("initialDate");
                if (StringUtils.isNotEmpty(initialDate)) {
                    trendLine.setInitialDate(loadDate(initialDate));
                }

                Element initialImageElement = trendLineElement.element("initialImage");
                if (initialImageElement != null) {
                    trendLine.setFinalImage(loadImage(initialImageElement));
                }

                String initialValue = trendLineElement.attributeValue("initialValue");
                if (StringUtils.isNotEmpty(initialValue)) {
                    trendLine.setInitialValue(Double.valueOf(initialValue));
                }

                String initialXValue = trendLineElement.attributeValue("initialXValue");
                if (StringUtils.isNotEmpty(initialXValue)) {
                    trendLine.setInitialXValue(Double.valueOf(initialXValue));
                }

                String isProtected = trendLineElement.attributeValue("isProtected");
                if (StringUtils.isNotEmpty(isProtected)) {
                    trendLine.setProtected(Boolean.valueOf(isProtected));
                }

                String lineAlpha = trendLineElement.attributeValue("lineAlpha");
                if (StringUtils.isNotEmpty(lineAlpha)) {
                    trendLine.setLineAlpha(Double.valueOf(lineAlpha));
                }

                String lineColor = trendLineElement.attributeValue("lineColor");
                if (StringUtils.isNotEmpty(lineColor)) {
                    trendLine.setLineColor(Color.valueOf(lineColor));
                }

                String lineThickness = trendLineElement.attributeValue("lineThickness");
                if (StringUtils.isNotEmpty(lineThickness)) {
                    trendLine.setLineThickness(Integer.valueOf(lineThickness));
                }

                String valueAxis = trendLineElement.attributeValue("valueAxis");
                if (StringUtils.isNotEmpty(valueAxis)) {
                    trendLine.setValueAxis(valueAxis);
                }

                String valueAxisX = trendLineElement.attributeValue("valueAxisX");
                if (StringUtils.isNotEmpty(valueAxisX)) {
                    trendLine.setValueAxisX(valueAxisX);
                }

                chart.addTrendLines(trendLine);
            }
        }
    }

    protected Image loadImage(Element element) {
        Image image = new Image();

        String balloonColor = element.attributeValue("balloonColor");
        if (StringUtils.isNotEmpty(balloonColor)) {
            image.setBalloonColor(Color.valueOf(balloonColor));
        }

        String balloonText = element.attributeValue("balloonText");
        if (StringUtils.isNotEmpty(balloonText)) {
            image.setBalloonText(balloonText);
        }

        String color = element.attributeValue("color");
        if (StringUtils.isNotEmpty(color)) {
            image.setColor(Color.valueOf(color));
        }

        String height = element.attributeValue("height");
        if (StringUtils.isNotEmpty(height)) {
            image.setHeight(Integer.parseInt(height));
        }

        String offsetX = element.attributeValue("offsetX");
        if (StringUtils.isNotEmpty(offsetX)) {
            image.setOffsetX(Integer.parseInt(offsetX));
        }

        String offsetY = element.attributeValue("offsetY");
        if (StringUtils.isNotEmpty(offsetY)) {
            image.setOffsetY(Integer.parseInt(offsetY));
        }

        String outlineColor = element.attributeValue("outlineColor");
        if (StringUtils.isNotEmpty(outlineColor)) {
            image.setOutlineColor(Color.valueOf(outlineColor));
        }

        String rotation = element.attributeValue("rotation");
        if (StringUtils.isNotEmpty(rotation)) {
            image.setRotation(Integer.parseInt(rotation));
        }

        String svgPath = element.attributeValue("svgPath");
        if (StringUtils.isNotEmpty(svgPath)) {
            image.setSvgPath(svgPath);
        }

        String url = element.attributeValue("url");
        if (StringUtils.isNotEmpty(url)) {
            image.setUrl(url);
        }

        String width = element.attributeValue("width");
        if (StringUtils.isNotEmpty(width)) {
            image.setWidth(Integer.parseInt(width));
        }

        return image;
    }

    protected void loadStartEffect(HasStartEffect chart, Element element) {
        String startDuration = element.attributeValue("startDuration");
        if (StringUtils.isNotEmpty(startDuration)) {
            chart.setStartDuration(Double.parseDouble(startDuration));
        }

        String startEffect = element.attributeValue("startEffect");
        if (StringUtils.isNotEmpty(startEffect)) {
            chart.setStartEffect(AnimationEffect.valueOf(startEffect));
        }
    }

    protected void loadCoordinateProperties(CoordinateChartModel chart, Element element) {
        loadChartData(chart, element);
        loadColors(chart, element);
        loadGraphs(chart, element);
        loadValueAxes(chart, element);

        loadStartEffect(chart, element);

        String gridAboveGraphs = element.attributeValue("gridAboveGraphs");
        if (StringUtils.isNotEmpty(gridAboveGraphs)) {
            chart.setGridAboveGraphs(Boolean.valueOf(gridAboveGraphs));
        }

        Element guidesElement = element.element("guides");
        if (guidesElement != null) {
            chart.setGuides(loadGuides(guidesElement));
        }

        String sequencedAnimation = element.attributeValue("sequencedAnimation");
        if (StringUtils.isNotEmpty(sequencedAnimation)) {
            chart.setSequencedAnimation(Boolean.valueOf(sequencedAnimation));
        }

        String startAlpha = element.attributeValue("startAlpha");
        if (StringUtils.isNotEmpty(startAlpha)) {
            chart.setStartAlpha(Double.valueOf(startAlpha));
        }

        String urlTarget = element.attributeValue("urlTarget");
        if (StringUtils.isNotEmpty(urlTarget)) {
            chart.setUrlTarget(urlTarget);
        }
    }

    protected void loadGraphs(CoordinateChartModel chart, Element element) {
        Element graphsElement = element.element("graphs");
        if (graphsElement != null) {
            for (Object graphItem : graphsElement.elements("graph")) {
                Element graphElement = (Element) graphItem;
                Graph graph = new Graph();
                loadGraph(graph, graphElement);
                chart.addGraphs(graph);
            }
        }
    }

    protected void loadValueAxes(CoordinateChartModel chart, Element element) {
        Element valueAxesElement = element.element("valueAxes");
        if (valueAxesElement != null) {
            for (Object axisItem : valueAxesElement.elements("axis")) {
                Element axisElement = (Element) axisItem;

                ValueAxis axis = loadValueAxis(axisElement);

                String labelFunction = valueAxesElement.elementText("labelFunction");
                if (StringUtils.isNotBlank(labelFunction)) {
                    axis.setLabelFunction(new JsFunction(labelFunction));
                }

                chart.addValueAxes(axis);
            }
        }
    }

    protected void loadColors(HasColors chart, Element element) {
        Element colorsElement = element.element("colors");
        if (colorsElement != null) {
            List<Color> colors = loadColors(colorsElement);
            if (CollectionUtils.isNotEmpty(colors)) {
                chart.setColors(colors);
            }
        }
    }

    protected void loadChartData(ChartModel chart, Element element) {
        Element dataElement = element.element("data");
        if (dataElement != null) {
            ListDataProvider listDataProvider = new ListDataProvider();

            for (Object item : dataElement.elements("item")) {
                Element itemElement = (Element) item;
                MapDataItem dataItem = new MapDataItem();

                for (Element property :  itemElement.elements("property")) {
                    loadDataItem(property, dataItem);
                }

                listDataProvider.addItem(dataItem);
                chart.setDataProvider(listDataProvider);
            }
        }
    }

    protected void loadLabels(ChartModel chart, Element element) {
        Element allLabels = element.element("allLabels");
        if (allLabels != null) {
            for (Object labelItem : allLabels.elements("label")) {
                Element labelElement = (Element) labelItem;

                Label label = new Label();

                String align = labelElement.attributeValue("align");
                if (StringUtils.isNotEmpty(align)) {
                    label.setAlign(Align.valueOf(align));
                }

                String alpha = labelElement.attributeValue("alpha");
                if (StringUtils.isNotEmpty(alpha)) {
                    label.setAlpha(Double.valueOf(alpha));
                }

                String bold = labelElement.attributeValue("bold");
                if (StringUtils.isNotEmpty(bold)) {
                    label.setBold(Boolean.valueOf(bold));
                }

                String color = labelElement.attributeValue("color");
                if (StringUtils.isNotEmpty(color)) {
                    label.setColor(Color.valueOf(color));
                }

                String id = labelElement.attributeValue("id");
                if (StringUtils.isNotEmpty(id)) {
                    label.setId(id);
                }

                String rotation = labelElement.attributeValue("rotation");
                if (StringUtils.isNotEmpty(rotation)) {
                    label.setRotation(Integer.parseInt(rotation));
                }

                String size = labelElement.attributeValue("size");
                if (StringUtils.isNotEmpty(size)) {
                    label.setSize(Integer.parseInt(size));
                }

                String text = labelElement.attributeValue("text");
                if (StringUtils.isNotEmpty(text)) {
                    label.setText(loadResourceString(text));
                }

                String tabIndex = labelElement.attributeValue("tabIndex");
                if (StringUtils.isNotEmpty(tabIndex)) {
                    label.setTabIndex(Integer.parseInt(tabIndex));
                }

                String url = labelElement.attributeValue("url");
                if (StringUtils.isNotEmpty(url)) {
                    label.setUrl(url);
                }

                String x = labelElement.attributeValue("x");
                if (StringUtils.isNotEmpty(x)) {
                    label.setX(x);
                }

                String y = labelElement.attributeValue("y");
                if (StringUtils.isNotEmpty(y)) {
                    label.setY(y);
                }

                chart.addLabels(label);
            }
        }
    }

    protected void loadTitles(ChartModel chart, Element element) {
        Element titles = element.element("titles");
        if (titles != null) {
            for (Object titleItem : titles.elements("title")) {
                Element titleElement = (Element) titleItem;

                Title title = new Title();

                String alpha = titleElement.attributeValue("alpha");
                if (StringUtils.isNotEmpty(alpha)) {
                    title.setAlpha(Double.valueOf(alpha));
                }

                String bold = titleElement.attributeValue("bold");
                if (StringUtils.isNotEmpty(bold)) {
                    title.setBold(Boolean.valueOf(bold));
                }

                String color = titleElement.attributeValue("color");
                if (StringUtils.isNotEmpty(color)) {
                    title.setColor(Color.valueOf(color));
                }

                String id = titleElement.attributeValue("id");
                if (StringUtils.isNotEmpty(id)) {
                    title.setId(id);
                }

                String size = titleElement.attributeValue("size");
                if (StringUtils.isNotEmpty(size)) {
                    title.setSize(Integer.parseInt(size));
                }

                String tabIndex = titleElement.attributeValue("tabIndex");
                if (StringUtils.isNotEmpty(tabIndex)) {
                    title.setTabIndex(Integer.parseInt(tabIndex));
                }

                String text = titleElement.attributeValue("text");
                if (StringUtils.isNotEmpty(text)) {
                    title.setText(loadResourceString(text));
                }

                chart.addTitles(title);
            }
        }
    }

    protected void loadBaseProperties(ChartModel chart, Element element) {
        loadLabels(chart, element);
        loadTitles(chart, element);

        String accessible = element.attributeValue("accessible");
        if (StringUtils.isNotEmpty(accessible)) {
            chart.setAccessible(Boolean.valueOf(accessible));
        }

        String accessibleTitle = element.attributeValue("accessibleTitle");
        if (StringUtils.isNotEmpty(accessibleTitle)) {
            chart.setAccessibleTitle(accessibleTitle);
        }

        String accessibleDescription = element.attributeValue("accessibleDescription");
        if (StringUtils.isNotEmpty(accessibleDescription)) {
            chart.setAccessibleDescription(accessibleDescription);
        }

        String addClassNames = element.attributeValue("addClassNames");
        if (StringUtils.isNotEmpty(addClassNames)) {
            chart.setAddClassNames(Boolean.valueOf(addClassNames));
        }

        String additionalFields = element.attributeValue("additionalFields");
        if (StringUtils.isNotEmpty(additionalFields)) {
            List<String> fields = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(additionalFields);

            chart.addAdditionalFields(fields.toArray(new String[fields.size()]));
        }

        String autoDisplay = element.attributeValue("autoDisplay");
        if (StringUtils.isNotEmpty(autoDisplay)) {
            chart.setAutoDisplay(Boolean.valueOf(autoDisplay));
        }

        String autoResize = element.attributeValue("autoResize");
        if (StringUtils.isNotEmpty(autoResize)) {
            chart.setAutoResize(Boolean.valueOf(autoResize));
        }

        String autoTransform = element.attributeValue("autoTransform");
        if (StringUtils.isNotEmpty(autoTransform)) {
            chart.setAutoTransform(Boolean.valueOf(autoTransform));
        }

        String backgroundAlpha = element.attributeValue("backgroundAlpha");
        if (StringUtils.isNotEmpty(backgroundAlpha)) {
            chart.setBackgroundAlpha(Double.valueOf(backgroundAlpha));
        }

        String backgroundColor = element.attributeValue("backgroundColor");
        if (StringUtils.isNotEmpty(backgroundColor)) {
            chart.setBackgroundColor(Color.valueOf(backgroundColor));
        }

        Element balloonElement = element.element("balloon");
        if (balloonElement != null) {
            chart.setBalloon(loadBalloon(balloonElement));
        }

        String classNamePrefix = element.attributeValue("classNamePrefix");
        if (StringUtils.isNotEmpty(classNamePrefix)) {
            chart.setClassNamePrefix(classNamePrefix);
        }

        chart.setCreditsPosition(loadCreditsPosition(element));

        String borderAlpha = element.attributeValue("borderAlpha");
        if (StringUtils.isNotEmpty(borderAlpha)) {
            chart.setBorderAlpha(Double.valueOf(borderAlpha));
        }

        String borderColor = element.attributeValue("borderColor");
        if (StringUtils.isNotEmpty(borderColor)) {
            chart.setBorderColor(Color.valueOf(borderColor));
        }

        String color = element.attributeValue("color");
        if (StringUtils.isNotEmpty(color)) {
            chart.setColor(Color.valueOf(color));
        }

        String decimalSeparator = element.attributeValue("decimalSeparator");
        if (StringUtils.isNotEmpty(decimalSeparator)) {
            chart.setDecimalSeparator(decimalSeparator);
        }

        Element exportElement = element.element("export");
        if (exportElement != null) {
            chart.setExport(loadExport(exportElement));
        }

        String fontFamily = element.attributeValue("fontFamily");
        if (StringUtils.isEmpty(fontFamily)) {
            chart.setFontFamily(fontFamily);
        }

        String fontSize = element.attributeValue("fontSize");
        if (StringUtils.isNotEmpty(fontSize)) {
            chart.setFontSize(Integer.parseInt(fontSize));
        }

        String handDrawn = element.attributeValue("handDrawn");
        if (StringUtils.isNotEmpty(handDrawn)) {
            chart.setHandDrawn(Boolean.valueOf(handDrawn));
        }

        String handDrawScatter = element.attributeValue("handDrawScatter");
        if (StringUtils.isNotEmpty(handDrawScatter)) {
            chart.setHandDrawScatter(Integer.parseInt(handDrawScatter));
        }

        String handDrawThickness = element.attributeValue("handDrawThickness");
        if (StringUtils.isNotEmpty(handDrawThickness)) {
            chart.setHandDrawThickness(Integer.parseInt(handDrawThickness));
        }

        String hideBalloonTime = element.attributeValue("hideBalloonTime");
        if (StringUtils.isNotEmpty(hideBalloonTime)) {
            chart.setHideBalloonTime(Integer.parseInt(hideBalloonTime));
        }

        String language = element.attributeValue("language");
        if (StringUtils.isNotEmpty(language)) {
            chart.setLanguage(language);
        }

        Element legendElement = element.element("legend");
        if (legendElement != null) {
            Legend legend = new Legend();
            loadLegend(legend, legendElement);
            chart.setLegend(legend);
        }

        String panEventsEnabled = element.attributeValue("panEventsEnabled");
        if (StringUtils.isNotEmpty(panEventsEnabled)) {
            chart.setPanEventsEnabled(Boolean.valueOf(panEventsEnabled));
        }

        String percentPrecision = element.attributeValue("percentPrecision");
        if (StringUtils.isNotEmpty(percentPrecision)) {
            chart.setPercentPrecision(Integer.parseInt(percentPrecision));
        }

        String precision = element.attributeValue("precision");
        if (StringUtils.isNotEmpty(precision)) {
            chart.setPrecision(Integer.parseInt(precision));
        }

        String processCount = element.attributeValue("processCount");
        if (StringUtils.isNotEmpty(processCount)) {
            chart.setProcessCount(Integer.parseInt(processCount));
        }

        String processTimeout = element.attributeValue("processTimeout");
        if (StringUtils.isNotEmpty(processTimeout)) {
            chart.setProcessTimeout(Integer.parseInt(processTimeout));
        }

        String svgIcons = element.attributeValue("svgIcons");
        if (StringUtils.isNotEmpty(svgIcons)) {
            chart.setSvgIcons(Boolean.valueOf(svgIcons));
        }

        String tapToActivate = element.attributeValue("tapToActivate");
        if (StringUtils.isNotEmpty(tapToActivate)) {
            chart.setTapToActivate(Boolean.valueOf(tapToActivate));
        }

        String usePrefixes = element.attributeValue("usePrefixes");
        if (StringUtils.isNotEmpty(usePrefixes)) {
            chart.setUsePrefixes(Boolean.valueOf(usePrefixes));
        }

        String theme = element.attributeValue("theme");
        if (StringUtils.isNotEmpty(theme)) {
            chart.setTheme(ChartTheme.valueOf(theme));
        }

        String thousandsSeparator = element.attributeValue("thousandsSeparator");
        if (StringUtils.isNotEmpty(thousandsSeparator)) {
            chart.setThousandsSeparator(thousandsSeparator);
        }

        String touchClickDuration = element.attributeValue("touchClickDuration");
        if (StringUtils.isNotEmpty(touchClickDuration)) {
            chart.setTouchClickDuration(Integer.parseInt(touchClickDuration));
        }

        String defs = element.attributeValue("defs");
        if (StringUtils.isNotEmpty(defs)) {
            chart.setDefs(defs);
        }
    }
}