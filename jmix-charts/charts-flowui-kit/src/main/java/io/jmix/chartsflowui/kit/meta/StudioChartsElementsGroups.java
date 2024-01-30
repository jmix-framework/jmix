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

import io.jmix.flowui.kit.meta.StudioElementsGroup;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioChartsElementsGroups {

    @StudioElementsGroup(
            name = "Rich",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.shared.RichStyle",
            xmlElement = "rich",
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.kit.component.model.shared.AbstractRichText"}
    )
    void rich();

    @StudioElementsGroup(
            name = "Grid",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.Grid",
            xmlElement = "grid",
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.component.Chart"}
    )
    void grid();

    @StudioElementsGroup(
            name = "XAxes",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.axis.XAxis",
            xmlElement = "xAxes",
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.component.Chart"}
    )
    void xAxes();

    @StudioElementsGroup(
            name = "YAxes",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.axis.YAxis",
            xmlElement = "yAxes",
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.component.Chart"}
    )
    void yAxes();

    @StudioElementsGroup(
            name = "Indicators",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.axis.Radar.Indicator",
            xmlElement = "indicators",
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.kit.component.model.axis.Radar"}
    )
    void indicators();

    @StudioElementsGroup(
            name = "DataZoom",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom",
            xmlElement = "dataZoom",
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.component.Chart"}
    )
    void dataZoom();

    @StudioElementsGroup(
            name = "VisualMap",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap",
            xmlElement = "visualMap",
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.component.Chart"}
    )
    void visualMap();

    @StudioElementsGroup(
            name = "Pieces",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap.Piece",
            xmlElement = "pieces",
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap"}
    )
    void pieces();

    @StudioElementsGroup(
            name = "Features",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.ToolboxFeature",
            xmlElement = "features",
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox"}
    )
    void toolboxFeatures();

    @StudioElementsGroup(
            name = "Decals",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.shared.Decal",
            xmlElement = "decals",
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            visible = true,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.kit.component.model.Aria.Decal"}
    )
    void ariaDecals();

    @StudioElementsGroup(
            name = "Series",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.series.AbstractSeries",
            xmlElement = "series",
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            visible = true,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.component.Chart"}
    )
    void series();

    @StudioElementsGroup(
            name = "Data",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint.Point",
            xmlElement = "data",
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            visible = true,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint"}
    )
    void markPointData();

    @StudioElementsGroup(
            name = "Data",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.PointPair",
            xmlElement = "data",
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            visible = true,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea"}
    )
    void markAreaData();

    @StudioElementsGroup(
            name = "Data",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.DataItem",
            xmlElement = "data",
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            visible = true,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"}
    )
    void gaugeData();

    @StudioElementsGroup(
            name = "ColorPalette",
            elementClassFqn = "io.jmix.chartsflowui.kit.meta.StudioChartColorPalette",
            xmlElement = "colorPalette",
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            visible = true,
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.AxisLine.LineStyle"}
    )
    void gaugeColorPalette();
}
