/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitoolsflowui.fragment.chat.component;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import io.jmix.aitoolsflowui.fragment.chat.HasAiChatSuggestedPrompts;
import io.jmix.core.annotation.Internal;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * INTERNAL.
 * <p>
 * The suggestions offered to the user of a chat, as a row of small buttons that wraps when it does not fit. Meant for
 * panels that implement {@link HasAiChatSuggestedPrompts}.
 */
@Internal
@NullMarked
public class AiChatSuggestions extends Composite<Div> {

    protected static final String BASE_CN = "jmix-ai-chat-suggestions";
    protected static final String ITEM_CN = "jmix-ai-chat-suggestion";

    protected List<String> items = List.of();

    /**
     * Shows a button for each item, replacing the shown ones. The same items again leave the buttons as they are,
     * so that a repeated call does not take the keyboard focus off a button.
     *
     * @param items items to show, or an empty list to show none
     */
    public void setItems(List<String> items) {
        if (this.items.equals(items)) {
            return;
        }
        this.items = List.copyOf(items);

        getContent().removeAll();
        for (String item : items) {
            getContent().add(createItemButton(item));
        }
    }

    /**
     * Adds a listener called when the user picks an item.
     *
     * @param listener the listener
     * @return a registration that removes the listener
     */
    public Registration addItemClickListener(ComponentEventListener<ItemClickEvent> listener) {
        return addListener(ItemClickEvent.class, listener);
    }

    @Override
    protected Div initContent() {
        Div root = new Div();
        root.addClassName(BASE_CN);
        return root;
    }

    protected Button createItemButton(String item) {
        Button button = new Button(item);
        button.addClassName(ITEM_CN);
        button.addThemeVariants(ButtonVariant.SMALL);
        button.addClickListener(clickEvent -> onItemClick(item, clickEvent.isFromClient()));
        return button;
    }

    protected void onItemClick(String item, boolean fromClient) {
        fireEvent(new ItemClickEvent(this, fromClient, item));
    }

    /**
     * Fired when the user picks one of the suggestions.
     */
    public static class ItemClickEvent extends ComponentEvent<AiChatSuggestions> {

        protected final String item;

        /**
         * Creates the event.
         *
         * @param source     the component the item belongs to
         * @param fromClient whether the event comes from the client side
         * @param item       the picked item
         */
        public ItemClickEvent(AiChatSuggestions source, boolean fromClient, String item) {
            super(source, fromClient);
            this.item = item;
        }

        /**
         * Returns the item the user picked.
         *
         * @return the text of the picked item
         */
        public String getItem() {
            return item;
        }
    }
}
