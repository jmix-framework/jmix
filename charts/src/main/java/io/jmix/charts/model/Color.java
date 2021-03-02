/*
 * Copyright 2021 Haulmont.
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

package io.jmix.charts.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

@SuppressWarnings("unused")
public final class Color implements Serializable {

    private static final long serialVersionUID = -8044478193482089996L;

    private final static Map<String, Color> namedColors = new HashMap<>();

    public final static Color ALICEBLUE = new Color("ALICEBLUE", "#F0F8FF");
    public final static Color ANTIQUEWHITE = new Color("ANTIQUEWHITE", "#FAEBD7");
    public final static Color AQUA = new Color("AQUA", "#00FFFF");
    public final static Color AQUAMARINE = new Color("AQUAMARINE", "#7FFFD4");
    public final static Color AZURE = new Color("AZURE", "#F0FFFF");
    public final static Color BEIGE = new Color("BEIGE", "#F5F5DC");
    public final static Color BISQUE = new Color("BISQUE", "#FFE4C4");
    public final static Color BLACK = new Color("BLACK", "#000000");
    public final static Color BLANCHEDALMOND = new Color("BLANCHEDALMOND", "#FFEBCD");
    public final static Color BLUE = new Color("BLUE", "#0000FF");
    public final static Color BLUEVIOLET = new Color("BLUEVIOLET", "#8A2BE2");
    public final static Color BROWN = new Color("BROWN", "#A52A2A");
    public final static Color BURLYWOOD = new Color("BURLYWOOD", "#DEB887");
    public final static Color CADETBLUE = new Color("CADETBLUE", "#5F9EA0");
    public final static Color CHARTREUSE = new Color("CHARTREUSE", "#7FFF00");
    public final static Color CHOCOLATE = new Color("CHOCOLATE", "#D2691E");
    public final static Color CORAL = new Color("CORAL", "#FF7F50");
    public final static Color CORNFLOWERBLUE = new Color("CORNFLOWERBLUE", "#6495ED");
    public final static Color CORNSILK = new Color("CORNSILK", "#FFF8DC");
    public final static Color CRIMSON = new Color("CRIMSON", "#DC143C");
    public final static Color CYAN = new Color("CYAN", "#00FFFF");
    public final static Color DARKBLUE = new Color("DARKBLUE", "#00008B");
    public final static Color DARKCYAN = new Color("DARKCYAN", "#008B8B");
    public final static Color DARKGOLDENROD = new Color("DARKGOLDENROD", "#B8860B");
    public final static Color DARKGRAY = new Color("DARKGRAY", "#A9A9A9");
    public final static Color DARKGREY = new Color("DARKGREY", "#A9A9A9");
    public final static Color DARKGREEN = new Color("DARKGREEN", "#006400");
    public final static Color DARKKHAKI = new Color("DARKKHAKI", "#BDB76B");
    public final static Color DARKMAGENTA = new Color("DARKMAGENTA", "#8B008B");
    public final static Color DARKOLIVEGREEN = new Color("DARKOLIVEGREEN", "#556B2F");
    public final static Color DARKORANGE = new Color("DARKORANGE", "#FF8C00");
    public final static Color DARKORCHID = new Color("DARKORCHID", "#9932CC");
    public final static Color DARKRED = new Color("DARKRED", "#8B0000");
    public final static Color DARKSALMON = new Color("DARKSALMON", "#E9967A");
    public final static Color DARKSEAGREEN = new Color("DARKSEAGREEN", "#8FBC8F");
    public final static Color DARKSLATEBLUE = new Color("DARKSLATEBLUE", "#483D8B");
    public final static Color DARKSLATEGRAY = new Color("DARKSLATEGRAY", "#2F4F4F");
    public final static Color DARKSLATEGREY = new Color("DARKSLATEGREY", "#2F4F4F");
    public final static Color DARKTURQUOISE = new Color("DARKTURQUOISE", "#00CED1");
    public final static Color DARKVIOLET = new Color("DARKVIOLET", "#9400D3");
    public final static Color DEEPPINK = new Color("DEEPPINK", "#FF1493");
    public final static Color DEEPSKYBLUE = new Color("DEEPSKYBLUE", "#00BFFF");
    public final static Color DIMGRAY = new Color("DIMGRAY", "#696969");
    public final static Color DIMGREY = new Color("DIMGREY", "#696969");
    public final static Color DODGERBLUE = new Color("DODGERBLUE", "#1E90FF");
    public final static Color FIREBRICK = new Color("FIREBRICK", "#B22222");
    public final static Color FLORALWHITE = new Color("FLORALWHITE", "#FFFAF0");
    public final static Color FORESTGREEN = new Color("FORESTGREEN", "#228B22");
    public final static Color FUCHSIA = new Color("FUCHSIA", "#FF00FF");
    public final static Color GAINSBORO = new Color("GAINSBORO", "#DCDCDC");
    public final static Color GHOSTWHITE = new Color("GHOSTWHITE", "#F8F8FF");
    public final static Color GOLD = new Color("GOLD", "#FFD700");
    public final static Color GOLDENROD = new Color("GOLDENROD", "#DAA520");
    public final static Color GRAY = new Color("GRAY", "#808080");
    public final static Color GREY = new Color("GREY", "#808080");
    public final static Color GREEN = new Color("GREEN", "#008000");
    public final static Color GREENYELLOW = new Color("GREENYELLOW", "#ADFF2F");
    public final static Color HONEYDEW = new Color("HONEYDEW", "#F0FFF0");
    public final static Color HOTPINK = new Color("HOTPINK", "#FF69B4");
    public final static Color INDIANRED = new Color("INDIANRED", "#CD5C5C");
    public final static Color INDIGO = new Color("INDIGO", "#4B0082");
    public final static Color IVORY = new Color("IVORY", "#FFFFF0");
    public final static Color KHAKI = new Color("KHAKI", "#F0E68C");
    public final static Color LAVENDER = new Color("LAVENDER", "#E6E6FA");
    public final static Color LAVENDERBLUSH = new Color("LAVENDERBLUSH", "#FFF0F5");
    public final static Color LAWNGREEN = new Color("LAWNGREEN", "#7CFC00");
    public final static Color LEMONCHIFFON = new Color("LEMONCHIFFON", "#FFFACD");
    public final static Color LIGHTBLUE = new Color("LIGHTBLUE", "#ADD8E6");
    public final static Color LIGHTCORAL = new Color("LIGHTCORAL", "#F08080");
    public final static Color LIGHTCYAN = new Color("LIGHTCYAN", "#E0FFFF");
    public final static Color LIGHTGOLDENRODYELLOW = new Color("LIGHTGOLDENRODYELLOW", "#FAFAD2");
    public final static Color LIGHTGRAY = new Color("LIGHTGRAY", "#D3D3D3");
    public final static Color LIGHTGREY = new Color("LIGHTGREY", "#D3D3D3");
    public final static Color LIGHTGREEN = new Color("LIGHTGREEN", "#90EE90");
    public final static Color LIGHTPINK = new Color("LIGHTPINK", "#FFB6C1");
    public final static Color LIGHTSALMON = new Color("LIGHTSALMON", "#FFA07A");
    public final static Color LIGHTSEAGREEN = new Color("LIGHTSEAGREEN", "#20B2AA");
    public final static Color LIGHTSKYBLUE = new Color("LIGHTSKYBLUE", "#87CEFA");
    public final static Color LIGHTSLATEGRAY = new Color("LIGHTSLATEGRAY", "#778899");
    public final static Color LIGHTSLATEGREY = new Color("LIGHTSLATEGREY", "#778899");
    public final static Color LIGHTSTEELBLUE = new Color("LIGHTSTEELBLUE", "#B0C4DE");
    public final static Color LIGHTYELLOW = new Color("LIGHTYELLOW", "#FFFFE0");
    public final static Color LIME = new Color("LIME", "#00FF00");
    public final static Color LIMEGREEN = new Color("LIMEGREEN", "#32CD32");
    public final static Color LINEN = new Color("LINEN", "#FAF0E6");
    public final static Color MAGENTA = new Color("MAGENTA", "#FF00FF");
    public final static Color MAROON = new Color("MAROON", "#800000");
    public final static Color MEDIUMAQUAMARINE = new Color("MEDIUMAQUAMARINE", "#66CDAA");
    public final static Color MEDIUMBLUE = new Color("MEDIUMBLUE", "#0000CD");
    public final static Color MEDIUMORCHID = new Color("MEDIUMORCHID", "#BA55D3");
    public final static Color MEDIUMPURPLE = new Color("MEDIUMPURPLE", "#9370DB");
    public final static Color MEDIUMSEAGREEN = new Color("MEDIUMSEAGREEN", "#3CB371");
    public final static Color MEDIUMSLATEBLUE = new Color("MEDIUMSLATEBLUE", "#7B68EE");
    public final static Color MEDIUMSPRINGGREEN = new Color("MEDIUMSPRINGGREEN", "#00FA9A");
    public final static Color MEDIUMTURQUOISE = new Color("MEDIUMTURQUOISE", "#48D1CC");
    public final static Color MEDIUMVIOLETRED = new Color("MEDIUMVIOLETRED", "#C71585");
    public final static Color MIDNIGHTBLUE = new Color("MIDNIGHTBLUE", "#191970");
    public final static Color MINTCREAM = new Color("MINTCREAM", "#F5FFFA");
    public final static Color MISTYROSE = new Color("MISTYROSE", "#FFE4E1");
    public final static Color MOCCASIN = new Color("MOCCASIN", "#FFE4B5");
    public final static Color NAVAJOWHITE = new Color("NAVAJOWHITE", "#FFDEAD");
    public final static Color NAVY = new Color("NAVY", "#000080");
    public final static Color OLDLACE = new Color("OLDLACE", "#FDF5E6");
    public final static Color OLIVE = new Color("OLIVE", "#808000");
    public final static Color OLIVEDRAB = new Color("OLIVEDRAB", "#6B8E23");
    public final static Color ORANGE = new Color("ORANGE", "#FFA500");
    public final static Color ORANGERED = new Color("ORANGERED", "#FF4500");
    public final static Color ORCHID = new Color("ORCHID", "#DA70D6");
    public final static Color PALEGOLDENROD = new Color("PALEGOLDENROD", "#EEE8AA");
    public final static Color PALEGREEN = new Color("PALEGREEN", "#98FB98");
    public final static Color PALETURQUOISE = new Color("PALETURQUOISE", "#AFEEEE");
    public final static Color PALEVIOLETRED = new Color("PALEVIOLETRED", "#DB7093");
    public final static Color PAPAYAWHIP = new Color("PAPAYAWHIP", "#FFEFD5");
    public final static Color PEACHPUFF = new Color("PEACHPUFF", "#FFDAB9");
    public final static Color PERU = new Color("PERU", "#CD853F");
    public final static Color PINK = new Color("PINK", "#FFC0CB");
    public final static Color PLUM = new Color("PLUM", "#DDA0DD");
    public final static Color POWDERBLUE = new Color("POWDERBLUE", "#B0E0E6");
    public final static Color PURPLE = new Color("PURPLE", "#800080");
    public final static Color RED = new Color("RED", "#FF0000");
    public final static Color ROSYBROWN = new Color("ROSYBROWN", "#BC8F8F");
    public final static Color ROYALBLUE = new Color("ROYALBLUE", "#4169E1");
    public final static Color SADDLEBROWN = new Color("SADDLEBROWN", "#8B4513");
    public final static Color SALMON = new Color("SALMON", "#FA8072");
    public final static Color SANDYBROWN = new Color("SANDYBROWN", "#F4A460");
    public final static Color SEAGREEN = new Color("SEAGREEN", "#2E8B57");
    public final static Color SEASHELL = new Color("SEASHELL", "#FFF5EE");
    public final static Color SIENNA = new Color("SIENNA", "#A0522D");
    public final static Color SILVER = new Color("SILVER", "#C0C0C0");
    public final static Color SKYBLUE = new Color("SKYBLUE", "#87CEEB");
    public final static Color SLATEBLUE = new Color("SLATEBLUE", "#6A5ACD");
    public final static Color SLATEGRAY = new Color("SLATEGRAY", "#708090");
    public final static Color SLATEGREY = new Color("SLATEGREY", "#708090");
    public final static Color SNOW = new Color("SNOW", "#FFFAFA");
    public final static Color SPRINGGREEN = new Color("SPRINGGREEN", "#00FF7F");
    public final static Color STEELBLUE = new Color("STEELBLUE", "#4682B4");
    public final static Color TAN = new Color("TAN", "#D2B48C");
    public final static Color TEAL = new Color("TEAL", "#008080");
    public final static Color THISTLE = new Color("THISTLE", "#D8BFD8");
    public final static Color TOMATO = new Color("TOMATO", "#FF6347");
    public final static Color TURQUOISE = new Color("TURQUOISE", "#40E0D0");
    public final static Color VIOLET = new Color("VIOLET", "#EE82EE");
    public final static Color WHEAT = new Color("WHEAT", "#F5DEB3");
    public final static Color WHITE = new Color("WHITE", "#FFFFFF");
    public final static Color WHITESMOKE = new Color("WHITESMOKE", "#F5F5F5");
    public final static Color YELLOW = new Color("YELLOW", "#FFFF00");
    public final static Color YELLOWGREEN = new Color("YELLOWGREEN", "#9ACD32");

    private final String value;

    public Color(String value) {
        checkNotNull(value);

        this.value = value;
    }

    private Color(String name, String code) {
        this(code);
        namedColors.put(name, this);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Color that = (Color) obj;

        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    public static Color valueOf(String color) {
        Color namedColor = namedColors.get(StringUtils.upperCase(color));
        if (namedColor != null) {
            return namedColor;
        }

        return new Color(color);
    }
}