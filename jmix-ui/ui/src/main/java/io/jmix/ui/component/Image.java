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
import io.jmix.ui.component.data.HasValueSource;
import io.jmix.ui.meta.CanvasIconSize;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * The Image component is intended for displaying graphic content.
 */
@StudioComponent(
        caption = "Image",
        category = "Components",
        xmlElement = "image",
        icon = "io/jmix/ui/icon/component/image.svg",
        canvasIconSize = CanvasIconSize.LARGE,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/image.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "editable", type = PropertyType.BOOLEAN),
                @StudioProperty(name = "required", type = PropertyType.BOOLEAN),
                @StudioProperty(name = "requiredMessage", type = PropertyType.STRING),
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF,
                        options = {"byteArray", "fileRef"}, typeParameter = "T"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface Image<T> extends ResourceView, HasValueSource<T> {
    String NAME = "image";

    /**
     * Resets the component source and disposes of the corresponding resource.
     */
    default void reset() {
        setSource((Resource) null);
    }

    /**
     * @return image scale mode
     */
    ScaleMode getScaleMode();

    /**
     * Applies the given scale mode to the image.
     *
     * @param scaleMode scale mode
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "NONE",
            options = {"NONE", "FILL", "CONTAIN", "COVER", "SCALE_DOWN"})
    void setScaleMode(ScaleMode scaleMode);

    /**
     * Defines image scale mode.
     * Corresponds to the {@code object-fit} CSS property.
     */
    enum ScaleMode {
        /**
         * The image is stretched according to the size of the component.
         */
        FILL,
        /**
         * The image is compressed or stretched to fit the component dimensions while preserving the proportions.
         */
        CONTAIN,
        /**
         * The image is compressed or stretched to fill the entire component while preserving the proportions.
         * If the image proportions do not match the component's proportions then the image will be clipped to fit.
         */
        COVER,
        /**
         * The image is sized as if {@link #NONE} or {@link #CONTAIN} were specified, whichever would result
         * in a smaller concrete object size.
         */
        SCALE_DOWN,
        /**
         * The image is not resized.
         */
        NONE
    }

    Subscription addClickListener(Consumer<ClickEvent> listener);

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    void removeClickListener(Consumer<ClickEvent> listener);

    /**
     * A {@link ClickEvent} is fired when the user clicks on an <code>Image</code>.
     */
    class ClickEvent extends EventObject {
        private final MouseEventDetails details;

        public ClickEvent(Image source, MouseEventDetails details) {
            super(source);
            this.details = details;
        }

        @Override
        public Image getSource() {
            return (Image) super.getSource();
        }

        /**
         * Returns an identifier describing which mouse button the user pushed.
         * Compare with {@link MouseEventDetails.MouseButton#LEFT},{@link MouseEventDetails.MouseButton#MIDDLE},
         * {@link MouseEventDetails.MouseButton#RIGHT} to find out which button it is.
         *
         * @return one of {@link MouseEventDetails.MouseButton#LEFT}, {@link MouseEventDetails.MouseButton#MIDDLE}, {@link MouseEventDetails.MouseButton#RIGHT}.
         */
        public MouseEventDetails.MouseButton getButton() {
            return details.getButton();
        }

        /**
         * Returns the mouse position (x coordinate) when the click took place.
         * The position is relative to the browser client area.
         *
         * @return The mouse cursor x position
         */
        public int getClientX() {
            return details.getClientX();
        }

        /**
         * Returns the mouse position (y coordinate) when the click took place.
         * The position is relative to the browser client area.
         *
         * @return The mouse cursor y position
         */
        public int getClientY() {
            return details.getClientY();
        }

        /**
         * Returns the relative mouse position (x coordinate) when the click
         * took place. The position is relative to the clicked component.
         *
         * @return The mouse cursor x position relative to the clicked layout
         * component or -1 if no x coordinate available
         */
        public int getRelativeX() {
            return details.getRelativeX();
        }

        /**
         * Returns the relative mouse position (y coordinate) when the click
         * took place. The position is relative to the clicked component.
         *
         * @return The mouse cursor y position relative to the clicked layout
         * component or -1 if no y coordinate available
         */
        public int getRelativeY() {
            return details.getRelativeY();
        }

        /**
         * Checks if the event is a double click event.
         *
         * @return {@code true} if the event is a double click event, {@code false} otherwise
         */
        public boolean isDoubleClick() {
            return details.isDoubleClick();
        }

        /**
         * Checks if the Alt key was down when the mouse event took place.
         *
         * @return {@code true} if Alt was down when the event occurred, {@code false} otherwise
         */
        public boolean isAltKey() {
            return details.isAltKey();
        }

        /**
         * Checks if the Ctrl key was down when the mouse event took place.
         *
         * @return {@code true} if Ctrl was pressed when the event occurred, {@code false} otherwise
         */
        public boolean isCtrlKey() {
            return details.isCtrlKey();
        }

        /**
         * Checks if the Meta key was down when the mouse event took place.
         *
         * @return {@code true} if Meta was pressed when the event occurred, {@code false} otherwise
         */
        public boolean isMetaKey() {
            return details.isMetaKey();
        }

        /**
         * Checks if the Shift key was down when the mouse event took place.
         *
         * @return {@code true} if Shift was pressed when the event occurred, {@code false} otherwise
         */
        public boolean isShiftKey() {
            return details.isShiftKey();
        }
    }
}
