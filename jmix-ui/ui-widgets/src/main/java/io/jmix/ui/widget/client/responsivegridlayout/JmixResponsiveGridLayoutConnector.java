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

package io.jmix.ui.widget.client.responsivegridlayout;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.Util;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.client.ui.LayoutClickEventHandler;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.LayoutClickRpc;
import io.jmix.ui.widget.JmixResponsiveGridLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Connect(JmixResponsiveGridLayout.class)
public class JmixResponsiveGridLayoutConnector extends AbstractHasComponentsConnector {

    protected List<String> containerStyles = new ArrayList<>();

    protected boolean layoutUpdated;

    protected LayoutClickEventHandler clickEventHandler = createLayoutClickEventHandler();

    protected LayoutClickEventHandler createLayoutClickEventHandler() {
        return new LayoutClickEventHandler(this) {

            @Override
            protected ComponentConnector getChildComponent(Element element) {
                return Util.getConnectorForElement(getConnection(), getWidget(), element);
            }

            @Override
            protected LayoutClickRpc getLayoutClickRPC() {
                return getRpcProxy(JmixResponsiveGridLayoutServerRpc.class);
            }
        };
    }

    @Override
    public JmixResponsiveGridLayoutWidget getWidget() {
        return (JmixResponsiveGridLayoutWidget) super.getWidget();
    }

    @Override
    public JmixResponsiveGridLayoutState getState() {
        return (JmixResponsiveGridLayoutState) super.getState();
    }

    @Override
    protected void init() {
        super.init();

        getWidget().client = getConnection();
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        JmixResponsiveGridLayoutWidget layout = getWidget();

        updateWidgetLayout();

        for (ComponentConnector child : getChildComponents()) {
            String location = getState().childLocations.get(child);
            try {
                getWidget().setWidget(child.getWidget(), location);
            } catch (final IllegalArgumentException e) {
                // If no location is found, this component is not visible
                getLogger().warn("Child not rendered as no slot with id '"
                        + location + "' has been defined");
            }
        }

        for (ComponentConnector oldChild : event.getOldChildren()) {
            if (oldChild.getParent() == this) {
                continue;
            }

            Widget oldChildWidget = oldChild.getWidget();
            if (oldChildWidget.isAttached()) {
                layout.remove(oldChildWidget);
            }
        }
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        getWidget().updateCaption(connector);
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        clickEventHandler.handleEventHandlerRegistration();

        if (stateChangeEvent.hasPropertyChanged("configuration")) {
            updateWidgetLayout();
        }
    }

    protected void updateWidgetLayout() {
        if (layoutUpdated) {
            return;
        }

        JSONObject confObject = JSONParser.parseStrict(getState().configuration).isObject();

        // Remove all old stylenames
        for (String oldStyle : containerStyles) {
            setWidgetStyleName(oldStyle, false);
        }
        containerStyles.clear();

        if (confObject.containsKey("style")) {
            String containerStyle = confObject.get("style").isString().stringValue();
            String[] newStyles = containerStyle.split(" ");

            for (String newStyle : newStyles) {
                setWidgetStyleName(newStyle, true);
                containerStyles.add(newStyle);
            }
        }

        getWidget().generateLayout(confObject);

        layoutUpdated = true;
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(JmixResponsiveGridLayoutConnector.class);
    }
}
