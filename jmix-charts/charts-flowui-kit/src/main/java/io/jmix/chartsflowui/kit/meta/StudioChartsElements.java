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

package io.jmix.chartsflowui.kit.meta;

import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.axis.*;
import io.jmix.chartsflowui.kit.component.model.datazoom.InsideDataZoom;
import io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom;
import io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend;
import io.jmix.chartsflowui.kit.component.model.legend.Legend;
import io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend;
import io.jmix.chartsflowui.kit.component.model.series.*;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint;
import io.jmix.chartsflowui.kit.component.model.shared.Label;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.chartsflowui.kit.component.model.toolbox.*;
import io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap;
import io.jmix.chartsflowui.kit.component.model.visualMap.ContinuousVisualMap;
import io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap;
import io.jmix.flowui.kit.meta.*;

@StudioUiKit
public interface StudioChartsElements {

    @StudioElement(
            name = "StateAnimation",
            classFqn = "io.jmix.chartsflowui.kit.component.model.ChartOptions.StateAnimation",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.STATE_ANIMATION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.StateAnimationComponent.class)
    ChartOptions.StateAnimation stateAnimation();

    @StudioElement(
            name = "TextStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.TextStyle",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.TEXT_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.TextStyleDefaultProperties.class)
    TextStyle globalTextStyle();

    @StudioElement(
            name = "Title",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Title",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.TITLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.TitleComponent.class)
    Title title();

    @StudioElement(
            name = "TextStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Title.TextStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.Title"},
            xmlElement = StudioXmlElements.TEXT_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.TextStyleDefaultProperties.class)
    Title.TextStyle titleTextStyle();

