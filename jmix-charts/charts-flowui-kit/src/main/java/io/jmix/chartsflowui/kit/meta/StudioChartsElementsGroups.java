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
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
public interface StudioChartsElementsGroups {

    @StudioElementsGroup(
            name = "Rich",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.shared.RichStyle",
            target = {"io.jmix.chartsflowui.kit.component.model.shared.AbstractRichText"},
            xmlElement = StudioXmlElements.RICH,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void rich();

    @StudioElementsGroup(
            name = "Grid",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.Grid",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.GRID,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void grid();

    @StudioElementsGroup(
            name = "XAxes",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.axis.XAxis",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.X_AXES,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void xAxes();

    @StudioElementsGroup(
            name = "YAxes",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.axis.YAxis",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.Y_AXES,
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void yAxes();

    @StudioElementsGroup(
            name = "Indicators",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.axis.Radar.Indicator",
            target = {"io.jmix.chartsflowui.kit.component.model.axis.Radar"},
            xmlElement = StudioXmlElements.INDICATORS,
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void indicators();

    @StudioElementsGroup(
            name = "DataZoom",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.DATA_ZOOM,
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void dataZoom();

    @StudioElementsGroup(
            name = "VisualMap",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.VISUAL_MAP,
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void visualMap();

    @StudioElementsGroup(
            name = "Pieces",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap.Piece",
            target = {"io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap"},
            xmlElement = StudioXmlElements.PIECES,
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void pieces();

    @StudioElementsGroup(
            name = "Features",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.ToolboxFeature",
            target = {"io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox"},
            xmlElement = StudioXmlElements.FEATURES,
            xmlnsAlias = "charts",
            xmlns = "http://jmix.io/schema/charts/ui",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void toolboxFeatures();

    @StudioElementsGroup(
            name = "Decals",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.shared.Decal",
            target = {"io.jmix.chartsflowui.kit.component.model.Aria.Decal"},
            xmlElement = StudioXmlElements.DECALS,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void ariaDecals();

    @StudioElementsGroup(
            name = "Series",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.series.AbstractSeries",
            target = {"io.jmix.chartsflowui.component.Chart"},
            xmlElement = StudioXmlElements.SERIES,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void series();

    @StudioElementsGroup(
            name = "Data",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint.Point",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint"},
            xmlElement = StudioXmlElements.DATA,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void markPointData();

    @StudioElementsGroup(
            name = "Data",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea.PointPair",
            target = {"io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea"},
            xmlElement = StudioXmlElements.DATA,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void markAreaData();

    @StudioElementsGroup(
            name = "Data",
            elementClassFqn = "io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.DataItem",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries"},
            xmlElement = StudioXmlElements.DATA,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void gaugeData();

    @StudioElementsGroup(
            name = "ColorPalette",
            elementClassFqn = "io.jmix.chartsflowui.kit.meta.StudioChartColorPalette",
            target = {"io.jmix.chartsflowui.kit.component.model.series.GaugeSeries.AxisLine.LineStyle"},
            xmlElement = StudioXmlElements.COLOR_PALETTE,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void gaugeColorPalette();
}
