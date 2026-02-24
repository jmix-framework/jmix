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

import com.vaadin.flow.component.ComponentEvent;

/**
 * Event fired when the {@link SideDialog} is opened or closed.
 */
public class SideDialogOpenedChangeEvent extends ComponentEvent<SideDialog> {

    protected final boolean opened;

    public SideDialogOpenedChangeEvent(SideDialog source, boolean fromClient) {
        super(source, fromClient);

        this.opened = source.isOpened();
    }

    /**
     * @return {@code true} if the dialog is opened, {@code false} otherwise
     */
    public boolean isOpened() {
        return opened;
    }
}
