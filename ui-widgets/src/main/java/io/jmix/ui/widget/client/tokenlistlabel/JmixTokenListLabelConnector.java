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

package io.jmix.ui.widget.client.tokenlistlabel;

import io.jmix.ui.widget.JmixTokenListLabel;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.csslayout.CssLayoutConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Is used for TokenList from compatibility module.
 */
@Connect(JmixTokenListLabel.class)
public class JmixTokenListLabelConnector extends CssLayoutConnector {

    @Override
    public JmixTokenListLabelWidget getWidget() {
        return (JmixTokenListLabelWidget) super.getWidget();
    }

    @Override
    public void init() {
        super.init();

        getWidget().handler = new JmixTokenListLabelWidget.TokenListLabelHandler() {
            @Override
            public void remove() {
                getRpcProxy(JmixTokenListLabelServerRpc.class).removeToken();
            }

            @Override
            public void click() {
                getRpcProxy(JmixTokenListLabelServerRpc.class).itemClick();
            }
        };
    }

    @Override
    public JmixTokenListLabelState getState() {
        return (JmixTokenListLabelState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("editable")) {
            getWidget().setEditable(getState().editable);
        }

        if (stateChangeEvent.hasPropertyChanged("canOpen")) {
            getWidget().setCanOpen(getState().canOpen);
        }

        if (stateChangeEvent.hasPropertyChanged("text")) {
            getWidget().setText(getState().text);
        }
    }
}
