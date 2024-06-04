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

package io.jmix.chartsflowui.kit.component.model;

import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.VisualEffect;
import jakarta.annotation.Nullable;

/**
 * Brush is an area-selecting component, with which user can select part of series from a chart to display in detail.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#brush">Brush documentation</a>
 */
public class Brush extends ChartObservableObject {

    protected String id;

    protected Toolbox[] toolboxes;

    protected BrushLink brushLink;

    protected SeriesIndex seriesIndex;

    protected IndexItem geoIndex;

    protected IndexItem xAxisIndex;

    protected IndexItem yAxisIndex;

    protected BrushType brushType;

    protected BrushMode brushMode;

    protected Boolean transformable;

    protected BrushStyle brushStyle;

    protected ThrottleType throttleType;

    protected Double throttleDelay;

    protected Boolean removeOnClick;

    protected VisualEffect inBrush;

    protected VisualEffect outOfBrush;

    protected Integer z;

    /**
     * Mode for brush selecting.
     */
    public enum BrushSelectMode implements HasEnumId {
        ALL("all"),
        NONE("none");

        private final String id;

        BrushSelectMode(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static BrushSelectMode fromId(String id) {
            for (BrushSelectMode at : BrushSelectMode.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    /**
     * Update type for brush selecting event.
     */
    public enum ThrottleType implements HasEnumId {
        DEBOUNCE("debounce"),
        FIX_RATE("fixRate");

        private final String id;

        ThrottleType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static ThrottleType fromId(String id) {
            for (ThrottleType at : ThrottleType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    /**
     * Enum for assigns which of the series can use brush selecting.
     */
    public enum SeriesIndex implements HasEnumId {
        ALL("all"),
        ARRAY("Array"),
        NUMBER("number");

        private final String id;

        SeriesIndex(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static SeriesIndex fromId(String id) {
            for (SeriesIndex at : SeriesIndex.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    /**
     * Brush selecting mode.
     */
    public enum BrushMode implements HasEnumId {
        SINGLE("single"),
        MULTIPLE("multiple");

        private final String id;

        BrushMode(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static BrushMode fromId(String id) {
            for (BrushMode at : BrushMode.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    /**
     * Type of brush.
     */
    public enum BrushType implements HasEnumId {
        RECT("rect"),
        POLYGON("polygon"),
        LINE_X("lineX"),
        LINE_Y("lineY");

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

    /**
     * Buttons in toolbox that is related to brush includes.
     */
    public enum Toolbox implements HasEnumId {
        RECT("rect"),
        POLYGON("polygon"),
        LINE_X("lineX"),
        LINE_Y("lineY"),
        KEEP("keep"),
        CLEAR("clear");

        private final String id;

        Toolbox(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static Toolbox fromId(String id) {
            for (Toolbox at : Toolbox.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    /**
     * Links interaction between selected items in different series.
     * More detailed information is provided in the documentation.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#brush.brushLink">BrushLink documentation</a>
     */
    public static class BrushLink {

        protected Integer[] brushLinkIndexes;

        protected BrushSelectMode brushSelectMode;

        public BrushLink(Integer... brushLinkIndexes) {
            this.brushLinkIndexes = brushLinkIndexes;
        }

        public BrushLink(BrushSelectMode brushSelectMode) {
            this.brushSelectMode = brushSelectMode;
        }

        public Integer[] getBrushLinkIndexes() {
            return brushLinkIndexes;
        }

        public BrushSelectMode getBrushSelectMode() {
            return brushSelectMode;
        }
    }

    /**
     * Index of item which can use brush selecting.
     */
    public static class IndexItem {

        protected Integer singleIndex;

        protected Integer[] indexes;

        protected BrushSelectMode brushSelectMode;

        public IndexItem(Integer singleIndex) {
            this.singleIndex = singleIndex;
        }

        public IndexItem(Integer... indexes) {
            this.indexes = indexes;
        }

        public IndexItem(BrushSelectMode brushSelectMode) {
            this.brushSelectMode = brushSelectMode;
        }

        public Integer getSingleIndex() {
            return singleIndex;
        }

        public Integer[] getIndexes() {
            return indexes;
        }

        public BrushSelectMode getBrushSelectMode() {
            return brushSelectMode;
        }
    }

    /**
     * Brush font style. More detailed information is provided in the documentation.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#brush.brushStyle">BrushStyle documentation</a>
     */
    public static class BrushStyle extends ChartObservableObject {

        protected Integer borderWidth;

        protected Color color;

        protected Color borderColor;

        public Integer getBorderWidth() {
            return borderWidth;
        }

        public void setBorderWidth(Integer borderWidth) {
            this.borderWidth = borderWidth;
            markAsDirty();
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            markAsDirty();
        }

        public Color getBorderColor() {
            return borderColor;
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            markAsDirty();
        }

        public BrushStyle withBorderWidth(Integer borderWidth) {
            setBorderWidth(borderWidth);
            return this;
        }

        public BrushStyle withColor(Color color) {
            setColor(color);
            return this;
        }

        public BrushStyle withBorderColor(Color borderColor) {
            setBorderColor(borderColor);
            return this;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public Toolbox[] getToolboxes() {
        return toolboxes;
    }

    public void setToolboxes(Toolbox... toolboxes) {
        this.toolboxes = toolboxes;
        markAsDirty();
    }

    public BrushLink getBrushLink() {
        return brushLink;
    }

    public void setBrushLink(Integer... seriesIndexes) {
        this.brushLink = new BrushLink(seriesIndexes);
        markAsDirty();
    }

    public void setBrushLink(BrushSelectMode brushSelectMode) {
        this.brushLink = new BrushLink(brushSelectMode);
        markAsDirty();
    }

    public SeriesIndex getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(SeriesIndex seriesIndex) {
        this.seriesIndex = seriesIndex;
        markAsDirty();
    }

    public IndexItem getGeoIndex() {
        return geoIndex;
    }

    public void setGeoIndex(Integer number) {
        this.geoIndex = new IndexItem(number);
        markAsDirty();
    }

    public void setGeoIndex(Integer... indexes) {
        this.geoIndex = new IndexItem(indexes);
        markAsDirty();
    }

    public void setGeoIndex(BrushSelectMode brushSelectMode) {
        this.geoIndex = new IndexItem(brushSelectMode);
        markAsDirty();
    }

    public IndexItem getXAxisIndex() {
        return xAxisIndex;
    }

    public void setXAxisIndex(Integer number) {
        this.xAxisIndex = new IndexItem(number);
        markAsDirty();
    }

    public void setXAxisIndex(Integer... indexes) {
        this.xAxisIndex = new IndexItem(indexes);
        markAsDirty();
    }

    public void setXAxisIndex(BrushSelectMode brushSelectMode) {
        this.xAxisIndex = new IndexItem(brushSelectMode);
        markAsDirty();
    }

    public IndexItem getYAxisIndex() {
        return yAxisIndex;
    }

    public void setYAxisIndex(Integer number) {
        this.yAxisIndex = new IndexItem(number);
        markAsDirty();
    }

    public void setYAxisIndex(Integer... indexes) {
        this.yAxisIndex = new IndexItem(indexes);
        markAsDirty();
    }

    public void setYAxisIndex(BrushSelectMode brushSelectMode) {
        this.yAxisIndex = new IndexItem(brushSelectMode);
        markAsDirty();
    }

    public BrushType getBrushType() {
        return brushType;
    }

    public void setBrushType(BrushType brushType) {
        this.brushType = brushType;
        markAsDirty();
    }

    public BrushMode getBrushMode() {
        return brushMode;
    }

    public void setBrushMode(BrushMode brushMode) {
        this.brushMode = brushMode;
        markAsDirty();
    }

    public Boolean getTransformable() {
        return transformable;
    }

    public void setTransformable(Boolean transformable) {
        this.transformable = transformable;
        markAsDirty();
    }

    public BrushStyle getBrushStyle() {
        return brushStyle;
    }

    public void setBrushStyle(BrushStyle brushStyle) {
        if (this.brushStyle != null) {
            removeChild(this.brushStyle);
        }

        this.brushStyle = brushStyle;
        addChild(brushStyle);
    }

    public ThrottleType getThrottleType() {
        return throttleType;
    }

    public void setThrottleType(ThrottleType throttleType) {
        this.throttleType = throttleType;
        markAsDirty();
    }

    public Double getThrottleDelay() {
        return throttleDelay;
    }

    public void setThrottleDelay(Double throttleDelay) {
        this.throttleDelay = throttleDelay;
        markAsDirty();
    }

    public Boolean getRemoveOnClick() {
        return removeOnClick;
    }

    public void setRemoveOnClick(Boolean removeOnClick) {
        this.removeOnClick = removeOnClick;
        markAsDirty();
    }

    public VisualEffect getInBrush() {
        return inBrush;
    }

    public void setInBrush(VisualEffect inBrush) {
        if (this.inBrush != null) {
            removeChild(this.inBrush);
        }

        this.inBrush = inBrush;
        addChild(inBrush);
    }

    public VisualEffect getOutOfBrush() {
        return outOfBrush;
    }

    public void setOutOfBrush(VisualEffect outOfBrush) {
        if (this.outOfBrush != null) {
            removeChild(this.outOfBrush);
        }

        this.outOfBrush = outOfBrush;
        addChild(outOfBrush);
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
        markAsDirty();
    }

    public Brush withId(String id) {
        setId(id);
        return this;
    }

    public Brush withToolboxes(Toolbox... toolbox) {
        setToolboxes(toolbox);
        return this;
    }

    public Brush withBrushLink(Integer... seriesIndexes) {
        setBrushLink(seriesIndexes);
        return this;
    }

    public Brush withBrushLine(BrushSelectMode brushSelectMode) {
        setBrushLink(brushSelectMode);
        return this;
    }

    public Brush withSeriesIndex(SeriesIndex seriesIndex) {
        setSeriesIndex(seriesIndex);
        return this;
    }

    public Brush withGeoIndex(Integer number) {
        setGeoIndex(number);
        return this;
    }

    public Brush withGeoIndex(Integer... indexes) {
        setGeoIndex(indexes);
        return this;
    }

    public Brush withGeoIndex(BrushSelectMode brushSelectedMode) {
        setGeoIndex(brushSelectedMode);
        return this;
    }

    public Brush withXAxisIndex(Integer number) {
        setXAxisIndex(number);
        return this;
    }

    public Brush withXAxisIndex(Integer... indexes) {
        setXAxisIndex(indexes);
        return this;
    }

    public Brush withXAxisIndex(BrushSelectMode brushSelectedMode) {
        setXAxisIndex(brushSelectedMode);
        return this;
    }

    public Brush withYAxisIndex(Integer number) {
        setYAxisIndex(number);
        return this;
    }

    public Brush withYAxisIndex(Integer... indexes) {
        setYAxisIndex(indexes);
        return this;
    }

    public Brush withYAxisIndex(BrushSelectMode brushSelectMode) {
        setYAxisIndex(brushSelectMode);
        return this;
    }

    public Brush withBrushType(BrushType brushType) {
        setBrushType(brushType);
        return this;
    }

    public Brush withBrushMode(BrushMode brushMode) {
        setBrushMode(brushMode);
        return this;
    }

    public Brush withTransformable(Boolean transformable) {
        setTransformable(transformable);
        return this;
    }

    public Brush withBrushStyle(BrushStyle brushStyle) {
        setBrushStyle(brushStyle);
        return this;
    }

    public Brush withThrottleType(ThrottleType throttleType) {
        setThrottleType(throttleType);
        return this;
    }

    public Brush withThrottleDelay(Double throttleDelay) {
        setThrottleDelay(throttleDelay);
        return this;
    }

    public Brush withRemoveOnClick(Boolean removeOnClick) {
        setRemoveOnClick(removeOnClick);
        return this;
    }

    public Brush withInBrush(VisualEffect inBrush) {
        setInBrush(inBrush);
        return this;
    }

    public Brush withOutOfBrush(VisualEffect outOfBrush) {
        setOutOfBrush(outOfBrush);
        return this;
    }

    public Brush withZ(Integer z) {
        setZ(z);
        return this;
    }
}
