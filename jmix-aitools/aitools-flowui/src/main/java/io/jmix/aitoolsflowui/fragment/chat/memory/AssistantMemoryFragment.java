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

package io.jmix.aitoolsflowui.fragment.chat.memory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.aitoolsflowui.fragment.chat.support.ActorNameResolver;
import io.jmix.aitoolsflowui.fragment.chat.support.ChatDateTimeSupport;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * The assistant memory of a conversation: the messages the assistant keeps in every reply, as cards with the author,
 * the first lines of the text and the time, and an explanation of the feature when the memory is empty.
 * <p>
 * The fragment shows what it is given and changes nothing itself: a click on a card, the remove control of a card and
 * the close control are reported to the host through handlers.
 */
@Experimental
@NullMarked
@FragmentDescriptor("assistant-memory-fragment.xml")
public class AssistantMemoryFragment extends Fragment<VerticalLayout> {

    protected static final String CHAT_MESSAGE_GROUP = "io.jmix.aitoolsflowui.fragment.chat";

    protected static final int TEXT_EXCERPT_MAX_LENGTH = 300;

    protected static final String CARD_CN = "assistant-memory-card";
    protected static final String CARD_ROW_CN = CARD_CN + "-row";
    protected static final String CARD_BODY_CN = CARD_CN + "-body";
    protected static final String CARD_HEADER_CN = CARD_CN + "-header";
    protected static final String CARD_ACTOR_CN = CARD_CN + "-actor";
    protected static final String CARD_TIME_CN = CARD_CN + "-time";
    protected static final String CARD_TEXT_CN = CARD_CN + "-text";
    protected static final String CARD_MARKDOWN_CN = CARD_CN + "-markdown";
    protected static final String CARD_REMOVE_CN = CARD_CN + "-remove";

    @ViewComponent
    protected Span memoryEmpty;
    @ViewComponent
    protected VerticalLayout memoryList;

    @Autowired
    protected Messages messages;
    @Autowired
    protected Icons icons;
    @Autowired
    protected ActorNameResolver actorNameResolver;
    @Autowired
    protected ChatDateTimeSupport chatDateTimeSupport;

    protected List<AiChatMessage> keptMessages = List.of();

    protected boolean readOnly;

    @Nullable
    protected SerializableConsumer<AiChatMessage> messageClickHandler;
    @Nullable
    protected SerializableConsumer<AiChatMessage> removeHandler;
    @Nullable
    protected SerializableRunnable closeHandler;

    /**
     * Shows the messages kept in the assistant memory, replacing the shown ones.
     *
     * @param keptMessages the kept messages, oldest first
     */
    public void setMessages(Collection<AiChatMessage> keptMessages) {
        this.keptMessages = List.copyOf(keptMessages);
        renderCards();
    }

    /**
     * Hides the remove control of the cards, for a chat that must not change what the assistant remembers. The list
     * stays available.
     *
     * @param readOnly {@code true} to hide the remove control
     */
    public void setReadOnly(boolean readOnly) {
        if (this.readOnly != readOnly) {
            this.readOnly = readOnly;
            renderCards();
        }
    }

    /**
     * Sets the handler of a click on a card, e.g. to bring the message into view in the chat.
     *
     * @param messageClickHandler receives the message of the clicked card, or {@code null} to remove the handler
     */
    public void setMessageClickHandler(@Nullable SerializableConsumer<AiChatMessage> messageClickHandler) {
        this.messageClickHandler = messageClickHandler;
    }

    /**
     * Sets the handler of the remove control of a card. The handler releases the message from the memory and gives
     * the fragment the updated list.
     *
     * @param removeHandler receives the message to remove, or {@code null} to remove the handler
     */
    public void setRemoveHandler(@Nullable SerializableConsumer<AiChatMessage> removeHandler) {
        this.removeHandler = removeHandler;
    }

    /**
     * Sets the handler of the close control, which the host uses to hide the fragment.
     *
     * @param closeHandler handler of the close control, or {@code null} to remove it
     */
    public void setCloseHandler(@Nullable SerializableRunnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        renderCards();
    }

