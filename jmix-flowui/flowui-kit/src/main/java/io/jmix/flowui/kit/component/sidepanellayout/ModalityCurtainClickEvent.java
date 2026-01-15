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

package io.jmix.flowui.kit.component.sidepanellayout;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Event fired when the user clicks on the modality curtain.
 */
@DomEvent("jmix-side-panel-layout-modality-curtain-click")
public class ModalityCurtainClickEvent extends ComponentEvent<JmixSidePanelLayout> {

    protected final int pageX;

    protected final int pageY;

    protected final boolean altKey;

    protected final boolean ctrlKey;

    protected final boolean metaKey;

    protected final boolean shiftKey;

    public ModalityCurtainClickEvent(JmixSidePanelLayout source, boolean fromClient,
                                     @EventData("event.detail.originalEvent.pageX") Integer pageX,
                                     @EventData("event.detail.originalEvent.pageY") Integer pageY,
                                     @EventData("event.detail.originalEvent.altKey") Boolean altKey,
                                     @EventData("event.detail.originalEvent.ctrlKey") Boolean ctrlKey,
                                     @EventData("event.detail.originalEvent.metaKey") Boolean metaKey,
                                     @EventData("event.detail.originalEvent.shiftKey") Boolean shiftKey) {
        super(source, fromClient);

        this.pageX = pageX;
        this.pageY = pageY;
        this.altKey = altKey;
        this.ctrlKey = ctrlKey;
        this.metaKey = metaKey;
        this.shiftKey = shiftKey;
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
}
