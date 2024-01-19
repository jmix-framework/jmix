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
import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;
import io.jmix.chartsflowui.kit.component.model.shared.LineStyle;
import jakarta.annotation.Nullable;

public class PieSeries extends AbstractAxisAwareSeries<PieSeries>
        implements HasPosition<PieSeries> {

    protected Integer geoIndex;

    protected Integer calendarIndex;

    protected Integer selectedOffset;

    protected Boolean clockwise;

    protected Integer startAngle;

    protected Integer minAngle;

    protected Integer minShowLabelAngle;

    protected RoseType roseType;

    protected Boolean avoidLabelOverlap;

    protected Boolean stillShowZeroSum;

    protected Integer percentPrecision;

    protected String left;

    protected String top;

    protected String right;

    protected String bottom;

    protected String width;

    protected String height;

    protected String cursor;

    protected Boolean showEmptyCircle;

    protected ItemStyle emptyCircleStyle;

    protected LabelLine labelLine;

    protected ItemStyle itemStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected Selected selected;

    protected String[] center;

    protected String[] radius;

    protected AnimationType animationType;

    protected AnimationUpdateType animationTypeUpdate;

    protected Boolean animation;

    protected Integer animationThreshold;

    protected Integer animationDurationUpdate;

    protected String animationEasingUpdate;

    protected Integer animationDelayUpdate;

    public PieSeries() {
        super(SeriesType.PIE);
    }

    public enum RoseType implements HasEnumId {
        RADIUS("radius"),
        AREA("area");

        private final String id;

        RoseType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static RoseType fromId(String id) {
            for (RoseType at : RoseType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public enum AnimationType implements HasEnumId {
        EXPANSION("expansion"),
        SCALE("scale");

        private final String id;

        AnimationType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static AnimationType fromId(String id) {
            for (AnimationType at : AnimationType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public enum AnimationUpdateType implements HasEnumId {
        TRANSITION("transition"),
        EXPANSION("expansion");

        private final String id;

        AnimationUpdateType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static AnimationUpdateType fromId(String id) {
            for (AnimationUpdateType at : AnimationUpdateType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public static class LabelLine extends ChartObservableObject {

        protected Boolean show;

        protected Boolean showAbove;

        protected Integer length;

        protected Integer length2;

        protected Boolean smooth;

        protected Integer minTurnAngle;

        protected LineStyle lineStyle;

        protected Integer maxSurfaceAngle;

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

        public Integer getLength2() {
            return length2;
        }

        public void setLength2(Integer length2) {
            this.length2 = length2;
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

        public Integer getMaxSurfaceAngle() {
            return maxSurfaceAngle;
        }

        public void setMaxSurfaceAngle(Integer maxSurfaceAngle) {
            this.maxSurfaceAngle = maxSurfaceAngle;
            markAsDirty();
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

        public LabelLine withLength2(Integer length2) {
            setLength2(length2);
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

        public LabelLine withMaxSurfaceAngle(Integer maxSurfaceAngle) {
            setMaxSurfaceAngle(maxSurfaceAngle);
            return this;
        }
    }

    public static class Emphasis extends AbstractPieElement<Emphasis> {

        protected Boolean disabled;

        protected Boolean scale;

        protected Integer scaleSize;

        protected FocusType focus;

        protected BlurScopeType blurScope;

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
            markAsDirty();
        }

        public Boolean getScale() {
            return scale;
        }

        public void setScale(Boolean scale) {
            this.scale = scale;
            markAsDirty();
        }

        public Integer getScaleSize() {
            return scaleSize;
        }

        public void setScaleSize(Integer scaleSize) {
            this.scaleSize = scaleSize;
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

        public Emphasis withScale(Boolean scale) {
            setScale(scale);
            return this;
        }

        public Emphasis withScaleSize(Integer scaleSize) {
            setScaleSize(scaleSize);
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

    public static class Blur extends AbstractPieElement<Blur> {
    }

    public static class Selected extends AbstractPieElement<Selected> {

        protected Boolean disabled;

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
            markAsDirty();
        }

        public Selected withDisabled(Boolean disabled) {
            setDisabled(disabled);
            return this;
        }
    }

    public static abstract class AbstractPieElement<T extends AbstractPieElement<T>> extends ChartObservableObject {

        protected Label label;

        protected ElementLabelLine labelLine;

        protected ItemStyle itemStyle;

        public Label getLabel() {
            return label;
        }

        public void setLabel(Label label) {
            if (this.label != null) {
                removeChild(this.labelLine);
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

    public PieSeries withGeoIndex(Integer geoIndex) {
        setGeoIndex(geoIndex);
        return this;
    }

    public PieSeries withCalendarIndex(Integer calendarIndex) {
        setCalendarIndex(calendarIndex);
        return this;
    }

    public PieSeries withSelectedOffset(Integer selectedOffset) {
        setSelectedOffset(selectedOffset);
        return this;
    }

    public PieSeries withClockwise(Boolean clockwise) {
        setClockwise(clockwise);
        return this;
    }

    public PieSeries withStartAngle(Integer startAngle) {
        setStartAngle(startAngle);
        return this;
    }

    public PieSeries withMinAngle(Integer minAngle) {
        setMinAngle(minAngle);
        return this;
    }

    public PieSeries withMinShowLabelAngle(Integer minShowLabelAngle) {
        setMinShowLabelAngle(minShowLabelAngle);
        return this;
    }

    public PieSeries withRoseType(RoseType roseType) {
        setRoseType(roseType);
        return this;
    }

    public PieSeries withAvoidLabelOverlap(Boolean avoidLabelOverlap) {
        setAvoidLabelOverlap(avoidLabelOverlap);
        return this;
    }

    public PieSeries withStillShowZeroSum(Boolean stillShowZeroSum) {
        setStillShowZeroSum(stillShowZeroSum);
        return this;
    }

    public PieSeries withPercentPrecision(Integer percentPrecision) {
        setPercentPrecision(percentPrecision);
        return this;
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

    public Integer getSelectedOffset() {
        return selectedOffset;
    }

    public void setSelectedOffset(Integer selectedOffset) {
        this.selectedOffset = selectedOffset;
        markAsDirty();
    }

    public Boolean getClockwise() {
        return clockwise;
    }

    public void setClockwise(Boolean clockwise) {
        this.clockwise = clockwise;
        markAsDirty();
    }

    public Integer getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(Integer startAngle) {
        this.startAngle = startAngle;
        markAsDirty();
    }

    public Integer getMinAngle() {
        return minAngle;
    }

    public void setMinAngle(Integer minAngle) {
        this.minAngle = minAngle;
        markAsDirty();
    }

    public Integer getMinShowLabelAngle() {
        return minShowLabelAngle;
    }

    public void setMinShowLabelAngle(Integer minShowLabelAngle) {
        this.minShowLabelAngle = minShowLabelAngle;
        markAsDirty();
    }

    public RoseType getRoseType() {
        return roseType;
    }

    public void setRoseType(RoseType roseType) {
        this.roseType = roseType;
        markAsDirty();
    }

    public Boolean getAvoidLabelOverlap() {
        return avoidLabelOverlap;
    }

    public void setAvoidLabelOverlap(Boolean avoidLabelOverlap) {
        this.avoidLabelOverlap = avoidLabelOverlap;
        markAsDirty();
    }

    public Boolean getStillShowZeroSum() {
        return stillShowZeroSum;
    }

    public void setStillShowZeroSum(Boolean stillShowZeroSum) {
        this.stillShowZeroSum = stillShowZeroSum;
        markAsDirty();
    }

    public Integer getPercentPrecision() {
        return percentPrecision;
    }

    public void setPercentPrecision(Integer percentPrecision) {
        this.percentPrecision = percentPrecision;
        markAsDirty();
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

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
        markAsDirty();
    }

    public Boolean getShowEmptyCircle() {
        return showEmptyCircle;
    }

    public void setShowEmptyCircle(Boolean showEmptyCircle) {
        this.showEmptyCircle = showEmptyCircle;
        markAsDirty();
    }

    public ItemStyle getEmptyCircleStyle() {
        return emptyCircleStyle;
    }

    public void setEmptyCircleStyle(ItemStyle emptyCircleStyle) {
        if (this.emptyCircleStyle != null) {
            removeChild(this.emptyCircleStyle);
        }

        this.emptyCircleStyle = emptyCircleStyle;
        addChild(emptyCircleStyle);
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
        addChild(this.itemStyle);
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

    public Selected getSelected() {
        return selected;
    }

    public void setSelected(Selected selected) {
        if (this.selected != null) {
            removeChild(this.selected);
        }

        this.selected = selected;
        addChild(selected);
    }

    public String[] getCenter() {
        return center;
    }

    public void setCenter(String x, String y) {
        this.center = new String[]{x, y};
        markAsDirty();
    }

    public String[] getRadius() {
        return radius;
    }

    public void setRadius(String innerRadius, String outerRadius) {
        this.radius = new String[]{innerRadius, outerRadius};
        markAsDirty();
    }

    public AnimationType getAnimationType() {
        return animationType;
    }

    public void setAnimationType(AnimationType animationType) {
        this.animationType = animationType;
        markAsDirty();
    }

    public AnimationUpdateType getAnimationTypeUpdate() {
        return animationTypeUpdate;
    }

    public void setAnimationTypeUpdate(AnimationUpdateType animationTypeUpdate) {
        this.animationTypeUpdate = animationTypeUpdate;
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

    public PieSeries withWidth(String width) {
        setWidth(width);
        return this;
    }

    public PieSeries withHeight(String height) {
        setHeight(height);
        return this;
    }

    public PieSeries withCursor(String cursor) {
        setCursor(cursor);
        return this;
    }

    public PieSeries withShowEmptyCircle(Boolean showEmptyCircle) {
        setShowEmptyCircle(showEmptyCircle);
        return this;
    }

    public PieSeries withEmptyCircleStyle(ItemStyle emptyCircleStyle) {
        setEmptyCircleStyle(emptyCircleStyle);
        return this;
    }

    public PieSeries withLabelLine(LabelLine labelLine) {
        setLabelLine(labelLine);
        return this;
    }

    public PieSeries withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public PieSeries withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public PieSeries withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public PieSeries withSelected(Selected selected) {
        setSelected(selected);
        return this;
    }

    public PieSeries withCenter(String x, String y) {
        setCenter(x, y);
        return this;
    }

    public PieSeries withRadius(String innerRadius, String outerRadius) {
        setRadius(innerRadius, outerRadius);
        return this;
    }

    public PieSeries withAnimationType(AnimationType animationType) {
        setAnimationType(animationType);
        return this;
    }

    public PieSeries withAnimationTypeUpdate(AnimationUpdateType animationTypeUpdate) {
        setAnimationTypeUpdate(animationTypeUpdate);
        return this;
    }

    public PieSeries withAnimation(Boolean animation) {
        setAnimation(animation);
        return this;
    }

    public PieSeries withAnimationThreshold(Integer animationThreshold) {
        setAnimationThreshold(animationThreshold);
        return this;
    }

    public PieSeries withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return this;
    }

    public PieSeries withAnimationEasingUpdate(String animationEasingUpdate) {
        setAnimationEasingUpdate(animationEasingUpdate);
        return this;
    }

    public PieSeries withAnimationDelayUpdate(Integer animationDelayUpdate) {
        setAnimationDelayUpdate(animationDelayUpdate);
        return this;
    }
}
