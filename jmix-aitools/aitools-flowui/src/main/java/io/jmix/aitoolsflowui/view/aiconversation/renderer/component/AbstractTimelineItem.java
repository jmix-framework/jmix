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

package io.jmix.aitoolsflowui.view.aiconversation.renderer.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractTimelineItem extends Composite<HorizontalLayout> {

    protected static final String BASE_CN = "timeline-message-row";
    protected static final String HEADER_CN = "timeline-message-header";
    protected static final String BODY_CN = "timeline-message-body";
    protected static final String ACTOR_CN = "timeline-message-actor";
    protected static final String TIME_CN = "timeline-message-time";
    protected static final String AVATAR_CONTAINER_CN = "timeline-avatar-container";

    protected Div avatarContainer;
    protected VerticalLayout body;
    protected HorizontalLayout header;
    protected Span actor;
    protected Span time;

    public void setTime(String time) {
        if (this.time != null) {
            this.time.setText(time);
        }
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

    protected VerticalLayout createBody() {
        VerticalLayout body = new VerticalLayout();
        body.setPadding(false);
        body.setSpacing(false);
        body.setWidth("95%"); // TODO: pinyazhim
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
}
