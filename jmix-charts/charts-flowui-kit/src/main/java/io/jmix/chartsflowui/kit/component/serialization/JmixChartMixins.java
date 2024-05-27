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

package io.jmix.chartsflowui.kit.component.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jmix.chartsflowui.kit.component.model.DataSet;
import io.jmix.chartsflowui.kit.component.model.HasLineStyle;
import io.jmix.chartsflowui.kit.component.model.axis.AngleAxis;
import io.jmix.chartsflowui.kit.component.model.axis.Radar.Indicator;
import io.jmix.chartsflowui.kit.component.model.axis.RadiusAxis;
import io.jmix.chartsflowui.kit.component.model.axis.XAxis;
import io.jmix.chartsflowui.kit.component.model.axis.YAxis;
import io.jmix.chartsflowui.kit.component.model.series.mark.Coordinate;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.Orientation;
import io.jmix.chartsflowui.kit.component.model.shared.Padding;
import io.jmix.chartsflowui.kit.component.model.shared.RichStyle;
import io.jmix.chartsflowui.kit.component.model.toolbox.BrushFeature.BrushType;
import io.jmix.chartsflowui.kit.component.model.toolbox.MagicTypeFeature.MagicType;
import io.jmix.chartsflowui.kit.component.model.toolbox.ToolboxFeature;

import java.util.List;
import java.util.Map;

/**
 * For internal use only
 */
@SuppressWarnings("unused")
class JmixChartMixins {

    static abstract class ChartOptions {

        @JsonIgnore
        abstract Boolean isDirtyInDepth();

        @JsonProperty("xAxis")
        abstract List<XAxis> getXAxes();

        @JsonProperty("yAxis")
        abstract List<YAxis> getYAxes();

        @JsonProperty("radiusAxis")
        abstract List<RadiusAxis> getRadiusAxes();

        @JsonProperty("angleAxis")
        abstract List<AngleAxis> getAngleAxes();

        @JsonProperty("grid")
        abstract List<Grid> getGrids();

        @JsonIgnore
        abstract DataSet getDataSet();

        @JsonIgnore
        abstract String getNativeJson();

        @JsonProperty("color")
        abstract List<Color> getColorPalette();

        @JsonProperty("useUTC")
        abstract Boolean getUseUtc();
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
        }
    }
}