    @Subscribe("memoryCloseBtn")
    public void onMemoryCloseBtnClick(final ClickEvent<JmixButton> event) {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    protected void renderCards() {
        memoryList.removeAll();
        for (AiChatMessage message : keptMessages) {
            memoryList.add(createCard(message));
        }
        memoryEmpty.setVisible(keptMessages.isEmpty());
        memoryList.setVisible(!keptMessages.isEmpty());
    }

    protected Component createCard(AiChatMessage message) {
        HorizontalLayout row = new HorizontalLayout(createCardBody(message));
        row.setWidthFull();
        row.setPadding(false);
        row.setSpacing(false);
        row.setAlignItems(FlexComponent.Alignment.START);
        row.addClassName(CARD_ROW_CN);
        if (!readOnly) {
            row.add(createRemoveButton(message));
        }

        Card card = new Card();
        card.setWidthFull();
        card.addThemeVariants(CardVariant.OUTLINED);
        card.addClassName(CARD_CN);
        card.add(row);
        // A click anywhere on the card except its remove button shows the message.
        card.getElement()
                .addEventListener("click", clickEvent -> onCardClick(message))
                .setFilter("!event.target.closest('." + CARD_REMOVE_CN + "')");
        return card;
    }

    /**
     * Creates the body of a card. It acts as a button for the keyboard: the card itself takes mouse clicks, but a
     * button inside a button is not allowed, so the remove button stays outside of it.
     *
     * @param message the kept message
     * @return the card body
     */
    protected Div createCardBody(AiChatMessage message) {
        Div body = new Div(createCardHeader(message), createCardText(message));
        body.addClassName(CARD_BODY_CN);
        body.getElement().setAttribute("role", "button");
        body.getElement().setAttribute("tabindex", "0");
        body.getElement()
                .addEventListener("keydown", keyEvent -> onCardClick(message))
                .setFilter("(event.key === 'Enter' || event.key === ' ') && (event.preventDefault() || true)");
        return body;
    }

    protected Div createCardHeader(AiChatMessage message) {
        Span actor = new Span(resolveActorName(message));
        actor.addClassName(CARD_ACTOR_CN);

        Span time = new Span(chatDateTimeSupport.formatInUserZone(message.getCreatedDate()));
        time.addClassName(CARD_TIME_CN);

        Div header = new Div(actor, time);
        header.addClassName(CARD_HEADER_CN);
        return header;
    }

    /**
     * Creates the preview of the message text. An assistant message is Markdown, as in the chat, so that a table
     * or a list reads as one; a user message is plain text. The Markdown is not cut, since a cut could break a table
     * or a code block; the card shows only its beginning.
     *
     * @param message the kept message
     * @return the preview
     */
    protected Component createCardText(AiChatMessage message) {
        if (message.getType() == AiChatMessageType.ASSISTANT) {
            String content = message.getContent();
            Markdown markdown = new Markdown(content != null ? content : "");
            markdown.addClassNames(CARD_TEXT_CN, CARD_MARKDOWN_CN);
            return markdown;
        }

        Span text = new Span(createExcerpt(message.getContent()));
        text.addClassName(CARD_TEXT_CN);
        return text;
    }

    protected Button createRemoveButton(AiChatMessage message) {
        String description = messages.getMessage("assistantMemory.remove");

        Button button = new Button(icons.get(JmixFontIcon.CLOSE));
        button.addClassName(CARD_REMOVE_CN);
        button.addThemeVariants(ButtonVariant.SMALL, ButtonVariant.TERTIARY);
        button.setAriaLabel(description);
        button.setTooltipText(description);
        button.addClickListener(clickEvent -> {
            if (removeHandler != null) {
                removeHandler.accept(message);
            }
        });
        return button;
    }

    protected void onCardClick(AiChatMessage message) {
        if (messageClickHandler != null) {
            messageClickHandler.accept(message);
        }
    }

    /**
     * Resolves the author the way the chat timeline names it: the user's name for a user message, the assistant for
     * an assistant one.
     *
     * @param message the kept message
     * @return the author's display name
     */
    protected String resolveActorName(AiChatMessage message) {
        if (message.getType() == AiChatMessageType.USER) {
            return actorNameResolver.resolve(message,
                    messages.getMessage(CHAT_MESSAGE_GROUP, "timelineItemRenderer.defaultActorName"));
        }
        return messages.getMessage(CHAT_MESSAGE_GROUP, "timelineItemRenderer.assistantName");
    }

    /**
     * Cuts the message text to what a card can show, so that a long message is not sent to the browser in full;
     * the card clamps the excerpt to its first lines.
     *
     * @param content the message text
     * @return the beginning of the text
     */
    protected String createExcerpt(@Nullable String content) {
        if (content == null) {
            return "";
        }
        String trimmed = content.strip();
        if (trimmed.length() <= TEXT_EXCERPT_MAX_LENGTH) {
            return trimmed;
        }
        int end = TEXT_EXCERPT_MAX_LENGTH;
        if (Character.isHighSurrogate(trimmed.charAt(end - 1))) {
            // Do not leave half of a surrogate pair at the cut.
            end--;
        }
        return trimmed.substring(0, end) + "…";
    }
}
