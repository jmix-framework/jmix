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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.StyleConstants;
import com.vaadin.client.VCaption;
import com.vaadin.shared.AbstractComponentState;
import com.vaadin.shared.AbstractFieldState;

import java.util.HashMap;
import java.util.Map;

public class JmixResponsiveGridLayoutWidget extends ComplexPanel {

    public static final String CLASSNAME = "jmix-responsivegridlayout";

    protected static final String ROWS_PROPERTY = "rows";
    protected static final String COLS_PROPERTY = "cols";
    protected static final String STYLE_PROPERTY = "style";
    protected static final String HEIGHT_PROPERTY = "height";
    protected static final String COLUMN_ID_PROPERTY = "columnId";
    protected static final String JTEST_ID_PROPERTY = "jTestId";

    protected Map<String, Element> locationToElement = new HashMap<>();

    protected Map<String, Widget> locationToWidget = new HashMap<>();

    protected Map<Widget, JmixResponsiveGridLayoutCaptionWrapper> childWidgetToCaptionWrapper = new HashMap<>();

    public ApplicationConnection client;

    protected boolean layoutInitialized = false;

    public JmixResponsiveGridLayoutWidget() {
        setElement(Document.get().createDivElement());

        setStyleName(CLASSNAME);
        addStyleName(StyleConstants.UI_LAYOUT);
    }

    public void setWidget(Widget widget, String location) {
        Element element = locationToElement.get(location);
        if (element == null && isLayoutInitialized()) {
            throw new IllegalArgumentException("No location " + location + " found");
        }

        Widget prevWidget = locationToWidget.get(location);
        if (prevWidget == widget) {
            return;
        }

        if (prevWidget != null) {
            remove(prevWidget);
        }

        // if template is missing add element in order
        if (!isLayoutInitialized()) {
            element = getElement();
        }

        // Add widget to location
        super.add(widget, element);
        locationToWidget.put(location, widget);
    }

    @Override
    public boolean remove(Widget widget) {
        String location = getLocation(widget);

        if (location != null) {
            locationToWidget.remove(location);
        }

        JmixResponsiveGridLayoutCaptionWrapper captionWrapper = childWidgetToCaptionWrapper.get(widget);

        if (captionWrapper != null) {
            childWidgetToCaptionWrapper.remove(widget);

            return super.remove(captionWrapper);
        } else if (widget != null) {
            return super.remove(widget);
        }

        return false;
    }

    public String getLocation(Widget widget) {
        for (String location : locationToWidget.keySet()) {
            if (locationToWidget.get(location) == widget) {
                return location;
            }
        }

        return null;
    }

    @Override
    public void clear() {
        super.clear();

        locationToWidget.clear();
        childWidgetToCaptionWrapper.clear();
    }

    public boolean isLayoutInitialized() {
        return layoutInitialized;
    }

    public void generateLayout(JSONObject confObject) {
        getElement().removeAllChildren();
        locationToElement.clear();

        if (confObject.containsKey(ROWS_PROPERTY)) {
            JSONArray rowsObject = confObject.get(ROWS_PROPERTY).isArray();

            for (int i = 0; i < rowsObject.size(); i++) {
                JSONObject rowObject = rowsObject.get(i).isObject();

                Element rowElement = Document.get().createDivElement();
                setupRowElement(rowElement, rowObject);

                getElement().appendChild(rowElement);
            }
        }

        layoutInitialized = true;
    }

    protected void setupRowElement(Element rowElement, JSONObject rowObject) {
        String style = rowObject.get(STYLE_PROPERTY).isString().stringValue();
        rowElement.setClassName(style);

        JSONValue heightValue = rowObject.get(HEIGHT_PROPERTY);
        if (heightValue != null) {
            rowElement.getStyle().setProperty("height", heightValue.isString().stringValue());
        }

        assignJTestId(rowElement, rowObject);

        if (rowObject.containsKey(COLS_PROPERTY)) {
            JSONArray colsObject = rowObject.get(COLS_PROPERTY).isArray();

            for (int i = 0; i < colsObject.size(); i++) {
                JSONObject colObject = colsObject.get(i).isObject();

                Element colElement = Document.get().createDivElement();
                setupColElement(colElement, colObject);

                rowElement.appendChild(colElement);
            }
        }
    }

    protected void setupColElement(Element colElement, JSONObject colObject) {
        String style = colObject.get(STYLE_PROPERTY).isString().stringValue();
        colElement.setClassName(style);
        assignJTestId(colElement, colObject);

        locationToElement.put(colObject.get(COLUMN_ID_PROPERTY).isString().stringValue(), colElement);
    }

    protected void assignJTestId(Element element, JSONObject obj) {
        if (obj.containsKey(JTEST_ID_PROPERTY)) {
            String jTestId = obj.get(JTEST_ID_PROPERTY).isString().stringValue();
            element.setAttribute("j-test-id", jTestId);
        }
    }

    public void updateCaption(ComponentConnector childConnector) {
        Widget widget = childConnector.getWidget();

        if (!widget.isAttached()) {
            // Widget has not been added because the location was not found
            return;
        }

        JmixResponsiveGridLayoutCaptionWrapper wrapper = childWidgetToCaptionWrapper.get(widget);
        if (isCaptionNeeded(childConnector)) {
            if (wrapper == null) {
                // Add a wrapper between the layout and the child widget
                String location = getLocation(widget);
                super.remove(widget);

                wrapper = new JmixResponsiveGridLayoutCaptionWrapper(childConnector, client);
                super.add(wrapper, locationToElement.get(location));

                childWidgetToCaptionWrapper.put(widget, wrapper);
            }
            wrapper.updateCaption();
        } else {
            if (wrapper != null) {
                // Remove the wrapper and add the widget directly to the layout
                String location = getLocation(widget);
                super.remove(wrapper);
                super.add(widget, locationToElement.get(location));

                childWidgetToCaptionWrapper.remove(widget);
            }
        }
    }

    protected boolean isCaptionNeeded(ComponentConnector child) {
        AbstractComponentState state = child.getState();
        return VCaption.isNeeded(child) || (state instanceof AbstractFieldState
                && ((AbstractFieldState) state).contextHelpText != null
                && !((AbstractFieldState) state).contextHelpText.isEmpty());
    }
}
