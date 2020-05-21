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

package io.jmix.ui.widget.client.button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.button.ButtonConnector;
import com.vaadin.shared.ui.Connect;
import io.jmix.ui.widget.JmixButton;

@Connect(value = JmixButton.class)
public class JmixButtonConnector extends ButtonConnector {

    public static final String ICON_STYLE = "icon";

    protected boolean pendingResponse = false;

    public JmixButtonConnector() {
        //noinspection Convert2Lambda,Anonymous2MethodRef
        registerRpc(JmixButtonClientRpc.class, new JmixButtonClientRpc() {
            @Override
            public void onClickHandled() {
                stopResponsePending();
            }
        });
    }

    @Override
    public JmixButtonState getState() {
        return (JmixButtonState) super.getState();
    }

    @Override
    public JmixButtonWidget getWidget() {
        return (JmixButtonWidget) super.getWidget();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        if (getState().useResponsePending) {
            stopResponsePending();
        }

        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("caption")) {
            String text = getState().caption;
            if (text == null || "".equals(text)) {
                getWidget().addStyleDependentName("empty-caption");
            } else {
                getWidget().removeStyleDependentName("empty-caption");
            }
        }

        if (getIconUri() != null) {
            getWidget().addStyleName(ICON_STYLE);
        } else {
            getWidget().removeStyleName(ICON_STYLE);
        }
    }

    @Override
    public void onClick(ClickEvent event) {
        if (pendingResponse) {
            return;
        }

        if (getState().useResponsePending) {
            startResponsePending();
        }

        super.onClick(event);
    }

    public void stopResponsePending() {
        pendingResponse = false;
        getWidget().removeStyleDependentName("wait");
    }

    protected void startResponsePending() {
        pendingResponse = true;
        getWidget().addStyleDependentName("wait");
    }
}
