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

package io.jmix.ui.widget.client.tabsheet;

import io.jmix.ui.widget.JmixMainTabSheet;
import io.jmix.ui.widget.client.action.RemoteAction;
import io.jmix.ui.widget.client.action.StaticActionOwner;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.VDragDropUtil;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.tabsheet.DDTabsheetConnector;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.tabsheet.VDDTabsheetDropHandler;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.UIDL;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.Action;
import com.vaadin.shared.ui.Connect;

@Connect(JmixMainTabSheet.class)
public class JmixMainTabSheetConnector extends DDTabsheetConnector {

    protected JmixMainTabSheetServerRpc rpc = RpcProxy.create(JmixMainTabSheetServerRpc.class, this);

    protected int lastContextMenuX = -1;
    protected int lastContextMenuY = -1;

    public JmixMainTabSheetConnector() {
        //noinspection Convert2Lambda
        registerRpc(JmixMainTabSheetClientRpc.class, new JmixMainTabSheetClientRpc() {
            @Override
            public void showTabContextMenu(final int tabIndex, ClientAction[] actions) {
                StaticActionOwner actionOwner = new StaticActionOwner(getConnection(), getConnectorId());

                Action[] contextMenuActions = new Action[actions.length];

                for (int i = 0; i < contextMenuActions.length; i++) {
                    contextMenuActions[i] = new RemoteAction(actions[i], actionOwner) {
                        @Override
                        public void execute() {
                            rpc.performAction(tabIndex, actionId);

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
    public JmixMainTabSheetWidget getWidget() {
        return (JmixMainTabSheetWidget) super.getWidget();
    }

    @Override
    protected void init() {
        super.init();

        getWidget().tabContextMenuHandler = (tabIndex, event) -> {
            lastContextMenuX = WidgetUtil.getTouchOrMouseClientX(event.getNativeEvent());
            lastContextMenuY = WidgetUtil.getTouchOrMouseClientY(event.getNativeEvent());

            if (getState().hasActionsHandlers) {
                rpc.onTabContextMenu(tabIndex);

                event.stopPropagation();
                event.preventDefault();
            }
        };
    }

    @Override
    public JmixMainTabSheetState getState() {
        return (JmixMainTabSheetState) super.getState();
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        VDragDropUtil.updateDropHandlerFromUIDL(uidl, this, new VDDTabsheetDropHandler(this));
        if (getState().ddHtmlEnable) {
            enableDDHtml5();
        }
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        getWidget().assignAdditionalCellStyles();

        if (stateChangeEvent.hasPropertyChanged("ddHtmlEnable")) {
            if (getState().ddHtmlEnable) {
                enableDDHtml5();
            } else {
                disableDDHtml5();
            }
        }
    }
}
