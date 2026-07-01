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

/**
 * Event fired before opening the side panel. The side panel is not displayed and animation is not started yet.
 * <p>
 * Use this event when you need to add/remove components to the side panel before it is opened.
 * <p>
 * <strong>Note</strong> that within this event focusing an element inside the side panel might break
 * the side panel animation.
 *
 * @see SidePanelAfterOpenEvent
 */
public class SidePanelBeforeOpenEvent extends ComponentEvent<JmixSidePanelLayout> {

    public SidePanelBeforeOpenEvent(JmixSidePanelLayout source, boolean fromClient) {
        super(source, fromClient);
    }
}
