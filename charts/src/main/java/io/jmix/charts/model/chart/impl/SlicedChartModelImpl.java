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


import io.jmix.charts.model.Color;
import io.jmix.charts.model.JsFunction;
import io.jmix.charts.model.animation.AnimationEffect;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.chart.SlicedChartModel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * See documentation for properties of AmSlicedChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmSlicedChart">http://docs.amcharts.com/3/javascriptcharts/AmSlicedChart</a>
 */
@SuppressWarnings("unchecked")
public abstract class SlicedChartModelImpl<T extends SlicedChartModelImpl> extends AbstractChart<T> implements SlicedChartModel<T> {

    private static final long serialVersionUID = -9176849613764858438L;

    private String accessibleLabel;

    private Double alpha;

    private String alphaField;

    private Color baseColor;

    private String classNameField;

    private String colorField;

    private List<Color> colors;

    private String descriptionField;

    private List<Double> gradientRatio;

    private Double groupedAlpha;

    private Color groupedColor;

    private String groupedDescription;

    private Boolean groupedPulled;

    private String groupedTitle;

    private Double groupPercent;

    private Double hideLabelsPercent;

    private Double hoverAlpha;

    private Color labelColorField;

    private JsFunction labelFunction;

    private Boolean labelsEnabled;

    private Double labelTickAlpha;

    private Color labelTickColor;

    private Integer marginBottom;

    private Integer marginLeft;

    private Integer marginRight;

    private Integer marginTop;

    private Integer maxLabelWidth;

    private Double outlineAlpha;

    private Color outlineColor;

    private Integer outlineThickness;

    private String patternField;

    private String pulledField;

    private Integer pullOutDuration;

    private AnimationEffect pullOutEffect;

    private Boolean pullOutOnlyOne;

    private Boolean sequencedAnimation;

    private Boolean showZeroSlices;

    private Double startAlpha;

    private Double startDuration;

    private AnimationEffect startEffect;

    private Integer tabIndex;

    private String titleField;

    private String urlField;

    private String urlTarget;

    private String valueField;

    private String visibleInLegendField;

    public SlicedChartModelImpl(ChartType type) {
        super(type);
    }

    @Override
    public Double getAlpha() {
        return alpha;
    }

    @Override
    public T setAlpha(Double alpha) {
        this.alpha = alpha;
        return (T) this;
    }

    @Override
    public String getAlphaField() {
        return alphaField;
    }

    @Override
    public T setAlphaField(String alphaField) {
        this.alphaField = alphaField;
        return (T) this;
    }

    @Override
    public Color getBaseColor() {
        return baseColor;
    }

    @Override
    public T setBaseColor(Color baseColor) {
        this.baseColor = baseColor;
        return (T) this;
    }

    @Override
    public String getColorField() {
        return colorField;
    }

    @Override
    public T setColorField(String colorField) {
        this.colorField = colorField;
        return (T) this;
    }

    @Override
    public List<Color> getColors() {
        return colors;
    }

    @Override
    public T setColors(List<Color> colors) {
        this.colors = colors;
        return (T) this;
    }

    @Override
    public T addColors(Color... colors) {
        if (colors != null) {
            if (this.colors == null) {
                this.colors = new ArrayList<>();
            }
            this.colors.addAll(Arrays.asList(colors));
        }
        return (T) this;
    }

    @Override
    public String getDescriptionField() {
        return descriptionField;
    }

    @Override
    public T setDescriptionField(String descriptionField) {
        this.descriptionField = descriptionField;
        return (T) this;
    }

    @Override
    public List<Double> getGradientRatio() {
        return gradientRatio;
    }

    @Override
    public T setGradientRatio(List<Double> gradientRatio) {
        this.gradientRatio = gradientRatio;
        return (T) this;
    }

    @Override
    public T addGradientRatio(Double... ratios) {
        if (ratios != null) {
            if (this.gradientRatio == null) {
                this.gradientRatio = new ArrayList<>();
            }
            this.gradientRatio.addAll(Arrays.asList(ratios));
        }
        return (T) this;
    }

    @Override
    public Double getGroupedAlpha() {
        return groupedAlpha;
    }

    @Override
    public T setGroupedAlpha(Double groupedAlpha) {
        this.groupedAlpha = groupedAlpha;
        return (T) this;
    }

    @Override
    public Color getGroupedColor() {
        return groupedColor;
    }

    @Override
    public T setGroupedColor(Color groupedColor) {
        this.groupedColor = groupedColor;
        return (T) this;
    }

    @Override
    public String getGroupedDescription() {
        return groupedDescription;
    }

