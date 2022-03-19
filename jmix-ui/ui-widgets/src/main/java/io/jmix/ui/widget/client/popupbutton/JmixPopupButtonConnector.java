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

package io.jmix.ui.widget.client.popupbutton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import io.jmix.ui.widget.JmixPopupButton;
import io.jmix.ui.widget.client.Tools;
import io.jmix.ui.widget.client.addon.popupbutton.PopupButtonConnector;
import io.jmix.ui.widget.client.addon.popupbutton.PopupButtonServerRpc;
import io.jmix.ui.widget.client.jqueryfileupload.JmixFileUploadWidget;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.VButton;
import com.vaadin.client.ui.VUpload;
import com.vaadin.shared.ui.Connect;

import static io.jmix.ui.widget.client.popupbutton.JmixPopupButtonWidget.SELECTED_ITEM_STYLE;

@Connect(JmixPopupButton.class)
public class JmixPopupButtonConnector extends PopupButtonConnector {

    protected PopupButtonServerRpc rpc = RpcProxy.create(PopupButtonServerRpc.class, this);

    @Override
    public JmixPopupButtonState getState() {
        return (JmixPopupButtonState) super.getState();
    }

    @Override
    protected JmixPopupButtonWidget createWidget() {
        return GWT.create(JmixPopupButtonWidget.class);
    }

    @Override
    public JmixPopupButtonWidget getWidget() {
        return (JmixPopupButtonWidget) super.getWidget();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("customLayout")) {
            getWidget().customLayout = getState().customLayout;
        }
    }

    @Override
    public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        if (getWidget().getPopup().isVisible()) {
            Element target = Element.as(nativeEvent.getEventTarget());
            if (getWidget().popupHasChild(target)) {

                if (event.getTypeInt() == Event.ONKEYDOWN
                        && (nativeEvent.getKeyCode() == KeyCodes.KEY_ESCAPE
                            || nativeEvent.getKeyCode() == KeyCodes.KEY_TAB && isLastChild(target))
                        && !nativeEvent.getAltKey()
                        && !nativeEvent.getCtrlKey()
                        && !nativeEvent.getShiftKey()
                        && !nativeEvent.getMetaKey()) {

                    event.cancel();
                    event.getNativeEvent().stopPropagation();
                    event.getNativeEvent().preventDefault();

                    Scheduler.get().scheduleDeferred(() -> {
                        getWidget().hidePopup();

                        rpc.setPopupVisible(false);
                        getWidget().setFocus(true);
                    });

                    return;
                }
            }
        }

        super.onPreviewNativeEvent(event);

        if (isEnabled()) {
            Element target = Element.as(nativeEvent.getEventTarget());
            switch (event.getTypeInt()) {
                case Event.ONCLICK:
                    handleClick(event, target);
                    break;

                case Event.ONKEYDOWN:
                    handleKeyDown(event, target);
                    break;

                case Event.ONMOUSEOVER:
                    handleMouseOver(event, target);
                    break;
            }
        }
    }

    protected void handleClick(@SuppressWarnings("unused") Event.NativePreviewEvent event, Element target) {
        if (getState().autoClose && getWidget().popupHasChild(target)) {
            Scheduler.get().scheduleDeferred(() -> {
                getWidget().hidePopup();

                // update state on server
                rpc.setPopupVisible(false);
            });
        }
    }

    protected void handleMouseOver(@SuppressWarnings("unused") Event.NativePreviewEvent event, Element target) {
        if (!getState().customLayout && getWidget().popupHasChild(target)) {
            Widget widget = WidgetUtil.findWidget(target, null);
            if ((widget instanceof VButton
                    || widget instanceof VUpload
                    || widget instanceof JmixFileUploadWidget)) {

                VButton button;
                if (widget instanceof VButton) {
                    button = (VButton) widget;
                } else if (widget instanceof JmixFileUploadWidget) {
                    button = ((JmixFileUploadWidget) widget).getSubmitButton();
                } else {
                    button = ((VUpload) widget).submitButton;
                }
                if (!button.getStyleName().contains(SELECTED_ITEM_STYLE)) {
                    getWidget().childWidgetFocused(button);
                    button.setFocus(true);
                }
            }
        }
    }

    protected void handleKeyDown(Event.NativePreviewEvent event, Element target) {
        if (!getState().customLayout && getWidget().popupHasChild(target)) {
            Widget widget = WidgetUtil.findWidget(target, null);
            if (widget instanceof VButton
                    || widget instanceof VUpload
                    || widget instanceof JmixFileUploadWidget) {

                Widget widgetParent = widget.getParent();
                if (widgetParent.getParent() instanceof VUpload) {
                    VUpload upload = (VUpload) widgetParent.getParent();
                    widgetParent = upload.getParent();
                } else if (widgetParent.getParent() instanceof JmixFileUploadWidget) {
                    JmixFileUploadWidget upload = (JmixFileUploadWidget) widgetParent.getParent();
                    widgetParent = upload.getParent();
                }

                FlowPanel layout = (FlowPanel) widgetParent;
                Widget focusWidget = null;

                int widgetIndex = layout.getWidgetIndex(widget);
                int keyCode = event.getNativeEvent().getKeyCode();

                if (keyCode == KeyCodes.KEY_DOWN) {
                    focusWidget = Tools.findNextWidget(layout, widgetIndex);
                } else if (keyCode == KeyCodes.KEY_UP) {
                    focusWidget = Tools.findPrevWidget(layout, widgetIndex);
                }

                if (focusWidget instanceof VButton
                        || focusWidget instanceof JmixFileUploadWidget
                        || focusWidget instanceof VUpload) {
                    VButton button;
                    if (focusWidget instanceof VButton) {
                        button = (VButton) focusWidget;
                    } else if (focusWidget instanceof JmixFileUploadWidget) {
                        button = ((JmixFileUploadWidget) focusWidget).getSubmitButton();
                    } else {
                        button = ((VUpload) focusWidget).submitButton;
                    }
                    getWidget().childWidgetFocused(button);
                    button.setFocus(true);
                }
            }
        }
    }

    protected boolean isLastChild(Element target) {
        Widget widget = WidgetUtil.findWidget(target, null);
        Widget widgetParent = widget.getParent();
        FlowPanel layout = (FlowPanel) widgetParent.getParent();
        int widgetIndex = layout.getWidgetIndex(widget);
        return widgetIndex == layout.getWidgetCount() - 1;
    }
}
