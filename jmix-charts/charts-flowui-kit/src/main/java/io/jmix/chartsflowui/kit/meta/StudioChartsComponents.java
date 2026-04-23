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

import io.jmix.flowui.kit.meta.*;

@StudioUiKit(studioClassloaderDependencies = "io.jmix.charts:jmix-charts-flowui-kit")
public interface StudioChartsComponents {

    @StudioComponent(
            name = "Chart",
            classFqn = "io.jmix.chartsflowui.component.Chart",
            category = "Components",
            xmlElement = StudioXmlElements.CHART,
            xmlns = "http://jmix.io/schema/charts/ui",
            xmlnsAlias = "charts",
            icon = "io/jmix/chartsflowui/kit/meta/icon/component/chart.svg",
            propertyGroups = StudioChartsPropertyGroups.ChartComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLOR_PALETTE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
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
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.BACKGROUND_COLOR, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.OPTIONS,
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
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ANIMATION, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ANIMATION_THRESHOLD, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ANIMATION_DURATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ANIMATION_EASING, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ANIMATION_DELAY, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ANIMATION_DURATION_UPDATE, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ANIMATION_EASING_UPDATE, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ANIMATION_DELAY_UPDATE, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.BLEND_MODE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.model.ChartOptions$BlendMode",
                            options = {"SOURCE_OVER", "SOURCE_IN", "SOURCE_OUT", "SOURCE_ATOP", "DESTINATION_OVER",
                                    "DESTINATION_IN", "DESTINATION_OUT", "DESTINATION_ATOP", "LIGHTER", "COPY", "XOR",
                                    "MULTIPLY", "SCREEN", "OVERLAY", "DARKEN", "LIGHTEN", "COLOR_DODGE", "COLOR_BURN",
                                    "HARD_LIGHT", "SOFT_LIGHT", "DIFFERENCE", "EXCLUSION", "HUE", "SATURATION", "COLOR",
                                    "LUMINOSITY"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HOVER_LAYER_THRESHOLD, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RENDERER, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.chartsflowui.kit.component.ChartRenderer",
                            options = {"CANVAS", "SVG"}, defaultValue = "CANVAS"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.USE_UTC, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    StudioChartPreview chart();
}
