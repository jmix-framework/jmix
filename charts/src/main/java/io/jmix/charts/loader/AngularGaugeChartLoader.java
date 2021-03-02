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


import io.jmix.charts.component.AngularGaugeChart;
import io.jmix.charts.model.*;
import io.jmix.charts.model.GaugeArrow;
import io.jmix.charts.model.GaugeAxis;
import io.jmix.charts.model.GaugeBand;
import io.jmix.charts.model.axis.UnitPosition;
import io.jmix.charts.model.JsFunction;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class AngularGaugeChartLoader extends AbstractChartLoader<AngularGaugeChart> {

    protected static final java.util.regex.Pattern GRADIENT_RATIO_PATTERN = java.util.regex.Pattern.compile("\\s*,\\s*");

    @Override
    public void createComponent() {
        resultComponent = factory.create(AngularGaugeChart.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadConfiguration(resultComponent, element);
    }

    @Override
    protected void loadConfiguration(AngularGaugeChart chart, Element element) {
        super.loadConfiguration(chart, element);

        loadArrows(chart, element);
        loadAxes(chart, element);

        Element facePatternElement = element.element("facePattern");
        if (facePatternElement != null) {
            chart.setFacePattern(loadPattern(facePatternElement));
        }

        String adjustSize = element.attributeValue("adjustSize");
        if (StringUtils.isNotEmpty(adjustSize)) {
            chart.setAdjustSize(Boolean.valueOf(adjustSize));
        }

        String clockWiseOnly = element.attributeValue("clockWiseOnly");
        if (StringUtils.isNotEmpty(clockWiseOnly)) {
            chart.setClockWiseOnly(Boolean.valueOf(clockWiseOnly));
        }

        String faceAlpha = element.attributeValue("faceAlpha");
        if (StringUtils.isNotEmpty(faceAlpha)) {
            chart.setFaceAlpha(Double.valueOf(faceAlpha));
        }

        String faceBorderAlpha = element.attributeValue("faceBorderAlpha");
        if (StringUtils.isNotEmpty(faceBorderAlpha)) {
            chart.setFaceBorderAlpha(Double.valueOf(faceBorderAlpha));
        }

        String faceBorderColor = element.attributeValue("faceBorderColor");
        if (StringUtils.isNotEmpty(faceBorderColor)) {
            chart.setFaceBorderColor(Color.valueOf(faceBorderColor));
        }

        String faceBorderWidth = element.attributeValue("faceBorderWidth");
        if (StringUtils.isNotEmpty(faceBorderWidth)) {
            chart.setFaceBorderWidth(Integer.valueOf(faceBorderWidth));
        }

        String faceColor = element.attributeValue("faceColor");
        if (StringUtils.isNotEmpty(faceColor)) {
            chart.setFaceColor(Color.valueOf(faceColor));
        }

        String gaugeX = element.attributeValue("gaugeX");
        if (StringUtils.isNotEmpty(gaugeX)) {
            chart.setGaugeX(gaugeX);
        }

        String gaugeY = element.attributeValue("gaugeY");
        if (StringUtils.isNotEmpty(gaugeY)) {
            chart.setGaugeY(gaugeY);
        }

        loadMargins(chart, element);

        String minRadius = element.attributeValue("minRadius");
        if (StringUtils.isNotEmpty(minRadius)) {
            chart.setMinRadius(Integer.valueOf(minRadius));
        }

        loadStartEffect(chart, element);
    }

    protected void loadAxes(AngularGaugeChart chart, Element element) {
        Element axesListElement = element.element("axes");
        if (axesListElement != null) {
            for (Object axisItem : axesListElement.elements("axis")) {
                Element axisElement = (Element) axisItem;

                GaugeAxis axis = new GaugeAxis();

                loadGaugeBands(axis, axisElement);

                String labelFunction = axesListElement.elementText("labelFunction");
                if (StringUtils.isNotBlank(labelFunction)) {
                    axis.setLabelFunction(new JsFunction(labelFunction));
                }

                String axisAlpha = axisElement.attributeValue("axisAlpha");
                if (StringUtils.isNotEmpty(axisAlpha)) {
                    axis.setAxisAlpha(Double.valueOf(axisAlpha));
                }

                String axisColor = axisElement.attributeValue("axisColor");
                if (StringUtils.isNotEmpty(axisColor)) {
                    axis.setAxisColor(Color.valueOf(axisColor));
                }

                String axisThickness = axisElement.attributeValue("axisThickness");
                if (StringUtils.isNotEmpty(axisThickness)) {
                    axis.setAxisThickness(Integer.valueOf(axisThickness));
                }

                String bandAlpha = axisElement.attributeValue("bandAlpha");
                if (StringUtils.isNotEmpty(bandAlpha)) {
                    axis.setBandAlpha(Double.valueOf(bandAlpha));
                }

                String bandGradientRatio = axisElement.attributeValue("bandGradientRatio");
                if (StringUtils.isNotEmpty(bandGradientRatio)) {
                    List<Float> bandGradientRatioList = new ArrayList<>();
                    for (String s : GRADIENT_RATIO_PATTERN.split(bandGradientRatio))
                        bandGradientRatioList.add(Float.valueOf(s));
                    axis.setBandGradientRatio(bandGradientRatioList);
                }

                String bandOutlineAlpha = axisElement.attributeValue("bandOutlineAlpha");
                if (StringUtils.isNotEmpty(bandOutlineAlpha)) {
                    axis.setBandOutlineAlpha(Double.valueOf(bandOutlineAlpha));
                }

                String bandOutlineColor = axisElement.attributeValue("bandOutlineColor");
                if (StringUtils.isNotEmpty(bandOutlineColor)) {
                    axis.setBandOutlineColor(Color.valueOf(bandOutlineColor));
                }

                String bandOutlineThickness = axisElement.attributeValue("bandOutlineThickness");
                if (StringUtils.isNotEmpty(bandOutlineThickness)) {
                    axis.setBandOutlineThickness(Integer.valueOf(bandOutlineThickness));
                }

                String bottomText = axisElement.attributeValue("bottomText");
                if (StringUtils.isNotEmpty(bottomText)) {
                    axis.setBottomText(loadResourceString(bottomText));
                }

                String bottomTextBold = axisElement.attributeValue("bottomTextBold");
                if (StringUtils.isNotEmpty(bottomTextBold)) {
                    axis.setBottomTextBold(Boolean.valueOf(bottomTextBold));
                }

                String bottomTextColor = axisElement.attributeValue("bottomTextColor");
                if (StringUtils.isNotEmpty(bottomTextColor)) {
                    axis.setBottomTextColor(Color.valueOf(bottomTextColor));
                }

                String bottomTextFontSize = axisElement.attributeValue("bottomTextFontSize");
                if (StringUtils.isNotEmpty(bottomTextFontSize)) {
                    axis.setBottomTextFontSize(Integer.valueOf(bottomTextFontSize));
                }

                String bottomTextYOffset = axisElement.attributeValue("bottomTextYOffset");
                if (StringUtils.isNotEmpty(bottomTextYOffset)) {
                    axis.setBottomTextYOffset(Integer.valueOf(bottomTextYOffset));
                }

                String centerX = axisElement.attributeValue("centerX");
                if (StringUtils.isNotEmpty(centerX)) {
                    axis.setCenterX(centerX);
                }

                String centerY = axisElement.attributeValue("centerY");
                if (StringUtils.isNotEmpty(centerY)) {
                    axis.setCenterY(centerY);
                }

                String color = axisElement.attributeValue("color");
                if (StringUtils.isNotEmpty(color)) {
                    axis.setColor(Color.valueOf(color));
                }

                String endAngle = axisElement.attributeValue("endAngle");
                if (StringUtils.isNotEmpty(endAngle)) {
                    axis.setEndAngle(Integer.valueOf(endAngle));
                }

                String endValue = axisElement.attributeValue("endValue");
                if (StringUtils.isNotEmpty(endValue)) {
                    axis.setEndValue(Double.valueOf(endValue));
                }

                String fontSize = axisElement.attributeValue("fontSize");
                if (StringUtils.isNotEmpty(fontSize)) {
                    axis.setFontSize(Integer.valueOf(fontSize));
                }

                String gridCount = axisElement.attributeValue("gridCount");
                if (StringUtils.isNotEmpty(gridCount)) {
                    axis.setGridCount(Integer.valueOf(gridCount));
                }

                String gridInside = axisElement.attributeValue("gridInside");
                if (StringUtils.isNotEmpty(gridInside)) {
                    axis.setGridInside(Boolean.valueOf(gridInside));
                }

                String id = axisElement.attributeValue("id");
                if (StringUtils.isNotEmpty(id)) {
                    axis.setId(id);
                }

                String inside = axisElement.attributeValue("inside");
                if (StringUtils.isNotEmpty(inside)) {
                    axis.setInside(Boolean.valueOf(inside));
                }

                String labelFrequency = axisElement.attributeValue("labelFrequency");
                if (StringUtils.isNotEmpty(labelFrequency)) {
                    axis.setLabelFrequency(Double.valueOf(labelFrequency));
                }

                String labelOffset = axisElement.attributeValue("labelOffset");
                if (StringUtils.isNotEmpty(labelOffset)) {
                    axis.setLabelOffset(Integer.valueOf(labelOffset));
                }

                String labelsEnabled = axisElement.attributeValue("labelsEnabled");
                if (StringUtils.isNotEmpty(labelsEnabled)) {
                    axis.setLabelsEnabled(Boolean.valueOf(labelsEnabled));
                }

                String minorTickInterval = axisElement.attributeValue("minorTickInterval");
                if (StringUtils.isNotEmpty(minorTickInterval)) {
                    axis.setMinorTickInterval(Double.valueOf(minorTickInterval));
                }

                String minorTickLength = axisElement.attributeValue("minorTickLength");
                if (StringUtils.isNotEmpty(minorTickLength)) {
                    axis.setMinorTickLength(Integer.valueOf(minorTickLength));
                }

                String radius = axisElement.attributeValue("radius");
                if (StringUtils.isNotEmpty(radius)) {
                    axis.setRadius(radius);
                }

                String showFirstLabel = axisElement.attributeValue("showFirstLabel");
                if (StringUtils.isNotEmpty(showFirstLabel)) {
                    axis.setShowFirstLabel(Boolean.valueOf(showFirstLabel));
                }

                String showLastLabel = axisElement.attributeValue("showLastLabel");
                if (StringUtils.isNotEmpty(showLastLabel)) {
                    axis.setShowLastLabel(Boolean.valueOf(showLastLabel));
                }

                String startAngle = axisElement.attributeValue("startAngle");
                if (StringUtils.isNotEmpty(startAngle)) {
                    axis.setStartAngle(Integer.valueOf(startAngle));
                }

                String startValue = axisElement.attributeValue("startValue");
                if (StringUtils.isNotEmpty(startValue)) {
                    axis.setStartValue(Double.valueOf(startValue));
                }

                String tickAlpha = axisElement.attributeValue("tickAlpha");
                if (StringUtils.isNotEmpty(tickAlpha)) {
                    axis.setTickAlpha(Double.valueOf(tickAlpha));
                }

                String tickColor = axisElement.attributeValue("tickColor");
                if (StringUtils.isNotEmpty(tickColor)) {
                    axis.setTickColor(Color.valueOf(tickColor));
                }

                String tickLength = axisElement.attributeValue("tickLength");
                if (StringUtils.isNotEmpty(tickLength)) {
                    axis.setTickLength(Integer.valueOf(tickLength));
                }

                String tickThickness = axisElement.attributeValue("tickThickness");
                if (StringUtils.isNotEmpty(tickThickness)) {
                    axis.setTickThickness(Integer.valueOf(tickThickness));
                }

                String topText = axisElement.attributeValue("topText");
                if (StringUtils.isNotEmpty(topText)) {
                    axis.setTopText(loadResourceString(topText));
                }

                String topTextBold = axisElement.attributeValue("topTextBold");
                if (StringUtils.isNotEmpty(topTextBold)) {
                    axis.setTopTextBold(Boolean.valueOf(topTextBold));
                }

                String topTextColor = axisElement.attributeValue("topTextColor");
                if (StringUtils.isNotEmpty(topTextColor)) {
                    axis.setTopTextColor(Color.valueOf(topTextColor));
                }

                String topTextFontSize = axisElement.attributeValue("topTextFontSize");
                if (StringUtils.isNotEmpty(topTextFontSize)) {
                    axis.setTopTextFontSize(Integer.valueOf(topTextFontSize));
                }

                String topTextYOffset = axisElement.attributeValue("topTextYOffset");
                if (StringUtils.isNotEmpty(topTextYOffset)) {
                    axis.setTopTextYOffset(Integer.valueOf(topTextYOffset));
                }

                String unit = axisElement.attributeValue("unit");
                if (StringUtils.isNotEmpty(unit)) {
                    axis.setUnit(unit);
                }

                String unitPosition = axisElement.attributeValue("unitPosition");
                if (StringUtils.isNotEmpty(unitPosition)) {
                    axis.setUnitPosition(UnitPosition.valueOf(unitPosition));
                }

                String valueInterval = axisElement.attributeValue("valueInterval");
                if (StringUtils.isNotEmpty(valueInterval)) {
                    axis.setValueInterval(Integer.valueOf(valueInterval));
                }

                String usePrefixes = axisElement.attributeValue("usePrefixes");
                if (StringUtils.isNotEmpty(usePrefixes)) {
                    axis.setUsePrefixes(Boolean.valueOf(usePrefixes));
                }

                chart.addAxes(axis);
            }
        }
    }

    protected void loadGaugeBands(GaugeAxis axis, Element axisElement) {
        Element bandsElement = axisElement.element("bands");
        if (bandsElement != null) {
            for (Object bandItem : bandsElement.elements("band")) {
                Element bandElement = (Element) bandItem;

                GaugeBand band = new GaugeBand();

                String alpha = bandElement.attributeValue("alpha");
                if (StringUtils.isNotEmpty(alpha)) {
                    band.setAlpha(Double.valueOf(alpha));
                }

                String gradientRatio = bandElement.attributeValue("gradientRatio");
                if (StringUtils.isNotEmpty(gradientRatio)) {
                    List<Float> gradientRatioList = new ArrayList<>();
                    for (String s : GRADIENT_RATIO_PATTERN.split(gradientRatio))
                        gradientRatioList.add(Float.valueOf(s));
                    band.setGradientRatio(gradientRatioList);
                }

                String balloonText = bandElement.attributeValue("balloonText");
                if (StringUtils.isNotEmpty(balloonText)) {
                    band.setBalloonText(balloonText);
                }

                String color = bandElement.attributeValue("color");
                if (StringUtils.isNotEmpty(color)) {
                    band.setColor(Color.valueOf(color));
                }

                String endValue = bandElement.attributeValue("endValue");
                if (StringUtils.isNotEmpty(endValue)) {
                    band.setEndValue(Double.valueOf(endValue));
                }

                String id = bandElement.attributeValue("id");
                if (StringUtils.isNotEmpty(id)) {
                    band.setId(id);
                }

                String innerRadius = bandElement.attributeValue("innerRadius");
                if (StringUtils.isNotEmpty(innerRadius)) {
                    band.setInnerRadius(innerRadius);
                }

                String radius = bandElement.attributeValue("radius");
                if (StringUtils.isNotEmpty(radius)) {
                    band.setRadius(radius);
                }

                String startValue = bandElement.attributeValue("startValue");
                if (StringUtils.isNotEmpty(startValue)) {
                    band.setStartValue(Double.valueOf(startValue));
                }

                String url = bandElement.attributeValue("url");
                if (StringUtils.isNotEmpty(url)) {
                    band.setUrl(url);
                }

                axis.addBands(band);
            }
        }
    }

    protected void loadArrows(AngularGaugeChart chart, Element element) {
        Element arrowsElement = element.element("arrows");
        if (arrowsElement != null) {
            for (Object arrowItem : arrowsElement.elements("arrow")) {
                Element arrowElement = (Element) arrowItem;

                GaugeArrow arrow = new GaugeArrow();

                String alpha = arrowElement.attributeValue("alpha");
                if (StringUtils.isNotEmpty(alpha)) {
                    arrow.setAlpha(Double.valueOf(alpha));
                }

                String axis = arrowElement.attributeValue("axis");
                if (StringUtils.isNotEmpty(axis)) {
                    arrow.setAxis(axis);
                }

                String borderAlpha = arrowElement.attributeValue("borderAlpha");
                if (StringUtils.isNotEmpty(borderAlpha)) {
                    arrow.setBorderAlpha(Double.valueOf(borderAlpha));
                }

                String clockWiseOnly = arrowElement.attributeValue("clockWiseOnly");
                if (StringUtils.isNotEmpty(clockWiseOnly)) {
                    arrow.setClockWiseOnly(Boolean.valueOf(clockWiseOnly));
                }

                String color = arrowElement.attributeValue("color");
                if (StringUtils.isNotEmpty(color)) {
                    arrow.setColor(Color.valueOf(color));
                }

                String id = arrowElement.attributeValue("id");
                if (StringUtils.isNotEmpty(id)) {
                    arrow.setId(id);
                }

                String innerRadius = arrowElement.attributeValue("innerRadius");
                if (StringUtils.isNotEmpty(innerRadius)) {
                    arrow.setInnerRadius(innerRadius);
                }

                String nailAlpha = arrowElement.attributeValue("nailAlpha");
                if (StringUtils.isNotEmpty(nailAlpha)) {
                    arrow.setNailAlpha(Double.valueOf(nailAlpha));
                }

                String nailBorderAlpha = arrowElement.attributeValue("nailBorderAlpha");
                if (StringUtils.isNotEmpty(nailBorderAlpha)) {
                    arrow.setNailBorderAlpha(Double.valueOf(nailBorderAlpha));
                }

                String nailBorderThickness = arrowElement.attributeValue("nailBorderThickness");
                if (StringUtils.isNotEmpty(nailBorderThickness)) {
                    arrow.setNailBorderThickness(Integer.valueOf(nailBorderThickness));
                }

                String nailRadius = arrowElement.attributeValue("nailRadius");
                if (StringUtils.isNotEmpty(nailRadius)) {
                    arrow.setNailRadius(Integer.valueOf(nailRadius));
                }

                String radius = arrowElement.attributeValue("radius");
                if (StringUtils.isNotEmpty(radius)) {
                    arrow.setRadius(radius);
                }

                String startWidth = arrowElement.attributeValue("startWidth");
                if (StringUtils.isNotEmpty(startWidth)) {
                    arrow.setStartWidth(Integer.valueOf(startWidth));
                }

                String value = arrowElement.attributeValue("value");
                if (StringUtils.isNotEmpty(value)) {
                    arrow.setValue(Double.valueOf(value));
                }

                chart.addArrows(arrow);
            }
        }
    }
}