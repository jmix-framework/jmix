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

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Resizable text area component.
 *
 * @param <V> type of value
 * @see TextArea
 */
@StudioComponent(
        caption = "ResizableTextArea",
        category = "Components",
        xmlElement = "resizableTextArea",
        icon = "io/jmix/ui/icon/component/resizableTextArea.svg",
        canvasBehaviour = CanvasBehaviour.TEXT_AREA,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/resizable-text-area.html"
)
public interface ResizableTextArea<V> extends TextArea<V> {
    String NAME = "resizableTextArea";

    /**
     * Allows resizing textArea in a given direction.
     *
     * @param direction the direction in which resizes textArea.
     */
    @StudioProperty(name = "resizableDirection", type = PropertyType.ENUMERATION, defaultValue = "BOTH",
            options = {"HORIZONTAL", "VERTICAL", "BOTH", "NONE"})
    void setResizableDirection(ResizeDirection direction);

    /**
     * Get the direction in which the textArea size changes.
     *
     * @return direction.
     */
    ResizeDirection getResizableDirection();

    /**
     * Adds a listener that is fired when the component is resized.
     *
     * @param listener a listener to add
     * @return a subscription
     */
    Subscription addResizeListener(Consumer<ResizeEvent> listener);

    class ResizeEvent extends EventObject {
        private final String prevWidth;
        private final String width;
        private final String prevHeight;
        private final String height;

        public ResizeEvent(ResizableTextArea component, String prevWidth, String width, String prevHeight, String height) {
            super(component);

            this.prevWidth = prevWidth;
            this.width = width;
            this.prevHeight = prevHeight;
            this.height = height;
        }

        /**
         * @return source component of event
         */
        @Override
        public ResizableTextArea getSource() {
            return (ResizableTextArea) super.getSource();
        }

        public String getHeight() {
            return height;
        }

        public String getPrevHeight() {
            return prevHeight;
        }

        public String getPrevWidth() {
            return prevWidth;
        }

        public String getWidth() {
            return width;
        }
    }

    /**
     * Represents directions in which textArea can be resized.
     */
    enum ResizeDirection {
        HORIZONTAL, VERTICAL, BOTH, NONE
    }
}