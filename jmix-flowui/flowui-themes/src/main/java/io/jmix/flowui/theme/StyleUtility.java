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

package io.jmix.flowui.theme;

/**
 * Contains the definition for all CSS utility classes suitable for any theme.
 */
public final class StyleUtility {

    private StyleUtility() {
    }

    public static final class Button {

        public static final String NESTED_BUTTON = "nested-button";
        public static final String DIALOG_CLOSE_BUTTON = "dialog-close-button";

        private Button() {
        }
    }

    /**
     * Containers related classes.
     */
    public static final class Container {

        public static final String BUTTONS_PANEL = "buttons-panel";

        public static final String BORDERED_PANEL = "bordered-panel";
        public static final String FOOTER_PANEL = "footer-panel";

        private Container() {
        }
    }

    /**
     * Classes for distributing space around and between items along a flexbox’s
     * cross axis or a grid’s block axis. Applies to flexbox and grid layouts.
     */
    public static final class AlignContent {

        public static final String AROUND = "content-around";
        public static final String BETWEEN = "content-between";
        public static final String CENTER = "content-center";
        public static final String END = "content-end";
        public static final String EVENLY = "content-evenly";
        public static final String START = "content-start";
        public static final String STRETCH = "content-stretch";

        private AlignContent() {
        }

    }

    /**
     * Classes for aligning items along a flexbox’s cross axis or a grid’s block
     * axis. Applies to flexbox and grid layouts.
     */
    public static final class AlignItems {
        public static final String BASELINE = "items-baseline";
        public static final String CENTER = "items-center";
        public static final String END = "items-end";
        public static final String START = "items-start";
        public static final String STRETCH = "items-stretch";

        private AlignItems() {
        }

    }

    /**
     * Classes for overriding individual items' align-item property. Applies to
     * flexbox and grid items.
     */
    public static final class AlignSelf {

        public static final String AUTO = "self-auto";
        public static final String BASELINE = "self-baseline";
        public static final String CENTER = "self-center";
        public static final String END = "self-end";
        public static final String START = "self-start";
        public static final String STRETCH = "self-stretch";

        private AlignSelf() {
        }

    }

    /**
     * Classes for setting how items grow and shrink in a flexbox layout.
     * Applies to flexbox items.
     */
    public static final class Flex {

        public static final String ONE = "flex-1";
        public static final String AUTO = "flex-auto";
        public static final String NONE = "flex-none";

        public static final String GROW = "flex-grow";
        public static final String GROW_NONE = "flex-grow-0";

        public static final String SHRINK = "flex-shrink";
        public static final String SHRINK_NONE = "flex-shrink-0";

        private Flex() {
        }

    }

    /**
     * Classes for setting the flex direction of a flexbox layout.
     */
    public static final class FlexDirection {

        public static final String COLUMN = "flex-col";
        public static final String COLUMN_REVERSE = "flex-col-reverse";
        public static final String ROW = "flex-row";
        public static final String ROW_REVERSE = "flex-row-reverse";

        private FlexDirection() {
        }

    }

    /**
     * Classes for setting how items wrap in a flexbox layout. Applies to
     * flexbox layouts.
     */
    public static final class FlexWrap {

        public static final String NOWRAP = "flex-nowrap";
        public static final String WRAP = "flex-wrap";
        public static final String WRAP_REVERSE = "flex-wrap-reverse";

        private FlexWrap() {
        }
    }

    /**
     * Classes for aligning items along a flexbox’s main axis or a grid’s inline
     * axis. Applies to flexbox and grid layouts.
     */
    public static final class JustifyContent {

        public static final String AROUND = "justify-around";
        public static final String BETWEEN = "justify-between";
        public static final String CENTER = "justify-center";
        public static final String END = "justify-end";
        public static final String EVENLY = "justify-evenly";
        public static final String START = "justify-start";

        private JustifyContent() {
        }

    }

    /**
     * Classes for setting the aspect ratio of an element.
     */
    public static final class AspectRatio {

        public static final String SQUARE = "aspect-square";
        public static final String VIDEO = "aspect-video";

        private AspectRatio() {

        }
    }

    /**
     * Classes for setting the box sizing property of an element. Box sizing
     * determines whether an element’s border and padding is considered a part
     * of its size.
     */
    public static final class BoxSizing {

        public static final String BORDER = "box-border";
        public static final String CONTENT = "box-content";

        private BoxSizing() {
        }

    }

    /**
     * Classes for setting the display property of an element. Determines
     * whether the element is a block or inline element and how its items are
     * laid out.
     */
    public static final class Display {

        public static final String BLOCK = "block";
        public static final String FLEX = "flex";
        public static final String GRID = "grid";
        public static final String HIDDEN = "hidden";
        public static final String INLINE = "inline";
        public static final String INLINE_BLOCK = "inline-block";
        public static final String INLINE_FLEX = "inline-flex";
        public static final String INLINE_GRID = "inline-grid";

        private Display() {
        }

    }

    /**
     * Classes for setting the overflow behavior of an element.
     */
    public static final class Overflow {

