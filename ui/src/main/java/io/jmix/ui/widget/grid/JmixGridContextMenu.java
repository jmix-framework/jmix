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

package io.jmix.ui.widget.grid;

import io.jmix.ui.widget.addon.contextmenu.GridContextMenu;
import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.shared.ui.grid.GridConstants;
import com.vaadin.ui.Grid;

import static com.vaadin.event.ContextClickEvent.ContextClickNotifier;
import static com.vaadin.ui.Grid.GridContextClickEvent;

public class JmixGridContextMenu<T> extends GridContextMenu<T> {

    protected ContextClickListener contextClickListener;

    protected boolean enabled = true;

    public JmixGridContextMenu(Grid<T> parentComponent) {
        super(parentComponent);
    }

    @Override
    public void setAsContextMenuOf(ContextClickNotifier component) {
        if (contextClickListener == null) {
            contextClickListener = this::onContextClick;
        }
        component.addContextClickListener(contextClickListener);
    }

    protected void onContextClick(ContextClickEvent event) {
        if (!isEnabled()) {
            return;
        }

        // prevent opening context menu in non BODY sections
        if (event instanceof Grid.GridContextClickEvent) {
            GridContextClickEvent gridEvent = (GridContextClickEvent) event;
            if (!gridEvent.getSection().equals(GridConstants.Section.BODY)) {
                return;
            }
        }

        fireEvent(new ContextMenuOpenListener.ContextMenuOpenEvent(JmixGridContextMenu.this, event));

        open(event.getClientX(), event.getClientY());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
