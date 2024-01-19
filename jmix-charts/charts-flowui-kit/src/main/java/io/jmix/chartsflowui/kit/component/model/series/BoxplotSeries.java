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
import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;
import io.jmix.chartsflowui.kit.component.model.shared.Orientation;

public class BoxplotSeries extends AbstractAxisAwareSeries<BoxplotSeries> {

    protected CoordinateSystem coordinateSystem;

    protected Integer xAxisIndex;

    protected Integer yAxisIndex;

    protected Boolean hoverAnimation;

    protected Orientation layout;

    protected String[] boxWidth;

    protected ItemStyle itemStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected Select select;

    public BoxplotSeries() {
        super(SeriesType.BOXPLOT);
    }

    public static class Emphasis extends AbstractBoxplotElement<Emphasis> {

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

    public static class Blur extends AbstractBoxplotElement<Blur> {
    }

    public static class Select extends AbstractBoxplotElement<Select> {

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

    public static abstract class AbstractBoxplotElement<T extends AbstractBoxplotElement<T>>
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

    public String[] getBoxWidth() {
        return boxWidth;
    }

    public void setBoxWidth(String min, String max) {
        this.boxWidth = new String[]{min, max};
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

    public BoxplotSeries withCoordinateSystem(CoordinateSystem coordinateSystem) {
        setCoordinateSystem(coordinateSystem);
        return this;
    }

    public BoxplotSeries withXAxisIndex(Integer xAxisIndex) {
        setXAxisIndex(xAxisIndex);
        return this;
    }

    public BoxplotSeries withYAxisIndex(Integer yAxisIndex) {
        setYAxisIndex(yAxisIndex);
        return this;
    }

    public BoxplotSeries withHoverAnimation(Boolean hoverAnimation) {
        setHoverAnimation(hoverAnimation);
        return this;
    }

    public BoxplotSeries withLayout(Orientation layout) {
        setLayout(layout);
        return this;
    }

    public BoxplotSeries withBoxWidth(String min, String max) {
        setBoxWidth(min, max);
        return this;
    }

    public BoxplotSeries withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public BoxplotSeries withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public BoxplotSeries withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public BoxplotSeries withSelect(Select select) {
        setSelect(select);
        return this;
    }
}