        public static final String AUTO = "overflow-auto";
        public static final String HIDDEN = "overflow-hidden";
        public static final String SCROLL = "overflow-scroll";

        private Overflow() {
        }

    }

    /**
     * Classes for setting the position of an element.
     */
    public static final class Position {

        public static final String ABSOLUTE = "absolute";
        public static final String FIXED = "fixed";
        public static final String RELATIVE = "relative";
        public static final String STATIC = "static";
        public static final String STICKY = "sticky";

        private Position() {
        }

    }

    public static final class Visibility {

        public static final String INVISIBLE = "invisible";
        public static final String VISIBLE = "visible";

        private Visibility() {
        }

    }

    /**
     * Classes for setting the margin of an element.
     */
    public static final class Margin {

        public static final String NONE = "m-0";
        public static final String AUTO = "m-auto";

        private Margin() {
        }

        /**
         * Classes for setting the bottom margin of an element.
         */
        public static final class Bottom {

            public static final String NONE = "mb-0";
            public static final String AUTO = "mb-auto";

            private Bottom() {
            }
        }

        /**
         * Classes for setting the logical inline end margin of an element. The
         * actual physical edge where the styles are applied depends on the text
         * flow of the element.
         */
        public static final class End {

            public static final String NONE = "me-0";
            public static final String AUTO = "me-auto";

            private End() {
            }
        }

        /**
         * Classes for setting both the left and the right margins an element.
         */
        public static final class Horizontal {

            public static final String NONE = "mx-0";
            public static final String AUTO = "mx-auto";

            private Horizontal() {
            }
        }

        /**
         * Classes for setting the left margin of an element.
         */
        public static final class Left {

            public static final String NONE = "ml-0";
            public static final String AUTO = "ml-auto";

            private Left() {
            }
        }

        /**
         * Classes for setting the right margin of an element.
         */
        public static final class Right {

            public static final String NONE = "mr-0";
            public static final String AUTO = "mr-auto";

            private Right() {
            }
        }

        /**
         * Classes for setting the logical inline start margin of an element.
         * The actual physical edge where the styles are applied depends on the
         * text flow of the element.
         */
        public static final class Start {

            public static final String NONE = "ms-0";
            public static final String AUTO = "ms-auto";

            private Start() {
            }
        }

        /**
         * Classes for setting the top margin of an element.
         */
        public static final class Top {

            public static final String NONE = "mt-0";
            public static final String AUTO = "mt-auto";

            private Top() {
            }
        }

        /**
         * Classes for setting both the top and the bottom margins of an
         * element.
         */
        public static final class Vertical {

            public static final String NONE = "my-0";
            public static final String AUTO = "my-auto";

            private Vertical() {
            }
        }
    }

    /**
     * Classes for setting the font weight of an element.
     */
    public static final class FontWeight {

        public static final String THIN = "font-thin";
        public static final String EXTRALIGHT = "font-extralight";
        public static final String LIGHT = "font-light";
        public static final String NORMAL = "font-normal";
        public static final String MEDIUM = "font-medium";
        public static final String SEMIBOLD = "font-semibold";
        public static final String BOLD = "font-bold";
        public static final String EXTRABOLD = "font-extrabold";
        public static final String BLACK = "font-black";

        private FontWeight() {
        }
    }

    /**
     * Classes for setting an element’s text alignment.
     */
    public static final class TextAlignment {

        public static final String LEFT = "text-left";
        public static final String CENTER = "text-center";
        public static final String RIGHT = "text-right";
        public static final String JUSTIFY = "text-justify";

        private TextAlignment() {
        }

    }

    /**
     * Classes for setting the text decoration.
     */
    public static final class TextDecoration {

        public static final String LINE_THROUGH = "line-through";
        public static final String NONE = "no-underline";
        public static final String UNDERLINE = "underline";

        private TextDecoration() {
        }

    }

    /**
     * Classes for setting the text overflow.
     */
    public static final class TextOverflow {

        public static final String CLIP = "overflow-clip";
        public static final String ELLIPSIS = "overflow-ellipsis";

        private TextOverflow() {
        }

    }

    /**
     * Classes for transforming the text.
     */
    public static final class TextTransform {

        public static final String CAPITALIZE = "capitalize";
        public static final String LOWERCASE = "lowercase";
        public static final String UPPERCASE = "uppercase";

        private TextTransform() {
        }

    }

    /**
     * Classes for setting how the white space inside an element is handled.
     */
    public static final class Whitespace {

        public static final String NORMAL = "whitespace-normal";
        public static final String NOWRAP = "whitespace-nowrap";
        public static final String PRE = "whitespace-pre";
        public static final String PRE_LINE = "whitespace-pre-line";
        public static final String PRE_WRAP = "whitespace-pre-wrap";

        private Whitespace() {
        }

    }

    /**
     * Class for removing the default look of a list.
     */
    public static final class ListStyleType {

        public static final String NONE = "list-none";

        private ListStyleType() {
        }

    }
}
