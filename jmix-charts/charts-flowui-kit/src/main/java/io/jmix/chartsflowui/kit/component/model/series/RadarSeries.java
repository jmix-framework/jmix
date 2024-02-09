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
import io.jmix.chartsflowui.kit.component.model.shared.*;

/**
 * Radar chart is mainly used to show multi-variable data, such as the analysis
 * of a football player's varied attributes. More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#series-radar">RadarSeries documentation</a>
 */
public class RadarSeries extends AbstractSeries<RadarSeries>
        implements HasSymbols<RadarSeries> {

    protected Integer radarIndex;

    protected HasSymbols.Symbol symbol;

    protected Integer symbolSize;

    protected JsFunction symbolSizeFunction;

    protected Integer symbolRotate;

    protected Boolean symbolKeepAspect;

    protected String[] symbolOffset;

    protected ItemStyleWithDecal itemStyle;

    protected LineStyle lineStyle;

    protected AreaStyle areaStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected Select select;

    protected Boolean animation;

    protected Integer animationThreshold;

    protected Integer animationDurationUpdate;

    protected String animationEasingUpdate;

    protected Integer animationDelayUpdate;

    public RadarSeries() {
        super(SeriesType.RADAR);
    }

    /**
     * Component to configure area filling style.
     */
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

    /**
     * Component to configure the emphasis state.
     */
    public static class Emphasis extends AbstractRadarElement<Emphasis> {

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

    /**
     * Component to configure the blur state.
     */
    public static class Blur extends AbstractRadarElement<Blur> {
    }

    /**
     * Component to configure the selection state.
     */
    public static class Select extends AbstractRadarElement<Select> {

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
     * Base component for radar elements.
     *
     * @param <T> origin element class type
     */
    public static abstract class AbstractRadarElement<T extends AbstractRadarElement<T>>
            extends ChartObservableObject {

        protected ItemStyle itemStyle;

        protected Label label;

        protected LineStyle lineStyle;

        protected RadarSeries.AreaStyle areaStyle;

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

        public RadarSeries.AreaStyle getAreaStyle() {
            return areaStyle;
        }

        public void setAreaStyle(RadarSeries.AreaStyle areaStyle) {
            if (this.areaStyle != null) {
                removeChild(this.areaStyle);
            }

            this.areaStyle = areaStyle;
            addChild(areaStyle);
        }

        @SuppressWarnings("unchecked")
        public T withItemStyle(ItemStyle itemStyle) {
            setItemStyle(itemStyle);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withLabel(Label label) {
            setLabel(label);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withAreaStyle(RadarSeries.AreaStyle areaStyle) {
            setAreaStyle(areaStyle);
            return (T) this;
        }
    }

    public Integer getRadarIndex() {
        return radarIndex;
    }

    public void setRadarIndex(Integer radarIndex) {
        this.radarIndex = radarIndex;
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
        addChild(this.areaStyle);
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

    public RadarSeries withRadarIndex(Integer radarIndex) {
        setRadarIndex(radarIndex);
        return this;
    }

    public RadarSeries withItemStyle(ItemStyleWithDecal itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public RadarSeries withLineStyle(LineStyle lineStyle) {
        setLineStyle(lineStyle);
        return this;
    }

    public RadarSeries withAreaStyle(AreaStyle areaStyle) {
        setAreaStyle(areaStyle);
        return this;
    }

    public RadarSeries withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public RadarSeries withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public RadarSeries withSelect(Select select) {
        setSelect(select);
        return this;
    }

    public RadarSeries withAnimation(Boolean animation) {
        setAnimation(animation);
        return this;
    }

    public RadarSeries withAnimationThreshold(Integer animationThreshold) {
        setAnimationThreshold(animationThreshold);
        return this;
    }

    public RadarSeries withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return this;
    }

    public RadarSeries withAnimationEasingUpdate(String animationEasingUpdate) {
        setAnimationEasingUpdate(animationEasingUpdate);
        return this;
    }

    public RadarSeries withAnimationDelayUpdate(Integer animationDelayUpdate) {
        setAnimationDelayUpdate(animationDelayUpdate);
        return this;
    }
}