    @Override
    public T setGroupedDescription(String groupedDescription) {
        this.groupedDescription = groupedDescription;
        return (T) this;
    }

    @Override
    public Boolean getGroupedPulled() {
        return groupedPulled;
    }

    @Override
    public T setGroupedPulled(Boolean groupedPulled) {
        this.groupedPulled = groupedPulled;
        return (T) this;
    }

    @Override
    public String getGroupedTitle() {
        return groupedTitle;
    }

    @Override
    public T setGroupedTitle(String groupedTitle) {
        this.groupedTitle = groupedTitle;
        return (T) this;
    }

    @Override
    public Double getGroupPercent() {
        return groupPercent;
    }

    @Override
    public T setGroupPercent(Double groupPercent) {
        this.groupPercent = groupPercent;
        return (T) this;
    }

    @Override
    public Double getHideLabelsPercent() {
        return hideLabelsPercent;
    }

    @Override
    public T setHideLabelsPercent(Double hideLabelsPercent) {
        this.hideLabelsPercent = hideLabelsPercent;
        return (T) this;
    }

    @Override
    public Double getHoverAlpha() {
        return hoverAlpha;
    }

    @Override
    public T setHoverAlpha(Double hoverAlpha) {
        this.hoverAlpha = hoverAlpha;
        return (T) this;
    }

    @Override
    public Boolean getLabelsEnabled() {
        return labelsEnabled;
    }

    @Override
    public T setLabelsEnabled(Boolean labelsEnabled) {
        this.labelsEnabled = labelsEnabled;
        return (T) this;
    }

    @Override
    public Double getLabelTickAlpha() {
        return labelTickAlpha;
    }

    @Override
    public T setLabelTickAlpha(Double labelTickAlpha) {
        this.labelTickAlpha = labelTickAlpha;
        return (T) this;
    }

    @Override
    public Color getLabelTickColor() {
        return labelTickColor;
    }

    @Override
    public T setLabelTickColor(Color labelTickColor) {
        this.labelTickColor = labelTickColor;
        return (T) this;
    }

    @Override
    public Integer getMarginBottom() {
        return marginBottom;
    }

    @Override
    public T setMarginBottom(Integer marginBottom) {
        this.marginBottom = marginBottom;
        return (T) this;
    }

    @Override
    public Integer getMarginLeft() {
        return marginLeft;
    }

    @Override
    public T setMarginLeft(Integer marginLeft) {
        this.marginLeft = marginLeft;
        return (T) this;
    }

    @Override
    public Integer getMarginRight() {
        return marginRight;
    }

    @Override
    public T setMarginRight(Integer marginRight) {
        this.marginRight = marginRight;
        return (T) this;
    }

    @Override
    public Integer getMarginTop() {
        return marginTop;
    }

    @Override
    public T setMarginTop(Integer marginTop) {
        this.marginTop = marginTop;
        return (T) this;
    }

    @Override
    public Double getOutlineAlpha() {
        return outlineAlpha;
    }

    @Override
    public T setOutlineAlpha(Double outlineAlpha) {
        this.outlineAlpha = outlineAlpha;
        return (T) this;
    }

    @Override
    public Color getOutlineColor() {
        return outlineColor;
    }

