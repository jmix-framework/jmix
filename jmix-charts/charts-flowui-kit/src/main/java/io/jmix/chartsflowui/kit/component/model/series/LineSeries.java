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
import io.jmix.chartsflowui.kit.component.model.HasShadow;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap;
import jakarta.annotation.Nullable;

/**
 * Line chart relates all the data points {@link LineSeries#symbol} by broken lines, which is used to show
 * the trend of data changing. It could be used in both rectangular and polar coordinate.<br/>
 * <b>Note</b>:  when {@link LineSeries#areaStyle} is set, area chart will be drawn.<br/>
 * <b>Note</b>: with {@link PiecewiseVisualMap} component, line chart / area chart can have different colors
 * on different sections.<br/>
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#series-line">LineSeries documentation</a>
 */
public class LineSeries extends AbstractAxisAwareSeries<LineSeries>
        implements HasSymbols<LineSeries>, HasStack<LineSeries> {

    protected CoordinateSystem coordinateSystem;

    protected Integer xAxisIndex;

    protected Integer yAxisIndex;

    protected Integer polarIndex;

    protected Boolean clip;

    protected SamplingType sampling;

    protected String stack;

    protected HasStack.StackStrategy stackStrategy;

    protected HasSymbols.Symbol symbol;

    protected Integer symbolSize;

    protected JsFunction symbolSizeFunction;

    protected Integer symbolRotate;

    protected Boolean symbolKeepAspect;

    protected String[] symbolOffset;

    protected Boolean showSymbol;

    protected Boolean showAllSymbol;

    protected EndLabel endLabel;

    protected LineStyle lineStyle;

    protected ItemStyle itemStyle;

    protected LabelLine labelLine;

    protected AreaStyle areaStyle;

    protected Step step;

    protected Boolean connectNulls;

    protected Boolean triggerLineEvent;

    protected String cursor;

    protected Emphasis emphasis;

    protected Blur blur;

    protected Select select;

    protected Double smooth;

    protected SmoothMonotoneType smoothMonotone;

    protected Boolean animation;

    protected Integer animationThreshold;

    protected Integer animationDurationUpdate;

    protected String animationEasingUpdate;

    protected Integer animationDelayUpdate;

    public LineSeries() {
        super(SeriesType.LINE);
    }

    /**
     * Type of step line.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-line.step">LineSeries.step</a>
     */
    public enum Step implements HasEnumId {
        START("start"),
        MIDDLE("middle"),
        END("end");

        private final String id;

        Step(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static Step fromId(String id) {
            for (Step at : Step.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    /**
     * Type for maintaining the monotonicity on XAxis or YAxis.
     */
    public enum SmoothMonotoneType implements HasEnumId {
        X("x"),
        Y("y");

        private final String id;

        SmoothMonotoneType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static SmoothMonotoneType fromId(String id) {
            for (SmoothMonotoneType at : SmoothMonotoneType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    /**
     * Label component on the end of line.
     */
    public static class EndLabel extends AbstractEnhancedLabel<EndLabel> {

        protected String formatter;

        protected JsFunction formatterFunction;

        protected Boolean valueAnimation;

        public String getFormatter() {
            return formatter;
        }

        public void setFormatter(String formatter) {
            this.formatter = formatter;
            markAsDirty();
        }

        public JsFunction getFormatterFunction() {
            return formatterFunction;
        }

        public void setFormatterFunction(JsFunction formatterFunction) {
            this.formatterFunction = formatterFunction;
            markAsDirty();
        }

        public void setFormatterFunction(String formatterFunction) {
            this.formatterFunction = new JsFunction(formatterFunction);
            markAsDirty();
        }

        public Boolean getValueAnimation() {
            return valueAnimation;
        }

        public void setValueAnimation(Boolean valueAnimation) {
            this.valueAnimation = valueAnimation;
            markAsDirty();
        }

        public EndLabel withFormatter(String formatter) {
            setFormatter(formatter);
            return this;
        }

        public EndLabel withFormatterFunction(JsFunction formatterFunction) {
            setFormatterFunction(formatterFunction);
            return this;
        }

        public EndLabel withFormatterFunction(String formatterFunction) {
            setFormatterFunction(formatterFunction);
            return this;
        }

        public EndLabel withValueAnimation(Boolean valueAnimation) {
            setValueAnimation(valueAnimation);
            return this;
        }
    }

    /**
     * The style of area. Converts a line chart to an area chart.
     */
    public static class AreaStyle extends ChartObservableObject
            implements HasShadow<AreaStyle> {

        protected Color color;

        protected Origin origin;

        protected Integer shadowBlur;

        protected Color shadowColor;

        protected Integer shadowOffsetX;

        protected Integer shadowOffsetY;

        protected Double opacity;

        public static class Origin {

            protected OriginType type;

            protected Double value;

            public Origin(Double value) {
                this.value = value;
            }

            public Origin(OriginType type) {
                this.type = type;
            }

            public enum OriginType implements HasEnumId {
                AUTO("auto"),
                START("start"),
                END("end");

                private final String id;

                OriginType(String id) {
                    this.id = id;
                }

                @Override
                public String getId() {
                    return id;
                }

                @Nullable
                public static OriginType fromId(String id) {
                    for (OriginType at : OriginType.values()) {
                        if (at.getId().equals(id)) {
                            return at;
                        }
                    }
                    return null;
                }
            }

            public OriginType getType() {
                return type;
            }

            public Double getValue() {
                return value;
            }
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            markAsDirty();
        }

        public Origin getOrigin() {
            return origin;
        }

        public void setOrigin(Origin.OriginType originType) {
            this.origin = new Origin(originType);
            markAsDirty();
        }

        public void setOrigin(Double value) {
            this.origin = new Origin(value);
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

        public AreaStyle withColor(Color color) {
            setColor(color);
            return this;
        }

        public AreaStyle withOrigin(Origin.OriginType originType) {
            setOrigin(originType);
            return this;
        }

        public AreaStyle withOrigin(Double value) {
            setOrigin(value);
            return this;
        }

        public AreaStyle withOpacity(Double opacity) {
            setOpacity(opacity);
            return this;
        }
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
    public static class Emphasis extends AbstractLineElement<Emphasis> {

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
    public static class Blur extends AbstractLineElement<Blur> {
    }

    /**
     * Component to configure the selection state.
     */
    public static class Select extends AbstractLineElement<Select> {

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
     * Base component for line elements.
     *
     * @param <T> origin element class type
     */
    public static abstract class AbstractLineElement<T extends AbstractLineElement<T>> extends ChartObservableObject {

        protected Label label;

        protected ElementLabelLine labelLine;

        protected io.jmix.chartsflowui.kit.component.model.shared.ItemStyle itemStyle;

        protected LineStyle lineStyle;

        protected AreaStyle areaStyle;

        protected EndLabel endLabel;

        public static class AreaStyle extends AbstractAreaStyle<AreaStyle> {

            protected Color color;

            public Color getColor() {
                return color;
            }

            public void setColor(Color color) {
                this.color = color;
                markAsDirty();
            }

            public AreaStyle withColor(Color color) {
                setColor(color);
                return this;
            }
        }

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

        public io.jmix.chartsflowui.kit.component.model.shared.ItemStyle getItemStyle() {
            return itemStyle;
        }

        public void setItemStyle(io.jmix.chartsflowui.kit.component.model.shared.ItemStyle itemStyle) {
            if (this.itemStyle != null) {
                removeChild(this.itemStyle);
            }

            this.itemStyle = itemStyle;
            addChild(itemStyle);
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

        public AreaStyle getAreaStyle() {
            return areaStyle;
        }

        public void setAreaStyle(AreaStyle areaStyle) {
            if (this.areaStyle != null) {
                removeChild(this.areaStyle);
            }

            this.areaStyle = areaStyle;
            addChild(areaStyle);
        }

        public EndLabel getEndLabel() {
            return endLabel;
        }

        public void setEndLabel(EndLabel endLabel) {
            if (this.endLabel != null) {
                removeChild(this.endLabel);
            }

            this.endLabel = endLabel;
            addChild(endLabel);
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
        public T withItemStyle(io.jmix.chartsflowui.kit.component.model.shared.ItemStyle itemStyle) {
            setItemStyle(itemStyle);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withAreaStyle(AreaStyle areaStyle) {
            setAreaStyle(areaStyle);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withEndLabel(EndLabel endLabel) {
            setEndLabel(endLabel);
            return (T) this;
        }
    }

    public static class ItemStyle extends AbstractBorderedTextStyle<ItemStyle> {

        protected Decal decal;

        public Decal getDecal() {
            return decal;
        }

        public void setDecal(Decal decal) {
            this.decal = decal;
            markAsDirty();
        }

        public ItemStyle withDecal(Decal decal) {
            setDecal(decal);
            return this;
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

    public Boolean getClip() {
        return clip;
    }

    public void setClip(Boolean clip) {
        this.clip = clip;
        markAsDirty();
    }

    public SamplingType getSampling() {
        return sampling;
    }

    public void setSampling(SamplingType sampling) {
        this.sampling = sampling;
        markAsDirty();
    }

    @Override
    public String getStack() {
        return stack;
    }

    @Override
    public void setStack(String stack) {
        this.stack = stack;
        markAsDirty();
    }

    @Override
    public StackStrategy getStackStrategy() {
        return stackStrategy;
    }

    @Override
    public void setStackStrategy(StackStrategy stackStrategy) {
        this.stackStrategy = stackStrategy;
        markAsDirty();
    }

    @Override
    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public void setSymbol(String icon) {
        this.symbol = new Symbol(icon);
        markAsDirty();
    }

    @Override
    public void setSymbol(SymbolType symbolType) {
        this.symbol = new Symbol(symbolType);
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

    public Boolean getShowSymbol() {
        return showSymbol;
    }

    public void setShowSymbol(Boolean showSymbol) {
        this.showSymbol = showSymbol;
        markAsDirty();
    }

    public Boolean getShowAllSymbol() {
        return showAllSymbol;
    }

    public void setShowAllSymbol(Boolean showAllSymbol) {
        this.showAllSymbol = showAllSymbol;
        markAsDirty();
    }

    public EndLabel getEndLabel() {
        return endLabel;
    }

    public void setEndLabel(EndLabel endLabel) {
        if (this.endLabel != null) {
            removeChild(this.endLabel);
        }

        this.endLabel = endLabel;
        addChild(endLabel);
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

    public AreaStyle getAreaStyle() {
        return areaStyle;
    }

    public void setAreaStyle(AreaStyle areaStyle) {
        if (this.areaStyle != null) {
            removeChild(this.areaStyle);
        }

        this.areaStyle = areaStyle;
        addChild(areaStyle);
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
        markAsDirty();
    }

    public Boolean getConnectNulls() {
        return connectNulls;
    }

    public void setConnectNulls(Boolean connectNulls) {
        this.connectNulls = connectNulls;
        markAsDirty();
    }

    public Boolean getTriggerLineEvent() {
        return triggerLineEvent;
    }

    public void setTriggerLineEvent(Boolean triggerLineEvent) {
        this.triggerLineEvent = triggerLineEvent;
        markAsDirty();
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
        markAsDirty();
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

    public Double getSmooth() {
        return smooth;
    }

    public void setSmooth(Double smooth) {
        this.smooth = smooth;
        markAsDirty();
    }

    public SmoothMonotoneType getSmoothMonotone() {
        return smoothMonotone;
    }

    public void setSmoothMonotone(SmoothMonotoneType smoothMonotone) {
        this.smoothMonotone = smoothMonotone;
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

    public LineSeries withCoordinateSystem(CoordinateSystem coordinateSystem) {
        setCoordinateSystem(coordinateSystem);
        return this;
    }

    public LineSeries withXAxisIndex(Integer xAxisIndex) {
        setXAxisIndex(xAxisIndex);
        return this;
    }

    public LineSeries withYAxisIndex(Integer yAxisIndex) {
        setYAxisIndex(yAxisIndex);
        return this;
    }

    public LineSeries withPolarIndex(Integer polarIndex) {
        setPolarIndex(polarIndex);
        return this;
    }

    public LineSeries withClip(Boolean clip) {
        setClip(clip);
        return this;
    }

    public LineSeries withSampling(SamplingType sampling) {
        setSampling(sampling);
        return this;
    }

    public LineSeries withShowSymbol(Boolean showSymbol) {
        setShowSymbol(showSymbol);
        return this;
    }

    public LineSeries withShowAllSymbol(Boolean showAllSymbol) {
        setShowAllSymbol(showAllSymbol);
        return this;
    }

    public LineSeries withEndLabel(EndLabel endLabel) {
        setEndLabel(endLabel);
        return this;
    }

    public LineSeries withLineStyle(LineStyle lineStyle) {
        setLineStyle(lineStyle);
        return this;
    }

    public LineSeries withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public LineSeries withLabelLine(LabelLine labelLine) {
        setLabelLine(labelLine);
        return this;
    }

    public LineSeries withAreaStyle(AreaStyle areaStyle) {
        setAreaStyle(areaStyle);
        return this;
    }

    public LineSeries withStep(Step step) {
        setStep(step);
        return this;
    }

    public LineSeries withConnectNulls(Boolean connectNulls) {
        setConnectNulls(connectNulls);
        return this;
    }

    public LineSeries withTriggerLineEvent(Boolean triggerLineEvent) {
        setTriggerLineEvent(triggerLineEvent);
        return this;
    }

    public LineSeries withCursor(String cursor) {
        setCursor(cursor);
        return this;
    }

    public LineSeries withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public LineSeries withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public LineSeries withSelect(Select select) {
        setSelect(select);
        return this;
    }

    public LineSeries withSmooth(Double smooth) {
        setSmooth(smooth);
        return this;
    }

    public LineSeries withSmoothMonotone(SmoothMonotoneType smoothMonotone) {
        setSmoothMonotone(smoothMonotone);
        return this;
    }

    public LineSeries withAnimation(Boolean animation) {
        setAnimation(animation);
        return this;
    }

    public LineSeries withAnimationThreshold(Integer animationThreshold) {
        setAnimationThreshold(animationThreshold);
        return this;
    }

    public LineSeries withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return this;
    }

    public LineSeries withAnimationEasingUpdate(String animationEasingUpdate) {
        setAnimationEasingUpdate(animationEasingUpdate);
        return this;
    }

    public LineSeries withAnimationDelayUpdate(Integer animationDelayUpdate) {
        setAnimationDelayUpdate(animationDelayUpdate);
        return this;
    }
}
