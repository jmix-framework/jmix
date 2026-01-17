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

package io.jmix.fullcalendarflowui.kit.component.event;

import io.jmix.fullcalendarflowui.kit.component.model.dom.DomMouseEventDetails;

/**
 * Class contains mouse details when the user clicks in a map component.
 */
public class MouseEventDetails {

    protected int clickCount;

    protected final MouseButton button;

    protected final int pageX;

    protected final int pageY;

    protected final boolean altKey;

    protected final boolean ctrlKey;

    protected final boolean metaKey;

    protected final boolean shiftKey;

    public MouseEventDetails(DomMouseEventDetails event) {
        this(event.getClickCount(),
                MouseButton.of(event.getButton()),
                event.getPageX(),
                event.getPageY(),
                event.isAltKey(),
                event.isCtrlKey(),
                event.isMetaKey(),
                event.isShiftKey());
    }

    public MouseEventDetails(int clickCount,
                             MouseButton button,
                             int pageX,
                             int pageY,
                             boolean altKey,
                             boolean ctrlKey,
                             boolean metaKey,
                             boolean shiftKey) {
        this.clickCount = clickCount;
        this.button = button;
        this.pageX = pageX;
        this.pageY = pageY;
        this.altKey = altKey;
        this.ctrlKey = ctrlKey;
        this.metaKey = metaKey;
        this.shiftKey = shiftKey;
    }

    /**
     * @return the number of consecutive clicks recently recorded
     */
    public int getClickCount() {
        return clickCount;
    }

    /**
     * @return mouse button that was pressed
     */
    public MouseButton getButton() {
        return button;
    }

    /**
     * @return the X coordinate of the mouse pointer relative to the whole document
     */
    public int getPageX() {
        return pageX;
    }

    /**
     * @return the Y coordinate of the mouse pointer relative to the whole document
     */
    public int getPageY() {
        return pageY;
    }

    /**
     * @return {@code true} if the {@code alt} key was down when the mouse event was fired
     */
    public boolean isAltKey() {
        return altKey;
    }

    /**
     * @return {@code true} if the {@code control} key was down when the mouse event was fired
     */
    public boolean isCtrlKey() {
        return ctrlKey;
    }

    /**
     * @return {@code true} if the {@code meta} key was down when the mouse event was fired
     */
    public boolean isMetaKey() {
        return metaKey;
    }

    /**
     * @return {@code true} if the {@code shift} key was down when the mouse event was fired.
     */
    public boolean isShiftKey() {
        return shiftKey;
    }

    /**
     * Contains for mouse button that was pressed.
     */
    public enum MouseButton {
        LEFT(0), MIDDLE(1), RIGHT(2);

        private Integer id;

        MouseButton(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }

        public static MouseButton of(int id) {
            for (MouseButton at : MouseButton.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }
}
