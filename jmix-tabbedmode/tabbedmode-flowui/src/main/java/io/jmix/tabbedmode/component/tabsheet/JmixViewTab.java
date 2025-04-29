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
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.shared.Registration;
import org.springframework.lang.Nullable;

@Tag("jmix-view-tab")
@JsModule("./src/tabsheet/jmix-view-tab.js")
@CssImport("./src/tabsheet/jmix-view-tab.css")
public class JmixViewTab extends Tab implements DragSource<JmixViewTab> {

    protected static final String BASE_CLASS_NAME = "jmix-view-tab";

    protected HasText textElement;

    protected Component closeButton;
    protected boolean closable = false;

    public JmixViewTab() {
        initComponent();
    }

    public JmixViewTab(String text) {
        setText(text);
    }

    public JmixViewTab(Component... components) {
        super(components);
    }

    protected void initComponent() {
        setEffectAllowed(EffectAllowed.MOVE);
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
        fireEvent(new CloseEvent<>(this, fromClient));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addCloseListener(ComponentEventListener<CloseEvent<JmixViewTab>> listener) {
        return addListener(CloseEvent.class, (ComponentEventListener) listener);
    }

    public static class CloseEvent<C extends Component> extends ComponentEvent<C> {

        public CloseEvent(C source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @Override
    public String toString() {
        return "Tab{" + getText() + "}";
    }
}
