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
import io.jmix.chartsflowui.kit.component.model.HasShadow;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.Orientation;

public class CandlestickSeries extends AbstractAxisAwareSeries<CandlestickSeries> {

    protected CoordinateSystem coordinateSystem;

    protected Integer xAxisIndex;

    protected Integer yAxisIndex;

    protected Boolean hoverAnimation;

    protected Orientation layout;

    protected String barWidth;

    protected String barMaxWidth;

    protected String barMinWidth;

    protected ItemStyle itemStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected Select select;

    protected Boolean large;

    protected Integer largeThreshold;

    protected Integer progressive;

    protected Integer progressiveThreshold;

    protected ProgressiveChunkMode progressiveChunkMode;

    protected Boolean clip;

    public CandlestickSeries() {
        super(SeriesType.CANDLESTICK);
    }

    public static class ItemStyle extends ChartObservableObject
            implements HasShadow<ItemStyle> {

        protected Color bullishColor;

        protected Color bearishColor;

        protected Color bullishBorderColor;

        protected Color bearishBorderColor;

        protected Color dojiBorderColor;

        protected Double borderWidth;

        protected Integer shadowBlur;

        protected Color shadowColor;

        protected Integer shadowOffsetX;

        protected Integer shadowOffsetY;

        protected Double opacity;

        public Color getBullishColor() {
            return bullishColor;
        }

        public void setBullishColor(Color bullishColor) {
            this.bullishColor = bullishColor;
            markAsDirty();
        }

        public Color getBearishColor() {
            return bearishColor;
        }

        public void setBearishColor(Color bearishColor) {
            this.bearishColor = bearishColor;
            markAsDirty();
        }

        public Color getBullishBorderColor() {
            return bullishBorderColor;
        }

        public void setBullishBorderColor(Color bullishBorderColor) {
            this.bullishBorderColor = bullishBorderColor;
            markAsDirty();
        }

        public Color getBearishBorderColor() {
            return bearishBorderColor;
        }

        public void setBearishBorderColor(Color bearishBorderColor) {
            this.bearishBorderColor = bearishBorderColor;
            markAsDirty();
        }

        public Color getDojiBorderColor() {
            return dojiBorderColor;
        }

        public void setDojiBorderColor(Color dojiBorderColor) {
            this.dojiBorderColor = dojiBorderColor;
            markAsDirty();
        }

        public Double getBorderWidth() {
            return borderWidth;
        }

        public void setBorderWidth(Double borderWidth) {
            this.borderWidth = borderWidth;
            markAsDirty();
        }

        @Override
        public Integer getShadowBlur() {
            return shadowBlur;
        }

        @Override
        public void setShadowBlur(Integer shadowBlur) {
            this.shadowBlur = shadowBlur;
            markAsDirty();
        }

        @Override
        public Color getShadowColor() {
            return shadowColor;
        }

        @Override
        public void setShadowColor(Color shadowColor) {
            this.shadowColor = shadowColor;
            markAsDirty();
        }

        @Override
        public Integer getShadowOffsetX() {
            return shadowOffsetX;
        }

        @Override
        public void setShadowOffsetX(Integer shadowOffsetX) {
            this.shadowOffsetX = shadowOffsetX;
            markAsDirty();
        }

        @Override
        public Integer getShadowOffsetY() {
            return shadowOffsetY;
        }

        @Override
        public void setShadowOffsetY(Integer shadowOffsetY) {
            this.shadowOffsetY = shadowOffsetY;
            markAsDirty();
        }

        public Double getOpacity() {
            return opacity;
        }

        public void setOpacity(Double opacity) {
            this.opacity = opacity;
            markAsDirty();
        }

        public ItemStyle withBullishColor(Color bullishColor) {
            setBullishColor(bullishColor);
            return this;
        }

        public ItemStyle withBearishColor(Color bearishColor) {
            setBearishColor(bearishColor);
            return this;
        }

        public ItemStyle withBullishBorderColor(Color bullishBorderColor) {
            setBullishBorderColor(bullishBorderColor);
            return this;
        }

        public ItemStyle withBearishBorderColor(Color bearishBorderColor) {
            setBearishBorderColor(bearishBorderColor);
            return this;
        }

        public ItemStyle withDojiBorderColor(Color dojiBorderColor) {
            setDojiBorderColor(dojiBorderColor);
            return this;
        }

        public ItemStyle withBorderWidth(Double borderWidth) {
            setBorderWidth(borderWidth);
            return this;
        }

        public ItemStyle withOpacity(Double opacity) {
            setOpacity(opacity);
            return this;
        }
    }

    public static class Emphasis extends AbstractCandlestickElement<Emphasis> {

        protected Boolean disabled;

        protected FocusType focus;

        protected BlurScopeType blurScope;

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
            markAsDirty();
        }

        public FocusType getFocus() {
            return focus;
        }

        public void setFocus(FocusType focus) {
            this.focus = focus;
            markAsDirty();
        }

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

    public static class Blur extends AbstractCandlestickElement<Blur> {
    }

    public static class Select extends AbstractCandlestickElement<Select> {

        protected Boolean disabled;

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