    @StudioElement(
            name = "SubtextStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Title.SubtextStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.Title"},
            xmlElement = StudioXmlElements.SUBTEXT_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.TextStyleDefaultProperties.class,
                    StudioChartsPropertyGroups.SharedAlign.class,
                    StudioChartsPropertyGroups.VerticalAlign.class
            })
    Title.SubtextStyle titleSubtextStyle();

    @StudioElement(
            name = "RichStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.RichStyle",
            xmlElement = StudioXmlElements.RICH_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.RichStyleComponent.class)
    RichStyle richStyle();

    @StudioElement(
            name = "Brush",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Brush",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.BRUSH,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.BrushComponent.class)
    Brush brush();

    @StudioElement(
            name = "BrushStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Brush.BrushStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.Brush"},
            xmlElement = StudioXmlElements.BRUSH_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.IntegerBorderWidth.class,
                    StudioChartsPropertyGroups.ChartColor.class,
                    StudioChartsPropertyGroups.BorderColor.class
            })
    Brush.BrushStyle brushBrushStyle();

    @StudioElement(
            name = "InBrush",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.VisualEffect",
            target = {"io.jmix.chartsflowui.kit.component.model.Brush"},
            xmlElement = StudioXmlElements.IN_BRUSH,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.InBrushComponent.class)
    VisualEffect inBrush();

    @StudioElement(
            name = "OutOfBrush",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.VisualEffect",
            target = {"io.jmix.chartsflowui.kit.component.model.Brush"},
            xmlElement = StudioXmlElements.OUT_OF_BRUSH,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.OutOfBrushComponent.class)
    VisualEffect outOfBrush();

    @StudioElement(
            name = "InRange",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.VisualEffect",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap",
                    "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap.VisualMapController"
            },
            xmlElement = StudioXmlElements.IN_RANGE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.InRangeComponent.class)
    VisualEffect inRange();

    @StudioElement(
            name = "OutRange",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.VisualEffect",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap",
                    "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap.VisualMapController"
            },
            xmlElement = StudioXmlElements.OUT_RANGE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.OutRangeComponent.class)
    VisualEffect outRange();

    @StudioElement(
            name = "Tooltip",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Tooltip",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend",
                    "io.jmix.chartsflowui.component.Chart",
                    "io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox"
            },
            xmlElement = StudioXmlElements.TOOLTIP,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.TooltipComponent.class)
    Tooltip tooltip();

    @StudioElement(
            name = "GridItem",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Grid",
            xmlElement = StudioXmlElements.GRID_ITEM,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            isInjectable = false,
            propertyGroups = StudioChartsPropertyGroups.GridItemComponent.class)
    Grid gridItem();

    @StudioElement(
            name = "TextStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend.TextStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend"},
            xmlElement = StudioXmlElements.TEXT_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.TextStyleDefaultProperties.class,
                    StudioChartsPropertyGroups.BoxStyleDefaultPropertiesWithDoubleBorderWidth.class
            })
    AbstractLegend.TextStyle legendTextStyle();

    @StudioElement(
            name = "ScrollableLegend",
            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend",
            xmlElement = StudioXmlElements.SCROLLABLE_LEGEND,
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ScrollableLegendComponent.class)
    ScrollableLegend scrollableLegend();

    @StudioElement(
            name = "Legend",
            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.Legend",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.LEGEND,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LegendComponent.class)
    Legend legend();

    @StudioElement(
            name = "PageIcons",
            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend.PageIcons",
            target = "io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend",
            xmlElement = StudioXmlElements.PAGE_ICONS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    ScrollableLegend.PageIcons pageIcons();

    @StudioElement(
            name = "HorizontalPageIcons",
            target = "io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend.PageIcons",
            xmlElement = StudioXmlElements.HORIZONTAL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.PageIconsDefaultProperties.class)
    void horizontalPageIcons();

    @StudioElement(
            name = "VerticalPageIcons",
            target = "io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend.PageIcons",
            xmlElement = StudioXmlElements.VERTICAL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.PageIconsDefaultProperties.class)
    void verticalPageIcons();

    @StudioElement(
            name = "PageTextStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.TextStyle",
            xmlElement = StudioXmlElements.PAGE_TEXT_STYLE,
            target = {"io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend"},
            xmlns = "http://jmix.io/schema/charts/ui",
            unlimitedCount = false,
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.TextStyleDefaultProperties.class)
    TextStyle pageTextStyle();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    AbstractLegend.Emphasis legendEmphasis();

    @StudioElement(
            name = "SelectorLabel",
            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend.SelectorLabel",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend",
                    "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend.Emphasis"
            },
            xmlElement = StudioXmlElements.SELECTOR_LABEL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.SelectorLabelComponent.class)
    AbstractLegend.SelectorLabel selectorLabel();

    @StudioElement(
            name = "ItemStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.ItemStyle",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.AbstractMarkElement",
                    "io.jmix.chartsflowui.kit.component.model.series.LineSeries.AbstractLineElement",
                    "io.jmix.chartsflowui.kit.component.model.series.PieSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.ScatterSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.BoxplotSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.PieSeries.AbstractPieElement",
                    "io.jmix.chartsflowui.kit.component.model.series.ScatterSeries.AbstractScatterElement",
                    "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.AbstractEffectScatterElement",
                    "io.jmix.chartsflowui.kit.component.model.series.RadarSeries.AbstractRadarElement",
                    "io.jmix.chartsflowui.kit.component.model.series.BoxplotSeries.AbstractBoxplotElement",
                    "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries.AbstractFunnelElement",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.DataItem",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Progress",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Pointer",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Anchor",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Emphasis"
            },
            xmlElement = StudioXmlElements.ITEM_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ItemStyleDefaultProperties.class)
    ItemStyle itemStyle();

    @StudioElement(
            name = "HandleStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.ItemStyle",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom",
                    "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom.Emphasis",
                    "io.jmix.chartsflowui.kit.component.model.visualMap.ContinuousVisualMap"
            },
            xmlElement = StudioXmlElements.HANDLE_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ItemStyleDefaultProperties.class)
    ItemStyle handleStyle();

    @StudioElement(
            name = "IndicatorStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.ItemStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.visualMap.ContinuousVisualMap"},
            xmlElement = StudioXmlElements.INDICATOR_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ItemStyleDefaultProperties.class)
    ItemStyle indicatorStyle();

    @StudioElement(
            name = "MoveHandleStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.ItemStyle",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom",
                    "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom.Emphasis"
            },
            xmlElement = StudioXmlElements.MOVE_HANDLE_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ItemStyleDefaultProperties.class)
    ItemStyle moveHandleStyle();

    @StudioElement(
            name = "BrushStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.ItemStyle",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom",
                    "io.jmix.chartsflowui.kit.component.model.toolbox.DataZoomFeature"
            },
            xmlElement = StudioXmlElements.BRUSH_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ItemStyleDefaultProperties.class)
    ItemStyle brushStyle();

    @StudioElement(
            name = "IconStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.ItemStyle",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox",
                    "io.jmix.chartsflowui.kit.component.model.toolbox.AbstractFeature"
            },
            xmlElement = StudioXmlElements.ICON_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ItemStyleDefaultProperties.class)
    ItemStyle iconStyle();

    @StudioElement(
            name = "Polar",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.Polar",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.POLAR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.PolarDefaultProperties.class)
    Polar polar();

    @StudioElement(
            name = "InnerTooltip",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.InnerTooltip",
            target = {"io.jmix.chartsflowui.kit.component.model.axis.Polar",
                    "io.jmix.chartsflowui.kit.component.model.Grid"},
            xmlElement = StudioXmlElements.TOOLTIP,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.InnerTooltipDefaultProperties.class)
    InnerTooltip innerTooltip();

    @StudioElement(
            name = "AxisPointer",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer",
            xmlElement = StudioXmlElements.AXIS_POINTER,
            target = {"io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip"},
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.TooltipAxisPointerComponent.class)
    AbstractTooltip.AxisPointer tooltipAxisPointer();

    @StudioElement(
            name = "AxisPointer",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis.AxisPointer",
            xmlElement = StudioXmlElements.AXIS_POINTER,
            target = {"io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"},
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AxisPointerDefaultProperties.class)
    AbstractAxis.AxisPointer axisAxisPointer();

    @StudioElement(
            name = "AxisPointer",
            classFqn = "io.jmix.chartsflowui.kit.component.model.AxisPointer",
            xmlElement = StudioXmlElements.AXIS_POINTER,
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.GlobalAxisPointerComponent.class)
    AxisPointer globalAxisPointer();

    @StudioElement(
            name = "Handle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AbstractAxisPointer.Handle",
            target = {"io.jmix.chartsflowui.kit.component.model.shared.AbstractAxisPointer"},
            xmlElement = StudioXmlElements.HANDLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AxisPointerHandleComponent.class)
    AbstractAxisPointer.Handle axisPointerHandle();

    @StudioElement(
            name = "Label",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Label",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer",
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractAxisPointer"
            },
            xmlElement = StudioXmlElements.LABEL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AxisPointerLabelDefaultProperties.class)
    Label label();

    @StudioElement(
            name = "Radar",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.Radar",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.RADAR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.RadarComponent.class)
    Radar radar();

    @StudioElement(
            name = "Indicator",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.Radar.Indicator",
            xmlElement = StudioXmlElements.INDICATOR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.IndicatorDefaultProperties.class)
    Radar.Indicator indicator();

    @StudioElement(
            name = "AxisName",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.Radar.AxisName",
            target = "io.jmix.chartsflowui.kit.component.model.axis.Radar",
            xmlElement = StudioXmlElements.AXIS_NAME,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Show.class,
                    StudioChartsPropertyGroups.FormatterDefaultProperties.class,
                    StudioChartsPropertyGroups.TextStyleWithBoxDefaultProperties.class
            })
    Radar.AxisName axisName();

    @StudioElement(
            name = "SplitArea",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.SplitArea",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.axis.Radar",
                    "io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"
            },
            xmlElement = StudioXmlElements.SPLIT_AREA,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.SplitDefaultProperties.class
            })
    SplitArea splitArea();

    @StudioElement(
            name = "AreaStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AreaStyle",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.axis.SplitArea",
                    "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom.DataBackground"
            },
            xmlElement = StudioXmlElements.AREA_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AreaStyleComponent.class)
    AreaStyle areaStyle();

    @StudioElement(
            name = "AxisLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AxisLine",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.axis.Radar",
                    "io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"
            },
            xmlElement = StudioXmlElements.AXIS_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AxisLineComponent.class)
    AxisLine axisLine();

    @StudioElement(
            name = "SplitLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.SplitLine",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.axis.Radar",
                    "io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"
            },
            xmlElement = StudioXmlElements.SPLIT_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.SplitDefaultProperties.class
            })
    SplitLine splitLine();

    @StudioElement(
            name = "MinorSplitLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.MinorSplitLine",
            target = {"io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"},
            xmlElement = StudioXmlElements.MINOR_SPLIT_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Show.class
            })
    MinorSplitLine minorSplitLine();

    @StudioElement(
            name = "AxisLabel",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AxisLabel",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.axis.Radar",
                    "io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"
            },
            xmlElement = StudioXmlElements.AXIS_LABEL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AxisLabelComponent.class)
    AxisLabel axisLabel();

    @StudioElement(
            name = "AxisTick",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AxisTick",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.axis.Radar",
                    "io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"
            },
            xmlElement = StudioXmlElements.AXIS_TICK,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AxisTickComponent.class)
    AxisTick axisTick();

    @StudioElement(
            name = "MinorTick",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.MinorTick",
            target = {"io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"},
            xmlElement = StudioXmlElements.MINOR_TICK,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Show.class,
                    StudioChartsPropertyGroups.SplitNumber.class,
                    StudioChartsPropertyGroups.Length.class
            })
    MinorTick minorTick();

    @StudioElement(
            name = "LineStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.LineStyle",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.axis.AxisTick",
                    "io.jmix.chartsflowui.kit.component.model.axis.MinorTick",
                    "io.jmix.chartsflowui.kit.component.model.axis.SplitLine",
                    "io.jmix.chartsflowui.kit.component.model.axis.MinorSplitLine",
                    "io.jmix.chartsflowui.kit.component.model.series.BarSeries.LabelLine",
                    "io.jmix.chartsflowui.kit.component.model.series.LineSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.ScatterSeries.LabelLine",
                    "io.jmix.chartsflowui.kit.component.model.series.LineSeries.LabelLine",
                    "io.jmix.chartsflowui.kit.component.model.series.PieSeries.LabelLine",
                    "io.jmix.chartsflowui.kit.component.model.axis.AxisLine",
                    "io.jmix.chartsflowui.kit.component.model.series.RadarSeries",
                    "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom.DataBackground",
                    "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend",
                    "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.LabelLine",
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer",
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractAxisPointer",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.AbstractMarkLineElement",
                    "io.jmix.chartsflowui.kit.component.model.series.ElementLabelLine",
                    "io.jmix.chartsflowui.kit.component.model.series.LineSeries.AbstractLineElement",
                    "io.jmix.chartsflowui.kit.component.model.series.RadarSeries.AbstractRadarElement",
                    "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries.LabelLine",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.SplitLine",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.AxisTick"
            },
            xmlElement = StudioXmlElements.LINE_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LineStyleDefaultProperties.class)
    LineStyle lineStyle();

    @StudioElement(
            name = "CrossStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.LineStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer"},
            xmlElement = StudioXmlElements.CROSS_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LineStyleDefaultProperties.class)
    LineStyle crossStyle();

    @StudioElement(
            name = "XAxis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.XAxis",
            xmlElement = StudioXmlElements.X_AXIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.CartesianAxisDefaultProperties.class
            })
    XAxis xAxis();

    @StudioElement(
            name = "YAxis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.YAxis",
            xmlElement = StudioXmlElements.Y_AXIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.CartesianAxisDefaultProperties.class
            })
    YAxis yAxis();

    @StudioElement(
            name = "AngleAxis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AngleAxis",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.ANGLE_AXIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AngleAxisDefaultProperties.class)
    AngleAxis angleAxis();

    @StudioElement(
            name = "RadiusAxis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.RadiusAxis",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.RADIUS_AXIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.RadiusAxisComponent.class)
    RadiusAxis radiusAxis();

    @StudioElement(
            name = "NameTextStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.HasAxisName.NameTextStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.axis.HasAxisName"},
            xmlElement = StudioXmlElements.NAME_TEXT_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.TextStyleWithBoxDefaultProperties.class,
                    StudioChartsPropertyGroups.SharedAlign.class,
                    StudioChartsPropertyGroups.VerticalAlign.class
            })
    HasAxisName.NameTextStyle nameTextStyle();

    @StudioElement(
            name = "ShadowStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.ShadowStyle",
            xmlElement = StudioXmlElements.SHADOW_STYLE,
            target = {
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer",
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractAxisPointer"
            },
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.AreaStyleDefaultProperties.class
            })
    ShadowStyle shadowStyle();

    @StudioElement(
            name = "InsideDataZoom",
            classFqn = "io.jmix.chartsflowui.kit.component.model.datazoom.InsideDataZoom",
            xmlElement = StudioXmlElements.INSIDE_DATA_ZOOM,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.InsideDataZoomComponent.class)
    InsideDataZoom insideDataZoom();

    @StudioElement(
            name = "SliderDataZoom",
            classFqn = "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom",
            xmlElement = StudioXmlElements.SLIDER_DATA_ZOOM,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.SliderDataZoomComponent.class)
    SliderDataZoom sliderDataZoom();

    @StudioElement(
            name = "DataBackground",
            classFqn = "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom.DataBackground",
            target = {"io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom"},
            xmlElement = StudioXmlElements.DATA_BACKGROUND,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    SliderDataZoom.DataBackground dataBackground();

    @StudioElement(
            name = "SelectedDataBackground",
            classFqn = "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom.DataBackground",
            target = {"io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom"},
            xmlElement = StudioXmlElements.SELECTED_DATA_BACKGROUND,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    SliderDataZoom.DataBackground selectedDataBackground();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    SliderDataZoom.Emphasis sliderDataZoomEmphasis();

    @StudioElement(
            name = "ContinuousVisualMap",
            classFqn = "io.jmix.chartsflowui.kit.component.model.visualMap.ContinuousVisualMap",
            xmlElement = StudioXmlElements.CONTINUOUS_VISUAL_MAP,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ContinuousVisualMapComponent.class)
    ContinuousVisualMap continuousVisualMap();

    @StudioElement(
            name = "PiecewiseVisualMap",
            classFqn = "io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap",
            xmlElement = StudioXmlElements.PIECEWISE_VISUAL_MAP,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.PiecewiseVisualMapComponent.class)
    PiecewiseVisualMap piecewiseVisualMap();

    @StudioElement(
            name = "Piece",
            classFqn = "io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap.Piece",
            xmlElement = StudioXmlElements.PIECE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.PieceDefaultProperties.class)
    PiecewiseVisualMap.Piece piece();

    @StudioElement(
            name = "Controller",
            classFqn = "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap.VisualMapController",
            xmlElement = StudioXmlElements.CONTROLLER,
            target = {"io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap"},
            xmlns = "http://jmix.io/schema/charts/ui",
            unlimitedCount = false,
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    AbstractVisualMap.VisualMapController visualMapController();

    @StudioElement(
            name = "TextStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.TextStyle",
            xmlElement = StudioXmlElements.TEXT_STYLE,
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.AbstractSeries.Tooltip",
                    "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap",
                    "io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom",
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip"
            },
            xmlns = "http://jmix.io/schema/charts/ui",
            unlimitedCount = false,
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.TextStyleDefaultProperties.class)
    TextStyle textStyle();

    @StudioElement(
            name = "Line",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries",
            xmlElement = StudioXmlElements.LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/element/lineSeries.svg",
            propertyGroups = StudioChartsPropertyGroups.LineSeriesComponent.class)
    LineSeries lineSeries();

    @StudioElement(
            name = "EndLabel",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.EndLabel",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.LineSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.LineSeries.AbstractLineElement"
            },
            xmlElement = StudioXmlElements.END_LABEL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.EndLabelComponent.class)
    LineSeries.EndLabel endLabel();

    @StudioElement(
            name = "ItemStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.ItemStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.series.LineSeries"},
            xmlElement = StudioXmlElements.ITEM_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ItemStyleDefaultProperties.class)
    LineSeries.ItemStyle lineSeriesItemStyle();

    @StudioElement(
            name = "EmptyCircleStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.ItemStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.series.PieSeries"},
            xmlElement = StudioXmlElements.EMPTY_CIRCLE_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ItemStyleDefaultProperties.class)
    ItemStyle emptyCircleStyle();

    @StudioElement(
            name = "LabelLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.LabelLine",
            target = {"io.jmix.chartsflowui.kit.component.model.series.LineSeries"},
            xmlElement = StudioXmlElements.LABEL_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LabelLineDefaultProperties.class)
    LineSeries.LabelLine lineSeriesLabelLine();

    @StudioElement(
            name = "AreaStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.AreaStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.series.LineSeries"},
            xmlElement = StudioXmlElements.AREA_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LineSeriesAreaStyleComponent.class)
    LineSeries.AreaStyle lineSeriesAreaStyle();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.Blur",
            target = {"io.jmix.chartsflowui.kit.component.model.series.LineSeries"},
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    LineSeries.Blur lineSeriesBlur();

    @StudioElement(
            name = "Select",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.Select",
            target = {"io.jmix.chartsflowui.kit.component.model.series.LineSeries"},
            xmlElement = StudioXmlElements.SELECT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    LineSeries.Select lineSeriesSelect();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.series.LineSeries"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.DoubleScaleEmphasisDefaultProperties.class)
    LineSeries.Emphasis lineSeriesEmphasis();

    @StudioElement(
            name = "LabelLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ElementLabelLine",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.LineSeries.AbstractLineElement",
                    "io.jmix.chartsflowui.kit.component.model.series.PieSeries.AbstractPieElement",
                    "io.jmix.chartsflowui.kit.component.model.series.ScatterSeries.AbstractScatterElement",
                    "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.AbstractEffectScatterElement",
                    "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries.AbstractFunnelElement"
            },
            xmlElement = StudioXmlElements.LABEL_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Show.class
            })
    ElementLabelLine elementLabelLine();

    @StudioElement(
            name = "AreaStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.AbstractLineElement.AreaStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.series.LineSeries.AbstractLineElement"},
            xmlElement = StudioXmlElements.AREA_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AreaStyleDefaultProperties.class)
    LineSeries.AbstractLineElement.AreaStyle lineElementAreaStyle();

    @StudioElement(
            name = "Bar",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BarSeries",
            xmlElement = StudioXmlElements.BAR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/element/barSeries.svg",
            propertyGroups = StudioChartsPropertyGroups.BarSeriesComponent.class)
    BarSeries barSeries();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BarSeries.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.series.BarSeries"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.EmphasisDefaultProperties.class)
    BarSeries.Emphasis barSeriesEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BarSeries.Blur",
            target = {"io.jmix.chartsflowui.kit.component.model.series.BarSeries"},
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    BarSeries.Blur barSeriesBlur();

    @StudioElement(
            name = "Select",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BarSeries.Select",
            target = {"io.jmix.chartsflowui.kit.component.model.series.BarSeries"},
            xmlElement = StudioXmlElements.SELECT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    BarSeries.Select barSeriesSelect();

    @StudioElement(
            name = "LabelLayout",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.AbstractSeries.LabelLayout",
            target = {"io.jmix.chartsflowui.kit.component.model.series.AbstractSeries"},
            unsupportedTarget = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.LABEL_LAYOUT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LabelLayoutComponent.class)
    AbstractSeries.LabelLayout labelLayout();

    @StudioElement(
            name = "Tooltip",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.AbstractSeries.Tooltip",
            target = {"io.jmix.chartsflowui.kit.component.model.series.AbstractSeries"},
            xmlElement = StudioXmlElements.TOOLTIP,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.TooltipStyleDefaultProperties.class)
    AbstractSeries.Tooltip seriesTooltip();

    @StudioElement(
            name = "MarkPoint",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.AbstractAxisAwareSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"
            },
            xmlElement = StudioXmlElements.MARK_POINT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.SymbolDefaultProperties.class,
                    StudioChartsPropertyGroups.Silent.class,
                    StudioChartsPropertyGroups.AnimationDefaultProperties.class
            })
    MarkPoint markPoint();

    @StudioElement(
            name = "MarkLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.AbstractAxisAwareSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"
            },
            xmlElement = StudioXmlElements.MARK_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.MarkLineComponent.class)
    MarkLine markLine();

    @StudioElement(
            name = "MarkArea",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.AbstractAxisAwareSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"
            },
            xmlElement = StudioXmlElements.MARK_AREA,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Silent.class,
                    StudioChartsPropertyGroups.AnimationDefaultProperties.class
            })
    MarkArea markArea();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.Emphasis",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.Point"
            },
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    MarkLine.Emphasis markLineEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.Blur",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.Point"
            },
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    MarkLine.Blur markLineBlur();

    @StudioElement(
            name = "Data",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.Data",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine"},
            xmlElement = StudioXmlElements.DATA,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    MarkLine.Data markLineData();

    @StudioElement(
            name = "SinglePointLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.Point",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.Data"},
            xmlElement = StudioXmlElements.SINGLE_POINT_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LinePointDefaultProperties.class)
    MarkLine.Point markLineSinglePointLine();

    @StudioElement(
            name = "PairPointLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.PointPair",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.Data"},
            xmlElement = StudioXmlElements.PAIR_POINT_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    MarkLine.PointPair markLinePairPointLine();

    @StudioElement(
            name = "StartPoint",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.Point",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.PointPair"},
            xmlElement = StudioXmlElements.START_POINT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LinePointDefaultProperties.class)
    MarkLine.Point markLineStartPoint();

    @StudioElement(
            name = "EndPoint",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.Point",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.PointPair"},
            xmlElement = StudioXmlElements.END_POINT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LinePointDefaultProperties.class)
    MarkLine.Point markLineEndPoint();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint.Emphasis",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint.Point"
            },
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    MarkPoint.Emphasis markPointEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint.Blur",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint"},
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    MarkPoint.Blur markPointBlur();

    @StudioElement(
            name = "Point",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint.Point",
            xmlElement = StudioXmlElements.POINT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.MarkPointPointDefaultProperties.class)
    MarkPoint.Point markPointPoint();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.Emphasis",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.Point"
            },
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    MarkArea.Emphasis markAreaEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.Blur",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.Point"
            },
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    MarkArea.Blur markAreaBlur();

    @StudioElement(
            name = "PointPair",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.PointPair",
            xmlElement = StudioXmlElements.POINT_PAIR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    MarkArea.PointPair markAreaPointPair();

    @StudioElement(
            name = "LeftTopPoint",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.Point",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.PointPair"},
            xmlElement = StudioXmlElements.LEFT_TOP_POINT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.PointDefaultProperties.class)
    MarkArea.Point leftTopPoint();

    @StudioElement(
            name = "RightBottomPoint",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.Point",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.PointPair"},
            xmlElement = StudioXmlElements.RIGHT_BOTTOM_POINT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.PointDefaultProperties.class)
    MarkArea.Point rightBottomPoint();

    @StudioElement(
            name = "AxisLabel",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.Label",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.AXIS_LABEL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.GaugeSeriesLabelComponent.class)
    io.jmix.chartsflowui.kit.component.model.series.Label gaugeSeriesLabel();

    @StudioElement(
            name = "Label",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.Label",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.AbstractSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.AbstractMark",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.AbstractMarkElement",
                    "io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine.AbstractMarkLineElement",
                    "io.jmix.chartsflowui.kit.component.model.series.LineSeries.AbstractLineElement",
                    "io.jmix.chartsflowui.kit.component.model.series.BarSeries.AbstractBarElement",
                    "io.jmix.chartsflowui.kit.component.model.series.PieSeries.AbstractPieElement",
                    "io.jmix.chartsflowui.kit.component.model.series.ScatterSeries.AbstractScatterElement",
                    "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.AbstractEffectScatterElement",
                    "io.jmix.chartsflowui.kit.component.model.series.RadarSeries.AbstractRadarElement",
                    "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries.AbstractFunnelElement"
            },
            unsupportedTarget = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.LABEL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.SeriesLabelComponent.class)
    io.jmix.chartsflowui.kit.component.model.series.Label seriesLabel();

    @StudioElement(
            name = "Encode",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.Encode",
            target = {"io.jmix.chartsflowui.kit.component.model.series.AbstractAxisAwareSeries"},
            xmlElement = StudioXmlElements.ENCODE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.EncodeComponent.class)
    Encode encode();

    @StudioElement(
            name = "BackgroundStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BarSeries.BackgroundStyle",
            xmlElement = StudioXmlElements.BACKGROUND_STYLE,
            target = "io.jmix.chartsflowui.kit.component.model.series.BarSeries",
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.RoundedItemStyleDefaultProperties.class)
    BarSeries.BackgroundStyle backgroundStyle();

    @StudioElement(
            name = "LabelLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BarSeries.LabelLine",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.BarSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.BarSeries.AbstractBarElement"
            },
            xmlElement = StudioXmlElements.LABEL_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Show.class
            })
    BarSeries.LabelLine labelLine();

    @StudioElement(
            name = "ItemStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ItemStyleWithDecal",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.BarSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.RadarSeries"
            },
            xmlElement = StudioXmlElements.ITEM_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.RoundedItemStyleDefaultProperties.class)
    ItemStyleWithDecal itemStyleWithDecal();

    @StudioElement(
            name = "ItemStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BarSeries.ItemStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.series.BarSeries.AbstractBarElement"},
            xmlElement = StudioXmlElements.ITEM_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.RoundedItemStyleDefaultProperties.class)
    BarSeries.ItemStyle barSeriesItemStyle();

    @StudioElement(
            name = "Decal",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Decal",
            target = {"io.jmix.chartsflowui.kit.component.model.series.ItemStyleWithDecal",
                    "io.jmix.chartsflowui.kit.component.model.series.LineSeries.ItemStyle"},
            xmlElement = StudioXmlElements.DECAL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.DecalComponent.class)
    Decal decal();

    @StudioElement(
            name = "Pie",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.PieSeries",
            xmlElement = StudioXmlElements.PIE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/element/pieSeries.svg",
            propertyGroups = StudioChartsPropertyGroups.PieSeriesComponent.class)
    PieSeries pieSeries();

    @StudioElement(
            name = "LabelLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.PieSeries.LabelLine",
            target = {"io.jmix.chartsflowui.kit.component.model.series.PieSeries"},
            xmlElement = StudioXmlElements.LABEL_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.PieSeriesLabelLineComponent.class)
    PieSeries.LabelLine pieSeriesLabelLine();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.PieSeries.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.series.PieSeries"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.PieSeriesEmphasisComponent.class)
    PieSeries.Emphasis pieSeriesEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.PieSeries.Blur",
            target = {"io.jmix.chartsflowui.kit.component.model.series.PieSeries"},
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    PieSeries.Blur pieSeriesBlur();

    @StudioElement(
            name = "Selected",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.PieSeries.Selected",
            target = {"io.jmix.chartsflowui.kit.component.model.series.PieSeries"},
            xmlElement = StudioXmlElements.SELECTED,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    PieSeries.Selected pieSeriesSelected();

    @StudioElement(
            name = "Scatter",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ScatterSeries",
            xmlElement = StudioXmlElements.SCATTER,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/element/scatterSeries.svg",
            propertyGroups = StudioChartsPropertyGroups.ScatterSeriesDefaultProperties.class)
    ScatterSeries scatterSeries();

    @StudioElement(
            name = "LabelLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ScatterSeries.LabelLine",
            target = {"io.jmix.chartsflowui.kit.component.model.series.ScatterSeries"},
            xmlElement = StudioXmlElements.LABEL_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LabelLineDefaultProperties.class)
    ScatterSeries.LabelLine scatterSeriesLabelLine();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ScatterSeries.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.series.ScatterSeries"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ScaleEmphasisDefaultProperties.class)
    ScatterSeries.Emphasis scatterSeriesEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ScatterSeries.Blur",
            target = {"io.jmix.chartsflowui.kit.component.model.series.ScatterSeries"},
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    ScatterSeries.Blur scatterSeriesBlur();

    @StudioElement(
            name = "Select",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ScatterSeries.Select",
            target = {"io.jmix.chartsflowui.kit.component.model.series.ScatterSeries"},
            xmlElement = StudioXmlElements.SELECT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    ScatterSeries.Select scatterSeriesSelect();

    @StudioElement(
            name = "EffectScatter",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries",
            xmlElement = StudioXmlElements.EFFECT_SCATTER,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/element/scatterSeries.svg",
            propertyGroups = StudioChartsPropertyGroups.EffectScatterSeriesComponent.class)
    EffectScatterSeries effectScatterSeries();

    @StudioElement(
            name = "RippleEffect",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.RippleEffect",
            target = {"io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries"},
            xmlElement = StudioXmlElements.RIPPLE_EFFECT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.RippleEffectComponent.class)
    EffectScatterSeries.RippleEffect rippleEffect();

    @StudioElement(
            name = "LabelLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.LabelLine",
            target = {"io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries"},
            xmlElement = StudioXmlElements.LABEL_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.LabelLineDefaultProperties.class)
    EffectScatterSeries.LabelLine effectScatterSeriesLabelLine();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ScaleEmphasisDefaultProperties.class)
    EffectScatterSeries.Emphasis effectScatterSeriesEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.Blur",
            target = {"io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries"},
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    EffectScatterSeries.Blur effectScatterSeriesBlur();

    @StudioElement(
            name = "Select",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.Select",
            target = {"io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries"},
            xmlElement = StudioXmlElements.SELECT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    EffectScatterSeries.Select effectScatterSeriesSelect();

    @StudioElement(
            name = "Radar",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.RadarSeries",
            xmlElement = StudioXmlElements.RADAR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/element/radarSeries.svg",
            propertyGroups = StudioChartsPropertyGroups.RadarSeriesComponent.class)
    RadarSeries radarSeries();

    @StudioElement(
            name = "AreaStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.RadarSeries.AreaStyle",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.RadarSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.RadarSeries.AbstractRadarElement"
            },
            xmlElement = StudioXmlElements.AREA_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AreaStyleDefaultProperties.class)
    RadarSeries.AreaStyle radarSeriesAreaStyle();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.RadarSeries.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.series.RadarSeries"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.EmphasisDefaultProperties.class)
    RadarSeries.Emphasis radarSeriesEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.RadarSeries.Blur",
            target = {"io.jmix.chartsflowui.kit.component.model.series.RadarSeries"},
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    RadarSeries.Blur radarSeriesBlur();

    @StudioElement(
            name = "Select",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.RadarSeries.Select",
            target = {"io.jmix.chartsflowui.kit.component.model.series.RadarSeries"},
            xmlElement = StudioXmlElements.SELECT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    RadarSeries.Select radarSeriesSelect();

    @StudioElement(
            name = "Boxplot",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BoxplotSeries",
            xmlElement = StudioXmlElements.BOXPLOT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.BoxplotSeriesComponent.class)
    BoxplotSeries boxplotSeries();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BoxplotSeries.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.series.BoxplotSeries"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.EmphasisDefaultProperties.class)
    BoxplotSeries.Emphasis boxplotSeriesEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BoxplotSeries.Blur",
            target = {"io.jmix.chartsflowui.kit.component.model.series.BoxplotSeries"},
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    BoxplotSeries.Blur boxplotSeriesBlur();

    @StudioElement(
            name = "Select",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BoxplotSeries.Select",
            target = {"io.jmix.chartsflowui.kit.component.model.series.BoxplotSeries"},
            xmlElement = StudioXmlElements.SELECT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    BoxplotSeries.Select boxplotSeriesSelect();

    @StudioElement(
            name = "Candlestick",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries",
            xmlElement = StudioXmlElements.CANDLESTICK,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/element/candlestickSeries.svg",
            propertyGroups = StudioChartsPropertyGroups.CandlestickSeriesDefaultProperties.class)
    CandlestickSeries candlestickSeries();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.EmphasisDefaultProperties.class)
    CandlestickSeries.Emphasis candlestickSeriesEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries.Blur",
            target = {"io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries"},
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    CandlestickSeries.Blur candlestickSeriesBlur();

    @StudioElement(
            name = "Select",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries.Select",
            target = {"io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries"},
            xmlElement = StudioXmlElements.SELECT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    CandlestickSeries.Select candlestickSeriesSelect();

    @StudioElement(
            name = "ItemStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries.ItemStyle",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries",
                    "io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries.AbstractCandlestickElement"
            },
            xmlElement = StudioXmlElements.ITEM_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.CandlestickSeriesItemStyleComponent.class)
    CandlestickSeries.ItemStyle candlestickSeriesItemStyle();

    @StudioElement(
            name = "Funnel",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries",
            xmlElement = StudioXmlElements.FUNNEL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/element/funnelSeries.svg",
            propertyGroups = StudioChartsPropertyGroups.FunnelSeriesComponent.class)
    FunnelSeries funnelSeries();

    @StudioElement(
            name = "LabelLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries.LabelLine",
            target = {"io.jmix.chartsflowui.kit.component.model.series.FunnelSeries"},
            xmlElement = StudioXmlElements.LABEL_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Show.class,
                    StudioChartsPropertyGroups.Length.class
            })
    FunnelSeries.LabelLine funnelSeriesLabelLine();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.series.FunnelSeries"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.EmphasisDefaultProperties.class)
    FunnelSeries.Emphasis funnelSeriesEmphasis();

    @StudioElement(
            name = "Blur",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries.Blur",
            target = {"io.jmix.chartsflowui.kit.component.model.series.FunnelSeries"},
            xmlElement = StudioXmlElements.BLUR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    FunnelSeries.Blur funnelSeriesBlur();

    @StudioElement(
            name = "Select",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries.Select",
            target = {"io.jmix.chartsflowui.kit.component.model.series.FunnelSeries"},
            xmlElement = StudioXmlElements.SELECT,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    FunnelSeries.Select funnelSeriesSelect();

    @StudioElement(
            name = "Gauge",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries",
            xmlElement = StudioXmlElements.GAUGE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/element/gaugeSeries.svg",
            propertyGroups = StudioChartsPropertyGroups.GaugeSeriesComponent.class)
    GaugeSeries gaugeSeries();

    @StudioElement(
            name = "DataItem",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.DataItem",
            xmlElement = StudioXmlElements.DATA_ITEM,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioPropertyGroups.LocalizedName.class,
                    StudioChartsPropertyGroups.DoubleValue.class
            })
    GaugeSeries.DataItem gaugeDataItem();

    @StudioElement(
            name = "Title",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Title",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.DataItem",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"
            },
            xmlElement = StudioXmlElements.TITLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.GaugeTextDefaultProperties.class)
    GaugeSeries.Title gaugeTitle();

    @StudioElement(
            name = "Detail",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Detail",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.DataItem",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"
            },
            xmlElement = StudioXmlElements.DETAIL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.GaugeTextDefaultProperties.class,
                    StudioChartsPropertyGroups.FormatterDefaultProperties.class
            })
    GaugeSeries.Detail gaugeDetail();

    @StudioElement(
            name = "AxisLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.AxisLine",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.AXIS_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Show.class,
                    StudioChartsPropertyGroups.RoundCap.class
            })
    GaugeSeries.AxisLine gaugeAxisLine();

    @StudioElement(
            name = "LineStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.AxisLine.LineStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.AxisLine"},
            xmlElement = StudioXmlElements.LINE_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioPropertyGroups.WidthWithIntegerType.class,
                    StudioChartsPropertyGroups.ShadowDefaultProperties.class,
                    StudioChartsPropertyGroups.Opacity.class
            })
    GaugeSeries.AxisLine.LineStyle gaugeAxisLineLineStyle();

    @StudioElement(
            name = "ColorItem",
            classFqn = "io.jmix.chartsflowui.kit.meta.StudioChartColorPalette",
            xmlElement = StudioXmlElements.COLOR_ITEM,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.GaugeAxisLineLineStyleColorItemComponent.class)
    void gaugeAxisLineLineStyleColorItem();

    @StudioElement(
            name = "Progress",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Progress",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.PROGRESS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.GaugeProgressComponent.class)
    GaugeSeries.Progress gaugeProgress();

    @StudioElement(
            name = "SplitLine",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.SplitLine",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.SPLIT_LINE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Show.class,
                    StudioChartsPropertyGroups.Length.class,
                    StudioChartsPropertyGroups.Distance.class
            })
    GaugeSeries.SplitLine gaugeSplitLine();

    @StudioElement(
            name = "AxisTick",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.AxisTick",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.AXIS_TICK,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.GaugeAxisTickDefaultProperties.class)
    GaugeSeries.AxisTick gaugeAxisTick();

    @StudioElement(
            name = "Pointer",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Pointer",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.POINTER,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.GaugePointerComponent.class)
    GaugeSeries.Pointer gaugePointer();

    @StudioElement(
            name = "Anchor",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Anchor",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.ANCHOR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.GaugeAnchorComponent.class)
    GaugeSeries.Anchor gaugeAnchor();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Emphasis",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Disabled.class
            })
    GaugeSeries.Emphasis gaugeEmphasis();

    @StudioElement(
            name = "Aria",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Aria",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.ARIA,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioPropertyGroups.EnabledWithoutDefaultValue.class
            })
    Aria aria();

    @StudioElement(
            name = "Label",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Aria.Label",
            target = {"io.jmix.chartsflowui.kit.component.model.Aria"},
            xmlElement = StudioXmlElements.LABEL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioPropertyGroups.EnabledWithoutDefaultValue.class,
                    StudioPropertyGroups.Description.class
            })
    Aria.Label ariaLabel();

    @StudioElement(
            name = "General",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Aria.Label.General",
            target = {"io.jmix.chartsflowui.kit.component.model.Aria.Label"},
            xmlElement = StudioXmlElements.GENERAL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.GeneralComponent.class)
    Aria.Label.General general();

    @StudioElement(
            name = "Series",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Aria.Label.Series",
            target = {"io.jmix.chartsflowui.kit.component.model.Aria.Label"},
            xmlElement = StudioXmlElements.SERIES,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.MaxCount.class
            })
    Aria.Label.Series ariaLabelSeries();

    @StudioElement(
            name = "Single",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Aria.Label.Series.Single",
            target = {"io.jmix.chartsflowui.kit.component.model.Aria.Label.Series"},
            xmlElement = StudioXmlElements.SINGLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.SeriesAriaLabelDefaultProperties.class)
    Aria.Label.Series.Single single();

    @StudioElement(
            name = "Multiple",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Aria.Label.Series.Multiple",
            target = {"io.jmix.chartsflowui.kit.component.model.Aria.Label.Series"},
            xmlElement = StudioXmlElements.MULTIPLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.SeriesAriaLabelDefaultProperties.class)
    Aria.Label.Series.Multiple multiple();

    @StudioElement(
            name = "Data",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Aria.Label.Data",
            target = {"io.jmix.chartsflowui.kit.component.model.Aria.Label"},
            xmlElement = StudioXmlElements.DATA,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.AriaLabelDataComponent.class)
    Aria.Label.Data ariaLabelData();

    @StudioElement(
            name = "Decal",
            classFqn = "io.jmix.chartsflowui.kit.component.model.Aria.Decal",
            target = {"io.jmix.chartsflowui.kit.component.model.Aria"},
            xmlElement = StudioXmlElements.DECAL,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.Show.class
            })
    Aria.Decal ariaDecal();

    @StudioElement(
            name = "Separator",
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Separator",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.Aria.Label.Series.Multiple",
                    "io.jmix.chartsflowui.kit.component.model.Aria.Label.Data"
            },
            xmlElement = StudioXmlElements.SEPARATOR,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.SeparatorComponent.class)
    Separator separator();

    @StudioElement(
            name = "Toolbox",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.TOOLBOX,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ToolboxComponent.class)
    Toolbox toolbox();

    @StudioElement(
            name = "Emphasis",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.Emphasis",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox",
                    "io.jmix.chartsflowui.kit.component.model.toolbox.AbstractFeature"
            },
            xmlElement = StudioXmlElements.EMPHASIS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    Emphasis toolboxEmphasis();

    @StudioElement(
            name = "IconStyle",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.Emphasis.IconStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.toolbox.Emphasis"},
            xmlElement = StudioXmlElements.ICON_STYLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.ToolboxEmphasisIconStyleComponent.class)
    Emphasis.IconStyle toolboxEmphasisIconStyle();

    @StudioElement(
            name = "BrushFeature",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.BrushFeature",
            xmlElement = StudioXmlElements.BRUSH,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.BrushFeatureComponent.class)
    BrushFeature brushFeature();

    @StudioElement(
            name = "Icon",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.BrushFeature.Icon",
            target = {"io.jmix.chartsflowui.kit.component.model.toolbox.BrushFeature"},
            xmlElement = StudioXmlElements.ICON,
            xmlns = "http://jmix.io/schema/charts/ui",
            unlimitedCount = false,
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.BrushFeatureIconComponent.class)
    BrushFeature.Icon brushFeatureIcon();

    @StudioElement(
            name = "Title",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.BrushFeature.Title",
            target = {"io.jmix.chartsflowui.kit.component.model.toolbox.BrushFeature"},
            xmlElement = StudioXmlElements.TITLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.BrushFeatureTitleComponent.class)
    BrushFeature.Title brushFeatureTitle();

    @StudioElement(
            name = "RestoreFeature",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.RestoreFeature",
            xmlElement = StudioXmlElements.RESTORE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioChartsPropertyGroups.ShowWithGeneralCategory.class,
                    StudioPropertyGroups.Title.class,
                    StudioPropertyGroups.IconString.class
            })
    RestoreFeature restoreFeature();

    @StudioElement(
            name = "MagicTypeFeature",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.MagicTypeFeature",
            xmlElement = StudioXmlElements.MAGIC_TYPE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.MagicTypeFeatureComponent.class)
    MagicTypeFeature magicTypeFeature();

    @StudioElement(
            name = "Icon",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.MagicTypeFeature.Icon",
            target = {"io.jmix.chartsflowui.kit.component.model.toolbox.MagicTypeFeature"},
            xmlElement = StudioXmlElements.ICON,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.MagicTypeFeatureIconComponent.class)
    MagicTypeFeature.Icon magicTypeFeatureIcon();

    @StudioElement(
            name = "Title",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.MagicTypeFeature.Title",
            target = {"io.jmix.chartsflowui.kit.component.model.toolbox.MagicTypeFeature"},
            xmlElement = StudioXmlElements.TITLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.MagicTypeFeatureTitleComponent.class)
    MagicTypeFeature.Title magicTypeFeatureTitle();

    @StudioElement(
            name = "DataZoomFeature",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.DataZoomFeature",
            xmlElement = StudioXmlElements.DATA_ZOOM,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.DataZoomFeatureDefaultProperties.class)
    DataZoomFeature dataZoomFeature();

    @StudioElement(
            name = "Icon",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.DataZoomFeature.Icon",
            target = {"io.jmix.chartsflowui.kit.component.model.toolbox.DataZoomFeature"},
            xmlElement = StudioXmlElements.ICON,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.DataZoomFeatureIconComponent.class)
    DataZoomFeature.Icon dataZoomFeatureIcon();

    @StudioElement(
            name = "Title",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.DataZoomFeature.Title",
            target = {"io.jmix.chartsflowui.kit.component.model.toolbox.DataZoomFeature"},
            xmlElement = StudioXmlElements.TITLE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.DataZoomFeatureTitleComponent.class)
    DataZoomFeature.Title dataZoomFeatureTitle();

    @StudioElement(
            name = "SaveAsImageFeature",
            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.SaveAsImageFeature",
            xmlElement = StudioXmlElements.SAVE_AS_IMAGE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.SaveAsImageFeatureComponent.class)
    SaveAsImageFeature saveAsImageFeature();

    @StudioElement(
            name = "DataSet",
            classFqn = "io.jmix.chartsflowui.kit.component.model.DataSet",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.DATA_SET,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            isInjectable = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioPropertyGroups.Id.class
            })
    DataSet dataSet();

    @SuppressWarnings("rawtypes")

    @StudioElement(
            name = "DataSource",
            classFqn = "io.jmix.chartsflowui.kit.component.model.DataSet.Source",
            target = {"io.jmix.chartsflowui.kit.component.model.DataSet"},
            xmlElement = StudioXmlElements.SOURCE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioChartsPropertyGroups.DataSourceComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "categoryField"
                    )
            }
    )
    DataSet.Source dataSource();

    @StudioElement(
            name = "Formatter function",
            xmlElement = StudioXmlElements.FORMATTER_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip",
                    "io.jmix.chartsflowui.kit.component.model.shared.Label",
                    "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend",
                    "io.jmix.chartsflowui.kit.component.model.axis.AxisLabel",
                    "io.jmix.chartsflowui.kit.component.model.axis.Radar.AxisName",
                    "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap",
                    "io.jmix.chartsflowui.kit.component.model.series.Label",
                    "io.jmix.chartsflowui.kit.component.model.series.LineSeries.EndLabel",
                    "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.Detail",
                    "io.jmix.chartsflowui.kit.component.model.series.AbstractSeries.Tooltip"
            },
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void formatterFunction();

    @StudioElement(
            name = "Value formatter function",
            xmlElement = StudioXmlElements.VALUE_FORMATTER_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip"
            },
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void valueFormatterFunction();

    @StudioElement(
            name = "Label formatter function",
            xmlElement = StudioXmlElements.LABEL_FORMATTER_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {"io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom"},
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void labelFormatterFunction();

    @StudioElement(
            name = "Animation duration function",
            xmlElement = StudioXmlElements.ANIMATION_DURATION_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer",
                    "io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"
            },
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void animationDurationFunction();

    @StudioElement(
            name = "Animation delay function",
            xmlElement = StudioXmlElements.ANIMATION_DELAY_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer",
                    "io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"
            },
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void animationDelayFunction();

    @StudioElement(
            name = "Animation duration update function",
            xmlElement = StudioXmlElements.ANIMATION_DURATION_UPDATE_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer",
                    "io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"
            },
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void animationDurationUpdateFunction();

    @StudioElement(
            name = "Animation delay update function",
            xmlElement = StudioXmlElements.ANIMATION_DELAY_UPDATE_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer",
                    "io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"
            },
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void animationDelayUpdateFunction();

    @StudioElement(
            name = "Page formatter function",
            xmlElement = StudioXmlElements.PAGE_FORMATTER_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {"io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend"},
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void pageFormatterFunction();

    @StudioElement(
            name = "Max function",
            xmlElement = StudioXmlElements.MAX_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {"io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"},
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void maxFunction();

    @StudioElement(
            name = "Min function",
            xmlElement = StudioXmlElements.MIN_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {"io.jmix.chartsflowui.kit.component.model.axis.AbstractAxis"},
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void minFunction();

    @StudioElement(
            name = "Interval function",
            xmlElement = StudioXmlElements.INTERVAL_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {
                    "io.jmix.chartsflowui.kit.component.model.axis.AxisTick",
                    "io.jmix.chartsflowui.kit.component.model.axis.AxisLabel",
                    "io.jmix.chartsflowui.kit.component.model.axis.SplitLine",
                    "io.jmix.chartsflowui.kit.component.model.axis.SplitArea"
            },
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void intervalFunction();

    @StudioElement(
            name = "Color function",
            xmlElement = StudioXmlElements.COLOR_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {"io.jmix.chartsflowui.kit.component.model.axis.AxisLabel"},
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void colorFunction();

    @StudioElement(
            name = "Sort function",
            xmlElement = StudioXmlElements.SORT_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {"io.jmix.chartsflowui.kit.component.model.series.FunnelSeries"},
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void sortFunction();

    @StudioElement(
            name = "Symbol size function",
            xmlElement = StudioXmlElements.SYMBOL_SIZE_FUNCTION,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {"io.jmix.chartsflowui.kit.component.model.shared.HasSymbols"},
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void symbolSizeFunction();

    @StudioElement(
            name = "NativeJson",
            xmlElement = StudioXmlElements.NATIVE_JSON,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            target = {"io.jmix.chartsflowui.component.Chart"},
            unlimitedCount = false,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void nativeJson();
}
