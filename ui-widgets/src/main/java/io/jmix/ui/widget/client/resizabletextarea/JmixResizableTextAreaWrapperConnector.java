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

package io.jmix.ui.widget.client.resizabletextarea;

import io.jmix.ui.widget.JmixResizableTextAreaWrapper;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.customfield.CustomFieldConnector;
import com.vaadin.shared.ui.Connect;

@Connect(JmixResizableTextAreaWrapper.class)
public class JmixResizableTextAreaWrapperConnector extends CustomFieldConnector {

    @Override
    protected void init() {
        super.init();

        getWidget().resizeHandler = new JmixResizableTextAreaWrapperWidget.ResizeHandler() {
            @Override
            public void handleResize() {
                getLayoutManager().setNeedsMeasure(JmixResizableTextAreaWrapperConnector.this);
            }

            @Override
            public void sizeChanged(String width, String height) {
                getRpcProxy(JmixResizableTextAreaWrapperServerRpc.class).sizeChanged(width, height);
            }

            @Override
            public void textChanged(String text) {
                getRpcProxy(JmixResizableTextAreaWrapperServerRpc.class).textChanged(text);
            }
        };
    }

    @Override
    public JmixResizableTextAreaWrapperState getState() {
        return (JmixResizableTextAreaWrapperState) super.getState();
    }

    @Override
    public JmixResizableTextAreaWrapperWidget getWidget() {
        return (JmixResizableTextAreaWrapperWidget) super.getWidget();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("resizableDirection")) {
            getWidget().setResizableDirection(getState().resizableDirection);
        }
        if (stateChangeEvent.hasPropertyChanged("enabled")) {
            getWidget().setEnabled(isEnabled());
        }
    }
}
