/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.component.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import io.jmix.pivottableflowui.kit.component.model.*;

import java.util.List;
import java.util.Map;

/**
 * For internal use only
 */
@SuppressWarnings("unused")
class JmixPivotTableMixins {

    static abstract class ChartOptions {

        /*
        protected Map<String, String> properties;                     // Haulmont API
        protected List<String> rows;                                  // pivot() and pivotUI()
        protected List<String> cols;                                  // pivot() and pivotUI()
        protected Aggregation aggregation;                            // pivot()
        protected Renderer renderer;                                  // pivot()
        protected List<String> aggregationProperties;                 // pivotUI()
        protected Aggregations aggregations;                          // pivotUI()
        protected Renderers renderers;                                // pivotUI()
        protected List<String> hiddenProperties;                      // pivotUI()
        protected List<String> hiddenFromAggregations;                // pivotUI()
        protected List<String> hiddenFromDragDrop;                    // pivotUI()
        protected Integer menuLimit;                                  // pivotUI()
        protected Boolean autoSortUnusedProperties;                   // pivotUI()
        protected UnusedPropertiesVertical unusedPropertiesVertical;  // pivotUI()
        protected RendererOptions rendererOptions;                    // pivot() and pivotUI()
        protected Map<String, List<String>> inclusions;               // pivotUI()
        protected Map<String, List<String>> exclusions;               // pivotUI()
        protected DerivedProperties derivedProperties;                // pivot() and pivotUI()
        protected String localeCode;                                  // Haulmont API
        protected Boolean showUI;                                     // pivotUI()
        protected Boolean rowTotals;                                  // pivot() and pivotUI()
        protected Boolean colTotals;                                  // pivot() and pivotUI()


        @JsonProperty("filter")
        abstract JsFunction getFilterFunction();

        @JsonProperty("sorters")
        abstract JsFunction getSortersFunction();

        @JsonIgnore
        abstract DataSet getDataSet();

        @JsonIgnore
        abstract String getNativeJson();
    }

    static abstract class Title {

        @JsonProperty("zlevel")
        abstract Integer getZLevel();
    }

    static abstract class AbstractRichText {

        @JsonProperty("rich")
        abstract Map<String, RichStyle> getRichStyles();
    }

    static abstract class AbstractLegend {

        @JsonProperty("zlevel")
        abstract Integer getZLevel();

        @JsonProperty("selected")
        abstract Map<String, Boolean> getSelectedSeries();

        @JsonProperty("orient")
        abstract Orientation getOrientation();
    }

    static abstract class Brush {

        @JsonProperty("toolbox")
        abstract Toolbox[] getToolboxes();

        @JsonProperty("xAxisIndex")
        abstract Toolbox[] getXAxisIndex();

        @JsonProperty("yAxisIndex")
        abstract Toolbox[] getYAxisIndex();
    }

    static abstract class Grid {

        @JsonProperty("zlevel")
        abstract Integer getZLevel();
    }

    static abstract class AbstractAxis {

        static abstract class AxisLine {

            @JsonProperty("symbol")
            abstract String[] getSymbols();

            @JsonProperty("symbolSize")
            abstract Integer[] getSymbolsSize();

            @JsonProperty("symbolOffset")
            abstract Integer[] getSymbolsOffset();
        }
    }

    static abstract class AreaStyle {

        @JsonProperty("color")
        abstract Color[] getColors();
    }

    static abstract class Radar {

        @JsonProperty("indicator")
        abstract List<Indicator> getIndicators();
    }

    static abstract class AbstractDataZoom {

        @JsonProperty("xAxisIndex")
        abstract Integer[] getXAxisIndexes();

        @JsonProperty("yAxisIndex")
        abstract Integer[] getYAxisIndexes();

        @JsonProperty("radiusAxisIndex")
        abstract Integer[] getRadiusAxisIndexes();

        @JsonProperty("angleAxisIndex")
        abstract Integer[] getAngleAxisIndexes();

        @JsonProperty("orient")
        abstract Orientation getOrientation();
    }

    static abstract class AbstractVisualMap {

        @JsonProperty("orient")
        abstract Orientation getOrientation();
    }

    static abstract class AbstractBorderedTextStyle {

        @JsonProperty("borderCap")
        abstract HasLineStyle.Cap getCap();

        @JsonProperty("borderJoin")
        abstract HasLineStyle.Join getJoin();

        @JsonProperty("borderMiterLimit")
        abstract Integer getMiterLimit();
    }

    static abstract class Toolbox {

        @JsonProperty("feature")
        abstract ToolboxFeature[] getFeatures();

        @JsonProperty("orient")
        abstract Orientation getOrientation();
    }

    static abstract class MagicTypeFeature {

        @JsonProperty("type")
        abstract MagicType[] getTypes();
    }

    static abstract class BrushFeature {

        @JsonProperty("type")
        abstract BrushType[] getTypes();
    }

    static abstract class ToolboxEmphasisIconStyle {

        @JsonProperty("textPadding")
        abstract Padding getPadding();
    }

    static abstract class LabelLine {

        @JsonProperty("length2")
        abstract Integer getLength();
    }

    static abstract class MarkPoint {

        static abstract class Point {

            @JsonProperty("coord")
            abstract Coordinate getCoordinate();
        }
    }

    static abstract class MarkLine {

        static abstract class Point {

            @JsonProperty("coord")
            abstract Coordinate getCoordinate();

            @JsonProperty("xAxis")
            abstract String getXAxis();

            @JsonProperty("yAxis")
            abstract String getYAxis();
        }
    }

    static abstract class MarkArea {

        static abstract class PointPair {

            @JsonProperty("0")
            abstract Point getLeftTopPoint();

            @JsonProperty("1")
            abstract Point getRightBottomPoint();
        }

        static abstract class Point {

            @JsonProperty("coord")
            abstract Coordinate getCoordinate();
        }
    }

    static abstract class Series {

        @JsonProperty("xAxisIndex")
        abstract Integer getXAxisIndex();

        @JsonProperty("yAxisIndex")
        abstract Integer getYAxisIndex();
    }

    static abstract class FunnelSeries {

        @JsonProperty("orient")
        abstract Orientation getOrientation();
    }

    static abstract class CandlestickSeries {

        static abstract class ItemStyle {

            @JsonProperty("color")
            abstract Color getBullishColor();

            @JsonProperty("color0")
            abstract Color getBearishColor();

            @JsonProperty("borderColor")
            abstract Color getBullishBorderColor();

            @JsonProperty("borderColor0")
            abstract Color getBearishBorderColor();

            @JsonProperty("borderColorDoji")
            abstract Color getDojiBorderColor();
        }*/
    }
}

