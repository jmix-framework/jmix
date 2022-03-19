/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.widget.client.grid.selection;

import com.google.gwt.dom.client.NativeEvent;
import io.jmix.ui.widget.client.Tools;
import io.jmix.ui.widget.grid.JmixSingleSelectionModel;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.connectors.grid.SingleSelectionModelConnector;
import com.vaadin.client.widget.grid.events.BodyClickHandler;
import com.vaadin.client.widget.grid.selection.ClickSelectHandler;
import com.vaadin.client.widgets.Grid;
import com.vaadin.shared.ui.Connect;
import elemental.json.JsonObject;

@Connect(JmixSingleSelectionModel.class)
public class JmixSingleSelectionModelConnector extends SingleSelectionModelConnector {

    @Override
    protected void extend(ServerConnector target) {
        super.extend(target);
    }

    @Override
    protected ClickSelectHandler<JsonObject> createClickSelectHandler() {
        return Tools.isUseSimpleMultiselectForTouchDevice()
                ? super.createClickSelectHandler()
                : new JmixClickSelectHandler(getGrid());
    }

    protected class JmixClickSelectHandler extends ClickSelectHandler<JsonObject> {

        public JmixClickSelectHandler(Grid<JsonObject> grid) {
            super(grid);
        }

        @Override
        protected BodyClickHandler createBodyClickHandler(Grid<JsonObject> grid) {
            return event -> {
                JsonObject row = grid.getEventCell().getRow();
                NativeEvent e = event.getNativeEvent();

                if (!e.getCtrlKey() && !e.getMetaKey()) {
                    if (!grid.isSelected(row)) {
                        grid.select(row);
                    }
                } else {
                    if (!grid.isSelected(row)) {
                        grid.select(row);
                    } else if (isDeselectAllowed()) {
                        grid.deselect(row);
                    }
                }
            };
        }
    }
}
