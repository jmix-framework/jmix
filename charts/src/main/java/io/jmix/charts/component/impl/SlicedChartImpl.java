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


import io.jmix.charts.model.chart.impl.SlicedChartModelImpl;
import io.jmix.core.common.event.Subscription;
import io.jmix.charts.component.SlicedChart;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.animation.AnimationEffect;
import io.jmix.charts.widget.amcharts.events.slice.listener.SliceClickListener;
import io.jmix.charts.widget.amcharts.events.slice.listener.SlicePullInListener;
import io.jmix.charts.widget.amcharts.events.slice.listener.SlicePullOutListener;
import io.jmix.charts.widget.amcharts.events.slice.listener.SliceRightClickListener;
import io.jmix.charts.model.JsFunction;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class SlicedChartImpl<T extends SlicedChart,
        M extends SlicedChartModelImpl> extends ChartImpl<T, M> implements io.jmix.charts.component.SlicedChart<T> {

    protected SliceClickListener sliceClickHandler;

    protected SliceRightClickListener sliceRightClickHandler;

    protected SlicePullInListener slicePullInHandler;

    protected SlicePullOutListener slicePullOutHandler;

    @Override
    public Double getAlpha() {
        return getModel().getAlpha();
    }

    @Override
    public T setAlpha(Double alpha) {
        getModel().setAlpha(alpha);
        return (T) this;
    }

    @Override
    public String getAlphaField() {
        return getModel().getAlphaField();
    }

    @Override
    public T setAlphaField(String alphaField) {
        getModel().setAlphaField(alphaField);
        return (T) this;
    }

    @Override
    public Color getBaseColor() {
        return getModel().getBaseColor();
    }

    @Override
    public T setBaseColor(Color baseColor) {
        getModel().setBaseColor(baseColor);
        return (T) this;
    }

    @Override
    public String getColorField() {
        return getModel().getColorField();
    }

    @Override
    public T setColorField(String colorField) {
        getModel().setColorField(colorField);
        return (T) this;
    }

    @Override
    public List<Color> getColors() {
        return getModel().getColors();
    }

    @Override
    public T setColors(List list) {
        getModel().setColors(list);
        return (T) this;
    }

    @Override
    public T addColors(Color... colors) {
        getModel().addColors(colors);
        return (T) this;
    }

    @Override
    public String getDescriptionField() {
        return getModel().getDescriptionField();
    }

    @Override
    public T setDescriptionField(String descriptionField) {
        getModel().setDescriptionField(descriptionField);
        return (T) this;
    }

    @Override
    public List<Double> getGradientRatio() {
        return getModel().getGradientRatio();
    }

    @Override
    public T setGradientRatio(List gradientRatio) {
        getModel().setGradientRatio(gradientRatio);
        return (T) this;
    }

    @Override
    public T addGradientRatio(Double... ratios) {
        getModel().addGradientRatio(ratios);
        return (T) this;
    }

    @Override
    public Double getGroupedAlpha() {
        return getModel().getGroupedAlpha();
    }

    @Override
    public T setGroupedAlpha(Double groupedAlpha) {
        getModel().setGroupedAlpha(groupedAlpha);
        return (T) this;
    }

    @Override
    public Color getGroupedColor() {
        return getModel().getGroupedColor();
    }

    @Override
    public T setGroupedColor(Color groupedColor) {
        getModel().setGroupedColor(groupedColor);
        return (T) this;
    }

    @Override
    public String getGroupedDescription() {
        return getModel().getGroupedDescription();
    }

    @Override
    public T setGroupedDescription(String groupedDescription) {
        getModel().setGroupedDescription(groupedDescription);
        return (T) this;
    }

    @Override
    public Boolean getGroupedPulled() {
        return getModel().getGroupedPulled();
    }

    @Override
    public T setGroupedPulled(Boolean groupedPulled) {
        getModel().setGroupedPulled(groupedPulled);
        return (T) this;
    }

    @Override
    public String getGroupedTitle() {
        return getModel().getGroupedTitle();
    }

    @Override
    public T setGroupedTitle(String groupedTitle) {
        getModel().setGroupedTitle(groupedTitle);
        return (T) this;
    }

    @Override
    public Double getGroupPercent() {
        return getModel().getGroupPercent();
    }

    @Override
    public T setGroupPercent(Double groupPercent) {
        getModel().setGroupPercent(groupPercent);
        return (T) this;
    }

    @Override
    public Double getHideLabelsPercent() {
        return getModel().getHideLabelsPercent();
    }

    @Override
    public T setHideLabelsPercent(Double hideLabelsPercent) {
        getModel().setHideLabelsPercent(hideLabelsPercent);
        return (T) this;
    }

    @Override
    public Double getHoverAlpha() {
        return getModel().getHoverAlpha();
    }

    @Override
    public T setHoverAlpha(Double hoverAlpha) {
        getModel().setHoverAlpha(hoverAlpha);
        return (T) this;
    }

    @Override
    public Boolean getLabelsEnabled() {
        return getModel().getLabelsEnabled();
    }

    @Override
    public T setLabelsEnabled(Boolean labelsEnabled) {
        getModel().setLabelsEnabled(labelsEnabled);
        return (T) this;
    }

    @Override
    public Double getLabelTickAlpha() {
        return getModel().getLabelTickAlpha();
    }

    @Override
    public T setLabelTickAlpha(Double labelTickAlpha) {
        getModel().setLabelTickAlpha(labelTickAlpha);
        return (T) this;
    }

    @Override
    public Color getLabelTickColor() {
        return getModel().getLabelTickColor();
    }

    @Override
    public T setLabelTickColor(Color labelTickColor) {
        getModel().setLabelTickColor(labelTickColor);
        return (T) this;
    }

    @Override
    public Integer getMarginBottom() {
        return getModel().getMarginBottom();
    }

    @Override
    public T setMarginBottom(Integer marginBottom) {
        getModel().setMarginBottom(marginBottom);
        return (T) this;
    }

    @Override
    public Integer getMarginLeft() {
        return getModel().getMarginLeft();
    }

    @Override
    public T setMarginLeft(Integer marginLeft) {
        getModel().setMarginLeft(marginLeft);
        return (T) this;
    }

    @Override
    public Integer getMarginRight() {
        return getModel().getMarginRight();
    }

    @Override
    public T setMarginRight(Integer marginRight) {
        getModel().setMarginRight(marginRight);
        return (T) this;
    }

    @Override
    public Integer getMarginTop() {
        return getModel().getMarginTop();
    }

    @Override
    public T setMarginTop(Integer marginTop) {
        getModel().setMarginTop(marginTop);
        return (T) this;
    }

    @Override
    public Double getOutlineAlpha() {
        return getModel().getOutlineAlpha();
    }

    @Override
    public T setOutlineAlpha(Double outlineAlpha) {
        getModel().setOutlineAlpha(outlineAlpha);
        return (T) this;
    }

    @Override
    public Color getOutlineColor() {
        return getModel().getOutlineColor();
    }

    @Override
    public T setOutlineColor(Color outlineColor) {
        getModel().setOutlineColor(outlineColor);
        return (T) this;
    }

    @Override
    public Integer getOutlineThickness() {
        return getModel().getOutlineThickness();
    }

    @Override
    public T setOutlineThickness(Integer outlineThickness) {
        getModel().setOutlineThickness(outlineThickness);
        return (T) this;
    }

    @Override
    public String getPatternField() {
        return getModel().getPatternField();
    }

    @Override
    public T setPatternField(String patternField) {
        getModel().setPatternField(patternField);
        return (T) this;
    }

    @Override
    public String getPulledField() {
        return getModel().getPulledField();
    }

    @Override
    public T setPulledField(String pulledField) {
        getModel().setPulledField(pulledField);
        return (T) this;
    }

    @Override
    public Integer getPullOutDuration() {
        return getModel().getPullOutDuration();
    }

    @Override
    public T setPullOutDuration(Integer pullOutDuration) {
        getModel().setPullOutDuration(pullOutDuration);
        return (T) this;
    }

    @Override
    public AnimationEffect getPullOutEffect() {
        return getModel().getPullOutEffect();
    }

    @Override
    public T setPullOutEffect(AnimationEffect pullOutEffect) {
        getModel().setPullOutEffect(pullOutEffect);
        return (T) this;
    }

    @Override
    public Boolean getPullOutOnlyOne() {
        return getModel().getPullOutOnlyOne();
    }

    @Override
    public T setPullOutOnlyOne(Boolean pullOutOnlyOne) {
        getModel().setPullOutOnlyOne(pullOutOnlyOne);
        return (T) this;
    }

    @Override
    public Boolean getSequencedAnimation() {
        return getModel().getSequencedAnimation();
    }

    @Override
    public T setSequencedAnimation(Boolean sequencedAnimation) {
        getModel().setSequencedAnimation(sequencedAnimation);
        return (T) this;
    }

    @Override
    public Double getStartAlpha() {
        return getModel().getStartAlpha();
    }

    @Override
    public T setStartAlpha(Double startAlpha) {
        getModel().setStartAlpha(startAlpha);
        return (T) this;
    }

    @Override
    public Double getStartDuration() {
        return getModel().getStartDuration();
    }

    @Override
    public T setStartDuration(Double startDuration) {
        getModel().setStartDuration(startDuration);
        return (T) this;
    }

    @Override
    public AnimationEffect getStartEffect() {
        return getModel().getStartEffect();
    }

    @Override
    public T setStartEffect(AnimationEffect startEffect) {
        getModel().setStartEffect(startEffect);
        return (T) this;
    }

    @Override
    public String getTitleField() {
        return getModel().getTitleField();
    }

    @Override
    public T setTitleField(String titleField) {
        getModel().setTitleField(titleField);
        return (T) this;
    }

    @Override
    public String getUrlField() {
        return getModel().getUrlField();
    }

    @Override
    public T setUrlField(String urlField) {
        getModel().setUrlField(urlField);
        return (T) this;
    }

    @Override
    public String getUrlTarget() {
        return getModel().getUrlTarget();
    }

    @Override
    public T setUrlTarget(String urlTarget) {
        getModel().setUrlTarget(urlTarget);
        return (T) this;
    }

    @Override
    public String getValueField() {
        return getModel().getValueField();
    }

    @Override
    public T setValueField(String valueField) {
        getModel().setValueField(valueField);
        return (T) this;
    }

    @Override
    public String getVisibleInLegendField() {
        return getModel().getVisibleInLegendField();
    }

    @Override
    public T setVisibleInLegendField(String visibleInLegendField) {
        getModel().setVisibleInLegendField(visibleInLegendField);
        return (T) this;
    }

    @Override
    public JsFunction getLabelFunction() {
        return getModel().getLabelFunction();
    }

    @Override
    public T setLabelFunction(JsFunction labelFunction) {
        getModel().setLabelFunction(labelFunction);
        return (T) this;
    }

    @Override
    public Integer getMaxLabelWidth() {
        return getModel().getMaxLabelWidth();
    }

    @Override
    public T setMaxLabelWidth(Integer maxLabelWidth) {
        getModel().setMaxLabelWidth(maxLabelWidth);
        return (T) this;
    }

    @Override
    public String getClassNameField() {
        return getModel().getClassNameField();
    }

    @Override
    public T setClassNameField(String classNameField) {
        getModel().setClassNameField(classNameField);
        return (T) this;
    }

    @Override
    public Boolean getShowZeroSlices() {
        return getModel().getShowZeroSlices();
    }

    @Override
    public T setShowZeroSlices(Boolean showZeroSlices) {
        getModel().setShowZeroSlices(showZeroSlices);
        return (T) this;
    }

    @Override
    public String getAccessibleLabel() {
        return getModel().getAccessibleLabel();
    }

    @Override
    public T setAccessibleLabel(String accessibleLabel) {
        getModel().setAccessibleLabel(accessibleLabel);
        return (T) this;
    }

    @Override
    public Color getLabelColorField() {
        return getModel().getLabelColorField();
    }

    @Override
    public T setLabelColorField(Color labelColorField) {
        getModel().setLabelColorField(labelColorField);
        return (T) this;
    }

    @Override
    public Integer getTabIndex() {
        return getModel().getTabIndex();
    }

    @Override
    public T setTabIndex(Integer tabIndex) {
        getModel().setTabIndex(tabIndex);
        return (T) this;
    }

    @Override
    public Subscription addSliceClickListener(Consumer<SliceClickEvent> listener) {
        if (sliceClickHandler == null) {
            sliceClickHandler = this::onSliceClick;
            component.addSliceClickListener(sliceClickHandler);
        }

        return getEventHub().subscribe(SliceClickEvent.class, listener);
    }

    protected void onSliceClick(io.jmix.charts.widget.amcharts.events.slice.SliceClickEvent e) {
        publish(SliceClickEvent.class,
                new SliceClickEvent(this, e.getDataItem(), e.getX(), e.getY(),
                        e.getAbsoluteX(), e.getAbsoluteY()));
    }

    @Override
    public Subscription addSliceRightClickListener(Consumer<SliceRightClickEvent> listener) {
        if (sliceRightClickHandler == null) {
            sliceRightClickHandler = this::onSliceRightClick;
            component.addSliceRightClickListener(sliceRightClickHandler);
        }

        return getEventHub().subscribe(SliceRightClickEvent.class, listener);
    }

    protected void onSliceRightClick(io.jmix.charts.widget.amcharts.events.slice.SliceRightClickEvent e) {
        publish(SliceRightClickEvent.class,
                new SliceRightClickEvent(this, e.getDataItem(), e.getX(), e.getY(),
                        e.getAbsoluteX(), e.getAbsoluteY()));
    }

    @Override
    public Subscription addSlicePullInListener(Consumer<SlicePullInEvent> listener) {
        if (slicePullInHandler == null) {
            slicePullInHandler = this::onSlicePullIn;
            component.addSlicePullInListener(slicePullInHandler);
        }

        return getEventHub().subscribe(SlicePullInEvent.class, listener);
    }

    protected void onSlicePullIn(io.jmix.charts.widget.amcharts.events.slice.SlicePullInEvent e) {
        publish(SlicePullInEvent.class, new SlicePullInEvent(this, e.getDataItem()));
    }

    @Override
    public Subscription addSlicePullOutListener(Consumer<SlicePullOutEvent> listener) {
        if (slicePullOutHandler == null) {
            slicePullOutHandler = this::onSlicePullOut;
            component.addSlicePullOutListener(slicePullOutHandler);
        }

        return getEventHub().subscribe(SlicePullOutEvent.class, listener);
    }

    protected void onSlicePullOut(io.jmix.charts.widget.amcharts.events.slice.SlicePullOutEvent e) {
        publish(SlicePullOutEvent.class, new SlicePullOutEvent(this, e.getDataItem()));
    }
}