/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.kit.component.drawerlayout;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

@DomEvent("jmix-drawer-layout-modality-curtain-click")
public class ModalityCurtainClickEvent extends ComponentEvent<JmixDrawerLayout> {

    protected final int pageX;

    protected final int pageY;

    protected final boolean altKey;

    protected final boolean ctrlKey;

    protected final boolean metaKey;

    protected final boolean shiftKey;

    public ModalityCurtainClickEvent(JmixDrawerLayout source, boolean fromClient,
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

    public int getPageX() {
        return pageX;
    }

    public int getPageY() {
        return pageY;
    }

    public boolean isAltKey() {
        return altKey;
    }

    public boolean isCtrlKey() {
        return ctrlKey;
    }

    public boolean isMetaKey() {
        return metaKey;
    }

    public boolean isShiftKey() {
        return shiftKey;
    }
}
