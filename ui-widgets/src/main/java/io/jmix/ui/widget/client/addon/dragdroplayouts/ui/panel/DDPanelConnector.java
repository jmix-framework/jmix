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
package io.jmix.ui.widget.client.addon.dragdroplayouts.ui.panel;

import io.jmix.ui.widget.addon.dragdroplayouts.DDPanel;
import io.jmix.ui.widget.client.addon.dragdroplayouts.VDragFilter;
import io.jmix.ui.widget.client.addon.dragdroplayouts.VGrabFilter;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.VDragCaptionProvider;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.VDragDropUtil;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.interfaces.VHasDragCaptionProvider;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.interfaces.VHasDragFilter;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.interfaces.VHasGrabFilter;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.util.HTML5Support;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.UIDL;
import com.vaadin.client.ui.panel.PanelConnector;
import com.vaadin.shared.ui.Connect;

@Connect(value = DDPanel.class, loadStyle = Connect.LoadStyle.LAZY)
public class DDPanelConnector extends PanelConnector implements VHasDragFilter,
        VHasGrabFilter, VHasDragCaptionProvider {

    private HTML5Support html5Support;

    @Override
    public void init() {
        super.init();
        VDragDropUtil.listenToStateChangeEvents(this, getWidget());
    }

    @Override
    public void setDragCaptionProvider(VDragCaptionProvider dragCaption) {
        getWidget().setDragCaptionProvider(dragCaption);
    }

    @Override
    public VDragCaptionProvider getDragCaptionProvider() {
        return getWidget().getDragCaptionProvider();
    }

    @Override
    public VGrabFilter getGrabFilter() {
        return getWidget().getGrabFilter();
    }

    @Override
    public void setGrabFilter(VGrabFilter grabFilter) {
        getWidget().setGrabFilter(grabFilter);
    }

    @Override
    public DDPanelState getState() {
        return (DDPanelState) super.getState();
    }

    @Override
    public VDDPanel getWidget() {
        return (VDDPanel) super.getWidget();
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);
        VDragDropUtil.updateDropHandlerFromUIDL(uidl, this, new VDDPanelDropHandler(this));
        if (html5Support != null) {
            html5Support.disable();
            html5Support = null;
        }
        VDDPanelDropHandler dropHandler = getWidget().getDropHandler();
        if (dropHandler != null) {
            html5Support = HTML5Support.enable(this, dropHandler);
        }
    }

    @Override
    public void onUnregister() {
        if (html5Support != null) {
            html5Support.disable();
            html5Support = null;
        }
        super.onUnregister();
    }

    @Override
    public VDragFilter getDragFilter() {
        return getWidget().getDragFilter();
    }

    @Override
    public void setDragFilter(VDragFilter filter) {
        getWidget().setDragFilter(filter);
    }
}
