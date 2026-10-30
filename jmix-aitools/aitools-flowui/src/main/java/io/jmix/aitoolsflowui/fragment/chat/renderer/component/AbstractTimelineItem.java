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

package io.jmix.aitoolsflowui.fragment.chat.renderer.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableConsumer;
import io.jmix.core.Messages;
import org.jspecify.annotations.Nullable;

/**
 * Base layout for a single timeline message row.
 */
public abstract class AbstractTimelineItem extends Composite<HorizontalLayout> {

    protected static final String BASE_CN = "timeline-message-row";
    protected static final String HEADER_CN = "timeline-message-header";
    protected static final String BODY_CN = "timeline-message-body";
    protected static final String ACTOR_CN = "timeline-message-actor";
    protected static final String TIME_CN = "timeline-message-time";
    protected static final String AVATAR_CONTAINER_CN = "timeline-avatar-container";
    protected static final String ACTIONS_CN = "timeline-message-actions";
    protected static final String KEEP_BUTTON_CN = "timeline-keep-button";
    protected static final String KEEP_BUTTON_KEPT_CN = "timeline-keep-button-kept";

    protected Div avatarContainer;
    protected VerticalLayout body;
    protected HorizontalLayout header;
    protected Span actor;
    protected Span time;
    protected HorizontalLayout actions;

    protected Button keepButton;
    @Nullable
    protected SerializableConsumer<Boolean> keepClickHandler;

    protected boolean kept;

    /**
     * Sets the timestamp text shown in the row header.
     *
     * @param time formatted timestamp (empty string if unknown)
     */
    public void setTime(String time) {
        if (this.time != null) {
            this.time.setText(time);
        }
    }

    /**
     * Sets whether the message of the row is kept in the assistant memory, which the pin control shows.
     *
     * @param kept {@code true} if the message is kept
     */
    public void setKept(boolean kept) {
        this.kept = kept;

        keepButton.setClassName(KEEP_BUTTON_KEPT_CN, kept);

        String description = getMessages().getMessage(kept
                ? "assistantMemory.unpinDescription"
                : "assistantMemory.pinDescription");
        keepButton.setAriaLabel(description);
        keepButton.setTooltipText(description);
    }

    /**
     * Sets the handler of a click on the pin control. A reused row shows another message, so the handler replaces
     * the previous one instead of adding to it.
     *
     * @param keepClickHandler receives the requested new state of the message, or {@code null} to remove the handler
     */
    public void setKeepClickHandler(@Nullable SerializableConsumer<Boolean> keepClickHandler) {
        this.keepClickHandler = keepClickHandler;
    }

    /**
     * Sets whether the pin control is shown under the message.
     *
     * @param visible {@code true} to show the control
     */
    public void setKeepVisible(boolean visible) {
        keepButton.setVisible(visible);
    }

    protected Button createKeepButton() {
        Button button = new Button(VaadinIcon.LIGHTBULB.create());
        button.addClassName(KEEP_BUTTON_CN);
        button.addThemeVariants(ButtonVariant.SMALL, ButtonVariant.TERTIARY);
        button.addClickListener(e -> {
            if (keepClickHandler != null) {
                keepClickHandler.accept(!kept);
            }
        });
        return button;
    }

    protected void initRow(String actorName) {
        getContent().removeAll();
        body.removeAll();
        body.add(header);

        actor.setText(actorName);

        avatarContainer.removeAll();
        avatarContainer.add(createAvatar(actorName));

        getContent().add(avatarContainer, body);
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout root = createRoot();

        body = createBody();
        header = createHeader();

        actor = createActor();
        time = createTime();
        header.add(actor, time);

        keepButton = createKeepButton();
        actions = createActions();
        actions.add(keepButton);

        avatarContainer = createAvatarContainer();

        return root;
    }

    protected HorizontalLayout createRoot() {
        HorizontalLayout root = new HorizontalLayout();
        root.setWidthFull();
        root.setSpacing(true);
        root.setPadding(false);
        root.setAlignItems(FlexComponent.Alignment.START);
        root.addClassName(BASE_CN);
        return root;
    }

    protected HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setPadding(false);
        header.setSpacing(true);
        header.setAlignItems(FlexComponent.Alignment.BASELINE);
        header.addClassName(HEADER_CN);
        return header;
    }

    protected HorizontalLayout createActions() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setPadding(false);
        actions.setSpacing(false);
        actions.addClassName(ACTIONS_CN);
        return actions;
    }

    protected VerticalLayout createBody() {
        VerticalLayout body = new VerticalLayout();
        body.setPadding(false);
        body.setSpacing(false);
        body.setWidthFull();
        body.addClassName(BODY_CN);
        return body;
    }

    protected Span createActor() {
        Span actor = new Span();
        actor.addClassName(ACTOR_CN);
        return actor;
    }

    protected Span createTime() {
        Span time = new Span();
        time.addClassName(TIME_CN);
        return time;
    }

    protected Div createAvatarContainer() {
        Div avatarContainer = new Div();
        avatarContainer.addClassName(AVATAR_CONTAINER_CN);
        return avatarContainer;
    }

    protected abstract Component createAvatar(String actorName);

    protected abstract Messages getMessages();
}
