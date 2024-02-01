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
import io.jmix.chartsflowui.kit.component.model.HasAlign;
import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.Position.ItemTriggerPosition;
import jakarta.annotation.Nullable;

/**
 * The base class for any series.
 *
 * @param <T> origin series class type
 */
public abstract class AbstractSeries<T extends AbstractSeries<T>> extends ChartObservableObject {

    protected SeriesType type;

    protected String id;

    protected String name;

    protected ColorBy colorBy;

    protected Label label;

    protected LabelLayout labelLayout;

    protected SelectedMode selectedMode;

    protected String dataGroupId;

    protected Integer zLevel;

    protected Integer z;

    protected Boolean silent;

    protected Integer animationDuration;

    protected String animationEasing;

    protected Integer animationDelay;

    protected Tooltip tooltip;

    /**
     * Unified layout configuration component of labels.
     */
    public static class LabelLayout extends ChartObservableObject
            implements HasAlign<LabelLayout> {

        protected Boolean hideOverlap;

        protected MoveOverlapPosition moveOverlap;

        protected String x;

        protected String y;

        protected Integer dx;

        protected Integer dy;

        protected Integer rotate;

        protected Integer width;

        protected Integer height;

        protected Align align;

        protected VerticalAlign verticalAlign;

        protected Integer fontSize;

        protected Boolean draggable;

        protected Integer[][] labelLinePoints;

        /**
         * Enum to move the overlapped labels to avoid overlapping.
         */
        public enum MoveOverlapPosition implements HasEnumId {
            SHIFT_X("shiftX"),
            SHIFT_Y("shiftY");

            private final String id;

            MoveOverlapPosition(String id) {
                this.id = id;
            }

            @Override
            public String getId() {
                return id;
            }

            @Nullable
            public static MoveOverlapPosition fromId(String id) {
                for (MoveOverlapPosition at : MoveOverlapPosition.values()) {
                    if (at.getId().equals(id)) {
                        return at;
                    }
                }
                return null;
            }
        }

        public Boolean getHideOverlap() {
            return hideOverlap;
        }

        public void setHideOverlap(Boolean hideOverlap) {
            this.hideOverlap = hideOverlap;
            markAsDirty();
        }

        public MoveOverlapPosition getMoveOverlap() {
            return moveOverlap;
        }

        public void setMoveOverlap(MoveOverlapPosition moveOverlap) {
            this.moveOverlap = moveOverlap;
            markAsDirty();
        }

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
            markAsDirty();
        }

        public String getY() {
            return y;
        }

        public void setY(String y) {
            this.y = y;
            markAsDirty();
        }

        public Integer getDx() {
            return dx;
        }

        public void setDx(Integer dx) {
            this.dx = dx;
            markAsDirty();
        }

        public Integer getDy() {
            return dy;
        }

        public void setDy(Integer dy) {
            this.dy = dy;
            markAsDirty();
        }

        public Integer getRotate() {
            return rotate;
        }

        public void setRotate(Integer rotate) {
            this.rotate = rotate;
            markAsDirty();
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
            markAsDirty();
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
            markAsDirty();
        }

        @Override
        public Align getAlign() {
            return align;
        }

        @Override
        public void setAlign(Align align) {
            this.align = align;
            markAsDirty();
        }

        @Override
        public VerticalAlign getVerticalAlign() {
            return verticalAlign;
        }

        @Override
        public void setVerticalAlign(VerticalAlign verticalAlign) {
            this.verticalAlign = verticalAlign;
            markAsDirty();
        }

        public Integer getFontSize() {
            return fontSize;
        }

        public void setFontSize(Integer fontSize) {
            this.fontSize = fontSize;
            markAsDirty();
        }

        public Boolean getDraggable() {
            return draggable;
        }

        public void setDraggable(Boolean draggable) {
            this.draggable = draggable;
            markAsDirty();
        }

        public Integer[][] getLabelLinePoints() {
            return labelLinePoints;
        }

        public void setLabelLinePoints(Integer x1, Integer y1, Integer x2, Integer y2, Integer x3, Integer y3) {
            this.labelLinePoints = new Integer[][]{{x1, y1}, {x2, y2}, {x3, y3}};
            markAsDirty();
        }

