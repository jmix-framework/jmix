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

package io.jmix.ui.widget.client.window;

import io.jmix.ui.widget.JmixWindow;
import io.jmix.ui.widget.client.action.RemoteAction;
import io.jmix.ui.widget.client.action.StaticActionOwner;
import io.jmix.ui.widget.client.tabsheet.ClientAction;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.Action;
import com.vaadin.client.ui.window.WindowConnector;
import com.vaadin.shared.ui.Connect;

@Connect(JmixWindow.class)
public class JmixWindowConnector extends WindowConnector {

    protected JmixWindowServerRpc rpc = RpcProxy.create(JmixWindowServerRpc.class, this);

    protected int lastContextMenuX = -1;
    protected int lastContextMenuY = -1;

    public JmixWindowConnector() {
        //noinspection Convert2Lambda
        registerRpc(JmixWindowClientRpc.class, new JmixWindowClientRpc() {
            @Override
            public void showTabContextMenu(ClientAction[] actions) {
                StaticActionOwner actionOwner = new StaticActionOwner(getConnection(), getConnectorId());

                Action[] contextMenuActions = new Action[actions.length];

                for (int i = 0; i < contextMenuActions.length; i++) {
                    contextMenuActions[i] = new RemoteAction(actions[i], actionOwner) {
                        @Override
                        public void execute() {
                            rpc.performContextMenuAction(actionId);

                            getConnection().getContextMenu().hide();
                        }
                    };
                }

                actionOwner.setActions(contextMenuActions);

                if (lastContextMenuX >= 0 && lastContextMenuY >= 0) {
                    getConnection().getContextMenu().showAt(actionOwner, lastContextMenuX, lastContextMenuY);

                    lastContextMenuX = -1;
                    lastContextMenuY = -1;
                }
            }
        });
    }

    @Override
    public JmixWindowState getState() {
        return (JmixWindowState) super.getState();
    }

    @Override
    public JmixWindowWidget getWidget() {
        return (JmixWindowWidget) super.getWidget();
    }

    @Override
    protected void init() {
        super.init();

        getWidget().contextMenuHandler = event -> {
            lastContextMenuX = WidgetUtil.getTouchOrMouseClientX(event);
            lastContextMenuY = WidgetUtil.getTouchOrMouseClientY(event);

            if (getState().hasContextActionHandlers) {
                rpc.onWindowContextMenu();

                event.stopPropagation();
                event.preventDefault();
            }
        };
        getWidget().clickOnModalityCurtain = () -> rpc.performCloseAction();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        getWidget().setCloseOnClickOutside(getState().closeOnClickOutside);
    }
}