    public static abstract class AbstractCandlestickElement<T extends AbstractCandlestickElement<T>>
            extends ChartObservableObject {

        protected ItemStyle itemStyle;

        public ItemStyle getItemStyle() {
            return itemStyle;
        }

        public void setItemStyle(ItemStyle itemStyle) {
            if (this.itemStyle != null) {
                removeChild(this.itemStyle);
            }

            this.itemStyle = itemStyle;
            addChild(itemStyle);
        }

        @SuppressWarnings("unchecked")
        public T withItemStyle(ItemStyle itemStyle) {
            setItemStyle(itemStyle);
            return (T) this;
        }
    }

    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    public void setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        markAsDirty();
    }

    public Integer getXAxisIndex() {
        return xAxisIndex;
    }

    public void setXAxisIndex(Integer xAxisIndex) {
        this.xAxisIndex = xAxisIndex;
        markAsDirty();
    }

    public Integer getYAxisIndex() {
        return yAxisIndex;
    }

    public void setYAxisIndex(Integer yAxisIndex) {
        this.yAxisIndex = yAxisIndex;
        markAsDirty();
    }

    public Boolean getHoverAnimation() {
        return hoverAnimation;
    }

    public void setHoverAnimation(Boolean hoverAnimation) {
        this.hoverAnimation = hoverAnimation;
        markAsDirty();
    }

    public Orientation getLayout() {
        return layout;
    }

    public void setLayout(Orientation layout) {
        this.layout = layout;
        markAsDirty();
    }

    public String getBarWidth() {
        return barWidth;
    }

    public void setBarWidth(String barWidth) {
        this.barWidth = barWidth;
        markAsDirty();
    }

    public String getBarMaxWidth() {
        return barMaxWidth;
    }

    public void setBarMaxWidth(String barMaxWidth) {
        this.barMaxWidth = barMaxWidth;
        markAsDirty();
    }

    public String getBarMinWidth() {
        return barMinWidth;
    }

    public void setBarMinWidth(String barMinWidth) {
        this.barMinWidth = barMinWidth;
        markAsDirty();
    }

    public ItemStyle getItemStyle() {
        return itemStyle;
    }

    public void setItemStyle(ItemStyle itemStyle) {
        if (this.itemStyle != null) {
            removeChild(this.itemStyle);
        }

        this.itemStyle = itemStyle;
        addChild(itemStyle);
    }

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

    public Boolean getLarge() {
        return large;
    }

    public void setLarge(Boolean large) {
        this.large = large;
        markAsDirty();
    }

    public Integer getLargeThreshold() {
        return largeThreshold;
    }

    public void setLargeThreshold(Integer largeThreshold) {
        this.largeThreshold = largeThreshold;
        markAsDirty();
    }

    public Integer getProgressive() {
        return progressive;
    }

    public void setProgressive(Integer progressive) {
        this.progressive = progressive;
        markAsDirty();
    }

    public Integer getProgressiveThreshold() {
        return progressiveThreshold;
    }

    public void setProgressiveThreshold(Integer progressiveThreshold) {
        this.progressiveThreshold = progressiveThreshold;
        markAsDirty();
    }

    public ProgressiveChunkMode getProgressiveChunkMode() {
        return progressiveChunkMode;
    }

    public void setProgressiveChunkMode(ProgressiveChunkMode progressiveChunkMode) {
        this.progressiveChunkMode = progressiveChunkMode;
        markAsDirty();
    }

    public Boolean getClip() {
        return clip;
    }

    public void setClip(Boolean clip) {
        this.clip = clip;
        markAsDirty();
    }

    public CandlestickSeries withCoordinateSystem(CoordinateSystem coordinateSystem) {
        setCoordinateSystem(coordinateSystem);
        return this;
    }

    public CandlestickSeries withXAxisIndex(Integer xAxisIndex) {
        setXAxisIndex(xAxisIndex);
        return this;
    }

    public CandlestickSeries withYAxisIndex(Integer yAxisIndex) {
        setYAxisIndex(yAxisIndex);
        return this;
    }

    public CandlestickSeries withHoverAnimation(Boolean hoverAnimation) {
        setHoverAnimation(hoverAnimation);
        return this;
    }

    public CandlestickSeries withLayout(Orientation layout) {
        setLayout(layout);
        return this;
    }

    public CandlestickSeries withBarWidth(String barWidth) {
        setBarWidth(barWidth);
        return this;
    }

    public CandlestickSeries withBarMaxWidth(String barMaxWidth) {
        setBarMaxWidth(barMaxWidth);
        return this;
    }

    public CandlestickSeries withBarMinWidth(String barMinWidth) {
        setBarMinWidth(barMinWidth);
        return this;
    }

    public CandlestickSeries withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public CandlestickSeries withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public CandlestickSeries withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public CandlestickSeries withSelect(Select select) {
        setSelect(select);
        return this;
    }

    public CandlestickSeries withLarge(Boolean large) {
        setLarge(large);
        return this;
    }

    public CandlestickSeries withLargeThreshold(Integer largeThreshold) {
        setLargeThreshold(largeThreshold);
        return this;
    }

    public CandlestickSeries withProgressive(Integer progressive) {
        setProgressive(progressive);
        return this;
    }

    public CandlestickSeries withProgressiveThreshold(Integer progressiveThreshold) {
        setProgressiveThreshold(progressiveThreshold);
        return this;
    }

    public CandlestickSeries withProgressiveChunkMode(ProgressiveChunkMode progressiveChunkMode) {
        setProgressiveChunkMode(progressiveChunkMode);
        return this;
    }

    public CandlestickSeries withClip(Boolean clip) {
        setClip(clip);
        return this;
    }
}
