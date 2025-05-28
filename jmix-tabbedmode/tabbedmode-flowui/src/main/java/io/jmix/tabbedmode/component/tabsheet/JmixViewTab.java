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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Tag;
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
import com.vaadin.flow.dom.DomEvent;
import org.springframework.lang.Nullable;

import java.util.function.Consumer;

/**
 * A tab component designed for use in a {@link JmixMainTabSheet}. Provides additional functionality,
 * such as support for tab closing and dragging.
 */
@Tag("jmix-view-tab")
@JsModule("./src/tabsheet/jmix-view-tab.js")
@CssImport("./src/tabsheet/jmix-view-tab.css")
// TODO: gg, rename
public class JmixViewTab extends Tab implements DragSource<JmixViewTab> {

    protected static final String BASE_CLASS_NAME = "jmix-view-tab";

    protected HasText textElement;

    protected Component closeButton;
    protected boolean closable = false;

    protected Consumer<CloseContext<JmixViewTab>> closeDelegate;

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

    /**
     * Returns the label of this tab.
     *
     * @return the label of this tab, or {@code null} if not is set
     */
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

    protected HasText createTextElement() {
        Span span = new Span();
        span.setClassName(BASE_CLASS_NAME + "-text");

        return span;
    }

    /**
     * Returns whether this tab is closable or not.
     *
     * @return {@code true} if the tab is closable, {@code false} otherwise
     */
    public boolean isClosable() {
        return closable;
    }

    /**
     * Sets whether the tab can be closed or not. If set to {@code true}, a close button
     * will be added to the tab. If set to {@code false}, the close button will be removed.
     *
     * @param closable whether the tab should be closable
     */
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
        closeButton.getElement()
                .addEventListener("click", this::onCloseButtonClicked)
                .stopPropagation();

        return closeButton;
    }

    protected void onCloseButtonClicked(DomEvent event) {
        closeInternal();
    }

    protected void closeInternal() {
        closeDelegate.accept(new CloseContext<>(this));
    }

    /**
     * Sets the delegate to handle the close event for this tab.
     *
     * @param delegate a close delegate to set, or {@code null} to remove
     */
    public void setCloseDelegate(@Nullable Consumer<CloseContext<JmixViewTab>> delegate) {
        closeDelegate = delegate;
    }

    /**
     * Represents the context passed to a close delegate when a {@link JmixViewTab} is closed.
     *
     * @param <C> the type of the source, which must extend {@link JmixViewTab}
     */
    public record CloseContext<C extends JmixViewTab>(C source) {
    }

    @Override
    public String toString() {
        return "Tab{" + getText() + "}";
    }
}
