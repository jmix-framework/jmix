/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.component.tabsheet;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.shared.Registration;
import org.springframework.lang.Nullable;

@Tag("jmix-view-tab")
@JsModule("./src/tabsheet/jmix-view-tab.js")
public class JmixViewTab extends Tab {

    protected static final String BASE_CLASS_NAME = "jmix-view-tab";

    protected HasText textElement;

    protected Component closeButton;
    protected boolean closable = false;

    public JmixViewTab() {
    }

    public JmixViewTab(String text) {
        setText(text);
    }

    public JmixViewTab(Component... components) {
        super(components);
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Nullable
    public String getText() {
        return textElement != null ? textElement.getText() : null;
    }

    /**
     * Sets the label of this tab.
     *
     * @param text the label to display
     */
    // TODO: gg, refactor
    public void setText(@Nullable String text) {
        if (!Strings.isNullOrEmpty(text)) {
            if (this.textElement == null) {
                this.textElement = createTextElement();
                getElement().appendChild(this.textElement.getElement());
            }

            this.textElement.setText(text);
        } else if (this.textElement != null) {
            this.textElement.getElement().removeFromParent();
            this.textElement = null;
        }
    }

    private HasText createTextElement() {
        Span span = new Span();
        span.setClassName(BASE_CLASS_NAME + "-text");

        return span;
    }

    public boolean isClosable() {
        return closable;
    }

    public void setClosable(boolean closable) {
        if (this.closable != closable) {
            this.closable = closable;

            if (closeButton != null) {
                remove(closeButton);
                closeButton = null;
            }

            if (closable) {
                this.closeButton = createCloseButton();
                SlotUtils.setSlot(this, "suffix", closeButton);
            }
        }
    }

    protected Component createCloseButton() {
        // TODO: gg, uiComponents?
        Button closeButton = new Button();
        closeButton.setIcon(new Icon(VaadinIcon.CLOSE_SMALL));
        closeButton.setClassName(BASE_CLASS_NAME + "-close-button");
        closeButton.addClickListener(this::onCloseButtonClicked);

        return closeButton;
    }

    protected void onCloseButtonClicked(ClickEvent<Button> event) {
        closeInternal(event.isFromClient());
    }

    protected void closeInternal(boolean fromClient) {
        fireEvent(new BeforeCloseEvent<>(this, fromClient));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addBeforeCloseListener(ComponentEventListener<BeforeCloseEvent<JmixViewTab>> listener) {
        return addListener(BeforeCloseEvent.class, (ComponentEventListener) listener);
    }

    // TODO: gg, rename
    public static class BeforeCloseEvent<C extends Component> extends ComponentEvent<C> {

        // TODO: gg, add usage
        protected boolean closePrevented = false;

        public BeforeCloseEvent(C source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
