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

package io.jmix.flowui.component.draweroverly;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.draweroverlay.JmixDrawerOverlay;

public class DrawerOverlay extends JmixDrawerOverlay {

    public Registration addOpenedChangeListener(ComponentEventListener<DrawerOverlayOpenedChangeEvent> listener) {
        return getContent().addOpenedChangeListener(event -> {
            listener.onComponentEvent(new DrawerOverlayOpenedChangeEvent(this, event.isFromClient()));
        });
    }

    public Registration addCloseActionListener(ComponentEventListener<DrawerOverlayCloseActionEvent> listener) {
        return getContent().addDialogCloseActionListener(event -> {
            listener.onComponentEvent(new DrawerOverlayCloseActionEvent(this, event.isFromClient()));
        });
    }
}
