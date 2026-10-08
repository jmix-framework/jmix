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
 * The event is fired once after the user finishes resizing the side panel by releasing the drag
 * handle. It does not fire during the drag.
 *
 * @see JmixSidePanelLayout#setSidePanelResizable(boolean)
 */
@DomEvent("jmix-side-panel-layout-after-resize-event")
public class SidePanelAfterResizeEvent extends ComponentEvent<JmixSidePanelLayout> {

    protected final String size;

    public SidePanelAfterResizeEvent(JmixSidePanelLayout source, boolean fromClient,
                                     @EventData("event.detail.size") String size) {
        super(source, fromClient);
        this.size = size;
    }

    /**
     * Returns the new side panel size: the width for horizontal positions, the height for vertical
     * positions. Reported as a CSS length in {@code px}.
     *
     * @return the new side panel size
     */
    public String getSize() {
        return size;
    }
}
