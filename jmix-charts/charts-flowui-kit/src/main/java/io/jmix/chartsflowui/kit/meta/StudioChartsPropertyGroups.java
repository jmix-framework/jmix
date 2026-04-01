/*
 * Copyright 2026 Haulmont.
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

import io.jmix.flowui.kit.meta.*;

@StudioAPI
public final class StudioChartsPropertyGroups {

    private StudioChartsPropertyGroups() {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "align", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Align",
                            options = {"LEFT", "RIGHT", "CENTER"})
            }
    )
    public interface SharedAlign {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "align", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend.Align",
                            options = {"AUTO", "LEFT", "RIGHT"})
            }
    )
    public interface LegendAlign {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "align", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap.MapAlign",
                            options = {"AUTO", "LEFT", "TOP", "BOTTOM", "RIGHT"})
            }
    )
    public interface MapAlign {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "alignTicks", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface AlignTicks {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "angleAxisIndexes", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface AngleAxisIndexes {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animation", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Animation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationDelayFunction", type = StudioPropertyType.STRING)
            }
    )
    public interface AnimationDelayFunction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationDelay", type = StudioPropertyType.INTEGER)
            }
    )
    public interface AnimationDelay {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationDelayUpdateFunction", type = StudioPropertyType.STRING)
            }
    )
    public interface AnimationDelayUpdateFunction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationDelayUpdate", type = StudioPropertyType.INTEGER)
            }
    )
    public interface AnimationDelayUpdate {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationDurationFunction", type = StudioPropertyType.STRING)
            }
    )
    public interface AnimationDurationFunction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationDuration", type = StudioPropertyType.INTEGER)
            }
    )
    public interface AnimationDuration {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationDurationUpdateFunction", type = StudioPropertyType.STRING)
            }
    )
    public interface AnimationDurationUpdateFunction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationDurationUpdate", type = StudioPropertyType.INTEGER)
            }
    )
    public interface AnimationDurationUpdate {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationEasing", type = StudioPropertyType.STRING)
            }
    )
    public interface AnimationEasing {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationEasingUpdate", type = StudioPropertyType.STRING)
            }
    )
    public interface AnimationEasingUpdate {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "animationThreshold", type = StudioPropertyType.INTEGER)
            }
    )
    public interface AnimationThreshold {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "backgroundColor", type = StudioPropertyType.OPTIONS,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"})
            }
    )
    public interface BackgroundColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "barMaxWidth", type = StudioPropertyType.STRING)
            }
    )
    public interface BarMaxWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "barMinWidth", type = StudioPropertyType.STRING)
            }
    )
    public interface BarMinWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "barWidth", type = StudioPropertyType.STRING)
            }
    )
    public interface BarWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "blurScope", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BlurScopeType",
                            options = {"COORDINATE_SYSTEM", "SERIES", "GLOBAL"})
            }
    )
    public interface BlurScope {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "borderColor", type = StudioPropertyType.OPTIONS,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"})
            }
    )
    public interface BorderColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "borderDashOffset", type = StudioPropertyType.INTEGER)
            }
    )
    public interface BorderDashOffset {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "borderRadius", type = StudioPropertyType.INTEGER)
            }
    )
    public interface BorderRadius {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "borderType", type = StudioPropertyType.STRING)
            }
    )
    public interface BorderType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "borderWidth", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface DoubleBorderWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "borderWidth", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerBorderWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "bottom", type = StudioPropertyType.STRING)
            }
    )
    public interface Bottom {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "calendarIndex", type = StudioPropertyType.INTEGER)
            }
    )
    public interface CalendarIndex {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "cap", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.HasLineStyle.Cap",
                            options = {"BUTT", "ROUND", "SQUARE"})
            }
    )
    public interface Cap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "categoryBoundaryGap", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface CategoryBoundaryGap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "center", type = StudioPropertyType.STRING)
            }
    )
    public interface Center {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "clip", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Clip {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "clockwise", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Clockwise {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "colorAlpha", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface ColorAlpha {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "colorBy", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ColorBy",
                            options = {"SERIES", "DATA"})
            }
    )
    public interface ColorBy {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "colorHue", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface ColorHue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "colorLightness", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface ColorLightness {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "colorSaturation", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface ColorSaturation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "color", type = StudioPropertyType.VALUES_LIST,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"})
            }
    )
    public interface Color {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "coordinateSystem", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.CoordinateSystem",
                            options = {"CARTESIAN_2_D", "POLAR"})
            }
    )
    public interface CoordinateSystem {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "cursor", type = StudioPropertyType.STRING)
            }
    )
    public interface Cursor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "dashOffset", type = StudioPropertyType.INTEGER)
            }
    )
    public interface DashOffset {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "dataGroupId", type = StudioPropertyType.STRING)
            }
    )
    public interface DataGroupId {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "datasetIndex", type = StudioPropertyType.INTEGER)
            }
    )
    public interface DatasetIndex {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "dimension", type = StudioPropertyType.STRING)
            }
    )
    public interface Dimension {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "disabled", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Disabled {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "distance", type = StudioPropertyType.INTEGER)
            }
    )
    public interface Distance {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "ellipsis", type = StudioPropertyType.STRING)
            }
    )
    public interface Ellipsis {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "end", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface End {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "endValue", type = StudioPropertyType.STRING)
            }
    )
    public interface EndValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "extraCssText", type = StudioPropertyType.STRING)
            }
    )
    public interface ExtraCssText {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "filterMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom.FilterMode",
                            options = {"FILTER", "WEAK_FILTER", "EMPTY", "NONE"})
            }
    )
    public interface FilterMode {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "focus", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.FocusType",
                            options = {"NONE", "SELF", "SERIES"})
            }
    )
    public interface Focus {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fontFamily", type = StudioPropertyType.STRING)
            }
    )
    public interface FontFamily {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fontSize", type = StudioPropertyType.INTEGER)
            }
    )
    public interface FontSize {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fontStyle", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.FontStyle",
                            options = {"NORMAL", "ITALIC", "OBLIQUE"})
            }
    )
    public interface FontStyle {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fontWeight", type = StudioPropertyType.STRING)
            }
    )
    public interface FontWeight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "formatterFunction", type = StudioPropertyType.STRING)
            }
    )
    public interface FormatterFunction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "formatter", type = StudioPropertyType.STRING)
            }
    )
    public interface Formatter {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "geoIndex", type = StudioPropertyType.INTEGER)
            }
    )
    public interface GeoIndex {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "gridIndex", type = StudioPropertyType.INTEGER)
            }
    )
    public interface GridIndex {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "handleIcon", type = StudioPropertyType.STRING)
            }
    )
    public interface HandleIcon {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "handleSize", type = StudioPropertyType.STRING)
            }
    )
    public interface HandleSize {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerHeight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.STRING)
            }
    )
    public interface StringHeight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "hideOverlap", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface HideOverlap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "hoverAnimation", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface HoverAnimation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "hoverLink", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface HoverLink {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.STRING)
            }
    )
    public interface Icon {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "inactiveBorderColor", type = StudioPropertyType.OPTIONS,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"})
            }
    )
    public interface InactiveBorderColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "inactiveBorderWidth", type = StudioPropertyType.INTEGER)
            }
    )
    public interface InactiveBorderWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "inactiveColor", type = StudioPropertyType.OPTIONS,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"})
            }
    )
    public interface InactiveColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "inside", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Inside {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "intervalFunction", type = StudioPropertyType.STRING)
            }
    )
    public interface IntervalFunction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "interval", type = StudioPropertyType.INTEGER)
            }
    )
    public interface Interval {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "inverse", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Inverse {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "itemGap", type = StudioPropertyType.INTEGER)
            }
    )
    public interface ItemGap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "itemHeight", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface DoubleItemHeight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "itemHeight", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerItemHeight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "itemWidth", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface DoubleItemWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "itemWidth", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerItemWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "join", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.HasLineStyle.Join",
                            options = {"BEVEL", "ROUND", "MITER"})
            }
    )
    public interface Join {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "keepAspect", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface KeepAspect {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "large", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Large {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "largeThreshold", type = StudioPropertyType.INTEGER)
            }
    )
    public interface LargeThreshold {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "layout", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Orientation",
                            options = {"HORIZONTAL", "VERTICAL"})
            }
    )
    public interface Layout {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "left", type = StudioPropertyType.STRING)
            }
    )
    public interface Left {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "legendHoverLink", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface LegendHoverLink {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "length", type = StudioPropertyType.INTEGER)
            }
    )
    public interface Length {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "lineHeight", type = StudioPropertyType.INTEGER)
            }
    )
    public interface LineHeight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "logBase", type = StudioPropertyType.INTEGER)
            }
    )
    public interface LogBase {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "margin", type = StudioPropertyType.INTEGER)
            }
    )
    public interface Margin {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "maxCount", type = StudioPropertyType.INTEGER)
            }
    )
    public interface MaxCount {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "max", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface DoubleMax {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "maxFunction", type = StudioPropertyType.STRING)
            }
    )
    public interface MaxFunction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "maxInterval", type = StudioPropertyType.INTEGER)
            }
    )
    public interface MaxInterval {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "maxSpan", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface MaxSpan {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "max", type = StudioPropertyType.STRING)
            }
    )
    public interface StringMax {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "maxValueSpan", type = StudioPropertyType.STRING)
            }
    )
    public interface MaxValueSpan {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "min", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface DoubleMin {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "minFunction", type = StudioPropertyType.STRING)
            }
    )
    public interface MinFunction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "minInterval", type = StudioPropertyType.INTEGER)
            }
    )
    public interface MinInterval {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "minSpan", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface MinSpan {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "min", type = StudioPropertyType.STRING)
            }
    )
    public interface StringMin {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "minTurnAngle", type = StudioPropertyType.INTEGER)
            }
    )
    public interface MinTurnAngle {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "minValueSpan", type = StudioPropertyType.STRING)
            }
    )
    public interface MinValueSpan {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "miterLimit", type = StudioPropertyType.INTEGER)
            }
    )
    public interface MiterLimit {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "nameGap", type = StudioPropertyType.INTEGER)
            }
    )
    public interface NameGap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "nameLocation", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AbstractCartesianAxis.NameLocation",
                            options = {"END", "CENTER", "START"})
            }
    )
    public interface NameLocation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "nameRotate", type = StudioPropertyType.INTEGER)
            }
    )
    public interface NameRotate {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.STRING)
            }
    )
    public interface Name {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "nextPageIcon", type = StudioPropertyType.STRING, required = true)
            }
    )
    public interface NextPageIcon {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "nonCategoryBoundaryGap", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface NonCategoryBoundaryGap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "numberCoordinate", type = StudioPropertyType.STRING)
            }
    )
    public interface NumberCoordinate {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "offsetCenter", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface OffsetCenter {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "offset", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerOffset {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "offset", type = StudioPropertyType.STRING)
            }
    )
    public interface StringOffset {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "opacity", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface Opacity {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "opacity", type = StudioPropertyType.STRING)
            }
    )
    public interface StringOpacity {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "orientation", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Orientation",
                            options = {"HORIZONTAL", "VERTICAL"})
            }
    )
    public interface Orientation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "overflow", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Overflow",
                            options = {"NONE", "TRUNCATE", "BREAK", "BREAK_ALL"})
            }
    )
    public interface Overflow {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "padding", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface Padding {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "polarIndex", type = StudioPropertyType.INTEGER)
            }
    )
    public interface PolarIndex {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "positionCoordinates", type = StudioPropertyType.STRING)
            }
    )
    public interface PositionCoordinates {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "position", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.Position.ItemTriggerPosition",
                            options = {"INSIDE", "TOP", "LEFT", "RIGHT", "BOTTOM"})
            }
    )
    public interface TooltipPosition {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "position", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AbstractCartesianAxis.Position",
                            options = {"TOP", "BOTTOM", "RIGHT", "LEFT"})
            }
    )
    public interface AxisPosition {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "precision", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface DoublePrecision {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "precision", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerPrecision {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "prefix", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface Prefix {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "prevPageIcon", type = StudioPropertyType.STRING, required = true)
            }
    )
    public interface PrevPageIcon {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "progressiveChunkMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ProgressiveChunkMode",
                            options = {"SEQUENTIAL", "MOD"})
            }
    )
    public interface ProgressiveChunkMode {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "progressive", type = StudioPropertyType.INTEGER)
            }
    )
    public interface Progressive {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "progressiveThreshold", type = StudioPropertyType.INTEGER)
            }
    )
    public interface ProgressiveThreshold {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "radiusAxisIndexes", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface RadiusAxisIndexes {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "radius", type = StudioPropertyType.STRING)
            }
    )
    public interface Radius {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "rangeMode", type = StudioPropertyType.VALUES_LIST,
                            options = {"VALUE", "PERCENT"})
            }
    )
    public interface RangeMode {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "realtime", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Realtime {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "right", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerRight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "right", type = StudioPropertyType.STRING)
            }
    )
    public interface StringRight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "rotate", type = StudioPropertyType.INTEGER)
            }
    )
    public interface Rotate {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "roundCap", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface RoundCap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "sampling", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.SamplingType",
                            options = {"LARGEST_TRIANGLE_THREE_BUCKET", "AVERAGE", "MAX", "MIN", "SUM"})
            }
    )
    public interface Sampling {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "scale", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface BooleanScale {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "scale", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface DoubleScale {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "selectedMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.SelectedMode",
                            options = {"DISABLED", "SINGLE", "MULTIPLE"})
            }
    )
    public interface SelectedMode {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "selector", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Selector {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "selectorButtonGap", type = StudioPropertyType.INTEGER)
            }
    )
    public interface SelectorButtonGap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "selectorItemGap", type = StudioPropertyType.INTEGER)
            }
    )
    public interface SelectorItemGap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "selectorPosition", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend.Position",
                            options = {"START", "END"})
            }
    )
    public interface SelectorPosition {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "seriesIndex", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface SeriesIndex {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "seriesLayoutBy", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.AbstractAxisAwareSeries.SeriesLayoutType",
                            options = {"COLUMN", "ROW"})
            }
    )
    public interface SeriesLayoutBy {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "shadowBlur", type = StudioPropertyType.INTEGER)
            }
    )
    public interface ShadowBlur {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "shadowColor", type = StudioPropertyType.OPTIONS,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"})
            }
    )
    public interface ShadowColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "shadowOffsetX", type = StudioPropertyType.INTEGER)
            }
    )
    public interface ShadowOffsetX {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "shadowOffsetY", type = StudioPropertyType.INTEGER)
            }
    )
    public interface ShadowOffsetY {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "showAbove", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface ShowAbove {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "show", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Show {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "silent", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Silent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "smooth", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Smooth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "snap", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Snap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "splitNumber", type = StudioPropertyType.INTEGER)
            }
    )
    public interface SplitNumber {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "stackStrategy", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.HasStack.StackStrategy",
                            options = {"SAME_SIGN", "ALL", "POSITIVE", "NEGATIVE"})
            }
    )
    public interface StackStrategy {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "stack", type = StudioPropertyType.STRING)
            }
    )
    public interface Stack {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "startAngle", type = StudioPropertyType.INTEGER)
            }
    )
    public interface StartAngle {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "start", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface Start {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "startValue", type = StudioPropertyType.STRING)
            }
    )
    public interface StartValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "status", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface Status {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "stringCoordinate", type = StudioPropertyType.STRING)
            }
    )
    public interface StringCoordinate {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "symbolKeepAspect", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface SymbolKeepAspect {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "symbolOffset", type = StudioPropertyType.STRING)
            }
    )
    public interface SymbolOffset {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "symbolRotate", type = StudioPropertyType.INTEGER)
            }
    )
    public interface SymbolRotate {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "symbolSizeFunction", type = StudioPropertyType.STRING)
            }
    )
    public interface SymbolSizeFunction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "symbolSize", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerSymbolSize {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "symbolSize", type = StudioPropertyType.STRING)
            }
    )
    public interface StringSymbolSize {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "symbolType", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.HasSymbols.SymbolType",
                            options = {"CIRCLE", "RECTANGLE", "ROUND_RECTANGLE", "PIN", "TRIANGLE", "DIAMOND",
                                    "ARROW", "NONE"})
            }
    )
    public interface SymbolType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "symbol", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface Symbol {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "symbolsSize", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface SymbolsSize {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "textBorderColor", type = StudioPropertyType.OPTIONS,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"})
            }
    )
    public interface TextBorderColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "textBorderDashOffset", type = StudioPropertyType.INTEGER)
            }
    )
    public interface TextBorderDashOffset {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "textBorderType", type = StudioPropertyType.STRING)
            }
    )
    public interface TextBorderType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "textBorderWidth", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface TextBorderWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "textGap", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface TextGap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "textShadowBlur", type = StudioPropertyType.INTEGER)
            }
    )
    public interface TextShadowBlur {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "textShadowColor", type = StudioPropertyType.OPTIONS,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"})
            }
    )
    public interface TextShadowColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "textShadowOffsetX", type = StudioPropertyType.INTEGER)
            }
    )
    public interface TextShadowOffsetX {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "textShadowOffsetY", type = StudioPropertyType.INTEGER)
            }
    )
    public interface TextShadowOffsetY {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface Text {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "throttle", type = StudioPropertyType.INTEGER)
            }
    )
    public interface Throttle {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "top", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerTop {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "top", type = StudioPropertyType.STRING)
            }
    )
    public interface StringTop {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "triggerEmphasis", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface TriggerEmphasis {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "trigger", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.Trigger",
                            options = {"ITEM", "AXIS", "NONE"})
            }
    )
    public interface Trigger {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "triggerEvent", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface TriggerEvent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "triggerOn", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.TriggerOnMode",
                            options = {"MOUSE_MOVE", "CLICK", "MOUSE_MOVE_CLICK", "NONE"})
            }
    )
    public interface TriggerOn {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "triggerTooltip", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface TriggerTooltip {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AbstractAxisPointer.IndicatorType",
                            options = {"LINE", "SHADOW", "NONE"})
            }
    )
    public interface AxisPointerType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AxisType",
                            options = {"CATEGORY", "VALUE", "TIME", "LOG"})
            }
    )
    public interface AxisType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.LineDataType",
                            options = {"MIN", "MAX", "AVERAGE", "MEDIAN"})
            }
    )
    public interface LineDataType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.PointDataType",
                            options = {"MIN", "MAX", "AVERAGE"})
            }
    )
    public interface PintDataType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.STRING)
            }
    )
    public interface Type {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "unselectedSeries", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface UnselectedSeries {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "valueAnimation", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface ValueAnimation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "valueDim", type = StudioPropertyType.INTEGER)
            }
    )
    public interface ValueDim {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface DoubleValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "valueFormatterFunction", type = StudioPropertyType.STRING)
            }
    )
    public interface ValueFormatterFunction {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "valueFormatter", type = StudioPropertyType.STRING)
            }
    )
    public interface ValueFormatter {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "valueIndex", type = StudioPropertyType.INTEGER)
            }
    )
    public interface ValueIndex {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "verticalAlign", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.VerticalAlign",
                            options = {"TOP", "BOTTOM", "MIDDLE"})
            }
    )
    public interface VerticalAlign {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.STRING)
            }
    )
    public interface Width {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "withName", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface WithName {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "withoutName", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface WithoutName {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "xAxisIndex", type = StudioPropertyType.INTEGER)
            }
    )
    public interface XAxisIndex {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "xAxisIndexes", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface XAxisIndexes {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "xAxis", type = StudioPropertyType.STRING)
            }
    )
    public interface XAxis {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "x", type = StudioPropertyType.STRING)
            }
    )
    public interface X {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "yAxisIndex", type = StudioPropertyType.INTEGER)
            }
    )
    public interface YAxisIndex {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "yAxisIndexes", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface YAxisIndexes {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "yAxis", type = StudioPropertyType.STRING)
            }
    )
    public interface YAxis {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "y", type = StudioPropertyType.STRING)
            }
    )
    public interface Y {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "z", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface DoubleZ {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "z", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerZ {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "zLevel", type = StudioPropertyType.DOUBLE)
            }
    )
    public interface DoubleZLevel {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "zLevel", type = StudioPropertyType.INTEGER)
            }
    )
    public interface IntegerZLevel {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "zoomLock", type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface ZoomLock {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "color", type = StudioPropertyType.OPTIONS,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"})
            }
    )
    public interface ChartColor {
    }

    @StudioPropertyGroup
    public interface VisualEffectDefaultProperties extends Symbol, StringSymbolSize, Color, ColorAlpha, StringOpacity,
            ColorLightness, ColorSaturation, ColorHue {
    }

    @StudioPropertyGroup
    public interface PageIconsDefaultProperties extends PrevPageIcon, NextPageIcon {
    }

    @StudioPropertyGroup
    public interface ItemStyleDefaultProperties extends ChartColor, BorderColor, IntegerBorderWidth, BorderType,
            BorderDashOffset, Cap, Join, MiterLimit, ShadowBlur, ShadowColor, ShadowOffsetX, ShadowOffsetY,
            Opacity {
    }

    @StudioPropertyGroup
    public interface LabelLineDefaultProperties extends Show, ShowAbove, Length, Smooth, MinTurnAngle {
    }

    @StudioPropertyGroup
    public interface ScaleEmphasisDefaultProperties extends Disabled, BooleanScale, Focus, BlurScope {
    }

    @StudioPropertyGroup
    public interface EmphasisDefaultProperties extends Disabled, Focus, BlurScope {
    }
}
