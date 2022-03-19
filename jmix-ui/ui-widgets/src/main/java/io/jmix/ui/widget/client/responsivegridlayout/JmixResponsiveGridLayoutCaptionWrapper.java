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

import com.google.gwt.user.client.ui.FlowPanel;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import io.jmix.ui.widget.client.caption.JmixCaptionWidget;

public class JmixResponsiveGridLayoutCaptionWrapper extends FlowPanel {

    public static final String CLASSNAME = JmixResponsiveGridLayoutWidget.CLASSNAME + "-caption-wrapper";

    protected JmixCaptionWidget caption;
    protected ComponentConnector wrappedConnector;

    protected boolean captionPlacedAfterComponent;

    public JmixResponsiveGridLayoutCaptionWrapper(ComponentConnector toBeWrapped, ApplicationConnection client) {
        caption = createCaption(toBeWrapped, client);
        initWrapper(caption, toBeWrapped);
    }

    protected void initWrapper(JmixCaptionWidget caption, ComponentConnector toBeWrapped) {
        setStyleName(CLASSNAME);

        add(caption);

        captionPlacedAfterComponent = caption.isCaptionPlacedAfterComponentByDefault();
        addStyleName(getCaptionPositionStyle());

        wrappedConnector = toBeWrapped;
        add(wrappedConnector.getWidget());
    }

    protected JmixCaptionWidget createCaption(ComponentConnector toBeWrapped, ApplicationConnection client) {
        return new JmixCaptionWidget(toBeWrapped, client);
    }

    public void updateCaption() {
        caption.updateCaption();

        updateCaptionPosition();
    }

    protected void updateCaptionPosition() {
        if (captionPlacedAfterComponent == caption.shouldBePlacedAfterComponent()) {
            return;
        }

        removeStyleName(getCaptionPositionStyle());

        captionPlacedAfterComponent = caption.shouldBePlacedAfterComponent();

        addStyleName(getCaptionPositionStyle());
    }

    protected String getCaptionPositionStyle() {
        return "jmix-caption-on-" +
                (captionPlacedAfterComponent ? "right" : "top");
    }

    public JmixCaptionWidget getCaption() {
        return caption;
    }

    public ComponentConnector getWrappedConnector() {
        return wrappedConnector;
    }
}
