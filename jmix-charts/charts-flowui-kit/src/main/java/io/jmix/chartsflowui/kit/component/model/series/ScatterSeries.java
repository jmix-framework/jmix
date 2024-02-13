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
import io.jmix.chartsflowui.kit.component.model.shared.HasSymbols;
import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;
import io.jmix.chartsflowui.kit.component.model.shared.JsFunction;
import io.jmix.chartsflowui.kit.component.model.shared.LineStyle;

/**
 * Scatter (bubble) chart. The scatter series in rectangular coordinate could be used to present
 * the relation between {@code X} and {@code Y}. More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#series-scatter">ScatterSeries documenation</a>
 */
public class ScatterSeries extends AbstractAxisAwareSeries<ScatterSeries>
        implements HasSymbols<ScatterSeries> {

    protected CoordinateSystem coordinateSystem;

    protected Integer xAxisIndex;

    protected Integer yAxisIndex;

    protected Integer polarIndex;

    protected Integer geoIndex;

    protected Integer calendarIndex;

    protected Boolean clip;

    protected HasSymbols.Symbol symbol;

    protected Integer symbolSize;

    protected JsFunction symbolSizeFunction;

    protected Integer symbolRotate;

    protected Boolean symbolKeepAspect;

    protected String[] symbolOffset;

    protected String cursor;

    protected Boolean large;

    protected Integer largeThreshold;

    protected LabelLine labelLine;

    protected ItemStyle itemStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected Select select;

    protected Integer progressive;

    protected Integer progressiveThreshold;

    protected Boolean animation;

    protected Integer animationThreshold;

    protected Integer animationDurationUpdate;

    protected String animationEasingUpdate;

    protected Integer animationDelayUpdate;

    public ScatterSeries() {
        super(SeriesType.SCATTER);
    }

    /**
     * Component to configure label guide line.
     */
    public static class LabelLine extends ChartObservableObject {

        protected Boolean show;

        protected Boolean showAbove;

        protected Integer length;

        protected Boolean smooth;

        protected Integer minTurnAngle;

        protected LineStyle lineStyle;

        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        public Boolean getShowAbove() {
            return showAbove;
        }

        public void setShowAbove(Boolean showAbove) {
            this.showAbove = showAbove;
            markAsDirty();
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
            markAsDirty();
        }

        public Boolean getSmooth() {
            return smooth;
        }

        public void setSmooth(Boolean smooth) {
            this.smooth = smooth;
            markAsDirty();
        }

        public Integer getMinTurnAngle() {
            return minTurnAngle;
        }

        public void setMinTurnAngle(Integer minTurnAngle) {
            this.minTurnAngle = minTurnAngle;
            markAsDirty();
        }

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

        public LabelLine withShowAbove(Boolean showAbove) {
            setShowAbove(showAbove);
            return this;
        }

        public LabelLine withLength(Integer length) {
            setLength(length);
            return this;
        }

        public LabelLine withSmooth(Boolean smooth) {
            setSmooth(smooth);
            return this;
        }

        public LabelLine withMinTurnAngle(Integer minTurnAngle) {
            setMinTurnAngle(minTurnAngle);
            return this;
        }

        public LabelLine withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return this;
        }
    }

    /**
     * Component to configure the emphasis state.
     */
    public static class Emphasis extends AbstractScatterElement<Emphasis> {

        protected Boolean disabled;

        protected Double scale;

        protected FocusType focus;

        protected BlurScopeType blurScope;

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
            markAsDirty();
        }

        public Double getScale() {
            return scale;
        }

        public void setScale(Double scale) {
            this.scale = scale;
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

        public Emphasis withScale(Double scale) {
            setScale(scale);
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
    public static class Blur extends AbstractScatterElement<Blur> {
    }

    /**
     * Component to configure the selection state.
     */
    public static class Select extends AbstractScatterElement<Select> {

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

    /**
     * Base component for scatter elements.
     *
     * @param <T> origin element class type
     */
    public static abstract class AbstractScatterElement<T extends AbstractScatterElement<T>>
            extends ChartObservableObject {

        protected Label label;

        protected ElementLabelLine labelLine;

        protected ItemStyle itemStyle;

        public Label getLabel() {
            return label;
        }

        public void setLabel(Label label) {
            if (this.label != null) {
                removeChild(this.label);
            }

            this.label = label;
            addChild(label);
        }

        public ElementLabelLine getLabelLine() {
            return labelLine;
        }

        public void setLabelLine(ElementLabelLine labelLine) {
            if (this.labelLine != null) {
                removeChild(this.labelLine);
            }

            this.labelLine = labelLine;
            addChild(labelLine);
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

        @SuppressWarnings("unchecked")
        public T withLabel(Label label) {
            setLabel(label);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withLabelLine(ElementLabelLine labelLine) {
            setLabelLine(labelLine);
            return (T) this;
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

    public Integer getPolarIndex() {
        return polarIndex;
    }

    public void setPolarIndex(Integer polarIndex) {
        this.polarIndex = polarIndex;
        markAsDirty();
    }

    public Integer getGeoIndex() {
        return geoIndex;
    }

    public void setGeoIndex(Integer geoIndex) {
        this.geoIndex = geoIndex;
        markAsDirty();
    }

    public Integer getCalendarIndex() {
        return calendarIndex;
    }

    public void setCalendarIndex(Integer calendarIndex) {
        this.calendarIndex = calendarIndex;
        markAsDirty();
    }

    public Boolean getClip() {
        return clip;
    }

    public void setClip(Boolean clip) {
        this.clip = clip;
        markAsDirty();
    }

    @Override
    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public void setSymbol(SymbolType symbolType) {
        this.symbol = new Symbol(symbolType);
        markAsDirty();
    }

    @Override
    public void setSymbol(String icon) {
        this.symbol = new Symbol(icon);
        markAsDirty();
    }

    @Override
    public Integer getSymbolSize() {
        return symbolSize;
    }

    @Override
    public void setSymbolSize(Integer symbolSize) {
        this.symbolSize = symbolSize;
        markAsDirty();
    }

    @Override
    public void setSymbolSizeFunction(String symbolSizeFunction) {
        this.symbolSizeFunction = new JsFunction(symbolSizeFunction);
        markAsDirty();
    }

    @Override
    public JsFunction getSymbolSizeFunction() {
        return symbolSizeFunction;
    }

    @Override
    public void setSymbolSizeFunction(JsFunction symbolSizeFunction) {
        this.symbolSizeFunction = symbolSizeFunction;
        markAsDirty();
    }

    @Override
    public Integer getSymbolRotate() {
        return symbolRotate;
    }

    @Override
    public void setSymbolRotate(Integer symbolRotate) {
        this.symbolRotate = symbolRotate;
        markAsDirty();
    }

    @Override
    public Boolean getSymbolKeepAspect() {
        return symbolKeepAspect;
    }

    @Override
    public void setSymbolKeepAspect(Boolean symbolKeepAspect) {
        this.symbolKeepAspect = symbolKeepAspect;
        markAsDirty();
    }

    @Override
    public String[] getSymbolOffset() {
        return symbolOffset;
    }

    @Override
    public void setSymbolOffset(String xOffset, String yOffset) {
        this.symbolOffset = new String[]{xOffset, yOffset};
        markAsDirty();
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
        markAsDirty();
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

    public Boolean getAnimation() {
        return animation;
    }

    public void setAnimation(Boolean animation) {
        this.animation = animation;
        markAsDirty();
    }

    public Integer getAnimationThreshold() {
        return animationThreshold;
    }

    public void setAnimationThreshold(Integer animationThreshold) {
        this.animationThreshold = animationThreshold;
        markAsDirty();
    }

    public Integer getAnimationDurationUpdate() {
        return animationDurationUpdate;
    }

    public void setAnimationDurationUpdate(Integer animationDurationUpdate) {
        this.animationDurationUpdate = animationDurationUpdate;
        markAsDirty();
    }

    public String getAnimationEasingUpdate() {
        return animationEasingUpdate;
    }

    public void setAnimationEasingUpdate(String animationEasingUpdate) {
        this.animationEasingUpdate = animationEasingUpdate;
        markAsDirty();
    }

    public Integer getAnimationDelayUpdate() {
        return animationDelayUpdate;
    }

    public void setAnimationDelayUpdate(Integer animationDelayUpdate) {
        this.animationDelayUpdate = animationDelayUpdate;
        markAsDirty();
    }

    public ScatterSeries withCoordinateSystem(CoordinateSystem coordinateSystem) {
        setCoordinateSystem(coordinateSystem);
        return this;
    }

    public ScatterSeries withXAxisIndex(Integer xAxisIndex) {
        setXAxisIndex(xAxisIndex);
        return this;
    }

    public ScatterSeries withYAxisIndex(Integer yAxisIndex) {
        setYAxisIndex(yAxisIndex);
        return this;
    }

    public ScatterSeries withPolarIndex(Integer polarIndex) {
        setPolarIndex(polarIndex);
        return this;
    }

    public ScatterSeries withGeoIndex(Integer geoIndex) {
        setGeoIndex(geoIndex);
        return this;
    }

    public ScatterSeries withCalendarIndex(Integer calendarIndex) {
        setCalendarIndex(calendarIndex);
        return this;
    }

    public ScatterSeries withClip(Boolean clip) {
        setClip(clip);
        return this;
    }

    public ScatterSeries withCursor(String cursor) {
        setCursor(cursor);
        return this;
    }

    public ScatterSeries withLarge(Boolean large) {
        setLarge(large);
        return this;
    }

    public ScatterSeries withLargeThreshold(Integer largeThreshold) {
        setLargeThreshold(largeThreshold);
        return this;
    }

    public ScatterSeries withLabelLine(LabelLine labelLine) {
        setLabelLine(labelLine);
        return this;
    }

    public ScatterSeries withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public ScatterSeries withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public ScatterSeries withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public ScatterSeries withSelect(Select select) {
        setSelect(select);
        return this;
    }

    public ScatterSeries withProgressive(Integer progressive) {
        setProgressive(progressive);
        return this;
    }

    public ScatterSeries withProgressiveThreshold(Integer progressiveThreshold) {
        setProgressiveThreshold(progressiveThreshold);
        return this;
    }

    public ScatterSeries withAnimation(Boolean animation) {
        setAnimation(animation);
        return this;
    }

    public ScatterSeries withAnimationThreshold(Integer animationThreshold) {
        setAnimationThreshold(animationThreshold);
        return this;
    }

    public ScatterSeries withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return this;
    }

    public ScatterSeries withAnimationEasingUpdate(String animationEasingUpdate) {
        setAnimationEasingUpdate(animationEasingUpdate);
        return this;
    }

    public ScatterSeries withAnimationDelayUpdate(Integer animationDelayUpdate) {
        setAnimationDelayUpdate(animationDelayUpdate);
        return this;
    }
}
