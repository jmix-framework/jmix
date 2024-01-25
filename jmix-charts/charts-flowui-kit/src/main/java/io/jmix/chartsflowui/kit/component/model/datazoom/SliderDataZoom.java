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

package io.jmix.chartsflowui.kit.component.model.datazoom;

import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.shared.*;

/**
 * Slider type data zoom component provides functions like
 * data thumbnail, zoom, brush to select, drag to move, click to locate.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#dataZoom-slider">SliderDataZoom documentation</a>
 */
public class SliderDataZoom extends AbstractDataZoom<SliderDataZoom>
        implements HasPosition<SliderDataZoom> {

    protected Boolean show;

    protected Color backgroundColor;

    protected DataBackground dataBackground;

    protected DataBackground selectedDataBackground;

    protected Color fillerColor;

    protected Color borderColor;

    protected Integer borderRadius;

    protected String handleIcon;

    protected String handleSize;

    protected ItemStyle handleStyle;

    protected String moveHandleIcon;

    protected Integer moveHandleSize;

    protected ItemStyle moveHandleStyle;

    protected Integer labelPrecision;

    protected String labelFormatter;

    protected JsFunction labelFormatterFunction;

    protected Boolean showDetail;

    protected Boolean showDataShadow;

    protected Boolean realtime;

    protected TextStyle textStyle;

    protected Integer zLevel;

    protected Integer z;

    protected String left;

    protected String top;

    protected String right;

    protected String bottom;

    protected String width;

    protected String height;

    protected Boolean brushSelect;

    protected ItemStyle brushStyle;

    protected Emphasis emphasis;

    public SliderDataZoom() {
        super(DataZoomType.SLIDER);
    }

    /**
     * The style of data shadow.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#dataZoom-slider.dataBackground">SliderDataZoom.dataBackground</a>
     */
    public static class DataBackground extends ChartObservableObject {

        protected LineStyle lineStyle;

        protected AreaStyle areaStyle;

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

        public DataBackground withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return this;
        }

        public DataBackground withAreaStyle(AreaStyle areaStyle) {
            setAreaStyle(areaStyle);
            return this;
        }
    }

    /**
     * Component to configure the highlighted state.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#dataZoom-slider.emphasis">SliderDataZoom.emphasis</a>
     */
    public static class Emphasis extends ChartObservableObject {

        protected ItemStyle handleStyle;

        protected ItemStyle moveHandleStyle;

        public ItemStyle getHandleStyle() {
            return handleStyle;
        }

        public void setHandleStyle(ItemStyle handleStyle) {
            if (this.handleStyle != null) {
                removeChild(this.handleStyle);
            }

            this.handleStyle = handleStyle;
            addChild(handleStyle);
        }

        public ItemStyle getMoveHandleStyle() {
            return moveHandleStyle;
        }

        public void setMoveHandleStyle(ItemStyle moveHandleStyle) {
            if (this.moveHandleStyle != null) {
                removeChild(this.moveHandleStyle);
            }

            this.moveHandleStyle = moveHandleStyle;
            addChild(moveHandleStyle);
        }

        public Emphasis withHandleStyle(ItemStyle handleStyle) {
            setHandleStyle(handleStyle);
            return this;
        }

        public Emphasis withMoveHandleStyle(ItemStyle moveHandleStyle) {
            setMoveHandleStyle(moveHandleStyle);
            return this;
        }
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        markAsDirty();
    }

    public DataBackground getDataBackground() {
        return dataBackground;
    }

    public void setDataBackground(DataBackground dataBackground) {
        if (this.dataBackground != null) {
            removeChild(this.dataBackground);
        }

        this.dataBackground = dataBackground;
        addChild(dataBackground);
    }

    public DataBackground getSelectedDataBackground() {
        return selectedDataBackground;
    }

    public void setSelectedDataBackground(DataBackground selectedDataBackground) {
        if (this.selectedDataBackground != null) {
            removeChild(this.selectedDataBackground);
        }

        this.selectedDataBackground = selectedDataBackground;
        addChild(selectedDataBackground);
    }

    public Color getFillerColor() {
        return fillerColor;
    }

    public void setFillerColor(Color fillerColor) {
        this.fillerColor = fillerColor;
        markAsDirty();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        markAsDirty();
    }

    public Integer getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(Integer borderRadius) {
        this.borderRadius = borderRadius;
        markAsDirty();
    }

    public String getHandleIcon() {
        return handleIcon;
    }

    public void setHandleIcon(String handleIcon) {
        this.handleIcon = handleIcon;
        markAsDirty();
    }

    public String getHandleSize() {
        return handleSize;
    }

    public void setHandleSize(String handleSize) {
        this.handleSize = handleSize;
        markAsDirty();
    }

    public ItemStyle getHandleStyle() {
        return handleStyle;
    }

    public void setHandleStyle(ItemStyle handleStyle) {
        if (this.handleStyle != null) {
            removeChild(this.handleStyle);
        }

        this.handleStyle = handleStyle;
        addChild(handleStyle);
    }

    public String getMoveHandleIcon() {
        return moveHandleIcon;
    }

    public void setMoveHandleIcon(String moveHandleIcon) {
        this.moveHandleIcon = moveHandleIcon;
        markAsDirty();
    }

    public Integer getMoveHandleSize() {
        return moveHandleSize;
    }

    public void setMoveHandleSize(Integer moveHandleSize) {
        this.moveHandleSize = moveHandleSize;
        markAsDirty();
    }

    public ItemStyle getMoveHandleStyle() {
        return moveHandleStyle;
    }

    public void setMoveHandleStyle(ItemStyle moveHandleStyle) {
        if (this.moveHandleStyle != null) {
            removeChild(this.moveHandleStyle);
        }

        this.moveHandleStyle = moveHandleStyle;
        addChild(moveHandleStyle);
    }

    public Integer getLabelPrecision() {
        return labelPrecision;
    }

    public void setLabelPrecision(Integer labelPrecision) {
        this.labelPrecision = labelPrecision;
        markAsDirty();
    }

    public String getLabelFormatter() {
        return labelFormatter;
    }

    public void setLabelFormatter(String labelFormatter) {
        this.labelFormatter = labelFormatter;
        markAsDirty();
    }

    public JsFunction getLabelFormatterFunction() {
        return labelFormatterFunction;
    }

    public void setLabelFormatterFunction(JsFunction labelFormatterFunction) {
        this.labelFormatterFunction = labelFormatterFunction;
        markAsDirty();
    }

    public void setLabelFormatterFunction(String labelFormatterFunction) {
        this.labelFormatterFunction = new JsFunction(labelFormatterFunction);
        markAsDirty();
    }

    public Boolean getShowDetail() {
        return showDetail;
    }

    public void setShowDetail(Boolean showDetail) {
        this.showDetail = showDetail;
        markAsDirty();
    }

    public Boolean getShowDataShadow() {
        return showDataShadow;
    }

    public void setShowDataShadow(Boolean showDataShadow) {
        this.showDataShadow = showDataShadow;
        markAsDirty();
    }

    public Boolean getRealtime() {
        return realtime;
    }

    public void setRealtime(Boolean realtime) {
        this.realtime = realtime;
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

    public Boolean getBrushSelect() {
        return brushSelect;
    }

    public void setBrushSelect(Boolean brushSelect) {
        this.brushSelect = brushSelect;
        markAsDirty();
    }

    public ItemStyle getBrushStyle() {
        return brushStyle;
    }

    public void setBrushStyle(ItemStyle brushStyle) {
        if (this.brushStyle != null) {
            removeChild(this.brushStyle);
        }

        this.brushStyle = brushStyle;
        addChild(brushStyle);
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

    public SliderDataZoom withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public SliderDataZoom withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return this;
    }

    public SliderDataZoom withDataBackground(DataBackground dataBackground) {
        setDataBackground(dataBackground);
        return this;
    }

    public SliderDataZoom withSelectedDataBackground(DataBackground selectedDataBackground) {
        setSelectedDataBackground(selectedDataBackground);
        return this;
    }

    public SliderDataZoom withFillerColor(Color fillerColor) {
        setFillerColor(fillerColor);
        return this;
    }

    public SliderDataZoom withBorderColor(Color borderColor) {
        setBorderColor(borderColor);
        return this;
    }

    public SliderDataZoom withBorderRadius(Integer borderRadius) {
        setBorderRadius(borderRadius);
        return this;
    }

    public SliderDataZoom withHandleIcon(String handleIcon) {
        setHandleIcon(handleIcon);
        return this;
    }

    public SliderDataZoom withHandleSize(String handleSize) {
        setHandleSize(handleSize);
        return this;
    }

    public SliderDataZoom withHandleStyle(ItemStyle handleStyle) {
        setHandleStyle(handleStyle);
        return this;
    }

    public SliderDataZoom withMoveHandleIcon(String moveHandleIcon) {
        setMoveHandleIcon(moveHandleIcon);
        return this;
    }

    public SliderDataZoom withMoveHandleSize(Integer moveHandleSize) {
        setMoveHandleSize(moveHandleSize);
        return this;
    }

    public SliderDataZoom withMoveHandleStyle(ItemStyle moveHandleStyle) {
        setMoveHandleStyle(moveHandleStyle);
        return this;
    }

    public SliderDataZoom withLabelPrecision(Integer labelPrecision) {
        setLabelPrecision(labelPrecision);
        return this;
    }

    public SliderDataZoom withLabelFormatter(String labelFormatter) {
        setLabelFormatter(labelFormatter);
        return this;
    }

    public SliderDataZoom withLabelFormatterFunction(JsFunction labelFormatterFunction) {
        setLabelFormatterFunction(labelFormatterFunction);
        return this;
    }

    public SliderDataZoom withLabelFormatterFunction(String labelFormatterFunction) {
        setLabelFormatterFunction(labelFormatterFunction);
        return this;
    }

    public SliderDataZoom withShowDetail(Boolean showDetail) {
        setShowDetail(showDetail);
        return this;
    }

    public SliderDataZoom withShowDataShadow(Boolean showDataShadow) {
        setShowDataShadow(showDataShadow);
        return this;
    }

    public SliderDataZoom withRealtime(Boolean realtime) {
        setRealtime(realtime);
        return this;
    }

    public SliderDataZoom withTextStyle(TextStyle textStyle) {
        setTextStyle(textStyle);
        return this;
    }

    public SliderDataZoom withZLevel(Integer zLevel) {
        setZLevel(zLevel);
        return this;
    }

    public SliderDataZoom withZ(Integer z) {
        setZ(z);
        return this;
    }

    public SliderDataZoom withWidth(String width) {
        setWidth(width);
        return this;
    }

    public SliderDataZoom withHeight(String height) {
        setHeight(height);
        return this;
    }

    public SliderDataZoom withBrushSelect(Boolean brushSelect) {
        setBrushSelect(brushSelect);
        return this;
    }

    public SliderDataZoom withBrushStyle(ItemStyle brushStyle) {
        setBrushStyle(brushStyle);
        return this;
    }

    public SliderDataZoom withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }
}
