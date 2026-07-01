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

package io.jmix.chartsflowui.kit.component.model.series;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.HasBorder;
import io.jmix.chartsflowui.kit.component.model.HasShadow;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.LineStyle;
import org.jspecify.annotations.Nullable;

/**
 * Bar chart shows different data through the height of a bar, which is used in rectangular coordinate
 * with at least 1 category axis. More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#series-bar">BarSeries documentation</a>
 */
public class BarSeries extends AbstractAxisAwareSeries<BarSeries>
        implements HasStack<BarSeries> {

    protected CoordinateSystem coordinateSystem;

    protected Integer xAxisIndex;

    protected Integer yAxisIndex;

    protected Integer polarIndex;

    protected SamplingType sampling;

    protected String stack;

    protected Boolean clip;

    protected HasStack.StackStrategy stackStrategy;

    protected Boolean roundCap;

    protected Boolean realtimeSort;

    protected Boolean showBackground;

    protected BackgroundStyle backgroundStyle;

    protected LabelLine labelLine;

    protected ItemStyleWithDecal itemStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected Select select;

    protected String barWidth;

    protected String barMaxWidth;

    protected String barMinWidth;

    protected Integer barMinHeight;

    protected Integer barMinAngle;

    protected String barGap;

    protected String barCategoryGap;

    protected String cursor;

    protected Boolean large;

    protected Integer largeThreshold;

    protected Integer progressive;

    protected Integer progressiveThreshold;

    protected ProgressiveChunkMode progressiveChunkMode;

    protected Boolean animation;

    protected Integer animationThreshold;

    protected Integer animationDurationUpdate;

    protected String animationEasingUpdate;

    protected Integer animationDelayUpdate;

    public BarSeries() {
        super(SeriesType.BAR);
    }

    /**
     * Background style of each bar. Only works if {@link BarSeries#showBackground} is set to {@code true}.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-bar.backgroundStyle">BarSeries.backgroundStyle</a>
     */
    public static class BackgroundStyle extends ChartObservableObject
            implements HasShadow<BackgroundStyle>, HasBorder<BackgroundStyle> {

        protected Color color;

        protected Color borderColor;

        protected Integer borderWidth;

        protected String borderType;

        protected Integer borderRadius;

        protected Integer shadowBlur;

        protected Color shadowColor;

        protected Integer shadowOffsetX;

        protected Integer shadowOffsetY;

        protected Double opacity;

        @Nullable
        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            markAsDirty();
        }

        @Nullable
        @Override
        public Color getBorderColor() {
            return borderColor;
        }

        @Override
        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            markAsDirty();
        }

        @Nullable
        @Override
        public Integer getBorderWidth() {
            return borderWidth;
        }

        @Override
        public void setBorderWidth(Integer borderWidth) {
            this.borderWidth = borderWidth;
            markAsDirty();
        }

        @Nullable
        public String getBorderType() {
            return borderType;
        }

        public void setBorderType(String borderType) {
            this.borderType = borderType;
            markAsDirty();
        }

        @Nullable
        @Override
        public Integer getBorderRadius() {
            return borderRadius;
        }

        @Override
        public void setBorderRadius(Integer borderRadius) {
            this.borderRadius = borderRadius;
            markAsDirty();
        }

        @Nullable
        @Override
        public Integer getShadowBlur() {
            return shadowBlur;
        }

        @Override
        public void setShadowBlur(Integer shadowBlur) {
            this.shadowBlur = shadowBlur;
            markAsDirty();
        }

        @Nullable
        @Override
        public Color getShadowColor() {
            return shadowColor;
        }

        @Override
        public void setShadowColor(Color shadowColor) {
            this.shadowColor = shadowColor;
            markAsDirty();
        }

        @Nullable
        @Override
        public Integer getShadowOffsetX() {
            return shadowOffsetX;
        }

        @Override
        public void setShadowOffsetX(Integer shadowOffsetX) {
            this.shadowOffsetX = shadowOffsetX;
            markAsDirty();
        }

        @Nullable
        @Override
        public Integer getShadowOffsetY() {
            return shadowOffsetY;
        }

        @Override
        public void setShadowOffsetY(Integer shadowOffsetY) {
            this.shadowOffsetY = shadowOffsetY;
            markAsDirty();
        }

        @Nullable
        public Double getOpacity() {
            return opacity;
        }

        public void setOpacity(Double opacity) {
            this.opacity = opacity;
            markAsDirty();
        }

        public BackgroundStyle withColor(Color color) {
            setColor(color);
            return this;
        }

        public BackgroundStyle withBorderType(String borderType) {
            setBorderType(borderType);
            return this;
        }

        public BackgroundStyle withOpacity(Double opacity) {
            setOpacity(opacity);
            return this;
        }
    }

    /**
     * Configuration component of label guideline.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-bar.labelLine">BarSeries.labelLine</a>
     */
    public static class LabelLine extends ChartObservableObject {

        protected Boolean show;

        protected LineStyle lineStyle;

        @Nullable
        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        @Nullable
        public LineStyle getLineStyle() {
            return lineStyle;
        }

        public void setLineStyle(LineStyle lineStyle) {
            if (this.lineStyle != null) {
                removeChild(this.lineStyle);
            }

            this.lineStyle = lineStyle;
            addChild(lineStyle);
        }

        public LabelLine withShow(Boolean show) {
            setShow(show);
            return this;
        }

        public LabelLine withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return this;
        }
    }

    /**
     * Item style of bar.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-bar.itemStyle">BarSeries.itemStyle</a>
     */
    public static class ItemStyle extends AbstractItemStyle<ItemStyle> {
    }

    /**
     * Component to configure the emphasis state.
     */
    public static class Emphasis extends AbstractBarElement<Emphasis> {

        protected Boolean disabled;

        protected FocusType focus;

        protected BlurScopeType blurScope;

        @Nullable
        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
            markAsDirty();
        }

        @Nullable
        public FocusType getFocus() {
            return focus;
        }

        public void setFocus(FocusType focus) {
            this.focus = focus;
            markAsDirty();
        }

        @Nullable
        public BlurScopeType getBlurScope() {
            return blurScope;
        }

        public void setBlurScope(BlurScopeType blurScope) {
            this.blurScope = blurScope;
            markAsDirty();
        }

        public Emphasis withDisabled(Boolean disabled) {
            setDisabled(disabled);
            return this;
        }

        public Emphasis withFocus(FocusType focus) {
            setFocus(focus);
            return this;
        }

        public Emphasis withBlurScope(BlurScopeType blurScope) {
            setBlurScope(blurScope);
            return this;
        }
    }

    /**
     * Component to configure the blur state.
     */
    public static class Blur extends AbstractBarElement<Blur> {
    }

    /**
     * Component to configure the selection state.
     */
    public static class Select extends AbstractBarElement<Select> {

        protected Boolean disabled;

        @Nullable
        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
            markAsDirty();
        }

        public Select withDisabled(Boolean disabled) {
            setDisabled(disabled);
            return this;
        }
    }

    /**
     * Base component for bar elements.
     *
     * @param <T> origin element class type
     */
    public static abstract class AbstractBarElement<T extends AbstractBarElement<T>>
            extends ChartObservableObject {

        protected Label label;

        protected LabelLine labelLine;

        protected ItemStyle itemStyle;

        @Nullable
        public Label getLabel() {
            return label;
        }

        public void setLabel(Label label) {
            this.label = label;
            markAsDirty();
        }

        @Nullable
        public LabelLine getLabelLine() {
            return labelLine;
        }

        public void setLabelLine(LabelLine labelLine) {
            this.labelLine = labelLine;
            markAsDirty();
        }

        @Nullable
        public ItemStyle getItemStyle() {
            return itemStyle;
        }

        public void setItemStyle(ItemStyle itemStyle) {
            this.itemStyle = itemStyle;
            markAsDirty();
        }

        @SuppressWarnings("unchecked")
        public T withLabel(Label label) {
            setLabel(label);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withLabelLine(LabelLine labelLine) {
            setLabelLine(labelLine);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withItemStyle(ItemStyle itemStyle) {
            setItemStyle(itemStyle);
            return (T) this;
        }
    }

    @Nullable
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    public void setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        markAsDirty();
    }

    @Nullable
    public Integer getXAxisIndex() {
        return xAxisIndex;
    }

    public void setXAxisIndex(Integer xAxisIndex) {
        this.xAxisIndex = xAxisIndex;
        markAsDirty();
    }

    @Nullable
    public Integer getYAxisIndex() {
        return yAxisIndex;
    }

    public void setYAxisIndex(Integer yAxisIndex) {
        this.yAxisIndex = yAxisIndex;
        markAsDirty();
    }

    @Nullable
    public Integer getPolarIndex() {
        return polarIndex;
    }

    public void setPolarIndex(Integer polarIndex) {
        this.polarIndex = polarIndex;
        markAsDirty();
    }

    @Nullable
    public SamplingType getSampling() {
        return sampling;
    }

    public void setSampling(SamplingType sampling) {
        this.sampling = sampling;
        markAsDirty();
    }

    @Nullable
    @Override
    public String getStack() {
        return stack;
    }

    @Override
    public void setStack(String stack) {
        this.stack = stack;
        markAsDirty();
    }

    @Nullable
    public Boolean getClip() {
        return clip;
    }

    public void setClip(Boolean clip) {
        this.clip = clip;
        markAsDirty();
    }

    @Nullable
    @Override
    public StackStrategy getStackStrategy() {
        return stackStrategy;
    }

    @Override
    public void setStackStrategy(StackStrategy stackStrategy) {
        this.stackStrategy = stackStrategy;
        markAsDirty();
    }

    @Nullable
    public Boolean getRoundCap() {
        return roundCap;
    }

    public void setRoundCap(Boolean roundCap) {
        this.roundCap = roundCap;
        markAsDirty();
    }

    @Nullable
    public Boolean getRealtimeSort() {
        return realtimeSort;
    }

    public void setRealtimeSort(Boolean realtimeSort) {
        this.realtimeSort = realtimeSort;
        markAsDirty();
    }

    @Nullable
    public Boolean getShowBackground() {
        return showBackground;
    }

    public void setShowBackground(Boolean showBackground) {
        this.showBackground = showBackground;
        markAsDirty();
    }

    @Nullable
    public BackgroundStyle getBackgroundStyle() {
        return backgroundStyle;
    }

    public void setBackgroundStyle(BackgroundStyle backgroundStyle) {
        if (this.backgroundStyle != null) {
            removeChild(this.backgroundStyle);
        }

        this.backgroundStyle = backgroundStyle;
        addChild(backgroundStyle);
    }

    @Nullable
    public LabelLine getLabelLine() {
        return labelLine;
    }

    public void setLabelLine(LabelLine labelLine) {
        if (this.labelLine != null) {
            removeChild(this.labelLine);
        }

        this.labelLine = labelLine;
        addChild(labelLine);
    }

    @Nullable
    public ItemStyleWithDecal getItemStyle() {
        return itemStyle;
    }

    public void setItemStyle(ItemStyleWithDecal itemStyle) {
        if (this.itemStyle != null) {
            removeChild(this.itemStyle);
        }

        this.itemStyle = itemStyle;
        addChild(itemStyle);
    }

    @Nullable
    public Emphasis getEmphasis() {
        return emphasis;
    }

    public void setEmphasis(Emphasis emphasis) {
        if (this.emphasis != null) {
            removeChild(this.emphasis);
        }

        this.emphasis = emphasis;
        addChild(emphasis);
    }

    @Nullable
    public Blur getBlur() {
        return blur;
    }

    public void setBlur(Blur blur) {
        if (this.blur != null) {
            removeChild(this.blur);
        }

        this.blur = blur;
        addChild(blur);
    }

    @Nullable
    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        if (this.select != null) {
            removeChild(this.select);
        }

        this.select = select;
        addChild(select);
    }

    @Nullable
    public String getBarWidth() {
        return barWidth;
    }

    public void setBarWidth(String barWidth) {
        this.barWidth = barWidth;
        markAsDirty();
    }

    @Nullable
    public String getBarMaxWidth() {
        return barMaxWidth;
    }

    public void setBarMaxWidth(String barMaxWidth) {
        this.barMaxWidth = barMaxWidth;
        markAsDirty();
    }

    @Nullable
    public String getBarMinWidth() {
        return barMinWidth;
    }

    public void setBarMinWidth(String barMinWidth) {
        this.barMinWidth = barMinWidth;
        markAsDirty();
    }

    @Nullable
    public Integer getBarMinHeight() {
        return barMinHeight;
    }

    public void setBarMinHeight(Integer barMinHeight) {
        this.barMinHeight = barMinHeight;
        markAsDirty();
    }

    @Nullable
    public Integer getBarMinAngle() {
        return barMinAngle;
    }

    public void setBarMinAngle(Integer barMinAngle) {
        this.barMinAngle = barMinAngle;
        markAsDirty();
    }

    @Nullable
    public String getBarGap() {
        return barGap;
    }

    public void setBarGap(String barGap) {
        this.barGap = barGap;
        markAsDirty();
    }

    @Nullable
    public String getBarCategoryGap() {
        return barCategoryGap;
    }

    public void setBarCategoryGap(String barCategoryGap) {
        this.barCategoryGap = barCategoryGap;
        markAsDirty();
    }

    @Nullable
    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
        markAsDirty();
    }

    @Nullable
    public Boolean getLarge() {
        return large;
    }

    public void setLarge(Boolean large) {
        this.large = large;
        markAsDirty();
    }

    @Nullable
    public Integer getLargeThreshold() {
        return largeThreshold;
    }

    public void setLargeThreshold(Integer largeThreshold) {
        this.largeThreshold = largeThreshold;
        markAsDirty();
    }

    @Nullable
    public Integer getProgressive() {
        return progressive;
    }

    public void setProgressive(Integer progressive) {
        this.progressive = progressive;
        markAsDirty();
    }

    @Nullable
    public Integer getProgressiveThreshold() {
        return progressiveThreshold;
    }

    public void setProgressiveThreshold(Integer progressiveThreshold) {
        this.progressiveThreshold = progressiveThreshold;
        markAsDirty();
    }

    @Nullable
    public ProgressiveChunkMode getProgressiveChunkMode() {
        return progressiveChunkMode;
    }

    public void setProgressiveChunkMode(ProgressiveChunkMode progressiveChunkMode) {
        this.progressiveChunkMode = progressiveChunkMode;
        markAsDirty();
    }

    @Nullable
    public Boolean getAnimation() {
        return animation;
    }

    public void setAnimation(Boolean animation) {
        this.animation = animation;
        markAsDirty();
    }

    @Nullable
    public Integer getAnimationThreshold() {
        return animationThreshold;
    }

    public void setAnimationThreshold(Integer animationThreshold) {
        this.animationThreshold = animationThreshold;
        markAsDirty();
    }

    @Nullable
    public Integer getAnimationDurationUpdate() {
        return animationDurationUpdate;
    }

    public void setAnimationDurationUpdate(Integer animationDurationUpdate) {
        this.animationDurationUpdate = animationDurationUpdate;
        markAsDirty();
    }

    @Nullable
    public String getAnimationEasingUpdate() {
        return animationEasingUpdate;
    }

    public void setAnimationEasingUpdate(String animationEasingUpdate) {
        this.animationEasingUpdate = animationEasingUpdate;
        markAsDirty();
    }

    @Nullable
    public Integer getAnimationDelayUpdate() {
        return animationDelayUpdate;
    }

    public void setAnimationDelayUpdate(Integer animationDelayUpdate) {
        this.animationDelayUpdate = animationDelayUpdate;
        markAsDirty();
    }

    public BarSeries withCoordinateSystem(CoordinateSystem coordinateSystem) {
        setCoordinateSystem(coordinateSystem);
        return this;
    }

    public BarSeries withXAxisIndex(Integer xAxisIndex) {
        setXAxisIndex(xAxisIndex);
        return this;
    }

    public BarSeries withYAxisIndex(Integer yAxisIndex) {
        setYAxisIndex(yAxisIndex);
        return this;
    }

    public BarSeries withPolarIndex(Integer polarIndex) {
        setPolarIndex(polarIndex);
        return this;
    }

    public BarSeries withSampling(SamplingType sampling) {
        setSampling(sampling);
        return this;
    }

    public BarSeries withClip(Boolean clip) {
        setClip(clip);
        return this;
    }

    public BarSeries withRoundCap(Boolean roundCap) {
        setRoundCap(roundCap);
        return this;
    }

    public BarSeries withRealtimeSort(Boolean realtimeSort) {
        setRealtimeSort(realtimeSort);
        return this;
    }

    public BarSeries withShowBackground(Boolean showBackground) {
        setShowBackground(showBackground);
        return this;
    }

    public BarSeries withBackgroundStyle(BackgroundStyle backgroundStyle) {
        setBackgroundStyle(backgroundStyle);
        return this;
    }

    public BarSeries withLabelLine(LabelLine labelLine) {
        setLabelLine(labelLine);
        return this;
    }

    public BarSeries withItemStyle(ItemStyleWithDecal itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public BarSeries withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public BarSeries withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public BarSeries withSelect(Select select) {
        setSelect(select);
        return this;
    }

    public BarSeries withBarWidth(String barWidth) {
        setBarWidth(barWidth);
        return this;
    }

    public BarSeries withBarMaxWidth(String barMaxWidth) {
        setBarMaxWidth(barMaxWidth);
        return this;
    }

    public BarSeries withBarMinWidth(String barMinWidth) {
        setBarMinWidth(barMinWidth);
        return this;
    }

    public BarSeries withBarMinHeight(Integer barMinHeight) {
        setBarMinHeight(barMinHeight);
        return this;
    }

    public BarSeries withBarMinAngle(Integer barMinAngle) {
        setBarMinAngle(barMinAngle);
        return this;
    }

    public BarSeries withBarGap(String barGap) {
        setBarGap(barGap);
        return this;
    }

    public BarSeries withBarCategoryGap(String barCategoryGap) {
        setBarCategoryGap(barCategoryGap);
        return this;
    }

    public BarSeries withCursor(String cursor) {
        setCursor(cursor);
        return this;
    }

    public BarSeries withLarge(Boolean large) {
        setLarge(large);
        return this;
    }

    public BarSeries withLargeThreshold(Integer largeThreshold) {
        setLargeThreshold(largeThreshold);
        return this;
    }

    public BarSeries withProgressive(Integer progressive) {
        setProgressive(progressive);
        return this;
    }

    public BarSeries withProgressiveThreshold(Integer progressiveThreshold) {
        setProgressiveThreshold(progressiveThreshold);
        return this;
    }

    public BarSeries withProgressiveChunkMode(ProgressiveChunkMode progressiveChunkMode) {
        setProgressiveChunkMode(progressiveChunkMode);
        return this;
    }

    public BarSeries withAnimation(Boolean animation) {
        setAnimation(animation);
        return this;
    }

    public BarSeries withAnimationThreshold(Integer animationThreshold) {
        setAnimationThreshold(animationThreshold);
        return this;
    }

    public BarSeries withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return this;
    }

    public BarSeries withAnimationEasingUpdate(String animationEasingUpdate) {
        setAnimationEasingUpdate(animationEasingUpdate);
        return this;
    }

    public BarSeries withAnimationDelayUpdate(Integer animationDelayUpdate) {
        setAnimationDelayUpdate(animationDelayUpdate);
        return this;
    }
}
