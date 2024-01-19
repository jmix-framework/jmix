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
import io.jmix.chartsflowui.kit.component.model.HasPosition;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import jakarta.annotation.Nullable;

public class FunnelSeries extends AbstractAxisAwareSeries<FunnelSeries>
        implements HasPosition<FunnelSeries> {

    protected Integer max;

    protected Integer min;

    protected String minSize;

    protected String maxSize;

    protected Orientation orientation;

    protected SortType sort;

    protected JsFunction sortFunction;

    protected Integer gap;

    protected Align funnelAlign;

    protected LabelLine labelLine;

    protected ItemStyle itemStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected Select select;

    protected String left;

    protected String top;

    protected String right;

    protected String bottom;

    protected String width;

    protected String height;

    protected Boolean animation;

    protected Integer animationThreshold;

    protected Integer animationDurationUpdate;

    protected String animationEasingUpdate;

    protected Integer animationDelayUpdate;

    public FunnelSeries() {
        super(SeriesType.FUNNEL);
    }

    public enum SortType implements HasEnumId {
        ASCENDING("ascending"),
        DESCENDING("descending"),
        NONE("none");

        private final String id;

        SortType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static SortType fromId(String id) {
            for (SortType at : SortType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public static class LabelLine extends ChartObservableObject {

        protected Boolean show;

        protected Integer length;

        protected LineStyle lineStyle;

        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
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

        public LabelLine withLength(Integer length) {
            setLength(length);
            return this;
        }

        public LabelLine withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return this;
        }
    }

    public static class Emphasis extends AbstractFunnelElement<Emphasis> {

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

    public static class Blur extends AbstractFunnelElement<Blur> {
    }

    public static class Select extends AbstractFunnelElement<Select> {

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

    public static abstract class AbstractFunnelElement<T extends AbstractFunnelElement<T>>
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

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
        markAsDirty();
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
        markAsDirty();
    }

    public String getMinSize() {
        return minSize;
    }

    public void setMinSize(String minSize) {
        this.minSize = minSize;
        markAsDirty();
    }

    public String getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
        markAsDirty();
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        markAsDirty();
    }

    public SortType getSort() {
        return sort;
    }

    public void setSort(SortType sort) {
        this.sort = sort;
        markAsDirty();
    }

    public JsFunction getSortFunction() {
        return sortFunction;
    }

    public void setSortFunction(JsFunction sortFunction) {
        this.sortFunction = sortFunction;
        markAsDirty();
    }

    public void setSortFunction(String sortFunction) {
        this.sortFunction = new JsFunction(sortFunction);
        markAsDirty();
    }

    public Integer getGap() {
        return gap;
    }

    public void setGap(Integer gap) {
        this.gap = gap;
        markAsDirty();
    }

    public Align getFunnelAlign() {
        return funnelAlign;
    }

    public void setFunnelAlign(Align funnelAlign) {
        this.funnelAlign = funnelAlign;
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

    @Override
    public String getLeft() {
        return left;
    }

    @Override
    public void setLeft(String left) {
        this.left = left;
        markAsDirty();
    }

    @Override
    public String getTop() {
        return top;
    }

    @Override
    public void setTop(String top) {
        this.top = top;
        markAsDirty();
    }

    @Override
    public String getRight() {
        return right;
    }

    @Override
    public void setRight(String right) {
        this.right = right;
        markAsDirty();
    }

    @Override
    public String getBottom() {
        return bottom;
    }

    @Override
    public void setBottom(String bottom) {
        this.bottom = bottom;
        markAsDirty();
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
        markAsDirty();
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
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

    public FunnelSeries withMax(Integer max) {
        setMax(max);
        return this;
    }

    public FunnelSeries withMin(Integer min) {
        setMin(min);
        return this;
    }

    public FunnelSeries withMinSize(String minSize) {
        setMinSize(minSize);
        return this;
    }

    public FunnelSeries withMaxSize(String maxSize) {
        setMaxSize(maxSize);
        return this;
    }

    public FunnelSeries withOrientation(Orientation orientation) {
        setOrientation(orientation);
        return this;
    }

    public FunnelSeries withSort(SortType sort) {
        setSort(sort);
        return this;
    }

    public FunnelSeries withSortFunction(JsFunction sortFunction) {
        setSortFunction(sortFunction);
        return this;
    }

    public FunnelSeries withSortFunction(String sortFunction) {
        setSortFunction(sortFunction);
        return this;
    }

    public FunnelSeries withGap(Integer gap) {
        setGap(gap);
        return this;
    }

    public FunnelSeries withFunnelAlign(Align funnelAlign) {
        setFunnelAlign(funnelAlign);
        return this;
    }

    public FunnelSeries withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public FunnelSeries withLabelLine(LabelLine labelLine) {
        setLabelLine(labelLine);
        return this;
    }

    public FunnelSeries withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public FunnelSeries withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public FunnelSeries withSelect(Select select) {
        setSelect(select);
        return this;
    }

    public FunnelSeries withWidth(String width) {
        setWidth(width);
        return this;
    }

    public FunnelSeries withHeight(String height) {
        setHeight(height);
        return this;
    }

    public FunnelSeries withAnimation(Boolean animation) {
        setAnimation(animation);
        return this;
    }

    public FunnelSeries withAnimationThreshold(Integer animationThreshold) {
        setAnimationThreshold(animationThreshold);
        return this;
    }

    public FunnelSeries withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return this;
    }

    public FunnelSeries withAnimationEasingUpdate(String animationEasingUpdate) {
        setAnimationEasingUpdate(animationEasingUpdate);
        return this;
    }

    public FunnelSeries withAnimationDelayUpdate(Integer animationDelayUpdate) {
        setAnimationDelayUpdate(animationDelayUpdate);
        return this;
    }
}
