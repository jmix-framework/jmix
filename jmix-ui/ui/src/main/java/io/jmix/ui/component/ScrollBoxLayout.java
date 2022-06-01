/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component;

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.ContainerType;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;

/**
 * Component container that shows scrollbars if its content does not fit the viewport.
 */
@StudioComponent(
        caption = "VerticalScrollBox",
        category = "Containers",
        xmlElement = "scrollBox",
        icon = "io/jmix/ui/icon/container/verticalScrollBox.svg",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.SCROLL_BOX,
        unsupportedProperties = {"orientation"},
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/scroll-box-layout.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "height", type = PropertyType.SIZE, defaultValue = "-1px", initialValue = "100px"),
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "100%")
        }
)
public interface ScrollBoxLayout
        extends OrderedContainer, Component.BelongToFrame, HasMargin, HasSpacing, HasOrientation,
        Component.HasIcon, Component.HasCaption, ShortcutNotifier, HasContextHelp,
        HasHtmlCaption, HasHtmlDescription, HasRequiredIndicator, LayoutClickNotifier, HasHtmlSanitizer {

    String NAME = "scrollBox";

    ScrollBarPolicy getScrollBarPolicy();

    void setScrollBarPolicy(ScrollBarPolicy scrollBarPolicy);

    /**
     * Sets content width.
     *
     * @param width width
     */
    @StudioProperty(name = "contentWidth", type = PropertyType.SIZE)
    void setContentWidth(@Nullable String width);

    /**
     * @return content width value
     */
    float getContentWidth();

    /**
     * @return content width size unit
     */
    SizeUnit getContentWidthSizeUnit();

    /**
     * Sets content height.
     *
     * @param height height
     */
    @StudioProperty(name = "contentHeight", type = PropertyType.SIZE)
    void setContentHeight(@Nullable String height);

    /**
     * @return content height value
     */
    float getContentHeight();

    /**
     * @return content height size unit
     */
    SizeUnit getContentHeightSizeUnit();

    /**
     * Sets minimum CSS width for content. Examples: "640px", "auto".
     *
     * @param minWidth minimum width
     */
    @StudioProperty(name = "contentMinWidth", type = PropertyType.SIZE)
    void setContentMinWidth(String minWidth);

    /**
     * @return minimal content width
     */
    @Nullable
    String getContentMinWidth();

    /**
     * Sets maximum CSS width for content. Examples: "640px", "100%".
     *
     * @param maxWidth maximum width
     */
    @StudioProperty(name = "contentMaxWidth", type = PropertyType.SIZE)
    void setContentMaxWidth(String maxWidth);

    /**
     * @return maximum content width
     */
    @Nullable
    String getContentMaxWidth();

    /**
     * Sets minimum CSS height for content. Examples: "640px", "auto".
     *
     * @param minHeight minimum height
     */
    @StudioProperty(name = "contentMinHeight", type = PropertyType.SIZE)
    void setContentMinHeight(String minHeight);

    /**
     * @return minimum content width
     */
    @Nullable
    String getContentMinHeight();

    /**
     * Sets maximum CSS height for content. Examples: "640px", "100%".
     *
     * @param maxHeight maximum height
     */
    @StudioProperty(name = "contentMaxHeight", type = PropertyType.SIZE)
    void setContentMaxHeight(String maxHeight);

    /**
     * @return maximum content width
     */
    @Nullable
    String getContentMaxHeight();

    /**
     * Gets scroll left offset.
     * <p>
     * Scrolling offset is the number of pixels this scrollable has been
     * scrolled right.
     *
     * @return horizontal scrolling position in pixels
     */
    int getScrollLeft();

    /**
     * Sets scroll left offset.
     * <p>
     * Scrolling offset is the number of pixels this scrollable has been
     * scrolled right.
     *
     * @param scrollLeft the xOffset
     */
    void setScrollLeft(int scrollLeft);

    /**
     * Gets scroll top offset.
     * <p>
     * Scrolling offset is the number of pixels this scrollable has been
     * scrolled down.
     *
     * @return vertical scrolling position in pixels
     */
    int getScrollTop();

    /**
     * Sets scroll top offset.
     * <p>
     * Scrolling offset is the number of pixels this scrollable has been
     * scrolled down.
     *
     * @param scrollTop the yOffset
     */
    void setScrollTop(int scrollTop);

    enum ScrollBarPolicy {
        VERTICAL,
        HORIZONTAL,
        BOTH,
        NONE
    }
}
