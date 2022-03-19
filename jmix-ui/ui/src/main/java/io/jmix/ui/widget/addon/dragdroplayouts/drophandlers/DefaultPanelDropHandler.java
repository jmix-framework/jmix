/*
 * Copyright 2015 John Ahlroos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.ui.widget.addon.dragdroplayouts.drophandlers;

import io.jmix.ui.widget.addon.dragdroplayouts.DDPanel;
import io.jmix.ui.widget.addon.dragdroplayouts.events.LayoutBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.SingleComponentContainer;

public class DefaultPanelDropHandler extends AbstractDefaultLayoutDropHandler {

    @Override
    protected void handleComponentReordering(DragAndDropEvent event) {
        handleDropFromLayout(event);
    }

    @Override
    protected void handleDropFromLayout(DragAndDropEvent event) {
        LayoutBoundTransferable transferable = (LayoutBoundTransferable) event
                .getTransferable();
        DDPanel.PanelTargetDetails details = (DDPanel.PanelTargetDetails) event
                .getTargetDetails();
        Component component = transferable.getComponent();
        DDPanel panel = (DDPanel) details.getTarget();

        // Detach from old source
        Component source = transferable.getSourceComponent();
        if (source instanceof ComponentContainer) {
            ((ComponentContainer) source).removeComponent(component);
        } else if (source instanceof SingleComponentContainer) {
            ((SingleComponentContainer) source).setContent(null);
        }

        // Attach to new source
        panel.setContent(component);
    }

    @Override
    protected void handleHTML5Drop(DragAndDropEvent event) {
        DDPanel.PanelTargetDetails details = (DDPanel.PanelTargetDetails) event
                .getTargetDetails();
        DDPanel panel = (DDPanel) details.getTarget();
        panel.setContent(resolveComponentFromHTML5Drop(event));
    }

    @Override
    public Class<Panel> getTargetLayoutType() {
        return Panel.class;
    }
}
