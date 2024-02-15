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
import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import jakarta.annotation.Nullable;

/**
 * The scatter (bubble) graph with ripple animation. The special animation effect can visually highlight some data.
 * More detailed information is provided in the documentation.
 *
 * @see ScatterSeries
 * @see <a href="https://echarts.apache.org/en/option.html#series-effectScatter">EffectScatterSeries documentation</a>
 */
public class EffectScatterSeries extends AbstractAxisAwareSeries<EffectScatterSeries>
        implements HasSymbols<EffectScatterSeries> {

    protected CoordinateSystem coordinateSystem;

    protected String effectType;

    protected EffectOn showEffectOn;

    protected RippleEffect rippleEffect;

    protected Integer xAxisIndex;

    protected Integer yAxisIndex;

    protected Integer polarIndex;

    protected Integer geoIndex;

    protected Integer calendarIndex;

    protected Boolean clip;

    protected String cursor;

    protected HasSymbols.Symbol symbol;

    protected Integer symbolSize;

    protected JsFunction symbolSizeFunction;

    protected Integer symbolRotate;

    protected Boolean symbolKeepAspect;

    protected String[] symbolOffset;

    protected LabelLine labelLine;

    protected ItemStyle itemStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected Select select;

    protected Boolean animation;

    protected Integer animationThreshold;

    protected Integer animationDurationUpdate;

    protected String animationEasingUpdate;

    protected Integer animationDelayUpdate;

    public EffectScatterSeries() {
        super(SeriesType.EFFECT_SCATTER);
    }

    /**
     * Enum of when to show the effect.
     */
    public enum EffectOn implements HasEnumId {
        RENDER("render"),
        EMPHASIS("emphasis");

        private final String id;

        EffectOn(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static EffectOn fromId(String id) {
            for (EffectOn at : EffectOn.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    /**
     * Related configurations component about ripple effect.
     */
    public static class RippleEffect extends ChartObservableObject {

        protected Color color;

        protected Integer number;

        protected Integer period;

        protected Double scale;

        protected BrushType brushType;

        /**
         * The brush type for ripples.
         */
        public enum BrushType implements HasEnumId {
            STROKE("stroke"),
            FILL("fill");

            private final String id;

            BrushType(String id) {
                this.id = id;
            }

            @Override
            public String getId() {
                return id;
            }

            @Nullable
            public static BrushType fromId(String id) {
                for (BrushType at : BrushType.values()) {
                    if (at.getId().equals(id)) {
                        return at;
                    }
                }
                return null;
            }
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            markAsDirty();
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
            markAsDirty();
        }

        public Integer getPeriod() {
            return period;
        }

        public void setPeriod(Integer period) {
            this.period = period;
            markAsDirty();
        }

        public Double getScale() {
            return scale;
        }

        public void setScale(Double scale) {
            this.scale = scale;
            markAsDirty();
        }

        public BrushType getBrushType() {
            return brushType;
        }

        public void setBrushType(BrushType brushType) {
            this.brushType = brushType;
            markAsDirty();
        }

        public RippleEffect withColor(Color color) {
            setColor(color);
            return this;
        }

        public RippleEffect withNumber(Integer number) {
            setNumber(number);
            return this;
        }

        public RippleEffect withPeriod(Integer period) {
            setPeriod(period);
            return this;
        }

        public RippleEffect withScale(Double scale) {
            setScale(scale);
            return this;
        }

        public RippleEffect withBrushType(BrushType brushType) {
            setBrushType(brushType);
            return this;
        }
    }

    /**
     * Configuration of label guideline.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-effectScatter.labelLine">EffectScatterSeries.labelLine</a>
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
    public static class Emphasis extends AbstractEffectScatterElement<Emphasis> {

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
    public static class Blur extends AbstractEffectScatterElement<Blur> {
    }

    /**
     * Component to configure the selection state.
     */
    public static class Select extends AbstractEffectScatterElement<Select> {

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
     * Base component for effect scatter elements.
     *
     * @param <T> origin element class type
     */
    public static abstract class AbstractEffectScatterElement<T extends AbstractEffectScatterElement<T>>
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

    public String getEffectType() {
        return effectType;
    }

    public void setEffectType(String effectType) {
        this.effectType = effectType;
        markAsDirty();
    }

    public EffectOn getShowEffectOn() {
        return showEffectOn;
    }

    public void setShowEffectOn(EffectOn showEffectOn) {
        this.showEffectOn = showEffectOn;
        markAsDirty();
    }

    public RippleEffect getRippleEffect() {
        return rippleEffect;
    }

    public void setRippleEffect(RippleEffect rippleEffect) {
        if (this.rippleEffect != null) {
            removeChild(this.rippleEffect);
        }

        this.rippleEffect = rippleEffect;
        addChild(rippleEffect);
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

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
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
    public JsFunction getSymbolSizeFunction() {
        return symbolSizeFunction;
    }

    @Override
    public void setSymbolSizeFunction(JsFunction symbolSizeFunction) {
        this.symbolSizeFunction = symbolSizeFunction;
        markAsDirty();
    }

    @Override
    public void setSymbolSizeFunction(String symbolSizeFunction) {
        this.symbolSizeFunction = new JsFunction(symbolSizeFunction);
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

    public EffectScatterSeries withCoordinateSystem(CoordinateSystem coordinateSystem) {
        setCoordinateSystem(coordinateSystem);
        return this;
    }

    public EffectScatterSeries withEffectType(String effectType) {
        setEffectType(effectType);
        return this;
    }

    public EffectScatterSeries withShowEffectOn(EffectOn showEffectOn) {
        setShowEffectOn(showEffectOn);
        return this;
    }

    public EffectScatterSeries withRippleEffect(RippleEffect rippleEffect) {
        setRippleEffect(rippleEffect);
        return this;
    }

    public EffectScatterSeries withXAxisIndex(Integer xAxisIndex) {
        setXAxisIndex(xAxisIndex);
        return this;
    }

    public EffectScatterSeries withYAxisIndex(Integer yAxisIndex) {
        setYAxisIndex(yAxisIndex);
        return this;
    }

    public EffectScatterSeries withPolarIndex(Integer polarIndex) {
        setPolarIndex(polarIndex);
        return this;
    }

    public EffectScatterSeries withGeoIndex(Integer geoIndex) {
        setGeoIndex(geoIndex);
        return this;
    }

    public EffectScatterSeries withCalendarIndex(Integer calendarIndex) {
        setCalendarIndex(calendarIndex);
        return this;
    }

    public EffectScatterSeries withClip(Boolean clip) {
        setClip(clip);
        return this;
    }

    public EffectScatterSeries withCursor(String cursor) {
        setCursor(cursor);
        return this;
    }

    public EffectScatterSeries withLabelLine(LabelLine labelLine) {
        setLabelLine(labelLine);
        return this;
    }

    public EffectScatterSeries withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public EffectScatterSeries withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public EffectScatterSeries withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public EffectScatterSeries withSelect(Select select) {
        setSelect(select);
        return this;
    }

    public EffectScatterSeries withAnimation(Boolean animation) {
        setAnimation(animation);
        return this;
    }

    public EffectScatterSeries withAnimationThreshold(Integer animationThreshold) {
        setAnimationThreshold(animationThreshold);
        return this;
    }

    public EffectScatterSeries withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return this;
    }

    public EffectScatterSeries withAnimationEasingUpdate(String animationEasingUpdate) {
        setAnimationEasingUpdate(animationEasingUpdate);
        return this;
    }

    public EffectScatterSeries withAnimationDelayUpdate(Integer animationDelayUpdate) {
        setAnimationDelayUpdate(animationDelayUpdate);
        return this;
    }
}
