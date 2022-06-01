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

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * A component for displaying a two different views to data. The minimized view is normally used to render the component,
 * and when it is clicked the full view is displayed on a popup.
 */
@StudioComponent(
        caption = "PopupView",
        category = "Components",
        xmlElement = "popupView",
        icon = "io/jmix/ui/icon/component/popupView.svg",
        canvasBehaviour = CanvasBehaviour.BUTTON,
        canvasText = "New Minimized Value",
        canvasTextProperty = "minimizedValue",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/popup-view.html"
)
public interface PopupView extends Component.HasCaption, Component.BelongToFrame,
        Component.HasIcon, HasContextHelp, HasHtmlCaption, HasHtmlDescription, HasHtmlSanitizer {
    String NAME = "popupView";

    /**
     * Sets visibility for the popup window.
     *
     * @param popupVisible popup visibility.
     */
    void setPopupVisible(boolean popupVisible);

    /**
     * @return true if popup is visible.
     */
    boolean isPopupVisible();

    /**
     * Sets value for the label of component. Value of the label can contain HTML.
     *
     * @param minimizedValue label text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setMinimizedValue(String minimizedValue);

    /**
     * @return value of the label of component.
     */
    String getMinimizedValue();

    /**
     * Sets inner content for the popup window.
     *
     * @param popupContent popup component.
     */
    void setPopupContent(@Nullable Component popupContent);

    /**
     * @return popup content component.
     */
    @Nullable
    Component getPopupContent();

    /**
     * Sets possibility to close popup window on cursor out.
     *
     * @param hideOnMouseOut popup hide option.
     */
    @StudioProperty(defaultValue = "true", initialValue = "false")
    void setHideOnMouseOut(boolean hideOnMouseOut);

    /**
     * @return true if popup window closes on cursor out.
     */
    boolean isHideOnMouseOut();

    /**
     * Sets caption rendering as HTML.
     *
     * @param captionAsHtml true if we want to show caption as HTML.
     */
    void setCaptionAsHtml(boolean captionAsHtml);

    /**
     * @return true if caption is shown as HTML.
     */
    boolean isCaptionAsHtml();

    /**
     * Sets the popup position.
     *
     * @param top  the top popup position in pixels
     * @param left the left popup position in pixels
     */
    void setPopupPosition(int top, int left);

    /**
     * Sets the top popup position.
     *
     * @param top the top popup position in pixels
     */
    @StudioProperty(name = "popupTop", defaultValue = "-1")
    void setPopupPositionTop(int top);

    /**
     * @return top popup position if position is set via {@link #setPopupPosition(int, int)}
     */
    int getPopupPositionTop();

    /**
     * Sets the left popup position.
     *
     * @param left the left popup position in pixels
     */
    @StudioProperty(name = "popupLeft", defaultValue = "-1")
    void setPopupPositionLeft(int left);

    /**
     * @return left popup position if position is set via {@link #setPopupPosition(int, int)}
     */
    int getPopupPositionLeft();

    /**
     * Sets the popup position.
     *
     * @param position the popup position
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "DEFAULT", options = {"DEFAULT", "TOP_LEFT",
            "TOP_CENTER", "TOP_RIGHT", "MIDDLE_LEFT", "MIDDLE_CENTER", "MIDDLE_RIGHT", "BOTTOM_LEFT", "BOTTOM_CENTER",
            "BOTTOM_RIGHT"})
    void setPopupPosition(@Nullable PopupPosition position);

    /**
     * return {@code PopupPosition} or {@code null} if position is set via {@link #setPopupPosition(PopupPosition)}
     */
    @Nullable
    PopupPosition getPopupPosition();

    /**
     * Popup position.
     */
    enum PopupPosition {
        /**
         * The default popup position is in the middle of the minimized value.
         */
        DEFAULT,

        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,

        MIDDLE_LEFT,
        MIDDLE_CENTER,
        MIDDLE_RIGHT,

        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT;

        @Nullable
        public static PopupPosition fromId(String position) {
            for (PopupPosition popupPosition : values()) {
                if (popupPosition.name().equals(position)) {
                    return popupPosition;
                }
            }
            return null;
        }
    }

    Subscription addPopupVisibilityListener(Consumer<PopupVisibilityEvent> listener);

    /**
     * Event sent when the visibility of the popup changes.
     */
    class PopupVisibilityEvent extends EventObject {
        public PopupVisibilityEvent(PopupView popupView) {
            super(popupView);
        }

        @Override
        public PopupView getSource() {
            return (PopupView) super.getSource();
        }

        public boolean isPopupVisible() {
            return getSource().isPopupVisible();
        }
    }
}