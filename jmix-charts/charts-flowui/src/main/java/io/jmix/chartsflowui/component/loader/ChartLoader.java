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

package io.jmix.chartsflowui.component.loader;

import com.google.common.base.Strings;
import io.jmix.chartsflowui.component.Chart;
import io.jmix.chartsflowui.data.ContainerChartItems;
import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.axis.*;
import io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom;
import io.jmix.chartsflowui.kit.component.model.datazoom.InsideDataZoom;
import io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom;
import io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend;
import io.jmix.chartsflowui.kit.component.model.legend.Legend;
import io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend;
import io.jmix.chartsflowui.kit.component.model.series.*;
import io.jmix.chartsflowui.kit.component.model.series.Label.Position.PositionType;
import io.jmix.chartsflowui.kit.component.model.series.mark.*;
import io.jmix.chartsflowui.kit.component.model.shared.Label;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.chartsflowui.kit.component.model.toolbox.*;
import io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap;
import io.jmix.chartsflowui.kit.component.model.visualMap.ContinuousVisualMap;
import io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChartLoader extends AbstractComponentLoader<Chart> {

    protected static final String NATIVE_JSON_ELEMENT_NAME = "nativeJson";

    protected ChartLoaderSupport chartLoaderSupport;

    @Override
    protected Chart createComponent() {
        return factory.create(Chart.class);
    }

    @Override
    public void loadComponent() {
        loadBoolean(element, "animation", resultComponent::setAnimation);
        loadInteger(element, "animationThreshold", resultComponent::setAnimationThreshold);
        loadInteger(element, "animationDuration", resultComponent::setAnimationDuration);
        loadString(element, "animationEasing", resultComponent::setAnimationEasing);
        loadInteger(element, "animationDelay", resultComponent::setAnimationDelay);
        loadInteger(element, "animationDurationUpdate", resultComponent::setAnimationDurationUpdate);
        loadString(element, "animationEasingUpdate", resultComponent::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", resultComponent::setAnimationDelayUpdate);
        loadEnum(element, ChartOptions.BlendMode.class, "blendMode", resultComponent::setBlendMode);
        loadInteger(element, "hoverLayerThreshold", resultComponent::setHoverLayerThreshold);
        loadBoolean(element, "useUtc", resultComponent::setUseUtc);

        chartLoaderSupport().loadColor(element, "backgroundColor", resultComponent::setBackgroundColor);
        loadTextStyle(element, "textStyle", resultComponent::setTextStyle);

        loadStateAnimation(resultComponent, element);
        loadTitle(resultComponent, element);
        loadLegend(resultComponent, element);
        loadGrid(resultComponent, element);
        loadAxes(resultComponent, element);
        loadPolar(resultComponent, element);
        loadRadiusAxis(resultComponent, element);
        loadAngleAxis(resultComponent, element);
        loadRadar(resultComponent, element);
        loadDataZoom(resultComponent, element);
        loadVisualMap(resultComponent, element);
        loadTooltip(element, resultComponent::setTooltip);
        loadAxisPointer(resultComponent, element);
        loadToolbox(resultComponent, element);
        loadBrush(resultComponent, element);
        loadAria(resultComponent, element);
        loadSeries(resultComponent, element);

        loadDataSet(resultComponent, element);
        loadColorPalette(resultComponent, element);
        loadNativeJson(resultComponent, element);

        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
    }

    protected void loadSeries(Chart chart, Element element) {
        Element seriesElements = element.element("series");

        if (seriesElements == null) {
            return;
        }

        List<Element> elements = seriesElements.elements();

        for (Element seriesElement : elements) {
            AbstractSeries<?> series = switch (seriesElement.getName()) {
                case "line" -> {
                    LineSeries lineSeries = new LineSeries();
                    loadLineSeries(lineSeries, seriesElement);
                    yield lineSeries;
                }
                case "bar" -> {
                    BarSeries barseries = new BarSeries();
                    loadBarSeries(barseries, seriesElement);
                    yield barseries;
                }
                case "pie" -> {
                    PieSeries pieseries = new PieSeries();
                    loadPieSeries(pieseries, seriesElement);
                    yield pieseries;
                }
                case "scatter" -> {
                    ScatterSeries scatterseries = new ScatterSeries();
                    loadScatterSeries(scatterseries, seriesElement);
                    yield scatterseries;
                }
                case "effectScatter" -> {
                    EffectScatterSeries effectscatterseries = new EffectScatterSeries();
                    loadEffectScatterSeries(effectscatterseries, seriesElement);
                    yield effectscatterseries;
                }
                case "radar" -> {
                    RadarSeries radarseries = new RadarSeries();
                    loadRadarSeries(radarseries, seriesElement);
                    yield radarseries;
                }
                case "boxplot" -> {
                    BoxplotSeries boxplotseries = new BoxplotSeries();
                    loadBoxplotSeries(boxplotseries, seriesElement);
                    yield boxplotseries;
                }
                case "candlestick" -> {
                    CandlestickSeries candlestickseries = new CandlestickSeries();
                    loadCandlestickSeries(candlestickseries, seriesElement);
                    yield candlestickseries;
                }
                case "funnel" -> {
                    FunnelSeries funnelseries = new FunnelSeries();
                    loadFunnelSeries(funnelseries, seriesElement);
                    yield funnelseries;
                }
                case "gauge" -> {
                    GaugeSeries gaugeseries = new GaugeSeries();
                    loadGaugeSeries(gaugeseries, seriesElement);
                    yield gaugeseries;
                }
                default -> throw new GuiDevelopmentException(
                        String.format("Unexpected series type '%s'", seriesElement.getName())
                        , context);
            };

            chart.addSeries(series);
        }
    }

    protected void loadLineSeries(LineSeries lineSeries, Element element) {
        loadAbstractAxisAwareSeries(lineSeries, element);

        loadEnum(element, CoordinateSystem.class, "coordinateSystem", lineSeries::setCoordinateSystem);
        loadInteger(element, "xAxisIndex", lineSeries::setXAxisIndex);
        loadInteger(element, "yAxisIndex", lineSeries::setYAxisIndex);
        loadInteger(element, "polarIndex", lineSeries::setPolarIndex);
        loadBoolean(element, "clip", lineSeries::setClip);
        loadEnum(element, SamplingType.class, "sampling", lineSeries::setSampling);
        loadBoolean(element, "showSymbol", lineSeries::setShowSymbol);
        loadBoolean(element, "showAllSymbol", lineSeries::setShowAllSymbol);
        loadLineStyle(element, "lineStyle", lineSeries::setLineStyle);
        loadEnum(element, LineSeries.Step.class, "step", lineSeries::setStep);
        loadBoolean(element, "connectNulls", lineSeries::setConnectNulls);
        loadBoolean(element, "triggerLineEvent", lineSeries::setTriggerLineEvent);
        loadString(element, "cursor", lineSeries::setCursor);
        loadDouble(element, "smooth", lineSeries::setSmooth);
        loadEnum(element, LineSeries.SmoothMonotoneType.class, "smoothMonotone", lineSeries::setSmoothMonotone);
        loadBoolean(element, "animation", lineSeries::setAnimation);
        loadInteger(element, "animationThreshold", lineSeries::setAnimationThreshold);
        loadInteger(element, "animationDurationUpdate", lineSeries::setAnimationDurationUpdate);
        loadString(element, "animationEasingUpdate", lineSeries::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", lineSeries::setAnimationDelayUpdate);

        Element endLabelElement = element.element("endLabel");
        if (endLabelElement != null) {
            LineSeries.EndLabel endLabel = new LineSeries.EndLabel();

            loadAbstractEnhancedLabel(endLabel, endLabelElement);
            loadString(endLabelElement, "formatter", endLabel::setFormatter);
            chartLoaderSupport().loadJsFunction(endLabelElement, "formatterFunction", endLabel::setFormatterFunction);
            loadBoolean(endLabelElement, "valueAnimation", endLabel::setValueAnimation);

            lineSeries.setEndLabel(endLabel);
        }

        Element itemStyleElement = element.element("itemStyle");
        if (itemStyleElement != null) {
            LineSeries.ItemStyle itemStyle = new LineSeries.ItemStyle();

            loadAbstractBorderedTextStyle(itemStyle, itemStyleElement);

            Element decalElement = itemStyleElement.element("decal");
            if (decalElement != null) {
                loadDecal(decalElement, itemStyle::setDecal);
            }

            lineSeries.setItemStyle(itemStyle);
        }

        Element labelLineElement = element.element("labelLine");
        if (labelLineElement != null) {
            LineSeries.LabelLine labelLine = new LineSeries.LabelLine();

            loadBoolean(labelLineElement, "show", labelLine::setShow);
            loadBoolean(labelLineElement, "showAbove", labelLine::setShowAbove);
            loadInteger(labelLineElement, "length", labelLine::setLength);
            loadBoolean(labelLineElement, "smooth", labelLine::setSmooth);
            loadInteger(labelLineElement, "minTurnAngle", labelLine::setMinTurnAngle);
            loadLineStyle(labelLineElement, "lineStyle", labelLine::setLineStyle);

            lineSeries.setLabelLine(labelLine);
        }

        Element areaStyleElement = element.element("areaStyle");
        if (areaStyleElement != null) {
            LineSeries.AreaStyle areaStyle = new LineSeries.AreaStyle();

            chartLoaderSupport().loadColor(areaStyleElement, "color", areaStyle::setColor);
            loadEnum(areaStyleElement, LineSeries.AreaStyle.Origin.OriginType.class, "originType", areaStyle::setOrigin);
            loadDouble(areaStyleElement, "originValue", areaStyle::setOrigin);
            loadDouble(areaStyleElement, "opacity", areaStyle::setOpacity);
            chartLoaderSupport().loadShadow(areaStyle, areaStyleElement);

            lineSeries.setAreaStyle(areaStyle);
        }

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            LineSeries.Emphasis emphasis = new LineSeries.Emphasis();

            loadAbstractLineElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);
            loadDouble(emphasisElement, "scale", emphasis::setScale);
            loadEnum(emphasisElement, FocusType.class, "focus", emphasis::setFocus);
            loadEnum(emphasisElement, BlurScopeType.class, "blurScope", emphasis::setBlurScope);

            lineSeries.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            LineSeries.Blur blur = new LineSeries.Blur();
            loadAbstractLineElement(blur, blurElement);
            lineSeries.setBlur(blur);
        }

        Element selectElement = element.element("select");
        if (selectElement != null) {
            LineSeries.Select select = new LineSeries.Select();

            loadAbstractLineElement(select, selectElement);
            loadBoolean(selectElement, "disabled", select::setDisabled);

            lineSeries.setSelect(select);
        }

        chartLoaderSupport().loadStack(lineSeries, element);
        chartLoaderSupport().loadSymbols(lineSeries, element);
    }

    protected void loadBarSeries(BarSeries barSeries, Element element) {
        loadAbstractAxisAwareSeries(barSeries, element);

        loadEnum(element, CoordinateSystem.class, "coordinateSystem", barSeries::setCoordinateSystem);
        loadInteger(element, "xAxisIndex", barSeries::setXAxisIndex);
        loadInteger(element, "yAxisIndex", barSeries::setYAxisIndex);
        loadInteger(element, "polarIndex", barSeries::setPolarIndex);
        loadEnum(element, SamplingType.class, "sampling", barSeries::setSampling);
        loadBoolean(element, "clip", barSeries::setClip);
        loadBoolean(element, "roundCap", barSeries::setRoundCap);
        loadBoolean(element, "realtimeSort", barSeries::setRealtimeSort);
        loadBoolean(element, "showBackground", barSeries::setShowBackground);
        loadString(element, "barWidth", barSeries::setBarWidth);
        loadString(element, "barMaxWidth", barSeries::setBarMaxWidth);
        loadString(element, "barMinWidth", barSeries::setBarMinWidth);
        loadInteger(element, "barMinHeight", barSeries::setBarMinHeight);
        loadInteger(element, "barMinAngle", barSeries::setBarMinAngle);
        loadString(element, "barGap", barSeries::setBarGap);
        loadString(element, "barCategoryGap", barSeries::setBarCategoryGap);
        loadString(element, "cursor", barSeries::setCursor);
        loadBoolean(element, "large", barSeries::setLarge);
        loadInteger(element, "largeThreshold", barSeries::setLargeThreshold);
        loadInteger(element, "progressive", barSeries::setProgressive);
        loadInteger(element, "progressiveThreshold", barSeries::setProgressiveThreshold);
        loadEnum(element, ProgressiveChunkMode.class, "progressiveChunkMode", barSeries::setProgressiveChunkMode);
        loadBoolean(element, "animation", barSeries::setAnimation);
        loadInteger(element, "animationThreshold", barSeries::setAnimationThreshold);
        loadInteger(element, "animationDurationUpdate", barSeries::setAnimationDurationUpdate);
        loadString(element, "animationEasingUpdate", barSeries::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", barSeries::setAnimationDelayUpdate);

        Element backgroundStyleElement = element.element("backgroundStyle");
        if (backgroundStyleElement != null) {
            BarSeries.BackgroundStyle backgroundStyle = new BarSeries.BackgroundStyle();

            chartLoaderSupport().loadColor(backgroundStyleElement, "color", backgroundStyle::setColor);
            loadString(backgroundStyleElement, "borderType", backgroundStyle::setBorderType);
            loadDouble(backgroundStyleElement, "opacity", backgroundStyle::setOpacity);

            chartLoaderSupport().loadShadow(backgroundStyle, backgroundStyleElement);
            chartLoaderSupport().loadBorder(backgroundStyle, backgroundStyleElement);

            barSeries.setBackgroundStyle(backgroundStyle);
        }

        Element itemStyleElement = element.element("itemStyle");
        if (itemStyleElement != null) {
            loadSeriesItemStyle(itemStyleElement, barSeries::setItemStyle);
        }

        loadBarLabelLine(element, barSeries::setLabelLine);

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            BarSeries.Emphasis emphasis = new BarSeries.Emphasis();

            loadAbstractBarElement(emphasis, emphasisElement);

            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);
            loadEnum(emphasisElement, FocusType.class, "focus", emphasis::setFocus);
            loadEnum(emphasisElement, BlurScopeType.class, "blurScope", emphasis::setBlurScope);

            barSeries.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            BarSeries.Blur blur = new BarSeries.Blur();
            loadAbstractBarElement(blur, blurElement);
            barSeries.setBlur(blur);
        }

        Element selectElement = element.element("select");
        if (selectElement != null) {
            BarSeries.Select select = new BarSeries.Select();
            loadAbstractBarElement(select, selectElement);
            loadBoolean(selectElement, "disabled", select::setDisabled);
            barSeries.setSelect(select);
        }

        chartLoaderSupport().loadStack(barSeries, element);
    }

    protected void loadPieSeries(PieSeries pieSeries, Element element) {
        loadAbstractAxisAwareSeries(pieSeries, element);

        loadInteger(element, "geoIndex", pieSeries::setGeoIndex);
        loadInteger(element, "calendarIndex", pieSeries::setCalendarIndex);
        loadInteger(element, "selectedOffset", pieSeries::setSelectedOffset);
        loadBoolean(element, "clockwise", pieSeries::setClockwise);
        loadInteger(element, "startAngle", pieSeries::setStartAngle);
        loadInteger(element, "minAngle", pieSeries::setMinAngle);
        loadInteger(element, "minShowLabelAngle", pieSeries::setMinShowLabelAngle);
        loadEnum(element, PieSeries.RoseType.class, "roseType", pieSeries::setRoseType);
        loadBoolean(element, "avoidLabelOverlap", pieSeries::setAvoidLabelOverlap);
        loadBoolean(element, "stillShowZeroSum", pieSeries::setStillShowZeroSum);
        loadInteger(element, "percentPrecision", pieSeries::setPercentPrecision);
        loadString(element, "width", pieSeries::setWidth);
        loadString(element, "height", pieSeries::setHeight);
        loadString(element, "cursor", pieSeries::setCursor);
        loadBoolean(element, "showEmptyCircle", pieSeries::setShowEmptyCircle);
        loadItemStyle(element, "emptyCircleStyle", pieSeries::setEmptyCircleStyle);
        loadItemStyle(element, "itemStyle", pieSeries::setItemStyle);
        chartLoaderSupport().loadStringPair(element, "center", pieSeries::setCenter,
                String.format("center of the %s must have x and y coordinates", PieSeries.class.getSimpleName()));
        chartLoaderSupport().loadStringPair(element, "radius", pieSeries::setRadius,
                String.format("radius of the %s must have inner and outer values", PieSeries.class.getSimpleName()));
        loadEnum(element, PieSeries.AnimationType.class, "animationType", pieSeries::setAnimationType);
        loadEnum(element, PieSeries.AnimationUpdateType.class, "animationTypeUpdate", pieSeries::setAnimationTypeUpdate);
        loadBoolean(element, "animation", pieSeries::setAnimation);
        loadInteger(element, "animationThreshold", pieSeries::setAnimationThreshold);
        loadInteger(element, "animationDurationUpdate", pieSeries::setAnimationDurationUpdate);
        loadString(element, "animationEasingUpdate", pieSeries::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", pieSeries::setAnimationDelayUpdate);

        Element labelLineElement = element.element("labelLine");
        if (labelLineElement != null) {
            PieSeries.LabelLine labelLine = new PieSeries.LabelLine();

            loadBoolean(labelLineElement, "show", labelLine::setShow);
            loadBoolean(labelLineElement, "showAbove", labelLine::setShowAbove);
            loadInteger(labelLineElement, "length", labelLine::setLength);
            loadInteger(labelLineElement, "length2", labelLine::setLength2);
            loadBoolean(labelLineElement, "smooth", labelLine::setSmooth);
            loadInteger(labelLineElement, "minTurnAngle", labelLine::setMinTurnAngle);
            loadLineStyle(labelLineElement, "lineStyle", labelLine::setLineStyle);
            loadInteger(labelLineElement, "maxSurfaceAngle", labelLine::setMaxSurfaceAngle);

            pieSeries.setLabelLine(labelLine);
        }

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            PieSeries.Emphasis emphasis = new PieSeries.Emphasis();

            loadAbstractPieElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);
            loadBoolean(emphasisElement, "scale", emphasis::setScale);
            loadInteger(emphasisElement, "scaleSize", emphasis::setScaleSize);
            loadEnum(emphasisElement, FocusType.class, "focus", emphasis::setFocus);
            loadEnum(emphasisElement, BlurScopeType.class, "blurScope", emphasis::setBlurScope);

            pieSeries.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            PieSeries.Blur blur = new PieSeries.Blur();
            loadAbstractPieElement(blur, blurElement);
            pieSeries.setBlur(blur);
        }

        Element selectedElement = element.element("selected");
        if (selectedElement != null) {
            PieSeries.Selected selected = new PieSeries.Selected();
            loadAbstractPieElement(selected, selectedElement);
            loadBoolean(selectedElement, "disabled", selected::setDisabled);
            pieSeries.setSelected(selected);
        }

        chartLoaderSupport().loadPosition(pieSeries, element);
    }

    protected void loadScatterSeries(ScatterSeries scatterSeries, Element element) {
        loadAbstractAxisAwareSeries(scatterSeries, element);

        loadEnum(element, CoordinateSystem.class, "coordinateSystem", scatterSeries::setCoordinateSystem);
        loadInteger(element, "xAxisIndex", scatterSeries::setXAxisIndex);
        loadInteger(element, "yAxisIndex", scatterSeries::setYAxisIndex);
        loadInteger(element, "polarIndex", scatterSeries::setPolarIndex);
        loadInteger(element, "geoIndex", scatterSeries::setGeoIndex);
        loadInteger(element, "calendarIndex", scatterSeries::setCalendarIndex);
        loadBoolean(element, "clip", scatterSeries::setClip);
        loadString(element, "cursor", scatterSeries::setCursor);
        loadBoolean(element, "large", scatterSeries::setLarge);
        loadInteger(element, "largeThreshold", scatterSeries::setLargeThreshold);
        loadItemStyle(element, "itemStyle", scatterSeries::setItemStyle);
        loadInteger(element, "progressive", scatterSeries::setProgressive);
        loadInteger(element, "progressiveThreshold", scatterSeries::setProgressiveThreshold);
        loadBoolean(element, "animation", scatterSeries::setAnimation);
        loadInteger(element, "animationThreshold", scatterSeries::setAnimationThreshold);
        loadInteger(element, "animationDurationUpdate", scatterSeries::setAnimationDurationUpdate);
        loadString(element, "animationEasingUpdate", scatterSeries::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", scatterSeries::setAnimationDelayUpdate);

        Element labelLineElement = element.element("labelLine");
        if (labelLineElement != null) {
            ScatterSeries.LabelLine labelLine = new ScatterSeries.LabelLine();

            loadBoolean(labelLineElement, "show", labelLine::setShow);
            loadBoolean(labelLineElement, "showAbove", labelLine::setShowAbove);
            loadInteger(labelLineElement, "length", labelLine::setLength);
            loadBoolean(labelLineElement, "smooth", labelLine::setSmooth);
            loadInteger(labelLineElement, "minTurnAngle", labelLine::setMinTurnAngle);
            loadLineStyle(labelLineElement, "lineStyle", labelLine::setLineStyle);

            scatterSeries.setLabelLine(labelLine);
        }

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            ScatterSeries.Emphasis emphasis = new ScatterSeries.Emphasis();

            loadAbstractScatterElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);
            loadDouble(emphasisElement, "scale", emphasis::setScale);
            loadEnum(emphasisElement, FocusType.class, "focus", emphasis::setFocus);
            loadEnum(emphasisElement, BlurScopeType.class, "blurScope", emphasis::setBlurScope);

            scatterSeries.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            ScatterSeries.Blur blur = new ScatterSeries.Blur();
            loadAbstractScatterElement(blur, blurElement);
            scatterSeries.setBlur(blur);
        }

        Element selectElement = element.element("select");
        if (selectElement != null) {
            ScatterSeries.Select select = new ScatterSeries.Select();
            loadAbstractScatterElement(select, selectElement);
            loadBoolean(selectElement, "disabled", select::setDisabled);
            scatterSeries.setSelect(select);
        }

        chartLoaderSupport().loadSymbols(scatterSeries, element);
    }

    protected void loadEffectScatterSeries(EffectScatterSeries effectScatterSeries, Element element) {
        loadAbstractAxisAwareSeries(effectScatterSeries, element);

        loadEnum(element, CoordinateSystem.class, "coordinateSystem", effectScatterSeries::setCoordinateSystem);
        loadString(element, "effectType", effectScatterSeries::setEffectType);
        loadEnum(element, EffectScatterSeries.EffectOn.class, "showEffectOn", effectScatterSeries::setShowEffectOn);
        loadInteger(element, "xAxisIndex", effectScatterSeries::setXAxisIndex);
        loadInteger(element, "yAxisIndex", effectScatterSeries::setYAxisIndex);
        loadInteger(element, "polarIndex", effectScatterSeries::setPolarIndex);
        loadInteger(element, "geoIndex", effectScatterSeries::setGeoIndex);
        loadInteger(element, "calendarIndex", effectScatterSeries::setCalendarIndex);
        loadBoolean(element, "clip", effectScatterSeries::setClip);
        loadString(element, "cursor", effectScatterSeries::setCursor);
        loadItemStyle(element, "itemStyle", effectScatterSeries::setItemStyle);
        loadBoolean(element, "animation", effectScatterSeries::setAnimation);
        loadInteger(element, "animationThreshold", effectScatterSeries::setAnimationThreshold);
        loadInteger(element, "animationDurationUpdate", effectScatterSeries::setAnimationDurationUpdate);
        loadString(element, "animationEasingUpdate", effectScatterSeries::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", effectScatterSeries::setAnimationDelayUpdate);

        Element rippleEffectElement = element.element("rippleEffect");
        if (rippleEffectElement != null) {
            EffectScatterSeries.RippleEffect rippleEffect = new EffectScatterSeries.RippleEffect();

            chartLoaderSupport().loadColor(rippleEffectElement, "color", rippleEffect::setColor);
            loadInteger(rippleEffectElement, "number", rippleEffect::setNumber);
            loadInteger(rippleEffectElement, "period", rippleEffect::setPeriod);
            loadDouble(rippleEffectElement, "scale", rippleEffect::setScale);
            loadEnum(rippleEffectElement, EffectScatterSeries.RippleEffect.BrushType.class, "brushType", rippleEffect::setBrushType);

            effectScatterSeries.setRippleEffect(rippleEffect);
        }

        Element labelLineElement = element.element("labelLine");
        if (labelLineElement != null) {
            EffectScatterSeries.LabelLine labelLine = new EffectScatterSeries.LabelLine();

            loadBoolean(labelLineElement, "show", labelLine::setShow);
            loadBoolean(labelLineElement, "showAbove", labelLine::setShowAbove);
            loadInteger(labelLineElement, "length", labelLine::setLength);
            loadBoolean(labelLineElement, "smooth", labelLine::setSmooth);
            loadInteger(labelLineElement, "minTurnAngle", labelLine::setMinTurnAngle);
            loadLineStyle(labelLineElement, "lineStyle", labelLine::setLineStyle);

            effectScatterSeries.setLabelLine(labelLine);
        }

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            EffectScatterSeries.Emphasis emphasis = new EffectScatterSeries.Emphasis();

            loadAbstractEffectScatterElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);
            loadDouble(emphasisElement, "scale", emphasis::setScale);
            loadEnum(emphasisElement, FocusType.class, "focus", emphasis::setFocus);
            loadEnum(emphasisElement, BlurScopeType.class, "blurScope", emphasis::setBlurScope);

            effectScatterSeries.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            EffectScatterSeries.Blur blur = new EffectScatterSeries.Blur();
            loadAbstractEffectScatterElement(blur, blurElement);
            effectScatterSeries.setBlur(blur);
        }

        Element selectElement = element.element("select");
        if (selectElement != null) {
            EffectScatterSeries.Select select = new EffectScatterSeries.Select();
            loadAbstractEffectScatterElement(select, selectElement);
            loadBoolean(selectElement, "disabled", select::setDisabled);
            effectScatterSeries.setSelect(select);
        }

        chartLoaderSupport().loadSymbols(effectScatterSeries, element);
    }

    protected void loadRadarSeries(RadarSeries radarSeries, Element element) {
        loadAbstractSeries(radarSeries, element);

        loadInteger(element, "radarIndex", radarSeries::setRadarIndex);
        loadLineStyle(element, "lineStyle", radarSeries::setLineStyle);
        loadBoolean(element, "animation", radarSeries::setAnimation);
        loadInteger(element, "animationThreshold", radarSeries::setAnimationThreshold);
        loadInteger(element, "animationDurationUpdate", radarSeries::setAnimationDurationUpdate);
        loadString(element, "animationEasingUpdate", radarSeries::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", radarSeries::setAnimationDelayUpdate);

        Element itemStyleElement = element.element("itemStyle");
        if (itemStyleElement != null) {
            loadSeriesItemStyle(itemStyleElement, radarSeries::setItemStyle);
        }

        Element areaStyleElement = element.element("areaStyle");
        if (areaStyleElement != null) {
            loadRadarAreaStyle(areaStyleElement, radarSeries::setAreaStyle);
        }

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            RadarSeries.Emphasis emphasis = new RadarSeries.Emphasis();

            loadAbstractRadarElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);
            loadEnum(emphasisElement, FocusType.class, "focus", emphasis::setFocus);
            loadEnum(emphasisElement, BlurScopeType.class, "blurScope", emphasis::setBlurScope);

            radarSeries.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            RadarSeries.Blur blur = new RadarSeries.Blur();
            loadAbstractRadarElement(blur, blurElement);
            radarSeries.setBlur(blur);
        }

        Element selectElement = element.element("select");
        if (selectElement != null) {
            RadarSeries.Select select = new RadarSeries.Select();
            loadAbstractRadarElement(select, selectElement);
            loadBoolean(selectElement, "disabled", select::setDisabled);
            radarSeries.setSelect(select);
        }

        chartLoaderSupport().loadSymbols(radarSeries, element);
    }

    protected void loadBoxplotSeries(BoxplotSeries boxplotSeries, Element element) {
        loadAbstractAxisAwareSeries(boxplotSeries, element);

        loadEnum(element, CoordinateSystem.class, "coordinateSystem", boxplotSeries::setCoordinateSystem);
        loadInteger(element, "xAxisIndex", boxplotSeries::setXAxisIndex);
        loadInteger(element, "yAxisIndex", boxplotSeries::setYAxisIndex);
        loadBoolean(element, "hoverAnimation", boxplotSeries::setHoverAnimation);
        loadEnum(element, Orientation.class, "layout", boxplotSeries::setLayout);
        chartLoaderSupport().loadStringPair(element, "boxWidth", boxplotSeries::setBoxWidth,
                String.format("boxWidth of %s must have min and max values", BoxplotSeries.class.getSimpleName()));
        loadItemStyle(element, "itemStyle", boxplotSeries::setItemStyle);

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            BoxplotSeries.Emphasis emphasis = new BoxplotSeries.Emphasis();

            loadAbstractBoxplotElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);
            loadEnum(emphasisElement, FocusType.class, "focus", emphasis::setFocus);
            loadEnum(emphasisElement, BlurScopeType.class, "blurScope", emphasis::setBlurScope);

            boxplotSeries.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            BoxplotSeries.Blur blur = new BoxplotSeries.Blur();
            loadAbstractBoxplotElement(blur, blurElement);
            boxplotSeries.setBlur(blur);
        }

        Element selectElement = element.element("select");
        if (selectElement != null) {
            BoxplotSeries.Select select = new BoxplotSeries.Select();
            loadAbstractBoxplotElement(select, selectElement);
            loadBoolean(selectElement, "disabled", select::setDisabled);
            boxplotSeries.setSelect(select);
        }
    }

    protected void loadCandlestickSeries(CandlestickSeries candlestickSeries, Element element) {
        loadAbstractAxisAwareSeries(candlestickSeries, element);

        loadEnum(element, CoordinateSystem.class, "coordinateSystem", candlestickSeries::setCoordinateSystem);
        loadInteger(element, "xAxisIndex", candlestickSeries::setXAxisIndex);
        loadInteger(element, "yAxisIndex", candlestickSeries::setYAxisIndex);
        loadBoolean(element, "hoverAnimation", candlestickSeries::setHoverAnimation);
        loadEnum(element, Orientation.class, "layout", candlestickSeries::setLayout);
        loadString(element, "barWidth", candlestickSeries::setBarWidth);
        loadString(element, "barMaxWidth", candlestickSeries::setBarMaxWidth);
        loadString(element, "barMinWidth", candlestickSeries::setBarMinWidth);
        loadBoolean(element, "large", candlestickSeries::setLarge);
        loadInteger(element, "largeThreshold", candlestickSeries::setLargeThreshold);
        loadInteger(element, "progressive", candlestickSeries::setProgressive);
        loadInteger(element, "progressiveThreshold", candlestickSeries::setProgressiveThreshold);
        loadEnum(element, ProgressiveChunkMode.class, "progressiveChunkMode", candlestickSeries::setProgressiveChunkMode);
        loadBoolean(element, "clip", candlestickSeries::setClip);
        loadCandlestickSeriesItemStyle(element, candlestickSeries::setItemStyle);

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            CandlestickSeries.Emphasis emphasis = new CandlestickSeries.Emphasis();

            loadCandlestickSeriesItemStyle(emphasisElement, emphasis::setItemStyle);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);
            loadEnum(emphasisElement, FocusType.class, "focus", emphasis::setFocus);
            loadEnum(emphasisElement, BlurScopeType.class, "blurScope", emphasis::setBlurScope);

            candlestickSeries.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            CandlestickSeries.Blur blur = new CandlestickSeries.Blur();
            loadCandlestickSeriesItemStyle(blurElement, blur::setItemStyle);
            candlestickSeries.setBlur(blur);
        }

        Element selectElement = element.element("select");
        if (selectElement != null) {
            CandlestickSeries.Select select = new CandlestickSeries.Select();
            loadCandlestickSeriesItemStyle(selectElement, select::setItemStyle);
            loadBoolean(selectElement, "disabled", select::setDisabled);
            candlestickSeries.setSelect(select);
        }
    }

    protected void loadFunnelSeries(FunnelSeries funnelSeries, Element element) {
        loadAbstractAxisAwareSeries(funnelSeries, element);

        loadInteger(element, "max", funnelSeries::setMax);
        loadInteger(element, "min", funnelSeries::setMin);
        loadString(element, "minSize", funnelSeries::setMinSize);
        loadString(element, "maxSize", funnelSeries::setMaxSize);
        loadEnum(element, Orientation.class, "orientation", funnelSeries::setOrientation);
        loadEnum(element, FunnelSeries.SortType.class, "sort", funnelSeries::setSort);
        chartLoaderSupport().loadJsFunction(element, "sortFunction", funnelSeries::setSortFunction);
        loadInteger(element, "gap", funnelSeries::setGap);
        loadEnum(element, Align.class, "funnelAlign", funnelSeries::setFunnelAlign);
        loadItemStyle(element, "itemStyle", funnelSeries::setItemStyle);
        loadString(element, "width", funnelSeries::setWidth);
        loadString(element, "height", funnelSeries::setHeight);
        loadBoolean(element, "animation", funnelSeries::setAnimation);
        loadInteger(element, "animationThreshold", funnelSeries::setAnimationThreshold);
        loadInteger(element, "animationDurationUpdate", funnelSeries::setAnimationDurationUpdate);
        loadString(element, "animationEasingUpdate", funnelSeries::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", funnelSeries::setAnimationDelayUpdate);

        Element labelLineElement = element.element("labelLine");
        if (labelLineElement != null) {
            FunnelSeries.LabelLine labelLine = new FunnelSeries.LabelLine();

            loadBoolean(labelLineElement, "show", labelLine::setShow);
            loadInteger(labelLineElement, "length", labelLine::setLength);
            loadLineStyle(labelLineElement, "lineStyle", labelLine::setLineStyle);

            funnelSeries.setLabelLine(labelLine);
        }

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            FunnelSeries.Emphasis emphasis = new FunnelSeries.Emphasis();

            loadAbstractFunnelElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);
            loadEnum(emphasisElement, FocusType.class, "focus", emphasis::setFocus);
            loadEnum(emphasisElement, BlurScopeType.class, "blurScope", emphasis::setBlurScope);

            funnelSeries.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            FunnelSeries.Blur blur = new FunnelSeries.Blur();
            loadAbstractFunnelElement(blur, blurElement);
            funnelSeries.setBlur(blur);
        }

        Element selectElement = element.element("select");
        if (selectElement != null) {
            FunnelSeries.Select select = new FunnelSeries.Select();
            loadAbstractFunnelElement(select, selectElement);
            loadBoolean(selectElement, "disabled", select::setDisabled);
            funnelSeries.setSelect(select);
        }

        chartLoaderSupport().loadPosition(funnelSeries, element);
    }

    protected void loadGaugeSeries(GaugeSeries gaugeSeries, Element element) {
        loadAbstractSeries(gaugeSeries, element);

        chartLoaderSupport().loadStringPair(element, "center", gaugeSeries::setCenter,
                String.format("center of the %s must have two coordinates", GaugeSeries.class.getSimpleName()));
        loadString(element, "radius", gaugeSeries::setRadius);
        loadBoolean(element, "legendHoverLink", gaugeSeries::setLegendHoverLink);
        loadInteger(element, "startAngle", gaugeSeries::setStartAngle);
        loadInteger(element, "endAngle", gaugeSeries::setEndAngle);
        loadBoolean(element, "clockwise", gaugeSeries::setClockwise);
        loadInteger(element, "min", gaugeSeries::setMin);
        loadInteger(element, "max", gaugeSeries::setMax);
        loadInteger(element, "splitNumber", gaugeSeries::setSplitNumber);
        loadItemStyle(element, "itemStyle", gaugeSeries::setItemStyle);
        loadMarkPoint(element, gaugeSeries::setMarkPoint);
        loadMarkLine(element, gaugeSeries::setMarkLine);
        loadMarkArea(element, gaugeSeries::setMarkArea);
        loadBoolean(element, "animation", gaugeSeries::setAnimation);
        loadInteger(element, "animationThreshold", gaugeSeries::setAnimationThreshold);
        loadInteger(element, "animationDurationUpdate", gaugeSeries::setAnimationDurationUpdate);
        loadString(element, "animationEasingUpdate", gaugeSeries::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", gaugeSeries::setAnimationDelayUpdate);

        Element axisLineElement = element.element("axisLine");
        if (axisLineElement != null) {
            GaugeSeries.AxisLine axisLine = new GaugeSeries.AxisLine();

            loadBoolean(axisLineElement, "show", axisLine::setShow);
            loadBoolean(axisLineElement, "roundCap", axisLine::setRoundCap);

            Element lineStyleElement = axisLineElement.element("lineStyle");
            if (lineStyleElement != null) {
                GaugeSeries.AxisLine.LineStyle lineStyle = new GaugeSeries.AxisLine.LineStyle();

                loadInteger(lineStyleElement, "width", lineStyle::setWidth);
                loadDouble(lineStyleElement, "opacity", lineStyle::setOpacity);
                chartLoaderSupport().loadShadow(lineStyle, lineStyleElement);

                Element colorPaletteElement = lineStyleElement.element("colorPalette");
                if (colorPaletteElement != null) {
                    colorPaletteElement.elements("colorItem")
                            .forEach(colorItem ->
                                    loadGaugeSeriesAxisLineStyleColorItem(colorItem, lineStyle::addColorToPalette));
                }

                axisLine.setLineStyle(lineStyle);
            }

            gaugeSeries.setAxisLine(axisLine);
        }

        Element progressElement = element.element("progress");
        if (progressElement != null) {
            GaugeSeries.Progress progress = new GaugeSeries.Progress();

            loadBoolean(progressElement, "show", progress::setShow);
            loadBoolean(progressElement, "overlap", progress::setOverlap);
            loadInteger(progressElement, "width", progress::setWidth);
            loadBoolean(progressElement, "roundCap", progress::setRoundCap);
            loadBoolean(progressElement, "clip", progress::setClip);
            loadItemStyle(progressElement, "itemStyle", progress::setItemStyle);

            gaugeSeries.setProgress(progress);
        }

        Element splitLineElement = element.element("splitLine");
        if (splitLineElement != null) {
            GaugeSeries.SplitLine splitLine = new GaugeSeries.SplitLine();

            loadBoolean(splitLineElement, "show", splitLine::setShow);
            loadInteger(splitLineElement, "length", splitLine::setLength);
            loadInteger(splitLineElement, "distance", splitLine::setDistance);
            loadLineStyle(splitLineElement, "lineStyle", splitLine::setLineStyle);

            gaugeSeries.setSplitLine(splitLine);
        }

        Element axisTickElement = element.element("axisTick");
        if (axisTickElement != null) {
            GaugeSeries.AxisTick axisTick = new GaugeSeries.AxisTick();

            loadBoolean(axisTickElement, "show", axisTick::setShow);
            loadInteger(axisTickElement, "splitNumber", axisTick::setSplitNumber);
            loadInteger(axisTickElement, "length", axisTick::setLength);
            loadInteger(axisTickElement, "distance", axisTick::setDistance);
            loadLineStyle(axisTickElement, "lineStyle", axisTick::setLineStyle);

            gaugeSeries.setAxisTick(axisTick);
        }

        loadSeriesLabel(element, "axisLabel", gaugeSeries::setAxisLabel);

        Element pointerElement = element.element("pointer");
        if (pointerElement != null) {
            GaugeSeries.Pointer pointer = new GaugeSeries.Pointer();

            loadBoolean(pointerElement, "show", pointer::setShow);
            loadBoolean(pointerElement, "showAbove", pointer::setShowAbove);
            loadString(pointerElement, "icon", pointer::setIcon);
            chartLoaderSupport().loadStringPair(pointerElement, "offsetCenter", pointer::setOffsetCenter,
                    String.format("offsetCenter of the %s must have two coordinates",
                            GaugeSeries.Pointer.class.getSimpleName())
            );
            loadString(pointerElement, "length", pointer::setLength);
            loadInteger(pointerElement, "width", pointer::setWidth);
            loadBoolean(pointerElement, "keepAspect", pointer::setKeepAspect);
            loadItemStyle(pointerElement, "itemStyle", pointer::setItemStyle);

            gaugeSeries.setPointer(pointer);
        }

        Element anchorElement = element.element("anchor");
        if (anchorElement != null) {
            GaugeSeries.Anchor anchor = new GaugeSeries.Anchor();

            loadBoolean(anchorElement, "show", anchor::setShow);
            loadBoolean(anchorElement, "showAbove", anchor::setShowAbove);
            loadInteger(anchorElement, "size", anchor::setSize);
            loadString(anchorElement, "icon", anchor::setIcon);
            chartLoaderSupport().loadStringPair(anchorElement, "offsetCenter", anchor::setOffsetCenter,
                    String.format("offsetCenter of the %s must have two coordinates",
                            GaugeSeries.Anchor.class.getSimpleName())
            );
            loadBoolean(anchorElement, "keepAspect", anchor::setKeepAspect);
            loadItemStyle(anchorElement, "itemStyle", anchor::setItemStyle);

            gaugeSeries.setAnchor(anchor);
        }

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            GaugeSeries.Emphasis emphasis = new GaugeSeries.Emphasis();

            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);
            loadItemStyle(emphasisElement, "itemStyle", emphasis::setItemStyle);

            gaugeSeries.setEmphasis(emphasis);
        }

        Element titleElement = element.element("title");
        if (titleElement != null) {
            GaugeSeries.Title title = new GaugeSeries.Title();
            loadAbstractGaugeText(title, titleElement);
            gaugeSeries.setTitle(title);
        }

        Element detailElement = element.element("detail");
        if (detailElement != null) {
            GaugeSeries.Detail detail = new GaugeSeries.Detail();
            loadAbstractGaugeText(detail, detailElement);
            loadString(detailElement, "formatter", detail::setFormatter);
            chartLoaderSupport().loadJsFunction(detailElement, "formatterFunction", detail::setFormatterFunction);
            gaugeSeries.setDetail(detail);
        }

        Element dataElement = element.element("data");
        if (dataElement != null) {
            dataElement.elements("dataItem")
                    .forEach(dataItem -> loadGaugeSeriesDataItem(dataItem, gaugeSeries::addData));
        }
    }

    protected void loadGaugeSeriesDataItem(Element element, Consumer<GaugeSeries.DataItem> setter) {
        GaugeSeries.DataItem dataItem = new GaugeSeries.DataItem();

        loadResourceString(element, "name", context.getMessageGroup(), dataItem::setName);
        loadDouble(element, "value", dataItem::setValue);
        loadItemStyle(element, "itemStyle", dataItem::setItemStyle);

        Element titleElement = element.element("title");
        if (titleElement != null) {
            GaugeSeries.Title title = new GaugeSeries.Title();
            loadAbstractGaugeText(title, titleElement);
            dataItem.setTitle(title);
        }

        Element detailElement = element.element("detail");
        if (detailElement != null) {
            GaugeSeries.Detail detail = new GaugeSeries.Detail();
            loadAbstractGaugeText(detail, detailElement);
            loadString(detailElement, "formatter", detail::setFormatter);
            chartLoaderSupport().loadJsFunction(detailElement, "formatterFunction", detail::setFormatterFunction);
            dataItem.setDetail(detail);
        }

        setter.accept(dataItem);
    }

    protected void loadGaugeSeriesAxisLineStyleColorItem(Element element, BiConsumer<Double, Color> setter) {
        Double range = loadDouble(element, "range")
                .orElseThrow(() -> new GuiDevelopmentException("range is required for colorItem", context));
        Color color = chartLoaderSupport().loadColor(element, "color")
                .orElseThrow(() -> new GuiDevelopmentException("color is required for colorItem", context));

        setter.accept(range, color);
    }

    protected void loadAbstractGaugeText(GaugeSeries.AbstractGaugeText<?> gaugeText, Element element) {
        loadAbstractRichText(gaugeText, element);

        loadBoolean(element, "show", gaugeText::setShow);
        chartLoaderSupport().loadStringPair(element, "offsetCenter", gaugeText::setOffsetCenter,
                String.format("offsetCenter of the %s must have two coordinates",
                        gaugeText.getClass().getSimpleName()));
        loadBoolean(element, "valueAnimation", gaugeText::setValueAnimation);
        chartLoaderSupport().loadColor(element, "backgroundColor", gaugeText::setBackgroundColor);
        loadString(element, "borderType", gaugeText::setBorderType);
        loadInteger(element, "borderDashOffset", gaugeText::setBorderDashOffset);

        chartLoaderSupport().loadShadow(gaugeText, element);
        chartLoaderSupport().loadPadding(gaugeText, element);
        chartLoaderSupport().loadBorder(gaugeText, element);
    }

    protected void loadAbstractFunnelElement(FunnelSeries.AbstractFunnelElement<?> funnelElement, Element element) {
        loadSeriesLabel(element, "label", funnelElement::setLabel);
        loadElementLabelLine(element, funnelElement::setLabelLine);
        loadItemStyle(element, "itemStyle", funnelElement::setItemStyle);
    }

    protected void loadCandlestickSeriesItemStyle(Element element, Consumer<CandlestickSeries.ItemStyle> setter) {
        Element itemStyleElement = element.element("itemStyle");
        if (itemStyleElement == null) {
            return;
        }

        CandlestickSeries.ItemStyle itemStyle = new CandlestickSeries.ItemStyle();

        chartLoaderSupport().loadColor(itemStyleElement, "bullishColor", itemStyle::setBullishColor);
        chartLoaderSupport().loadColor(itemStyleElement, "bearishColor", itemStyle::setBearishColor);
        chartLoaderSupport().loadColor(itemStyleElement, "bullishBorderColor", itemStyle::setBullishBorderColor);
        chartLoaderSupport().loadColor(itemStyleElement, "bearishBorderColor", itemStyle::setBearishBorderColor);
        chartLoaderSupport().loadColor(itemStyleElement, "dojiBorderColor", itemStyle::setDojiBorderColor);
        loadDouble(itemStyleElement, "borderWidth", itemStyle::setBorderWidth);
        loadDouble(itemStyleElement, "opacity", itemStyle::setOpacity);

        chartLoaderSupport().loadShadow(itemStyle, itemStyleElement);

        setter.accept(itemStyle);
    }

    protected void loadAbstractBoxplotElement(BoxplotSeries.AbstractBoxplotElement<?> boxplotElement, Element element) {
        loadItemStyle(element, "itemStyle", boxplotElement::setItemStyle);
    }

    protected void loadRadarAreaStyle(Element element, Consumer<RadarSeries.AreaStyle> setter) {
        RadarSeries.AreaStyle areaStyle = new RadarSeries.AreaStyle();
        loadAbstractAreaStyle(areaStyle, element);
        chartLoaderSupport().loadColor(element, "color", areaStyle::setColor);
        setter.accept(areaStyle);
    }

    protected void loadAbstractRadarElement(RadarSeries.AbstractRadarElement<?> radarElement, Element element) {
        loadItemStyle(element, "itemStyle", radarElement::setItemStyle);
        loadSeriesLabel(element, "label", radarElement::setLabel);
        loadLineStyle(element, "lineStyle", radarElement::setLineStyle);

        Element areaStyleElement = element.element("areaStyle");
        if (areaStyleElement != null) {
            loadRadarAreaStyle(areaStyleElement, radarElement::setAreaStyle);
        }
    }

    protected void loadAbstractEffectScatterElement(EffectScatterSeries.AbstractEffectScatterElement<?> scatterElement,
                                                    Element element) {
        loadSeriesLabel(element, "label", scatterElement::setLabel);
        loadElementLabelLine(element, scatterElement::setLabelLine);
        loadItemStyle(element, "itemStyle", scatterElement::setItemStyle);
    }

    protected void loadAbstractScatterElement(ScatterSeries.AbstractScatterElement<?> scatterElement, Element element) {
        loadSeriesLabel(element, "label", scatterElement::setLabel);
        loadElementLabelLine(element, scatterElement::setLabelLine);
        loadItemStyle(element, "itemStyle", scatterElement::setItemStyle);
    }

    protected void loadAbstractPieElement(PieSeries.AbstractPieElement<?> pieElement, Element element) {
        loadSeriesLabel(element, "label", pieElement::setLabel);
        loadElementLabelLine(element, pieElement::setLabelLine);
        loadItemStyle(element, "itemStyle", pieElement::setItemStyle);
    }

    protected void loadBarLabelLine(Element element, Consumer<BarSeries.LabelLine> setter) {
        Element labelLineElement = element.element("labelLine");
        if (labelLineElement != null) {
            BarSeries.LabelLine labelLine = new BarSeries.LabelLine();

            loadBoolean(labelLineElement, "show", labelLine::setShow);
            loadLineStyle(labelLineElement, "lineStyle", labelLine::setLineStyle);

            setter.accept(labelLine);
        }
    }

    protected void loadAbstractItemStyle(AbstractItemStyle<?> itemStyle, Element element) {
        chartLoaderSupport().loadColor(element, "color", itemStyle::setColor);
        loadString(element, "borderType", itemStyle::setBorderType);
        loadDouble(element, "opacity", itemStyle::setOpacity);

        chartLoaderSupport().loadBorder(itemStyle, element);
        chartLoaderSupport().loadShadow(itemStyle, element);
    }

    protected void loadSeriesItemStyle(Element element, Consumer<ItemStyleWithDecal> setter) {
        ItemStyleWithDecal itemStyle = new ItemStyleWithDecal();

        loadAbstractItemStyle(itemStyle, element);

        Element decalElement = element.element("decal");
        if (decalElement != null) {
            loadDecal(decalElement, itemStyle::setDecal);
        }

        setter.accept(itemStyle);
    }

    protected void loadAbstractBarElement(BarSeries.AbstractBarElement<?> barElement, Element element) {
        loadSeriesLabel(element, "label", barElement::setLabel);
        loadBarLabelLine(element, barElement::setLabelLine);

        Element itemStyleElement = element.element("itemStyle");
        if (itemStyleElement != null) {
            BarSeries.ItemStyle itemStyle = new BarSeries.ItemStyle();
            loadAbstractItemStyle(itemStyle, itemStyleElement);
            barElement.setItemStyle(itemStyle);
        }
    }

    protected void loadAbstractLineElement(LineSeries.AbstractLineElement<?> lineElement, Element element) {
        loadSeriesLabel(element, "label", lineElement::setLabel);
        loadElementLabelLine(element, lineElement::setLabelLine);
        loadItemStyle(element, "itemStyle", lineElement::setItemStyle);
        loadLineStyle(element, "lineStyle", lineElement::setLineStyle);

        Element areaStyleElement = element.element("areaStyle");
        if (areaStyleElement != null) {
            LineSeries.AbstractLineElement.AreaStyle areaStyle = new LineSeries.AbstractLineElement.AreaStyle();

            loadAbstractAreaStyle(areaStyle, areaStyleElement);
            chartLoaderSupport().loadColor(areaStyleElement, "color", areaStyle::setColor);

            lineElement.setAreaStyle(areaStyle);
        }

        Element endLabelElement = element.element("endLabel");
        if (endLabelElement != null) {
            LineSeries.EndLabel endLabel = new LineSeries.EndLabel();

            loadAbstractEnhancedLabel(endLabel, endLabelElement);
            loadString(endLabelElement, "formatter", endLabel::setFormatter);
            chartLoaderSupport().loadJsFunction(endLabelElement, "formatterFunction", endLabel::setFormatterFunction);
            loadBoolean(endLabelElement, "valueAnimation", endLabel::setValueAnimation);

            lineElement.setEndLabel(endLabel);
        }
    }

    protected void loadElementLabelLine(Element element, Consumer<ElementLabelLine> setter) {
        Element labelLineElement = element.element("labelLine");

        if (labelLineElement == null) {
            return;
        }

        ElementLabelLine elementLabelLine = new ElementLabelLine();
        loadBoolean(labelLineElement, "show", elementLabelLine::setShow);
        loadLineStyle(labelLineElement, "lineStyle", elementLabelLine::setLineStyle);

        setter.accept(elementLabelLine);
    }

    protected void loadAbstractAxisAwareSeries(AbstractAxisAwareSeries<?> series, Element element) {
        loadAbstractSeries(series, element);

        loadBoolean(element, "legendHoverLink", series::setLegendHoverLink);
        loadEnum(element, AbstractAxisAwareSeries.SeriesLayoutType.class, "seriesLayoutBy", series::setSeriesLayoutBy);
        loadInteger(element, "datasetIndex", series::setDatasetIndex);

        Element encodeElement = element.element("encode");
        if (encodeElement != null) {
            Encode encode = new Encode();

            chartLoaderSupport().loadStringList(encodeElement, "x", encode::setX);
            chartLoaderSupport().loadStringList(encodeElement, "y", encode::setY);
            chartLoaderSupport().loadStringList(encodeElement, "radius", encode::setRadius);
            chartLoaderSupport().loadStringList(encodeElement, "angle", encode::setAngle);
            chartLoaderSupport().loadStringList(encodeElement, "value", encode::setValue);
            chartLoaderSupport().loadStringList(encodeElement, "tooltip", encode::setTooltip);

            series.setEncode(encode);
        }

        loadMarkPoint(element, series::setMarkPoint);
        loadMarkLine(element, series::setMarkLine);
        loadMarkArea(element, series::setMarkArea);
    }

    protected void loadAbstractSeries(AbstractSeries<?> series, Element element) {
        loadString(element, "id", series::setId);
        loadResourceString(element, "name", context.getMessageGroup(), series::setName);
        loadEnum(element, SelectedMode.class, "selectedMode", series::setSelectedMode);
        loadEnum(element, ColorBy.class, "colorBy", series::setColorBy);
        loadString(element, "dataGroupId", series::setDataGroupId);
        loadInteger(element, "zLevel", series::setZLevel);
        loadInteger(element, "z", series::setZ);
        loadBoolean(element, "silent", series::setSilent);
        loadInteger(element, "animationDuration", series::setAnimationDuration);
        loadString(element, "animationEasing", series::setAnimationEasing);
        loadInteger(element, "animationDelay", series::setAnimationDelay);

        loadSeriesLabelLayout(element, series::setLabelLayout);
        loadSeriesTooltip(element, series::setTooltip);
        loadSeriesLabel(element, "label", series::setLabel);
    }

    protected void loadSeriesLabelLayout(Element element, Consumer<AbstractSeries.LabelLayout> setter) {
        Element labelLayoutElement = element.element("labelLayout");

        if (labelLayoutElement == null) {
            return;
        }

        AbstractSeries.LabelLayout labelLayout = new AbstractSeries.LabelLayout();

        loadBoolean(labelLayoutElement, "hideOverlap", labelLayout::setHideOverlap);
        loadEnum(labelLayoutElement, AbstractSeries.LabelLayout.MoveOverlapPosition.class, "moveOverlap",
                labelLayout::setMoveOverlap);
        loadString(labelLayoutElement, "x", labelLayout::setX);
        loadString(labelLayoutElement, "y", labelLayout::setY);
        loadInteger(labelLayoutElement, "dx", labelLayout::setDx);
        loadInteger(labelLayoutElement, "dy", labelLayout::setDy);
        loadInteger(labelLayoutElement, "rotate", labelLayout::setRotate);
        loadInteger(labelLayoutElement, "width", labelLayout::setWidth);
        loadInteger(labelLayoutElement, "height", labelLayout::setHeight);
        loadInteger(labelLayoutElement, "fontSize", labelLayout::setFontSize);
        loadBoolean(labelLayoutElement, "draggable", labelLayout::setDraggable);
        chartLoaderSupport().loadIntegerList(labelLayoutElement, "labelLinePoints", integers -> {
            if (integers.length != 6) {
                throw new GuiDevelopmentException(
                        String.format("LabelLinePoints of %s must match pattern: 'x1, y1, x2, y2, x3, y3'",
                                AbstractSeries.LabelLayout.class.getSimpleName()),
                        context
                );
            }

            labelLayout.setLabelLinePoints(integers[0], integers[1], integers[2], integers[3], integers[4], integers[5]);
        });

        chartLoaderSupport().loadAlign(labelLayout, labelLayoutElement);

        setter.accept(labelLayout);
    }

    protected void loadSeriesTooltip(Element element, Consumer<AbstractSeries.Tooltip> setter) {
        Element tooltipElement = element.element("tooltip");

        if (tooltipElement == null) {
            return;
        }

        AbstractSeries.Tooltip tooltip = new AbstractSeries.Tooltip();

        loadEnum(tooltipElement, AbstractTooltip.Position.ItemTriggerPosition.class, "position", tooltip::setPosition);
        chartLoaderSupport().loadStringPair(tooltipElement, "positionCoordinates", tooltip::setPosition,
                String.format("%s supports only horizontal and vertical position coordinates",
                        AbstractSeries.Tooltip.class.getSimpleName()));
        loadString(tooltipElement, "formatter", tooltip::setFormatter);
        loadString(tooltipElement, "valueFormatter", tooltip::setValueFormatter);
        chartLoaderSupport().loadColor(tooltipElement, "backgroundColor", tooltip::setBackgroundColor);
        chartLoaderSupport().loadColor(tooltipElement, "borderColor", tooltip::setBorderColor);
        loadInteger(tooltipElement, "borderWidth", tooltip::setBorderWidth);
        loadTextStyle(tooltipElement, "textStyle", tooltip::setTextStyle);
        loadString(tooltipElement, "extraCssText", tooltip::setExtraCssText);

        chartLoaderSupport().loadPadding(tooltip, tooltipElement);

        setter.accept(tooltip);
    }

    protected void loadSeriesLabel(Element element,
                                   String attributeName,
                                   Consumer<io.jmix.chartsflowui.kit.component.model.series.Label> setter) {
        Element labelElement = element.element(attributeName);
        if (labelElement == null) {
            return;
        }

        io.jmix.chartsflowui.kit.component.model.series.Label label =
                new io.jmix.chartsflowui.kit.component.model.series.Label();

        loadAbstractEnhancedLabel(label, labelElement);

        loadString(labelElement, "formatter", label::setFormatter);
        chartLoaderSupport().loadJsFunction(labelElement, "formatterFunction", label::setFormatterFunction);
        loadEnum(labelElement, PositionType.class, "positionType", label::setPosition);
        chartLoaderSupport().loadStringPair(labelElement, "positionCoordinate", label::setPosition,
                String.format(
                        "Position of %s must have two coordinates",
                        io.jmix.chartsflowui.kit.component.model.series.Label.class.getSimpleName()
                ));

        setter.accept(label);
    }

    protected void loadMarkPoint(Element element, Consumer<MarkPoint> setter) {
        Element markPointElement = element.element("markPoint");

        if (markPointElement == null) {
            return;
        }

        MarkPoint markPoint = new MarkPoint();

        loadAbstractMark(markPoint, markPointElement);
        loadItemStyle(markPointElement, "itemStyle", markPoint::setItemStyle);

        Element emphasisElement = markPointElement.element("emphasis");
        if (emphasisElement != null) {
            MarkPoint.Emphasis emphasis = new MarkPoint.Emphasis();

            loadAbstractMarkPointElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);

            markPoint.setEmphasis(emphasis);
        }

        Element blurElement = markPointElement.element("blur");
        if (blurElement != null) {
            MarkPoint.Blur blur = new MarkPoint.Blur();
            loadAbstractMarkPointElement(blur, blurElement);
            markPoint.setBlur(blur);
        }

        Element dataElement = markPointElement.element("data");
        if (dataElement != null) {
            dataElement.elements("point")
                    .forEach(point -> loadMarkPointDataPoint(point, markPoint));
        }

        chartLoaderSupport().loadSymbols(markPoint, markPointElement);

        setter.accept(markPoint);
    }

    protected void loadMarkLine(Element element, Consumer<MarkLine> setter) {
        Element markLineElement = element.element("markLine");

        if (markLineElement == null) {
            return;
        }

        MarkLine markLine = new MarkLine();

        loadAbstractMark(markLine, markLineElement);

        chartLoaderSupport().loadEnumPair(markLineElement, HasSymbols.SymbolType.class, "symbolTypes", markLine::setSymbol,
                String.format(
                        "symbolTypes of %s must contains startSymbolType and endSymbolType",
                        MarkLine.class.getSimpleName()
                ));
        chartLoaderSupport().loadStringPair(markLineElement, "symbolIcons", markLine::setSymbol,
                String.format(
                        "symbolIcons of %s must contains startSymbolIcon and endSymbolIcon",
                        MarkLine.class.getSimpleName()
                ));
        chartLoaderSupport().loadIntegerPair(markLineElement, "symbolSize", markLine::setSymbolSize,
                String.format(
                        "symbolSize of %s must contains startSymbolSize and endSymbolSize",
                        MarkLine.class.getSimpleName()
                ));
        loadInteger(markLineElement, "precision", markLine::setPrecision);
        loadLineStyle(markLineElement, "lineStyle", markLine::setLineStyle);

        Element emphasisElement = markLineElement.element("emphasis");
        if (emphasisElement != null) {
            MarkLine.Emphasis emphasis = new MarkLine.Emphasis();

            loadAbstractMarkLineElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);

            markLine.setEmphasis(emphasis);
        }

        Element blurElement = markLineElement.element("blur");
        if (blurElement != null) {
            MarkLine.Blur blur = new MarkLine.Blur();
            loadAbstractMarkLineElement(blur, blurElement);
            markLine.setBlur(blur);
        }

        Element dataElement = element.element("data");
        if (dataElement != null) {
            MarkLine.Data data = new MarkLine.Data();

            dataElement.elements("singlePointLine")
                    .forEach(point -> data.addSinglePointLine(loadMarkLineDataPoint(point)));

            dataElement.elements("pairPointLine")
                    .forEach(pointPair -> loadMarkLineDataPointPair(pointPair, data::addPairPointLine));

            markLine.setData(data);
        }

        setter.accept(markLine);
    }

    protected void loadMarkArea(Element element, Consumer<MarkArea> setter) {
        Element markAreaElement = element.element("markArea");

        if (markAreaElement == null) {
            return;
        }

        MarkArea markArea = new MarkArea();

        loadAbstractMark(markArea, markAreaElement);

        loadItemStyle(markAreaElement, "itemStyle", markArea::setItemStyle);

        Element emphasisElement = markAreaElement.element("emphasis");
        if (emphasisElement != null) {
            MarkArea.Emphasis emphasis = new MarkArea.Emphasis();

            loadAbstractMarkPointElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);

            markArea.setEmphasis(emphasis);
        }

        Element blurElement = markAreaElement.element("blur");
        if (blurElement != null) {
            MarkArea.Blur blur = new MarkArea.Blur();
            loadAbstractMarkPointElement(blur, blurElement);
            markArea.setBlur(blur);
        }

        Element dataElement = markAreaElement.element("data");
        if (dataElement != null) {
            dataElement.elements("pointPair")
                    .forEach(pointPair -> loadMarkAreaDataPointPair(pointPair, markArea::addPointPair));
        }

        setter.accept(markArea);
    }

    protected void loadMarkPointDataPoint(Element element, MarkPoint markPoint) {
        MarkPoint.Point point = new MarkPoint.Point();

        loadAbstractMarkPointElement(point, element);

        loadString(element, "name", point::setName);
        loadEnum(element, PointDataType.class, "type", point::setType);
        loadInteger(element, "valueIndex", point::setValueIndex);
        loadString(element, "valueDim", point::setValueDim);
        chartLoaderSupport().loadDoublePair(element, "numberCoordinate", point::setCoordinate,
                String.format(
                        "stringCoordinate of %s must contains both coordinate",
                        MarkPoint.Point.class
                ));
        chartLoaderSupport().loadStringPair(element, "stringCoordinate", point::setCoordinate,
                String.format(
                        "stringCoordinate of %s must contains both coordinate",
                        MarkPoint.Point.class
                ));
        loadString(element, "x", point::setX);
        loadString(element, "y", point::setY);

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            MarkPoint.Emphasis emphasis = new MarkPoint.Emphasis();

            loadAbstractMarkPointElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);

            point.setEmphasis(emphasis);
        }

        chartLoaderSupport().loadSymbols(point, element);

        markPoint.addData(point);
    }

    protected void loadMarkLineDataPointPair(Element element, Consumer<MarkLine.PointPair> setter) {
        Element startPointElement = element.element("startPoint");
        Element endPointElement = element.element("endPoint");

        if (startPointElement == null || endPointElement == null) {
            throw new GuiDevelopmentException("startPoint and endPoint is required for the pairPointLine", context);
        }

        MarkLine.Point startPoint = loadMarkLineDataPoint(startPointElement);
        MarkLine.Point endPoint = loadMarkLineDataPoint(endPointElement);

        MarkLine.PointPair pointPair = new MarkLine.PointPair(startPoint, endPoint);
        setter.accept(pointPair);
    }

    protected MarkLine.Point loadMarkLineDataPoint(Element element) {
        MarkLine.Point point = new MarkLine.Point();

        loadAbstractMarkLineElement(point, element);

        loadEnum(element, LineDataType.class, "type", point::setType);
        loadInteger(element, "valueIndex", point::setValueIndex);
        loadString(element, "valueDim", point::setValueDim);
        chartLoaderSupport().loadDoublePair(element, "numberCoordinate", point::setCoordinate,
                String.format(
                        "stringCoordinate of %s must contains both coordinate",
                        MarkLine.Point.class
                ));
        chartLoaderSupport().loadStringPair(element, "stringCoordinate", point::setCoordinate,
                String.format(
                        "stringCoordinate of %s must contains both coordinate",
                        MarkLine.Point.class
                ));
        loadString(element, "name", point::setName);
        loadString(element, "x", point::setX);
        loadString(element, "y", point::setY);
        loadString(element, "xAxis", point::setXAxis);
        loadString(element, "yAxis", point::setYAxis);
        loadDouble(element, "value", point::setValue);

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            MarkLine.Emphasis emphasis = new MarkLine.Emphasis();

            loadAbstractMarkLineElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);

            point.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            MarkLine.Blur blur = new MarkLine.Blur();
            loadAbstractMarkLineElement(blur, blurElement);
            point.setBlur(blur);
        }

        chartLoaderSupport().loadSymbols(point, element);

        return point;
    }

    protected void loadMarkAreaDataPointPair(Element element, Consumer<MarkArea.PointPair> setter) {
        Element leftTopPointElement = element.element("leftTopPoint");
        Element rightBottomPointElement = element.element("rightBottomPoint");

        if (leftTopPointElement == null || rightBottomPointElement == null) {
            throw new GuiDevelopmentException("leftTopPoint and rightBottomPoint is required for the pairPointLine", context);
        }

        MarkArea.Point leftTopPoint = loadMarkAreaDataPoint(leftTopPointElement);
        MarkArea.Point rightBottomPoint = loadMarkAreaDataPoint(rightBottomPointElement);

        MarkArea.PointPair pointPair = new MarkArea.PointPair(leftTopPoint, rightBottomPoint);
        setter.accept(pointPair);
    }

    protected MarkArea.Point loadMarkAreaDataPoint(Element element) {
        MarkArea.Point point = new MarkArea.Point();

        loadAbstractMarkPointElement(point, element);

        loadEnum(element, PointDataType.class, "type", point::setType);
        loadInteger(element, "valueIndex", point::setValueIndex);
        loadString(element, "valueDim", point::setValueDim);
        chartLoaderSupport().loadDoublePair(element, "numberCoordinate", point::setCoordinate,
                String.format(
                        "stringCoordinate of %s must contains both coordinate",
                        MarkArea.Point.class
                ));
        chartLoaderSupport().loadStringPair(element, "stringCoordinate", point::setCoordinate,
                String.format(
                        "stringCoordinate of %s must contains both coordinate",
                        MarkArea.Point.class
                ));
        loadString(element, "name", point::setName);
        loadString(element, "x", point::setX);
        loadString(element, "y", point::setY);
        loadDouble(element, "value", point::setValue);

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            MarkArea.Emphasis emphasis = new MarkArea.Emphasis();

            loadAbstractMarkPointElement(emphasis, emphasisElement);
            loadBoolean(emphasisElement, "disabled", emphasis::setDisabled);

            point.setEmphasis(emphasis);
        }

        Element blurElement = element.element("blur");
        if (blurElement != null) {
            MarkArea.Blur blur = new MarkArea.Blur();
            loadAbstractMarkPointElement(blur, blurElement);
            point.setBlur(blur);
        }

        return point;
    }

    protected void loadAbstractMarkPointElement(AbstractMarkElement<?> markPointElement, Element element) {
        loadSeriesLabel(element, "label", markPointElement::setLabel);
        loadItemStyle(element, "itemStyle", markPointElement::setItemStyle);
    }

    protected void loadAbstractMarkLineElement(MarkLine.AbstractMarkLineElement<?> markLineElement, Element element) {
        loadSeriesLabel(element, "label", markLineElement::setLabel);
        loadLineStyle(element, "lineStyle", markLineElement::setLineStyle);
    }

    protected void loadAbstractMark(AbstractMark<?> mark, Element element) {
        loadBoolean(element, "silent", mark::setSilent);
        loadSeriesLabel(element, "label", mark::setLabel);
        loadBoolean(element, "animation", mark::setAnimation);
        loadInteger(element, "animationThreshold", mark::setAnimationThreshold);
        loadInteger(element, "animationDuration", mark::setAnimationDuration);
        loadString(element, "animationEasing", mark::setAnimationEasing);
        loadInteger(element, "animationDelay", mark::setAnimationDelay);
        loadInteger(element, "animationDurationUpdate", mark::setAnimationDurationUpdate);
        loadString(element, "animationEasingUpdate", mark::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", mark::setAnimationDelayUpdate);
    }

    protected void loadDataSet(Chart chart, Element element) {
        Element dataSetElement = element.element("dataSet");

        if (dataSetElement == null) {
            return;
        }

        DataSet dataSet = new DataSet();

        loadString(dataSetElement, "id", dataSet::setId);

        Element sourceElement = dataSetElement.element("source");

        if (sourceElement != null) {
            DataSet.Source<?> source = new DataSet.Source<>();

            loadString(sourceElement, "categoryField", source::setCategoryField);
            chartLoaderSupport().loadStringList(sourceElement, "valueFields", source::setValueFields);
            loadDataContainer(source, sourceElement);

            dataSet.setSource(source);
        }

        chart.setDataSet(dataSet);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void loadDataContainer(DataSet.Source<?> source, Element element) {
        loadString(element, "dataContainer")
                .ifPresent(dataContainerId -> {
                    InstanceContainer container = getComponentContext().getViewData().getContainer(dataContainerId);

                    CollectionContainer collectionContainer;
                    if (container instanceof CollectionContainer cc) {
                        collectionContainer = cc;
                    } else {
                        throw new GuiDevelopmentException("Not a CollectionContainer: " + container, context);
                    }

                    source.setDataProvider(new ContainerChartItems<>(collectionContainer));
                });
    }

    protected void loadColorPalette(Chart chart, Element element) {
        loadString(element, "colorPalette")
                .map(chartLoaderSupport()::split)
                .stream()
                .flatMap(Collection::stream)
                .map(Color::valueOf)
                .forEach(chart::addColorToPalette);
    }

    protected void loadNativeJson(Chart chart, Element element) {
        Element contentElement = element.element(NATIVE_JSON_ELEMENT_NAME);
        if (contentElement == null) {
            return;
        }

        String nativeJsonText = contentElement.getText();
        if (Strings.isNullOrEmpty(nativeJsonText)) {
            throw new GuiDevelopmentException(
                    String.format("'%s' element cannot be empty", NATIVE_JSON_ELEMENT_NAME), context);
        }

        chart.setNativeJson(nativeJsonText);
    }

    protected void loadStateAnimation(Chart chart, Element element) {
        Element stateAnimationElement = element.element("stateAnimation");

        if (stateAnimationElement == null) {
            return;
        }

        ChartOptions.StateAnimation stateAnimation = new ChartOptions.StateAnimation();

        loadInteger(stateAnimationElement, "duration", stateAnimation::setDuration);
        loadString(stateAnimationElement, "easing", stateAnimation::setEasing);

        chart.setStateAnimation(stateAnimation);
    }

    protected void loadTextStyle(Element element, String attributeName, Consumer<TextStyle> setter) {
        Element textStyleElement = element.element(attributeName);

        if (textStyleElement == null) {
            return;
        }

        TextStyle textStyle = new TextStyle();
        loadAbstractText(textStyle, textStyleElement);

        setter.accept(textStyle);
    }

    protected void loadTitle(Chart chart, Element element) {
        Element titleElement = element.element("title");
        if (titleElement == null) {
            return;
        }

        Title title = new Title();

        loadString(titleElement, "id", title::setId);
        loadBoolean(titleElement, "show", title::setShow);
        loadResourceString(titleElement, "text", context.getMessageGroup(), title::setText);
        loadString(titleElement, "link", title::setLink);
        loadEnum(titleElement, Title.Target.class, "target", title::setTarget);
        loadResourceString(titleElement, "subtext", context.getMessageGroup(), title::setSubtext);
        loadString(titleElement, "sublink", title::setSublink);
        loadEnum(titleElement, Title.Target.class, "subtarget", title::setSubtarget);
        loadEnum(titleElement, Title.TextAlign.class, "textAlign", title::setTextAlign);
        loadEnum(titleElement, Title.TextVerticalAlign.class, "textVerticalAlign", title::setTextVerticalAlign);
        loadBoolean(titleElement, "triggerEvent", title::setTriggerEvent);
        loadInteger(titleElement, "itemGap", title::setItemGap);
        loadInteger(titleElement, "zLevel", title::setZLevel);
        loadInteger(titleElement, "z", title::setZ);

        chartLoaderSupport().loadColor(titleElement, "backgroundColor", title::setBackgroundColor);

        chartLoaderSupport().loadPosition(title, titleElement);
        chartLoaderSupport().loadShadow(title, titleElement);
        chartLoaderSupport().loadPadding(title, titleElement);
        chartLoaderSupport().loadBorder(title, titleElement);

        Element textStyleElement = titleElement.element("textStyle");
        if (textStyleElement != null) {
            Title.TextStyle textStyle = new Title.TextStyle();

            loadAbstractRichText(textStyle, textStyleElement);
            title.setTextStyle(textStyle);
        }

        Element subtextStyleElement = titleElement.element("subtextStyle");
        if (subtextStyleElement != null) {
            Title.SubtextStyle subtextStyle = new Title.SubtextStyle();

            loadAbstractRichText(subtextStyle, subtextStyleElement);
            chartLoaderSupport().loadAlign(subtextStyle, subtextStyleElement);

            title.setSubtextStyle(subtextStyle);
        }

        chart.setTitle(title);
    }

    protected void loadLegend(Chart chart, Element element) {
        Element legendElement = element.element("legend");
        Element scrollabelLegendElement = element.element("scrollableLegend");

        if (legendElement == null && scrollabelLegendElement == null) {
            return;
        }

        AbstractLegend<?> legend;

        if (legendElement != null && scrollabelLegendElement != null) {
            String message = String.format("%s with '%s' ID must have a single legend element",
                    chart.getClass().getSimpleName(), chart.getId().orElse(null));
            throw new GuiDevelopmentException(message, context);
        } else if (legendElement != null) {
            legend = new Legend();
            loadAbstractLegend(legend, legendElement);
        } else {
            legend = new ScrollableLegend();
            loadScrollableLegend((ScrollableLegend) legend, scrollabelLegendElement);
        }

        chart.setLegend(legend);
    }

    protected void loadScrollableLegend(ScrollableLegend scrollableLegend, Element element) {
        loadAbstractLegend(scrollableLegend, element);

        loadInteger(element, "scrollDataIndex", scrollableLegend::setScrollDataIndex);
        loadInteger(element, "pageButtonItemGap", scrollableLegend::setPageButtonItemGap);
        loadInteger(element, "pageButtonGap", scrollableLegend::setPageButtonGap);
        loadEnum(element, AbstractLegend.Position.class, "pageButtonPosition", scrollableLegend::setPageButtonPosition);
        loadString(element, "pageFormatter", scrollableLegend::setPageFormatter);
        chartLoaderSupport().loadJsFunction(element, "pageFormatterFunction", scrollableLegend::setPageFormatterFunction);
        chartLoaderSupport().loadColor(element, "pageIconColor", scrollableLegend::setPageIconColor);
        chartLoaderSupport().loadColor(element, "pageIconInactiveColor", scrollableLegend::setPageIconInactiveColor);
        loadInteger(element, "pageIconSize", scrollableLegend::setPageIconSize);
        loadBoolean(element, "animation", scrollableLegend::setAnimation);
        loadInteger(element, "animationDurationUpdate", scrollableLegend::setAnimationDurationUpdate);
        loadTextStyle(element, "pageTextStyle", scrollableLegend::setPageTextStyle);

        Element pageIconsElement = element.element("pageIcons");
        if (pageIconsElement != null) {
            ScrollableLegend.PageIcons pageIcons = new ScrollableLegend.PageIcons();

            Element horizontalPageIconsElement = pageIconsElement.element("horizontal");
            if (horizontalPageIconsElement != null) {
                loadPageIcons(horizontalPageIconsElement, pageIcons::setHorizontal);
            }

            Element verticalPageIconsElement = pageIconsElement.element("vertical");
            if (verticalPageIconsElement != null) {
                loadPageIcons(verticalPageIconsElement, pageIcons::setVertical);
            }
        }
    }

    protected void loadPageIcons(Element element, BiConsumer<String, String> setter) {
        String prevPageIcon = loadString(element, "prevPageIcon")
                .orElseThrow(() -> new GuiDevelopmentException(
                        String.format("'prevPageIcon' is required for %s", ScrollableLegend.PageIcons.class),
                        context
                ));
        String nextPageIcon = loadString(element, "nextPageIcon")
                .orElseThrow(() -> new GuiDevelopmentException(
                        String.format("'nextPageIcon' is required for %s", ScrollableLegend.PageIcons.class),
                        context
                ));

        setter.accept(prevPageIcon, nextPageIcon);
    }

    protected void loadAbstractLegend(AbstractLegend<?> legend, Element element) {
        loadString(element, "id", legend::setId);
        loadBoolean(element, "show", legend::setShow);
        loadInteger(element, "zLevel", legend::setZLevel);
        loadInteger(element, "z", legend::setZ);
        loadString(element, "width", legend::setWidth);
        loadString(element, "height", legend::setHeight);
        loadEnum(element, Orientation.class, "orientation", legend::setOrientation);
        loadEnum(element, AbstractLegend.Align.class, "align", legend::setAlign);
        loadInteger(element, "itemGap", legend::setItemGap);
        loadInteger(element, "itemWidth", legend::setItemWidth);
        loadInteger(element, "itemHeight", legend::setItemHeight);
        loadInteger(element, "symbolRotate", legend::setSymbolRotate);
        loadString(element, "formatter", legend::setFormatter);
        loadEnum(element, SelectedMode.class, "selectedMode", legend::setSelectedMode);
        chartLoaderSupport().loadColor(element, "inactiveColor", legend::setInactiveColor);
        chartLoaderSupport().loadColor(element, "inactiveBorderColor", legend::setInactiveBorderColor);
        loadInteger(element, "inactiveBorderWidth", legend::setInactiveBorderWidth);
        loadString(element, "icon", legend::setIcon);
        chartLoaderSupport().loadColor(element, "backgroundColor", legend::setBackgroundColor);
        loadBoolean(element, "selector", legend::setSelector);
        loadEnum(element, AbstractLegend.Position.class, "selectorPosition", legend::setSelectorPosition);
        loadInteger(element, "selectorItemGap", legend::setSelectorItemGap);
        loadInteger(element, "selectorButtonGap", legend::setSelectorButtonGap);
        chartLoaderSupport().loadJsFunction(element, "formatterFunction", legend::setFormatterFunction);

        HashMap<String, Boolean> unselectedSeries = loadString(element, "unselectedSeries")
                .map(chartLoaderSupport()::split)
                .stream()
                .flatMap(Collection::stream)
                .collect(HashMap::new, (m, v) -> m.put(v, false), HashMap::putAll);

        if (!unselectedSeries.isEmpty()) {
            legend.setSelectedSeries(unselectedSeries);
        }

        loadItemStyle(element, "itemStyle", legend::setItemStyle);
        loadLineStyle(element, "lineStyle", legend::setLineStyle);

        Element emphasisElement = element.element("emphasis");
        if (emphasisElement != null) {
            AbstractLegend.Emphasis emphasis = new AbstractLegend.Emphasis();

            Element selectorLabelElement = emphasisElement.element("selectorLabel");
            if (selectorLabelElement != null) {
                AbstractLegend.SelectorLabel selectorLabel = new AbstractLegend.SelectorLabel();
                loadAbstractEnhancedLabel(selectorLabel, selectorLabelElement);
                emphasis.setSelectorLabel(selectorLabel);
            }

            legend.setEmphasis(emphasis);
        }

        Element selectorLabelElement = element.element("selectorLabel");
        if (selectorLabelElement != null) {
            AbstractLegend.SelectorLabel selectorLabel = new AbstractLegend.SelectorLabel();
            loadAbstractEnhancedLabel(selectorLabel, selectorLabelElement);
            legend.setSelectorLabel(selectorLabel);
        }

        Element textStyleElement = element.element("textStyle");
        if (textStyleElement != null) {
            AbstractLegend.TextStyle textStyle = new AbstractLegend.TextStyle();

            loadAbstractRichText(textStyle, textStyleElement);

            chartLoaderSupport().loadColor(textStyleElement, "backgroundColor", textStyle::setBackgroundColor);
            loadString(textStyleElement, "borderType", textStyle::setBorderType);
            loadInteger(textStyleElement, "borderDashOffset", textStyle::setBorderDashOffset);

            chartLoaderSupport().loadShadow(textStyle, textStyleElement);
            chartLoaderSupport().loadBorder(textStyle, textStyleElement);
            chartLoaderSupport().loadPadding(textStyle, textStyleElement);

            legend.setTextStyle(textStyle);
        }

        loadTooltip(element, legend::setTooltip);

        chartLoaderSupport().loadShadow(legend, element);
        chartLoaderSupport().loadPosition(legend, element);
        chartLoaderSupport().loadBorder(legend, element);
        chartLoaderSupport().loadPadding(legend, element);
    }

    protected void loadGrid(Chart chart, Element element) {
        Element gridElement = element.element("grid");

        if (gridElement == null) {
            return;
        }

        List<Element> gridItemElements = gridElement.elements("gridItem");

        if (!gridItemElements.isEmpty()) {
            gridItemElements.forEach(gridItem -> loadGridItem(chart, gridItem));
        }
    }

    protected void loadGridItem(Chart chart, Element element) {
        Grid grid = new Grid();

        loadString(element, "id", grid::setId);
        loadBoolean(element, "show", grid::setShow);
        loadInteger(element, "zLevel", grid::setZLevel);
        loadInteger(element, "z", grid::setZ);
        loadString(element, "width", grid::setWidth);
        loadString(element, "height", grid::setHeight);
        loadBoolean(element, "containLabel", grid::setContainLabel);
        loadInteger(element, "borderWidth", grid::setBorderWidth);

        chartLoaderSupport().loadColor(element, "backgroundColor", grid::setBackgroundColor);
        chartLoaderSupport().loadColor(element, "borderColor", grid::setBorderColor);

        chartLoaderSupport().loadPosition(grid, element);
        chartLoaderSupport().loadShadow(grid, element);

        Element innerTooltipElement = element.element("tooltip");
        if (innerTooltipElement != null) {
            InnerTooltip innerTooltip = new InnerTooltip();

            loadAbstractTooltip(innerTooltip, innerTooltipElement);

            grid.setTooltip(innerTooltip);
        }

        chart.addGrid(grid);
    }

    protected void loadAxes(Chart chart, Element element) {
        loadCartesianAxes(element, XAxis::new, "xAxes", "xAxis", chart::addXAxis);
        loadCartesianAxes(element, YAxis::new, "yAxes", "yAxis", chart::addYAxis);
    }

    protected void loadPolar(Chart chart, Element element) {
        Element polarElement = element.element("polar");

        if (polarElement == null) {
            return;
        }

        Polar polar = new Polar();

        loadString(polarElement, "id", polar::setId);
        loadInteger(polarElement, "zLevel", polar::setZLevel);
        loadInteger(polarElement, "z", polar::setZ);
        chartLoaderSupport().loadStringPair(polarElement, "center", polar::setCenter,
                String.format("center of the %s has only two coordinates", Polar.class.getSimpleName()));
        chartLoaderSupport().loadStringPair(polarElement, "radius", polar::setRadius,
                String.format("radius of the %s has only inner and outer values", Polar.class.getSimpleName()));

        Element innerTooltipElement = element.element("tooltip");
        if (innerTooltipElement != null) {
            InnerTooltip innerTooltip = new InnerTooltip();

            loadAbstractTooltip(innerTooltip, innerTooltipElement);

            polar.setTooltip(innerTooltip);
        }

        chart.setPolar(polar);
    }

    protected void loadRadiusAxis(Chart chart, Element element) {
        Element radiusAxisElement = element.element("radiusAxis");

        if (radiusAxisElement == null) {
            return;
        }

        RadiusAxis radiusAxis = new RadiusAxis();

        loadPolarAxis(radiusAxis, radiusAxisElement);
        chartLoaderSupport().loadAxisNameAttributes(radiusAxis, radiusAxisElement, this::loadAbstractRichText);

        chart.setRadiusAxis(radiusAxis);
    }

    protected void loadAngleAxis(Chart chart, Element element) {
        Element angleAxisElement = element.element("angleAxis");

        if (angleAxisElement == null) {
            return;
        }

        AngleAxis angleAxis = new AngleAxis();

        loadPolarAxis(angleAxis, angleAxisElement);
        loadInteger(angleAxisElement, "startAngle", angleAxis::setStartAngle);
        loadBoolean(angleAxisElement, "clockwise", angleAxis::setClockwise);

        chart.setAngleAxis(angleAxis);
    }

    protected void loadRadar(Chart chart, Element element) {
        Element radarElement = element.element("radar");

        if (radarElement == null) {
            return;
        }

        Radar radar = new Radar();

        loadString(radarElement, "id", radar::setId);
        loadInteger(radarElement, "zLevel", radar::setZLevel);
        loadInteger(radarElement, "z", radar::setZ);
        chartLoaderSupport().loadStringPair(radarElement, "center", radar::setCenter,
                String.format("center of %s must have two coordinates", Radar.class.getSimpleName()));
        chartLoaderSupport().loadStringPair(radarElement, "radius", radar::setRadius,
                String.format("radius of %s must have inner and outer values", Radar.class.getSimpleName()));
        loadInteger(radarElement, "startAngle", radar::setStartAngle);
        loadInteger(radarElement, "nameGap", radar::setNameGap);
        loadInteger(radarElement, "splitNumber", radar::setSplitNumber);
        loadBoolean(radarElement, "scale", radar::setScale);
        loadBoolean(radarElement, "silent", radar::setSilent);
        loadBoolean(radarElement, "triggerEvent", radar::setTriggerEvent);
        loadEnum(radarElement, Radar.Shape.class, "shape", radar::setShape);

        Element axisNameElement = radarElement.element("axisName");

        if (axisNameElement != null) {
            Radar.AxisName axisName = new Radar.AxisName();

            loadBoolean(axisNameElement, "show", axisName::setShow);
            loadString(axisNameElement, "formatter", axisName::setFormatter);
            chartLoaderSupport().loadJsFunction(axisNameElement, "formatterFunction", axisName::setFormatterFunction);
            loadString(axisNameElement, "borderType", axisName::setBorderType);
            loadInteger(axisNameElement, "borderDashOffset", axisName::setBorderDashOffset);

            chartLoaderSupport().loadColor(axisNameElement, "backgroundColor", axisName::setBackgroundColor);

            chartLoaderSupport().loadBorder(axisName, axisNameElement);
            chartLoaderSupport().loadShadow(axisName, axisNameElement);
            chartLoaderSupport().loadPadding(axisName, axisNameElement);
        }

        loadAxisLine(radarElement, radar::setAxisLine);
        loadAxisTick(radarElement, radar::setAxisTick);
        loadAxisLabel(radarElement, radar::setAxisLabel);
        loadSplitLine(radarElement, radar::setSplitLine);
        loadSplitArea(radarElement, radar::setSplitArea);

        Element indicatorsElement = radarElement.element("indicators");
        if (indicatorsElement != null) {
            List<Element> indicatorElements = indicatorsElement.elements("indicator");

            if (!indicatorElements.isEmpty()) {
                indicatorElements.forEach(indicator -> loadIndicator(radar, indicator));
            }
        }

        chart.setRadar(radar);
    }

    protected void loadDataZoom(Chart chart, Element element) {
        Element dataZoomElement = element.element("dataZoom");

        if (dataZoomElement == null) {
            return;
        }

        List<Element> insideDataZoomElements = dataZoomElement.elements("insideDataZoom");
        if (!insideDataZoomElements.isEmpty()) {
            insideDataZoomElements.forEach(insideDataZoom -> loadInsideDataZoom(chart, insideDataZoom));
        }

        List<Element> sliderDataZoomElements = dataZoomElement.elements("sliderDataZoom");
        if (!sliderDataZoomElements.isEmpty()) {
            sliderDataZoomElements.forEach(sliderDataZoom -> loadSliderDataZoom(chart, sliderDataZoom));
        }
    }

    protected void loadInsideDataZoom(Chart chart, Element element) {
        InsideDataZoom insideDataZoom = new InsideDataZoom();

        loadAbstractDataZoom(insideDataZoom, element);

        loadBoolean(element, "disabled", insideDataZoom::setDisabled);
        loadBoolean(element, "zoomOnMouseWheel", insideDataZoom::setZoomOnMouseWheel);
        loadBoolean(element, "moveOnMouseMove", insideDataZoom::setMoveOnMouseMove);
        loadBoolean(element, "moveOnMouseWheel", insideDataZoom::setMoveOnMouseWheel);
        loadBoolean(element, "preventDefaultMouseMove", insideDataZoom::setPreventDefaultMouseMove);

        chart.addDataZoom(insideDataZoom);
    }

    protected void loadSliderDataZoom(Chart chart, Element element) {
        SliderDataZoom sliderDataZoom = new SliderDataZoom();

        loadAbstractDataZoom(sliderDataZoom, element);

        loadBoolean(element, "show", sliderDataZoom::setShow);
        loadInteger(element, "borderRadius", sliderDataZoom::setBorderRadius);
        loadString(element, "handleIcon", sliderDataZoom::setHandleIcon);
        loadString(element, "handleSize", sliderDataZoom::setHandleSize);
        loadItemStyle(element, "handleStyle", sliderDataZoom::setHandleStyle);
        loadString(element, "moveHandleIcon", sliderDataZoom::setMoveHandleIcon);
        loadInteger(element, "moveHandleSize", sliderDataZoom::setMoveHandleSize);
        loadItemStyle(element, "moveHandleStyle", sliderDataZoom::setMoveHandleStyle);
        loadInteger(element, "labelPrecision", sliderDataZoom::setLabelPrecision);
        loadString(element, "labelFormatter", sliderDataZoom::setLabelFormatter);
        chartLoaderSupport().loadJsFunction(element, "labelFormatterFunction", sliderDataZoom::setLabelFormatterFunction);
        loadBoolean(element, "showDetail", sliderDataZoom::setShowDetail);
        loadBoolean(element, "showDataShadow", sliderDataZoom::setShowDataShadow);
        loadBoolean(element, "realtime", sliderDataZoom::setRealtime);
        loadTextStyle(element, "textStyle", sliderDataZoom::setTextStyle);
        loadInteger(element, "zLevel", sliderDataZoom::setZLevel);
        loadInteger(element, "z", sliderDataZoom::setZ);
        loadString(element, "width", sliderDataZoom::setWidth);
        loadString(element, "height", sliderDataZoom::setHeight);
        loadBoolean(element, "brushSelect", sliderDataZoom::setBrushSelect);
        loadItemStyle(element, "brushStyle", sliderDataZoom::setBrushStyle);

        chartLoaderSupport().loadPosition(sliderDataZoom, element);
        chartLoaderSupport().loadColor(element, "backgroundColor", sliderDataZoom::setBackgroundColor);
        chartLoaderSupport().loadColor(element, "fillerColor", sliderDataZoom::setFillerColor);
        chartLoaderSupport().loadColor(element, "borderColor", sliderDataZoom::setBorderColor);

        Element dataBackgroundElement = element.element("dataBackground");
        if (dataBackgroundElement != null) {
            SliderDataZoom.DataBackground dataBackground = new SliderDataZoom.DataBackground();
            loadDataBackground(dataBackground, dataBackgroundElement);
            sliderDataZoom.setDataBackground(dataBackground);
        }

        Element selectedDataBackgroundElement = element.element("selectedDataBackground");
        if (selectedDataBackgroundElement != null) {
            SliderDataZoom.DataBackground selectedDataBackground = new SliderDataZoom.DataBackground();
            loadDataBackground(selectedDataBackground, selectedDataBackgroundElement);
            sliderDataZoom.setSelectedDataBackground(selectedDataBackground);
        }

        Element emphasisElement = element.element("emphasis");

        if (emphasisElement != null) {
            SliderDataZoom.Emphasis emphasis = new SliderDataZoom.Emphasis();
            loadItemStyle(emphasisElement, "handleStyle", emphasis::setHandleStyle);
            loadItemStyle(emphasisElement, "moveHandleStyle", emphasis::setMoveHandleStyle);
            sliderDataZoom.setEmphasis(emphasis);
        }

        chart.addDataZoom(sliderDataZoom);
    }

    protected void loadVisualMap(Chart chart, Element element) {
        Element visualMapElement = element.element("visualMap");

        if (visualMapElement == null) {
            return;
        }

        for (Element visualMapItemElement : visualMapElement.elements()) {
            switch (visualMapItemElement.getName()) {
                case "piecewiseVisualMap" -> loadPiecewiseVisualMap(chart, visualMapItemElement);
                case "continuousVisualMap" -> loadContinuousVisualMap(chart, visualMapItemElement);
                default -> throw new GuiDevelopmentException(
                        String.format("Unexpected %s %s", AbstractVisualMap.class.getSimpleName(),
                                visualMapItemElement.getName()), context);
            }
        }
    }

    protected void loadPiecewiseVisualMap(Chart chart, Element element) {
        PiecewiseVisualMap piecewiseVisualMap = new PiecewiseVisualMap();

        loadAbstractVisualMap(piecewiseVisualMap, element);

        loadInteger(element, "splitNumber", piecewiseVisualMap::setSplitNumber);
        chartLoaderSupport().loadStringList(element, "categories", piecewiseVisualMap::setCategories);
        loadBoolean(element, "minOpen", piecewiseVisualMap::setMinOpen);
        loadBoolean(element, "maxOpen", piecewiseVisualMap::setMaxOpen);
        loadEnum(element, SelectedMode.class, "selectedMode", piecewiseVisualMap::setSelectedMode);
        loadBoolean(element, "showLabel", piecewiseVisualMap::setShowLabel);
        loadInteger(element, "itemGap", piecewiseVisualMap::setItemGap);
        loadEnum(element, HasSymbols.SymbolType.class, "itemSymbol", piecewiseVisualMap::setItemSymbol);

        Element piecesElements = element.element("pieces");
        if (piecesElements != null) {
            piecesElements.elements("piece")
                    .forEach(piece -> loadPiece(piecewiseVisualMap, piece));
        }

        chart.addVisualMap(piecewiseVisualMap);
    }

    protected void loadContinuousVisualMap(Chart chart, Element element) {
        ContinuousVisualMap continuousVisualMap = new ContinuousVisualMap();

        loadAbstractVisualMap(continuousVisualMap, element);

        loadBoolean(element, "calculable", continuousVisualMap::setCalculable);
        chartLoaderSupport().loadIntegerPair(element, "range", continuousVisualMap::setRange,
                String.format("The %s range must contain both the min and max values",
                        ContinuousVisualMap.class.getSimpleName()
                ));
        loadBoolean(element, "realtime", continuousVisualMap::setRealtime);
        loadString(element, "handleIcon", continuousVisualMap::setHandleIcon);
        loadString(element, "handleSize", continuousVisualMap::setHandleSize);
        loadItemStyle(element, "handleStyle", continuousVisualMap::setHandleStyle);
        loadString(element, "indicatorIcon", continuousVisualMap::setIndicatorIcon);
        loadString(element, "indicatorSize", continuousVisualMap::setIndicatorSize);
        loadItemStyle(element, "indicatorStyle", continuousVisualMap::setIndicatorStyle);

        chart.addVisualMap(continuousVisualMap);
    }

    protected void loadAxisPointer(Chart chart, Element element) {
        Element axisPointerElement = element.element("axisPointer");

        if (axisPointerElement == null) {
            return;
        }

        AxisPointer axisPointer = new AxisPointer();

        loadAbstractAxisPointer(axisPointer, axisPointerElement);

        loadString(axisPointerElement, "id", axisPointer::setId);
        loadEnum(axisPointerElement, TriggerOnMode.class, "triggerOn", axisPointer::setTriggerOn);

        chart.setAxisPointer(axisPointer);
    }

    protected void loadToolbox(Chart chart, Element element) {
        Element toolboxElement = element.element("toolbox");

        if (toolboxElement == null) {
            return;
        }

        Toolbox toolbox = new Toolbox();

        loadString(toolboxElement, "id", toolbox::setId);
        loadBoolean(toolboxElement, "show", toolbox::setShow);
        loadEnum(toolboxElement, Orientation.class, "orientation", toolbox::setOrientation);
        loadInteger(toolboxElement, "itemSize", toolbox::setItemSize);
        loadInteger(toolboxElement, "itemGap", toolbox::setItemGap);
        loadBoolean(toolboxElement, "showTitle", toolbox::setShowTitle);
        loadItemStyle(toolboxElement, "iconStyle", toolbox::setIconStyle);
        loadInteger(toolboxElement, "zLevel", toolbox::setZLevel);
        loadInteger(toolboxElement, "z", toolbox::setZ);
        loadString(toolboxElement, "width", toolbox::setWidth);
        loadString(toolboxElement, "height", toolbox::setHeight);
        loadTooltip(toolboxElement, toolbox::setTooltip);

        loadToolboxEmphasis(toolboxElement, toolbox::setEmphasis);

        Element toolboxFeaturesElements = toolboxElement.element("features");
        if (toolboxFeaturesElements != null) {
            toolboxFeaturesElements.elements().forEach(feature -> loadToolboxFeature(toolbox, feature));
        }

        chartLoaderSupport().loadPosition(toolbox, toolboxElement);

        chart.setToolbox(toolbox);
    }

    protected void loadBrush(Chart chart, Element element) {
        Element brushElement = element.element("brush");

        if (brushElement == null) {
            return;
        }

        Brush brush = new Brush();

        loadString(brushElement, "id", brush::setId);
        chartLoaderSupport().loadEnumList(brushElement, Brush.Toolbox.class, "toolboxes", brush::setToolboxes);
        chartLoaderSupport().loadIntegerList(brushElement, "brushLinkIndexes", brush::setBrushLink);
        loadEnum(brushElement, Brush.BrushSelectMode.class, "brushLinkMode", brush::setBrushLink);
        loadEnum(brushElement, Brush.SeriesIndex.class, "seriesIndex", brush::setSeriesIndex);
        loadInteger(brushElement, "geoIndex", brush::setGeoIndex);
        chartLoaderSupport().loadIntegerList(brushElement, "geoIndexes", brush::setGeoIndex);
        loadEnum(brushElement, Brush.BrushSelectMode.class, "geoIndexMode", brush::setGeoIndex);
        loadInteger(brushElement, "xAxisIndex", brush::setXAxisIndex);
        chartLoaderSupport().loadIntegerList(brushElement, "xAxisIndexes", brush::setXAxisIndex);
        loadEnum(brushElement, Brush.BrushSelectMode.class, "xAxisIndexMode", brush::setXAxisIndex);
        loadInteger(brushElement, "yAxisIndex", brush::setYAxisIndex);
        chartLoaderSupport().loadIntegerList(brushElement, "yAxisIndexes", brush::setYAxisIndex);
        loadEnum(brushElement, Brush.BrushSelectMode.class, "yAxisIndexMode", brush::setYAxisIndex);
        loadEnum(brushElement, Brush.BrushType.class, "brushType", brush::setBrushType);
        loadEnum(brushElement, Brush.BrushMode.class, "brushMode", brush::setBrushMode);
        loadBoolean(brushElement, "transformable", brush::setTransformable);
        loadEnum(brushElement, Brush.ThrottleType.class, "throttleType", brush::setThrottleType);
        loadDouble(brushElement, "throttleDelay", brush::setThrottleDelay);
        loadBoolean(brushElement, "removeOnClick", brush::setRemoveOnClick);
        loadVisualEffect(brushElement, "inBrush", brush::setInBrush);
        loadVisualEffect(brushElement, "outOfBrush", brush::setOutOfBrush);
        loadInteger(brushElement, "z", brush::setZ);

        Element brushStyleElement = brushElement.element("brushStyle");
        if (brushStyleElement != null) {
            Brush.BrushStyle brushStyle = new Brush.BrushStyle();

            loadInteger(brushStyleElement, "borderWidth", brushStyle::setBorderWidth);
            chartLoaderSupport().loadColor(brushStyleElement, "color", brushStyle::setColor);
            chartLoaderSupport().loadColor(brushStyleElement, "borderColor", brushStyle::setBorderColor);

            brush.setBrushStyle(brushStyle);
        }

        chart.setBrush(brush);
    }

    protected void loadAria(Chart chart, Element element) {
        Element ariaElement = element.element("aria");

        if (ariaElement == null) {
            return;
        }

        Aria aria = new Aria();

        loadBoolean(ariaElement, "enabled", aria::setEnabled);

        Element ariaLabelElement = ariaElement.element("label");
        if (ariaLabelElement != null) {
            Aria.Label ariaLabel = new Aria.Label();
            loadAriaLabel(ariaLabel, ariaLabelElement);
            aria.setLabel(ariaLabel);
        }

        Element decalElement = ariaElement.element("decal");
        if (decalElement != null) {
            Aria.Decal decal = new Aria.Decal();

            loadBoolean(decalElement, "show", decal::setShow);

            Element decalsElement = decalElement.element("decals");
            if (decalsElement != null) {
                List<Element> decalElements = decalsElement.elements("decal");

                decalElements.forEach(decalEl -> loadDecal(decalEl, decal::addDecal));
            }

            aria.setDecal(decal);
        }

        chart.setAria(aria);
    }

    protected void loadDecal(Element element, Consumer<Decal> setter) {
        Decal decal = new Decal();

        loadEnum(element, HasSymbols.SymbolType.class, "symbol", decal::setSymbol);
        loadDouble(element, "symbolSize", decal::setSymbolSize);
        loadBoolean(element, "symbolKeepAspect", decal::setSymbolKeepAspect);
        chartLoaderSupport().loadColor(element, "color", decal::setColor);
        chartLoaderSupport().loadColor(element, "backgroundColor", decal::setBackgroundColor);
        loadInteger(element, "dashGapX", decal::setDashArrayX);
        chartLoaderSupport().loadIntegerList(element, "dashArrayX", decal::setDashArrayX);
        loadInteger(element, "dashGapY", decal::setDashArrayY);
        chartLoaderSupport().loadIntegerList(element, "dashArrayY", decal::setDashArrayY);
        loadDouble(element, "rotation", decal::setRotation);
        loadInteger(element, "maxTileWidth", decal::setMaxTileWidth);
        loadInteger(element, "maxTileHeight", decal::setMaxTileHeight);

        setter.accept(decal);
    }

    protected void loadAriaLabel(Aria.Label label, Element element) {
        loadBoolean(element, "enabled", label::setEnabled);
        loadResourceString(element, "description", context.getMessageGroup(), label::setDescription);

        Element generalElement = element.element("general");
        if (generalElement != null) {
            Aria.Label.General general = new Aria.Label.General();

            loadResourceString(generalElement, "withTitle", context.getMessageGroup(), general::setWithTitle);
            loadResourceString(generalElement, "withoutTitle", context.getMessageGroup(), general::setWithoutTitle);

            label.setGeneral(general);
        }

        Element seriesElement = element.element("series");
        if (seriesElement != null) {
            Aria.Label.Series series = new Aria.Label.Series();

            loadInteger(seriesElement, "maxCount", series::setMaxCount);

            Element singleElement = seriesElement.element("single");
            if (singleElement != null) {
                Aria.Label.Series.Single single = new Aria.Label.Series.Single();
                loadAbstractAriaSeriesLabel(single, singleElement);
                series.setSingle(single);
            }

            Element multipleElement = seriesElement.element("multiple");
            if (multipleElement != null) {
                Aria.Label.Series.Multiple multiple = new Aria.Label.Series.Multiple();

                loadAbstractAriaSeriesLabel(multiple, multipleElement);
                loadSeparator(multipleElement, multiple::setSeparator);

                series.setMultiple(multiple);
            }

            label.setSeries(series);
        }

        Element dataElement = element.element("data");
        if (dataElement != null) {
            Aria.Label.Data data = new Aria.Label.Data();

            loadInteger(dataElement, "maxCount", data::setMaxCount);
            loadResourceString(dataElement, "allData", context.getMessageGroup(), data::setAllData);
            loadResourceString(dataElement, "partialData", context.getMessageGroup(), data::setPartialData);
            loadResourceString(dataElement, "withName", context.getMessageGroup(), data::setWithName);
            loadResourceString(dataElement, "withoutName", context.getMessageGroup(), data::setWithoutName);
            loadSeparator(dataElement, data::setSeparator);

            label.setData(data);
        }
    }

    protected void loadAbstractAriaSeriesLabel(Aria.Label.Series.AbstractLabel<?> seriesLabel, Element element) {
        loadResourceString(element, "prefix", context.getMessageGroup(), seriesLabel::setPrefix);
        loadResourceString(element, "withName", context.getMessageGroup(), seriesLabel::setWithName);
        loadResourceString(element, "withoutName", context.getMessageGroup(), seriesLabel::setWithoutName);
    }

    protected void loadSeparator(Element element, Consumer<Separator> setter) {
        Element separatorElement = element.element("separator");

        if (separatorElement == null) {
            return;
        }

        Separator separator = new Separator();

        loadString(separatorElement, "middle", separator::setMiddle);
        loadString(separatorElement, "end", separator::setEnd);

        setter.accept(separator);
    }

    protected void loadToolboxEmphasis(Element element, Consumer<Emphasis> setter) {
        Element emphasisElement = element.element("emphasis");

        if (emphasisElement == null) {
            return;
        }

        Emphasis emphasis = new Emphasis();

        Element iconStyleElement = emphasisElement.element("iconStyle");
        if (iconStyleElement != null) {
            Emphasis.IconStyle iconStyle = new Emphasis.IconStyle();

            loadAbstractBorderedTextStyle(iconStyle, iconStyleElement);

            loadEnum(iconStyleElement, Emphasis.IconStyle.TextPosition.class, "textPosition", iconStyle::setTextPosition);
            chartLoaderSupport().loadColor(iconStyleElement, "textFill", iconStyle::setTextFill);
            loadEnum(iconStyleElement, Align.class, "textAlign", iconStyle::setTextAlign);
            chartLoaderSupport().loadColor(iconStyleElement, "textBackgroundColor", iconStyle::setTextBackgroundColor);
            loadInteger(iconStyleElement, "textBorderRadius", iconStyle::setTextBorderRadius);

            chartLoaderSupport().loadPadding(iconStyle, iconStyleElement);

            emphasis.setIconStyle(iconStyle);
        }

        setter.accept(emphasis);
    }

    protected void loadToolboxFeature(Toolbox toolbox, Element element) {
        ToolboxFeature feature = switch (element.getName()) {
            case "saveAsImage" -> {
                SaveAsImageFeature saveAsImageFeature = new SaveAsImageFeature();
                loadSaveAsImageFeature(saveAsImageFeature, element);
                yield saveAsImageFeature;
            }
            case "restore" -> {
                RestoreFeature restoreFeature = new RestoreFeature();
                loadRestoreFeature(restoreFeature, element);
                yield restoreFeature;
            }
            case "dataZoom" -> {
                DataZoomFeature dataZoomFeature = new DataZoomFeature();
                loadDataZoomFeature(dataZoomFeature, element);
                yield dataZoomFeature;
            }
            case "magicType" -> {
                MagicTypeFeature magicTypeFeature = new MagicTypeFeature();
                loadMagicTypeFeature(magicTypeFeature, element);
                yield magicTypeFeature;
            }
            case "brush" -> {
                BrushFeature brushFeature = new BrushFeature();
                loadBrushFeature(brushFeature, element);
                yield brushFeature;
            }
            default -> throw new GuiDevelopmentException(
                    String.format("Unexpected %s feature with '%s' name", Toolbox.class.getSimpleName(),
                            element.getName()),
                    context
            );
        };

        toolbox.addFeature(feature);
    }

    protected void loadSaveAsImageFeature(SaveAsImageFeature feature, Element element) {
        loadAbstractToolboxFeature(feature, element);

        loadEnum(element, SaveAsImageFeature.SaveType.class, "type", feature::setType);
        loadString(element, "name", feature::setName);
        chartLoaderSupport().loadColor(element, "backgroundColor", feature::setBackgroundColor);
        chartLoaderSupport().loadColor(element, "connectedBackgroundColor", feature::setConnectedBackgroundColor);
        chartLoaderSupport().loadStringList(element, "excludeComponents", feature::setExcludeComponents);
        loadResourceString(element, "title", context.getMessageGroup(), feature::setTitle);
        loadString(element, "icon", feature::setIcon);
        loadInteger(element, "pixelRatio", feature::setPixelRatio);
    }

    protected void loadRestoreFeature(RestoreFeature feature, Element element) {
        loadAbstractToolboxFeature(feature, element);

        loadResourceString(element, "title", context.getMessageGroup(), feature::setTitle);
        loadString(element, "icon", feature::setIcon);
    }

    protected void loadDataZoomFeature(DataZoomFeature feature, Element element) {
        loadAbstractToolboxFeature(feature, element);

        loadEnum(element, AbstractDataZoom.FilterMode.class, "filterMode", feature::setFilterMode);
        chartLoaderSupport().loadIntegerList(element, "xAxisIndexes", feature::setXAxisIndexes);
        chartLoaderSupport().loadIntegerList(element, "yAxisIndexes", feature::setYAxisIndexes);
        loadItemStyle(element, "brushStyle", feature::setBrushStyle);

        Element titleElement = element.element("title");
        if (titleElement != null) {
            DataZoomFeature.Title title = new DataZoomFeature.Title();

            loadResourceString(titleElement, "zoom", context.getMessageGroup(), title::setZoom);
            loadResourceString(titleElement, "back", context.getMessageGroup(), title::setBack);

            feature.setTitle(title);
        }

        Element iconElement = element.element("icon");
        if (iconElement != null) {
            DataZoomFeature.Icon icon = new DataZoomFeature.Icon();

            loadString(iconElement, "zoom", icon::setZoom);
            loadString(iconElement, "back", icon::setBack);

            feature.setIcon(icon);
        }
    }

    protected void loadMagicTypeFeature(MagicTypeFeature feature, Element element) {
        loadAbstractToolboxFeature(feature, element);

        chartLoaderSupport().loadEnumList(element, MagicTypeFeature.MagicType.class, "types", feature::setTypes);

        Element titleElement = element.element("title");
        if (titleElement != null) {
            MagicTypeFeature.Title title = new MagicTypeFeature.Title();

            loadResourceString(titleElement, "bar", context.getMessageGroup(), title::setBar);
            loadResourceString(titleElement, "line", context.getMessageGroup(), title::setLine);
            loadResourceString(titleElement, "stack", context.getMessageGroup(), title::setStack);
            loadResourceString(titleElement, "tiled", context.getMessageGroup(), title::setTiled);

            feature.setTitle(title);
        }

        Element iconElement = element.element("icon");
        if (iconElement != null) {
            MagicTypeFeature.Icon icon = new MagicTypeFeature.Icon();

            loadString(iconElement, "line", icon::setLine);
            loadString(iconElement, "bar", icon::setBar);
            loadString(iconElement, "stack", icon::setBar);

            feature.setIcon(icon);
        }
    }

    protected void loadBrushFeature(BrushFeature feature, Element element) {
        chartLoaderSupport().loadEnumList(element, BrushFeature.BrushType.class, "types", feature::setTypes);

        Element titleElement = element.element("title");
        if (titleElement != null) {
            BrushFeature.Title title = new BrushFeature.Title();

            loadResourceString(titleElement, "rect", context.getMessageGroup(), title::setRect);
            loadResourceString(titleElement, "polygon", context.getMessageGroup(), title::setPolygon);
            loadResourceString(titleElement, "lineX", context.getMessageGroup(), title::setLineX);
            loadResourceString(titleElement, "lineY", context.getMessageGroup(), title::setLineY);
            loadResourceString(titleElement, "keep", context.getMessageGroup(), title::setKeep);
            loadResourceString(titleElement, "clear", context.getMessageGroup(), title::setClear);

            feature.setTitle(title);
        }

        Element iconElement = element.element("icon");
        if (iconElement != null) {
            BrushFeature.Icon icon = new BrushFeature.Icon();

            loadString(iconElement, "rect", icon::setRect);
            loadString(iconElement, "polygon", icon::setPolygon);
            loadString(iconElement, "lineX", icon::setLineX);
            loadString(iconElement, "lineY", icon::setLineY);
            loadString(iconElement, "keep", icon::setKeep);
            loadString(iconElement, "clear", icon::setClear);

            feature.setIcon(icon);
        }
    }

    protected void loadAbstractToolboxFeature(AbstractFeature<?> feature, Element element) {
        loadBoolean(element, "show", feature::setShow);
        loadItemStyle(element, "iconStyle", feature::setIconStyle);
        loadToolboxEmphasis(element, feature::setEmphasis);
    }

    protected void loadPiece(PiecewiseVisualMap piecewiseVisualMap, Element element) {
        PiecewiseVisualMap.Piece piece = new PiecewiseVisualMap.Piece();

        loadDouble(element, "min", piece::setMin);
        loadDouble(element, "max", piece::setMax);
        loadResourceString(element, "label", context.getMessageGroup(), piece::setLabel);
        loadDouble(element, "value", piece::setValue);
        chartLoaderSupport().loadColor(element, "color", piece::setColor);

        piecewiseVisualMap.addPiece(piece);
    }

    protected void loadAbstractVisualMap(AbstractVisualMap<?> visualMap, Element element) {
        loadString(element, "id", visualMap::setId);
        loadDouble(element, "min", visualMap::setMin);
        loadDouble(element, "max", visualMap::setMax);
        loadBoolean(element, "inverse", visualMap::setInverse);
        loadDouble(element, "precision", visualMap::setPrecision);
        loadDouble(element, "itemWidth", visualMap::setItemWidth);
        loadDouble(element, "itemHeight", visualMap::setItemHeight);
        loadEnum(element, AbstractVisualMap.MapAlign.class, "align", visualMap::setAlign);
        chartLoaderSupport().loadStringPair(element, "text", visualMap::setText,
                String.format("The %s text must contain both the start and end values",
                        AbstractVisualMap.class.getSimpleName())
        );
        loadDouble(element, "textGap", visualMap::setTextGap);
        loadBoolean(element, "show", visualMap::setShow);
        loadString(element, "dimension", visualMap::setDimension);
        chartLoaderSupport().loadIntegerList(element, "seriesIndex", visualMap::setSeriesIndex);
        loadBoolean(element, "hoverLink", visualMap::setHoverLink);
        loadVisualEffect(element, "inRange", visualMap::setInRange);
        loadVisualEffect(element, "outOfRange", visualMap::setOutOfRange);
        loadDouble(element, "zLevel", visualMap::setZLevel);
        loadDouble(element, "z", visualMap::setZ);
        chartLoaderSupport().loadColor(element, "backgroundColor", visualMap::setBackgroundColor);
        chartLoaderSupport().loadColor(element, "borderColor", visualMap::setBorderColor);
        loadDouble(element, "borderWidth", visualMap::setBorderWidth);
        loadTextStyle(element, "textStyle", visualMap::setTextStyle);
        loadString(element, "formatter", visualMap::setFormatter);
        chartLoaderSupport().loadJsFunction(element, "formatterFunction", visualMap::setFormatterFunction);
        loadEnum(element, Orientation.class, "orientation", visualMap::setOrientation);

        Element controllerElement = element.element("controller");
        if (controllerElement != null) {
            AbstractVisualMap.VisualMapController visualMapController = new AbstractVisualMap.VisualMapController();

            loadVisualEffect(controllerElement, "inRange", visualMapController::setInRange);
            loadVisualEffect(controllerElement, "outOfRange", visualMapController::setOutOfRange);

            visualMap.setController(visualMapController);
        }

        chartLoaderSupport().loadPadding(visualMap, element);
        chartLoaderSupport().loadPosition(visualMap, element);
    }

    protected void loadVisualEffect(Element element, String attributeName, Consumer<VisualEffect> setter) {
        Element visualEffectElement = element.element(attributeName);

        if (visualEffectElement == null) {
            return;
        }

        VisualEffect visualEffect = new VisualEffect();

        chartLoaderSupport().loadStringList(visualEffectElement, "symbol", visualEffect::setSymbol);
        chartLoaderSupport().loadIntegerPair(visualEffectElement, "symbolSize", visualEffect::setSymbolSize,
                String.format("The %s can only have minSymbolSize and maxSymbolSize values",
                        VisualEffect.class.getSimpleName()
                ));
        chartLoaderSupport().loadColorList(visualEffectElement, "color", visualEffect::setColor);
        chartLoaderSupport().loadDoublePair(visualEffectElement, "colorAlpha", visualEffect::setColorAlpha,
                String.format("The %s can only have minColorAlpha and maxColorAlpha values",
                        VisualEffect.class.getSimpleName()
                ));
        chartLoaderSupport().loadDoublePair(visualEffectElement, "opacity", visualEffect::setOpacity,
                String.format("The %s can only have minOpacity and maxOpacity values",
                        VisualEffect.class.getSimpleName()
                ));
        chartLoaderSupport().loadDoublePair(visualEffectElement, "colorLightness", visualEffect::setColorLightness,
                String.format("The %s can only have minColorLightness and maxColorLightness values",
                        VisualEffect.class.getSimpleName()
                ));
        chartLoaderSupport().loadDoublePair(visualEffectElement, "colorSaturation", visualEffect::setColorSaturation,
                String.format("The %s can only have minColorSaturation and maxColorSaturation values",
                        VisualEffect.class.getSimpleName()
                ));
        chartLoaderSupport().loadDoublePair(visualEffectElement, "colorHue", visualEffect::setColorHue,
                String.format("The %s can only have minColorHue and maxColorHue values",
                        VisualEffect.class.getSimpleName()
                ));

        setter.accept(visualEffect);
    }

    protected void loadDataBackground(SliderDataZoom.DataBackground dataBackground, Element element) {
        loadLineStyle(element, "lineStyle", dataBackground::setLineStyle);
        loadAreaStyle(element, dataBackground::setAreaStyle);
    }

    protected void loadAbstractDataZoom(AbstractDataZoom<?> dataZoom, Element element) {
        loadString(element, "id", dataZoom::setId);
        chartLoaderSupport().loadIntegerList(element, "xAxisIndexes", dataZoom::setXAxisIndexes);
        chartLoaderSupport().loadIntegerList(element, "yAxisIndexes", dataZoom::setYAxisIndexes);
        chartLoaderSupport().loadIntegerList(element, "radiusAxisIndexes", dataZoom::setRadiusAxisIndexes);
        chartLoaderSupport().loadIntegerList(element, "angleAxisIndexes", dataZoom::setAngleAxisIndexes);
        loadEnum(element, AbstractDataZoom.FilterMode.class, "filterMode", dataZoom::setFilterMode);
        loadDouble(element, "start", dataZoom::setStart);
        loadDouble(element, "end", dataZoom::setEnd);
        loadString(element, "startValue", dataZoom::setStartValue);
        loadString(element, "endValue", dataZoom::setEndValue);
        loadDouble(element, "minSpan", dataZoom::setMinSpan);
        loadDouble(element, "maxSpan", dataZoom::setMaxSpan);
        loadString(element, "minValueSpan", dataZoom::setMinValueSpan);
        loadString(element, "maxValueSpan", dataZoom::setMaxValueSpan);
        loadEnum(element, Orientation.class, "orientation", dataZoom::setOrientation);
        loadBoolean(element, "zoomLock", dataZoom::setZoomLock);
        loadInteger(element, "throttle", dataZoom::setThrottle);
        chartLoaderSupport().loadStringPair(element, "rangeMode",
                (startRangeMode, endRangeMode) -> dataZoom.setRangeMode(
                        AbstractDataZoom.RangeMode.valueOf(startRangeMode),
                        AbstractDataZoom.RangeMode.valueOf(endRangeMode)
                ),
                String.format("%s supports only start and end values of rangeMode",
                        AbstractDataZoom.class.getSimpleName())
        );
    }

    protected void loadIndicator(Radar radar, Element element) {
        Radar.Indicator indicator = new Radar.Indicator();

        loadResourceString(element, "name", context.getMessageGroup(), indicator::setName);
        loadInteger(element, "max", indicator::setMax);
        loadInteger(element, "min", indicator::setMin);
        chartLoaderSupport().loadColor(element, "color", indicator::setColor);

        radar.addIndicator(indicator);
    }

    protected <T extends AbstractCartesianAxis<?>> void loadCartesianAxes(Element element, Supplier<T> constructor,
                                                                          String elementsName, String elementName,
                                                                          Consumer<T> setter) {
        Element axesElement = element.element(elementsName);

        if (axesElement != null) {
            List<Element> axisElements = axesElement.elements(elementName);

            if (!axisElements.isEmpty()) {
                for (Element axisElement : axisElements) {
                    T axis = constructor.get();
                    loadCartesianAxis(axis, axisElement);
                    setter.accept(axis);
                }
            }
        }
    }

    protected void loadCartesianAxis(AbstractCartesianAxis<?> axis, Element element) {
        loadAbstractAxis(axis, element);
        chartLoaderSupport().loadAxisNameAttributes(axis, element, this::loadAbstractRichText);

        loadBoolean(element, "show", axis::setShow);
        loadInteger(element, "gridIndex", axis::setGridIndex);
        loadBoolean(element, "alignTicks", axis::setAlignTicks);
        loadEnum(element, AbstractCartesianAxis.Position.class, "position", axis::setPosition);
        loadInteger(element, "offset", axis::setOffset);
    }

    protected void loadPolarAxis(AbstractPolarAxis<?> axis, Element element) {
        loadAbstractAxis(axis, element);

        loadInteger(element, "polarIndex", axis::setPolarIndex);
    }

    protected void loadAbstractAxis(AbstractAxis<?> axis, Element element) {
        loadString(element, "id", axis::setId);
        loadEnum(element, AxisType.class, "type", axis::setType);
        loadBoolean(element, "categoryBoundaryGap", axis::setBoundaryGap);
        loadString(element, "min", axis::setMin);
        chartLoaderSupport().loadJsFunction(element, "minFunction", axis::setMinFunction);
        loadString(element, "max", axis::setMax);
        chartLoaderSupport().loadJsFunction(element, "maxFunction", axis::setMaxFunction);
        loadBoolean(element, "scale", axis::setScale);
        loadInteger(element, "splitNumber", axis::setSplitNumber);
        loadInteger(element, "minInterval", axis::setMinInterval);
        loadInteger(element, "maxInterval", axis::setMaxInterval);
        loadInteger(element, "interval", axis::setInterval);
        loadInteger(element, "logBase", axis::setLogBase);
        loadBoolean(element, "silent", axis::setSilent);
        loadBoolean(element, "triggerEvent", axis::setTriggerEvent);
        loadBoolean(element, "animation", axis::setAnimation);
        loadInteger(element, "animationThreshold", axis::setAnimationThreshold);
        loadInteger(element, "animationDuration", axis::setAnimationDuration);
        chartLoaderSupport().loadJsFunction(element, "animationDurationFunction", axis::setAnimationDurationFunction);
        loadString(element, "animationEasing", axis::setAnimationEasing);
        loadInteger(element, "animationDelay", axis::setAnimationDelay);
        chartLoaderSupport().loadJsFunction(element, "animationDelayFunction", axis::setAnimationDelayFunction);
        loadInteger(element, "animationDurationUpdate", axis::setAnimationDurationUpdate);
        chartLoaderSupport().loadJsFunction(element, "animationDurationUpdateFunction", axis::setAnimationDurationUpdateFunction);
        loadString(element, "animationEasingUpdate", axis::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", axis::setAnimationDelayUpdate);
        chartLoaderSupport().loadJsFunction(element, "animationDelayUpdateFunction", axis::setAnimationDelayUpdateFunction);
        loadInteger(element, "zLevel", axis::setZLevel);
        loadInteger(element, "z", axis::setZ);
        chartLoaderSupport().loadStringPair(element, "nonCategoryBoundaryGap", axis::setBoundaryGap,
                String.format("%s supports only max and min values of non-category boundary gap",
                        AbstractAxis.class.getSimpleName()));

        loadAxisLine(element, axis::setAxisLine);
        loadAxisTick(element, axis::setAxisTick);
        loadAxisLabel(element, axis::setAxisLabel);
        loadSplitLine(element, axis::setSplitLine);
        loadSplitArea(element, axis::setSplitArea);

        Element minorTickElement = element.element("minorTick");
        if (minorTickElement != null) {
            MinorTick minorTick = new MinorTick();

            loadBoolean(minorTickElement, "show", minorTick::setShow);
            loadInteger(minorTickElement, "splitNumber", minorTick::setSplitNumber);
            loadInteger(minorTickElement, "length", minorTick::setLength);
            loadLineStyle(minorTickElement, "lineStyle", minorTick::setLineStyle);

            axis.setMinorTick(minorTick);
        }

        Element minorSplitLineElement = element.element("minorSplitLine");
        if (minorSplitLineElement != null) {
            MinorSplitLine minorSplitLine = new MinorSplitLine();

            loadBoolean(minorSplitLineElement, "show", minorSplitLine::setShow);
            loadLineStyle(minorSplitLineElement, "lineStyle", minorSplitLine::setLineStyle);

            axis.setMinorSplitLine(minorSplitLine);
        }

        Element axisPointerElement = element.element("axisPointer");
        if (axisPointerElement != null) {
            AbstractAxis.AxisPointer axisPointer = new AbstractAxis.AxisPointer();

            loadAbstractAxisPointer(axisPointer, axisPointerElement);

            axis.setAxisPointer(axisPointer);
        }
    }

    protected void loadAxisLine(Element element, Consumer<AxisLine> setter) {
        Element axisLineElement = element.element("axisLine");

        if (axisLineElement == null) {
            return;
        }

        AxisLine axisLine = new AxisLine();

        loadBoolean(axisLineElement, "show", axisLine::setShow);
        loadBoolean(axisLineElement, "onZero", axisLine::setOnZero);
        loadInteger(axisLineElement, "onZeroAxisIndex", axisLine::setOnZeroAxisIndex);
        chartLoaderSupport().loadStringPair(axisLineElement, "symbols", axisLine::setSymbols,
                String.format("%s supports only start and end symbols",
                        AxisLine.class.getSimpleName()));
        chartLoaderSupport().loadIntegerPair(axisLineElement, "symbolsSize", axisLine::setSymbolsSize,
                String.format("%s supports only start and end symbols size",
                        AxisLine.class.getSimpleName()));
        chartLoaderSupport().loadIntegerPair(axisLineElement, "symbolsOffset", axisLine::setSymbolsOffset,
                String.format("%s supports only start and end symbols offset",
                        AxisLine.class.getSimpleName()));
        loadLineStyle(axisLineElement, "lineStyle", axisLine::setLineStyle);

        setter.accept(axisLine);
    }

    protected void loadAxisTick(Element element, Consumer<AxisTick> setter) {
        Element axisTickElement = element.element("axisTick");
        if (axisTickElement == null) {
            return;
        }

        AxisTick axisTick = new AxisTick();

        loadBoolean(axisTickElement, "show", axisTick::setShow);
        loadBoolean(axisTickElement, "alignWithLabel", axisTick::setAlignWithLabel);
        loadInteger(axisTickElement, "interval", axisTick::setInterval);
        chartLoaderSupport().loadJsFunction(axisTickElement, "intervalFunction", axisTick::setIntervalFunction);
        loadBoolean(axisTickElement, "inside", axisTick::setInside);
        loadInteger(axisTickElement, "length", axisTick::setLength);
        loadLineStyle(axisTickElement, "lineStyle", axisTick::setLineStyle);

        setter.accept(axisTick);
    }

    protected void loadAxisLabel(Element element, Consumer<AxisLabel> setter) {
        Element axisLabelElement = element.element("axisLabel");
        if (axisLabelElement == null) {
            return;
        }

        AxisLabel axisLabel = new AxisLabel();

        loadAbstractRichText(axisLabel, axisLabelElement);

        loadBoolean(axisLabelElement, "show", axisLabel::setShow);
        loadInteger(axisLabelElement, "interval", axisLabel::setInterval);
        chartLoaderSupport().loadJsFunction(axisLabelElement, "intervalFunction", axisLabel::setIntervalFunction);
        loadBoolean(axisLabelElement, "inside", axisLabel::setInside);
        loadInteger(axisLabelElement, "rotate", axisLabel::setRotate);
        loadInteger(axisLabelElement, "margin", axisLabel::setMargin);
        loadString(axisLabelElement, "formatter", axisLabel::setFormatter);
        chartLoaderSupport().loadJsFunction(axisLabelElement, "formatterFunction", axisLabel::setFormatterFunction);
        chartLoaderSupport().loadJsFunction(axisLabelElement, "colorFunction", axisLabel::setColorFunction);
        loadBoolean(axisLabelElement, "showMinLabel", axisLabel::setShowMinLabel);
        loadBoolean(axisLabelElement, "showMaxLabel", axisLabel::setShowMaxLabel);
        loadBoolean(axisLabelElement, "hideOverlap", axisLabel::setHideOverlap);
        chartLoaderSupport().loadColor(axisLabelElement, "backgroundColor", axisLabel::setBackgroundColor);
        loadString(axisLabelElement, "borderType", axisLabel::setBorderType);
        loadInteger(axisLabelElement, "borderDashOffset", axisLabel::setBorderDashOffset);

        chartLoaderSupport().loadAlign(axisLabel, axisLabelElement);
        chartLoaderSupport().loadShadow(axisLabel, axisLabelElement);
        chartLoaderSupport().loadPadding(axisLabel, axisLabelElement);
        chartLoaderSupport().loadBorder(axisLabel, axisLabelElement);

        setter.accept(axisLabel);
    }

    protected void loadSplitLine(Element element, Consumer<SplitLine> setter) {
        Element splitLineElement = element.element("splitLine");
        if (splitLineElement == null) {
            return;
        }

        SplitLine splitLine = new SplitLine();

        loadBoolean(splitLineElement, "show", splitLine::setShow);
        loadInteger(splitLineElement, "interval", splitLine::setInterval);
        chartLoaderSupport().loadJsFunction(splitLineElement, "intervalFunction", splitLine::setIntervalFunction);
        loadLineStyle(splitLineElement, "lineStyle", splitLine::setLineStyle);

        setter.accept(splitLine);
    }

    protected void loadSplitArea(Element element, Consumer<SplitArea> setter) {
        Element splitAreaElement = element.element("splitArea");
        if (splitAreaElement == null) {
            return;
        }

        SplitArea splitArea = new SplitArea();

        loadInteger(splitAreaElement, "interval", splitArea::setInterval);
        chartLoaderSupport().loadJsFunction(splitAreaElement, "intervalFunction", splitArea::setIntervalFunction);
        loadBoolean(splitAreaElement, "show", splitArea::setShow);
        loadAreaStyle(splitAreaElement, splitArea::setAreaStyle);

        setter.accept(splitArea);
    }

    protected void loadTooltip(Element element, Consumer<Tooltip> setter) {
        Element tooltipElement = element.element("tooltip");
        if (tooltipElement == null) {
            return;
        }

        Tooltip tooltip = new Tooltip();

        loadAbstractTooltip(tooltip, tooltipElement);

        loadBoolean(tooltipElement, "showContent", tooltip::setShowContent);
        loadBoolean(tooltipElement, "alwaysShowContent", tooltip::setAlwaysShowContent);
        loadEnum(tooltipElement, TriggerOnMode.class, "triggerOn", tooltip::setTriggerOn);
        loadInteger(tooltipElement, "showDelay", tooltip::setShowDelay);
        loadInteger(tooltipElement, "hideDelay", tooltip::setHideDelay);
        loadBoolean(tooltipElement, "enterable", tooltip::setEnterable);
        loadEnum(tooltipElement, Tooltip.RenderMode.class, "renderMode", tooltip::setRenderMode);
        loadBoolean(tooltipElement, "confine", tooltip::setConfine);
        loadBoolean(tooltipElement, "appendToBody", tooltip::setAppendToBody);
        loadString(tooltipElement, "className", tooltip::setClassName);
        loadDouble(tooltipElement, "transitionDuration", tooltip::setTransitionDuration);
        loadEnum(tooltipElement, Tooltip.OrderType.class, "order", tooltip::setOrder);

        setter.accept(tooltip);
    }

    protected void loadAbstractTooltip(AbstractTooltip<?> tooltip, Element element) {
        loadBoolean(element, "show", tooltip::setShow);
        loadEnum(element, AbstractTooltip.Trigger.class, "trigger", tooltip::setTrigger);
        loadString(element, "formatter", tooltip::setFormatter);
        chartLoaderSupport().loadJsFunction(element, "formatterFunction", tooltip::setFormatterFunction);
        loadString(element, "valueFormatter", tooltip::setValueFormatter);
        chartLoaderSupport().loadJsFunction(element, "valueFormatterFunction", tooltip::setValueFormatterFunction);
        chartLoaderSupport().loadColor(element, "backgroundColor", tooltip::setBackgroundColor);
        chartLoaderSupport().loadColor(element, "borderColor", tooltip::setBorderColor);
        loadInteger(element, "borderWidth", tooltip::setBorderWidth);
        loadTextStyle(element, "textStyle", tooltip::setTextStyle);
        loadString(element, "extraCssText", tooltip::setExtraCssText);
        loadEnum(element, AbstractTooltip.Position.ItemTriggerPosition.class, "position", tooltip::setPosition);
        chartLoaderSupport().loadStringPair(element, "positionCoordinates", tooltip::setPosition,
                String.format("%s supports only horizontal and vertical position coordinates",
                        AbstractTooltip.class.getSimpleName()));

        chartLoaderSupport().loadPadding(tooltip, element);

        Element axisPointerElement = element.element("axisPointer");
        if (axisPointerElement != null) {
            AbstractTooltip.AxisPointer axisPointer = new AbstractTooltip.AxisPointer();

            loadTooltipAxisPointer(axisPointer, axisPointerElement);

            tooltip.setAxisPointer(axisPointer);
        }
    }

    protected void loadTooltipAxisPointer(AbstractTooltip.AxisPointer axisPointer, Element element) {
        loadEnum(element, AbstractTooltip.AxisPointer.IndicatorType.class, "type", axisPointer::setType);
        loadEnum(element, AbstractTooltip.AxisPointer.AxisType.class, "axis", axisPointer::setAxis);
        loadBoolean(element, "snap", axisPointer::setSnap);
        loadInteger(element, "z", axisPointer::setZ);
        loadLineStyle(element, "lineStyle", axisPointer::setLineStyle);
        loadLineStyle(element, "crossStyle", axisPointer::setCrossStyle);
        loadBoolean(element, "animation", axisPointer::setAnimation);
        loadInteger(element, "animationThreshold", axisPointer::setAnimationThreshold);
        loadInteger(element, "animationDuration", axisPointer::setAnimationDuration);
        chartLoaderSupport().loadJsFunction(element, "animationDurationFunction", axisPointer::setAnimationDurationFunction);
        loadString(element, "animationEasing", axisPointer::setAnimationEasing);
        loadInteger(element, "animationDelay", axisPointer::setAnimationDelay);
        chartLoaderSupport().loadJsFunction(element, "animationDelayFunction", axisPointer::setAnimationDelayFunction);
        loadInteger(element, "animationDurationUpdate", axisPointer::setAnimationDurationUpdate);
        chartLoaderSupport().loadJsFunction(element, "animationDurationUpdateFunction", axisPointer::setAnimationDurationUpdateFunction);
        loadString(element, "animationEasingUpdate", axisPointer::setAnimationEasingUpdate);
        loadInteger(element, "animationDelayUpdate", axisPointer::setAnimationDelayUpdate);
        chartLoaderSupport().loadJsFunction(element, "animationDelayUpdateFunction", axisPointer::setAnimationDelayUpdateFunction);

        loadShadowStyle(element, axisPointer::setShadowStyle);
        loadLabel(element, axisPointer::setLabel);
    }

    protected void loadLabel(Element element, Consumer<Label> setter) {
        Element labelElement = element.element("label");
        if (labelElement == null) {
            return;
        }

        Label label = new Label();

        loadAbstractText(label, labelElement);

        loadBoolean(labelElement, "show", label::setShow);
        loadInteger(labelElement, "precision", label::setPrecision);
        loadString(labelElement, "formatter", label::setFormatter);
        chartLoaderSupport().loadJsFunction(labelElement, "formatterFunction", label::setFormatterFunction);
        loadInteger(labelElement, "margin", label::setMargin);
        chartLoaderSupport().loadColor(labelElement, "backgroundColor", label::setBackgroundColor);
        chartLoaderSupport().loadColor(labelElement, "borderColor", label::setBorderColor);
        loadInteger(labelElement, "borderWidth", label::setBorderWidth);

        chartLoaderSupport().loadShadow(label, labelElement);
        chartLoaderSupport().loadPadding(label, labelElement);

        setter.accept(label);
    }

    protected void loadShadowStyle(Element element, Consumer<ShadowStyle> setter) {
        Element shadowStyleElement = element.element("shadowStyle");
        if (shadowStyleElement == null) {
            return;
        }

        ShadowStyle shadowStyle = new ShadowStyle();

        chartLoaderSupport().loadColor(shadowStyleElement, "color", shadowStyle::setColor);
        loadDouble(shadowStyleElement, "opacity", shadowStyle::setOpacity);

        chartLoaderSupport().loadShadow(shadowStyle, shadowStyleElement);

        setter.accept(shadowStyle);
    }

    protected void loadItemStyle(Element element, String attributeName, Consumer<ItemStyle> setter) {
        Element itemStyleElement = element.element(attributeName);

        if (itemStyleElement == null) {
            return;
        }

        ItemStyle itemStyle = new ItemStyle();
        loadAbstractBorderedTextStyle(itemStyle, itemStyleElement);
        setter.accept(itemStyle);
    }

    protected void loadLineStyle(Element element, String attributeName, Consumer<LineStyle> setter) {
        Element lineStyleElement = element.element(attributeName);

        if (lineStyleElement == null) {
            return;
        }

        LineStyle lineStyle = new LineStyle();

        chartLoaderSupport().loadColor(lineStyleElement, "color", lineStyle::setColor);
        loadInteger(lineStyleElement, "width", lineStyle::setWidth);
        loadString(lineStyleElement, "type", lineStyle::setType);
        loadInteger(lineStyleElement, "dashOffset", lineStyle::setDashOffset);
        loadDouble(lineStyleElement, "opacity", lineStyle::setOpacity);

        chartLoaderSupport().loadShadow(lineStyle, lineStyleElement);
        chartLoaderSupport().loadLineStyle(lineStyle, lineStyleElement);

        setter.accept(lineStyle);
    }

    protected void loadAbstractEnhancedLabel(AbstractEnhancedLabel<?> abstractEnhancedLabel, Element element) {
        loadAbstractRichText(abstractEnhancedLabel, element);

        loadBoolean(element, "show", abstractEnhancedLabel::setShow);
        loadInteger(element, "distance", abstractEnhancedLabel::setDistance);
        loadInteger(element, "rotate", abstractEnhancedLabel::setRotate);
        chartLoaderSupport().loadColor(element, "backgroundColor", abstractEnhancedLabel::setBackgroundColor);
        loadString(element, "borderType", abstractEnhancedLabel::setBorderType);
        loadInteger(element, "borderDashOffset", abstractEnhancedLabel::setBorderDashOffset);
        chartLoaderSupport().loadIntegerPair(element, "offset", abstractEnhancedLabel::setOffset,
                String.format("%s supports only horizontal and vertical offset",
                        AbstractEnhancedLabel.class.getSimpleName()));

        chartLoaderSupport().loadShadow(abstractEnhancedLabel, element);
        chartLoaderSupport().loadBorder(abstractEnhancedLabel, element);
        chartLoaderSupport().loadAlign(abstractEnhancedLabel, element);
        chartLoaderSupport().loadPadding(abstractEnhancedLabel, element);
    }

    protected void loadAbstractBorderedTextStyle(AbstractBorderedTextStyle<?> abstractBorderedTextStyle,
                                                 Element element) {
        chartLoaderSupport().loadColor(element, "color", abstractBorderedTextStyle::setColor);
        chartLoaderSupport().loadColor(element, "borderColor", abstractBorderedTextStyle::setBorderColor);
        loadInteger(element, "borderWidth", abstractBorderedTextStyle::setBorderWidth);
        loadString(element, "borderType", abstractBorderedTextStyle::setBorderType);
        loadInteger(element, "borderDashOffset", abstractBorderedTextStyle::setBorderDashOffset);
        loadDouble(element, "opacity", abstractBorderedTextStyle::setOpacity);

        chartLoaderSupport().loadShadow(abstractBorderedTextStyle, element);
        chartLoaderSupport().loadLineStyle(abstractBorderedTextStyle, element);
    }

    protected void loadAbstractRichText(AbstractRichText<?> abstractRichText, Element element) {
        loadAbstractText(abstractRichText, element);

        Element richElement = element.element("rich");
        if (richElement == null) {
            return;
        }

        for (Element richStyleElement : richElement.elements("richStyle")) {
            String name = loadString(richStyleElement, "name")
                    .orElseThrow(
                            () -> new GuiDevelopmentException(
                                    String.format("'name' is required for %s", RichStyle.class.getCanonicalName()),
                                    context
                            )
                    );

            RichStyle richStyle = new RichStyle();
            loadRichStyle(richStyle, richStyleElement);
            abstractRichText.addRichStyle(name, richStyle);
        }
    }

    protected void loadAbstractText(AbstractText<?> abstractText, Element element) {
        loadEnum(element, FontStyle.class, "fontStyle", abstractText::setFontStyle);
        loadString(element, "fontWeight", abstractText::setFontWeight);
        loadString(element, "fontFamily", abstractText::setFontFamily);
        loadInteger(element, "fontSize", abstractText::setFontSize);
        loadInteger(element, "lineHeight", abstractText::setLineHeight);
        loadInteger(element, "width", abstractText::setWidth);
        loadInteger(element, "height", abstractText::setHeight);
        loadEnum(element, Overflow.class, "overflow", abstractText::setOverflow);
        loadString(element, "ellipsis", abstractText::setEllipsis);

        chartLoaderSupport().loadColor(element, "color", abstractText::setColor);
        chartLoaderSupport().loadTextAttributes(abstractText, element);
    }

    protected void loadRichStyle(RichStyle richStyle, Element element) {
        loadEnum(element, FontStyle.class, "fontStyle", richStyle::setFontStyle);
        loadString(element, "fontWeight", richStyle::setFontWeight);
        loadString(element, "fontFamily", richStyle::setFontFamily);
        loadInteger(element, "fontSize", richStyle::setFontSize);
        loadInteger(element, "lineHeight", richStyle::setLineHeight);
        loadString(element, "borderType", richStyle::setBorderType);
        loadInteger(element, "borderDashOffset", richStyle::setBorderDashOffset);
        loadInteger(element, "width", richStyle::setWidth);
        loadInteger(element, "height", richStyle::setHeight);

        chartLoaderSupport().loadColor(element, "color", richStyle::setColor);
        chartLoaderSupport().loadColor(element, "backgroundColor", richStyle::setBackgroundColor);

        chartLoaderSupport().loadPadding(richStyle, element);
        chartLoaderSupport().loadTextAttributes(richStyle, element);
        chartLoaderSupport().loadShadow(richStyle, element);
        chartLoaderSupport().loadBorder(richStyle, element);
        chartLoaderSupport().loadAlign(richStyle, element);
    }

    protected void loadAbstractAxisPointer(AbstractAxisPointer<?> axisPointer, Element element) {
        loadBoolean(element, "show", axisPointer::setShow);
        loadEnum(element, AbstractAxisPointer.IndicatorType.class, "type", axisPointer::setType);
        loadBoolean(element, "snap", axisPointer::setSnap);
        loadInteger(element, "z", axisPointer::setZ);
        loadLineStyle(element, "lineStyle", axisPointer::setLineStyle);
        loadBoolean(element, "triggerEmphasis", axisPointer::setTriggerEmphasis);
        loadBoolean(element, "triggerTooltip", axisPointer::setTriggerTooltip);
        loadInteger(element, "value", axisPointer::setValue);
        loadBoolean(element, "status", axisPointer::setStatus);

        Element handleElement = element.element("handle");
        if (handleElement != null) {
            AbstractAxisPointer.Handle handle = new AbstractAxisPointer.Handle();

            loadBoolean(handleElement, "show", handle::setShow);
            loadString(handleElement, "icon", handle::setIcon);
            chartLoaderSupport().loadIntegerPair(handleElement, "size", handle::setSize,
                    String.format("%s supports only width and height size",
                            AbstractAxisPointer.Handle.class.getSimpleName()));
            loadInteger(handleElement, "margin", handle::setMargin);
            loadInteger(handleElement, "throttle", handle::setThrottle);
            chartLoaderSupport().loadShadow(handle, handleElement);
            chartLoaderSupport().loadColor(handleElement, "color", handle::setColor);

            axisPointer.setHandle(handle);
        }

        loadLabel(element, axisPointer::setLabel);
        loadShadowStyle(element, axisPointer::setShadowStyle);
    }

    protected void loadAreaStyle(Element element, Consumer<AreaStyle> setter) {
        Element areaStyleElement = element.element("areaStyle");

        if (areaStyleElement == null) {
            return;
        }

        AreaStyle areaStyle = new AreaStyle();

        loadAbstractAreaStyle(areaStyle, areaStyleElement);
        chartLoaderSupport().loadColorPair(areaStyleElement, "colors", areaStyle::setColors,
                String.format("%s supports only two colors", AreaStyle.class.getSimpleName()));

        setter.accept(areaStyle);
    }

    protected void loadAbstractAreaStyle(AbstractAreaStyle<?> areaStyle, Element element) {
        loadDouble(element, "opacity", areaStyle::setOpacity);

        chartLoaderSupport().loadShadow(areaStyle, element);
    }

    protected ChartLoaderSupport chartLoaderSupport() {
        if (chartLoaderSupport == null) {
            chartLoaderSupport = applicationContext.getBean(ChartLoaderSupport.class, context);
        }

        return chartLoaderSupport;
    }
}
