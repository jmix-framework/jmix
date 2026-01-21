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

package io.jmix.flowui.component.sidedialog;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.sidedialog.JmixSideDialog;
import io.jmix.flowui.kit.component.sidedialog.SideDialogPlacement;

/**
 * The composite component represents a side dialog with a header, content area, and footer. It functions as a drawer
 * panel.
 * <p>
 * The side dialog manages the opening and closing of the dialog and its content, featuring a pop-out animation.
 * It can be configured to appear relative to the application window (see {@link SideDialogPlacement}).
 */
public class SideDialog extends JmixSideDialog {

    /**
     * Adds a listener for opened change events.
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addOpenedChangeListener(ComponentEventListener<SideDialogOpenedChangeEvent> listener) {
        return getContent().addOpenedChangeListener(event -> {
            listener.onComponentEvent(new SideDialogOpenedChangeEvent(this, event.isFromClient()));
        });
    }

    /**
     * Add a listener that controls whether the dialog should be closed or not.
     * <p>
     * The listener is informed when the user wants to close the dialog by
     * clicking outside the dialog or by pressing escape. Then you can decide
     * whether to close or to keep opened the dialog. It means that dialog won't
     * be closed automatically unless you call {@link #close()} method
     * explicitly in the listener implementation.
     * <p>
     * NOTE: adding this listener changes the behavior of the dialog. Dialog is
     * closed automatically in case there are no any close listeners. And the
     * {@link #close()} method should be called explicitly to close the dialog
     * in case there are close listeners.
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addCloseActionListener(ComponentEventListener<SideDialogCloseActionEvent> listener) {
        return getContent().addDialogCloseActionListener(event -> {
            listener.onComponentEvent(new SideDialogCloseActionEvent(this, event.isFromClient()));
        });
    }
}
