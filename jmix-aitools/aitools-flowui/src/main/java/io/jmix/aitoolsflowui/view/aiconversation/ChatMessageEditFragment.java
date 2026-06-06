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

package io.jmix.aitoolsflowui.view.aiconversation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Id;
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewValidation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * Editor for a single {@link ChatMessage}, hosted in the side panel of
 * {@link AiConversationMessagesView}. A fresh instance is created on every open
 * (see the host view), so it always starts with clean field/validation state.
 * <p>
 * Persists immediately via {@link DataManager} (the messages list is decoupled
 * from the conversation editor — see the design spec), then fires {@link SaveEvent}
 * so the host can refresh its list, and closes the side panel.
 */
@FragmentDescriptor("chat-message-edit-fragment.xml")
public class ChatMessageEditFragment extends Fragment<VerticalLayout> {

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected ViewValidation viewValidation;

    @ViewComponent
    protected InstanceContainer<ChatMessage> chatMessageDc;

    protected SidePanelLayout sidePanelLayout;

    public ChatMessageEditFragment withSidePanelLayout(SidePanelLayout sidePanelLayout) {
        this.sidePanelLayout = sidePanelLayout;
        return this;
    }

    /**
     * Prepares a new message bound to the conversation by an id-only reference,
     * so saving the message never rewrites the conversation row.
     */
    public ChatMessageEditFragment withNewItem(UUID conversationId) {
        ChatMessage message = dataManager.create(ChatMessage.class);
        message.setConversation(dataManager.getReference(AiConversation.class, conversationId));
        chatMessageDc.setItem(message);
        return this;
    }

    /**
     * Loads a fresh copy (including the {@code conversation} reference, so the FK
     * is preserved on save) so edits do not touch the grid's instance until save.
     */
    public ChatMessageEditFragment withEditedItem(ChatMessage message) {
        ChatMessage reloaded = dataManager.load(Id.of(message))
                .fetchPlan(builder -> builder.addFetchPlan(FetchPlan.BASE).add("conversation"))
                .one();
        chatMessageDc.setItem(reloaded);
        return this;
    }

    public ChatMessageEditFragment withSaveListener(ComponentEventListener<SaveEvent> listener) {
        addListener(SaveEvent.class, listener);
        return this;
    }

    public void focusFirstField() {
        Component field = getInnerComponent("typeField");
        if (field instanceof Focusable<?> focusable) {
            focusable.focus();
        }
    }

    @Subscribe(id = "saveButton", subject = "clickListener")
    public void onSaveButtonClick(final ClickEvent<JmixButton> event) {
        ValidationErrors errors = viewValidation.validateUiComponents(getContent());
        if (errors.isEmpty()) {
            ChatMessage saved = dataManager.save(chatMessageDc.getItem());
            fireEvent(new SaveEvent(this, event.isFromClient(), saved));
            sidePanelLayout.closeSidePanel();
        } else {
            viewValidation.showValidationErrors(errors);
        }
    }

    @Subscribe(id = "cancelButton", subject = "clickListener")
    public void onCancelButtonClick(final ClickEvent<JmixButton> event) {
        sidePanelLayout.closeSidePanel();
    }

    @Subscribe(id = "closeButton", subject = "clickListener")
    public void onCloseButtonClick(final ClickEvent<JmixButton> event) {
        sidePanelLayout.closeSidePanel();
    }

    /**
     * Fired after a message is persisted; carries the saved instance so the host
     * can reload the loader (create) or replace the item in the container (edit).
     */
    public static class SaveEvent extends ComponentEvent<ChatMessageEditFragment> {

        protected final ChatMessage item;

        public SaveEvent(ChatMessageEditFragment source, boolean fromClient, ChatMessage item) {
            super(source, fromClient);
            this.item = item;
        }

        public ChatMessage getItem() {
            return item;
        }
    }
}
