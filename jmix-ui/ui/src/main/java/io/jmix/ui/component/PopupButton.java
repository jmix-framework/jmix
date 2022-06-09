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
 * A {@link Button} with a popup. The popup can contain actions or popup panel with custom content.
 */
@StudioComponent(
        caption = "PopupButton",
        category = "Components",
        xmlElement = "popupButton",
        icon = "io/jmix/ui/icon/component/popupButton.svg",
        canvasTextProperty = "caption",
        canvasBehaviour = CanvasBehaviour.POPUP_BUTTON,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/popup-button.html"
)
public interface PopupButton extends ActionsHolder, Component.HasCaption, Component.BelongToFrame,
        Component.HasIcon, Component.Focusable, HasHtmlCaption, HasHtmlDescription, HasHtmlSanitizer {

    String NAME = "popupButton";

    /**
     * @return true if popup is opened
     */
    boolean isPopupVisible();

    /**
     * Open or close popup panel.
     *
     * @param popupVisible whether open or close popup panel.
     */
    void setPopupVisible(boolean popupVisible);

    /**
     * Sets menu width.
     *
     * @param width new menu width
     */
    @StudioProperty(type = PropertyType.SIZE)
    void setMenuWidth(@Nullable String width);

    /**
     * @return menu width
     */
    float getMenuWidth();

    /**
     * Gets the menu width property units.
     *
     * @return units used in the menu width property.
     */
    SizeUnit getMenuWidthSizeUnit();

    /**
     * @return whether to close menu automatically after action triggering or not
     */
    boolean isAutoClose();

    /**
     * Sets menu automatic close after option click.
     *
     * @param autoClose whether to close menu automatically after action triggering or not
     */
    @StudioProperty(defaultValue = "true")
    void setAutoClose(boolean autoClose);

    /**
     * Sets show icons for action buttons
     */
    @StudioProperty(defaultValue = "false")
    void setShowActionIcons(boolean showActionIcons);

    /**
     * Returns show icons for action buttons
     */
    boolean isShowActionIcons();

    /**
     * @return if sequential click on popup will toggle popup visibility
     */
    boolean isTogglePopupVisibilityOnClick();

    /**
     * Sets sequential click on popup will toggle popup visibility.
     *
     * @param togglePopupVisibilityOnClick true if sequential click on popup should toggle popup visibility
     */
    @StudioProperty(defaultValue = "true")
    void setTogglePopupVisibilityOnClick(boolean togglePopupVisibilityOnClick);

    /**
     * @return opening direction for the popup
     */
    PopupOpenDirection getPopupOpenDirection();

    /**
     * Sets opening direction for the popup.
     *
     * @param direction new direction
     */
    @StudioProperty(type = PropertyType.ENUMERATION, options = {"BOTTOM_LEFT", "BOTTOM_RIGHT", "BOTTOM_CENTER"})
    void setPopupOpenDirection(PopupOpenDirection direction);

    /**
     * @return true if a click outside the popup closing the popup, otherwise - false
     */
    boolean isClosePopupOnOutsideClick();

    /**
     * If set to true, clicking on outside the popup closes it. Note that this doesn't affect clicking on the button itself.
     *
     * @param closePopupOnOutsideClick whether to close popup on outside click
     */
    @StudioProperty(defaultValue = "true")
    void setClosePopupOnOutsideClick(boolean closePopupOnOutsideClick);

    /**
     * Sets custom inner content for the popup. Actions are ignored if a custom popup content is set.
     *
     * @param popupContent popup component.
     */
    void setPopupContent(@Nullable Component popupContent);

    /**
     * @return popup content component
     */
    @Nullable
    Component getPopupContent();

    Subscription addPopupVisibilityListener(Consumer<PopupVisibilityEvent> listener);

    /**
     * Event sent when the visibility of the popup changes.
     */
    class PopupVisibilityEvent extends EventObject {
        public PopupVisibilityEvent(PopupButton popupButton) {
            super(popupButton);
        }

        @Override
        public PopupButton getSource() {
            return (PopupButton) super.getSource();
        }
    }

    /**
     * Opening direction for the popup.
     */
    enum PopupOpenDirection {
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        BOTTOM_CENTER,
    }
}