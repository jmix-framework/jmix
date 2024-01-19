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

package io.jmix.chartsflowui.kit.component.model.legend;

import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class AbstractLegend<T extends AbstractLegend<T>> extends ChartObservableObject
        implements HasShadow<T>, HasPosition<T>, HasBorder<T>, HasPadding<T> {

    protected final LegendType type;

    protected String id;

    protected Boolean show;

    protected Integer zLevel;

    protected Integer z;

    protected String left;

    protected String top;

    protected String right;

    protected String bottom;

    protected String width;

    protected String height;

    protected Orientation orientation;

    protected Align align;

    protected Padding padding;

    protected Integer itemGap;

    protected Integer itemWidth;

    protected Integer itemHeight;

    protected ItemStyle itemStyle;

    protected LineStyle lineStyle;

    protected Integer symbolRotate;

    protected String formatter;

    protected JsFunction formatterFunction;

    protected SelectedMode selectedMode;

    protected Color inactiveColor;

    protected Color inactiveBorderColor;

    protected Integer inactiveBorderWidth;

    protected Map<String, Boolean> selectedSeries;

    protected TextStyle textStyle;

    protected Tooltip tooltip;

    protected String icon;

    protected Color backgroundColor;

    protected Color borderColor;

    protected Integer borderWidth;

    protected Integer borderRadius;

    protected Integer shadowBlur;

    protected Color shadowColor;

    protected Integer shadowOffsetX;

    protected Integer shadowOffsetY;

    protected Emphasis emphasis;

    protected Boolean selector;

    protected SelectorLabel selectorLabel;

    protected Position selectorPosition;

    protected Integer selectorItemGap;

    protected Integer selectorButtonGap;

    protected AbstractLegend(LegendType type) {
        this.type = type;
    }

    public enum Align implements HasEnumId {
        AUTO("auto"),
        LEFT("left"),
        RIGHT("right");

        private final String id;

        Align(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static Align fromId(String id) {
            for (Align at : Align.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public enum Position implements HasEnumId {
        START("start"),
        END("end");

        private final String id;

        Position(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static Position fromId(String id) {
            for (Position at : Position.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public static class TextStyle extends AbstractRichText<TextStyle>
            implements HasShadow<TextStyle>, HasBorder<TextStyle>, HasPadding<TextStyle> {

        protected Color backgroundColor;

        protected Color borderColor;

        protected Integer borderWidth;

        protected String borderType;

        protected Integer borderDashOffset;

        protected Integer borderRadius;

        protected Padding padding;

        protected Integer shadowBlur;

        protected Color shadowColor;

        protected Integer shadowOffsetX;

        protected Integer shadowOffsetY;

        public Color getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            markAsDirty();
        }

        @Override
        public Color getBorderColor() {
            return borderColor;
        }

        @Override
        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            markAsDirty();
        }

        @Override
        public Integer getBorderWidth() {
            return borderWidth;
        }

        @Override
        public void setBorderWidth(Integer borderWidth) {
            this.borderWidth = borderWidth;
            markAsDirty();
        }

        public String getBorderType() {
            return borderType;
        }

        public void setBorderType(String borderType) {
            this.borderType = borderType;
            markAsDirty();
        }

        public Integer getBorderDashOffset() {
            return borderDashOffset;
        }

        public void setBorderDashOffset(Integer borderDashOffset) {
            this.borderDashOffset = borderDashOffset;
            markAsDirty();
        }

        @Override
        public Integer getBorderRadius() {
            return borderRadius;
        }

        @Override
        public void setBorderRadius(Integer borderRadius) {
            this.borderRadius = borderRadius;
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

        public TextStyle withBackgroundColor(Color backgroundColor) {
            setBackgroundColor(backgroundColor);
            return this;
        }

        public TextStyle withBorderType(String borderType) {
            setBorderType(borderType);
            return this;
        }

        public TextStyle withBorderDashOffset(Integer borderDashOffset) {
            setBorderDashOffset(borderDashOffset);
            return this;
        }
    }

    public static class Emphasis extends ChartObservableObject {

        protected SelectorLabel selectorLabel;

        public SelectorLabel getSelectorLabel() {
            return selectorLabel;
        }

        public void setSelectorLabel(SelectorLabel selectorLabel) {
            if (this.selectorLabel != null) {
                removeChild(this.selectorLabel);
            }

            this.selectorLabel = selectorLabel;
            addChild(selectorLabel);
        }

        public Emphasis withSelectorLabel(SelectorLabel selectorLabel) {
            setSelectorLabel(selectorLabel);
            return this;
        }
    }

    public static class SelectorLabel extends AbstractEnhancedLabel<SelectorLabel> {
    }

    public LegendType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public T withId(String id) {
        setId(id);
        return (T) this;
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public T withShow(Boolean show) {
        setShow(show);
        return (T) this;
    }

    public Integer getZLevel() {
        return zLevel;
    }

    public void setZLevel(Integer zLevel) {
        this.zLevel = zLevel;
        markAsDirty();
    }

    public T withZLevel(Integer zLevel) {
        setZLevel(zLevel);
        return (T) this;
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
        markAsDirty();
    }

    public T withZ(Integer z) {
        setZ(z);
        return (T) this;
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

    public T withWidth(String width) {
        setWidth(width);
        return (T) this;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
        markAsDirty();
    }

    public T withHeight(String height) {
        setHeight(height);
        return (T) this;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        markAsDirty();
    }

    public T withOrientation(Orientation orientation) {
        setOrientation(orientation);
        return (T) this;
    }

    public Align getAlign() {
        return align;
    }

    public void setAlign(Align align) {
        this.align = align;
        markAsDirty();
    }

    public T withAlign(Align align) {
        setAlign(align);
        return (T) this;
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

    public Integer getItemGap() {
        return itemGap;
    }

    public void setItemGap(Integer itemGap) {
        this.itemGap = itemGap;
        markAsDirty();
    }

    public T withItemGap(Integer itemGap) {
        setItemGap(itemGap);
        return (T) this;
    }

    public Integer getItemWidth() {
        return itemWidth;
    }

    public void setItemWidth(Integer itemWidth) {
        this.itemWidth = itemWidth;
        markAsDirty();
    }

    public T withItemWidth(Integer itemWidth) {
        setItemWidth(itemWidth);
        return (T) this;
    }

    public Integer getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(Integer itemHeight) {
        this.itemHeight = itemHeight;
        markAsDirty();
    }

    public T withItemHeight(Integer itemHeight) {
        setItemHeight(itemHeight);
        return (T) this;
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

    public T withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return (T) this;
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

    public T withLineStyle(LineStyle lineStyle) {
        setLineStyle(lineStyle);
        return (T) this;
    }

    public Integer getSymbolRotate() {
        return symbolRotate;
    }

    public void setSymbolRotate(Integer symbolRotate) {
        this.symbolRotate = symbolRotate;
        markAsDirty();
    }

    public T withSymbolRotate(Integer symbolRotate) {
        setSymbolRotate(symbolRotate);
        return (T) this;
    }

    public String getFormatter() {
        return formatter;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
        markAsDirty();
    }

    public T withFormatter(String formatter) {
        setFormatter(formatter);
        return (T) this;
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

    public T withFormatterFunction(JsFunction formatterFunction) {
        setFormatterFunction(formatterFunction);
        return (T) this;
    }

    public T withFormatterFunction(String formatterFunction) {
        setFormatterFunction(formatterFunction);
        return (T) this;
    }

    public SelectedMode getSelectedMode() {
        return selectedMode;
    }

    public void setSelectedMode(SelectedMode selectedMode) {
        this.selectedMode = selectedMode;
        markAsDirty();
    }

    public T withSelectedMode(SelectedMode selectedMode) {
        setSelectedMode(selectedMode);
        return (T) this;
    }

    public Color getInactiveColor() {
        return inactiveColor;
    }

    public void setInactiveColor(Color inactiveColor) {
        this.inactiveColor = inactiveColor;
        markAsDirty();
    }

    public T withInactiveColor(Color inactiveColor) {
        setInactiveColor(inactiveColor);
        return (T) this;
    }

    public Color getInactiveBorderColor() {
        return inactiveBorderColor;
    }

    public void setInactiveBorderColor(Color inactiveBorderColor) {
        this.inactiveBorderColor = inactiveBorderColor;
        markAsDirty();
    }

    public T withInactiveBorderColor(Color inactiveBorderColor) {
        setInactiveBorderColor(inactiveBorderColor);
        return (T) this;
    }

    public Integer getInactiveBorderWidth() {
        return inactiveBorderWidth;
    }

    public void setInactiveBorderWidth(Integer inactiveBorderWidth) {
        this.inactiveBorderWidth = inactiveBorderWidth;
        markAsDirty();
    }

    public T withInactiveBorderWidth(Integer inactiveBorderWidth) {
        setInactiveBorderWidth(inactiveBorderWidth);
        return (T) this;
    }

    public Map<String, Boolean> getSelectedSeries() {
        return selectedSeries;
    }

    public void setSelectedSeries(Map<String, Boolean> selectedSeries) {
        this.selectedSeries = selectedSeries;
        markAsDirty();
    }

    public void addSelectedSeries(String seriesName, Boolean select) {
        if (selectedSeries == null) {
            selectedSeries = new HashMap<>();
        }

        selectedSeries.put(seriesName, select);
        markAsDirty();
    }

    public void removeSelectedSeries(String seriesName) {
        if (selectedSeries != null && selectedSeries.remove(seriesName) != null) {
            markAsDirty();
        }
    }

    public T withSelectedSeries(String seriesName, Boolean select) {
        addSelectedSeries(seriesName, select);
        return (T) this;
    }

    public T withSelectedSeries(Map<String, Boolean> selectedSeries) {
        setSelectedMode(selectedMode);
        return (T) this;
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

    public T withTextStyle(TextStyle textStyle) {
        setTextStyle(textStyle);
        return (T) this;
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

    public T withTooltip(Tooltip tooltip) {
        setTooltip(tooltip);
        return (T) this;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
        markAsDirty();
    }

    public T withIcon(String icon) {
        setIcon(icon);
        return (T) this;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        markAsDirty();
    }

    public T withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return (T) this;
    }

    @Override
    public Color getBorderColor() {
        return borderColor;
    }

    @Override
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        markAsDirty();
    }

    @Override
    public Integer getBorderWidth() {
        return borderWidth;
    }

    @Override
    public void setBorderWidth(Integer borderWidth) {
        this.borderWidth = borderWidth;
        markAsDirty();
    }

    @Override
    public Integer getBorderRadius() {
        return borderRadius;
    }

    @Override
    public void setBorderRadius(Integer borderRadius) {
        this.borderRadius = borderRadius;
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

    public T withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return (T) this;
    }

    public Boolean getSelector() {
        return selector;
    }

    public void setSelector(Boolean selector) {
        this.selector = selector;
        markAsDirty();
    }

    public T withSelector(Boolean selector) {
        setSelector(selector);
        return (T) this;
    }

    public SelectorLabel getSelectorLabel() {
        return selectorLabel;
    }

    public void setSelectorLabel(SelectorLabel selectorLabel) {
        if (this.selectorLabel != null) {
            removeChild(this.selectorLabel);
        }

        this.selectorLabel = selectorLabel;
        addChild(selectorLabel);
    }

    public T withSelectorLabel(SelectorLabel selectorLabel) {
        setSelectorLabel(selectorLabel);
        return (T) this;
    }

    public Position getSelectorPosition() {
        return selectorPosition;
    }

    public void setSelectorPosition(Position selectorPosition) {
        this.selectorPosition = selectorPosition;
        markAsDirty();
    }

    public T withSelectorPosition(Position selectorPosition) {
        setSelectorPosition(selectorPosition);
        return (T) this;
    }

    public Integer getSelectorItemGap() {
        return selectorItemGap;
    }

    public void setSelectorItemGap(Integer selectorItemGap) {
        this.selectorItemGap = selectorItemGap;
        markAsDirty();
    }

    public T withSelectorItemGap(Integer selectorItemGap) {
        setSelectorItemGap(selectorItemGap);
        return (T) this;
    }

    public Integer getSelectorButtonGap() {
        return selectorButtonGap;
    }

    public void setSelectorButtonGap(Integer selectorButtonGap) {
        this.selectorButtonGap = selectorButtonGap;
        markAsDirty();
    }

    public T withSelectorButtonGap(Integer selectorButtonGap) {
        setSelectorButtonGap(selectorButtonGap);
        return (T) this;
    }
}