    @Override
    public T setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
        return (T) this;
    }

    @Override
    public Integer getOutlineThickness() {
        return outlineThickness;
    }

    @Override
    public T setOutlineThickness(Integer outlineThickness) {
        this.outlineThickness = outlineThickness;
        return (T) this;
    }

    @Override
    public String getPatternField() {
        return patternField;
    }

    @Override
    public T setPatternField(String patternField) {
        this.patternField = patternField;
        return (T) this;
    }

    @Override
    public String getPulledField() {
        return pulledField;
    }

    @Override
    public T setPulledField(String pulledField) {
        this.pulledField = pulledField;
        return (T) this;
    }

    @Override
    public Integer getPullOutDuration() {
        return pullOutDuration;
    }

    @Override
    public T setPullOutDuration(Integer pullOutDuration) {
        this.pullOutDuration = pullOutDuration;
        return (T) this;
    }

    @Override
    public AnimationEffect getPullOutEffect() {
        return pullOutEffect;
    }

    @Override
    public T setPullOutEffect(AnimationEffect pullOutEffect) {
        this.pullOutEffect = pullOutEffect;
        return (T) this;
    }

    @Override
    public Boolean getPullOutOnlyOne() {
        return pullOutOnlyOne;
    }

    @Override
    public T setPullOutOnlyOne(Boolean pullOutOnlyOne) {
        this.pullOutOnlyOne = pullOutOnlyOne;
        return (T) this;
    }

    @Override
    public Boolean getSequencedAnimation() {
        return sequencedAnimation;
    }

    @Override
    public T setSequencedAnimation(Boolean sequencedAnimation) {
        this.sequencedAnimation = sequencedAnimation;
        return (T) this;
    }

    @Override
    public Double getStartAlpha() {
        return startAlpha;
    }

    @Override
    public T setStartAlpha(Double startAlpha) {
        this.startAlpha = startAlpha;
        return (T) this;
    }

    @Override
    public Double getStartDuration() {
        return startDuration;
    }

    @Override
    public T setStartDuration(Double startDuration) {
        this.startDuration = startDuration;
        return (T) this;
    }

    @Override
    public AnimationEffect getStartEffect() {
        return startEffect;
    }

    @Override
    public T setStartEffect(AnimationEffect startEffect) {
        this.startEffect = startEffect;
        return (T) this;
    }

    @Override
    public String getTitleField() {
        return titleField;
    }

    @Override
    public T setTitleField(String titleField) {
        this.titleField = titleField;
        return (T) this;
    }

    @Override
    public String getUrlField() {
        return urlField;
    }

    @Override
    public T setUrlField(String urlField) {
        this.urlField = urlField;
        return (T) this;
    }

    @Override
    public String getUrlTarget() {
        return urlTarget;
    }

    @Override
    public T setUrlTarget(String urlTarget) {
        this.urlTarget = urlTarget;
        return (T) this;
    }

    @Override
    public String getValueField() {
        return valueField;
    }

    @Override
    public T setValueField(String valueField) {
        this.valueField = valueField;
        return (T) this;
    }

    @Override
    public String getVisibleInLegendField() {
        return visibleInLegendField;
    }

    @Override
    public T setVisibleInLegendField(String visibleInLegendField) {
        this.visibleInLegendField = visibleInLegendField;
        return (T) this;
    }

    @Override
    public JsFunction getLabelFunction() {
        return labelFunction;
    }

    @Override
    public T setLabelFunction(JsFunction labelFunction) {
        this.labelFunction = labelFunction;
        return (T) this;
    }

    @Override
    public Integer getMaxLabelWidth() {
        return maxLabelWidth;
    }

    @Override
    public T setMaxLabelWidth(Integer maxLabelWidth) {
        this.maxLabelWidth = maxLabelWidth;
        return (T) this;
    }

    @Override
    public String getClassNameField() {
        return classNameField;
    }

    @Override
    public T setClassNameField(String classNameField) {
        this.classNameField = classNameField;
        return (T) this;
    }

    @Override
    public Boolean getShowZeroSlices() {
        return showZeroSlices;
    }

    @Override
    public T setShowZeroSlices(Boolean showZeroSlices) {
        this.showZeroSlices = showZeroSlices;
        return (T) this;
    }

    @Override
    public String getAccessibleLabel() {
        return accessibleLabel;
    }

    @Override
    public T setAccessibleLabel(String accessibleLabel) {
        this.accessibleLabel = accessibleLabel;
        return (T) this;
    }

    @Override
    public Color getLabelColorField() {
        return labelColorField;
    }

    @Override
    public T setLabelColorField(Color labelColorField) {
        this.labelColorField = labelColorField;
        return (T) this;
    }

    @Override
    public Integer getTabIndex() {
        return tabIndex;
    }

    @Override
    public T setTabIndex(Integer tabIndex) {
        this.tabIndex = tabIndex;
        return (T) this;
    }

    @Override
    public List<String> getWiredFields() {
        List<String> wiredFields = new ArrayList<>(super.getWiredFields());

        if (StringUtils.isNotEmpty(alphaField)) {
            wiredFields.add(alphaField);
        }

        if (StringUtils.isNotEmpty(colorField)) {
            wiredFields.add(colorField);
        }

        if (StringUtils.isNotEmpty(descriptionField)) {
            wiredFields.add(descriptionField);
        }

        if (StringUtils.isNotEmpty(patternField)) {
            wiredFields.add(patternField);
        }

        if (StringUtils.isNotEmpty(pulledField)) {
            wiredFields.add(pulledField);
        }

        if (StringUtils.isNotEmpty(titleField)) {
            wiredFields.add(titleField);
        }

        if (StringUtils.isNotEmpty(urlField)) {
            wiredFields.add(urlField);
        }

        if (StringUtils.isNotEmpty(valueField)) {
            wiredFields.add(valueField);
        }

        if (StringUtils.isNotEmpty(visibleInLegendField)) {
            wiredFields.add(visibleInLegendField);
        }

        return wiredFields;
    }
}