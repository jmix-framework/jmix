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


import io.jmix.charts.component.SlicedChart;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.animation.AnimationEffect;
import io.jmix.charts.model.JsFunction;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public abstract class SlicedChartLoader<T extends SlicedChart> extends AbstractChartLoader<T> {

    @Override
    protected void loadConfiguration(T chart, Element element) {
        super.loadConfiguration(chart, element);

        loadChartData(chart, element);

        loadColors(chart, element);
        loadGradientRatios(chart, element);

        String accessibleLabel = element.attributeValue("accessibleLabel");
        if (StringUtils.isNotEmpty(accessibleLabel)) {
            chart.setAccessibleLabel(accessibleLabel);
        }

        String alpha = element.attributeValue("alpha");
        if (StringUtils.isNotEmpty(alpha)) {
            chart.setAlpha(Double.valueOf(alpha));
        }

        String alphaField = element.attributeValue("alphaField");
        if (StringUtils.isNotEmpty(alphaField)) {
            chart.setAlphaField(alphaField);
        }

        String baseColor = element.attributeValue("baseColor");
        if (StringUtils.isNotEmpty(baseColor)) {
            chart.setBaseColor(Color.valueOf(baseColor));
        }

        String classNameField = element.attributeValue("classNameField");
        if (StringUtils.isNotEmpty(classNameField)) {
            chart.setClassNameField(classNameField);
        }

        String colorField = element.attributeValue("colorField");
        if (StringUtils.isNotEmpty(colorField)) {
            chart.setColorField(colorField);
        }

        String descriptionField = element.attributeValue("descriptionField");
        if (StringUtils.isNotEmpty(descriptionField)) {
            chart.setDescriptionField(descriptionField);
        }

        String groupedAlpha = element.attributeValue("groupedAlpha");
        if (StringUtils.isNotEmpty(groupedAlpha)) {
            chart.setGroupedAlpha(Double.valueOf(groupedAlpha));
        }

        String groupedColor = element.attributeValue("groupedColor");
        if (StringUtils.isNotEmpty(groupedColor)) {
            chart.setGroupedColor(Color.valueOf(groupedColor));
        }

        String groupedDescription = element.attributeValue("groupedDescription");
        if (StringUtils.isNotEmpty(groupedDescription)) {
            chart.setGroupedDescription(loadResourceString(groupedDescription));
        }

        String groupedPulled = element.attributeValue("groupedPulled");
        if (StringUtils.isNotEmpty(groupedPulled)) {
            chart.setGroupedPulled(Boolean.valueOf(groupedPulled));
        }

        String groupedTitle = element.attributeValue("groupedTitle");
        if (StringUtils.isNotEmpty(groupedTitle)) {
            chart.setGroupedTitle(loadResourceString(groupedTitle));
        }

        String groupPercent = element.attributeValue("groupPercent");
        if (StringUtils.isNotEmpty(groupPercent)) {
            chart.setGroupPercent(Double.valueOf(groupPercent));
        }

        String hideLabelsPercent = element.attributeValue("hideLabelsPercent");
        if (StringUtils.isNotEmpty(hideLabelsPercent)) {
            chart.setHideLabelsPercent(Double.valueOf(hideLabelsPercent));
        }

        String hoverAlpha = element.attributeValue("hoverAlpha");
        if (StringUtils.isNotEmpty(hoverAlpha)) {
            chart.setHoverAlpha(Double.valueOf(hoverAlpha));
        }

        String labelColorField = element.attributeValue("labelColorField");
        if (StringUtils.isNotEmpty(labelColorField)) {
            chart.setLabelColorField(Color.valueOf(labelColorField));
        }

        String labelFunction = element.elementText("labelFunction");
        if (StringUtils.isNotEmpty(labelFunction)) {
            chart.setLabelFunction(new JsFunction(labelFunction));
        }

        String labelsEnabled = element.attributeValue("labelsEnabled");
        if (StringUtils.isNotEmpty(labelsEnabled)) {
            chart.setLabelsEnabled(Boolean.valueOf(labelsEnabled));
        }

        String labelTickAlpha = element.attributeValue("labelTickAlpha");
        if (StringUtils.isNotEmpty(labelTickAlpha)) {
            chart.setLabelTickAlpha(Double.valueOf(labelTickAlpha));
        }

        String labelTickColor = element.attributeValue("labelTickColor");
        if (StringUtils.isNotEmpty(labelTickColor)) {
            chart.setLabelTickColor(Color.valueOf(labelTickColor));
        }

        loadMargins(chart, element);

        String maxLabelWidth = element.attributeValue("maxLabelWidth");
        if (StringUtils.isNotEmpty(maxLabelWidth)) {
            chart.setMaxLabelWidth(Integer.valueOf(maxLabelWidth));
        }

        String outlineAlpha = element.attributeValue("outlineAlpha");
        if (StringUtils.isNotEmpty(outlineAlpha)) {
            chart.setOutlineAlpha(Double.valueOf(outlineAlpha));
        }

        String outlineColor = element.attributeValue("outlineColor");
        if (StringUtils.isNotEmpty(outlineColor)) {
            chart.setOutlineColor(Color.valueOf(outlineColor));
        }

        String outlineThickness = element.attributeValue("outlineThickness");
        if (StringUtils.isNotEmpty(outlineThickness)) {
            chart.setOutlineThickness(Integer.valueOf(outlineThickness));
        }

        String patternField = element.attributeValue("patternField");
        if (StringUtils.isNotEmpty(patternField)) {
            chart.setPatternField(patternField);
        }

        String pulledField = element.attributeValue("pulledField");
        if (StringUtils.isNotEmpty(pulledField)) {
            chart.setPulledField(pulledField);
        }

        String pullOutDuration = element.attributeValue("pullOutDuration");
        if (StringUtils.isNotEmpty(pullOutDuration)) {
            chart.setPullOutDuration(Integer.valueOf(pullOutDuration));
        }

        String pullOutEffect = element.attributeValue("pullOutEffect");
        if (StringUtils.isNotEmpty(pullOutEffect)) {
            chart.setPullOutEffect(AnimationEffect.valueOf(pullOutEffect));
        }

        String pullOutOnlyOne = element.attributeValue("pullOutOnlyOne");
        if (StringUtils.isNotEmpty(pullOutOnlyOne)) {
            chart.setPullOutOnlyOne(Boolean.valueOf(pullOutOnlyOne));
        }

        String sequencedAnimation = element.attributeValue("sequencedAnimation");
        if (StringUtils.isNotEmpty(sequencedAnimation)) {
            chart.setSequencedAnimation(Boolean.valueOf(sequencedAnimation));
        }

        String showZeroSlices = element.attributeValue("showZeroSlices");
        if (StringUtils.isNotEmpty(showZeroSlices)) {
            chart.setShowZeroSlices(Boolean.valueOf(showZeroSlices));
        }

        String startAlpha = element.attributeValue("startAlpha");
        if (StringUtils.isNotEmpty(startAlpha)) {
            chart.setStartAlpha(Double.valueOf(startAlpha));
        }

        String tabIndex = element.attributeValue("tabIndex");
        if (StringUtils.isNotEmpty(tabIndex)) {
            chart.setTabIndex(Integer.valueOf(tabIndex));
        }

        loadStartEffect(chart, element);

        String titleField = element.attributeValue("titleField");
        if (StringUtils.isNotEmpty(titleField)) {
            chart.setTitleField(titleField);
        }

        String urlField = element.attributeValue("urlField");
        if (StringUtils.isNotEmpty(urlField)) {
            chart.setUrlField(urlField);
        }

        String urlTarget = element.attributeValue("urlTarget");
        if (StringUtils.isNotEmpty(urlTarget)) {
            chart.setUrlTarget(urlTarget);
        }

        String valueField = element.attributeValue("valueField");
        if (StringUtils.isNotEmpty(valueField)) {
            chart.setValueField(valueField);
        }

        String visibleInLegendField = element.attributeValue("visibleInLegendField");
        if (StringUtils.isNotEmpty(visibleInLegendField)) {
            chart.setVisibleInLegendField(visibleInLegendField);
        }
    }

    protected void loadGradientRatios(T chart, Element element) {
        Element gradientRatiosElement = element.element("gradientRatios");
        if (gradientRatiosElement != null) {
            for (Object ratioItem : gradientRatiosElement.elements("ratio")) {
                Element ratioElement = (Element) ratioItem;

                String value = ratioElement.attributeValue("value");
                if (StringUtils.isNotEmpty(value)) {
                    chart.addGradientRatio(Double.valueOf(value));
                }
            }
        }
    }
}