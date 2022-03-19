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

package io.jmix.ui.widget.client.grid;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class JmixGridEmptyState implements EventListener {

    protected Runnable linkClickHandler;

    protected DivElement container;
    protected DivElement messageBox;
    protected DivElement messageLabel;

    protected SpanElement linkMessageLabel;

    public JmixGridEmptyState() {
        container = Document.get().createDivElement();
        container.setClassName("jmix-datagrid-empty-state");

        messageBox = Document.get().createDivElement();
        messageBox.setClassName("jmix-datagrid-empty-state-message-box");

        messageLabel = Document.get().createDivElement();
        messageLabel.setClassName("jmix-datagrid-empty-state-message");

        linkMessageLabel = Document.get().createSpanElement();
        linkMessageLabel.setClassName("jmix-datagrid-empty-state-link-message v-button-link");

        container.appendChild(messageBox);

        Event.sinkEvents(container, Event.ONCLICK);
        Event.setEventListener(container, this);
    }

    public void setMessage(String message) {
        messageLabel.setInnerText(message);

        if (message == null || message.isEmpty()) {
            messageLabel.removeFromParent();
        } else if (!messageLabel.getParentElement().equals(messageBox)) {
            messageBox.appendChild(messageLabel);
        }
    }

    public void setLinkMessage(String message) {
        linkMessageLabel.setInnerText(message);

        if (message == null || message.isEmpty()) {
            linkMessageLabel.removeFromParent();
        } else if (!linkMessageLabel.getParentElement().equals(messageBox)) {
            messageBox.appendChild(linkMessageLabel);
        }
    }

    public void setLinkClickHandler(Runnable linkClickHandler) {
        this.linkClickHandler = linkClickHandler;
    }

    public Element getElement() {
        return container;
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCLICK) {
            Element fromElement = Element.as(event.getEventTarget());

            if (linkMessageLabel.isOrHasChild(fromElement) && linkClickHandler != null) {
                linkClickHandler.run();
            }
        }
    }
}
