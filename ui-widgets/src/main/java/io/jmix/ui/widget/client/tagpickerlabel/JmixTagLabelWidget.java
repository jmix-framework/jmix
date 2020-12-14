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

package io.jmix.ui.widget.client.tagpickerlabel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.vaadin.client.StyleConstants;

public class JmixTagLabelWidget extends FlowPanel {

    public static final String TAGLABEL_STYLENAME = "jmix-taglabel";
    public static final String LABEL_CONTENT_STYLENAME = "jmix-taglabel-content";
    public static final String CLOSE_STYLENAME = "jmix-taglabel-close";

    protected Label label = new Label();
    protected Element closeDiv = DOM.createDiv();

    protected boolean editable;
    protected boolean clickable;

    protected Runnable itemClickHandler;
    protected Runnable removeItemHandler;

    public JmixTagLabelWidget() {
        setStyleName(TAGLABEL_STYLENAME);
        addStyleName(StyleConstants.UI_WIDGET);

        initLabel();
        add(label);

        initRemoveBtn();
        getElement().appendChild(closeDiv);
        DOM.sinkEvents(closeDiv, Event.ONCLICK);
    }

    protected void initLabel() {
        label.setStyleName(LABEL_CONTENT_STYLENAME);
        label.addClickHandler(event -> {
            if (clickable) {
                itemClickHandler.run();
            }
        });
    }

    protected void initRemoveBtn() {
        closeDiv.setClassName(CLOSE_STYLENAME);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        if (!editable) {
            getElement().addClassName("noedit");
        } else {
            getElement().removeClassName("noedit");
        }
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
        if (clickable)
            getElement().addClassName("clickable");
        else
            getElement().removeClassName("clickable");
    }

    public void setText(String text) {
        label.setText(text);
    }

    public Runnable getItemClickHandler() {
        return itemClickHandler;
    }

    public void setItemClickHandler(Runnable itemClickHandler) {
        this.itemClickHandler = itemClickHandler;
    }

    public Runnable getRemoveItemHandler() {
        return removeItemHandler;
    }

    public void setRemoveItemHandler(Runnable removeItemHandler) {
        this.removeItemHandler = removeItemHandler;
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONCLICK && removeItemHandler != null) {
            if (DOM.eventGetTarget(event) == closeDiv && editable) {
                removeItemHandler.run();
            }
        }
    }
}
