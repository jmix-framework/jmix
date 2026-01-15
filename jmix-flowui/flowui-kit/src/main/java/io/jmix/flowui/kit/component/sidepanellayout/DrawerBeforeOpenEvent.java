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
 * Event fired before opening the drawer. Drawer panel is not displayed and animation is not started.
 * <p>
 * Use this event when you need to add/remove components to the drawer panel before it is opened.
 * <p>
 * <strong>Note</strong> that within this event focusing an element inside the drawer might break
 * the drawer animation.
 *
 * @see DrawerAfterOpenEvent
 */
public class DrawerBeforeOpenEvent extends ComponentEvent<JmixDrawerLayout> {

    public DrawerBeforeOpenEvent(JmixDrawerLayout source, boolean fromClient) {
        super(source, fromClient);
    }
}