        public LabelLayout withHideOverlap(Boolean hideOverlap) {
            setHideOverlap(hideOverlap);
            return this;
        }

        public LabelLayout withMoveOverlap(MoveOverlapPosition moveOverlap) {
            setMoveOverlap(moveOverlap);
            return this;
        }

        public LabelLayout withX(String x) {
            setX(x);
            return this;
        }

        public LabelLayout withY(String y) {
            setY(y);
            return this;
        }

        public LabelLayout withDx(Integer dx) {
            setDx(dx);
            return this;
        }

        public LabelLayout withDy(Integer dy) {
            setDy(dy);
            return this;
        }

        public LabelLayout withRotate(Integer rotate) {
            setRotate(rotate);
            return this;
        }

        public LabelLayout withWidth(Integer width) {
            setWidth(width);
            return this;
        }

        public LabelLayout withHeight(Integer height) {
            setHeight(height);
            return this;
        }

        public LabelLayout withFontSize(Integer fontSize) {
            setFontSize(fontSize);
            return this;
        }

        public LabelLayout withDraggable(Boolean draggable) {
            setDraggable(draggable);
            return this;
        }

        public LabelLayout withLabelLinePoints(Integer x1, Integer y1, Integer x2, Integer y2, Integer x3, Integer y3) {
            setLabelLinePoints(x1, y1, x2, y2, x3, y3);
            return this;
        }
    }

    /**
     * Tooltip component for series.
     */
    public static class Tooltip extends ChartObservableObject
            implements HasPadding<Tooltip> {

        protected AbstractTooltip.Position position;

        protected String formatter;

        protected String valueFormatter;

        protected Color backgroundColor;

        protected Color borderColor;

        protected Integer borderWidth;

        protected Padding padding;

        protected TextStyle textStyle;

        protected String extraCssText;

        public AbstractTooltip.Position getPosition() {
            return position;
        }

        public void setPosition(String horizontalPosition, String verticalPosition) {
            this.position =
                    new AbstractTooltip.Position(horizontalPosition, verticalPosition);
            markAsDirty();
        }

        public void setPosition(ItemTriggerPosition itemTriggerPosition) {
            this.position =
                    new AbstractTooltip.Position(itemTriggerPosition);
            markAsDirty();
        }

        public String getFormatter() {
            return formatter;
        }

        public void setFormatter(String formatter) {
            this.formatter = formatter;
            markAsDirty();
        }

        public String getValueFormatter() {
            return valueFormatter;
        }

        public void setValueFormatter(String valueFormatter) {
            this.valueFormatter = valueFormatter;
            markAsDirty();
        }

        public Color getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            markAsDirty();
        }

        public Color getBorderColor() {
            return borderColor;
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            markAsDirty();
        }

        public Integer getBorderWidth() {
            return borderWidth;
        }

        public void setBorderWidth(Integer borderWidth) {
            this.borderWidth = borderWidth;
            markAsDirty();
        }

        @Override
        public Padding getPadding() {
            return padding;
        }

        @Override
        public void setPadding(Integer padding) {
            this.padding = new Padding(padding);
            markAsDirty();
        }

        @Override
        public void setPadding(Integer vertical, Integer horizontal) {
            this.padding = new Padding(vertical, horizontal);
            markAsDirty();
        }

        @Override
        public void setPadding(Integer top, Integer right, Integer bottom, Integer left) {
            this.padding = new Padding(top, right, bottom, left);
            markAsDirty();
        }

        public TextStyle getTextStyle() {
            return textStyle;
        }

        public void setTextStyle(TextStyle textStyle) {
            if (this.textStyle != null) {
                removeChild(this.textStyle);
            }

            this.textStyle = textStyle;
            addChild(textStyle);
        }

        public String getExtraCssText() {
            return extraCssText;
        }

        public void setExtraCssText(String extraCssText) {
            this.extraCssText = extraCssText;
            markAsDirty();
        }

        public Tooltip withPosition(String horizontalPosition, String verticalPosition) {
            setPosition(horizontalPosition, verticalPosition);
            return this;
        }

        public Tooltip withPosition(ItemTriggerPosition itemTriggerPosition) {
            setPosition(itemTriggerPosition);
            return this;
        }

        public Tooltip withFormatter(String formatter) {
            setFormatter(formatter);
            return this;
        }

        public Tooltip withValueFormatter(String valueFormatter) {
            setValueFormatter(valueFormatter);
            return this;
        }

        public Tooltip withBackgroundColor(Color backgroundColor) {
            setBackgroundColor(backgroundColor);
            return this;
        }

        public Tooltip withBorderColor(Color borderColor) {
            setBorderColor(borderColor);
            return this;
        }

        public Tooltip withBorderWidth(Integer borderWidth) {
            setBorderWidth(borderWidth);
            return this;
        }

        public Tooltip withTextStyle(TextStyle textStyle) {
            setTextStyle(textStyle);
            return this;
        }

        public Tooltip withExtraCssText(String extraCssText) {
            setExtraCssText(extraCssText);
            return this;
        }
    }

    protected AbstractSeries(SeriesType type) {
        this.type = type;
    }

    public SeriesType getType() {
        return type;
    }

    public void setType(SeriesType type) {
        this.type = type;
        markAsDirty();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        markAsDirty();
    }

    public ColorBy getColorBy() {
        return colorBy;
    }

    public void setColorBy(ColorBy colorBy) {
        this.colorBy = colorBy;
        markAsDirty();
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

    public LabelLayout getLabelLayout() {
        return labelLayout;
    }

    public void setLabelLayout(LabelLayout labelLayout) {
        if (this.labelLayout != null) {
            removeChild(this.labelLayout);
        }

        this.labelLayout = labelLayout;
        addChild(labelLayout);
    }

    public SelectedMode getSelectedMode() {
        return selectedMode;
    }

    public void setSelectedMode(SelectedMode selectedMode) {
        this.selectedMode = selectedMode;
        markAsDirty();
    }

    public String getDataGroupId() {
        return dataGroupId;
    }

    public void setDataGroupId(String dataGroupId) {
        this.dataGroupId = dataGroupId;
        markAsDirty();
    }

    public Integer getZLevel() {
        return zLevel;
    }

    public void setZLevel(Integer zLevel) {
        this.zLevel = zLevel;
        markAsDirty();
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
        markAsDirty();
    }

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(Boolean silent) {
        this.silent = silent;
        markAsDirty();
    }

    public Integer getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(Integer animationDuration) {
        this.animationDuration = animationDuration;
        markAsDirty();
    }

    public String getAnimationEasing() {
        return animationEasing;
    }

    public void setAnimationEasing(String animationEasing) {
        this.animationEasing = animationEasing;
        markAsDirty();
    }

    public Integer getAnimationDelay() {
        return animationDelay;
    }

    public void setAnimationDelay(Integer animationDelay) {
        this.animationDelay = animationDelay;
        markAsDirty();
    }

    public Tooltip getTooltip() {
        return tooltip;
    }

    public void setTooltip(Tooltip tooltip) {
        if (this.tooltip != null) {
            removeChild(this.tooltip);
        }

        this.tooltip = tooltip;
        addChild(tooltip);
    }

    @SuppressWarnings("unchecked")
    public T withType(SeriesType type) {
        setType(type);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withId(String id) {
        setId(id);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withName(String name) {
        setName(name);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withColorBy(ColorBy colorBy) {
        setColorBy(colorBy);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withLabel(Label label) {
        setLabel(label);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withLabelLayout(LabelLayout labelLayout) {
        setLabelLayout(labelLayout);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withSelectedMode(SelectedMode selectedMode) {
        setSelectedMode(selectedMode);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withDataGroupId(String dataGroupId) {
        setDataGroupId(dataGroupId);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withZLevel(Integer zLevel) {
        setZLevel(zLevel);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withZ(Integer z) {
        setZ(z);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withSilent(Boolean silent) {
        setSilent(silent);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDuration(Integer animationDuration) {
        setAnimationDuration(animationDuration);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationEasing(String animationEasing) {
        setAnimationEasing(animationEasing);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDelay(Integer animationDelay) {
        setAnimationDelay(animationDelay);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withTooltip(Tooltip tooltip) {
        setTooltip(tooltip);
        return (T) this;
    }
}
