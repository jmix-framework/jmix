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
final class StudioChartsPropertyGroups {

    private StudioChartsPropertyGroups() {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALIGN,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Align",
            options = {"LEFT", "RIGHT", "CENTER"}))
    public interface SharedAlign {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALIGN,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend.Align",
            options = {"AUTO", "LEFT", "RIGHT"}))
    public interface LegendAlign {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALIGN,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap.MapAlign",
            options = {"AUTO", "LEFT", "TOP", "BOTTOM", "RIGHT"}))
    public interface MapAlign {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALIGN_TICKS,
            type = StudioPropertyType.BOOLEAN))
    public interface AlignTicks {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANGLE_AXIS_INDEXES,
            type = StudioPropertyType.VALUES_LIST))
    public interface AngleAxisIndexes {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION,
            type = StudioPropertyType.BOOLEAN))
    public interface Animation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_DELAY_FUNCTION,
            type = StudioPropertyType.STRING))
    public interface AnimationDelayFunction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_DELAY,
            type = StudioPropertyType.INTEGER))
    public interface AnimationDelay {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_DELAY_UPDATE_FUNCTION,
            type = StudioPropertyType.STRING))
    public interface AnimationDelayUpdateFunction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_DELAY_UPDATE,
            type = StudioPropertyType.INTEGER))
    public interface AnimationDelayUpdate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_DURATION_FUNCTION,
            type = StudioPropertyType.STRING))
    public interface AnimationDurationFunction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_DURATION,
            type = StudioPropertyType.INTEGER))
    public interface AnimationDuration {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_DURATION_UPDATE_FUNCTION,
            type = StudioPropertyType.STRING))
    public interface AnimationDurationUpdateFunction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_DURATION_UPDATE,
            type = StudioPropertyType.INTEGER))
    public interface AnimationDurationUpdate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_EASING,
            type = StudioPropertyType.STRING))
    public interface AnimationEasing {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_EASING_UPDATE,
            type = StudioPropertyType.STRING))
    public interface AnimationEasingUpdate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ANIMATION_THRESHOLD,
            type = StudioPropertyType.INTEGER))
    public interface AnimationThreshold {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BACKGROUND_COLOR,
            type = StudioPropertyType.OPTIONS,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE", "BLACK",
                    "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE", "CHARTREUSE",
                    "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN", "DARKBLUE", "DARKCYAN",
                    "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN", "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN",
                    "DARKORANGE", "DARKORCHID", "DARKRED", "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE",
                    "DARKSLATEGRAY", "DARKSLATEGREY", "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE",
                    "DIMGRAY", "DIMGREY", "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA",
                    "GAINSBORO", "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW", "HONEYDEW",
                    "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER", "LAVENDERBLUSH", "LAWNGREEN",
                    "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL", "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY",
                    "LIGHTGREY", "LIGHTGREEN", "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE",
                    "LIGHTSLATEGRAY", "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID", "MEDIUMPURPLE",
                    "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN", "MEDIUMTURQUOISE", "MEDIUMVIOLETRED",
                    "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE", "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE",
                    "OLIVEDRAB", "ORANGE", "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE", "PURPLE", "RED",
                    "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN", "SEAGREEN", "SEASHELL", "SIENNA",
                    "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY", "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE",
                    "TAN", "TEAL", "THISTLE", "TOMATO", "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW",
                    "YELLOWGREEN"}))
    public interface BackgroundColor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BAR_MAX_WIDTH,
            type = StudioPropertyType.STRING))
    public interface BarMaxWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BAR_MIN_WIDTH,
            type = StudioPropertyType.STRING))
    public interface BarMinWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BAR_WIDTH,
            type = StudioPropertyType.STRING))
    public interface BarWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BLUR_SCOPE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.BlurScopeType",
            options = {"COORDINATE_SYSTEM", "SERIES", "GLOBAL"}))
    public interface BlurScope {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BORDER_COLOR,
            type = StudioPropertyType.OPTIONS,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE", "BLACK",
                    "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE", "CHARTREUSE",
                    "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN", "DARKBLUE", "DARKCYAN",
                    "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN", "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN",
                    "DARKORANGE", "DARKORCHID", "DARKRED", "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE",
                    "DARKSLATEGRAY", "DARKSLATEGREY", "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE",
                    "DIMGRAY", "DIMGREY", "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA",
                    "GAINSBORO", "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW", "HONEYDEW",
                    "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER", "LAVENDERBLUSH", "LAWNGREEN",
                    "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL", "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY",
                    "LIGHTGREY", "LIGHTGREEN", "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE",
                    "LIGHTSLATEGRAY", "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID", "MEDIUMPURPLE",
                    "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN", "MEDIUMTURQUOISE", "MEDIUMVIOLETRED",
                    "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE", "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE",
                    "OLIVEDRAB", "ORANGE", "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE", "PURPLE", "RED",
                    "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN", "SEAGREEN", "SEASHELL", "SIENNA",
                    "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY", "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE",
                    "TAN", "TEAL", "THISTLE", "TOMATO", "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW",
                    "YELLOWGREEN"}))
    public interface BorderColor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BORDER_DASH_OFFSET,
            type = StudioPropertyType.INTEGER))
    public interface BorderDashOffset {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BORDER_RADIUS,
            type = StudioPropertyType.INTEGER))
    public interface BorderRadius {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BORDER_TYPE,
            type = StudioPropertyType.STRING))
    public interface BorderType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BORDER_WIDTH,
            type = StudioPropertyType.DOUBLE))
    public interface DoubleBorderWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BORDER_WIDTH,
            type = StudioPropertyType.INTEGER))
    public interface IntegerBorderWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BOTTOM,
            type = StudioPropertyType.STRING))
    public interface Bottom {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CALENDAR_INDEX,
            type = StudioPropertyType.INTEGER))
    public interface CalendarIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CAP,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.HasLineStyle.Cap",
            options = {"BUTT", "ROUND", "SQUARE"}))
    public interface Cap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CATEGORY_BOUNDARY_GAP,
            type = StudioPropertyType.BOOLEAN))
    public interface CategoryBoundaryGap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CENTER,
            type = StudioPropertyType.STRING))
    public interface Center {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLIP,
            type = StudioPropertyType.BOOLEAN))
    public interface Clip {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLOCKWISE,
            type = StudioPropertyType.BOOLEAN))
    public interface Clockwise {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLOR_ALPHA,
            type = StudioPropertyType.VALUES_LIST))
    public interface ColorAlpha {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLOR_BY,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ColorBy",
            options = {"SERIES", "DATA"}))
    public interface ColorBy {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLOR_HUE,
            type = StudioPropertyType.VALUES_LIST))
    public interface ColorHue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLOR_LIGHTNESS,
            type = StudioPropertyType.VALUES_LIST))
    public interface ColorLightness {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLOR_SATURATION,
            type = StudioPropertyType.VALUES_LIST))
    public interface ColorSaturation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLOR,
            type = StudioPropertyType.VALUES_LIST,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE", "BLACK",
                    "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE", "CHARTREUSE",
                    "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN", "DARKBLUE", "DARKCYAN",
                    "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN", "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN",
                    "DARKORANGE", "DARKORCHID", "DARKRED", "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE",
                    "DARKSLATEGRAY", "DARKSLATEGREY", "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE",
                    "DIMGRAY", "DIMGREY", "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA",
                    "GAINSBORO", "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW", "HONEYDEW",
                    "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER", "LAVENDERBLUSH", "LAWNGREEN",
                    "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL", "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY",
                    "LIGHTGREY", "LIGHTGREEN", "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE",
                    "LIGHTSLATEGRAY", "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID", "MEDIUMPURPLE",
                    "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN", "MEDIUMTURQUOISE", "MEDIUMVIOLETRED",
                    "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE", "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE",
                    "OLIVEDRAB", "ORANGE", "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE", "PURPLE", "RED",
                    "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN", "SEAGREEN", "SEASHELL", "SIENNA",
                    "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY", "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE",
                    "TAN", "TEAL", "THISTLE", "TOMATO", "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW",
                    "YELLOWGREEN"}))
    public interface Color {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COORDINATE_SYSTEM,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.CoordinateSystem",
            options = {"CARTESIAN_2_D", "POLAR"}))
    public interface CoordinateSystem {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CURSOR,
            type = StudioPropertyType.STRING))
    public interface Cursor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DASH_OFFSET,
            type = StudioPropertyType.INTEGER))
    public interface DashOffset {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DATA_GROUP_ID,
            type = StudioPropertyType.STRING))
    public interface DataGroupId {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DATASET_INDEX,
            type = StudioPropertyType.INTEGER))
    public interface DatasetIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DIMENSION,
            type = StudioPropertyType.STRING))
    public interface Dimension {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DISABLED,
            type = StudioPropertyType.BOOLEAN))
    public interface Disabled {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DISTANCE,
            type = StudioPropertyType.INTEGER))
    public interface Distance {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ELLIPSIS,
            type = StudioPropertyType.STRING))
    public interface Ellipsis {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.END,
            type = StudioPropertyType.DOUBLE))
    public interface End {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.END_VALUE,
            type = StudioPropertyType.STRING))
    public interface EndValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.EXTRA_CSS_TEXT,
            type = StudioPropertyType.STRING))
    public interface ExtraCssText {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FILTER_MODE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom.FilterMode",
            options = {"FILTER", "WEAK_FILTER", "EMPTY", "NONE"}))
    public interface FilterMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FOCUS,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.FocusType",
            options = {"NONE", "SELF", "SERIES"}))
    public interface Focus {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FONT_FAMILY,
            type = StudioPropertyType.STRING))
    public interface FontFamily {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FONT_SIZE,
            type = StudioPropertyType.INTEGER))
    public interface FontSize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FONT_STYLE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.FontStyle",
            options = {"NORMAL", "ITALIC", "OBLIQUE"}))
    public interface FontStyle {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FONT_WEIGHT,
            type = StudioPropertyType.STRING))
    public interface FontWeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FORMATTER_FUNCTION,
            type = StudioPropertyType.STRING))
    public interface FormatterFunction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FORMATTER,
            type = StudioPropertyType.STRING))
    public interface Formatter {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.GEO_INDEX,
            type = StudioPropertyType.INTEGER))
    public interface GeoIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.GRID_INDEX,
            type = StudioPropertyType.INTEGER))
    public interface GridIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HANDLE_ICON,
            type = StudioPropertyType.STRING))
    public interface HandleIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HANDLE_SIZE,
            type = StudioPropertyType.STRING))
    public interface HandleSize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HEIGHT,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.SIZE))
    public interface IntegerHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HEIGHT,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.SIZE))
    public interface StringHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HIDE_OVERLAP,
            type = StudioPropertyType.BOOLEAN))
    public interface HideOverlap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HOVER_ANIMATION,
            type = StudioPropertyType.BOOLEAN))
    public interface HoverAnimation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.HOVER_LINK,
            type = StudioPropertyType.BOOLEAN))
    public interface HoverLink {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INACTIVE_BORDER_COLOR,
            type = StudioPropertyType.OPTIONS,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE", "BLACK",
                    "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE", "CHARTREUSE",
                    "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN", "DARKBLUE", "DARKCYAN",
                    "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN", "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN",
                    "DARKORANGE", "DARKORCHID", "DARKRED", "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE",
                    "DARKSLATEGRAY", "DARKSLATEGREY", "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE",
                    "DIMGRAY", "DIMGREY", "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA",
                    "GAINSBORO", "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW", "HONEYDEW",
                    "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER", "LAVENDERBLUSH", "LAWNGREEN",
                    "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL", "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY",
                    "LIGHTGREY", "LIGHTGREEN", "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE",
                    "LIGHTSLATEGRAY", "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID", "MEDIUMPURPLE",
                    "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN", "MEDIUMTURQUOISE", "MEDIUMVIOLETRED",
                    "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE", "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE",
                    "OLIVEDRAB", "ORANGE", "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE", "PURPLE", "RED",
                    "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN", "SEAGREEN", "SEASHELL", "SIENNA",
                    "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY", "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE",
                    "TAN", "TEAL", "THISTLE", "TOMATO", "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW",
                    "YELLOWGREEN"}))
    public interface InactiveBorderColor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INACTIVE_BORDER_WIDTH,
            type = StudioPropertyType.INTEGER))
    public interface InactiveBorderWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INACTIVE_COLOR,
            type = StudioPropertyType.OPTIONS,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE", "BLACK",
                    "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE", "CHARTREUSE",
                    "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN", "DARKBLUE", "DARKCYAN",
                    "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN", "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN",
                    "DARKORANGE", "DARKORCHID", "DARKRED", "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE",
                    "DARKSLATEGRAY", "DARKSLATEGREY", "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE",
                    "DIMGRAY", "DIMGREY", "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA",
                    "GAINSBORO", "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW", "HONEYDEW",
                    "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER", "LAVENDERBLUSH", "LAWNGREEN",
                    "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL", "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY",
                    "LIGHTGREY", "LIGHTGREEN", "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE",
                    "LIGHTSLATEGRAY", "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID", "MEDIUMPURPLE",
                    "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN", "MEDIUMTURQUOISE", "MEDIUMVIOLETRED",
                    "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE", "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE",
                    "OLIVEDRAB", "ORANGE", "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE", "PURPLE", "RED",
                    "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN", "SEAGREEN", "SEASHELL", "SIENNA",
                    "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY", "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE",
                    "TAN", "TEAL", "THISTLE", "TOMATO", "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW",
                    "YELLOWGREEN"}))
    public interface InactiveColor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INSIDE,
            type = StudioPropertyType.BOOLEAN))
    public interface Inside {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INTERVAL_FUNCTION,
            type = StudioPropertyType.STRING))
    public interface IntervalFunction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INTERVAL,
            type = StudioPropertyType.INTEGER))
    public interface Interval {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INVERSE,
            type = StudioPropertyType.BOOLEAN))
    public interface Inverse {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ITEM_GAP,
            type = StudioPropertyType.INTEGER))
    public interface ItemGap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ITEM_HEIGHT,
            type = StudioPropertyType.DOUBLE))
    public interface DoubleItemHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ITEM_HEIGHT,
            type = StudioPropertyType.INTEGER))
    public interface IntegerItemHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ITEM_WIDTH,
            type = StudioPropertyType.DOUBLE))
    public interface DoubleItemWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ITEM_WIDTH,
            type = StudioPropertyType.INTEGER))
    public interface IntegerItemWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.JOIN,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.HasLineStyle.Join",
            options = {"BEVEL", "ROUND", "MITER"}))
    public interface Join {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.KEEP_ASPECT,
            type = StudioPropertyType.BOOLEAN))
    public interface KeepAspect {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LARGE,
            type = StudioPropertyType.BOOLEAN))
    public interface Large {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LARGE_THRESHOLD,
            type = StudioPropertyType.INTEGER))
    public interface LargeThreshold {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LAYOUT,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Orientation",
            options = {"HORIZONTAL", "VERTICAL"}))
    public interface Layout {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LEFT,
            type = StudioPropertyType.STRING))
    public interface Left {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LEGEND_HOVER_LINK,
            type = StudioPropertyType.BOOLEAN))
    public interface LegendHoverLink {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LENGTH,
            type = StudioPropertyType.INTEGER))
    public interface Length {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LINE_HEIGHT,
            type = StudioPropertyType.INTEGER))
    public interface LineHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LOG_BASE,
            type = StudioPropertyType.INTEGER))
    public interface LogBase {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MARGIN,
            type = StudioPropertyType.INTEGER))
    public interface Margin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_COUNT,
            type = StudioPropertyType.INTEGER))
    public interface MaxCount {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX,
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.GENERAL))
    public interface DoubleMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_FUNCTION,
            type = StudioPropertyType.STRING))
    public interface MaxFunction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_INTERVAL,
            type = StudioPropertyType.INTEGER))
    public interface MaxInterval {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_SPAN,
            type = StudioPropertyType.DOUBLE))
    public interface MaxSpan {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX,
            type = StudioPropertyType.STRING))
    public interface StringMax {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MAX_VALUE_SPAN,
            type = StudioPropertyType.STRING))
    public interface MaxValueSpan {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN,
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.GENERAL))
    public interface DoubleMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_FUNCTION,
            type = StudioPropertyType.STRING))
    public interface MinFunction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_INTERVAL,
            type = StudioPropertyType.INTEGER))
    public interface MinInterval {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_SPAN,
            type = StudioPropertyType.DOUBLE))
    public interface MinSpan {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN,
            type = StudioPropertyType.STRING))
    public interface StringMin {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_TURN_ANGLE,
            type = StudioPropertyType.INTEGER))
    public interface MinTurnAngle {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MIN_VALUE_SPAN,
            type = StudioPropertyType.STRING))
    public interface MinValueSpan {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MITER_LIMIT,
            type = StudioPropertyType.INTEGER))
    public interface MiterLimit {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME_GAP,
            type = StudioPropertyType.INTEGER))
    public interface NameGap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME_LOCATION,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AbstractCartesianAxis.NameLocation",
            options = {"END", "CENTER", "START"}))
    public interface NameLocation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME_ROTATE,
            type = StudioPropertyType.INTEGER))
    public interface NameRotate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface Name {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NEXT_PAGE_ICON,
            type = StudioPropertyType.STRING,
            required = true))
    public interface NextPageIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NON_CATEGORY_BOUNDARY_GAP,
            type = StudioPropertyType.VALUES_LIST))
    public interface NonCategoryBoundaryGap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NUMBER_COORDINATE,
            type = StudioPropertyType.STRING))
    public interface NumberCoordinate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OFFSET_CENTER,
            type = StudioPropertyType.VALUES_LIST))
    public interface OffsetCenter {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OFFSET,
            type = StudioPropertyType.INTEGER))
    public interface IntegerOffset {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OFFSET,
            type = StudioPropertyType.STRING))
    public interface StringOffset {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OPACITY,
            type = StudioPropertyType.DOUBLE))
    public interface Opacity {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ORIENTATION,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Orientation",
            options = {"HORIZONTAL", "VERTICAL"}))
    public interface Orientation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OVERFLOW,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Overflow",
            options = {"NONE", "TRUNCATE", "BREAK", "BREAK_ALL"}))
    public interface Overflow {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PADDING,
            type = StudioPropertyType.VALUES_LIST))
    public interface Padding {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.POLAR_INDEX,
            type = StudioPropertyType.INTEGER))
    public interface PolarIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.POSITION_COORDINATES,
            type = StudioPropertyType.STRING))
    public interface PositionCoordinates {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.POSITION,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.Position.ItemTriggerPosition",
            options = {"INSIDE", "TOP", "LEFT", "RIGHT", "BOTTOM"}))
    public interface TooltipPosition {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.POSITION,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AbstractCartesianAxis.Position",
            options = {"TOP", "BOTTOM", "RIGHT", "LEFT"}))
    public interface AxisPosition {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PRECISION,
            type = StudioPropertyType.DOUBLE))
    public interface DoublePrecision {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PRECISION,
            type = StudioPropertyType.INTEGER))
    public interface IntegerPrecision {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PREFIX,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface Prefix {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PREV_PAGE_ICON,
            type = StudioPropertyType.STRING,
            required = true))
    public interface PrevPageIcon {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PROGRESSIVE_CHUNK_MODE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.ProgressiveChunkMode",
            options = {"SEQUENTIAL", "MOD"}))
    public interface ProgressiveChunkMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PROGRESSIVE,
            type = StudioPropertyType.INTEGER))
    public interface Progressive {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.PROGRESSIVE_THRESHOLD,
            type = StudioPropertyType.INTEGER))
    public interface ProgressiveThreshold {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RADIUS_AXIS_INDEXES,
            type = StudioPropertyType.VALUES_LIST))
    public interface RadiusAxisIndexes {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RADIUS,
            type = StudioPropertyType.STRING))
    public interface Radius {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RADIUS,
            type = StudioPropertyType.VALUES_LIST))
    public interface RadiusWithValuesListType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RANGE_MODE,
            type = StudioPropertyType.VALUES_LIST,
            options = {"VALUE", "PERCENT"}))
    public interface RangeMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.REALTIME,
            type = StudioPropertyType.BOOLEAN))
    public interface Realtime {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RIGHT,
            type = StudioPropertyType.INTEGER))
    public interface IntegerRight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RIGHT,
            type = StudioPropertyType.STRING))
    public interface StringRight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ROTATE,
            type = StudioPropertyType.INTEGER))
    public interface Rotate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ROUND_CAP,
            type = StudioPropertyType.BOOLEAN))
    public interface RoundCap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SAMPLING,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.SamplingType",
            options = {"LARGEST_TRIANGLE_THREE_BUCKET", "AVERAGE", "MAX", "MIN", "SUM"}))
    public interface Sampling {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SCALE,
            type = StudioPropertyType.BOOLEAN))
    public interface BooleanScale {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SCALE,
            type = StudioPropertyType.DOUBLE))
    public interface DoubleScale {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SELECTED_MODE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.SelectedMode",
            options = {"DISABLED", "SINGLE", "MULTIPLE"}))
    public interface SelectedMode {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SELECTOR,
            type = StudioPropertyType.BOOLEAN))
    public interface Selector {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SELECTOR_BUTTON_GAP,
            type = StudioPropertyType.INTEGER))
    public interface SelectorButtonGap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SELECTOR_ITEM_GAP,
            type = StudioPropertyType.INTEGER))
    public interface SelectorItemGap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SELECTOR_POSITION,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend.Position",
            options = {"START", "END"}))
    public interface SelectorPosition {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SERIES_INDEX,
            type = StudioPropertyType.VALUES_LIST))
    public interface SeriesIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SERIES_LAYOUT_BY,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.AbstractAxisAwareSeries.SeriesLayoutType",
            options = {"COLUMN", "ROW"}))
    public interface SeriesLayoutBy {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHADOW_BLUR,
            type = StudioPropertyType.INTEGER))
    public interface ShadowBlur {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHADOW_COLOR,
            type = StudioPropertyType.OPTIONS,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE", "BLACK",
                    "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE", "CHARTREUSE",
                    "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN", "DARKBLUE", "DARKCYAN",
                    "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN", "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN",
                    "DARKORANGE", "DARKORCHID", "DARKRED", "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE",
                    "DARKSLATEGRAY", "DARKSLATEGREY", "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE",
                    "DIMGRAY", "DIMGREY", "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA",
                    "GAINSBORO", "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW", "HONEYDEW",
                    "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER", "LAVENDERBLUSH", "LAWNGREEN",
                    "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL", "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY",
                    "LIGHTGREY", "LIGHTGREEN", "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE",
                    "LIGHTSLATEGRAY", "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID", "MEDIUMPURPLE",
                    "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN", "MEDIUMTURQUOISE", "MEDIUMVIOLETRED",
                    "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE", "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE",
                    "OLIVEDRAB", "ORANGE", "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE", "PURPLE", "RED",
                    "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN", "SEAGREEN", "SEASHELL", "SIENNA",
                    "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY", "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE",
                    "TAN", "TEAL", "THISTLE", "TOMATO", "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW",
                    "YELLOWGREEN"}))
    public interface ShadowColor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHADOW_OFFSET_X,
            type = StudioPropertyType.INTEGER))
    public interface ShadowOffsetX {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHADOW_OFFSET_Y,
            type = StudioPropertyType.INTEGER))
    public interface ShadowOffsetY {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHOW_ABOVE,
            type = StudioPropertyType.BOOLEAN))
    public interface ShowAbove {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHOW,
            type = StudioPropertyType.BOOLEAN))
    public interface Show {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHOW,
            type = StudioPropertyType.BOOLEAN,
            category = StudioProperty.Category.GENERAL))
    public interface ShowWithGeneralCategory {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SILENT,
            type = StudioPropertyType.BOOLEAN))
    public interface Silent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SMOOTH,
            type = StudioPropertyType.BOOLEAN))
    public interface Smooth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SNAP,
            type = StudioPropertyType.BOOLEAN))
    public interface Snap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SPLIT_NUMBER,
            type = StudioPropertyType.INTEGER))
    public interface SplitNumber {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.STACK_STRATEGY,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.HasStack.StackStrategy",
            options = {"SAME_SIGN", "ALL", "POSITIVE", "NEGATIVE"}))
    public interface StackStrategy {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.STACK,
            type = StudioPropertyType.STRING))
    public interface Stack {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.START_ANGLE,
            type = StudioPropertyType.INTEGER))
    public interface StartAngle {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.START,
            type = StudioPropertyType.DOUBLE))
    public interface Start {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.START_VALUE,
            type = StudioPropertyType.STRING))
    public interface StartValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.STATUS,
            type = StudioPropertyType.BOOLEAN))
    public interface Status {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.STRING_COORDINATE,
            type = StudioPropertyType.STRING))
    public interface StringCoordinate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SYMBOL_KEEP_ASPECT,
            type = StudioPropertyType.BOOLEAN))
    public interface SymbolKeepAspect {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SYMBOL_OFFSET,
            type = StudioPropertyType.STRING))
    public interface SymbolOffset {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SYMBOL_ROTATE,
            type = StudioPropertyType.INTEGER))
    public interface SymbolRotate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SYMBOL_SIZE_FUNCTION,
            type = StudioPropertyType.STRING))
    public interface SymbolSizeFunction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SYMBOL_SIZE,
            type = StudioPropertyType.INTEGER))
    public interface IntegerSymbolSize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SYMBOL_TYPE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.HasSymbols.SymbolType",
            options = {"CIRCLE", "RECTANGLE", "ROUND_RECTANGLE", "PIN", "TRIANGLE", "DIAMOND", "ARROW", "NONE"}))
    public interface SymbolType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SYMBOL,
            type = StudioPropertyType.VALUES_LIST))
    public interface Symbol {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SYMBOLS_SIZE,
            type = StudioPropertyType.VALUES_LIST))
    public interface SymbolsSize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_BORDER_COLOR,
            type = StudioPropertyType.OPTIONS,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE", "BLACK",
                    "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE", "CHARTREUSE",
                    "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN", "DARKBLUE", "DARKCYAN",
                    "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN", "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN",
                    "DARKORANGE", "DARKORCHID", "DARKRED", "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE",
                    "DARKSLATEGRAY", "DARKSLATEGREY", "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE",
                    "DIMGRAY", "DIMGREY", "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA",
                    "GAINSBORO", "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW", "HONEYDEW",
                    "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER", "LAVENDERBLUSH", "LAWNGREEN",
                    "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL", "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY",
                    "LIGHTGREY", "LIGHTGREEN", "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE",
                    "LIGHTSLATEGRAY", "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID", "MEDIUMPURPLE",
                    "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN", "MEDIUMTURQUOISE", "MEDIUMVIOLETRED",
                    "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE", "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE",
                    "OLIVEDRAB", "ORANGE", "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE", "PURPLE", "RED",
                    "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN", "SEAGREEN", "SEASHELL", "SIENNA",
                    "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY", "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE",
                    "TAN", "TEAL", "THISTLE", "TOMATO", "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW",
                    "YELLOWGREEN"}))
    public interface TextBorderColor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_BORDER_DASH_OFFSET,
            type = StudioPropertyType.INTEGER))
    public interface TextBorderDashOffset {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_BORDER_TYPE,
            type = StudioPropertyType.STRING))
    public interface TextBorderType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_BORDER_WIDTH,
            type = StudioPropertyType.DOUBLE))
    public interface TextBorderWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_GAP,
            type = StudioPropertyType.DOUBLE))
    public interface TextGap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_SHADOW_BLUR,
            type = StudioPropertyType.INTEGER))
    public interface TextShadowBlur {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_SHADOW_COLOR,
            type = StudioPropertyType.OPTIONS,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE", "BLACK",
                    "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE", "CHARTREUSE",
                    "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN", "DARKBLUE", "DARKCYAN",
                    "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN", "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN",
                    "DARKORANGE", "DARKORCHID", "DARKRED", "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE",
                    "DARKSLATEGRAY", "DARKSLATEGREY", "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE",
                    "DIMGRAY", "DIMGREY", "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA",
                    "GAINSBORO", "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW", "HONEYDEW",
                    "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER", "LAVENDERBLUSH", "LAWNGREEN",
                    "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL", "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY",
                    "LIGHTGREY", "LIGHTGREEN", "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE",
                    "LIGHTSLATEGRAY", "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID", "MEDIUMPURPLE",
                    "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN", "MEDIUMTURQUOISE", "MEDIUMVIOLETRED",
                    "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE", "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE",
                    "OLIVEDRAB", "ORANGE", "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE", "PURPLE", "RED",
                    "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN", "SEAGREEN", "SEASHELL", "SIENNA",
                    "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY", "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE",
                    "TAN", "TEAL", "THISTLE", "TOMATO", "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW",
                    "YELLOWGREEN"}))
    public interface TextShadowColor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_SHADOW_OFFSET_X,
            type = StudioPropertyType.INTEGER))
    public interface TextShadowOffsetX {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_SHADOW_OFFSET_Y,
            type = StudioPropertyType.INTEGER))
    public interface TextShadowOffsetY {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT,
            type = StudioPropertyType.VALUES_LIST))
    public interface Text {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.THROTTLE,
            type = StudioPropertyType.INTEGER))
    public interface Throttle {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TOP,
            type = StudioPropertyType.INTEGER))
    public interface IntegerTop {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TOP,
            type = StudioPropertyType.STRING))
    public interface StringTop {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TRIGGER_EMPHASIS,
            type = StudioPropertyType.BOOLEAN))
    public interface TriggerEmphasis {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TRIGGER,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.Trigger",
            options = {"ITEM", "AXIS", "NONE"}))
    public interface Trigger {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TRIGGER_EVENT,
            type = StudioPropertyType.BOOLEAN))
    public interface TriggerEvent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TRIGGER_ON,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.TriggerOnMode",
            options = {"MOUSE_MOVE", "CLICK", "MOUSE_MOVE_CLICK", "NONE"}))
    public interface TriggerOn {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TRIGGER_TOOLTIP,
            type = StudioPropertyType.BOOLEAN))
    public interface TriggerTooltip {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AbstractAxisPointer.IndicatorType",
            options = {"LINE", "SHADOW", "NONE"}))
    public interface AxisPointerType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.AxisType",
            options = {"CATEGORY", "VALUE", "TIME", "LOG"}))
    public interface AxisType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.LineDataType",
            options = {"MIN", "MAX", "AVERAGE", "MEDIAN"}))
    public interface LineDataType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPE,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.series.mark.PointDataType",
            options = {"MIN", "MAX", "AVERAGE"}))
    public interface PintDataType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPE,
            type = StudioPropertyType.STRING))
    public interface Type {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.UNSELECTED_SERIES,
            type = StudioPropertyType.VALUES_LIST))
    public interface UnselectedSeries {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE_ANIMATION,
            type = StudioPropertyType.BOOLEAN))
    public interface ValueAnimation {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE_DIM,
            type = StudioPropertyType.INTEGER))
    public interface ValueDim {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.DOUBLE,
            category = StudioProperty.Category.GENERAL))
    public interface DoubleValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE_FORMATTER_FUNCTION,
            type = StudioPropertyType.STRING))
    public interface ValueFormatterFunction {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE_FORMATTER,
            type = StudioPropertyType.STRING))
    public interface ValueFormatter {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE_INDEX,
            type = StudioPropertyType.INTEGER))
    public interface ValueIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.INTEGER,
            category = StudioProperty.Category.GENERAL))
    public interface IntegerValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VERTICAL_ALIGN,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.VerticalAlign",
            options = {"TOP", "BOTTOM", "MIDDLE"}))
    public interface VerticalAlign {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WIDTH,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.SIZE))
    public interface Width {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WITH_NAME,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface WithName {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WITHOUT_NAME,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface WithoutName {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.X_AXIS_INDEX,
            type = StudioPropertyType.INTEGER))
    public interface XAxisIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.X_AXIS_INDEXES,
            type = StudioPropertyType.VALUES_LIST))
    public interface XAxisIndexes {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.X_AXIS,
            type = StudioPropertyType.STRING))
    public interface XAxis {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.X,
            type = StudioPropertyType.STRING))
    public interface X {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.Y_AXIS_INDEX,
            type = StudioPropertyType.INTEGER))
    public interface YAxisIndex {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.Y_AXIS_INDEXES,
            type = StudioPropertyType.VALUES_LIST))
    public interface YAxisIndexes {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.Y_AXIS,
            type = StudioPropertyType.STRING))
    public interface YAxis {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.Y,
            type = StudioPropertyType.STRING))
    public interface Y {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.Z,
            type = StudioPropertyType.DOUBLE))
    public interface DoubleZ {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.Z,
            type = StudioPropertyType.INTEGER))
    public interface IntegerZ {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.Z_LEVEL,
            type = StudioPropertyType.DOUBLE))
    public interface DoubleZLevel {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.Z_LEVEL,
            type = StudioPropertyType.INTEGER))
    public interface IntegerZLevel {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ZOOM_LOCK,
            type = StudioPropertyType.BOOLEAN))
    public interface ZoomLock {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLOR,
            type = StudioPropertyType.OPTIONS,
            category = StudioProperty.Category.LOOK_AND_FEEL,
            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE", "BLACK",
                    "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE", "CHARTREUSE",
                    "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN", "DARKBLUE", "DARKCYAN",
                    "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN", "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN",
                    "DARKORANGE", "DARKORCHID", "DARKRED", "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE",
                    "DARKSLATEGRAY", "DARKSLATEGREY", "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE",
                    "DIMGRAY", "DIMGREY", "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA",
                    "GAINSBORO", "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW", "HONEYDEW",
                    "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER", "LAVENDERBLUSH", "LAWNGREEN",
                    "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL", "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY",
                    "LIGHTGREY", "LIGHTGREEN", "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE",
                    "LIGHTSLATEGRAY", "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID", "MEDIUMPURPLE",
                    "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN", "MEDIUMTURQUOISE", "MEDIUMVIOLETRED",
                    "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE", "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE",
                    "OLIVEDRAB", "ORANGE", "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE", "PURPLE", "RED",
                    "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN", "SEAGREEN", "SEASHELL", "SIENNA",
                    "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY", "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE",
                    "TAN", "TEAL", "THISTLE", "TOMATO", "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW",
                    "YELLOWGREEN"}))
    public interface ChartColor {
    }

    @StudioPropertyGroup
    public interface AnimationDefaultProperties extends Animation, AnimationThreshold, AnimationDuration,
            AnimationEasing, AnimationDelay, AnimationDurationUpdate, AnimationEasingUpdate,
            AnimationDelayUpdate {
    }

    @StudioPropertyGroup
    public interface AnimationFunctionDefaultProperties extends AnimationDelayFunction,
            AnimationDurationFunction, AnimationDelayUpdateFunction, AnimationDurationUpdateFunction {
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

    @StudioPropertyGroup
    public interface TextStyleDefaultProperties extends ChartColor, FontStyle, FontWeight, FontFamily, FontSize,
            LineHeight, StudioPropertyGroups.WidthWithIntegerType, IntegerHeight, TextBorderColor,
            TextBorderWidth, TextBorderType, TextBorderDashOffset, TextShadowColor, TextShadowBlur,
            TextShadowOffsetX, TextShadowOffsetY, Overflow, Ellipsis {
    }

    @StudioPropertyGroup
    public interface FormatterDefaultProperties extends Formatter, FormatterFunction {
    }

    @StudioPropertyGroup
    public interface ShadowDefaultProperties extends ShadowColor, ShadowBlur, ShadowOffsetX, ShadowOffsetY {
    }

    @StudioPropertyGroup
    public interface BoxStyleDefaultPropertiesWithIntegerBorderWidth extends BackgroundColor, BorderColor,
            IntegerBorderWidth, BorderType, BorderDashOffset, BorderRadius, Padding, ShadowDefaultProperties {
    }

    @StudioPropertyGroup
    public interface BoxStyleDefaultPropertiesWithDoubleBorderWidth extends BackgroundColor, BorderColor,
            DoubleBorderWidth, BorderType, BorderDashOffset, BorderRadius, Padding, ShadowDefaultProperties {
    }

    @StudioPropertyGroup
    public interface LabelBoxStyleDefaultProperties extends BackgroundColor, BorderColor, IntegerBorderWidth, Padding,
            ShadowDefaultProperties {
    }

    @StudioPropertyGroup
    public interface TextStyleWithBoxDefaultProperties extends TextStyleDefaultProperties,
            BoxStyleDefaultPropertiesWithIntegerBorderWidth {
    }

    @StudioPropertyGroup
    public interface GaugeTextDefaultProperties extends Show, OffsetCenter, ValueAnimation,
            TextStyleWithBoxDefaultProperties {
    }

    @StudioPropertyGroup
    public interface AxisPointerLabelDefaultProperties extends Show, IntegerPrecision, FormatterDefaultProperties,
            Margin, TextStyleDefaultProperties, LabelBoxStyleDefaultProperties {
    }

    @StudioPropertyGroup
    public interface SymbolDefaultProperties extends StudioPropertyGroups.Symbol, IntegerSymbolSize,
            SymbolSizeFunction, SymbolRotate, SymbolKeepAspect, SymbolOffset, SymbolType {
    }

    @StudioPropertyGroup
    public interface TooltipStyleDefaultProperties extends TooltipPosition, PositionCoordinates,
            FormatterDefaultProperties, ValueFormatter, BackgroundColor, BorderColor, IntegerBorderWidth, Padding,
            ExtraCssText {
    }

    @StudioPropertyGroup
    public interface InnerTooltipDefaultProperties extends Show, Trigger, TooltipStyleDefaultProperties,
            ValueFormatterFunction {
    }

    @StudioPropertyGroup
    public interface AxisPointerDefaultProperties extends Show, AxisPointerType, Snap, IntegerZ, TriggerEmphasis,
            TriggerTooltip, IntegerValue, Status {
    }

    @StudioPropertyGroup
    public interface DoubleScaleEmphasisDefaultProperties extends Disabled, DoubleScale, Focus, BlurScope {
    }

    @StudioPropertyGroup
    public interface AngleAxisDefaultProperties extends StartAngle, Clockwise, PolarIndex, MinFunction, MaxFunction,
            AnimationFunctionDefaultProperties, StudioPropertyGroups.Id, AxisType, CategoryBoundaryGap,
            NonCategoryBoundaryGap, StringMin, StringMax, BooleanScale, SplitNumber, MinInterval, MaxInterval,
            Interval, LogBase, Silent, TriggerEvent, AnimationDefaultProperties, IntegerZLevel, IntegerZ {
    }

    @StudioPropertyGroup
    public interface ScatterSeriesDefaultProperties extends CoordinateSystem, XAxisIndex, YAxisIndex, PolarIndex,
            GeoIndex, CalendarIndex, Clip, Cursor, Large, LargeThreshold, Progressive, ProgressiveThreshold,
            AnimationDefaultProperties, SymbolDefaultProperties, LegendHoverLink, SeriesLayoutBy, DatasetIndex,
            StudioPropertyGroups.Id, Name, ColorBy, SelectedMode, DataGroupId, IntegerZLevel, IntegerZ, Silent {
    }

    @StudioPropertyGroup
    public interface CandlestickSeriesDefaultProperties extends CoordinateSystem, XAxisIndex, YAxisIndex,
            HoverAnimation, Layout, BarWidth, BarMaxWidth, BarMinWidth, Large, LargeThreshold, Progressive,
            ProgressiveThreshold, ProgressiveChunkMode, Clip, LegendHoverLink, SeriesLayoutBy, DatasetIndex,
            StudioPropertyGroups.Id, Name, ColorBy, SelectedMode, DataGroupId, IntegerZLevel, IntegerZ, Silent,
            AnimationDuration, AnimationEasing, AnimationDelay {
    }

    @StudioPropertyGroup
    public interface MarkPointPointDefaultProperties extends Name, PintDataType, ValueIndex, ValueDim,
            StringCoordinate, NumberCoordinate, X, Y, SymbolDefaultProperties {
    }

    @StudioPropertyGroup
    public interface PolarDefaultProperties extends StudioPropertyGroups.Id, IntegerZLevel, IntegerZ, Center, Radius {
    }

    @StudioPropertyGroup
    public interface PieceDefaultProperties extends DoubleMin, DoubleMax, StudioPropertyGroups.Label, DoubleValue,
            ChartColor {
    }

    @StudioPropertyGroup
    public interface IndicatorDefaultProperties extends StudioPropertyGroups.LocalizedName,
            StudioPropertyGroups.IntegerMax, StudioPropertyGroups.IntegerMin, ChartColor {
    }

    @StudioPropertyGroup
    public interface GaugeAxisTickDefaultProperties extends Show, SplitNumber, Length, Distance {
    }

    @StudioPropertyGroup
    public interface DataZoomFeatureDefaultProperties extends Show, FilterMode, XAxisIndexes, YAxisIndexes {
    }

    @StudioPropertyGroup
    public interface LineStyleDefaultProperties extends AreaStyleDefaultProperties,
            StudioPropertyGroups.WidthWithIntegerType, Type, DashOffset, Cap, Join, MiterLimit {
    }

    @StudioPropertyGroup
    public interface AreaStyleDefaultProperties extends ChartColor, ShadowColor, ShadowBlur, ShadowOffsetX,
            ShadowOffsetY, Opacity {
    }

    @StudioPropertyGroup
    public interface LinePointDefaultProperties extends Name, LineDataType, ValueIndex, ValueDim,
            StringCoordinate, NumberCoordinate, X, Y, XAxis, YAxis, DoubleValue, StudioPropertyGroups.Symbol,
            IntegerSymbolSize, SymbolSizeFunction, SymbolRotate, SymbolKeepAspect, SymbolOffset, SymbolType {
    }

    @StudioPropertyGroup
    public interface PointDefaultProperties extends Name, PintDataType, ValueIndex, ValueDim, StringCoordinate,
            NumberCoordinate, X, Y, DoubleValue {
    }

    @StudioPropertyGroup
    public interface RoundedItemStyleDefaultProperties extends AreaStyleDefaultProperties, BorderColor,
            IntegerBorderWidth, BorderType, BorderRadius {
    }

    @StudioPropertyGroup
    public interface SeriesAriaLabelDefaultProperties extends Prefix, WithName, WithoutName {
    }

    @StudioPropertyGroup
    public interface SplitDefaultProperties extends Show, Interval, IntervalFunction {
    }

    @StudioPropertyGroup
    public interface CartesianAxisDefaultProperties extends StudioPropertyGroups.Id, Show, GridIndex, AlignTicks,
            AxisPosition, IntegerOffset, Name, NameLocation, NameGap, NameRotate, Inverse, AxisType,
            CategoryBoundaryGap, NonCategoryBoundaryGap, StringMin, StringMax, BooleanScale, SplitNumber,
            MinInterval, MaxInterval, Interval, LogBase, Silent, TriggerEvent, AnimationDefaultProperties,
            AnimationFunctionDefaultProperties, MinFunction, MaxFunction, IntegerZLevel, IntegerZ {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COLOR_PALETTE,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BLEND_MODE,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.ChartOptions$BlendMode",
                            options = {"SOURCE_OVER", "SOURCE_IN", "SOURCE_OUT", "SOURCE_ATOP", "DESTINATION_OVER",
                                    "DESTINATION_IN", "DESTINATION_OUT", "DESTINATION_ATOP", "LIGHTER", "COPY", "XOR",
                                    "MULTIPLY", "SCREEN", "OVERLAY", "DARKEN", "LIGHTEN", "COLOR_DODGE", "COLOR_BURN",
                                    "HARD_LIGHT", "SOFT_LIGHT", "DIFFERENCE", "EXCLUSION", "HUE", "SATURATION", "COLOR",
                                    "LUMINOSITY"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HOVER_LAYER_THRESHOLD,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RENDERER,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            classFqn = "io.jmix.chartsflowui.kit.component.ChartRenderer",
                            defaultValue = "CANVAS",
                            options = {"CANVAS", "SVG"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.USE_UTC,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL)
            }
    )
    public interface ChartComponent extends StudioPropertyGroups.AddonComponentDefaultProperties,
            AnimationDefaultProperties, BackgroundColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DURATION,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EASING,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface StateAnimationComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LINK,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TARGET,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Title$Target",
                            options = {"SELF", "BLANK"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SUBTEXT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SUBLINK,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SUBTARGET,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Title$Target",
                            options = {"SELF", "BLANK"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT_ALIGN,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Title$TextAlign",
                            options = {"AUTO", "LEFT", "RIGHT", "CENTER"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT_VERTICAL_ALIGN,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Title$TextVerticalAlign",
                            options = {"AUTO", "TOP", "BOTTOM", "MIDDLE"})
            }
    )
    public interface TitleComponent extends Show, Left, Bottom, Padding, ItemGap, IntegerZ, StringTop,
            ShadowBlur, StringRight, ShadowColor, BorderColor, TriggerEvent, BorderRadius, ShadowOffsetY, ShadowOffsetX,
            IntegerZLevel, BackgroundColor, IntegerBorderWidth, StudioPropertyGroups.Id, StudioPropertyGroups.Text {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_BORDER_WIDTH,
            type = StudioPropertyType.INTEGER))
    public interface RichStyleComponent extends Padding, FontSize, FontStyle, ShadowBlur, LineHeight, FontWeight,
            FontFamily, ChartColor, BorderType, SharedAlign, ShadowColor, BorderColor, BorderRadius, VerticalAlign,
            ShadowOffsetY, ShadowOffsetX, IntegerHeight, TextShadowBlur, TextBorderType, TextShadowColor,
            TextBorderColor, BackgroundColor, BorderDashOffset, TextShadowOffsetY, TextShadowOffsetX,
            IntegerBorderWidth, TextBorderDashOffset, StudioPropertyGroups.RequiredStringName,
            StudioPropertyGroups.WidthWithIntegerType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TOOLBOXES,
                            type = StudioPropertyType.VALUES_LIST,
                            options = {"RECT", "POLYGON", "LINE_X", "LINE_Y", "KEEP", "CLEAR"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BRUSH_LINK_INDEXES,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BRUSH_LINK_MODE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Brush.BrushSelectMode",
                            options = {"ALL", "NONE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SERIES_INDEX,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Brush.SeriesIndex",
                            options = {"ALL", "ARRAY", "NUMBER"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.GEO_INDEXES,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.GEO_INDEX_MODE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Brush.BrushSelectMode",
                            options = {"ALL", "NONE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.X_AXIS_INDEX_MODE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Brush.BrushSelectMode",
                            options = {"ALL", "NONE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.Y_AXIS_INDEX_MODE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Brush.BrushSelectMode",
                            options = {"ALL", "NONE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BRUSH_TYPE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Brush.BrushType",
                            options = {"RECT", "POLYGON", "LINE_X", "LINE_Y"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BRUSH_MODE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Brush.BrushMode",
                            options = {"SINGLE", "MULTIPLE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TRANSFORMABLE,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THROTTLE_TYPE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Brush.ThrottleType",
                            options = {"DEBOUNCE", "FIX_RATE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THROTTLE_DELAY,
                            type = StudioPropertyType.DOUBLE),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.REMOVE_ON_CLICK,
                            type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface BrushComponent extends IntegerZ, GeoIndex, YAxisIndex, XAxisIndex, YAxisIndexes,
            XAxisIndexes, StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SYMBOL_SIZE,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.OPACITY,
                            type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface InBrushComponent extends Color, Symbol, ColorHue, ColorAlpha, ColorLightness,
            ColorSaturation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SYMBOL_SIZE,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.OPACITY,
                            type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface OutOfBrushComponent extends Color, Symbol, ColorHue, ColorAlpha, ColorLightness,
            ColorSaturation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SYMBOL_SIZE,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.OPACITY,
                            type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface InRangeComponent extends Color, Symbol, ColorHue, ColorAlpha, ColorLightness,
            ColorSaturation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SYMBOL_SIZE,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.OPACITY,
                            type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface OutRangeComponent extends Color, Symbol, ColorHue, ColorAlpha, ColorLightness,
            ColorSaturation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_CONTENT,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ALWAYS_SHOW_CONTENT,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_DELAY,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ENTERABLE,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RENDER_MODE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Tooltip.RenderMode",
                            options = {"HTML", "RICH_TEXT"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CONFINE,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.APPEND_TO_BODY,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CLASS_NAME,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TRANSITION_DURATION,
                            type = StudioPropertyType.DOUBLE),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ORDER,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.Tooltip.OrderType",
                            options = {"SERIES_ASC", "SERIES_DESC", "VALUE_ASC", "VALUE_DESC"})
            }
    )
    public interface TooltipComponent extends Show, Trigger, Padding, TriggerOn, Formatter, BorderColor,
            ExtraCssText, ValueFormatter, TooltipPosition, BackgroundColor, FormatterFunction, IntegerBorderWidth,
            PositionCoordinates, ValueFormatterFunction, StudioPropertyGroups.HideDelay {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LEFT,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BOTTOM,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CONTAIN_LABEL,
                            type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface GridItemComponent extends Show, IntegerZ, ShadowBlur, IntegerTop, ShadowColor, BorderColor,
            IntegerRight, ShadowOffsetY, ShadowOffsetX, IntegerZLevel, IntegerHeight, BackgroundColor,
            IntegerBorderWidth, StudioPropertyGroups.Id, StudioPropertyGroups.WidthWithIntegerType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LEFT,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BOTTOM,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SCROLL_DATA_INDEX,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PAGE_BUTTON_ITEM_GAP,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PAGE_BUTTON_GAP,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PAGE_BUTTON_POSITION,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend.Position",
                            options = {"START", "END"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PAGE_FORMATTER,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PAGE_FORMATTER_FUNCTION,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PAGE_ICON_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PAGE_ICON_INACTIVE_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PAGE_ICON_SIZE,
                            type = StudioPropertyType.INTEGER)
            }
    )
    public interface ScrollableLegendComponent extends Show, StudioPropertyGroups.IconString, Padding, ItemGap,
            Selector, IntegerZ, Formatter, Animation, ShadowBlur, IntegerTop, ShadowColor, Orientation, LegendAlign,
            BorderColor, SymbolRotate, SelectedMode, IntegerRight, BorderRadius, ShadowOffsetY, ShadowOffsetX,
            IntegerZLevel, IntegerHeight, InactiveColor, SelectorItemGap, BackgroundColor, UnselectedSeries,
            SelectorPosition, IntegerItemWidth, SelectorButtonGap, IntegerItemHeight, FormatterFunction,
            IntegerBorderWidth, InactiveBorderWidth, InactiveBorderColor, AnimationDurationUpdate,
            StudioPropertyGroups.Id, StudioPropertyGroups.WidthWithIntegerType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LEFT,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BOTTOM,
                            type = StudioPropertyType.INTEGER)
            }
    )
    public interface LegendComponent extends Show, StudioPropertyGroups.IconString, Padding, ItemGap, Selector,
            IntegerZ, Formatter, ShadowBlur, IntegerTop, ShadowColor, Orientation, LegendAlign, BorderColor,
            SymbolRotate, SelectedMode, IntegerRight, BorderRadius, ShadowOffsetY, ShadowOffsetX, IntegerZLevel,
            IntegerHeight, InactiveColor, SelectorItemGap, BackgroundColor, UnselectedSeries, SelectorPosition,
            IntegerItemWidth, SelectorButtonGap, IntegerItemHeight, FormatterFunction, IntegerBorderWidth,
            InactiveBorderWidth, InactiveBorderColor, StudioPropertyGroups.Id,
            StudioPropertyGroups.WidthWithIntegerType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OFFSET,
            type = StudioPropertyType.VALUES_LIST))
    public interface SelectorLabelComponent extends TextStyleDefaultProperties, Show, Rotate, Padding, Distance,
            ShadowBlur, BorderType, SharedAlign, ShadowColor, BorderColor, BorderRadius, VerticalAlign, ShadowOffsetY,
            ShadowOffsetX, BackgroundColor, BorderDashOffset, IntegerBorderWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TYPE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer.IndicatorType",
                            options = {"LINE", "SHADOW", "NONE", "CROSS"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AXIS,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip.AxisPointer.AxisType",
                            options = {"X", "Y", "RADIUS", "ANGLE"})
            }
    )
    public interface TooltipAxisPointerComponent extends AnimationDefaultProperties,
            AnimationFunctionDefaultProperties, Snap, IntegerZ {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ID,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface GlobalAxisPointerComponent extends Snap, Show, Status, IntegerZ, TriggerOn, IntegerValue,
            TriggerTooltip, TriggerEmphasis, AxisPointerType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SIZE,
            type = StudioPropertyType.STRING))
    public interface AxisPointerHandleComponent extends Show, StudioPropertyGroups.IconString, Margin, Throttle,
            ShadowBlur, ChartColor, ShadowColor, ShadowOffsetY, ShadowOffsetX {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.Z,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CENTER,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHAPE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.Radar.Shape",
                            options = {"POLYGON", "CIRCLE"})
            }
    )
    public interface RadarComponent extends Silent, NameGap, StartAngle, SplitNumber, TriggerEvent, BooleanScale,
            IntegerZLevel, StudioPropertyGroups.Id, RadiusWithValuesListType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.COLORS,
            type = StudioPropertyType.VALUES_LIST,
            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE", "BLACK",
                    "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE", "CHARTREUSE",
                    "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN", "DARKBLUE", "DARKCYAN",
                    "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN", "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN",
                    "DARKORANGE", "DARKORCHID", "DARKRED", "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE",
                    "DARKSLATEGRAY", "DARKSLATEGREY", "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE",
                    "DIMGRAY", "DIMGREY", "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA",
                    "GAINSBORO", "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW", "HONEYDEW",
                    "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER", "LAVENDERBLUSH", "LAWNGREEN",
                    "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL", "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY",
                    "LIGHTGREY", "LIGHTGREEN", "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE",
                    "LIGHTSLATEGRAY", "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID", "MEDIUMPURPLE",
                    "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN", "MEDIUMTURQUOISE", "MEDIUMVIOLETRED",
                    "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE", "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE",
                    "OLIVEDRAB", "ORANGE", "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE", "PURPLE", "RED",
                    "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN", "SEAGREEN", "SEASHELL", "SIENNA",
                    "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY", "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE",
                    "TAN", "TEAL", "THISTLE", "TOMATO", "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW",
                    "YELLOWGREEN"}))
    public interface AreaStyleComponent extends Opacity, ShadowBlur, ShadowColor, ShadowOffsetY, ShadowOffsetX {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ON_ZERO,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ON_ZERO_AXIS_INDEX,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SYMBOLS,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SYMBOLS_OFFSET,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface AxisLineComponent extends Show, SymbolsSize {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_MIN_LABEL,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_MAX_LABEL,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COLOR_FUNCTION,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface AxisLabelComponent extends TextStyleDefaultProperties, SplitDefaultProperties, Rotate,
            Margin, Inside, Padding, Formatter, ShadowBlur, BorderType, SharedAlign, ShadowColor, HideOverlap,
            BorderColor, BorderRadius, VerticalAlign, ShadowOffsetY, ShadowOffsetX, BackgroundColor, BorderDashOffset,
            FormatterFunction, IntegerBorderWidth {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALIGN_WITH_LABEL,
            type = StudioPropertyType.BOOLEAN))
    public interface AxisTickComponent extends SplitDefaultProperties, Length, Inside {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME_LOCATION,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.chartsflowui.kit.component.model.axis.HasAxisName.NameLocation",
            options = {"END", "CENTER", "START"}))
    public interface RadiusAxisComponent extends AnimationDefaultProperties, AnimationFunctionDefaultProperties,
            Name, Silent, NameGap, LogBase, Inverse, Interval, IntegerZ, AxisType, StringMin, StringMax, PolarIndex,
            NameRotate, SplitNumber, MinInterval, MinFunction, MaxInterval, MaxFunction, TriggerEvent, BooleanScale,
            IntegerZLevel, CategoryBoundaryGap, NonCategoryBoundaryGap, StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ZOOM_ON_MOUSE_WHEEL,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MOVE_ON_MOUSE_MOVE,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MOVE_ON_MOUSE_WHEEL,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PREVENT_DEFAULT_MOUSE_MOVE,
                            type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface InsideDataZoomComponent extends End, Start, MinSpan, MaxSpan, ZoomLock, Throttle, EndValue,
            Disabled, RangeMode, StartValue, FilterMode, Orientation, YAxisIndexes, XAxisIndexes, MinValueSpan,
            MaxValueSpan, AngleAxisIndexes, RadiusAxisIndexes, StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FILLER_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MOVE_HANDLE_ICON,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MOVE_HANDLE_SIZE,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LABEL_PRECISION,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LABEL_FORMATTER,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LABEL_FORMATTER_FUNCTION,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_DETAIL,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_DATA_SHADOW,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BRUSH_SELECT,
                            type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface SliderDataZoomComponent extends End, Show, Left, Width, Start, Bottom, MinSpan, MaxSpan,
            ZoomLock, Throttle, Realtime, IntegerZ, EndValue, StringTop, RangeMode, StartValue, HandleSize, HandleIcon,
            FilterMode, StringRight, Orientation, BorderColor, YAxisIndexes, XAxisIndexes, StringHeight, MinValueSpan,
            MaxValueSpan, BorderRadius, IntegerZLevel, BackgroundColor, AngleAxisIndexes, RadiusAxisIndexes,
            StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CALCULABLE,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RANGE,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INDICATOR_ICON,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INDICATOR_SIZE,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface ContinuousVisualMapComponent extends Text, Show, Left, Bottom, TextGap, Padding, Inverse,
            DoubleZ, Realtime, MapAlign, StringTop, HoverLink, Formatter, DoubleMin, DoubleMax, Dimension, HandleSize,
            HandleIcon, StringRight, SeriesIndex, Orientation, BorderColor, DoubleZLevel, DoublePrecision,
            DoubleItemWidth, BackgroundColor, DoubleItemHeight, FormatterFunction, DoubleBorderWidth,
            StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CATEGORIES,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MIN_OPEN,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX_OPEN,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_LABEL,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEM_SYMBOL,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.HasSymbols.SymbolType",
                            options = {"CIRCLE", "RECTANGLE", "ROUND_RECTANGLE", "PIN", "TRIANGLE", "DIAMOND", "ARROW",
                                    "NONE"})
            }
    )
    public interface PiecewiseVisualMapComponent extends Text, Show, Left, Bottom, TextGap, Padding, ItemGap,
            Inverse, DoubleZ, MapAlign, StringTop, HoverLink, Formatter, DoubleMin, DoubleMax, Dimension, StringRight,
            SplitNumber, SeriesIndex, Orientation, BorderColor, SelectedMode, DoubleZLevel, DoublePrecision,
            DoubleItemWidth, BackgroundColor, DoubleItemHeight, FormatterFunction, DoubleBorderWidth,
            StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_SYMBOL,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_ALL_SYMBOL,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.STEP,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.Step",
                            options = {"START", "MIDDLE", "END"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CONNECT_NULLS,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TRIGGER_LINE_EVENT,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SMOOTH,
                            type = StudioPropertyType.DOUBLE),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SMOOTH_MONOTONE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.SmoothMonotoneType",
                            options = {"X", "Y"})
            }
    )
    public interface LineSeriesComponent extends AnimationDefaultProperties, Clip, Stack, Silent, Cursor,
            ColorBy, Sampling, IntegerZ, YAxisIndex, XAxisIndex, SymbolType, PolarIndex, DataGroupId, SymbolRotate,
            SymbolOffset, SelectedMode, DatasetIndex, StackStrategy, IntegerZLevel, SeriesLayoutBy, LegendHoverLink,
            SymbolKeepAspect, CoordinateSystem, IntegerSymbolSize, SymbolSizeFunction, StudioPropertyGroups.Id,
            StudioPropertyGroups.Name, StudioPropertyGroups.Symbol {
    }

    @StudioPropertyGroup
    public interface EndLabelComponent extends TextStyleDefaultProperties, Show, Rotate, Padding, Distance,
            Formatter, ShadowBlur, BorderType, SharedAlign, ShadowColor, BorderColor, StringOffset, BorderRadius,
            VerticalAlign, ShadowOffsetY, ShadowOffsetX, ValueAnimation, BackgroundColor, BorderDashOffset,
            FormatterFunction, IntegerBorderWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ORIGIN_TYPE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.LineSeries.AreaStyle.Origin.OriginType",
                            options = {"AUTO", "START", "END"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ORIGIN_VALUE,
                            type = StudioPropertyType.DOUBLE)
            }
    )
    public interface LineSeriesAreaStyleComponent extends AreaStyleDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.REALTIME_SORT,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_BACKGROUND,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BAR_MIN_HEIGHT,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BAR_MIN_ANGLE,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BAR_GAP,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BAR_CATEGORY_GAP,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface BarSeriesComponent extends AnimationDefaultProperties, Name, Clip, Stack, Large, Silent,
            Cursor, ColorBy, Sampling, RoundCap, IntegerZ, BarWidth, YAxisIndex, XAxisIndex, PolarIndex, Progressive,
            DataGroupId, BarMinWidth, BarMaxWidth, SelectedMode, DatasetIndex, StackStrategy, IntegerZLevel,
            SeriesLayoutBy, LargeThreshold, LegendHoverLink, CoordinateSystem, ProgressiveThreshold,
            ProgressiveChunkMode, StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MOVE_OVERLAP,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.AbstractSeries.LabelLayout.MoveOverlapPosition",
                            options = {"SHIFT_X", "SHIFT_Y"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DX,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DY,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DRAGGABLE,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LABEL_LINE_POINTS,
                            type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface LabelLayoutComponent extends Y, X, Rotate, FontSize, SharedAlign, HideOverlap,
            VerticalAlign, IntegerHeight, StudioPropertyGroups.WidthWithIntegerType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SYMBOL_ICONS,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SYMBOL_TYPES,
                            type = StudioPropertyType.VALUES_LIST,
                            options = {"CIRCLE", "RECTANGLE", "ROUND_RECTANGLE", "PIN", "TRIANGLE", "DIAMOND", "ARROW",
                                    "NONE"})
            }
    )
    public interface MarkLineComponent extends AnimationDefaultProperties, Silent, IntegerPrecision,
            IntegerSymbolSize {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OFFSET,
            type = StudioPropertyType.VALUES_LIST))
    public interface GaugeSeriesLabelComponent extends TextStyleDefaultProperties, Show, Rotate, Padding,
            Distance, Formatter, ShadowBlur, BorderType, SharedAlign, ShadowColor, BorderColor, BorderRadius,
            VerticalAlign, ShadowOffsetY, ShadowOffsetX, TooltipPosition, BackgroundColor, BorderDashOffset,
            FormatterFunction, IntegerBorderWidth, PositionCoordinates {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OFFSET,
            type = StudioPropertyType.VALUES_LIST))
    public interface SeriesLabelComponent extends TextStyleDefaultProperties, Show, Rotate, Padding, Distance,
            Formatter, ShadowBlur, BorderType, SharedAlign, ShadowColor, BorderColor, BorderRadius, VerticalAlign,
            ShadowOffsetY, ShadowOffsetX, TooltipPosition, BackgroundColor, BorderDashOffset, FormatterFunction,
            IntegerBorderWidth, PositionCoordinates {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.X,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.Y,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ANGLE,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VALUE,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TOOLTIP,
                            type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface EncodeComponent extends RadiusWithValuesListType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SYMBOL,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.HasSymbols.SymbolType",
                            options = {"CIRCLE", "RECTANGLE", "ROUND_RECTANGLE", "TRIANGLE", "DIAMOND", "PIN", "ARROW",
                                    "NONE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SYMBOL_SIZE,
                            type = StudioPropertyType.DOUBLE),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DASH_GAP_X,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DASH_GAP_Y,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DASH_ARRAY_X,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DASH_ARRAY_Y,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX_TILE_WIDTH,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX_TILE_HEIGHT,
                            type = StudioPropertyType.INTEGER)
            }
    )
    public interface DecalComponent extends ChartColor, BackgroundColor, SymbolKeepAspect,
            StudioPropertyGroups.Rotation {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECTED_OFFSET,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MIN_ANGLE,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MIN_SHOW_LABEL_ANGLE,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ROSE_TYPE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.PieSeries.RoseType",
                            options = {"RADIUS", "AREA"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AVOID_LABEL_OVERLAP,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.STILL_SHOW_ZERO_SUM,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PERCENT_PRECISION,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_EMPTY_CIRCLE,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ANIMATION_TYPE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.PieSeries.AnimationType",
                            options = {"EXPANSION", "SCALE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ANIMATION_TYPE_UPDATE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.PieSeries.AnimationUpdateType",
                            options = {"TRANSITION", "EXPANSION"})
            }
    )
    public interface PieSeriesComponent extends AnimationDefaultProperties, Name, Left, Width, Silent, Radius,
            Cursor, Center, Bottom, ColorBy, IntegerZ, GeoIndex, StringTop, Clockwise, StartAngle, StringRight,
            DataGroupId, StringHeight, SelectedMode, DatasetIndex, IntegerZLevel, CalendarIndex, SeriesLayoutBy,
            LegendHoverLink, StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LENGTH2,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX_SURFACE_ANGLE,
                            type = StudioPropertyType.INTEGER)
            }
    )
    public interface PieSeriesLabelLineComponent extends LabelLineDefaultProperties {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SCALE_SIZE,
            type = StudioPropertyType.INTEGER))
    public interface PieSeriesEmphasisComponent extends ScaleEmphasisDefaultProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_EFFECT_ON,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.EffectOn",
                            options = {"RENDER", "EMPHASIS"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EFFECT_TYPE,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface EffectScatterSeriesComponent extends AnimationDefaultProperties, Name, Clip, Silent, Cursor,
            ColorBy, IntegerZ, GeoIndex, YAxisIndex, XAxisIndex, SymbolType, PolarIndex, DataGroupId, SymbolRotate,
            SymbolOffset, SelectedMode, DatasetIndex, IntegerZLevel, CalendarIndex, SeriesLayoutBy, LegendHoverLink,
            SymbolKeepAspect, CoordinateSystem, IntegerSymbolSize, SymbolSizeFunction, StudioPropertyGroups.Id,
            StudioPropertyGroups.Symbol {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.NUMBER,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PERIOD,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BRUSH_TYPE,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries.RippleEffect.BrushType",
                            options = {"STROKE", "FILL"})
            }
    )
    public interface RippleEffectComponent extends ChartColor, DoubleScale {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RADAR_INDEX,
            type = StudioPropertyType.INTEGER))
    public interface RadarSeriesComponent extends AnimationDefaultProperties, Name, Silent, ColorBy, IntegerZ,
            SymbolType, DataGroupId, SymbolRotate, SymbolOffset, SelectedMode, IntegerZLevel, SymbolKeepAspect,
            IntegerSymbolSize, SymbolSizeFunction, StudioPropertyGroups.Id, StudioPropertyGroups.Symbol {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BOX_WIDTH,
            type = StudioPropertyType.VALUES_LIST))
    public interface BoxplotSeriesComponent extends Name, Silent, Layout, ColorBy, IntegerZ, YAxisIndex,
            XAxisIndex, DataGroupId, SelectedMode, DatasetIndex, IntegerZLevel, SeriesLayoutBy, HoverAnimation,
            AnimationDelay, LegendHoverLink, AnimationEasing, CoordinateSystem, AnimationDuration,
            StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BULLISH_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BEARISH_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BULLISH_BORDER_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BEARISH_BORDER_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DOJI_BORDER_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
    public interface CandlestickSeriesItemStyleComponent extends Opacity, ShadowBlur, ShadowColor, ShadowOffsetY,
            ShadowOffsetX, DoubleBorderWidth {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MIN_SIZE,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX_SIZE,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SORT,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.series.FunnelSeries.SortType",
                            options = {"ASCENDING", "DESCENDING", "NONE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SORT_FUNCTION,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.GAP,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FUNNEL_ALIGN,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Align",
                            options = {"LEFT", "RIGHT", "CENTER"})
            }
    )
    public interface FunnelSeriesComponent extends AnimationDefaultProperties, Name, Left, Width, Silent, Bottom,
            ColorBy, IntegerZ, StringTop, StringRight, Orientation, DataGroupId, StringHeight, SelectedMode,
            DatasetIndex, IntegerZLevel, SeriesLayoutBy, LegendHoverLink, StudioPropertyGroups.Id,
            StudioPropertyGroups.IntegerMin, StudioPropertyGroups.IntegerMax {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CENTER,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.END_ANGLE,
                            type = StudioPropertyType.INTEGER)
            }
    )
    public interface GaugeSeriesComponent extends AnimationDefaultProperties, Name, Silent, ColorBy, IntegerZ,
            Clockwise, StartAngle, SplitNumber, DataGroupId, SelectedMode, IntegerZLevel, LegendHoverLink,
            StudioPropertyGroups.Id, StudioPropertyGroups.IntegerMin, StudioPropertyGroups.IntegerMax,
            RadiusWithValuesListType {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RANGE,
                            type = StudioPropertyType.DOUBLE,
                            required = true),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            required = true,
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
    public interface GaugeAxisLineLineStyleColorItemComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OVERLAP,
            type = StudioPropertyType.BOOLEAN))
    public interface GaugeProgressComponent extends Show, Clip, RoundCap,
            StudioPropertyGroups.WidthWithIntegerType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LENGTH,
            type = StudioPropertyType.STRING))
    public interface GaugePointerComponent extends Show, StudioPropertyGroups.IconString, ShowAbove, KeepAspect,
            OffsetCenter, StudioPropertyGroups.WidthWithIntegerType {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SIZE,
            type = StudioPropertyType.INTEGER))
    public interface GaugeAnchorComponent extends Show, StudioPropertyGroups.IconString, ShowAbove, KeepAspect,
            OffsetCenter {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.WITH_TITLE,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.WITHOUT_TITLE,
                            type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface GeneralComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ALL_DATA,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PARTIAL_DATA,
                            type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface AriaLabelDataComponent extends WithName, MaxCount, WithoutName {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MIDDLE,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.END,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface SeparatorComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ITEM_SIZE,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_TITLE,
                            type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface ToolboxComponent extends Show, Left, Width, Bottom, ItemGap, IntegerZ, StringTop,
            StringRight, Orientation, StringHeight, IntegerZLevel, StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT_POSITION,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.Emphasis.IconStyle.TextPosition",
                            options = {"LEFT", "RIGHT", "TOP", "BOTTOM"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT_FILL,
                            type = StudioPropertyType.OPTIONS,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT_ALIGN,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.shared.Align",
                            options = {"LEFT", "RIGHT", "CENTER"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT_BACKGROUND_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT_BORDER_RADIUS,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PADDING,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface ToolboxEmphasisIconStyleComponent extends ItemStyleDefaultProperties {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPES,
            type = StudioPropertyType.VALUES_LIST,
            options = {"RECT", "POLYGON", "LINE_X", "LINE_Y", "KEEP", "CLEAR"}))
    public interface BrushFeatureComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RECT,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.POLYGON,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LINE_X,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LINE_Y,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.KEEP,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CLEAR,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface BrushFeatureIconComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RECT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.POLYGON,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LINE_X,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LINE_Y,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.KEEP,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CLEAR,
                            type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface BrushFeatureTitleComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TYPES,
            type = StudioPropertyType.VALUES_LIST,
            options = {"LINE", "BAR", "STACK"}))
    public interface MagicTypeFeatureComponent extends Show {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LINE,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BAR,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface MagicTypeFeatureIconComponent extends Stack {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LINE,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BAR,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.STACK,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TILED,
                            type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface MagicTypeFeatureTitleComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ZOOM,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BACK,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface DataZoomFeatureIconComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ZOOM,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BACK,
                            type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface DataZoomFeatureTitleComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TYPE,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.GENERAL,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.toolbox.SaveAsImageFeature.SaveType",
                            options = {"PNG", "JPG", "SVG"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CONNECTED_BACKGROUND_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
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
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EXCLUDE_COMPONENTS,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PIXEL_RATIO,
                            type = StudioPropertyType.INTEGER)
            }
    )
    public interface SaveAsImageFeatureComponent extends ShowWithGeneralCategory, Name,
            StudioPropertyGroups.IconString, BackgroundColor, StudioPropertyGroups.Title {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATA_CONTAINER,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
                            category = StudioProperty.Category.DATA_BINDING,
                            required = true,
                            typeParameter = "T"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CATEGORY_FIELD,
                            type = StudioPropertyType.PROPERTY_REF,
                            category = StudioProperty.Category.DATA_BINDING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VALUE_FIELDS,
                            type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface DataSourceComponent {
    }

}
