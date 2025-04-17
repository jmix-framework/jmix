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

package io.jmix.tabbedmode.component.tabsheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.tabs.Tab;
import elemental.json.JsonString;
import org.springframework.lang.Nullable;

import java.util.Arrays;

@DomEvent("main-tabsheet-drop")
public class MainTabsheetDropEvent extends ComponentEvent<JmixMainTabSheet> {

    protected final Tab dropTarget;
    protected final Tab dragSource;
    protected final MainTabsheetDropLocation dropLocation;

    public MainTabsheetDropEvent(JmixMainTabSheet source, boolean fromClient,
                                 @Nullable @EventData("event.detail.dropTargetTab") JsonString tabId,
                                 @EventData("event.detail.dropLocation") String dropLocation) {
        super(source, fromClient);

        this.dropTarget = tabId != null
                ? source.findTab(tabId.getString()).orElse(null)
                : null;
        this.dragSource = getActiveDragSourceComponent() instanceof Tab tab ? tab : null;

        MainTabsheetDropLocation clientDropLocation = MainTabsheetDropLocation.fromClientName(dropLocation);
        this.dropLocation = clientDropLocation != null
                ? clientDropLocation
                : MainTabsheetDropLocation.EMPTY;
    }

    @Nullable
    public Tab getDropTarget() {
        return dropTarget;
    }

    @Nullable
    public Tab getDragSource() {
        return dragSource;
    }

    public MainTabsheetDropLocation getDropLocation() {
        return dropLocation;
    }

    protected Component getActiveDragSourceComponent() {
        return getSource().getUI()
                .orElseThrow(() -> new IllegalStateException(
                        "Drop target received a drop event but not attached to an UI"
                ))
                .getActiveDragSourceComponent();
    }
}
