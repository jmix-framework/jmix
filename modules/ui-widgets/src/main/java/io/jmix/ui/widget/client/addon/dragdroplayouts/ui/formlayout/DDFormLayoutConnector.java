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
package io.jmix.ui.widget.client.addon.dragdroplayouts.ui.formlayout;

import io.jmix.ui.widget.addon.dragdroplayouts.DDFormLayout;
import io.jmix.ui.widget.client.addon.dragdroplayouts.VDragFilter;
import io.jmix.ui.widget.client.addon.dragdroplayouts.VGrabFilter;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.VDragCaptionProvider;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.VDragDropUtil;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.interfaces.VHasDragCaptionProvider;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.interfaces.VHasDragFilter;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.interfaces.VHasGrabFilter;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.util.HTML5Support;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.ui.formlayout.FormLayoutConnector;
import com.vaadin.shared.ui.Connect;

@Connect(value = DDFormLayout.class, loadStyle = Connect.LoadStyle.LAZY)
public class DDFormLayoutConnector extends FormLayoutConnector
        implements Paintable, VHasDragFilter, VHasGrabFilter, VHasDragCaptionProvider {

    private HTML5Support html5Support;

    @Override
    public VDDFormLayout getWidget() {
        return (VDDFormLayout) super.getWidget();
    }

    @Override
    public DDFormLayoutState getState() {
        return (DDFormLayoutState) super.getState();
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
    protected void init() {
        super.init();
        VDragDropUtil.listenToStateChangeEvents(this, getWidget());
    }

    @Override
    public VGrabFilter getGrabFilter() {
        return getWidget().getGrabFilter();
    }

    @Override
    public void setGrabFilter(VGrabFilter grabFilter) {
        getWidget().setGrabFilter(grabFilter);
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        VDragDropUtil.updateDropHandlerFromUIDL(uidl, this, new VDDFormLayoutDropHandler(this));
        if (html5Support != null) {
            html5Support.disable();
            html5Support = null;
        }
        VDDFormLayoutDropHandler dropHandler = getWidget().getDropHandler();
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
